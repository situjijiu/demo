package com.websocket.ws;


import cn.hutool.core.util.StrUtil;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import cn.hutool.json.JSONUtil;
import com.websocket.enums.Type;
import com.websocket.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{username}")
public class ChatWebSocket {


    private static final ConcurrentHashMap<String, Session> onlineSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        ChatMessage msg = ChatMessage.builder()
                .type(Type.SYSTEM)
                .from(username)
                .content("进入了聊天室")
                .time(LocalDateTime.now()).build();
        String format = JSONUtil.toJsonStr(msg);
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

        try {
            cn.hutool.json.JSONObject jsonObj = JSONUtil.parseObj(message);
            String msgType = jsonObj.getStr("type");
            String content = jsonObj.getStr("content");

            ChatMessage chatMessage = ChatMessage.builder()
                    .type(msgType != null ? msgType : Type.CHAT)
                    .from(username)
                    .content(content != null ? content : "")
                    .time(LocalDateTime.now()).build();
            log.info(JSONUtil.toJsonPrettyStr(chatMessage));
            broadcast(chatMessage, session);
        } catch (Exception e) {
            log.error("消息解析失败: {}", e.getMessage());
            ChatMessage chatMessage = ChatMessage.builder()
                    .type(Type.CHAT)
                    .from(username)
                    .content(message)
                    .time(LocalDateTime.now()).build();
            broadcast(chatMessage, session);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        ChatMessage format = ChatMessage.builder()
                .type(Type.SYSTEM)
                .from(username)
                .content("离开了聊天室")
                .time(LocalDateTime.now()).build();
        log.info(JSONUtil.toJsonPrettyStr(format));
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

    private static void broadcast(Object message, Session session) {
        String jsonStr = JSONUtil.toJsonStr(message);
        onlineSessions.forEach((username, onlineSession) -> {
            if (onlineSession.isOpen() && !Objects.equals(onlineSession, session)) {
                onlineSession.getAsyncRemote().sendText(jsonStr, result -> {
                    if (!result.isOK()) {
                        log.error("发送失败：{}", result.getException().getMessage());
                    }
                });
            }
        });
    }
}