package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记实体类
 */
@Data
public class Note
{
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long folderId;

    private String path;

    private String title;

    private String content;

    private NoteVisibilityTypeEnum visibility;

    private String tags;

    private Integer viewCount;

    private Integer isDeleted;

    private LocalDateTime deletedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
