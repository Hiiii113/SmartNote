package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设置好友分组 dto
 */
@Data
public class SetFriendGroupDto
{
    // 好友分组
    @NotBlank(message = "好友分组不能为空")
    String group;
}
