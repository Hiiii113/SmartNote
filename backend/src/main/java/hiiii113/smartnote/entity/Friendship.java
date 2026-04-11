package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
@Data
@TableName("friendship")
public class Friendship
{
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long friendId;

    private String groupName;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
