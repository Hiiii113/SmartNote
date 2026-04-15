package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.FriendRequestDto;
import hiiii113.smartnote.dto.HandleFriendRequestDto;
import hiiii113.smartnote.dto.SendFriendRequestDto;
import hiiii113.smartnote.entity.FriendRequest;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.FriendRequestService;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友申请 controller
 */
@RestController
@RequestMapping("/friend-requests")
@RequiredArgsConstructor
public class FriendRequestController
{
    private final FriendRequestService friendRequestService;
    private final UserService userService;

    // 发送好友申请
    @PostMapping
    @LogAnnotation(module = "好友申请", operator = "发送好友申请")
    public Result<Void> sendRequest(@RequestBody SendFriendRequestDto dto)
    {
        // 获取发送请求的用户 id
        Long requesterId = StpUtil.getLoginIdAsLong();
        String account = dto.getContact(); // 手机号或者邮箱
        if (account == null || account.trim().isEmpty())
        {
            return Result.fail("请输入手机号或邮箱", Result.CODE_BAD_REQUEST);
        }

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

    // 获取收到的好友申请列表
    @GetMapping("/received")
    @LogAnnotation(module = "好友申请", operator = "获取收到的好友申请")
    public Result<List<FriendRequestDto>> getReceivedRequests()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取列表
        List<FriendRequest> requests = friendRequestService.getReceivedRequests(userId);
        // 转化成 dto 并返回给前端
        List<FriendRequestDto> dtoList = requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return Result.ok(dtoList);
    }

    // 获取发出的好友申请列表
    @GetMapping("/sent")
    @LogAnnotation(module = "好友申请", operator = "获取发出的好友申请")
    public Result<List<FriendRequestDto>> getSentRequests()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取列表
        List<FriendRequest> requests = friendRequestService.getSentRequests(userId);
        // 转化成 dto 并返回给前端
        List<FriendRequestDto> dtoList = requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return Result.ok(dtoList);
    }

    // 处理好友申请
    @PutMapping("/{requestId}")
    @LogAnnotation(module = "好友申请", operator = "处理好友申请")
    public Result<Void> handleRequest(@PathVariable Long requestId, @RequestBody HandleFriendRequestDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 处理请求
        friendRequestService.handleRequest(userId, requestId, dto.getAccept());
        return Result.ok(dto.getAccept() ? "已同意好友申请" : "已拒绝好友申请");
    }

    // 转换为 DTO
    private FriendRequestDto convertToDto(FriendRequest request)
    {
        FriendRequestDto dto = new FriendRequestDto();
        dto.setId(request.getId());
        dto.setRequesterId(request.getRequesterId());
        dto.setReceiverId(request.getReceiverId());
        dto.setStatus(request.getStatus().name());
        dto.setCreatedAt(request.getCreatedAt());

        // 查询申请人信息（名字和头像）
        User requester = userService.getById(request.getRequesterId());
        if (requester != null)
        {
            dto.setRequesterName(requester.getUsername());
            dto.setRequesterAvatar(requester.getAvatar());
        }

        // 查询接收者信息（名字和头像）
        User receiver = userService.getById(request.getReceiverId());
        if (receiver != null)
        {
            dto.setReceiverName(receiver.getUsername());
            dto.setReceiverAvatar(receiver.getAvatar());
        }
        return dto;
    }
}
