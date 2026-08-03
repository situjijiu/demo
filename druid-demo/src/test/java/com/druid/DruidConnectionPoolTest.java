package com.druid;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DruidConnectionPoolTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 测试数据源类型是否为 DruidDataSource
     */
    @Test
    public void testDataSourceType() {
        assertNotNull(dataSource, "数据源不应为空");
        assertTrue(dataSource instanceof DruidDataSource,
                "数据源类型应为 DruidDataSource, 实际类型: " + dataSource.getClass().getName());
        System.out.println("数据源类型: " + dataSource.getClass().getName());
    }

    /**
     * 测试获取数据库连接
     */
    @Test
    public void testGetConnection() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应为空");
            System.out.println("获取数据库连接成功: " + connection);

            // 验证连接是否有效
            assertTrue(connection.isValid(5), "连接应有效");
            System.out.println("数据库连接有效");
        }
    }

    /**
     * 测试执行 SQL 查询
     */
    @Test
    public void testExecuteQuery() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1 AS result")) {

            assertTrue(resultSet.next(), "查询应返回结果");
            int result = resultSet.getInt("result");
            assertEquals(1, result, "查询结果应为 1");
            System.out.println("SQL 查询执行成功, 结果: " + result);
        }
    }

    /**
     * 测试 Druid 连接池监控统计信息
     */
    @Test
    public void testDruidPoolStats() throws Exception {
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;

        // 获取连接前打印池状态
        System.out.println("===== 连接池状态 (获取连接前) =====");
        System.out.println("初始连接数: " + druidDataSource.getInitialSize());
        System.out.println("最小空闲连接数: " + druidDataSource.getMinIdle());
        System.out.println("最大活跃连接数: " + druidDataSource.getMaxActive());
        System.out.println("当前活跃连接数: " + druidDataSource.getActiveCount());
        System.out.println("当前空闲连接数: " + druidDataSource.getPoolingCount());
        System.out.println("等待获取连接的线程数: " + druidDataSource.getWaitThreadCount());

        // 多次获取和关闭连接，模拟业务操作
        for (int i = 0; i < 10; i++) {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT " + (i + 1) + " AS num")) {
                if (resultSet.next()) {
                    System.out.println("查询 " + (i + 1) + ": " + resultSet.getInt("num"));
                }
            }
        }

        // 获取连接后打印池状态
        System.out.println("===== 连接池状态 (执行完查询后) =====");
        System.out.println("当前活跃连接数: " + druidDataSource.getActiveCount());
        System.out.println("当前空闲连接数: " + druidDataSource.getPoolingCount());
        System.out.println("等待获取连接的线程数: " + druidDataSource.getWaitThreadCount());
        System.out.println("逻辑连接打开次数: " + druidDataSource.getConnectCount());
        System.out.println("逻辑连接关闭次数: " + druidDataSource.getCloseCount());
        System.out.println("物理连接打开次数: " + druidDataSource.getCreateCount());
        System.out.println("物理连接关闭次数: " + druidDataSource.getDestroyCount());

        // 验证连接池复用情况
        assertTrue(druidDataSource.getConnectCount() > 0, "应该有连接被获取");
        assertEquals(druidDataSource.getConnectCount(), druidDataSource.getCloseCount(),
                "连接打开次数应等于关闭次数（无泄漏）");
    }

    /**
     * 测试多线程并发获取连接
     */
    @Test
    public void testConcurrentConnections() throws Exception {
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        int threadCount = 10;
        int queriesPerThread = 5;

        Runnable task = () -> {
            try {
                for (int i = 0; i < queriesPerThread; i++) {
                    try (Connection connection = dataSource.getConnection();
                         Statement statement = connection.createStatement();
                         ResultSet resultSet = statement.executeQuery("SELECT SLEEP(0.1)")) {
                        // 模拟耗时操作
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(task, "test-thread-" + i);
            threads.add(thread);
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证并发执行后的连接池状态
        System.out.println("===== 并发测试结果 =====");
        System.out.println("并发线程数: " + threadCount);
        System.out.println("每个线程查询次数: " + queriesPerThread);
        System.out.println("逻辑连接打开次数: " + druidDataSource.getConnectCount());
        System.out.println("逻辑连接关闭次数: " + druidDataSource.getCloseCount());
        System.out.println("物理连接创建次数: " + druidDataSource.getCreateCount());
        System.out.println("当前活跃连接数: " + druidDataSource.getActiveCount());
        System.out.println("当前空闲连接数: " + druidDataSource.getPoolingCount());

        // 验证连接无泄漏（打开的连接数应等于关闭的连接数）
        assertEquals(druidDataSource.getConnectCount(), druidDataSource.getCloseCount(),
                "连接无泄漏: 打开次数 " + druidDataSource.getConnectCount()
                        + " 应等于关闭次数 " + druidDataSource.getCloseCount());
    }
}