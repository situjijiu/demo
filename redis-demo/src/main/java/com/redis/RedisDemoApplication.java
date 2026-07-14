package com.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Redis 演示应用启动类
 */
@SpringBootApplication
public class RedisDemoApplication {

    /**
     * 应用主入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);
    }
}
