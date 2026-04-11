package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import hiiii113.smartnote.enums.FriendRequestStatusTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友申请实体类
 */
@Data
@TableName("friend_request")
public class FriendRequest
{
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long requesterId;

    private Long receiverId;

    private FriendRequestStatusTypeEnum status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
