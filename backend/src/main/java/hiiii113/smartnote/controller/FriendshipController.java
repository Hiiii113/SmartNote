package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.FriendDto;
import hiiii113.smartnote.dto.SetFriendGroupDto;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.FriendshipService;
import hiiii113.smartnote.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友关系
 */
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendshipController
{
    private final FriendshipService friendshipService;

    /**
     * 获取好友列表
     *
     * @return List<FriendDto>
     */
    @GetMapping
    @LogAnnotation(module = "好友", operator = "获取好友列表")
    public Result<List<FriendDto>> getFriendList()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取好友列表
        List<FriendDto> friendships = friendshipService.getFriendList(userId);
        return Result.ok(friendships);
    }

    /**
     * 删除好友
     *
     * @param friendId 好友 id
     */
    @DeleteMapping("/{friendId}")
    @LogAnnotation(module = "好友", operator = "删除好友")
    public Result<Void> deleteFriend(@PathVariable Long friendId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 删除好友
        friendshipService.deleteFriend(userId, friendId);
        return Result.ok("好友删除成功");
    }

    // 设置好友标签
    @PutMapping("/group/{friendId}")
    @LogAnnotation(module = "好友", operator = "设置好友标签")
    public Result<Void> setFriendGroup(@Valid @RequestBody SetFriendGroupDto dto, @PathVariable Long friendId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 设置
        friendshipService.setFriendGroup(userId, friendId, dto.getGroup());
        return Result.ok("好友分组设置成功");
    }
}
