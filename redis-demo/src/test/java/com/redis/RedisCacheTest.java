package com.redis;

import com.redis.model.User;
import com.redis.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存功能测试类
 * 测试 Spring Cache 注解和随机 TTL 功能
 */
@Slf4j
@SpringBootTest
class RedisCacheTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试 @Cacheable 注解：首次查询从数据库，后续从缓存
     */
    @Test
    void testCacheable() throws InterruptedException {
        log.info("=== 测试 @Cacheable 注解 ===");

        // 第一次查询，应该打印 "从数据库查询用户"
        log.info("第一次查询用户 ID=1");
        User user1 = userService.getUserById(1L);
        log.info("查询结果: {}", user1);

        // 第二次查询，不应该打印日志，直接从缓存获取
        log.info("第二次查询用户 ID=1");
        User user2 = userService.getUserById(1L);
        log.info("查询结果: {}", user2);

        // 验证两次查询结果相同
        assert user1.equals(user2) : "两次查询结果不一致";
        log.info("✓ @Cacheable 测试通过");
    }

    /**
     * 测试 @CachePut 注解：更新数据并同步更新缓存
     */
    @Test
    void testCachePut() {
        log.info("=== 测试 @CachePut 注解 ===");

        // 先查询一次，确保缓存中有数据
        User originalUser = userService.getUserById(1L);
        log.info("原始用户: {}", originalUser);

        // 更新用户信息
        User updatedUser = new User(1L, "zhangsan_updated", "zhangsan_updated@example.com", 26);
        userService.updateUser(updatedUser);
        log.info("更新后的用户: {}", updatedUser);

        // 再次查询，应该从缓存获取更新后的数据
        User cachedUser = userService.getUserById(1L);
        log.info("缓存中的用户: {}", cachedUser);

        // 验证缓存中的数据已更新
        assert cachedUser.getUsername().equals("zhangsan_updated") : "缓存未更新";
        log.info("✓ @CachePut 测试通过");
    }

    /**
     * 测试 @CacheEvict 注解：删除指定缓存
     */
    @Test
    void testCacheEvict() {
        log.info("=== 测试 @CacheEvict 注解 ===");

        // 先查询一次，确保缓存中有数据
        userService.getUserById(1L);

        // 检查 Redis 中是否存在缓存
        Boolean exists = redisTemplate.hasKey("demo:1");
        assert exists : "缓存不存在";
        log.info("删除前缓存存在: {}", exists);

        // 删除用户，应该清除缓存
        userService.deleteUser(1L);

        // 检查 Redis 中缓存是否已删除
        exists = redisTemplate.hasKey("demo:1");
        assert !exists : "缓存未删除";
        log.info("删除后缓存存在: {}", exists);

        // 再次查询，应该从数据库查询（因为缓存已删除）
        log.info("删除后再次查询，应该打印数据库查询日志");
        userService.getUserById(1L);

        log.info("✓ @CacheEvict 测试通过");
    }

    /**
     * 测试 @CacheEvict allEntries=true：清除所有缓存
     */
    @Test
    void testCacheEvictAllEntries() {
        log.info("=== 测试 @CacheEvict allEntries=true ===");

        // 先查询多个用户，确保缓存中有数据
        userService.getUserById(1L);
        userService.getUserById(2L);
        userService.getUserById(3L);

        // 检查缓存是否存在
        Boolean exists1 = redisTemplate.hasKey("demo:1");
        Boolean exists2 = redisTemplate.hasKey("demo:2");
        Boolean exists3 = redisTemplate.hasKey("demo:3");
        assert exists1 && exists2 && exists3 : "缓存不存在";
        log.info("清除前缓存数量: 1={}, 2={}, 3={}", exists1, exists2, exists3);

        // 清除所有缓存
        userService.clearAllUserCache();

        // 检查缓存是否已清除
        exists1 = redisTemplate.hasKey("demo:1");
        exists2 = redisTemplate.hasKey("demo:2");
        exists3 = redisTemplate.hasKey("demo:3");
        assert !exists1 && !exists2 && !exists3 : "缓存未全部清除";
        log.info("清除后缓存数量: 1={}, 2={}, 3={}", exists1, exists2, exists3);

        log.info("✓ @CacheEvict allEntries 测试通过");
    }

    /**
     * 测试随机 TTL 功能（缓存雪崩解决方案）
     */
    @Test
    void testRandomTtl() throws InterruptedException {
        log.info("=== 测试随机 TTL 功能 ===");

        // 多次查询同一用户，验证每次的 TTL 都不同
        int[] ttls = new int[5];
        for (int i = 0; i < 5; i++) {
            // 清除缓存
            redisTemplate.delete("demo:1");

            // 查询用户，触发缓存写入
            userService.getUserById(1L);

            // 获取 TTL（单位：秒）
            Long ttl = redisTemplate.getExpire("demo:1", TimeUnit.SECONDS);
            ttls[i] = ttl.intValue();
            log.info("第 {} 次缓存 TTL: {} 秒", i + 1, ttls[i]);

            // 等待一秒再进行下一次测试
            Thread.sleep(1000);
        }

        // 验证 TTL 在预期范围内（3-9分钟 = 180-540秒）
        for (int i = 0; i < ttls.length; i++) {
            assert ttls[i] >= 180 && ttls[i] <= 540 : 
                "TTL " + ttls[i] + " 超出预期范围 (180-540秒)";
        }

        // 验证至少有两个不同的 TTL（证明随机性）
        boolean hasDifferentTtl = false;
        for (int i = 1; i < ttls.length; i++) {
            if (ttls[i] != ttls[0]) {
                hasDifferentTtl = true;
                break;
            }
        }
        assert hasDifferentTtl : "所有 TTL 相同，随机 TTL 功能未生效";

        log.info("✓ 随机 TTL 测试通过");
    }

    /**
     * 测试缓存穿透解决方案：缓存空值
     */
    @Test
    void testCacheNullValue() {
        log.info("=== 测试缓存空值（防止缓存穿透）===");

        // 查询不存在的用户
        log.info("查询不存在的用户 ID=999");
        User user1 = userService.getUserById(999L);
        log.info("查询结果: {}", user1);

        // 再次查询，应该从缓存获取（虽然是 null）
        log.info("再次查询不存在的用户 ID=999");
        User user2 = userService.getUserById(999L);
        log.info("查询结果: {}", user2);

        // 验证两次结果都是 null
        assert user1 == null && user2 == null : "查询结果不为 null";
        log.info("✓ 缓存空值测试通过");
    }

    /**
     * 测试并发查询（验证缓存击穿解决方案）
     */
    @Test
    void testConcurrentQuery() throws InterruptedException {
        log.info("=== 测试并发查询（验证缓存击穿解决方案）===");

        // 清除缓存
        redisTemplate.delete("demo:1");

        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        int[] queryCount = {0};

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    userService.getUserById(1L);
                    endLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        // 同时开始所有线程
        log.info("启动 {} 个并发线程查询用户 ID=1", threadCount);
        startLatch.countDown();

        // 等待所有线程完成
        endLatch.await(5, TimeUnit.SECONDS);

        log.info("✓ 并发查询测试通过");
    }
}
