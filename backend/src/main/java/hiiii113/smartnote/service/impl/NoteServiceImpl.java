package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.CreateNoteDto;
import hiiii113.smartnote.dto.NoteDetailDto;
import hiiii113.smartnote.dto.UpdateNoteDto;
import hiiii113.smartnote.entity.Folder;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.FolderMapper;
import hiiii113.smartnote.mapper.NoteMapper;
import hiiii113.smartnote.service.BrowseHistoryService;
import hiiii113.smartnote.service.NotePermissionService;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService
{
    private final FolderMapper folderMapper;
    private final BrowseHistoryService browseHistoryService;
    private final NotePermissionService notePermissionService;

    @Override
    public void createNote(Long userId, CreateNoteDto dto)
    {
        // 先查找是否有同名的
        Note n = lambdaQuery()
                .eq(Note::getUserId, userId)
                .eq(Note::getTitle, dto.getTitle()).one();
        // 查询到了
        if (n != null)
        {
            throw new BusinessException("已存在同名笔记！", Result.CODE_BAD_REQUEST);
        }

        // 构建笔记实体
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setTags(dto.getTags());
        note.setVisibility(NoteVisibilityTypeEnum.PRIVATE); // 默认私有
        note.setIsDeleted(0); // 未删除

        // 设置 folderId 和 path
        if (dto.getFolderId() == 0L)
        {
            // 根目录下
            note.setFolderId(0L);
            note.setPath("/root/" + dto.getTitle());
        }
        else
        {
            // 子文件夹下，查询文件夹的 path
            Folder folder = folderMapper.selectById(dto.getFolderId());
            if (folder == null)
            {
                throw new BusinessException("文件夹不存在", Result.CODE_NOT_FOUND);
            }
            note.setFolderId(dto.getFolderId());
            note.setPath(folder.getPath() + "/" + dto.getTitle());
        }

        // 保存到数据库
        this.save(note);
    }

    @Override
    public void updateNote(Long userId, Long noteId, UpdateNoteDto dto)
    {
        // 查询笔记
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", 404);
        }

        // 只有所有者或被授权可编辑的用户才能修改
        boolean canEdit = note.getUserId().equals(userId) || notePermissionService.canEdit(noteId, userId);
        if (!canEdit)
        {
            throw new BusinessException("无权修改此笔记", Result.CODE_FORBIDDEN);
        }

        // 只有所有者可以修改 visibility
        if (dto.getVisibility() != null && !note.getUserId().equals(userId))
        {
            throw new BusinessException("只有笔记所有者可以修改可见性", Result.CODE_FORBIDDEN);
        }

        // 更新字段（选择性更新）
        if (dto.getTitle() != null)
        {
            note.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null)
        {
            note.setContent(dto.getContent());
        }
        if (dto.getTags() != null)
        {
            note.setTags(dto.getTags());
        }
        if (dto.getVisibility() != null)
        {
            note.setVisibility(dto.getVisibility());
        }

        // 保存更新
        this.updateById(note);
    }

    // 删除笔记（移入回收站）
    @Override
    public void deleteNote(Long userId, Long noteId)
    {
        // 查询笔记
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", Result.CODE_NOT_FOUND);
        }

        // 设置删除标记，改为 -1 表示回收站根目录
        note.setIsDeleted(1);
        note.setFolderId(-1L);
        note.setDeletedAt(LocalDateTime.now());
        this.updateById(note);
    }

    @Override
    public void restoreNote(Long userId, Long noteId)
    {
        // 查询笔记
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", 404);
        }

        // 设置 is_deleted = 0，folderId = 0，path = /root
        note.setIsDeleted(0);
        note.setDeletedAt(null);
        note.setFolderId(0L); // 根目录
        note.setPath("/root/" + note.getTitle());
        this.updateById(note);
    }

    @Override
    public NoteDetailDto getNoteDetail(Long userId, Long noteId)
    {
        // 查询笔记
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", 404);
        }

        // 检查是否已删除
        if (note.getIsDeleted() != null && note.getIsDeleted() == 1)
        {
            throw new BusinessException("笔记已删除", Result.CODE_NOT_FOUND);
        }

        // 权限校验
        boolean canView = false;
        boolean canEdit = false;

        // 笔记所有者
        if (note.getUserId().equals(userId))
        {
            canView = true;
            canEdit = true;
        }
        // 被授权的用户
        else if (notePermissionService.hasPermission(noteId, userId))
        {
            canView = true;
            canEdit = notePermissionService.canEdit(noteId, userId);
        }
        // 公开
        else if (note.getVisibility() == NoteVisibilityTypeEnum.PUBLIC)
        {
            canView = true;
        }

        if (!canView)
        {
            throw new BusinessException("无权查看此笔记", Result.CODE_FORBIDDEN);
        }

        // 记录浏览历史
        browseHistoryService.recordView(userId, noteId);

        // 转换为 DTO 返回
        NoteDetailDto dto = new NoteDetailDto();
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setTags(note.getTags());
        dto.setVisibility(note.getVisibility());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setCanEdit(canEdit); // 返回的时候带上，前端也做限制

        return dto;
    }

    @Override
    public void permanentDelete(Long userId, Long noteId)
    {
        // 查询笔记
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", 404);
        }

        // 物理删除
        this.removeById(noteId);
    }

    @Override
    public String getNoteContent(Long userId, Long noteId)
    {
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", 404);
        }
        return "标题：" + note.getTitle() + "\n\n内容：" + note.getContent();
    }

    @Override
    public void updateVisibility(Long userId, Long noteId, NoteVisibilityTypeEnum visibility)
    {
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", Result.CODE_NOT_FOUND);
        }
        if (!note.getUserId().equals(userId))
        {
            throw new BusinessException("无权修改", Result.CODE_FORBIDDEN);
        }

        // 修改
        note.setVisibility(visibility);
        this.updateById(note);
    }
}
