package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hiiii113.smartnote.dto.ChatMessageDto;
import hiiii113.smartnote.entity.ChatMsg;
import hiiii113.smartnote.service.ChatMsgService;
import hiiii113.smartnote.utils.Result;
import hiiii113.smartnote.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天接口
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController
{
    private final ChatMsgService chatMsgService;
    private final ChatWebSocketHandler webSocketHandler;

    // 发送消息
    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestBody ChatMessageDto dto)
    {
        // 获取当前用户 ID
        Long fromId = StpUtil.getLoginIdAsLong();

        // 保存消息
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setFromId(fromId);
        chatMsg.setToId(dto.getToId());
        chatMsg.setContent(dto.getContent());
        chatMsg.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : 1);
        chatMsg.setIsRead(0);
        chatMsgService.save(chatMsg);

        // 如果对方在线，推送消息
        if (webSocketHandler.isOnline(dto.getToId()))
        {
            webSocketHandler.pushMessage(dto.getToId(), "{\"fromId\":" + fromId
                    + ",\"toId\":" + dto.getToId()
                    + ",\"content\":\"" + dto.getContent() + "\""
                    + ",\"msgType\":" + chatMsg.getMsgType() + "}");
        }

        return Result.ok();
    }

    // 获取聊天记录
    @GetMapping("/history/{friendId}")
    public Result<List<ChatMsg>> getHistory(@PathVariable Long friendId)
    {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询双方的聊天记录
        LambdaQueryWrapper<ChatMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(w1 -> w1.eq(ChatMsg::getFromId, userId).eq(ChatMsg::getToId, friendId))
                .or(w2 -> w2.eq(ChatMsg::getFromId, friendId).eq(ChatMsg::getToId, userId))
        ).orderByAsc(ChatMsg::getCreatedAt);

        List<ChatMsg> history = chatMsgService.list(wrapper);
        return Result.ok(history);
    }

    // 获取未读消息数
    @GetMapping("/unread")
    public Result<Integer> getUnreadCount()
    {
        Long userId = StpUtil.getLoginIdAsLong();

        long count = chatMsgService.lambdaQuery()
                .eq(ChatMsg::getToId, userId)
                .eq(ChatMsg::getIsRead, 0)
                .count();

        return Result.ok((int) count);
    }

    // 标记已读
    @PostMapping("/read/{friendId}")
    public Result<Void> markAsRead(@PathVariable Long friendId)
    {
        Long userId = StpUtil.getLoginIdAsLong();

        chatMsgService.lambdaUpdate()
                .eq(ChatMsg::getFromId, friendId)
                .eq(ChatMsg::getToId, userId)
                .eq(ChatMsg::getIsRead, 0)
                .set(ChatMsg::getIsRead, 1)
                .update();

        return Result.ok();
    }
}
