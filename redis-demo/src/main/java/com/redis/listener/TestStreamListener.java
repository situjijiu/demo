package com.redis.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

@Slf4j
public class TestStreamListener implements StreamListener<String, MapRecord<String, String, String>> {


    @Override
    public void onMessage(MapRecord<String, String, String> message) {

        log.info("消费消息：{}:{}", message.getId(), message.getValue());
    }
}
