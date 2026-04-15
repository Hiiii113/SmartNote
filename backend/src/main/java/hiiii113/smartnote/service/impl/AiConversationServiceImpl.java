package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.entity.AiConversation;
import hiiii113.smartnote.mapper.AiConversationMapper;
import hiiii113.smartnote.service.AiConversationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI对话记录 service 实现类
 */
@Service
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation> implements AiConversationService
{
    // 保存用户消息
    @Override
    public void saveUserMessage(Long userId, String conversationId, String content)
    {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setConversationId(conversationId);
        conversation.setRole("user");
        conversation.setContent(content);
        save(conversation);
    }

    // 保存 ai 回复的消息
    @Override
    public void saveAssistantMessage(Long userId, String conversationId, String content)
    {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setConversationId(conversationId);
        conversation.setRole("assistant");
        conversation.setContent(content);
        save(conversation);
    }

    // 获取对话历史
    @Override
    public List<AiConversation> getConversationHistory(Long userId, String conversationId)
    {
        return lambdaQuery()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getConversationId, conversationId)
                .orderByAsc(AiConversation::getCreatedAt)
                .list();
    }

    // 清除对话历史
    @Override
    public void clearHistory(Long userId, String conversationId)
    {
        lambdaUpdate()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getConversationId, conversationId)
                .remove();
    }

    // 获取用户的所有会话 id 列表
    @Override
    public List<String> getUserConversationIds(Long userId)
    {
        return lambdaQuery()
                .eq(AiConversation::getUserId, userId)
                .select(AiConversation::getConversationId)
                .groupBy(AiConversation::getConversationId) // 去重
                .list()
                .stream()
                .map(AiConversation::getConversationId) // 只提取这一个字段
                .collect(Collectors.toList());
    }
}
