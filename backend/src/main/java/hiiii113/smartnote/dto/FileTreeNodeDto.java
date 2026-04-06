package hiiii113.smartnote.dto;

import hiiii113.smartnote.enums.FileTreeNodeTypeEnum;
import lombok.Data;

/**
 * 返回给前端的文件树节点的 dto
 */
@Data
public class FileTreeNodeDto
{
    // 节点的 id，也是文件夹、笔记的主键 id
    private Long id;

    // 显示的名称（文件夹名、笔记标题）
    private String name;

    // 父节点 id（上一级的文件夹 id）
    private Long parentId;

    // 节点类型（枚举类定义）
    private FileTreeNodeTypeEnum type;
}
