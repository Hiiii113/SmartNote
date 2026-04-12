package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
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
        // 查询笔记内容
        String noteContent = noteService.getNoteContent(userId, noteId);

        // 调用 ai 大模型回答
        String response = chatClient.prompt()
                .user("请分析以下笔记，给出总结和3-5个标签建议：\n\n" + noteContent)
                .call()
                .content();

        // 调用 service 层代码实现 ai 总结持久化
        aiSummaryService.saveAiSummary(noteId, response);

        return Result.ok("分析成功", response);
    }

    // 获取一篇笔记的 ai 总结
    @GetMapping("/analyze/{noteId}")
    public Result<String> getAiSummary(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取内容
        String response = aiSummaryService.getAiSummary(userId, noteId);
        return Result.ok("查询成功！", response);
    }
}
