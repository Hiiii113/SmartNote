package hiiii113.smartnote.websocket;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 */
@Slf4j
@Component
public class WsAuthInterceptor implements HandshakeInterceptor
{
    public static final String USER_ID_KEY = "userId";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes)
    {
        String query = request.getURI().getQuery();

        if (query == null || !query.contains("token="))
        {
            log.warn("WebSocket 握手失败：缺少 token 参数");
            return false;
        }

        // 提取 token，处理可能存在的尾部斜杠
        String token = query.replace("token=", "");
        // 移除 token 后面可能存在的路径部分（y-websocket 会追加 room name）
        if (token.contains("/"))
        {
            token = token.substring(0, token.indexOf("/"));
        }

        try
        {
            Object loginId = StpUtil.getLoginIdByToken(token);

            if (loginId == null)
            {
                log.warn("WebSocket 握手失败：token 无效，token={}", token);
                return false;
            }

            attributes.put(USER_ID_KEY, Long.parseLong(loginId.toString()));
            log.info("WebSocket 握手成功，用户 ID: {}", loginId);
            return true;
        }
        catch (NotLoginException e)
        {
            log.warn("WebSocket 握手失败：未登录，{}", e.getMessage());
            return false;
        }
        catch (Exception e)
        {
            log.error("WebSocket 握手异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, Exception exception)
    {
    }
}
