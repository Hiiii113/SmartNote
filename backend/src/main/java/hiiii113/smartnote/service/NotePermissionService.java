package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.dto.NotePermissionDto;
import hiiii113.smartnote.entity.NotePermission;

import java.util.List;

/**
 * 笔记权限 service
 */
public interface NotePermissionService extends IService<NotePermission>
{
    // 设置用户权限
    void setPermission(Long noteId, Long userId, Boolean canEdit);

    // 删除用户权限
    void removePermission(Long noteId, Long userId);

    // 获取笔记的所有授权用户
    List<NotePermissionDto> getNotePermissions(Long noteId);

    // 检查用户是否有权限
    boolean hasPermission(Long noteId, Long userId);

    // 检查用户是否可编辑
    boolean canEdit(Long noteId, Long userId);
}
