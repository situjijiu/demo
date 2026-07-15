package com.redis.controller;

import com.redis.model.User;
import com.redis.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 提供 REST API 端点测试 Spring Cache 缓存功能
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RedisTemplate<String,Object> redisTemplate;


    /**
     * 根据ID查询用户
     * 首次请求会从数据库查询，后续相同ID的请求直接从缓存获取
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * 更新用户信息
     * 更新后会同步更新缓存
     *
     * @param id   用户ID
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 删除用户
     * 删除后会清除对应缓存
     *
     * @param id 用户ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 清除所有用户缓存
     *
     * @return 成功响应
     */
    @DeleteMapping("/cache/clear")
    public ResponseEntity<String> clearAllCache() {
        userService.clearAllUserCache();
        return ResponseEntity.ok("所有用户缓存已清除");
    }

    @GetMapping("/test")
    public ResponseEntity<String> testStream() {
        userService.testStream();
        return ResponseEntity.ok().build();
    }

    @GetMapping("create")
    public void create() {
        redisTemplate.opsForStream().createGroup("test", "test-group");
    }
}
