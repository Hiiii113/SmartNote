package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友私聊消息实体类
 */
@Data
@TableName("chat_msg")
public class ChatMsg
{
    @TableId(type = IdType.AUTO)
    private Long id;

    // 发送者
    private Long fromId;

    // 接受者
    private Long toId;

    // 内容
    private String content;

    // 类型：1-文字 2-图片
    private Integer msgType;

    // 是否已读
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
