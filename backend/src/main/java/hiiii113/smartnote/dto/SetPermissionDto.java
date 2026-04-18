package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置笔记权限
 */
@Data
public class SetPermissionDto
{
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    // 是否可编辑
    @NotNull(message = "请指定是否可编辑")
    private Boolean canEdit;
}
