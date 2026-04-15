package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话记录实体类
 */
@Data
@TableName("ai_conversation")
public class AiConversation
{
    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户ID
    private Long userId;

    // 会话ID，用于区分不同对话
    private String conversationId;

    // 角色：user-用户 / assistant-AI
    private String role;

    // 对话内容
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
