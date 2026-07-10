package com.websocket.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpringChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话属性中获取用户名（由 HandshakeInterceptor 注入）
        String username = (String) session.getAttributes().get("username");
        if (username == null) {
            // 容错：如果用户名未设置，使用 session ID 作为降级标识
            username = "匿名-" + session.getId().substring(0, 8);
            log.warn("用户名未设置，使用降级标识：{}", username);
        }
        sessions.put(username, session);
        log.info("{} 连接成功", username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String username = session.getAttributes().get("username").toString();

        log.info("{}发送消息：{}", username, payload);

        // 广播给所有在线用户（排除发送者自身）
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.values().remove(session);
        log.info("连接关闭，状态：{}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("传输错误：{}", exception.getMessage());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
}
