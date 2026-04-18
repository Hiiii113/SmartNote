package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.FriendDto;
import hiiii113.smartnote.entity.Friendship;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.FriendshipMapper;
import hiiii113.smartnote.mapper.UserMapper;
import hiiii113.smartnote.service.FriendshipService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友关系 service 实现类
 */
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl extends ServiceImpl<FriendshipMapper, Friendship> implements FriendshipService
{
    private final UserMapper userMapper;

    @Override
    public void createFriendship(Long userId, Long friendId)
    {
        // 看看这两个是否存在
        User user = userMapper.selectById(userId);
        User friend = userMapper.selectById(friendId);
        if (user == null || friend == null)
        {
            throw new BusinessException("一方账户不存在", Result.CODE_BAD_REQUEST);
        }

        // userId 的字段
        Friendship friendship1 = new Friendship();
        friendship1.setUserId(userId);
        friendship1.setFriendId(friendId);
        friendship1.setGroupName("默认");
        this.save(friendship1);

        // friendId 的字段
        Friendship friendship2 = new Friendship();
        friendship2.setUserId(friendId);
        friendship2.setFriendId(userId);
        friendship2.setGroupName("默认");
        this.save(friendship2);
    }

    // 获取好友列表
    @Override
    public List<FriendDto> getFriendList(Long userId)
    {
        return lambdaQuery()
                .eq(Friendship::getUserId, userId)
                .list()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 删除好友关系
    @Override
    @Transactional // 同时修改，添加事务
    public void deleteFriend(Long userId, Long friendId)
    {
        // 删除 userId 的好友关系表
        lambdaUpdate()
                .eq(Friendship::getUserId, userId)
                .eq(Friendship::getFriendId, friendId)
                .remove();

        // 删除 friendId 的好友关系表
        lambdaUpdate()
                .eq(Friendship::getUserId, friendId)
                .eq(Friendship::getFriendId, userId)
                .remove();
    }

    // 判断是否是好友
    @Override
    public boolean isFriend(Long userId, Long friendId)
    {
        return lambdaQuery()
                .eq(Friendship::getUserId, userId)
                .eq(Friendship::getFriendId, friendId)
                .exists();
    }

    @Override
    public void setFriendGroup(Long userId, Long friendId, String group)
    {
        lambdaUpdate()
                .eq(Friendship::getUserId, userId)
                .eq(Friendship::getFriendId, friendId)
                .set(Friendship::getGroupName, group)
                .update();
    }

    private FriendDto convertToDto(Friendship friendship)
    {
        FriendDto dto = new FriendDto();
        dto.setId(friendship.getId());
        dto.setFriendId(friendship.getFriendId());
        dto.setGroupName(friendship.getGroupName());
        dto.setCreatedAt(friendship.getCreatedAt());

        // 查询好友信息（设置用户名，头像地址和座右铭）
        User friend = userMapper.selectById(friendship.getFriendId());
        if (friend != null)
        {
            dto.setFriendName(friend.getUsername());
            dto.setFriendAvatar(friend.getAvatar());
            dto.setFriendMotto(friend.getMotto());
        }
        return dto;
    }
}
