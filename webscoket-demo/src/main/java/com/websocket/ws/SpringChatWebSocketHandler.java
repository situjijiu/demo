package com.websocket.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 聊天业务处理器
 * 负责用户会话管理和消息广播，不包含心跳等基础设施逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringChatWebSocketHandler extends TextWebSocketHandler {

    private final ApplicationEventPublisher publisher;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username == null) {
            username = "匿名-" + session.getId().substring(0, 8);
            log.warn("用户名未设置，使用降级标识：{}", username);
        }
        sessions.put(username, session);
        publisher.publishEvent(username + "已登录聊天室");
        log.info("{} 连接成功", username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String username = session.getAttributes().get("username").toString();

        log.info("{}发送消息：{}", username, payload);

        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            sessions.remove(username);
        }
        log.info("{} 连接关闭，状态：{}", username, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String username = (String) session.getAttributes().get("username");
        log.error("{} 传输错误：{}", username, exception.getMessage());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
        if (username != null) {
            sessions.remove(username);
        }
    }
}