package com.websocket.ws;


import cn.hutool.core.util.StrUtil;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{username}")
public class ChatWebSocket {


    private static final ConcurrentHashMap<String, Session> onlineSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        String format = StrUtil.format("system:join:{}", username);
        log.info(format);
        onlineSessions.put(username, session);
        session.getAsyncRemote().sendText(format);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("username") String username) {
        if ("ping".equals(message)) {
            log.info("收到心跳 ping");
            session.getAsyncRemote().sendText("pong", result -> {
                if (result.isOK()) {
                    log.info("心跳响应 pong 发送成功");
                }
            });
            return;
        }
        String format = StrUtil.format("msg:{}:{}", username, message);
        log.info(format);
        broadcast(format, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        String format = StrUtil.format("system:leave:{}", username);
        log.info(format);
        onlineSessions.remove(username);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private static void broadcast(String msg) {
        onlineSessions.forEach((username, onlineSession) -> {
            if (onlineSession.isOpen()) {
                onlineSession.getAsyncRemote().sendText(msg, result -> {
                    if (!result.isOK()) {
                        log.error("发送失败：{}", result.getException().getMessage());
                    }
                });
            }
        });
    }

    private static void broadcast(String msg, Session session) {
        onlineSessions.forEach((username, onlineSession) -> {
            if (onlineSession.isOpen() && !Objects.equals(onlineSession, session)) {
                onlineSession.getAsyncRemote().sendText(msg, result -> {
                    if (!result.isOK()) {
                        log.error("发送失败：{}", result.getException().getMessage());
                    }
                });
            }
        });
    }
}