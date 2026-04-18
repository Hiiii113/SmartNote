package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.entity.BrowseHistory;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.BrowseHistoryService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 浏览历史 controller
 */
@RestController
@RequestMapping("/browse-history")
@RequiredArgsConstructor
public class BrowseHistoryController
{
    private final BrowseHistoryService browseHistoryService;

    /**
     * 获取浏览历史列表
     *
     * @param limit 限制读取多少条
     * @return List<BrowseHistory>
     */
    @GetMapping
    @LogAnnotation(module = "浏览历史", operator = "获取浏览历史")
    public Result<List<BrowseHistory>> getHistoryList(@RequestParam(required = false) Integer limit)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取历史浏览记录并查询标题添加到实体类然后返回给前端
        List<BrowseHistory> historyList = browseHistoryService.getHistoryList(userId, limit);
        // 返回
        return Result.ok(historyList);
    }

    /**
     * 清空浏览历史
     */
    @DeleteMapping
    @LogAnnotation(module = "浏览历史", operator = "清空浏览历史")
    public Result<Void> clearHistory()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 清除浏览记录
        browseHistoryService.clearHistory(userId);
        // 返回
        return Result.ok("浏览历史已清空");
    }
}
