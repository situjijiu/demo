package com.websocket.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatRoomJoinEventListener {


    @EventListener
    public void notifyOthers(String joinEvent) {
        log.info(joinEvent);
    }

}
