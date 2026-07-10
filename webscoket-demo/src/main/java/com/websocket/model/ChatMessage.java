package com.websocket.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessage {
    private String sender;
    private String content;
    private LocalDateTime time;
}