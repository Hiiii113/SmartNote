package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.dto.FriendDto;
import hiiii113.smartnote.entity.Friendship;

import java.util.List;

/**
 * 好友关系的 service 层
 */
public interface FriendshipService extends IService<Friendship>
{
    // 创建好友关系（双向）
    void createFriendship(Long userId, Long friendId);

    // 获取好友列表
    List<FriendDto> getFriendList(Long userId);

    // 删除好友
    void deleteFriend(Long userId, Long friendId);

    // 判断是否是好友
    boolean isFriend(Long userId, Long friendId);

    // 设置好友分组
    void setFriendGroup(Long userId, Long friendId, String group);
}
