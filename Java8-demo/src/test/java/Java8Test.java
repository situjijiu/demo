import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class Java8Test {

    /**
     * 需求1.1: 简化集合排序
     * - **类定义**: 创建 `Student` 类,包含姓名(name)、年龄(age)、成绩(score)字段
     * - **排序要求**:
     * - 按年龄升序排序
     * - 按成绩降序排序
     * - 按姓名长度排序,长度相同则按年龄排序
     */
    @Test
    void test() {
        // 模拟student集合
        List<Student> students = Arrays.asList(
                new Student("张三", 18, 90d),
                new Student("李四4213", 19, 85d),
                new Student("王五", 19, 95d),
                new Student("赵六12", 20, 88d),
                new Student("钱七1", 19, 92d)
        );

        // log.info("按年龄升序排序：");
        // students.stream()
        //         .sorted(Comparator.comparingInt(Student::getAge))
        //         .forEach(System.out::println);
        // log.info("=========");
        //
        // log.info("按成绩降序排序：");
        // students.stream()
        //         .sorted(Comparator.comparingDouble(Student::getScore).reversed())
        //         .forEach(System.out::println);
        // log.info("=========");

        log.info("按姓名长度排序,长度相同则按年龄排序：");
        students.stream()
                .sorted(Comparator.comparingInt((Student s) -> s.getName().length()).reversed()
                        .thenComparing(Student::getAge, Comparator.reverseOrder())
                        .thenComparing(Comparator.comparingInt(Student::getAge).reversed()))
                .forEach(System.out::println);
        log.info("=========");

    }

    /**
     * 需求4.1: 异步查询
     * - **模拟服务**: 用户基本信息服务(耗时1秒)、订单服务(耗时2秒)、积分服务(耗时1.5秒)
     * - **实现要求**:
     * - 三个服务异步调用
     * - 汇总三个服务的结果返回
     * - 设置超时时间3秒
     */
    @Test
    void test1() {
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 用户服务，1s
        CompletableFuture<String> userInfoFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "用户基本信息";
        }, executor);

        // 订单服务，2s
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "订单信息";
        }, executor);

        // 积分服务，1.5s
        CompletableFuture<String> pointsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "积分信息";
        }, executor);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(userInfoFuture, orderFuture, pointsFuture);

        try {
            allFutures.get(4, TimeUnit.SECONDS);

            String userInfo = userInfoFuture.get();
            String orderInfo = orderFuture.get();
            String pointsInfo = pointsFuture.get();

            log.info("异步查询完成，总耗时: {}ms", System.currentTimeMillis() - startTime);
            log.info("用户信息: {}", userInfo);
            log.info("订单信息: {}", orderInfo);
            log.info("积分信息: {}", pointsInfo);
        } catch (Exception e) {
            log.error("异步查询超时或失败: {}", e.getMessage());
        } finally {
            executor.shutdown();
        }
    }


}