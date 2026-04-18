package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重命名文件夹
 */
@Data
public class RenameFolderDto
{
    // 修改后的文件名
    @NotBlank(message = "文件夹名称不能为空")
    private String name;
}
