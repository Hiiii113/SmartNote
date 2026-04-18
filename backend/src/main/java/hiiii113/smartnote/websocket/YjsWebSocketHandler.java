package hiiii113.smartnote.websocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Note WebSocket 处理器
 * 实现笔记协同编辑的广播功能
 * <p>
 */
@Slf4j
@Component
public class YjsWebSocketHandler extends BinaryWebSocketHandler
{
    // 每一个笔记房间都对应多个 session
    // key: noteId, value: 该笔记的所有编辑会话
    private static final Map<String, Set<WebSocketSession>> noteRooms = new ConcurrentHashMap<>();

    // session -> noteId 的映射，用于断开连接时快速清理
    private static final Map<String, String> sessionNoteMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session)
    {
        String noteId = getNoteId(session);
        if (noteId == null)
        {
            log.warn("无法获取笔记 ID，关闭连接");
            tryCloseSession(session);
            return;
        }

        // 添加
        noteRooms.computeIfAbsent(noteId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionNoteMap.put(session.getId(), noteId);

        int userCount = noteRooms.get(noteId).size();
        log.info("用户加入笔记协同，笔记 ID: {}, 当前在线人数: {}", noteId, userCount);
    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message)
    {
        String noteId = getNoteId(session);
        if (noteId == null)
        {
            return;
        }

        Set<WebSocketSession> sessions = noteRooms.get(noteId);
        if (sessions == null)
        {
            return;
        }

        // 获取消息字节数组
        byte[] payload = message.getPayload().array();

        // 广播给房间里所有人（包括发送者）
        // 前端通过 Yjs 的 origin 参数区分本地/远程更新
        int broadcastCount = 0;
        for (WebSocketSession s : sessions)
        {
            if (s.isOpen())
            {
                try
                {
                    s.sendMessage(new BinaryMessage(payload));
                    broadcastCount++;
                }
                catch (IOException e)
                {
                    log.warn("广播消息失败: {}", e.getMessage());
                    // 移除失效的 session
                    sessions.remove(s);
                }
            }
        }

        log.debug("笔记 {} 广播消息给 {} 个用户", noteId, broadcastCount);
    }

    // 处理心跳消息
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message)
    {
        String payload = message.getPayload();
        // 心跳消息直接忽略，不广播
        if (payload.contains("\"type\":\"heartbeat\""))
        {
            return;
        }
        // 其他文本消息也忽略（Yjs 只使用二进制消息）
        log.warn("收到非心跳的文本消息，已忽略: {}", payload);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
    {
        String noteId = sessionNoteMap.remove(session.getId());
        if (noteId != null)
        {
            Set<WebSocketSession> sessions = noteRooms.get(noteId);
            if (sessions != null)
            {
                sessions.remove(session);
                // 如果房间空了，移除房间
                if (sessions.isEmpty())
                {
                    noteRooms.remove(noteId);
                    log.info("笔记房间已清空，笔记 ID: {}", noteId);
                }
                else
                {
                    log.info("用户离开笔记协同，笔记 ID: {}, 剩余在线人数: {}", noteId, sessions.size());
                }
            }
        }
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception)
    {
        log.error("WebSocket 传输错误: {}", exception.getMessage());
    }

    // 从路径获取笔记 ID
    // y-websocket 会自动拼接 roomName 到 URL 末尾
    // 实际 URL 格式: /ws/yjs/{noteId}
    private String getNoteId(WebSocketSession session)
    {
        try
        {
            URI uri = session.getUri();
            if (uri == null)
            {
                return null;
            }
            String path = uri.getPath();
            log.debug("WebSocket 路径: {}", path);
            String[] parts = path.split("/");
            // /ws/yjs/{noteId} -> ["", "ws", "yjs", "noteId"]
            if (parts.length >= 4)
            {
                return parts[3];
            }
            return null;
        }
        catch (Exception e)
        {
            log.error("解析笔记 ID 失败: {}", e.getMessage());
            return null;
        }
    }

    // 安全关闭 session
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

    // 获取笔记当前在线人数
    public static int getOnlineCount(String noteId)
    {
        Set<WebSocketSession> sessions = noteRooms.get(noteId);
        return sessions == null ? 0 : sessions.size();
    }
}
