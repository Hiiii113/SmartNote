package hiiii113.smartnote.service;

import hiiii113.smartnote.dto.FileTreeNodeDto;

import java.util.List;

public interface FileTreeService
{
    // 获取节点（通用方法）
    List<FileTreeNodeDto> getNodes(Long userId, Long parentId, Integer isDeleted);

    // 查询根目录 root 下的节点
    List<FileTreeNodeDto> getRootNodes(Long userId);

    // 查询某个文件夹下面的节点
    List<FileTreeNodeDto> getChildrenNodes(Long userId, Long parentId);

    // 获取回收站根目录节点
    List<FileTreeNodeDto> getTrashRootNodes(Long userId);
}
