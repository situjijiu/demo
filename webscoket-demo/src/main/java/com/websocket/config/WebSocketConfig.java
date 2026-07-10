package com.websocket.config;

import com.websocket.ws.SpringChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket 配置类，注册 Handler、握手拦截器和心跳装饰器工厂
 */
@Configuration
@RequiredArgsConstructor
public class WebSocketConfig extends WebSocketConfigurationSupport {

    private final SpringChatWebSocketHandler springChatWebSocketHandler;

    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(springChatWebSocketHandler, "/ws/spring/chat")
                .addInterceptors(new UsernameHandshakeInterceptor())
                .setAllowedOrigins("*");
    }

    /**
     * 握手拦截器 — 从 URL query 参数中提取 username，注入到 WebSocket 会话属性中
     * 连接地址示例：ws://localhost:8080/ws/spring/chat?username=用户A
     */
    private static class UsernameHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            URI uri = request.getURI();
            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2 && "username".equals(kv[0])) {
                        attributes.put("username", kv[1]);
                        return true;
                    }
                }
            }
            attributes.put("username", "匿名用户");
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }
    }
}