package hiiii113.smartnote.config;

import hiiii113.smartnote.websocket.ChatWebSocketHandler;
import hiiii113.smartnote.websocket.WsAuthInterceptor;
import hiiii113.smartnote.websocket.YjsWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer
{
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WsAuthInterceptor wsAuthInterceptor;
    private final YjsWebSocketHandler yjsWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(chatWebSocketHandler, "/ws/chat") // "/ws/chat" 路径的都交给 ChatWebSocketHandler 处理
                .addInterceptors(wsAuthInterceptor) // 被 wsAuthInterceptor 拦截
                .setAllowedOrigins("*");

        registry.addHandler(yjsWebSocketHandler, "/ws/yjs/*") // "/ws/chat" 路径的都交给 YjsWebSocketHandler 处理
                .addInterceptors(wsAuthInterceptor) // 被 wsAuthInterceptor 拦截
                .setAllowedOrigins("*");
    }
}
