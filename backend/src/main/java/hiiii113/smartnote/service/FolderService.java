package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.dto.CreateFolderDto;
import hiiii113.smartnote.entity.Folder;

public interface FolderService extends IService<Folder>
{
    // 创建文件夹
    void createFolder(Long userId, CreateFolderDto dto);

    // 重命名文件夹
    void renameFolder(Long userId, Long folderId, String name);

    // 删除文件夹（移入回收站）
    void deleteFolder(Long userId, Long folderId);

    // 恢复文件夹（移回根目录）
    void restoreFolder(Long userId, Long folderId);

    // 永久删除文件夹
    void permanentDelete(Long userId, Long folderId);
}
