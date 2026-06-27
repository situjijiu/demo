---
title: CompletableFuture 异步编程
date: 2026-06-26
tags:
  - Java
  - Java8
  - CompletableFuture
  - 异步编程
  - 多线程
aliases:
  - 异步编排
  - 可组合Future
---

> [!abstract] 知识地图
> 本笔记系统讲解 `CompletableFuture`：==Future 的局限性== → ==创建与获取结果== → ==链式处理（转换/消费/组合）== → ==异常处理== → ==多任务编排== → ==线程池与原理== → ==实战场景==。

---

# 概述

## 什么是 CompletableFuture

> [!quote] 一句话理解
> `CompletableFuture` 是 Java 8 引入的 ==可组合异步编程== 工具，支持链式回调、多任务编排、异常处理，是 `Future` 的增强版。

`CompletableFuture` 实现了 `Future` 和 `CompletionStage` 两个接口：
- ==Future==：提供 `get()` / `cancel()` 等基础异步能力
- ==CompletionStage==：提供丰富的 ==链式编排== 能力（`thenApply`、`thenCompose` 等）

```
Future（基础异步）
  └── CompletableFuture（增强：可回调 + 可组合 + 可异常处理）
        implements CompletionStage（链式编排接口）
```

> [!info] 核心价值
> - ==非阻塞回调==：任务完成后自动触发回调，不用 `get()` 阻塞等待
> - ==链式编排==：任务 A 完成后自动执行任务 B，B 完成后执行 C
> - ==多任务组合==：`allOf` 等待全部完成，`anyOf` 等待任一完成
> - ==异常处理==：`exceptionally` / `handle` 优雅处理异常

---

# Future 的局限性

## 为什么需要 CompletableFuture

| 局限 | Future 的问题 | CompletableFuture 的解决 |
|------|-------------|------------------------|
| ==不能回调== | `get()` 阻塞等待，无法注册完成回调 | `whenComplete` / `thenApply` 自动回调 |
| ==不能链式== | 任务间无法自动串联 | `thenCompose` 链式串联 |
| ==不能组合== | 无法等待多个任务全部完成 | `allOf` / `anyOf` |
| ==不能异常处理== | 异常只能 `get()` 时 try-catch | `exceptionally` / `handle` |
| ==不能主动完成== | 无法手动设置结果 | `complete()` / `completeExceptionally()` |

```java
// Future 的痛点：阻塞 + 无法串联
Future<User> userFuture = executor.submit(() -> getUser(id));
Future<Order> orderFuture = executor.submit(() -> getOrder(id));

// 必须阻塞等待，无法注册回调
User user = userFuture.get();   // 阻塞！
Order order = orderFuture.get(); // 又阻塞！

// 无法在 userFuture 完成后自动执行下一步
```

> [!danger] Future 的根本缺陷
> `Future` 是 ==拉模型==（主动 `get()` 拉取结果），而 `CompletableFuture` 是 ==推模型==（完成后自动推送回调），这才是本质区别。

---

# 创建与获取结果

## 创建 CompletableFuture

| 方法                           | 返回值                       | 说明                            |
| ---------------------------- | ------------------------- | ----------------------------- |
| `supplyAsync(供给型接口)`         | `CompletableFuture<T>`    | ==异步执行，有返回值==                 |
| `runAsync(消费型接口)`            | `CompletableFuture<Void>` | ==异步执行，无返回值==                 |
| `completedFuture(value)`     | `CompletableFuture<T>`    | 直接返回已完成的 Future               |
| `new CompletableFuture<T>()` | `CompletableFuture<T>`    | 创建未完成的 Future（需手动 `complete`） |

```java
// 1. 异步执行有返回值（默认 ForkJoinPool.commonPool）
CompletableFuture<String> future1 = CompletableFuture
    .supplyAsync(() -> {
        System.out.println("执行任务：" + Thread.currentThread().getName());
        return "结果";
    });

// 2. 异步执行无返回值
CompletableFuture<Void> future2 = CompletableFuture
    .runAsync(() -> System.out.println("无返回值任务"));

// 3. 指定自定义线程池（推荐）
ExecutorService executor = Executors.newFixedThreadPool(10);
CompletableFuture<String> future3 = CompletableFuture
    .supplyAsync(() -> "自定义线程池", executor);

// 4. 直接创建已完成的 Future（测试用）
CompletableFuture<String> done = CompletableFuture.completedFuture("已完成");
```

