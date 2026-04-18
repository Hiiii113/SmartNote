package hiiii113.smartnote.dto;

import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import lombok.Data;

/**
 * 更新笔记
 */
@Data
public class UpdateNoteDto
{
    // 标题
    private String title;

    // 内容
    private String content;

    // 标签
    private String tags;
}
