package com.redis.config;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.redis.listener.CacheEvictListener;
import com.redis.listener.TestStreamListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

/**
 * Redis 配置类
 * 配置 RedisTemplate 和 RedisCacheManager，统一序列化策略
 * 使用 Spring Data Redis 官方 TtlFunction 实现随机 TTL，防止缓存雪崩
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {


    /**
     * 配置 RedisTemplate
     * key 使用 StringRedisSerializer，value 使用 GenericJackson2JsonRedisSerializer
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 RedisCacheManager
     * 使用 Spring Data Redis 官方 TtlFunction 实现随机 TTL，防止缓存雪崩
     * 缓存时间 = random(3, 9) 分钟
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> "demo:" + cacheName)
                .entryTtl((key, value) -> Duration.ofMinutes(ThreadLocalRandom.current().nextInt(3, 10)))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }


    public final CacheEvictListener cacheEvictListener;

    /**
     * Redis pub/sub 监听器容器
     *
     * @param factory Redis配置工厂
     * @return 发布订阅容器
     */
    @Bean
    public RedisMessageListenerContainer pubSubContainer(RedisConnectionFactory factory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);

        // 配置消息分发的线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("redis-pubsub-");
        executor.initialize();
        container.setTaskExecutor(executor);

        container.addMessageListener(cacheEvictListener, ChannelTopic.of("cache:evict"));
        return container;
    }


    /**
     * 配置 Redis Stream 消费者组和监听器容器
     * <p>
     * 使用 StreamMessageListenerContainer 实现 Stream 消息的自动消费。
     * 流程：
     * 1. 使用 xGroupCreate + MKSTREAM 创建消费者组（Stream 不存在时自动创建）
     * 2. 创建 StreamMessageListenerContainer，配置轮询超时和批处理大小
     * 3. 注册 StreamListener 并指定消费者组和消费者名称
     * 4. 启动容器，开始持续监听 Stream 消息
     *
     * @param factory       Redis 连接工厂
     * @param redisTemplate Redis 模板，用于获取原生连接创建消费者组
     * @return StreamMessageListenerContainer 容器实例
     */
    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamContainer(RedisConnectionFactory factory, RedisTemplate<String, Object> redisTemplate) {
        // // ========== 1. 创建 Stream 和消费者组 ==========
        // // XADD 会自动创建 Stream（无需预先存在），再创建消费者组
        // String streamKey = "test:stream:simple";
        // String groupName = "test-group";
        // if (Boolean.FALSE.equals(redisTemplate.hasKey(streamKey))) {
        //     redisTemplate.opsForStream().add(streamKey, Map.of("init", "1"));
        // }
        // // 创建消费者组（若已存在则忽略 BUSYGROUP 异常，保证幂等）
        // try {
        //     redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), groupName);
        //     log.info("创建 Stream 消费者组成功：stream={}, group={}", streamKey, groupName);
        // } catch (Exception e) {
        //     log.info("消费者组 {} 已存在或创建失败：{}", groupName, e.getMessage());
        // }

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("stream-listener-");
        executor.initialize();
        // ========== 2. 构建监听器容器 ==========
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(factory,
                        StreamMessageListenerContainer
                                .StreamMessageListenerContainerOptions.builder()
                                .executor(executor) // 配置自定义线程池
                                .pollTimeout(Duration.ofMillis(1000))  // 轮询超时 1 秒，避免长阻塞
                                .batchSize(10)                         // 每次拉取最多 10 条消息
                                .build());

        // ========== 3. 注册消费者 ==========
        // receiveAutoAck：消费后自动确认（无需手动 ACK）
        // Consumer.from(组名, 消费者名) —— 同一个组内多个消费者分摊消息
        // ReadOffset.lastConsumed() 对应 ">"，表示消费从未投递给任何消费者的新消息
        // 注意：XREADGROUP 中 ">" 表示新消息，"0" 表示读取当前消费者 PEL（未确认）中的旧消息
        container.receiveAutoAck(
                Consumer.from("test-group", "test-consumer1"),
                StreamOffset.create("test:stream:simple", ReadOffset.lastConsumed()),
                // new TestStreamListener()
                message -> log.info("Redis监听容器处理消息：{}", message.getValue())
        );


        // ========== 4. 启动容器 ==========
        // 容器启动后，会在后台线程中持续监听 Stream 新消息
        container.start();
        return container;
    }
}
