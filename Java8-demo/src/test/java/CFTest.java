import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CFTest {

    ExecutorService pool = Executors.newFixedThreadPool(3);

    @Test
    void complete() throws InterruptedException {
        CompletableFuture<String> test = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "hello world";
        }, pool);
        TimeUnit.SECONDS.sleep(3);
        boolean flag = test.complete("fuck world");
        if (flag) {
            System.out.println("你个废物，执行这么慢");
        } else System.out.println("nbnb");
        String res = test.join();
        System.out.println(res);
    }

    @Test
    void then() {
        // 动手练习：把笔记本里的"完整流程"代码敲到 IDE 里跑一遍，然后改成用 thenCompose 串联（获取用户 → 根据用户查订单）。
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> "用户查询", pool);
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> ("订单查询"), pool);

        // CompletableFuture<String> res = userFuture.thenCombine(orderFuture, (user, order) -> {
        //     log.info(user);
        //     log.info(order);
        //     return user + order;
        // });

        CompletableFuture<String> res = userFuture.thenCompose(user -> orderFuture);
        log.info(res.join());

    }

    /**
     * 尝试编写一个程序，使用 supplyAsync() 模拟从远程API获取数据，使用 runAsync() 记录日志。
     */
    @Test
    void test02() throws InterruptedException {
        // 使用 supplyAsync 模拟从远程API获取数据
        CompletableFuture<List<Data>> dataFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);  // 模拟网络延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("从远程API获取数据成功");
            return List.of(new Data(), new Data(), new Data());
        }, pool);

        // 使用 thenRun 在数据获取完成后记录日志
        CompletableFuture<Void> logFuture = dataFuture.thenRun(() -> {
            log.info("数据获取完成，已记录到日志");
        });

        // 等待两个任务完成
        List<Data> res = dataFuture.join();
        logFuture.join();

        log.info("获取到 {} 条数据", res.size());
    }

    record Data() {
    }

    /**
     * 尝试实现一个场景：先获取用户信息，然后并行获取用户的订单和地址，最后合并所有信息。
     */
    @Test
    void test03() {
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> "用户信息");

        // 方案1：使用 thenCompose（扁平化异步链）
        CompletableFuture<String> result1 = userFuture.thenCompose(user ->
                CompletableFuture.supplyAsync(() -> user + " + 用户订单"));

        log.info("方案1（thenCompose）: {}", result1.join());

        // 方案2：使用 thenCombine（合并两个异步任务）
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> "用户订单");
        CompletableFuture<String> result2 = userFuture.thenCombine(orderFuture, (user, order) ->
                user + " + " + order);

        log.info("方案2（thenCombine）: {}", result2.join());
    }

    @Test
    void test04() {
    }
}
