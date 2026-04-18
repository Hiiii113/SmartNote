package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新用户名
 */
@Data
public class UpdateUsernameDto
{
    @NotBlank(message = "用户名不能为空")
    private String username;
}
