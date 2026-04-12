package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ai总结实体类
 */
@Data
@TableName("ai_summary")
public class AiSummary
{
    private Long id;

    private Long NoteId;

    // 笔记总结的内容
    private String summary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
