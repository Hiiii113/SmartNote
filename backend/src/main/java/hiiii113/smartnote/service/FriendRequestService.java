package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.entity.FriendRequest;

import java.util.List;

/**
 * 好友申请的 service 层
 */
public interface FriendRequestService extends IService<FriendRequest>
{
    // 发送好友申请
    void sendRequest(Long requesterId, Long receiverId);

    // 获取收到的好友申请列表
    List<FriendRequest> getReceivedRequests(Long userId);

    // 获取发出的好友申请列表
    List<FriendRequest> getSentRequests(Long userId);

    // 处理好友申请（同意或拒绝）
    void handleRequest(Long userId, Long requestId, boolean accept);
}
