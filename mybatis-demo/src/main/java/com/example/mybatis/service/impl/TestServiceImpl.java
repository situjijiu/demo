package com.example.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mybatis.mapper.TestMapper;
import com.example.mybatis.model.User;
import com.example.mybatis.service.TestService;
import org.springframework.stereotype.Service;

/**
 * @author JJBond
 * @date 2025-10-30 21:59
 */
@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, User> implements TestService {

}
