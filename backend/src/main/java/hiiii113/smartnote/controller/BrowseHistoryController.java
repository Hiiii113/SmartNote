package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.entity.BrowseHistory;
import hiiii113.smartnote.service.BrowseHistoryService;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 浏览历史 controller
 */
@RestController
@RequestMapping("/browse-history")
@RequiredArgsConstructor
public class BrowseHistoryController
{
    private final BrowseHistoryService browseHistoryService;
    private final NoteService noteService;

    // 获取浏览历史列表
    @GetMapping
    public Result<List<BrowseHistory>> getHistoryList(@RequestParam(required = false) Integer limit)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取历史浏览记录并查询标题添加到实体类然后返回给前端
        List<BrowseHistory> historyList = browseHistoryService.getHistoryList(userId, limit)
                .stream().peek(history ->
                        history.setNoteTitle(noteService.getById(history.getNoteId()).getTitle())).collect(Collectors.toList());
        return Result.ok(historyList);
    }

    // 删除单条浏览记录
    @DeleteMapping("/{noteId}")
    public Result<Void> deleteHistory(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 删除
        browseHistoryService.deleteHistory(userId, noteId);
        return Result.ok("删除成功");
    }

    // 清空浏览历史
    @DeleteMapping
    public Result<Void> clearHistory()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 清除浏览记录
        browseHistoryService.clearHistory(userId);
        return Result.ok("清空成功");
    }
}
