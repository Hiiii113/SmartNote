package hiiii113.smartnote.dto;

import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 返回给前端的笔记详情
 */
@Data
public class NoteDetailDto
{
    // 标题
    private String title;

    // 正文内容
    private String content;

    // 可见性
    private NoteVisibilityTypeEnum visibility;

    // 标签
    private String tags;

    // 最后更新时间
    private LocalDateTime updatedAt;

    // 是否可编辑（当前用户）
    private Boolean canEdit;
}
