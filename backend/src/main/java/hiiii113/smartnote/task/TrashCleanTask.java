package hiiii113.smartnote.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hiiii113.smartnote.entity.Folder;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.mapper.FolderMapper;
import hiiii113.smartnote.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 回收站定时清理任务
 */
@Slf4j // 日志记录：定时任务需要加日志记录确保可以看到记录
@Component
@RequiredArgsConstructor
public class TrashCleanTask
{
    private final NoteMapper noteMapper;
    private final FolderMapper folderMapper;

    // 读取 application.yml 中的配置项，没有的话默认使用 5 分钟
    @Value("${trash.retention-minutes:5}")
    private int retentionMinutes;

    // 每 clean-interval-minutes 分钟执行一次，默认 5 分钟
    @Scheduled(fixedRateString = "#{${trash.clean-interval-minutes:5} * 60 * 1000}")
    public void cleanExpiredTrash()
    {
        // 记录日志
        log.info("开始执行回收站清理任务，保留时间: {} 分钟", retentionMinutes);

        // 当前时间减去配置项，和数据库对比来判断
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(retentionMinutes);

        // 清理过期的笔记
        List<Note> expiredNotes = noteMapper.selectList(
                new LambdaQueryWrapper<Note>()
                        .eq(Note::getIsDeleted, 1) // 被删除了的
                        .isNotNull(Note::getDeletedAt) // 确保含有这个字段
                        .lt(Note::getDeletedAt, threshold)
        );

        // 非空则清理
        if (!expiredNotes.isEmpty())
        {
            for (Note note : expiredNotes)
            {
                noteMapper.deleteById(note.getId());
                log.info("已永久删除笔记: {} (ID: {})", note.getTitle(), note.getId());
            }
        }

        // 清理过期的文件夹
        List<Folder> expiredFolders = folderMapper.selectList(
                new LambdaQueryWrapper<Folder>()
                        .eq(Folder::getIsDeleted, 1) // 被删除了
                        .isNotNull(Folder::getDeletedAt) // 确保有这个字段
                        .lt(Folder::getDeletedAt, threshold)
        );

        // 非空则清理
        if (!expiredFolders.isEmpty())
        {
            for (Folder folder : expiredFolders)
            {
                folderMapper.deleteById(folder.getId());
                log.info("已永久删除文件夹: {} (ID: {})", folder.getName(), folder.getId());
            }
        }

        // 记录删除数量
        int total = expiredNotes.size() + expiredFolders.size();
        if (total > 0)
        {
            log.info("回收站清理完成，共删除 {} 条记录", total);
        }
    }
}
