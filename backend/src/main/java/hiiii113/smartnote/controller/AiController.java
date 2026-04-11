package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

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

    // 分析笔记
    @PostMapping("/analyze/{noteId}")
    public Result<Void> analyzeNote(@PathVariable Long noteId)
    {
        Long userId = StpUtil.getLoginIdAsLong();
        String noteContent = noteService.getNoteContent(userId, noteId);

        String response = chatClient.prompt()
                .user("请分析以下笔记，给出总结和3-5个标签建议：\n\n" + noteContent)
                .call()
                .content();

        return Result.ok(response);
    }
}
