package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.CreateNoteDto;
import hiiii113.smartnote.dto.NoteDetailDto;
import hiiii113.smartnote.dto.NoteSyncMessageDto;
import hiiii113.smartnote.dto.UpdateNoteDto;
import hiiii113.smartnote.entity.Folder;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.FolderMapper;
import hiiii113.smartnote.mapper.NoteMapper;
import hiiii113.smartnote.service.*;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService
{
    private final FolderMapper folderMapper;
    private final BrowseHistoryService browseHistoryService;
    private final NotePermissionService notePermissionService;
    private final FriendshipService friendshipService;
    private final VectorStore vectorStore;
    private final NoteCacheService noteCacheService;

    // 向量数据库内容最大长度（嵌入模型限制 2048 tokens，保守估计约 1500 字符）
    private static final int MAX_VECTOR_CONTENT_LENGTH = 1500;

    // 创建笔记
    @Override
    @Transactional
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

        // 插入向量数据库
        try
        {
            addNoteToVectorStore(note);
        }
        catch (Exception e)
        {
            log.warn("插入向量数据库失败: noteId = {}, error={}", note.getId(), e.getMessage());
        }
    }

    // 将笔记添加到向量数据库
    private void addNoteToVectorStore(Note note)
    {
        // 元数据，用于筛选
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteId", note.getId());
        metadata.put("userId", note.getUserId());
        metadata.put("title", note.getTitle());
        metadata.put("tags", note.getTags());

        // 移除 base64 图片并截断长度，防止超长报错
        String content = preprocessContentForVector(note.getContent());

        Document document = new Document(note.getId().toString(), content, metadata);
        vectorStore.add(List.of(document));
    }

    // 预处理内容，移除 base64 图片，截断过长内容
    private String preprocessContentForVector(String content)
    {
        if (content == null || content.isEmpty())
        {
            return "";
        }

        // 移除 base64 图片，格式如：![image-Hiiii113](assets/image-Hiiii113.png)，并替换成"[图片]"
        String processed = content.replaceAll("!\\[.*?]\\(data:image/[^)]+\\)", "[图片]");

        // 截断过长内容
        if (processed.length() > MAX_VECTOR_CONTENT_LENGTH)
        {
            processed = processed.substring(0, MAX_VECTOR_CONTENT_LENGTH) + "...";
        }

        return processed;
    }

    // 从向量数据库删除笔记
    private void deleteNoteFromVectorStore(Long noteId)
    {
        vectorStore.delete(List.of(noteId.toString()));
    }

    // 更新笔记
    @Override
    @Transactional
    @CacheEvict(value = "noteCache", key = "#noteId")
    public void updateNote(Long userId, Long noteId, UpdateNoteDto dto)
    {
        // 查询笔记
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", 404);
        }

        // 非笔记所有者：必须是好友才能编辑
        if (!note.getUserId().equals(userId))
        {
            if (!friendshipService.isFriend(userId, note.getUserId()))
            {
                throw new BusinessException("无权修改此笔记", Result.CODE_FORBIDDEN);
            }
        }

        // 只有所有者或被授权可编辑的用户才能修改
        boolean canEdit = note.getUserId().equals(userId) || notePermissionService.canEdit(noteId, userId);
        if (!canEdit)
        {
            throw new BusinessException("无权修改此笔记", Result.CODE_FORBIDDEN);
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

        // 保存更新
        this.updateById(note);

        // 更新向量数据库
        try
        {
            // 删除
            deleteNoteFromVectorStore(noteId);
            // 重新添加
            addNoteToVectorStore(note);
        }
        catch (Exception e)
        {
            log.warn("更新向量数据库失败: noteId = {}, error={}", noteId, e.getMessage());
        }
    }

    // 删除笔记（移入回收站）
    @Override
    @Transactional
    @CacheEvict(value = "noteCache", key = "#noteId")
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

        // 从向量数据库删除
        try
        {
            deleteNoteFromVectorStore(noteId);
        }
        catch (Exception e)
        {
            log.warn("删除时向量数据库删除失败: noteId = {}, error={}", noteId, e.getMessage());
        }
    }

    // 恢复笔记
    @Override
    @Transactional
    @CacheEvict(value = "noteCache", key = "#noteId")
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

        // 恢复到向量数据库
        try
        {
            addNoteToVectorStore(note);
        }
        catch (Exception e)
        {
            log.warn("恢复向量数据库失败: noteId = {}, error={}", noteId, e.getMessage());
        }
    }

    // 获取笔记详情
    @Override
    public NoteDetailDto getNoteDetail(Long userId, Long noteId)
    {
        // 权限验证
        boolean canEdit = checkGetNodeDetailPermission(userId, noteId);

        // 查询
        NoteDetailDto dto = noteCacheService.getNodeDetailCache(noteId);

        // 记录浏览历史
        browseHistoryService.recordView(userId, noteId);

        // 全局浏览次数增加
        incrementViewCount(noteId);

        if (dto != null)
        {
            dto.setCanEdit(canEdit); // 返回的时候带上，前端也做限制
        }

        return dto;
    }

    // 永久删除
    @Override
    @Transactional
    @CacheEvict(value = "noteCache", key = "#noteId")
    public void permanentDelete(Long userId, Long noteId)
    {
        // 查询笔记
        Note note = this.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", 404);
        }
        if (!userId.equals(note.getUserId()))
        {
            throw new BusinessException("无权删除此笔记", 403);
        }

        // 从向量数据库删除
        try
        {
            deleteNoteFromVectorStore(noteId);
        }
        catch (Exception e)
        {
            log.warn("永久删除时向量数据库删除失败: noteId = {}, error={}", noteId, e.getMessage());
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

    // 更新可见性
    @Override
    @CacheEvict(value = "noteCache", key = "#noteId")
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

    // 看看是否可以查看笔记详情
    @Override
    public boolean checkGetNodeDetailPermission(Long userId, Long noteId)
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

        // 所在文件夹已进回收站时不可见
        if (note.getFolderId() != null && note.getFolderId() > 0)
        {
            Folder folder = folderMapper.selectById(note.getFolderId());
            if (folder != null && folder.getIsDeleted() != null && folder.getIsDeleted() == 1)
            {
                throw new BusinessException("笔记所在文件夹已删除", Result.CODE_NOT_FOUND);
            }
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
        else
        {
            // 必须是好友
            if (!friendshipService.isFriend(userId, note.getUserId()))
            {
                throw new BusinessException("无权查看此笔记", Result.CODE_FORBIDDEN);
            }

            // 被授权的用户
            if (notePermissionService.hasPermission(noteId, userId))
            {
                canView = true;
                canEdit = notePermissionService.canEdit(noteId, userId);
            }
            // 公开
            else if (note.getVisibility() == NoteVisibilityTypeEnum.PUBLIC)
            {
                canView = true;
            }
        }


        if (!canView)
        {
            throw new BusinessException("无权查看此笔记", Result.CODE_FORBIDDEN);
        }

        return canEdit;
    }

    // 增加笔记的查看次数
    @Override
    public void incrementViewCount(Long noteId)
    {
        lambdaUpdate()
                .eq(Note::getId, noteId)
                .setSql("view_count = IFNULL(view_count, 0) + 1") // viewCount + 1
                .update();
    }

    // 获取最近常看3条笔记
    @Override
    public List<Note> getHotNotes(Long userId, Long noteId)
    {
        return lambdaQuery()
                .eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, 0)
                .orderByDesc(Note::getViewCount)
                .orderByDesc(Note::getUpdatedAt)
                .last("LIMIT 3")
                .list();
    }

    // 获取笔记同步消息
    @Override
    public NoteSyncMessageDto getNoteSyncMessage(Long noteId)
    {
        Note note = this.getById(noteId);
        if (note == null)
        {
            return null;
        }

        NoteSyncMessageDto dto = new NoteSyncMessageDto();
        dto.setType("note-updated");
        dto.setNoteId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setTags(note.getTags());
        dto.setVisibility(note.getVisibility());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }

    public int getViewCount(Long noteId)
    {
        Note note = this.getById(noteId);
        return note == null || note.getViewCount() == null ? 0 : note.getViewCount();
    }
}
