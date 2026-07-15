package com.example.mybatis;

import com.example.mybatis.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author JJBond
 * @date 2025-11-01 20:27
 */

@Slf4j
class MybatisApplicationTest {

    @Test
    public void test() throws NoSuchFieldException {
        Class<UserInfo> userInfoClass = UserInfo.class;

        Field userId = userInfoClass.getDeclaredField("userId");
        // Field[] fields = userInfoClass.getDeclaredFields();

        log.info("userId: {}", userId.getName());
        log.info("type: {}", userId.getType());

        int modifiers = userId.getModifiers();
        Modifier.isFinal(modifiers);
    }

    @Test
    void name() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // String 对象:
        String s = "Hello world";
        // 获取 String substring(int)方法，形参为 int:
        Method m = String.class.getMethod("substring", int.class);
        // 获取 String substring(int, int)方法，形参为 int, int:
        Method m2 = String.class.getMethod("substring", int.class, int.class);
        // 在 s 对象上调用该方法并获取结果:
        String r = (String) m.invoke(s, 6);
        String r2 = (String) m2.invoke(s, 0, 5);
        // 打印调用结果:
        System.out.println(r);
        System.out.println(r2);
    }
}