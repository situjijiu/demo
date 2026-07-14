package com.redis.service;

import com.redis.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 用户服务类
 * 演示 Spring Cache 注解的使用：@Cacheable、@CachePut、@CacheEvict
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    /**
     * 模拟数据库存储
     */
    private final Map<Long, User> userDatabase = new HashMap<>();

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 初始化模拟数据（Spring 依赖注入完成后自动执行）
     */
    @PostConstruct
    public void init() {
        userDatabase.put(1L, new User(1L, "zhangsan", "zhangsan@example.com", 25));
        userDatabase.put(2L, new User(2L, "lisi", "lisi@example.com", 30));
        userDatabase.put(3L, new User(3L, "wangwu", "wangwu@example.com", 35));
    }

    /**
     * 根据ID查询用户
     * 使用 @Cacheable 注解，首次查询会执行方法体并将结果存入缓存
     * 后续相同参数的请求直接从缓存获取，不再执行方法体
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Cacheable(cacheNames = "user:", key = "#id", unless = "#result == null")
    public User getUserById(Long id) {
        log.info("从数据库查询用户，ID: {}", id);
        return userDatabase.get(id);
    }

    /**
     * 更新用户信息
     * 使用 @CachePut 注解，每次都会执行方法体，并将结果更新到缓存
     * 常用于更新操作后同步更新缓存
     *
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    @CachePut(cacheNames = "user:", key = "#user.id")
    public User updateUser(User user) {
        log.info("更新用户信息，ID: {}", user.getId());
        userDatabase.put(user.getId(), user);
        return user;
    }

    /**
     * 删除用户
     * 使用 @CacheEvict 注解，删除指定缓存
     *
     * @param id 用户ID
     */
    @CacheEvict(cacheNames = "user:", key = "#id")
    public void deleteUser(Long id) {
        log.info("删除用户，ID: {}", id);
        userDatabase.remove(id);
        CompletableFuture.runAsync(() ->
                redisTemplate.convertAndSend("cache:evict", "demo:user:" + id));
        // redisTemplate.convertAndSend("cache:evict", "user:id");
    }

    /**
     * 删除所有用户缓存
     * 使用 @CacheEvict 注解，allEntries = true 表示删除该缓存分区下的所有缓存
     */
    @CacheEvict(cacheNames = "user:", allEntries = true)
    public void clearAllUserCache() {
        log.info("清除所有用户缓存");
    }
}
