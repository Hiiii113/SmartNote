package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.CreateFolderDto;
import hiiii113.smartnote.entity.Folder;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.FolderMapper;
import hiiii113.smartnote.mapper.NoteMapper;
import hiiii113.smartnote.service.FolderService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService
{
    private final NoteMapper noteMapper;

    @Override
    public void createFolder(Long userId, CreateFolderDto dto)
    {
        // 先查找是否有同名的
        Folder f = lambdaQuery()
                .eq(Folder::getUserId, userId)
                .eq(Folder::getName, dto.getName()).one();

        // 查找到了同名的
        if (f != null)
        {
            throw new BusinessException("已存在同名文件夹！", Result.CODE_BAD_REQUEST);
        }

        // 构建文件夹实体
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(dto.getName());
        folder.setIsDeleted(0);

        // 设置 path
        if (dto.getParentId() == 0L)
        {
            // 根目录下
            folder.setParentId(0L);
            folder.setPath("/root/" + dto.getName());
        }
        else
        {
            // 子文件夹，查询父文件夹的 path
            Folder parent = this.getById(dto.getParentId());
            if (parent == null)
            {
                throw new BusinessException("父文件夹不存在", Result.CODE_NOT_FOUND);
            }
            folder.setParentId(dto.getParentId());
            folder.setPath(parent.getPath() + "/" + dto.getName()); // 拼接 path
        }

        // 保存到数据库
        this.save(folder);
    }

    @Override
    public void renameFolder(Long userId, Long folderId, String name)
    {
        // 查询文件夹，校验权限
        Folder folder = this.getById(folderId);
        if (folder == null)
        {
            throw new BusinessException("文件夹不存在", Result.CODE_NOT_FOUND);
        }

        // 更新名称
        folder.setName(name);
        this.updateById(folder);
    }

    // 删除文件夹（移入回收站）
    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId)
    {
        Folder folder = this.getById(folderId);
        if (folder == null)
        {
            throw new BusinessException("文件夹不存在", Result.CODE_NOT_FOUND);
        }
        if (!userId.equals(folder.getUserId()))
        {
            throw new BusinessException("无权删除此文件夹", Result.CODE_FORBIDDEN);
        }
        // 递归子文件夹
        softDeleteFolderSubtree(userId, folderId, folderId, LocalDateTime.now());
    }

    /**
     * @param rootDeletedId 本次删除的最外层文件夹 id
     */
    private void softDeleteFolderSubtree(Long userId, Long folderId, Long rootDeletedId, LocalDateTime now)
    {
        // 对本文件夹底下的所有未删除的文件夹进行递归删除
        List<Folder> children = this.lambdaQuery()
                .eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, folderId)
                .eq(Folder::getIsDeleted, 0)
                .list();
        for (Folder child : children)
        {
            softDeleteFolderSubtree(userId, child.getId(), rootDeletedId, now);
        }

        // 当前层笔记标记删除
        LambdaUpdateWrapper<Note> noteUw = new LambdaUpdateWrapper<>();
        noteUw.eq(Note::getUserId, userId)
                .eq(Note::getFolderId, folderId)
                .eq(Note::getIsDeleted, 0)
                .set(Note::getIsDeleted, 1)
                .set(Note::getDeletedAt, now);
        noteMapper.update(null, noteUw);

        // 当前文件夹标记删除
        Folder folder = this.getById(folderId);
        if (folder != null && userId.equals(folder.getUserId()))
        {
            folder.setIsDeleted(1);
            folder.setDeletedAt(now);
            // 如果是最外层文件夹，则挂到回收站根目录
            if (folderId.equals(rootDeletedId))
            {
                folder.setParentId(-1L);
            }
            this.updateById(folder);
        }
    }

    @Override
    @Transactional
    public void restoreFolder(Long userId, Long folderId)
    {
        // 查询文件夹
        Folder folder = this.getById(folderId);
        if (folder == null)
        {
            throw new BusinessException("文件夹不存在", Result.CODE_NOT_FOUND);
        }

        // 递归恢复子文件夹和笔记
        restoreFolderSubtree(userId, folderId, folder.getName());
    }

    // 递归恢复文件夹及其所有子内容
    private void restoreFolderSubtree(Long userId, Long folderId, String rootFolderName)
    {
        // 恢复当前文件夹下的所有已删除的子文件夹
        List<Folder> children = this.lambdaQuery()
                .eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, folderId)
                .eq(Folder::getIsDeleted, 1)
                .list();
        for (Folder child : children)
        {
            restoreFolderSubtree(userId, child.getId(), rootFolderName);
        }

        // 恢复当前文件夹下的所有已删除的笔记
        LambdaUpdateWrapper<Note> noteUw = new LambdaUpdateWrapper<>();
        noteUw.eq(Note::getUserId, userId)
                .eq(Note::getFolderId, folderId)
                .eq(Note::getIsDeleted, 1)
                .set(Note::getIsDeleted, 0)
                .set(Note::getDeletedAt, null)
                .set(Note::getFolderId, folderId);
        noteMapper.update(null, noteUw);

        // 恢复当前文件夹
        Folder folder = this.getById(folderId);
        if (folder != null && userId.equals(folder.getUserId()))
        {
            folder.setIsDeleted(0);
            folder.setDeletedAt(null);
            // 如果是最外层文件夹，放到根目录；否则保持原来的父子关系
            if (folderId.equals(folderId) && folder.getParentId().equals(-1L))
            {
                folder.setParentId(0L);
                folder.setPath("/root/" + rootFolderName);
            }
            this.updateById(folder);
        }
    }

    @Override
    @Transactional
    public void permanentDelete(Long userId, Long folderId)
    {
        Folder folder = this.getById(folderId);
        if (folder == null)
        {
            throw new BusinessException("文件夹不存在", Result.CODE_NOT_FOUND);
        }
        if (!userId.equals(folder.getUserId()))
        {
            throw new BusinessException("无权删除此文件夹", Result.CODE_FORBIDDEN);
        }
        // 递归物理删除子文件夹和笔记
        permanentDeleteFolderSubtree(userId, folderId);
    }

    private void permanentDeleteFolderSubtree(Long userId, Long folderId)
    {
        // 递归删除子文件夹
        List<Folder> children = this.lambdaQuery()
                .eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, folderId)
                .eq(Folder::getIsDeleted, 1)
                .list();
        for (Folder child : children)
        {
            permanentDeleteFolderSubtree(userId, child.getId());
        }

        // 删除当前层笔记
        LambdaUpdateWrapper<Note> noteDw = new LambdaUpdateWrapper<>();
        noteDw.eq(Note::getUserId, userId)
                .eq(Note::getFolderId, folderId)
                .eq(Note::getIsDeleted, 1);
        noteMapper.delete(noteDw);

        // 删除当前文件夹
        this.removeById(folderId);
    }
}
