package hiiii113.smartnote.service.impl;

import hiiii113.smartnote.config.AiAssistantProperties;
import hiiii113.smartnote.dto.AiChatDto;
import hiiii113.smartnote.entity.AiConversation;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.NoteMapper;
import hiiii113.smartnote.service.AiConversationService;
import hiiii113.smartnote.service.AiAssistantService;
import hiiii113.smartnote.tools.NoteTools;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 助手服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService
{
    private final OpenAiChatModel chatModel;
    private final AiAssistantProperties aiAssistantProperties;
    private final NoteMapper noteMapper;
    private final NoteTools noteTools;
    private final AiConversationService aiConversationService;

    @Override
    public Flux<String> chat(Long userId, AiChatDto dto)
    {
        // 获取会话 id
        String conversationId = dto.getConversationId();

        // 智能路由
        String mode = detectMode(dto.getMessage());
        log.info("智能路由结果: {}, conversationId: {}", mode, conversationId);

        // 获取对应模式的系统提示词
        String systemPrompt = aiAssistantProperties.getPrompt(mode);

        // 构建用户消息
        String userMessage = buildUserMessage(dto, mode, userId);

        // 先加载会话历史
        List<AiConversation> history = aiConversationService.getConversationHistory(userId, conversationId);
        log.info("加载历史消息数量: {}", history.size());

        // 构建历史消息列表
        List<Message> messages = buildHistoryMessages(history);

        // 保存用户消息到数据库
        aiConversationService.saveUserMessage(userId, conversationId, userMessage);

        // 使用非流式调用（避免流式模式下工具调用参数被截断的问题）
        try
        {
            String response = ChatClient.create(chatModel)
                    .prompt()
                    .system(systemPrompt)
                    .messages(messages)
                    .user(userMessage)
                    .tools(noteTools)
                    .call()
                    .content();

            if (response == null || response.isBlank())
            {
                response = "抱歉，AI 响应异常，请稍后重试。";
            }

            aiConversationService.saveAssistantMessage(userId, conversationId, response);
            log.info("保存AI响应到数据库, conversationId: {}, 响应长度: {}", conversationId, response.length());

            return Flux.just(response);
        }
        catch (Exception e)
        {
            log.error("AI调用出错: {}", e.getMessage(), e);
            String errorMsg = "抱歉，处理您的请求时出现了问题，请稍后重试。";
            aiConversationService.saveAssistantMessage(userId, conversationId, errorMsg);
            return Flux.just(errorMsg);
        }
    }

    // 构建历史消息列表
    private List<Message> buildHistoryMessages(List<AiConversation> history)
    {
        List<Message> messages = new ArrayList<>();
        for (AiConversation conv : history)
        {
            if ("user".equals(conv.getRole()))
            {
                messages.add(new UserMessage(conv.getContent()));
            }
            else if ("assistant".equals(conv.getRole()))
            {
                messages.add(new AssistantMessage(conv.getContent()));
            }
        }
        return messages;
    }

    // 智能路由
    private String detectMode(String message)
    {
        String routerPrompt = aiAssistantProperties.getPrompt("router");
        if (routerPrompt.isEmpty())
        {
            return "chat";
        }

        try
        {
            String result = ChatClient.create(chatModel)
                    .prompt()
                    .system(routerPrompt) // 智能路由的提示词
                    .user(message) // 用户的请求
                    .call()
                    .content();

            int modeNum = parseModeNumber(result);
            return switch (modeNum)
            {
                case 2 -> "note-summary";
                case 3 -> "knowledge-search";
                default -> "chat";
            };
        }
        catch (Exception e)
        {
            log.error("智能路由失败，使用默认模式", e);
            return "chat";
        }
    }

    // 转化智能路由返回的数字
    private int parseModeNumber(String result)
    {
        if (result == null || result.isBlank())
        {
            return 1;
        }
        String trimmed = result.trim();
        if (!trimmed.isEmpty())
        {
            // 取第一个
            char c = trimmed.charAt(0);
            // 如果是字符
            if (c >= '1' && c <= '3')
            {
                return c - '0';
            }
        }
        return 1;
    }

    // 构建用户的 message
    private String buildUserMessage(AiChatDto dto, String mode, Long userId)
    {
        String userMessage = dto.getMessage();

        // 如果是笔记总结模式，需要获取笔记内容
        if ("note-summary".equals(mode) && dto.getNoteId() != null)
        {
            Note note = noteMapper.selectById(dto.getNoteId());
            if (note == null)
            {
                throw new BusinessException("笔记不存在", Result.CODE_NOT_FOUND);
            }
            userMessage = "请总结以下笔记内容：\n\n标题：" + note.getTitle() + "\n\n内容：\n" + note.getContent();
        }

        // 添加用户ID上下文
        userMessage = "[当前用户ID: " + userId + "]\n" + userMessage;

        return userMessage;
    }

    // 会话 id
    @Override
    public List<String> getConversationIds(Long userId)
    {
        return aiConversationService.getUserConversationIds(userId);
    }

    // 清除会话历史
    @Override
    public void clearHistory(Long userId, String conversationId)
    {
        aiConversationService.clearHistory(userId, conversationId);
    }
}
