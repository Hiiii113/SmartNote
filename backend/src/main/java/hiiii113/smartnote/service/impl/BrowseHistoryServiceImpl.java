package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.entity.BrowseHistory;
import hiiii113.smartnote.mapper.BrowseHistoryMapper;
import hiiii113.smartnote.mapper.NoteMapper;
import hiiii113.smartnote.service.BrowseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 浏览历史 service 实现类
 */
@Service
@RequiredArgsConstructor
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> implements BrowseHistoryService
{
    private final NoteMapper noteMapper;

    // 记录浏览
    @Override
    public void recordView(Long userId, Long noteId)
    {
        // 查找是否已存在记录
        BrowseHistory history = lambdaQuery()
                .eq(BrowseHistory::getUserId, userId)
                .eq(BrowseHistory::getNoteId, noteId)
                .one();

        if (history != null)
        {
            // 更新浏览次数和时间
            history.setViewCount(history.getViewCount() + 1);
            history.setBrowsedAt(LocalDateTime.now());
            this.updateById(history);
        }
        else
        {
            // 不存在，新增记录
            history = new BrowseHistory();
            history.setUserId(userId);
            history.setNoteId(noteId);
            history.setViewCount(1); // 一开始是 1
            history.setBrowsedAt(LocalDateTime.now());
            this.save(history);
        }
    }

    // 获取浏览历史列表
    @Override
    public List<BrowseHistory> getHistoryList(Long userId, Integer limit)
    {
        return lambdaQuery()
                .eq(BrowseHistory::getUserId, userId)
                .orderByDesc(BrowseHistory::getBrowsedAt)
                .last(limit != null, "LIMIT " + limit) // 如果有限制就添加
                .list()
                .stream()
                .map(
                        history ->
                        {
                            var note = noteMapper.selectById(history.getNoteId());
                            if (note != null)
                            {
                                history.setNoteTitle(note.getTitle());
                                return history;
                            }
                            return null;
                        }
                )
                .filter(Objects::nonNull) // 过滤掉已经被删除了的
                .collect(Collectors.toList());
    }

    // 清空浏览历史
    @Override
    public void clearHistory(Long userId)
    {
        lambdaUpdate()
                .eq(BrowseHistory::getUserId, userId)
                .remove();
    }
}