> [!tip] 线程池选择
> `supplyAsync` / `runAsync` 默认使用 `ForkJoinPool.commonPool()`，线程数为 `CPU核心数 - 1`。==生产环境强烈建议传入自定义线程池==，避免 IO 阻塞任务拖垮全局。

## 获取结果

| 方法 | 阻塞 | 异常 | 说明 |
|------|------|------|------|
| `get()` | ==阻塞== | 抛 `InterruptedException` + `ExecutionException` | 最常用 |
| `get(timeout, unit)` | ==阻塞（带超时）== | 同上 + `TimeoutException` | 防止永久阻塞 |
| `join()` | ==阻塞== | 抛 `CompletionException`（==非受检==） | 链式编排中常用 |
| `getNow(defaultValue)` | ==不阻塞== | 无 | 未完成时返回默认值 |
| `complete(value)` | — | — | ==手动完成==，返回是否成功 |

```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "hello");

// 1. 阻塞获取（抛受检异常）
try {
    String result = future.get();
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}

// 2. 带超时获取
try {
    String result = future.get(3, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    // 超时处理
}

// 3. join（抛非受检异常，链式中更方便）
String result = future.join();

// 4. 非阻塞获取，未完成返回默认值
String now = future.getNow("默认值");

// 5. 手动完成（可用于超时兜底）
boolean completed = future.complete("手动结果");
```

> [!warning] get() vs join()
> - `get()` 抛 ==受检异常==，必须 try-catch，代码冗长
> - `join()` 抛 ==非受检异常==（CompletionException），代码简洁
> - ==链式编排中优先用 `join`==，最终消费时用 `get`

---

# 链式处理

CompletableFuture 的链式处理是核心能力，分为三大类操作：==转换==、==消费==、==组合==。

## 三类操作总览

| 类别 | 方法 | 输入 → 输出 | 语义 |
|------|------|-----------|------|
| ==转换== | `thenApply` | `T → R` | 拿上一步结果，返回新值 |
| ==消费== | `thenAccept` | `T → void` | 消费结果，无返回 |
| ==执行== | `thenRun` | `() → void` | 不关心结果，执行副作用 |
| ==组合（扁平）== | `thenCompose` | `T → CompletableFuture<R>` | 串联另一个异步任务 |
| ==合并（双输入）== | `thenCombine` | `(T, U) → R` | 合并两个独立任务的结果 |

## 同步与异步变体

每个方法都有 ==三个版本==：

| 后缀 | 执行方式 | 说明 |
|------|---------|------|
| 无后缀 | ==同步== | 在 ==上一步完成的线程== 中执行 |
| `Async` | ==异步== | 提交到 ==ForkJoinPool.commonPool== 执行 |
| `Async(executor)` | ==异步== | 提交到 ==指定线程池== 执行 |

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "hello");

// 同步：在 future 完成的线程中执行
CompletableFuture<String> f1 = future.thenApply(s -> s + " world");

// 异步：提交到 ForkJoinPool
CompletableFuture<String> f2 = future.thenApplyAsync(s -> s + " world");

// 异步：提交到自定义线程池
CompletableFuture<String> f3 = future.thenApplyAsync(s -> s + " world", executor);
```

> [!warning] 同步 vs 异步的选择
> - 回调操作 ==轻量==（简单转换）→ 用 ==同步== 版本，减少线程切换开销
> - 回调操作 ==耗时==（IO/计算密集）→ 用 ==异步== 版本，避免阻塞完成线程

## 转换：thenApply

```java
// thenApply：接收上一步结果，返回新值
CompletableFuture<Integer> future = CompletableFuture
    .supplyAsync(() -> "hello")
    .thenApply(String::length)          // "hello" → 5
    .thenApply(len -> len * 2);         // 5 → 10

System.out.println(future.join());  // 10
```

## 消费：thenAccept / thenRun

```java
// thenAccept：消费结果，无返回值
CompletableFuture<Void> f1 = CompletableFuture
    .supplyAsync(() -> "hello")
    .thenAccept(s -> System.out.println("收到：" + s));  // 收到：hello

// thenRun：不关心结果，执行副作用
CompletableFuture<Void> f2 = CompletableFuture
    .supplyAsync(() -> "hello")
    .thenRun(() -> System.out.println("任务完成了"));  // 任务完成了
