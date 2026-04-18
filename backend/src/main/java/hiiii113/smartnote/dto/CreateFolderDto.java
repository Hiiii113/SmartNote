package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建文件夹
 */
@Data
public class CreateFolderDto
{
    // 父文件夹 id
    @NotNull(message = "父文件夹ID不能为空")
    private Long parentId;

    // 文件名
    @NotBlank(message = "文件夹名称不能为空")
    private String name;
}
