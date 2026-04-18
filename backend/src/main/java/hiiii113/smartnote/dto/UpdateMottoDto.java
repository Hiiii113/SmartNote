package hiiii113.smartnote.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 更新座右铭
 */
@Data
public class UpdateMottoDto
{
    @Length(max = 100, message = "座右铭不能超过100个字符")
    private String motto;
}
