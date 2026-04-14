package hiiii113.smartnote.service.impl;

import hiiii113.smartnote.config.AiAssistantProperties;
import hiiii113.smartnote.dto.AiChatDto;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.NoteMapper;
import hiiii113.smartnote.service.AiAssistantService;
import hiiii113.smartnote.tools.NoteTools;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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

    @Override
    public Flux<String> chat(Long userId, AiChatDto dto)
    {
        // 智能路由
        String mode = detectMode(dto.getMessage());
        log.info("智能路由结果: {}", mode);

        // 获取对应模式的系统提示词
        String systemPrompt = aiAssistantProperties.getPrompt(mode);

        // 构建用户消息
        String userMessage = buildUserMessage(dto, mode, userId);

        // 流式调用 AI，直接传入工具对象
        // Spring AI 会自动扫描 @Tool 注解的方法
        return ChatClient.create(chatModel)
                .prompt()
                .system(systemPrompt)
                .user(userMessage)
                .tools(noteTools)
                .stream()
                .content();
    }

    // 智能路由：判断应该使用哪种模式
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
                    .system(routerPrompt)
                    .user(message)
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

    private int parseModeNumber(String result)
    {
        if (result == null || result.isBlank())
        {
            return 1;
        }
        String trimmed = result.trim();
        if (!trimmed.isEmpty())
        {
            char c = trimmed.charAt(0);
            if (c >= '1' && c <= '3')
            {
                return c - '0';
            }
        }
        return 1;
    }

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

        // 添加用户ID上下文，方便工具使用
        userMessage = "[当前用户ID: " + userId + "]\n" + userMessage;

        return userMessage;
    }
}
