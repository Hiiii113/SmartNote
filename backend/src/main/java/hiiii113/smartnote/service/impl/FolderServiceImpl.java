package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.CreateFolderDto;
import hiiii113.smartnote.entity.Folder;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.FolderMapper;
import hiiii113.smartnote.service.FolderService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService
{
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
    public void deleteFolder(Long userId, Long folderId)
    {
        // 查询文件夹
        Folder folder = this.getById(folderId);
        if (folder == null)
        {
            throw new BusinessException("文件夹不存在", Result.CODE_NOT_FOUND);
        }

        // parentId 改为 -1 表示回收站根目录
        folder.setIsDeleted(1);
        folder.setParentId(-1L);
        folder.setDeletedAt(LocalDateTime.now());
        this.updateById(folder);
    }

    @Override
    public void restoreFolder(Long userId, Long folderId)
    {
        // 查询文件夹
        Folder folder = this.getById(folderId);
        if (folder == null)
        {
            throw new BusinessException("文件夹不存在", Result.CODE_NOT_FOUND);
        }

        // 恢复：设置 is_deleted = 0，parentId = 0（放回根目录），path = /root
        folder.setIsDeleted(0);
        folder.setDeletedAt(null);
        folder.setParentId(0L);
        folder.setPath("/root/" + folder.getName());
        this.updateById(folder);
    }

    @Override
    public void permanentDelete(Long userId, Long folderId)
    {
        // 查询文件夹
        Folder folder = this.getById(folderId);
        if (folder == null)
        {
            throw new BusinessException("文件夹不存在", Result.CODE_NOT_FOUND);
        }

        // 物理删除
        this.removeById(folderId);
    }
}
