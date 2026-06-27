---
title: Java Optional 类详解
date: 2026-06-16
tags:
  - Java
  - Java8
  - Optional
aliases:
  - Optional
  - 空值处理
---


> [!abstract] 概述
> `Optional` 用于包装可能为 `null` 的对象，

- 适合：
	- 方法返回值 ：明确表达可能为 `null` 的值
	- 嵌套对象处理 ：简化多层 `null` 检查
	- 复杂的 `null` 检查链 ：提高代码可读性
	- 替代 `if` 判空抛异常 ：简化异常处理

- 不适合：
	- 值永远不为 `null` 的场景
	- 简单局部变量的 `null` 检查
	- 方法参数和类字段
	- 基本类型的简单 `null` 检查

# 获取一个Optional对象

```java
// 最常用
Optional.ofNullable(T value); // 创建包含值的 Optional，如果 value 为 null 则创建空 Optional

Optional.of(T value); // 创建包含非 null 值的 Optional，如果 value 为 null 会抛异常 NPE
Optional.empty(); // 创建空 Optional
```

# 获取 Optional 内的值

```java
// 包装一个可能为 null 的对象，用泛型代替
Optional<T> opt = Optional.ofNullable(value);

// 最常用
opt.ifPresent(value -> System.out.println("值: " + value)); // 如果值存在，则执行消费函数

opt.isPresent(); // 检查 Optional 是否包含非 null 值
if (opt.isPresent()) { String value = opt.get(); }
String value = opt.get(); // 获取 Optional 中的值，如果值为 null 则抛出 NoSuchElementException
```

# 处理 null 值

```java
// 最常用
opt.orElseThrow(() -> new IllegalArgumentException("值不存在")); // 不为空则会返回容器内值，可代替 if 判空抛异常

opt.orElse(T other); // 如果值存在返回值，否则返回 other
opt.orElseGet(() -> createDefaultValue()); // 与 orElse 类似，不同的是只有在值不存在时才会执行函数
```

# 过滤

```java
// filter - 过滤 Optional 中的值
// 如果 optional 内 值为 null 会跳过 filter 过滤，所以不用担心方法调用会抛 NPE
// 不符合条件会被过滤掉，配合 orElseThrow 抛异常可用来代替复杂的 if 判断
// 验证用户输入

Optional<String> usernameOpt = Optional.ofNullable(inputUsername);
usernameOpt
    .filter(s -> s.length() >= 3)
    .filter(s -> s.length() <= 20)
    .filter(s -> s.matches("[a-zA-Z0-9_]+"))
    .orElseThrow(() -> new IllegalArgumentException("用户名格式不正确"));
```

# 转换

```java
// 类似 stream 流的 map 方法，可用于对象取属性
// 最常用
opt.map(String::toUpperCase).ifPresent(System.out::println); // 转换值并处理结果

// 详细示例
// 1. map - 转换 Optional 中的值
// 基本用法
// 获取字符串长度  
Integer len = Optional.ofNullable("hello")  
        .map(String::length)  
        .orElse(-1);  
System.out.println("字符串长度" + len);

// 2. map - 获取嵌套对象的指定值
User user = new User();  
Address address = new Address();  
City city = new City();  
city.setName("南京");  
address.setCity(city);  
user.setName("迪迦");  
user.setAddress(address);  
  
String cityName = Optional.ofNullable(user)  
        .map(User::getAddress)  
        .map(Address::getCity)  
        .map(City::getName)  
        .orElse("未知城市");  
  
System.out.println("城市: " + cityName); // 南京

@Data  
class User {  
    private String name;  
    private Address address;  
}  
  
@Data  
class Address {  
    private City city;  
}  
  
@Data  
class City {  
    private String name;  
}
```