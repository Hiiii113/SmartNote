package hiiii113.smartnote.config;

import hiiii113.smartnote.websocket.ChatWebSocketHandler;
import hiiii113.smartnote.websocket.WsAuthInterceptor;
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

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(wsAuthInterceptor)  // 握手拦截器，验证 token
                .setAllowedOrigins("*");
    }
}
