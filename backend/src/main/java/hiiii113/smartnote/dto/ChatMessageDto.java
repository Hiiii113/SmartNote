package hiiii113.smartnote.dto;

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
    private Long toId;

    // 内容
    private String content;

    // 消息类型
    private Integer msgType;
}
