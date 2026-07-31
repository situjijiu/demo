package com.redis.config;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@SpringBootTest
public class RedisAPITest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testString() {
        redisTemplate.setValueSerializer(new RedisSerializer<Student>() {
            @Override
            public byte[] serialize(Student value) throws SerializationException {
                if (value == null) return null;
                String jsonStr = JSONUtil.toJsonStr(value);
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Student deserialize(byte[] bytes) throws SerializationException {
                String s = bytes == null ? null : new String(bytes);
                return JSONUtil.toBean(s, Student.class);
            }
        });
        Student test = Student.builder().name("test").age(12).build();

        redisTemplate.opsForValue().set("demo:redis:string", test, Duration.ofSeconds(100));
    }

    @Data
    @Builder
    static class Student {
        private String name;
        private Integer age;
    }
}
