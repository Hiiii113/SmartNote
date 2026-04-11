package hiiii113.smartnote.dto;

import lombok.Data;

/**
 * 创建笔记
 */
@Data
public class CreateNoteDto
{
    // 父文件夹 id
    private Long folderId;

    // 标题
    private String title;

    // 内容
    private String content;

    // 标签
    private String tags;
}