```

## 组合：thenCompose（串联异步）

> [!important] thenApply vs thenCompose
> - `thenApply`：回调返回 ==普通值==（`T → R`）
> - `thenCompose`：回调返回 ==CompletableFuture==（`T → CompletableFuture<R>`），会自动 ==扁平化==
> - 类似 `Stream.map` vs `Stream.flatMap` 的关系

```java
// thenCompose：串联两个异步任务
// 场景：先获取用户ID → 再根据ID获取订单

// ❌ 错误：用 thenApply 会嵌套
CompletableFuture<CompletableFuture<Order>> wrong = getUserFuture()
    .thenApply(user -> getOrderFuture(user.getId()));  // 嵌套了！

// ✅ 正确：用 thenCompose 扁平化
CompletableFuture<Order> correct = getUserFuture()
    .thenCompose(user -> getOrderFuture(user.getId()));  // 自动展开

// 完整链式编排
CompletableFuture<UserOrderDTO> result = getUserFuture()              // 异步：获取用户
    .thenCompose(user -> getOrderFuture(user.getId()))                // 异步：获取订单
    .thenCompose(order -> getLogisticsFuture(order.getId()))          // 异步：获取物流
    .thenApply(logistics -> new UserOrderDTO(logistics));             // 同步：组装DTO
```

## 合并：thenCombine（独立合并）

```java
// thenCombine：合并两个独立任务的结果
// 场景：并行获取价格和折扣，合并计算最终价格

CompletableFuture<Integer> priceFuture = CompletableFuture
    .supplyAsync(() -> getPrice());        // 异步获取价格

CompletableFuture<Double> discountFuture = CompletableFuture
    .supplyAsync(() -> getDiscount());     // 异步获取折扣

// 两个任务并行执行，都完成后合并
CompletableFuture<Double> finalPrice = priceFuture
    .thenCombine(discountFuture, (price, discount) -> price * discount);

System.out.println("最终价格：" + finalPrice.join());
```

---

# 异常处理

## 三种异常处理方式

| 方法 | 触发条件 | 能否访问结果 | 能否恢复 |
|------|---------|------------|---------|
| `exceptionally(Function)` | ==仅异常时== | ❌ 只能拿到异常 | ✅ 返回兜底值 |
| `handle(BiFunction)` | ==始终触发== | ✅ 结果和异常都有 | ✅ 可返回新值 |
| `whenComplete(BiConsumer)` | ==始终触发== | ✅ 可读取 | ❌ 不能修改结果 |

## exceptionally：异常兜底

```java
// exceptionally：仅在异常时触发，返回兜底值
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> {
        if (Math.random() > 0.5) throw new RuntimeException("出错了");
        return "成功";
    })
    .exceptionally(ex -> {
        System.out.println("捕获异常：" + ex.getMessage());
        return "默认值";  // 返回兜底值，后续链式继续执行
    });

System.out.println(future.join());  // "成功" 或 "默认值"
```

## handle：结果与异常都处理

```java
// handle：无论成功失败都触发，可访问结果和异常
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> {
        if (Math.random() > 0.5) throw new RuntimeException("出错了");
        return "成功";
    })
    .handle((result, ex) -> {
        if (ex != null) {
            return "异常恢复：" + ex.getMessage();
        }
        return "处理结果：" + result;
    });

System.out.println(future.join());
```

## whenComplete：观察但不修改

```java
// whenComplete：观察结果或异常，但不能修改（类似 finally）
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "hello")
    .whenComplete((result, ex) -> {
        if (ex != null) {
            log.error("任务异常", ex);
        } else {
            log.info("任务完成：{}", result);
        }
    })
    .thenApply(s -> s + " world");  // whenComplete 不影响后续

System.out.println(future.join());  // "hello world"
```

> [!danger] 异常处理陷阱
> - 不处理异常会导致 `CompletionException` 被 ==静默吞掉==（只在 `get`/`join` 时抛出）
> - `whenComplete` ==不能修改结果==，异常会继续向下传递
> - `exceptionally` 只处理 ==上一步== 的异常，链中间的异常需在对应位置处理
> ```java
> // 异常会跳过中间步骤，直达 exceptionally
> CompletableFuture.supplyAsync(() -> { throw new RuntimeException("step1"); })
>     .thenApply(s -> s + " step2")           // 跳过，不执行
>     .thenApply(s -> s + " step3")           // 跳过，不执行
>     .exceptionally(ex -> "兜底值")           // 执行，返回 "兜底值"
>     .thenApply(s -> s + " step4");          // 执行，返回 "兜底值 step4"
> ```

---

# 多任务编排

## allOf：等待全部完成

```java
// allOf：等待所有任务完成，返回 CompletableFuture<Void>
CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> queryService1());
CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> queryService2());
CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> queryService3());

