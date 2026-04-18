package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.entity.BrowseHistory;

import java.util.List;

/**
 * 浏览历史 service 层
 */
public interface BrowseHistoryService extends IService<BrowseHistory>
{
    // 记录浏览
    void recordView(Long userId, Long noteId);

    // 获取浏览历史列表
    List<BrowseHistory> getHistoryList(Long userId, Integer limit);

    // 清空浏览历史
    void clearHistory(Long userId);
}
