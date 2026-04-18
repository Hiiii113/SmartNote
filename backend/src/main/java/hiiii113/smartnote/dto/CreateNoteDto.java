package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建笔记
 */
@Data
public class CreateNoteDto
{
    // 父文件夹 id
    @NotNull(message = "文件夹ID不能为空")
    private Long folderId;

    // 标题
    @NotBlank(message = "笔记标题不能为空")
    private String title;

    // 内容
    private String content;

    // 标签
    private String tags;
}