// 等待全部完成
CompletableFuture<Void> allDone = CompletableFuture.allOf(task1, task2, task3);

// 全部完成后汇总结果
allDone.thenRun(() -> {
    // 此时三个任务都已完成，join 不会阻塞
    String r1 = task1.join();
    String r2 = task2.join();
    String r3 = task3.join();
    System.out.println("汇总：" + r1 + r2 + r3);
});

allDone.join();  // 阻塞等待全部完成
```

> [!tip] allOf 的结果获取技巧
> `allOf` 返回 `CompletableFuture<Void>`，==不包含结果==。需通过各子任务的 `join()` 获取。可用工具方法封装：
> ```java
> // 泛型工具：等待全部完成并收集结果
> @SafeVarargs
> static <T> CompletableFuture<List<T>> allOfToList(CompletableFuture<T>... futures) {
>     CompletableFuture<Void> allDone = CompletableFuture.allOf(futures);
>     return allDone.thenApply(v -> Arrays.stream(futures)
>         .map(CompletableFuture::join)
>         .collect(Collectors.toList()));
> }
> ```

## anyOf：任一完成即返回

```java
// anyOf：任一任务完成即返回，结果为最先完成的那个
CompletableFuture<String> fast = CompletableFuture
    .supplyAsync(() -> { Thread.sleep(100); return "快速"; });
CompletableFuture<String> slow = CompletableFuture
    .supplyAsync(() -> { Thread.sleep(3000); return "慢速"; });

CompletableFuture<Object> any = CompletableFuture.anyOf(fast, slow);
System.out.println(any.join());  // "快速"（100ms 后返回）
```

> [!info] 多任务编排对比
> | 方法 | 等待条件 | 返回值 | 适用场景 |
> |------|---------|--------|---------|
> | `allOf(cf...)` | ==全部完成== | `Void` | 并行查询后汇总 |
> | `anyOf(cf...)` | ==任一完成== | `Object` | 多源竞速取最快 |

---

# 实战场景

## 场景一：并行查询汇总

```java
/**
 * 并行调用用户服务、订单服务、商品服务，汇总返回详情
 */
public UserDetailDTO getUserDetail(Long userId) {
    // 三个查询并行执行
    CompletableFuture<User> userFuture = CompletableFuture
        .supplyAsync(() -> userService.getById(userId), executor);

    CompletableFuture<List<Order>> orderFuture = CompletableFuture
        .supplyAsync(() -> orderService.getByUserId(userId), executor);

    CompletableFuture<List<Coupon>> couponFuture = CompletableFuture
        .supplyAsync(() -> couponService.getByUserId(userId), executor);

    // 等待全部完成，组装结果
    try {
        CompletableFuture.allOf(userFuture, orderFuture, couponFuture).get(3, TimeUnit.SECONDS);
        return new UserDetailDTO(userFuture.join(), orderFuture.join(), couponFuture.join());
    } catch (Exception e) {
        throw new RuntimeException("查询用户详情超时", e);
    }
}
```

## 场景二：链式依赖调用

```java
/**
 * 串联调用：用户 → 订单 → 物流，每步依赖上一步结果
 */
public CompletableFuture<LogisticsDTO> getLogisticsInfo(Long userId) {
    return CompletableFuture
        // 1. 异步获取用户
        .supplyAsync(() -> userService.getById(userId), executor)
        // 2. 根据用户ID获取订单
        .thenComposeAsync(user -> CompletableFuture
            .supplyAsync(() -> orderService.getByUser(user.getId()), executor), executor)
        // 3. 根据订单ID获取物流
        .thenComposeAsync(order -> CompletableFuture
            .supplyAsync(() -> logisticsService.getByOrder(order.getId()), executor), executor)
        // 4. 异常兜底
        .exceptionally(ex -> {
            log.error("获取物流信息失败", ex);
            return LogisticsDTO.empty();
        });
}
```

## 场景三：超时控制

```java
/**
 * CompletableFuture 超时控制（Java 9+ 有 orTimeout，Java 8 需手动实现）
 */
// Java 9+：原生超时
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> slowCall())
    .orTimeout(3, TimeUnit.SECONDS)                    // 3秒超时
    .exceptionally(ex -> "超时兜底");                   // 超时返回默认值

