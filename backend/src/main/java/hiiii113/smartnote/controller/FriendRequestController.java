package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.FriendRequestDto;
import hiiii113.smartnote.dto.HandleFriendRequestDto;
import hiiii113.smartnote.dto.SendFriendRequestDto;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.FriendRequestService;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友申请
 */
@RestController
@RequestMapping("/friend-requests")
@RequiredArgsConstructor
public class FriendRequestController
{
    private final FriendRequestService friendRequestService;
    private final UserService userService;

    /**
     * 发送好友申请
     *
     * @param dto 相关数据
     */
    @PostMapping
    @LogAnnotation(module = "好友申请", operator = "发送好友申请")
    public Result<Void> sendRequest(@Valid @RequestBody SendFriendRequestDto dto)
    {
        // 获取发送请求的用户 id
        Long requesterId = StpUtil.getLoginIdAsLong();
        // 对方账号：手机号或者邮箱
        String account = dto.getAccount().trim();

        // 根据手机号或邮箱查找接收者
        User receiver = userService.getUserByAccount(account);

        if (receiver == null)
        {
            return Result.fail("用户不存在", Result.CODE_NOT_FOUND);
        }

        // 调用方法操作
        friendRequestService.sendRequest(requesterId, receiver.getId());
        return Result.ok("好友申请已发送");
    }

    /**
     * 获取收到的好友申请列表
     *
     * @return List<FriendRequestDto>
     */
    @GetMapping("/received")
    @LogAnnotation(module = "好友申请", operator = "获取收到的好友申请")
    public Result<List<FriendRequestDto>> getReceivedRequests()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取列表
        List<FriendRequestDto> requests = friendRequestService.getReceivedRequests(userId);
        return Result.ok(requests);
    }

    /**
     * 处理好友申请
     *
     * @param requestId 申请的 id
     * @param dto       相关数据
     */
    @PutMapping("/{requestId}")
    @LogAnnotation(module = "好友申请", operator = "处理好友申请")
    public Result<Void> handleRequest(@PathVariable Long requestId, @Valid @RequestBody HandleFriendRequestDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 处理请求
        friendRequestService.handleRequest(userId, requestId, dto.getAccept());
        return Result.ok(dto.getAccept() ? "已同意好友申请" : "已拒绝好友申请");
    }
}
