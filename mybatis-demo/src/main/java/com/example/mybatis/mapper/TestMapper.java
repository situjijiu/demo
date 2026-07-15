package com.example.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mybatis.model.User;
import org.apache.ibatis.annotations.Param;

/**
 * @author JJBond
 * @date 2025-10-30 21:59
 */
public interface TestMapper extends BaseMapper<User> {

    User selectUserById(@Param("id") Integer id);
}
