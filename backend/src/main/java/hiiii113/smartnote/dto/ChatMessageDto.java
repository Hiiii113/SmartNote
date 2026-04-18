package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聊天消息 DTO
 */
@Data
public class ChatMessageDto
{
    // 发送者
    private Long fromId;

    // 接收者
    @NotNull(message = "接收者ID不能为空")
    private Long toId;

    // 内容
    @NotBlank(message = "消息内容不能为空")
    private String content;

    // 消息类型
    @NotNull(message = "消息类型不能为空")
    private Integer msgType;
}
