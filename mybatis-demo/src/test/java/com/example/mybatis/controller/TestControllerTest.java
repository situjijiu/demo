package com.example.mybatis.controller;

import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JJBond
 * @date 2025-11-01 22:09
 */
class TestControllerTest {

    @Test
    void getUserById() {
        String s = HttpUtil.get("http://localhost:8080/test/-1");

    }
}