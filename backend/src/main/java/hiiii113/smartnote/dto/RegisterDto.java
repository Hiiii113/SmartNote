package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDto
{

    // 手机号（第一位-1 第二位3-9 长度共11位）
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确"
    )
    private String phone;

    // 邮箱（123abcABC_-@123abcABC_-.123abcABC_-.123abcABC_-）
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+",
            message = "邮箱格式不正确"
    )
    @Length(max = 20, message = "邮箱长度过长")
    private String email;

    // 密码不能为空
    @NotBlank(message = "密码不能为空")
    @Length(min = 8, message = "密码长度必须大于8位")
    @Length(max = 20, message = "密码过长")
    private String password;
}
