---
title: Java Lambda 表达式
date: 2026-06-16
tags:
  - Java
  - Java8
  - Lambda
aliases:
  - Lambda表达式
  - Lambda
---

# 介绍

> [!abstract] 概述
> Lambda 是一个**匿名函数**，我们可以把 Lambda 表达式理解为是一段可以传递的代码（将代码像数据一样进行传递）。使用它可以写出更简洁、更灵活的代码。作为一种更紧凑的代码风格，使Java的语言表达能力得到了提升。

- Lambda 基本语法

~~~java
// parameters表示方法的参数, expression是一个表达式
(parameters) -> expression

// statements是一个或多个语句块
(parameters) -> { statements; }
~~~

> 格式
> `->` : Lambda 操作符 或 箭头操作符
> `左边->`：Lambda 形参列表 （其实就是接口中的抽象方法的形参列表）
> `->右边`：Lambda 体 （其实就是重写的抽象方法的方法体）


- Lambda 表达式的返回值

一个简单的 Lambda 表达式如 `(参数) -> 表达式` 返回的对象是一个实现了相应函数式接口的对象。具体来说：

# 使用

Lambda 表达式在其他语言中是一个**匿名函数**，但在java中更像一个 [[函数式接口]] 的实例，所以只有创建**函数式接口实例**的时候才能使用 Lambda 表达式。

## 无参数，无返回值

在java多线程中，经常使用 `Runnable` 接口，`Runnable` 就是一个函数式接口，在使用时我们通常这样写。

```java
@Test
public void test1(){
    Runnable r1 = new Runnable() {
        @Override
        public void run() {
            System.out.println("哈哈哈哈哈");
        }
    };
    r1.run();
}
// 使用Lambda 表达式可以这么写。
@Test
public void test2(){
    Runnable r2 = () -> {
      System.out.println("呵呵呵呵呵");
    }
    r2.run();
}
```

> **详解**
> 因为 Runnable 只有一个抽象方法，所以可以直接使用 `() -> {}` 来代替 `new Runnable(){}`
> 
> `() -> {}`：`()` 里面是函数需要的参数，因为 `run` 不需要参数所以 `()` 不需要传参。`{}` 里面是 `run` 方法中需要执行的内容。

## 有参数，没有返回值

Lambda 表达式还可以传递参数，如

```java
// 没有Lambda 之前
@Test
public void test1(){
  Consumer<String> con = new Consumer<String>() {
        @Override
        public void accept(String s) {
            System.out.println(s);
        }
    };
    con.accept("谎言和誓言的区别是什么？");
}

// Lambda 表达式
@Test
public void test2(){

    Consumer<String> con1 = (String s) -> {
        System.out.println(s);
    };
    con1.accept("一个是听得人当真了，一个是说的人当真了");
}
```

> **注意**
> `Consumer <String>` ：数据类型已经规定了，所以参数的数据类型可以省略。(s) -> {}
> 
> 只有一个参数时 `()` 可以省略。s -> {}
> 
> 函数体中只有一条语句`{}`也可以省略。s -> System.out.println(s);

## 有参数，有返回值

```java
@Test
public void test1(){
    Comparator<Integer> com1 = (o1,o2) -> {
        return o1.compareTo(o2);
    };
    System.out.println(com1.compare(12,6));
}
```

> **注意**
> 
> 参数方面的和上面的一样
> 
> 有返回值，同时函数体只有一条语句，此时`return`和`{}`都可以省略。`(o1,o2) -> o1.compareTo(o2);`


## Lambda 表达式的返回值

- **返回类型**：Lambda 表达式的返回值类型由其所实现的函数式接口的抽象方法决定。
- 示例
	- 对于 `Function<T, R>` 接口，Lambda 表达式的返回值是类型 `R`。
	- 对于 `Predicate<T>` 接口，Lambda 表达式的返回值是 `boolean`。
	- 对于 `Consumer<T>` 接口，Lambda 表达式没有返回值（返回类型为 `void`）。

# 示例代码

```java

// 示例：使用 Function 接口
Function<Integer, String> intToString = (num) -> "Number: " + num;
// 返回类型是 String

// 示例：使用 Predicate 接口
Predicate<String> startsWithA = (name) -> name.startsWith("A");
// 返回类型是 boolean

// 示例：使用 Consumer 接口
Consumer<String> printName = (name) -> System.out.println(name);
// 返回类型是 void
```



