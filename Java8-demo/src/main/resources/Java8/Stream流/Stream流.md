---
title: Java Stream 流详解
date: 2026-06-16
tags:
  - Java
  - Java8
  - Stream
aliases:
  - Stream流
  - Stream API
---


> [!abstract] 概述
> Java8中有两大最为重要的改变。第一个是 Lambda 表达式；另外一个则是 Stream API。

Stream 是 Java8 中处理集合的关键抽象概念，它可以指定你希望对集合进行的操作，可以执行非常复杂的**查找、过滤和映射数据等**操作。 使用 Stream API 对集合数据进行操作，就类似于使用 SQL 执行的数据库查询。也可以使用 Stream API 来并行执行操作。简言之，Stream API 提供了一种高效且易于使用的处理数据的方式。

> **注意**
> 
> - Stream 自己不会存储元素。
> - Stream 不会改变源对象。相反，他们会返回一个持有结果的新 Stream。
> - **惰性求值**，流在中间处理过程中，只是对操作进行了记录，并不会立即执行，需要等到执行终止操作的时候才会进行实际的计算。

# Streams(流)

`java.util.Stream` 表示能应用在一组元素上一次执行的操作序列。Stream 操作分为中间操作或者最终操作两种，最终操作返回一特定类型的计算结果，而中间操作返回 `Stream` 本身，这样你就可以将多个操作依次串起来。`Stream` 的创建需要指定一个数据源，比如`java.util.Collection` 的子类，`List` 或者 `Set`， **`Map` 不支持**。`Stream` 的操作可以串行执行或者并行执行。

# 获得流方法

- `collection.stream()`：实现了 `Collection` 接口的类都可以用这种方式获取对应的流
	- 如：`List、ArrayList、LinkedList` 、`Set、HashSet、TreeSet`等 

- `map.entrySet().stream()`：通过该方法可以将双列集合转换为 stream 流操作

- `Arrays.stream()`：该方法可以将任意类型的数组转换为对应的流，
	- 需要注意的是对于基本数据类型数组如 `int、long、double` 该方法会返回对应的特殊流 `IntStream、LongStream、DoubleStream`
		- `public static IntStream stream(int[] array)`
		- `public static LongStream stream(long[] array)`
		- `public static DoubleStream stream(double[] array)`
	- 这些特殊流有特别实现的数据计算的方法如*求和、平均值、最大值、最小值*  对应方法 `sum、average、max、min`

- `Stream.of()`：调用Stream类静态方法 `of()`, 通过显示值创建一个流。它可以接收任意数量的参数。

# 流转换为其他对象

Java8 
- `stream.collect(Collectors.toList)`：返回列表
	- Collectors 类实现了很多归约操作，
	- 例如将流转换成集合和聚合元素。
	- Collectors 可用于返回列表或字符串

- 返回双列集合：
```java
Map<String, String> result = map.entrySet().stream()
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue
    ));
```

- [[Collectors常用方法]]

Java16
- `stream.toList()`：返回一个 `List`
- `stream.toArray()`：
	- 空参 返回一个 `Object[]` 数组
	- 新建数组 返回该数组

# [[流常用方法]]

# 一个简单的实现案例

看看 `Stream` 是怎么用，首先创建实例代码需要用到的数据 `List`：

```java
List<String> stringList = new ArrayList<>();
stringList.add("ddd2");
stringList.add("aaa2");
stringList.add("bbb1");
stringList.add("aaa1");
stringList.add("bbb3");
stringList.add("ccc");
stringList.add("bbb2");
stringList.add("ddd1");
```



Java 8 扩展了集合类，可以通过 `Collection.stream()` 或者 `Collection.parallelStream()` 来创建一个 `Stream`。下面几节将详细解释常用的 `Stream` 操作：

- `Collection.stream()` 、 `Collection.parallelStream()`：表示继承 `Collection` 接口的实现类都可以通过 `.stream()` 或 `.parallelStream()` 创建 `Stream`
- 如上文的集合 `ArrayList` 

```java

Stream<String> stream = stringList.stream();  
Stream<String> parallelStream = stringList.parallelStream();
```

