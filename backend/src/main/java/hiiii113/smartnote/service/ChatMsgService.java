package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.dto.ChatMessageDto;
import hiiii113.smartnote.entity.ChatMsg;

/**
 * 好友私聊消息 service 层
 */
public interface ChatMsgService extends IService<ChatMsg>
{
    // 保存聊天消息
    void saveChatMsg(ChatMessageDto dto, Long fromId);
}
