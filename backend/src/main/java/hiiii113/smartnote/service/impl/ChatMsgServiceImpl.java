package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.ChatMessageDto;
import hiiii113.smartnote.entity.ChatMsg;
import hiiii113.smartnote.mapper.ChatMsgMapper;
import hiiii113.smartnote.service.ChatMsgService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友私聊消息 service 实现类
 */
@Service
public class ChatMsgServiceImpl extends ServiceImpl<ChatMsgMapper, ChatMsg> implements ChatMsgService
{
    @Override
    public void saveChatMsg(ChatMessageDto dto, Long fromId)
    {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setFromId(fromId);
        chatMsg.setToId(dto.getToId());
        chatMsg.setContent(dto.getContent());
        chatMsg.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : 1); // 1-文字 2-图片
        chatMsg.setIsRead(0);
        save(chatMsg);
    }

    @Override
    public List<ChatMsg> getChatMsg(Long userId, Long friendId)
    {
        return lambdaQuery()
                .and(w -> w
                        .and(w1 -> w1.eq(ChatMsg::getFromId, userId).eq(ChatMsg::getToId, friendId))
                        .or(w2 -> w2.eq(ChatMsg::getFromId, friendId).eq(ChatMsg::getToId, userId))
                )
                .orderByAsc(ChatMsg::getCreatedAt)
                .list();
    }

    @Override
    public Integer getUnreadCount(Long userId)
    {
        Long res = lambdaQuery()
                .eq(ChatMsg::getToId, userId)
                .eq(ChatMsg::getIsRead, 0)
                .count();

        return Math.toIntExact(res);
    }

    @Override
    public void markAsRead(Long userId, Long friendId)
    {
        lambdaUpdate()
                .eq(ChatMsg::getFromId, friendId)
                .eq(ChatMsg::getToId, userId)
                .eq(ChatMsg::getIsRead, 0)
                .set(ChatMsg::getIsRead, 1)
                .update();
    }

    // 构建推送消息
    @Override
    public ChatMessageDto buildChatMessageDto(Long fromId, ChatMessageDto dto)
    {
        ChatMessageDto pushDto = new ChatMessageDto();
        pushDto.setFromId(fromId);
        pushDto.setToId(dto.getToId());
        pushDto.setContent(dto.getContent());
        pushDto.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : 1);
        return pushDto;
    }
}
