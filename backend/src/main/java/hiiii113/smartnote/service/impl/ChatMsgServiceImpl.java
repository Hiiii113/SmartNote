package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.ChatMessageDto;
import hiiii113.smartnote.entity.ChatMsg;
import hiiii113.smartnote.mapper.ChatMsgMapper;
import hiiii113.smartnote.service.ChatMsgService;
import org.springframework.stereotype.Service;

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
        chatMsg.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : 1);
        chatMsg.setIsRead(0);
        save(chatMsg);
    }
}
