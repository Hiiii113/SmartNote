package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 浏览历史实体类
 */
@Data
@TableName("browse_history")
public class BrowseHistory
{
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long noteId;

    @TableField(exist = false)
    private String noteTitle;

    private Integer viewCount;

    @TableField("browsed_at")
    private LocalDateTime browsedAt;
}
