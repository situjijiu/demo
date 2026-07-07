# Java8 知识点巩固练习

> 本练习文档旨在帮助你巩固 Java8 的核心知识点,包括 Lambda 表达式、Stream 流、Optional、CompletableFuture、新日期API 等。通过由浅入深的练习,你将逐步掌握函数式编程和异步编程的精髓。

---

## 📚 基础练习(单个知识点)

### 1. Lambda表达式练习

#### 需求1.1: 简化集合排序

- **类定义**: 创建 `Student` 类,包含姓名(name)、年龄(age)、成绩(score)字段
- **排序要求**:
  - 按年龄升序排序
  - 按成绩降序排序
  - 按姓名长度排序,长度相同则按年龄排序

#### 需求1.2: 自定义函数式接口

- **接口定义**: 创建函数式接口 `StringProcessor`,包含方法 `String process(String str)`
- **实现要求**:
  - 字符串反转
  - 字符串首字母大写
  - 字符串去空格并转小写

#### 需求1.3: 方法引用练习

将以下 Lambda 表达式改写为方法引用:
```java
list.stream().map(s -> s.toUpperCase())
list.stream().map(s -> s.length())
list.stream().filter(s -> s.isEmpty())
```

---

### 2. Stream流练习

#### 需求2.1: 数据统计

- **类定义**: 创建 `Transaction` 类,包含金额(amount)、类型(type)、日期(date)字段
- **统计要求**:
  - 计算总交易金额
  - 计算每种类型的交易笔数
  - 找出金额最大的3笔交易
  - 按月份分组统计交易金额

#### 需求2.2: 数据转换

- **类定义**: 创建 `Employee` 类,包含部门(department)、姓名(name)、薪资(salary)字段
- **转换要求**:
  - 获取所有员工姓名列表
  - 按部门分组,每个部门按薪资降序排序
  - 找出每个部门薪资最高的员工
  - 计算每个部门的平均薪资

#### 需求2.3: 复杂查询

- **类定义**: 创建 `Order` 和 `OrderItem` 类(Order包含OrderItem列表)
- **查询要求**:
  - 查询总金额超过1000元的订单
  - 统计每种商品的销售数量
  - 找出购买次数最多的客户
  - 按订单日期分组,统计每天的订单数量

---

### 3. Optional练习

#### 需求3.1: 安全获取嵌套属性

- **类定义**: User -> Address -> City (嵌套关系)
- **实现要求**: 使用 Optional 安全获取用户所在城市名称,避免 NPE

#### 需求3.2: 参数校验

- **校验规则**:
  - 用户名长度3-20位,只允许字母数字下划线
  - 邮箱格式正确
  - 年龄在18-100之间
  - 校验失败抛出具体的异常信息

#### 需求3.3: 默认值处理

- **实现要求**:
  - 从Map中获取配置值,如果不存在则使用默认值
  - 支持配置值的类型转换(如String转Integer)
  - 配置值无效时调用方法生成默认值

---

### 4. CompletableFuture练习

#### 需求4.1: 异步查询

- **模拟服务**: 用户基本信息服务(耗时1秒)、订单服务(耗时2秒)、积分服务(耗时1.5秒)
- **实现要求**:
  - 三个服务异步调用
  - 汇总三个服务的结果返回
  - 设置超时时间3秒

#### 需求4.2: 链式调用

- **流程**: 查询用户信息 -> 查询商品库存 -> 扣减库存 -> 创建订单
- **实现要求**:
  - 每步异步执行,下一步依赖上一步结果
  - 每步失败都有异常处理和兜底方案

#### 需求4.3: 多数据源竞速

- **数据源**: 本地缓存、Redis缓存、数据库
- **实现要求**:
  - 同时查询三个数据源
  - 返回最先响应的结果
  - 其他查询自动取消(可选)

---

## 🔥 进阶练习(多知识点结合)

### 5. Stream + Optional 结合

#### 需求5.1: 安全的集合处理

- **数据**: 可能为null的用户列表
- **实现要求**:
  - 安全地过滤出年龄大于18岁的用户
  - 如果列表为null或过滤后为空,返回空集合而不是null
  - 提取用户的邮箱地址,过滤掉null和空字符串
  - 统计有效邮箱的数量

#### 需求5.2: 嵌套集合处理

