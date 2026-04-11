package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.FriendDto;
import hiiii113.smartnote.entity.Friendship;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.service.FriendshipService;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友关系 controller
 */
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendshipController
{
    private final FriendshipService friendshipService;
    private final UserService userService;

    // 获取好友列表
    @GetMapping
    public Result<List<FriendDto>> getFriendList()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取好友列表
        List<Friendship> friendships = friendshipService.getFriendList(userId);
        // 转换成 dto 返回给前端
        List<FriendDto> dtoList = friendships.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return Result.ok(dtoList);
    }

    // 删除好友
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(@PathVariable Long friendId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 删除好友
        friendshipService.deleteFriend(userId, friendId);
        return Result.ok("已删除好友");
    }

    // 转换为 DTO
    private FriendDto convertToDto(Friendship friendship)
    {
        FriendDto dto = new FriendDto();
        dto.setId(friendship.getId());
        dto.setFriendId(friendship.getFriendId());
        dto.setGroupName(friendship.getGroupName());
        dto.setCreatedAt(friendship.getCreatedAt());

        // 查询好友信息（设置用户名，头像地址和座右铭）
        User friend = userService.getById(friendship.getFriendId());
        if (friend != null)
        {
            dto.setFriendName(friend.getUsername());
            dto.setFriendAvatar(friend.getAvatar());
            dto.setFriendMotto(friend.getMotto());
        }
        return dto;
    }
}
