package hiiii113.smartnote.dto;

import lombok.Data;

/**
 * 设置笔记权限
 */
@Data
public class SetPermissionDto
{
    private Long userId;

    // 是否可编辑
    private Boolean canEdit;
}
