package hiiii113.smartnote.dto;

import lombok.Data;

/**
 * 创建文件夹
 */
@Data
public class CreateFolderDto
{
    // 父文件夹 id
    private Long parentId;

    // 文件名
    private String name;
}
