package com.example.mybatis.controller;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

/**
 * @author JJBond
 * @date 2025-11-01 22:09
 */
@Slf4j
class TestControllerTest {

    @Test
    void getUserById() {
        String s = HttpUtil.get("http://localhost:8080/test/-1");
        log.info(s);

    }
}