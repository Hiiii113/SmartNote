package hiiii113.smartnote.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友信息返回
 */
@Data
public class FriendDto
{
    private Long id;

    // 好友信息
    private Long friendId;
    private String friendName;
    private String friendAvatar;
    private String friendMotto;

    // 分组
    private String groupName;

    private LocalDateTime createdAt;
}
