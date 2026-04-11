package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User
{
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String phone;

    private String email;

    private String password;

    private String avatar;

    private String motto;

    private Integer isBanned;

    @TableLogic // 逻辑删除，加上了之后自动处理逻辑
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
