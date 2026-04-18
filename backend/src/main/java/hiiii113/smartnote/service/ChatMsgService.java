package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.dto.ChatMessageDto;
import hiiii113.smartnote.entity.ChatMsg;

import java.util.List;

/**
 * 好友私聊消息的 service
 */
public interface ChatMsgService extends IService<ChatMsg>
{
    // 保存聊天消息
    void saveChatMsg(ChatMessageDto dto, Long fromId);

    // 获取聊天记录
    List<ChatMsg> getChatMsg(Long userId, Long friendId);

    // 获取未读消息数
    Integer getUnreadCount(Long userId);

    // 标记为已读
    void markAsRead(Long userId, Long friendId);

    // 构建推送消息
    ChatMessageDto buildChatMessageDto(Long fromId, ChatMessageDto dto);
}
