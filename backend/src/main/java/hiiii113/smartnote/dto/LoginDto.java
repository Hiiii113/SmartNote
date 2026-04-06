package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginDto
{

    // 手机号
    private String phone;

    // 邮箱
    private String email;

    // 密码不能为空
    @NotBlank(message = "密码不能为空")
    private String password;
}
