package hiiii113.smartnote.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记权限返回
 */
@Data
public class NotePermissionDto
{
    private Long id;

    private Long userId;

    private String username;

    private String avatar;

    private Integer canEdit;

    private LocalDateTime createdAt;
}
