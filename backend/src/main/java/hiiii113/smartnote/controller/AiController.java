package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.config.AiAssistantProperties;
import hiiii113.smartnote.dto.AiChatDto;
import hiiii113.smartnote.entity.AiConversation;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.AiAssistantService;
import hiiii113.smartnote.service.AiConversationService;
import hiiii113.smartnote.service.AiSummaryService;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

/**
 * AI 相关接口 controller
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController
{
    private final NoteService noteService;
    private final AiSummaryService aiSummaryService;
    private final AiAssistantService aiAssistantService;
    private final AiAssistantProperties aiAssistantProperties;
    private final AiConversationService aiConversationService;
    private final OpenAiChatModel chatModel;

    /**
     * 分析笔记
     *
     * @param noteId 需要分析的笔记的 id
     * @return String 类型结果
     */
    @PostMapping("/analyze/{noteId}")
    @LogAnnotation(module = "AI", operator = "分析笔记")
    @Retryable( // 防止 llm 超时
            retryFor = {ResourceAccessException.class}, // 遇到这个异常就重试
            backoff = @Backoff(delay = 1000, multiplier = 2) // 每次等待 1000ms 重试，延迟倍数 2
    )
    public Result<String> analyzeNote(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取笔记内容
        String noteContent = noteService.getNoteContent(userId, noteId);
        // 获取笔记总结的 prompt
        String noteSummaryPrompt = aiAssistantProperties.getPrompt("note-summary");
        // 组装 prompt 并发送请求
        String response = ChatClient
                .create(chatModel)
                .prompt()
                .system(noteSummaryPrompt)
                .user("请分析以下笔记\n\n" + noteContent)
                .call()
                .content();

        // 保存到数据库
        aiSummaryService.saveAiSummary(noteId, response);
        // 返回
        return Result.ok("笔记分析成功", response);
    }

    /**
     * 获取一篇笔记的 ai 总结
     *
     * @param noteId 需要获取的笔记 id
     * @return String 类型返回值
     */
    @GetMapping("/analyze/{noteId}")
    @LogAnnotation(module = "AI", operator = "获取AI总结")
    public Result<String> getAiSummary(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取总结
        String response = aiSummaryService.getAiSummary(userId, noteId);
        // 返回结果
        return Result.ok("AI总结获取成功", response);
    }

    /**
     * 全局 AI 助手聊天
     *
     * @param dto 需要的数据
     * @return 返回 String 类型的 ai 语句
     */
    @PostMapping("/chat/stream")
    @LogAnnotation(module = "AI", operator = "AI助手聊天")
    public Result<String> chatStream(@Valid @RequestBody AiChatDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();

        // 获取完整响应
        String response = aiAssistantService.chat(userId, dto);
        // 返回结果
        return Result.ok("AI回复成功", response);
    }

    /**
     * 获取用户的所有会话ID列表
     *
     * @return 返回 List<String> 类型的 conversationIds
     */
    @GetMapping("/conversations")
    @LogAnnotation(module = "AI", operator = "获取会话列表")
    public Result<List<String>> getConversations()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取 conversationIds
        List<String> conversationIds = aiConversationService.getUserConversationIds(userId);
        // 返回结果
        return Result.ok(conversationIds);
    }

    /**
     * 获取指定会话的历史记录
     *
     * @param conversationId 对话 id
     * @return 返回 List<conversations>
     */
    @GetMapping("/conversation/{conversationId}")
    @LogAnnotation(module = "AI", operator = "获取会话历史")
    public Result<List<AiConversation>> getConversationHistory(@PathVariable String conversationId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取 conversations
        List<AiConversation> conversations = aiConversationService.getConversationHistory(userId, conversationId);
        // 返回
        return Result.ok(conversations);
    }

    /**
     * 清空指定会话的历史
     *
     * @param conversationId 对话 id
     */
    @DeleteMapping("/conversation/{conversationId}")
    @LogAnnotation(module = "AI", operator = "清空会话历史")
    public Result<Void> clearConversation(@PathVariable String conversationId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 删除会话历史
        aiConversationService.clearHistory(userId, conversationId);
        // 返回
        return Result.ok("会话记录已清空");
    }
}
