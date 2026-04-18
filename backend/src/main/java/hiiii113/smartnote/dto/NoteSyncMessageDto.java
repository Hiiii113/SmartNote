package hiiii113.smartnote.dto;

import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记同步消息
 */
@Data
public class NoteSyncMessageDto
{
    // 类型
    private String type;

    private Long noteId;

    private String title;

    private String content;

    private String tags;

    private NoteVisibilityTypeEnum visibility;

    private LocalDateTime updatedAt;
}
