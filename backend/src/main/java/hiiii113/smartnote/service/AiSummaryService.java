package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.entity.AiSummary;

/**
 * 保存 ai 总结笔记内容的 service
 */
public interface AiSummaryService extends IService<AiSummary>
{
    // 保存 ai 总结内容到数据库
    void saveAiSummary(Long noteId, String response);

    // 获取 ai 总结内容
    String getAiSummary(Long userId, Long noteId);
}
