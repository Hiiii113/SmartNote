package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送好友申请
 */
@Data
public class SendFriendRequestDto
{
    // 手机号或邮箱
    @NotBlank(message = "请输入手机号或邮箱")
    private String account;
}
