package hiiii113.smartnote.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiiii113.smartnote.dto.NoteSyncMessageDto;
import hiiii113.smartnote.service.NoteService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Note WebSocket 处理器
 * 点击同步后进行推送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YjsWebSocketHandler extends TextWebSocketHandler
{
    // 最大长度
    private static final int WS_TEXT_MESSAGE_LIMIT = 64 * 1024;

    // noteId -> sessions
    private static final Map<String, Set<WebSocketSession>> noteRooms = new ConcurrentHashMap<>();
    // session -> note
    private static final Map<String, String> sessionNoteMap = new ConcurrentHashMap<>();

    private final NoteService noteService;
    private final ObjectMapper objectMapper;

    // websocket开启
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session)
    {
        // 设置大小限制
        session.setTextMessageSizeLimit(WS_TEXT_MESSAGE_LIMIT);

        String noteId = getNoteId(session);
        if (noteId == null)
        {
            log.warn("无法识别连接中的笔记 ID");
            tryCloseSession(session);
            return;
        }

        // 添加进池
        noteRooms.computeIfAbsent(noteId, key -> ConcurrentHashMap.newKeySet()).add(session);
        sessionNoteMap.put(session.getId(), noteId);

        log.info("用户加入笔记同步，noteId={}, onlineCount={}", noteId, getOnlineCount(noteId));
    }

    // 处理同步
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message)
    {
        String payload = message.getPayload();
        // 心跳忽略
        if (payload.contains("\"type\":\"heartbeat\""))
        {
            return;
        }

        log.debug("收到笔记同步消息，sessionId={}, payload={}", session.getId(), payload);
    }

    // 连接关闭
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
    {
        String noteId = sessionNoteMap.remove(session.getId());
        if (noteId == null)
        {
            return;
        }

        Set<WebSocketSession> sessions = noteRooms.get(noteId);
        if (sessions == null)
        {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty())
        {
            noteRooms.remove(noteId);
        }

        log.info("用户离开笔记同步，noteId={}, onlineCount={}", noteId, getOnlineCount(noteId));
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception)
    {
        String noteId = sessionNoteMap.get(session.getId());
        log.error("笔记同步 WebSocket 传输错误: noteId={}, sessionId={}, error={}", noteId, session.getId(), exception.getMessage());
    }

    // 推送消息
    public void pushNoteUpdated(Long noteId)
    {
        if (noteId == null)
        {
            return;
        }

        // noteId
        String roomKey = noteId.toString();
        // 对应的 sessions
        Set<WebSocketSession> sessions = noteRooms.get(roomKey);
        if (sessions == null || sessions.isEmpty())
        {
            return;
        }

        // 需要同步的消息
        NoteSyncMessageDto message = noteService.getNoteSyncMessage(noteId);
        if (message == null)
        {
            return;
        }

        try
        {
            String json = objectMapper.writeValueAsString(message);
            broadcast(roomKey, json); // 广播
        }
        catch (Exception e)
        {
            log.error("推送笔记同步消息失败: noteId={}, error={}", noteId, e.getMessage());
        }
    }

    // 获取在线人数
    public static int getOnlineCount(String noteId)
    {
        Set<WebSocketSession> sessions = noteRooms.get(noteId);
        return sessions == null ? 0 : sessions.size();
    }

    // 广播
    private void broadcast(String noteId, String payload)
    {
        Set<WebSocketSession> sessions = noteRooms.get(noteId);
        if (sessions == null)
        {
            return;
        }

        // 对每一个 session 都广播
        for (WebSocketSession session : sessions)
        {
            if (!session.isOpen())
            {
                sessions.remove(session);
                continue;
            }

            try
            {
                session.sendMessage(new TextMessage(payload));
            }
            catch (IOException e)
            {
                sessions.remove(session);
                log.warn("广播笔记同步消息失败: noteId={}, sessionId={}, error={}", noteId, session.getId(), e.getMessage());
            }
        }
    }

    private String getNoteId(WebSocketSession session)
    {
        try
        {
            URI uri = session.getUri();
            if (uri == null)
            {
                return null;
            }

            String[] parts = uri.getPath().split("/");
            if (parts.length >= 4)
            {
                return parts[3]; // 第四个片段
            }
            return null;
        }
        catch (Exception e)
        {
            log.error("解析笔记 ID 失败: {}", e.getMessage());
            return null;
        }
    }

    // 安全关闭
    private void tryCloseSession(WebSocketSession session)
    {
        try
        {
            if (session.isOpen())
            {
                session.close();
            }
        }
        catch (IOException e)
        {
            log.warn("关闭 session 失败: {}", e.getMessage());
        }
    }
}