// Java 8：手动超时控制
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> slowCall());
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.schedule(() -> future.complete("超时兜底"), 3, TimeUnit.SECONDS);
String result = future.join();  // 3秒内完成返回结果，否则返回"超时兜底"
```

## 场景四：多源竞速

```java
/**
 * 多缓存源竞速：先到先用
 */
public String getFromCache(String key) {
    CompletableFuture<String> localCache = CompletableFuture
        .supplyAsync(() -> localCacheService.get(key));
    CompletableFuture<String> redisCache = CompletableFuture
        .supplyAsync(() -> redisService.get(key));

    // 哪个先返回就用哪个
    return (String) CompletableFuture
        .anyOf(localCache, redisCache)
        .join();
}
```

---

# 线程池与原理

## 线程模型

```
supplyAsync(task)
       ↓
  提交到线程池执行
       ↓
  ┌─────────────────────┐
  │  ForkJoinPool       │  ← 默认线程池
  │  commonPool         │     线程数 = CPU核心数 - 1
  │  （全局共享）         │
  └─────────────────────┘
       ↓
  任务完成
       ↓
  触发 thenApply / thenCompose 等回调
       ↓
  回调在哪个线程执行？
  ├── 同步版：在完成的线程中执行
  ├── Async版：提交到 ForkJoinPool
  └── Async(executor)版：提交到自定义线程池
```

## 默认线程池的问题

> [!danger] commonPool 的风险
> `ForkJoinPool.commonPool()` 是 ==全局共享== 的线程池，所有 `parallelStream` 和未指定线程池的 `CompletableFuture` 共用：
> - ==IO 阻塞任务== 会占满线程，导致 `parallelStream` 也被阻塞
> - 线程数少（`CPU - 1`），==不适合 IO 密集型== 任务
> - 一个任务异常导致线程终止，影响全局

```java
// ❌ 危险：IO 操作用默认线程池
CompletableFuture.supplyAsync(() -> httpCall());  // 阻塞 commonPool 线程

// ✅ 正确：IO 操作用自定义线程池
ExecutorService ioExecutor = new ThreadPoolExecutor(
    20, 50, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(1000),
    new ThreadFactoryBuilder().setNameFormat("cf-io-%d").build()
);
CompletableFuture.supplyAsync(() -> httpCall(), ioExecutor);
```

> [!tip] 线程池配置建议
> | 任务类型 | 推荐线程数 | 说明 |
> |---------|-----------|------|
> | ==CPU 密集型== | `CPU + 1` | 纯计算，线程数不宜多 |
> | ==IO 密集型== | `CPU × 2` 或更多 | 大量等待，线程数可以多 |
> | ==混合型== | 按瓶颈类型配置 | 或拆分为不同线程池 |

---

# 总结

> [!abstract] 核心知识速记
> **创建**：`supplyAsync`（有返回）/ `runAsync`（无返回）→ ==传自定义线程池==
>
> **链式处理**：`thenApply`（转换）→ `thenAccept`（消费）→ `thenCompose`（串联异步）→ `thenCombine`（合并独立）
>
> **异常处理**：`exceptionally`（兜底）→ `handle`（结果+异常）→ `whenComplete`（观察不改）
>
> **多任务**：`allOf`（全部完成）→ `anyOf`（任一完成）
>
> **关键陷阱**：默认 commonPool 不适合 IO → `join` 抛非受检异常 → `whenComplete` 不能修改结果 → 异常不处理会被静默吞掉

> [!warning] 生产环境清单
> - [ ] ==所有异步任务传入自定义线程池==（不用 commonPool）
> - [ ] ==所有链式调用末尾加异常处理==（`exceptionally` 或 `handle`）
> - [ ] ==所有 `get()` 设置超时时间==（防止永久阻塞）
> - [ ] ==不在回调中执行阻塞操作==（会占用线程池线程）
> - [ ] ==线程池合理命名==（便于排查问题）

---

# 相关笔记

- [[Lambda表达式]] — Lambda 语法是 CompletableFuture 的基础
- [[函数式接口]] — `Supplier`、`Function`、`Consumer` 等接口
- [[Stream流]] — 同属 Java 8 函数式编程，Stream 是同步流式处理，CF 是异步编排
- [[Optional]] — 同为 Java 8 新特性，处理 null 与异步的互补
- [[接口默认方法]] — `CompletableFuture` 大量依赖接口默认方法实现链式 API
