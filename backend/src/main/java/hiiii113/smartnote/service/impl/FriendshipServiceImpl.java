package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.entity.Friendship;
import hiiii113.smartnote.mapper.FriendshipMapper;
import hiiii113.smartnote.service.FriendshipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 好友关系 service 实现类
 */
@Service
public class FriendshipServiceImpl extends ServiceImpl<FriendshipMapper, Friendship> implements FriendshipService
{
    // 获取好友列表
    @Override
    public List<Friendship> getFriendList(Long userId)
    {
        return lambdaQuery()
                .eq(Friendship::getUserId, userId)
                .list();
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
}
