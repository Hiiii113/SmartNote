package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.ChatMessageDto;
import hiiii113.smartnote.entity.ChatMsg;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.ChatMsgService;
import hiiii113.smartnote.utils.Result;
import hiiii113.smartnote.websocket.ChatWebSocketHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天接口 controller
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController
{
    private final ChatMsgService chatMsgService;
    private final ChatWebSocketHandler webSocketHandler;

    /**
     * 发送消息
     *
     * @param dto 相关信息 dto
     */
    @PostMapping("/send")
    @LogAnnotation(module = "聊天", operator = "发送消息")
    public Result<Void> sendMessage(@Valid @RequestBody ChatMessageDto dto)
    {
        // 发送者 id
        Long fromId = StpUtil.getLoginIdAsLong();

        // 保存消息到数据库
        chatMsgService.saveChatMsg(dto, fromId);

        // 构建推送消息
        ChatMessageDto pushDto = chatMsgService.buildChatMessageDto(fromId, dto);

        // 推送给对方（websocket）
        webSocketHandler.pushMessage(dto.getToId(), pushDto);

        return Result.ok("消息发送成功");
    }

    /**
     * 获取聊天记录
     *
     * @param friendId 好友 id
     * @return List<ChatMsg>
     */
    @GetMapping("/history/{friendId}")
    @LogAnnotation(module = "聊天", operator = "获取聊天记录")
    public Result<List<ChatMsg>> getHistory(@PathVariable Long friendId)
    {
        // 当前用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取
        List<ChatMsg> history = chatMsgService.getChatMsg(userId, friendId);
        // 返回
        return Result.ok(history);
    }

    /**
     * 获取未读消息数
     *
     * @return Integer
     */
    @GetMapping("/unread")
    @LogAnnotation(module = "聊天", operator = "获取未读消息数")
    public Result<Integer> getUnreadCount()
    {
        // 当前用户 id
        Long userId = StpUtil.getLoginIdAsLong();

        // 获取未读数量
        Integer count = chatMsgService.getUnreadCount(userId);
        return Result.ok(count);
    }

    /**
     * 标记已读
     *
     * @param friendId 好友 id
     */
    @PostMapping("/read/{friendId}")
    @LogAnnotation(module = "聊天", operator = "标记已读")
    public Result<Void> markAsRead(@PathVariable Long friendId)
    {
        // 当前用户 id
        Long userId = StpUtil.getLoginIdAsLong();

        // 标记为已读
        chatMsgService.markAsRead(userId, friendId);
        return Result.ok("消息已标记为已读");
    }
}
