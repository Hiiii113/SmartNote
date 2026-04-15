package hiiii113.smartnote.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hiiii113.smartnote.entity.Folder;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.mapper.FolderMapper;
import hiiii113.smartnote.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 可调用的笔记工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteTools
{
    private final NoteMapper noteMapper;
    private final FolderMapper folderMapper;
    private final VectorStore vectorStore;

    /**
     * 根据关键词搜索笔记
     * 使用向量数据库
     */
    @Tool(description = "根据语义搜索笔记，返回匹配的笔记ID和标题列表。当用户想查找某主题的笔记时使用。")
    public String searchNotes(
            @ToolParam(description = "搜索语义关键词，例如：关于工作计划的笔记") String keyword,
            @ToolParam(description = "用户ID") Long userId)
    {
        log.info("语义搜索笔记: keyword={}, userId={}", keyword, userId);

        // 构建元数据过滤条件（只搜索当前用户的笔记）
        var builder = new FilterExpressionBuilder();
        var filter = builder.eq("userId", userId).build(); // 匹配当前用户

        // 构建向量搜索请求
        SearchRequest request = SearchRequest.builder()
                .query(keyword)
                .filterExpression(filter)
                .topK(5)
                .build();

        // 执行搜索
        List<Document> results = vectorStore.similaritySearch(request);

        if (results != null && results.isEmpty())
        {
            return "未找到关于 \"" + keyword + "\" 的相关笔记。";
        }

        // 格式化输出
        StringBuilder result = new StringBuilder();
        if (results != null)
        {
            result.append("找到 ").append(results.size()).append(" 篇语义相关笔记：\n");

            // 每一篇笔记加上对应的东西
            for (Document doc : results)
            {
                String content = doc.getText();
                String title = (String) doc.getMetadata().getOrDefault("title", "无标题");
                String noteId = String.valueOf(doc.getMetadata().getOrDefault("noteId", "未知ID"));

                result.append("--- [ID: ").append(noteId).append(" | 标题: ").append(title).append("] ---\n");
                result.append(content).append("\n\n");
            }
        }

        return result.toString();
    }

    /**
     * 通过 ID 读取笔记完整内容
     * 根据笔记 ID 精确获取笔记的标题和内容
     */
    @Tool(description = "通过笔记ID读取笔记的完整内容，在文件夹搜索得到笔记 id 后调用使用")
    public String readNote(
            @ToolParam(description = "笔记ID，必须是有效的数字ID") Long noteId)
    {
        log.info("读取笔记: noteId={}", noteId);

        if (noteId == null)
        {
            return "错误：笔记 ID 不能为空，请提供有效的笔记ID";
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

        return "【笔记标题】" + note.getTitle() + "\n" +
                "【笔记路径】" + note.getPath() + "\n" +
                "【创建时间】" + note.getCreatedAt() + "\n" +
                "【更新时间】" + note.getUpdatedAt() + "\n" +
                "\n【笔记内容】\n" + (note.getContent() != null ? note.getContent() : "（内容为空）");
    }

    /**
     * 列出指定文件夹下的文件夹和笔记
     * 返回指定文件夹下的直接子文件夹和笔记列表（包含 ID 和标题）
     */
    @Tool(description = "列出指定文件夹下的直接子文件夹和笔记，folderId为0时表示根目录。如需查看子文件夹内容，请再次调用此方法。")
    public String listNotes(
            @ToolParam(description = "文件夹ID，0表示根目录") Long folderId,
            @ToolParam(description = "用户ID，从上下文中获取") Long userId)
    {
        log.info("列出文件夹和笔记: folderId={}, userId={}", folderId, userId);

        if (userId == null)
        {
            return "错误：用户ID不能为空";
        }

        long targetFolderId = (folderId != null && folderId >= 0) ? folderId : 0;

        // 查询子文件夹
        LambdaQueryWrapper<Folder> folderWrapper = new LambdaQueryWrapper<>();
        folderWrapper.eq(Folder::getUserId, userId)
                .eq(Folder::getIsDeleted, 0)
                .eq(Folder::getParentId, targetFolderId)
                .orderByAsc(Folder::getName);
        List<Folder> folders = folderMapper.selectList(folderWrapper);

        // 查询笔记
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, 0)
                .eq(Note::getFolderId, targetFolderId)
                .orderByDesc(Note::getUpdatedAt);
        List<Note> notes = noteMapper.selectList(noteWrapper);

        // 构建结果
        StringBuilder result = new StringBuilder();
        String folderName = targetFolderId == 0 ? "根目录" : "该文件夹";

        if (folders.isEmpty() && notes.isEmpty())
        {
            return folderName + "下暂无文件夹和笔记";
        }

        result.append(folderName).append("下有：\n");

        // 列出文件夹
        if (!folders.isEmpty())
        {
            result.append("\n【文件夹】共 ").append(folders.size()).append(" 个：\n");
            for (Folder folder : folders)
            {
                result.append(String.format("  📁 [ID: %d] %s\n", folder.getId(), folder.getName()));
            }
        }

        // 列出笔记
        if (!notes.isEmpty())
        {
            result.append("\n【笔记】共 ").append(notes.size()).append(" 篇：\n");
            for (Note note : notes)
            {
                result.append(String.format("  📄 [ID: %d] %s\n", note.getId(), note.getTitle()));
            }
        }

        result.append("\n提示：如需查看某个文件夹的内容，请使用该文件夹的ID再次调用此方法。");

        return result.toString();
    }

    /**
     * 获取笔记基本信息（不含内容）
     * 用于快速预览笔记的信息
     */
    @Tool(description = "获取笔记的基本信息（标题、路径、标签等），不包含笔记内容。")
    public String getNoteInfo(
            @ToolParam(description = "笔记ID，必须是有效的数字ID") Long noteId)
    {
        log.info("获取笔记信息: noteId={}", noteId);

        if (noteId == null)
        {
            return "错误：笔记 ID 不能为空，请提供有效的笔记ID";
        }

        Note note = noteMapper.selectById(noteId);

        if (note == null)
        {
            return "错误：笔记不存在（ID: " + noteId + "）";
        }

        return "【笔记 ID】" + note.getId() + "\n" +
                "【标题】" + note.getTitle() + "\n" +
                "【路径】" + note.getPath() + "\n" +
                "【可见性】" + note.getVisibility() + "\n" +
                "【标签】" + (note.getTags() != null ? note.getTags() : "无") + "\n" +
                "【创建时间】" + note.getCreatedAt() + "\n" +
                "【更新时间】" + note.getUpdatedAt();
    }
}
