package hiiii113.smartnote.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiiii113.smartnote.dto.ChatMessageDto;
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
        userSessions.put(userId, session);
        log.info("用户 {} 连接成功，当前在线人数: {}", userId, userSessions.size());
    }

    // 收到消息
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception
    {
        String payload = message.getPayload();

        // 心跳消息直接忽略
        if (payload.contains("\"type\":\"heartbeat\""))
        {
            return;
        }

        // 解析聊天消息
        ChatMessageDto dto = objectMapper.readValue(payload, ChatMessageDto.class);

        // 验证字段
        if (dto.getToId() == null || dto.getContent() == null)
        {
            return;
        }

        // 获取发送者 ID
        Long fromId = getUserId(session);

        // 保存消息
        chatMsgService.saveChatMsg(dto, fromId);

        // 推送给对方（如果在线）
        pushMessage(dto.getToId(), dto);
    }

    // 连接关闭
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
    {
        Long userId = getUserId(session);
        userSessions.remove(userId);
        log.info("用户 {} 断开连接，当前在线人数: {}", userId, userSessions.size());
    }

    // 推送消息（如果对方在线）
    public void pushMessage(Long toId, ChatMessageDto dto)
    {
        WebSocketSession session = userSessions.get(toId);
        if (session == null || !session.isOpen())
        {
            log.debug("用户 {} 不在线，不推送", toId);
            return;
        }

        try
        {
            String json = objectMapper.writeValueAsString(dto);
            session.sendMessage(new TextMessage(json));
            log.info("消息已推送给用户 {}", toId);
        }
        catch (Exception e)
        {
            log.error("推送消息失败: {}", e.getMessage());
        }
    }

    // 检查是否在线
    public boolean isOnline(Long userId)
    {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    // 从 session attributes 获取用户 ID
    private Long getUserId(WebSocketSession session)
    {
        Object userId = session.getAttributes().get(WsAuthInterceptor.USER_ID_KEY);
        return userId != null ? (Long) userId : null;
    }
}
