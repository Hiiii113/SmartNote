package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.AiChatDto;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.AiAssistantService;
import hiiii113.smartnote.service.AiConversationService;
import hiiii113.smartnote.service.AiSummaryService;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

/**
 * AI 分析笔记控制器
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController
{
    private final ChatClient chatClient;
    private final NoteService noteService;
    private final AiSummaryService aiSummaryService;
    private final AiAssistantService aiAssistantService;
    private final AiConversationService aiConversationService;

    // 分析笔记
    @PostMapping("/analyze/{noteId}")
    @LogAnnotation(module = "AI", operator = "分析笔记")
    @Retryable(
            retryFor = {ResourceAccessException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Result<String> analyzeNote(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取笔记内容
        String noteContent = noteService.getNoteContent(userId, noteId);

        String response = chatClient.prompt()
                .user("请分析以下笔记，给出总结和3-5个标签建议：\n\n" + noteContent)
                .call()
                .content();

        aiSummaryService.saveAiSummary(noteId, response);
        return Result.ok("分析成功", response);
    }

    // 获取一篇笔记的 ai 总结
    @GetMapping("/analyze/{noteId}")
    @LogAnnotation(module = "AI", operator = "获取AI总结")
    public Result<String> getAiSummary(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取总结
        String response = aiSummaryService.getAiSummary(userId, noteId);
        return Result.ok("查询成功！", response);
    }

    // 全局 AI 助手聊天
    @PostMapping("/chat/stream")
    @LogAnnotation(module = "AI", operator = "AI助手聊天")
    public Result<String> chatStream(@RequestBody AiChatDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();

        // 获取完整响应
        String response = aiAssistantService.chat(userId, dto).blockFirst();
        return Result.ok("成功", response);
    }

    // 获取用户的所有会话ID列表
    @GetMapping("/conversations")
    @LogAnnotation(module = "AI", operator = "获取会话列表")
    public Result<?> getConversations()
    {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok("获取成功", aiAssistantService.getConversationIds(userId));
    }

    // 获取指定会话的历史记录
    @GetMapping("/conversation/{conversationId}")
    @LogAnnotation(module = "AI", operator = "获取会话历史")
    public Result<?> getConversationHistory(@PathVariable String conversationId)
    {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok("获取成功", aiConversationService.getConversationHistory(userId, conversationId));
    }

    // 清空指定会话的历史
    @DeleteMapping("/conversation/{conversationId}")
    @LogAnnotation(module = "AI", operator = "清空会话历史")
    public Result<?> clearConversation(@PathVariable String conversationId)
    {
        Long userId = StpUtil.getLoginIdAsLong();
        aiAssistantService.clearHistory(userId, conversationId);
        return Result.ok("清空成功");
    }
}
