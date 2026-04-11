package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.entity.FriendRequest;
import hiiii113.smartnote.entity.Friendship;
import hiiii113.smartnote.enums.FriendRequestStatusTypeEnum;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.FriendRequestMapper;
import hiiii113.smartnote.service.FriendRequestService;
import hiiii113.smartnote.service.FriendshipService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 好友申请 service 实现类
 */
@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest> implements FriendRequestService
{
    private final FriendshipService friendshipService;

    // 发送好友申请
    @Override
    public void sendRequest(Long requesterId, Long receiverId)
    {
        // 不能向自己发送申请
        if (requesterId.equals(receiverId))
        {
            throw new BusinessException("不能向自己发送好友申请", Result.CODE_BAD_REQUEST);
        }

        // 检查是否已经是好友
        if (friendshipService.isFriend(requesterId, receiverId))
        {
            throw new BusinessException("已经是好友了", Result.CODE_BAD_REQUEST);
        }

        // 检查是否已有待处理的申请
        FriendRequest existingRequest = lambdaQuery()
                .eq(FriendRequest::getRequesterId, requesterId)
                .eq(FriendRequest::getReceiverId, receiverId)
                .eq(FriendRequest::getStatus, FriendRequestStatusTypeEnum.PENDING) // 正在等待处理
                .one();

        if (existingRequest != null)
        {
            throw new BusinessException("已有待处理的好友申请", Result.CODE_BAD_REQUEST);
        }

        // 检查对方是否已向你发送过申请
        FriendRequest reverseRequest = lambdaQuery()
                .eq(FriendRequest::getRequesterId, receiverId)
                .eq(FriendRequest::getReceiverId, requesterId)
                .eq(FriendRequest::getStatus, FriendRequestStatusTypeEnum.PENDING) // 正在等待处理
                .one();

        if (reverseRequest != null)
        {
            throw new BusinessException("对方已向你发送过好友申请，请先处理", Result.CODE_BAD_REQUEST);
        }

        // 创建好友申请
        FriendRequest request = new FriendRequest();
        request.setRequesterId(requesterId);
        request.setReceiverId(receiverId);
        request.setStatus(FriendRequestStatusTypeEnum.PENDING); // 默认正在等待处理
        this.save(request);
    }

    // 获取收到的好友申请列表
    @Override
    public List<FriendRequest> getReceivedRequests(Long userId)
    {
        return lambdaQuery()
                .eq(FriendRequest::getReceiverId, userId)
                .eq(FriendRequest::getStatus, FriendRequestStatusTypeEnum.PENDING) // 正在等待处理的
                .orderByDesc(FriendRequest::getCreatedAt)
                .list();
    }

    // 获取发出的好友申请列表
    @Override
    public List<FriendRequest> getSentRequests(Long userId)
    {
        return lambdaQuery()
                .eq(FriendRequest::getRequesterId, userId)
                .orderByDesc(FriendRequest::getCreatedAt)
                .list();
    }

    // 处理好友申请
    @Override
    @Transactional // 需要同时处理两张表，开启事务
    public void handleRequest(Long userId, Long requestId, boolean accept)
    {
        // 查询申请
        FriendRequest request = this.getById(requestId);
        if (request == null)
        {
            throw new BusinessException("好友申请不存在", Result.CODE_NOT_FOUND);
        }

        // 校验是否是接收者
        if (!request.getReceiverId().equals(userId))
        {
            throw new BusinessException("无权处理此申请", Result.CODE_FORBIDDEN);
        }

        // 校验状态
        if (request.getStatus() != FriendRequestStatusTypeEnum.PENDING)
        {
            throw new BusinessException("该申请已处理", Result.CODE_BAD_REQUEST);
        }

        // 更新申请状态
        if (accept)
        {
            request.setStatus(FriendRequestStatusTypeEnum.ACCEPTED);
            this.updateById(request);
            // 建立双向好友关系
            createFriendship(request.getRequesterId(), request.getReceiverId());
        }
        else
        {
            request.setStatus(FriendRequestStatusTypeEnum.REJECTED);
            this.updateById(request);
        }
    }

    // 创建好友关系
    private void createFriendship(Long userId, Long friendId)
    {
        // userId 的字段
        Friendship friendship1 = new Friendship();
        friendship1.setUserId(userId);
        friendship1.setFriendId(friendId);
        friendship1.setGroupName("默认");
        friendshipService.save(friendship1);

        // friendId 的字段
        Friendship friendship2 = new Friendship();
        friendship2.setUserId(friendId);
        friendship2.setFriendId(userId);
        friendship2.setGroupName("默认");
        friendshipService.save(friendship2);
    }
}
