package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记权限实体类
 */
@Data
@TableName("note_permission")
public class NotePermission
{
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long userId;

    // 是否可编辑 0-只读 1-可编辑
    private Integer canEdit;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