- **数据**: 部门列表,每个部门包含员工列表
- **实现要求**:
  - 获取所有部门中薪资最高的员工(Optional处理可能为空的情况)
  - 按部门分组,统计每个部门的员工数量,处理部门无员工的情况
  - 查找指定部门中年龄最大的员工,部门不存在返回Optional.empty()

---

### 6. Lambda + Stream + 函数式接口

#### 需求6.1: 动态查询构建器

- **功能**: 灵活的查询构建器
- **实现要求**:
  - 使用 `Predicate<T>` 组合多个查询条件(年龄、薪资、部门)
  - 支持动态添加条件(and/or组合)
  - 使用 Stream 过滤数据
  - 支持结果转换(Function<T,R>)

**示例代码**:
```java
List<Employee> result = QueryBuilder.from(employees)
    .where(e -> e.getAge() > 25)
    .and(e -> e.getSalary() > 5000)
    .or(e -> "技术部".equals(e.getDepartment()))
    .select(e -> new EmployeeDTO(e))
    .execute();
```

#### 需求6.2: 策略模式优化

- **功能**: 使用 Lambda + 函数式接口优化策略模式
- **实现要求**:
  - 定义计算策略接口 `CalculationStrategy`
  - 实现多种计算策略(求和、平均值、最大值、最小值)
  - 使用 Stream 对数据进行计算
  - 支持策略的动态切换

---

### 7. CompletableFuture + Stream 结合

#### 需求7.1: 并行数据处理

- **数据**: 包含1000个URL的列表
- **实现要求**:
  - 并发请求所有URL(限制并发数为10)
  - 解析每个URL返回的JSON数据
  - 使用 Stream 汇总所有数据
  - 设置整体超时时间为30秒
  - 统计成功和失败的请求数量

#### 需求7.2: 异步流式处理

- **功能**: 订单处理系统
- **实现要求**:
  - 使用 Stream 读取订单列表
  - 对每个订单异步调用风控服务、库存服务、支付服务
  - 使用 allOf 等待所有异步任务完成
  - 使用 Stream 过滤出处理成功的订单
  - 异常订单使用 exceptionally 处理并记录日志

---

### 8. 新日期API + Stream

#### 需求8.1: 时间段统计

- **实现要求**:
  - 统计最近7天每天的销售总额
  - 找出销售额最高的日期
  - 计算环比增长率
  - 按周、月分组统计数据

#### 需求8.2: 时区处理

- **实现要求**:
  - 将用户活动时间按不同时区分组
  - 计算两个时间点之间的工作时长(排除周末和节假日)
  - 使用 Stream 统计每个时区的活跃用户数

---

## 🎯 综合练习(实际业务场景)

### 9. 电商数据分析系统

#### 数据模型

```java
class Order {
    Long orderId;              // 订单ID
    Long userId;              // 用户ID
    List<OrderItem> items;    // 订单项列表
    LocalDateTime createTime; // 创建时间
    OrderStatus status;       // 订单状态
}

class OrderItem {
    Long productId;           // 商品ID
    String productName;        // 商品名称
    Integer quantity;         // 数量
    BigDecimal price;         // 单价
}

class Product {
    Long productId;           // 商品ID
    String categoryName;      // 类别名称
    BigDecimal cost;          // 成本
}

class User {
    Long userId;              // 用户ID
    String username;          // 用户名
    Integer age;              // 年龄
    String city;              // 城市
}
```

#### 9.1 订单统计 (Stream)

- 统计每天的订单数量和总金额
- 按订单状态分组统计
- 找出订单金额最高的前10名用户
- 计算每个商品的销售数量和销售额

#### 9.2 用户分析 (Stream + Optional)

- 统计各年龄段用户的订单总额(使用 Optional 处理可能为空的年龄)
- 按城市分组,计算每个城市的用户数量和消费总额
- 找出消费金额最高的城市

#### 9.3 异步报表生成 (CompletableFuture)

- 异步查询订单数据、用户数据、商品数据
- 并行生成多个报表(日销售报表、用户消费报表、商品销售报表)
- 设置超时时间为10秒
- 报表生成失败时返回默认报表

#### 9.4 实时推荐 (Stream + Lambda)

- 根据用户历史订单,计算每个商品的购买次数
- 使用 Stream 找出用户购买最多的商品类别
- 使用 Optional 安全地获取推荐商品
- 组合多个推荐策略(Function接口)

---

### 10. 任务调度系统

