package hiiii113.smartnote.service;

import hiiii113.smartnote.dto.AiChatDto;
import reactor.core.publisher.Flux;

/**
 * AI 助手服务接口
 */
public interface AiAssistantService
{
    // 聊天
    Flux<String> chat(Long userId, AiChatDto dto);
}
