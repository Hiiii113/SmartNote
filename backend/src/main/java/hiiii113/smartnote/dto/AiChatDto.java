package hiiii113.smartnote.dto;

import lombok.Data;

/**
 * AI 聊天请求 DTO
 */
@Data
public class AiChatDto
{
    // 用户消息
    private String message;

    // 笔记 ID（可选，用于笔记总结）
    private Long noteId;
}
