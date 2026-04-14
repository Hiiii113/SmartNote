package hiiii113.smartnote.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 可调用的笔记工具类
 * <p>
 * 使用 @Tool 注解让 AI 能够自动发现和调用这些方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteTools
{
    private final NoteMapper noteMapper;

    /**
     * 根据关键词搜索笔记
     * <p>
     * 在笔记标题和内容中搜索匹配的关键词，返回笔记列表（包含 ID、标题）
     * AI 应先调用此方法获取笔记列表，再根据用户意图选择合适的笔记 ID 调用 readNote
     */
    @Tool(description = "根据关键词搜索笔记，返回匹配的笔记ID和标题列表。当用户想查找某主题的笔记时使用。")
    public String searchNotes(
            @ToolParam(description = "搜索关键词") String keyword,
            @ToolParam(description = "用户ID") Long userId)
    {
        log.info("搜索笔记: keyword={}, userId={}", keyword, userId);

        if (keyword == null || keyword.isBlank())
        {
            return "错误：搜索关键词不能为空";
        }

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, 0)
                .and(w -> w
                        .like(Note::getTitle, keyword)
                        .or()
                        .like(Note::getContent, keyword)
                )
                .orderByDesc(Note::getUpdatedAt)
                .last("LIMIT 10");

        List<Note> notes = noteMapper.selectList(wrapper);

        if (notes.isEmpty())
        {
            return "未找到包含 \"" + keyword + "\" 的笔记";
        }

        StringBuilder result = new StringBuilder();
        result.append("找到 ").append(notes.size()).append(" 篇相关笔记：\n");

        for (int i = 0; i < notes.size(); i++)
        {
            Note note = notes.get(i);
            result.append(String.format("%d. [ID: %d] %s\n", i + 1, note.getId(), note.getTitle()));
        }

        result.append("\n请告诉我你想查看哪篇笔记的详细内容，提供笔记 ID 即可。");

        return result.toString();
    }

    /**
     * 通过 ID 读取笔记完整内容
     * <p>
     * 根据笔记 ID 精确获取笔记的标题和内容
     */
    @Tool(description = "通过笔记ID读取笔记的完整内容。在searchNotes获取到笔记ID后使用此方法读取具体内容。")
    public String readNote(
            @ToolParam(description = "笔记ID") Long noteId)
    {
        log.info("读取笔记: noteId={}", noteId);

        if (noteId == null)
        {
            return "错误：笔记 ID 不能为空";
        }

        Note note = noteMapper.selectById(noteId);

        if (note == null)
        {
            return "错误：笔记不存在（ID: " + noteId + "）";
        }

        if (note.getIsDeleted() == 1)
        {
            return "错误：该笔记已在回收站中";
        }

        StringBuilder result = new StringBuilder();
        result.append("【笔记标题】").append(note.getTitle()).append("\n");
        result.append("【笔记路径】").append(note.getPath()).append("\n");
        result.append("【创建时间】").append(note.getCreatedAt()).append("\n");
        result.append("【更新时间】").append(note.getUpdatedAt()).append("\n");
        result.append("\n【笔记内容】\n").append(note.getContent() != null ? note.getContent() : "（内容为空）");

        return result.toString();
    }

    /**
     * 列出指定文件夹下的笔记
     * <p>
     * 返回指定文件夹下的所有笔记列表（包含 ID 和标题）
     */
    @Tool(description = "列出指定文件夹下的所有笔记。folderId为0时表示根目录。")
    public String listNotes(
            @ToolParam(description = "文件夹ID，0表示根目录") Long folderId,
            @ToolParam(description = "用户ID") Long userId)
    {
        log.info("列出笔记: folderId={}, userId={}", folderId, userId);

        long targetFolderId = (folderId != null && folderId >= 0) ? folderId : 0;

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, 0)
                .eq(Note::getFolderId, targetFolderId)
                .orderByDesc(Note::getUpdatedAt);

        List<Note> notes = noteMapper.selectList(wrapper);

        if (notes.isEmpty())
        {
            String folderName = targetFolderId == 0 ? "根目录" : "该文件夹";
            return folderName + "下暂无笔记";
        }

        StringBuilder result = new StringBuilder();
        String folderName = targetFolderId == 0 ? "根目录" : "文件夹";
        result.append(folderName).append("下共有 ").append(notes.size()).append(" 篇笔记：\n");

        for (int i = 0; i < notes.size(); i++)
        {
            Note note = notes.get(i);
            result.append(String.format("%d. [ID: %d] %s\n", i + 1, note.getId(), note.getTitle()));
        }

        return result.toString();
    }

    /**
     * 获取笔记基本信息（不含内容）
     * <p>
     * 用于快速预览笔记的元数据信息
     */
    @Tool(description = "获取笔记的基本信息（标题、路径、标签等），不包含笔记内容。")
    public String getNoteInfo(
            @ToolParam(description = "笔记ID") Long noteId)
    {
        log.info("获取笔记信息: noteId={}", noteId);

        if (noteId == null)
        {
            return "错误：笔记 ID 不能为空";
        }

        Note note = noteMapper.selectById(noteId);

        if (note == null)
        {
            return "错误：笔记不存在（ID: " + noteId + "）";
        }

        StringBuilder result = new StringBuilder();
        result.append("【笔记 ID】").append(note.getId()).append("\n");
        result.append("【标题】").append(note.getTitle()).append("\n");
        result.append("【路径】").append(note.getPath()).append("\n");
        result.append("【可见性】").append(note.getVisibility()).append("\n");
        result.append("【标签】").append(note.getTags() != null ? note.getTags() : "无").append("\n");
        result.append("【创建时间】").append(note.getCreatedAt()).append("\n");
        result.append("【更新时间】").append(note.getUpdatedAt());

        return result.toString();
    }
}
