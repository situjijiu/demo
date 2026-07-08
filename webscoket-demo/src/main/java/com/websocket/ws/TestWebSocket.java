package com.websocket.ws;

import cn.hutool.core.util.StrUtil;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/test/{username}")
public class TestWebSocket {

    private static final ConcurrentHashMap<String, Session> onlineSession = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        log.info("「系统」：新用户「{}」加入广播室", username);
        session.getUserProperties().put("username", username);
        onlineSession.put(username, session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        onlineSession.forEach((username, singleSession) -> {
            if (singleSession.isOpen() && !Objects.equals(singleSession, session)) {
                String format = StrUtil.format("「{}」发送消息：{}", session.getUserProperties().get("username"), message);
                log.info(format);
                singleSession.getAsyncRemote().sendText(format, result -> {
                    if (!result.isOK()) {
                        log.error("发送失败：{}", result.getException().getMessage());
                    }
                });
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        log.info("「系统」：用户「{}」离开广播室", username);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("发生错误：" + error.getMessage());
    }
}