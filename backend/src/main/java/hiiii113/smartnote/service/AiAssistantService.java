package hiiii113.smartnote.service;

import hiiii113.smartnote.dto.AiChatDto;

/**
 * AI 助手服务接口
 */
public interface AiAssistantService
{
    // 聊天
    String chat(Long userId, AiChatDto dto);
}
