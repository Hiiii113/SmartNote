package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.entity.Friendship;

import java.util.List;

/**
 * 好友关系的 service 层
 */
public interface FriendshipService extends IService<Friendship>
{
    // 获取好友列表
    List<Friendship> getFriendList(Long userId);

    // 删除好友
    void deleteFriend(Long userId, Long friendId);

    // 判断是否是好友
    boolean isFriend(Long userId, Long friendId);
}
