package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 修改密码 DTO
 */
@Data
public class UpdatePasswordDto
{
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Length(min = 8, message = "密码长度必须大于8位")
    @Length(max = 20, message = "密码过长")
    private String newPassword;
}
