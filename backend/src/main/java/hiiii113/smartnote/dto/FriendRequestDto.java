package hiiii113.smartnote.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友申请返回
 */
@Data
public class FriendRequestDto
{
    private Long id;

    // 申请人信息
    private Long requesterId;
    private String requesterName;
    private String requesterAvatar;

    // 接收者信息
    private Long receiverId;
    private String receiverName;
    private String receiverAvatar;

    // 状态
    private String status;

    private LocalDateTime createdAt;
}
