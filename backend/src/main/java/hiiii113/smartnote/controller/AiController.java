package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.AiChatDto;
import hiiii113.smartnote.service.AiAssistantService;
import hiiii113.smartnote.service.AiSummaryService;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import reactor.core.publisher.Flux;

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

    // 分析笔记
    @PostMapping("/analyze/{noteId}")
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
    public Result<String> getAiSummary(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取总结
        String response = aiSummaryService.getAiSummary(userId, noteId);
        return Result.ok("查询成功！", response);
    }

    // 全局 AI 助手聊天（SSE 流式）
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody AiChatDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 返回 SSE
        return aiAssistantService.chat(userId, dto);
    }
}