#### 数据模型

```java
class Task {
    Long taskId;              // 任务ID
    String taskName;          // 任务名称
    TaskType type;            // 任务类型
    TaskPriority priority;    // 任务优先级
    LocalDateTime scheduleTime; // 调度时间
    TaskStatus status;        // 任务状态
}

enum TaskType {
    EMAIL_SEND, DATA_SYNC, REPORT_GENERATION, SYSTEM_BACKUP
}

enum TaskPriority {
    HIGH, MEDIUM, LOW
}

class TaskResult {
    Long taskId;              // 任务ID
    Boolean success;         // 是否成功
    String message;           // 结果消息
    LocalDateTime executeTime; // 执行时间
    Long duration;            // 执行时长(毫秒)
}
```

#### 10.1 任务管理 (Stream)

- 按优先级对任务排序
- 按任务类型分组统计
- 过滤出待执行的任务(状态为PENDING且调度时间已到)
- 找出执行时间最长的任务

#### 10.2 异步执行 (CompletableFuture)

- 异步执行任务(支持并发执行)
- 高优先级任务优先执行
- 设置任务超时时间(每个任务最长执行时间)
- 任务执行失败时记录异常并重试(最多3次)

#### 10.3 结果汇总 (Stream + Optional)

- 统计任务执行成功率
- 计算平均执行时间
- 找出执行时间最长的前5个任务
- 使用 Optional 处理可能为null的任务结果

#### 10.4 动态策略 (函数式接口)

- 支持动态配置任务执行策略(如邮件任务用邮件服务、数据同步任务用同步服务)
- 使用 `Function<Task, TaskResult>` 定义执行逻辑
- 使用 `Predicate<Task>` 定义任务过滤条件
- 使用 `Consumer<TaskResult>` 定义结果处理逻辑

#### 10.5 定时统计 (新日期API)

- 每小时统计任务执行情况
- 每天生成执行报告
- 计算任务执行时间分布(按小时)
- 统计工作时间和非工作时间的任务数量

---

### 11. 缓存系统优化

#### 11.1 缓存查询 (CompletableFuture)

- 同时查询本地缓存、Redis缓存、数据库(多数据源竞速)
- 使用 `anyOf` 返回最先响应的结果
- 查询失败时使用 `exceptionally` 提供兜底数据

#### 11.2 缓存更新 (Stream + 函数式接口)

- 批量更新缓存时,使用 Stream 并行处理
- 使用 `Consumer<T>` 定义缓存更新策略
- 使用 `Predicate<T>` 定义缓存过期条件

#### 11.3 缓存统计 (Stream)

- 统计缓存命中率
- 按缓存Key前缀分组统计
- 找出访问最频繁的缓存Key
- 计算缓存大小

#### 11.4 过期清理 (新日期API + Stream)

- 定期清理过期缓存(使用 `LocalDateTime` 判断过期)
- 统计各时间段过期的缓存数量
- 使用 Optional 安全地处理清理结果

---

## 💡 学习建议

### 学习路径

1. **基础阶段**: 完成基础练习(需求1-4),熟悉每个知识点的核心用法
2. **进阶阶段**: 完成进阶练习(需求5-8),学会知识点之间的组合使用
3. **综合阶段**: 完成综合练习(需求9-11),模拟真实业务场景

### 编码规范

- ✅ 所有方法必须添加**函数级注释**
- ✅ 使用中文注释
- ✅ 代码命名规范,使用有意义的变量名
- ✅ 注意异常处理和边界情况

### 关键知识点

| 知识点 | 核心技能 |
|--------|----------|
| Lambda表达式 | 函数式接口、参数简化、返回值处理 |
| Stream流 | 中间操作(filter/map/sorted)、终止操作(collect/reduce)、并行流 |
| Optional | 处理null值、避免NPE、理解使用场景 |
| CompletableFuture | 异步编程、链式调用、异常处理、多任务编排 |
| 新日期API | LocalDateTime、时区处理、日期计算 |

### 扩展挑战

- 🌟 使用 **接口默认方法** 优化代码结构
- 🌟 使用 **方法引用** 简化Lambda表达式
- 🌟 探索 **Collectors工具类** 的更多高级用法
- 🌟 研究 **CompletableFuture线程池** 的最佳配置

---

> **提示**: 如果你遇到问题或需要具体的实现示例,可以随时提问!
