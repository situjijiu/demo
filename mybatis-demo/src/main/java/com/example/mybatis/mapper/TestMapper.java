package com.example.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author JJBond
 * @date 2025-10-30 21:59
 */
@Mapper
public interface TestMapper {

    String selectUserById(@Param("id") Integer id);
}
