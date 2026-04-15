package hiiii113.smartnote.service;

import hiiii113.smartnote.dto.AiChatDto;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 助手服务接口
 */
public interface AiAssistantService
{
    // 聊天
    Flux<String> chat(Long userId, AiChatDto dto);

    // 获取用户的所有会话ID列表
    List<String> getConversationIds(Long userId);

    // 清空指定会话的历史
    void clearHistory(Long userId, String conversationId);
}
