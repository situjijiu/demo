import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

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
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync((String s) -> s.concat("订单查询"), pool);

        // CompletableFuture<String> res = userFuture.thenCombine(orderFuture, (user, order) -> {
        //     log.info(user);
        //     log.info(order);
        //     return user + order;
        // });


    }
}
