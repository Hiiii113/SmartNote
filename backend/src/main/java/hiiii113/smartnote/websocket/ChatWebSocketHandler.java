package hiiii113.smartnote.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import hiiii113.smartnote.dto.ChatMessageDto;
import hiiii113.smartnote.entity.ChatMsg;
import hiiii113.smartnote.service.ChatMsgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 聊天处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler
{
    // 用户连接池：userId -> WebSocketSession
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final ChatMsgService chatMsgService;
    private final ObjectMapper objectMapper;

    // 连接建立
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session)
    {
        Long userId = getUserId(session);
        if (userId != null)
        {
            userSessions.put(userId, session);
            log.info("用户 {} 连接成功，当前在线人数: {}", userId, userSessions.size());
        }
    }

    // 收到消息
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception
    {
        String payload = message.getPayload();
        log.info("收到消息: {}", payload);

        ChatMessageDto dto = objectMapper.readValue(payload, ChatMessageDto.class);

        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setFromId(dto.getFromId());
        chatMsg.setToId(dto.getToId());
        chatMsg.setContent(dto.getContent());
        chatMsg.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : 1);
        chatMsg.setIsRead(0);
        chatMsgService.save(chatMsg);

        pushMessage(dto.getToId(), payload);
    }

    // 连接关闭
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
    {
        Long userId = getUserId(session);
        if (userId != null)
        {
            userSessions.remove(userId);
            log.info("用户 {} 断开连接，当前在线人数: {}", userId, userSessions.size());
        }
    }

    // 推送消息
    public void pushMessage(Long userId, String message)
    {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen())
        {
            try
            {
                session.sendMessage(new TextMessage(message));
                log.info("消息已推送给用户 {}", userId);
            }
            catch (Exception e)
            {
                log.error("推送消息失败: {}", e.getMessage());
            }
        }
    }

    // 检查是否在线
    public boolean isOnline(Long userId)
    {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    // 从 session 获取用户 ID
    private Long getUserId(WebSocketSession session)
    {
        try
        {
            String query = session.getUri().getQuery();
            if (query == null) return null;
            String token = query.replace("token=", "");
            return Long.parseLong(StpUtil.getLoginIdByToken(token).toString());
        }
        catch (Exception e)
        {
            log.error("解析 token 失败: {}", e.getMessage());
            return null;
        }
    }
}
