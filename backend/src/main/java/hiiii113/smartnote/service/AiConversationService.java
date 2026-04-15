package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.entity.AiConversation;

import java.util.List;

/**
 * AI对话记录 service 层
 */
public interface AiConversationService extends IService<AiConversation>
{
    // 保存用户消息
    void saveUserMessage(Long userId, String conversationId, String content);

    // 保存AI消息
    void saveAssistantMessage(Long userId, String conversationId, String content);

    // 获取会话的对话历史（按时间升序，用于构建上下文）
    List<AiConversation> getConversationHistory(Long userId, String conversationId);

    // 清空会话历史
    void clearHistory(Long userId, String conversationId);

    // 获取用户的所有会话ID列表
    List<String> getUserConversationIds(Long userId);
}
