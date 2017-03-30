# Spring 4 支持的 Java8特性

Spring 框架 4 支持 Java 8 语言和 API 功能。在本文中，我们将重点放在 Spring 4 支持新的 Java 8 的功能。最重要的是 Lambda 表达式，方法引用，JSR-310 的日期和时间，和可重复注释。

## Lambda 表达式

Spring 的代码库使用了 Java 8 大量的函数式接口，Lambda 表达式可以用来编写更干净和紧凑的代码。每当出现函数式接口的对象的预期时我们便可以提供一个 Lambda 表达式。让我们进一步继续之前首先学习函数式接口。

### 函数式接口

有单一抽象方法的接口被称为函数式接口。下面是 JDK 中函数式接口的一些例子：

![img](http://img2.tuicool.com/JNbI7rZ.png!web)

![img](http://img1.tuicool.com/3Y7fyiR.png!web)

![img](http://img2.tuicool.com/faMZ7fq.jpg!web)

Comparator 是仅具有一个抽象的非对象方法的函数。尽管声明了两个抽象方法，因为 equals 是对应于对象的公共方法所以从计数里排除了。其中有一个对象类方法且没有非对象方法的接口并不是函数式接口。

![img](http://img1.tuicool.com/uAb2iuI.png!web)

一个接口如果有一个抽象的非对象类方法并且扩展自具有唯一对象类方法的非函数式接口，则称为函数式接口。

![img](http://img0.tuicool.com/FrAFjma.png!web)

Spring 框架的函数式接口的例子：

![img](http://img2.tuicool.com/mUreeiN.png!web)

![img](http://img1.tuicool.com/vi6zmuR.png!web)

@FunctionalInterface 注解可以在接口声明的顶部声明中被使用，但这并不是必需的。此注解用于由编译器来检测该接口是不是有效的函数式接口。如果我们试图在接口里定义多个单一抽象方法，编译器将抛出一个错误。

![img](http://img0.tuicool.com/VvMBb2z.png!web)

![img](http://img0.tuicool.com/v6B7RnB.png!web)

### 函数描述符

接口的函数描述符是该接口的一个抽象方法的方法的类型。该方法类型包括参数类型，返回类型和 throws 子句。

例:

```java
Interface
```

```java
Runnable
```

```java
Comparable
```

```java
Comparator
```

```java
interface A { void foo() throws IOException; } 
interface B void foo() throws EOFException; } 
interface AB extends A, B {}
```

```java
interface X {List<String> bar(List<String> arg) throws IOException, SQLTransientException;}
interface Y {List bar(List<String> arg) throws EOFException, SQLException;} 
interface Z extends X, Y {}
```

### 如何编写 Lambda 表达式

Lambda 表达式的语法可以拆分成三部分：

\* 一个箭头 (–>)

\* 参数列表:

一个 Lambda 表达式可以包含0个或多个参数

例:

```java
() → { System.out.println(“ No arguments”)  } ;
```

```java
(String arg) → { System.out.println(“ One argument :  ”+arg); }
```

(String arg1, Integer arg2) → { System.out.println(“Two arguments : ”+arg1+” and ”+arg2); }

\* 表达式体:

可以是单个表达式或代码块。单个表达式将被简单地求值并返回。

例: (String arg) → { System.out.println(“ One argument : ”+arg); }

如果表达式体（Body）中存在语句块，那么它将被判定为方法体，并且在块执行后隐藏的返回语句将控制权交给调用者。

现在我们看一下如何使用 Lambda 表达式：

例1：

// 使用 Lambda 表达式

![img](http://img1.tuicool.com/BrU3qaN.png!web)

例2：

//使用 Lambda 表达式

![img](http://img2.tuicool.com/Mn2meuv.png!web)

你可以通过 Spring 的回调函数使用 Lambda 表达式。例如，用一个 ConnectionCallback 检索给定 JDBC 连接的列表，可写成如下语句：

```java
jdbcTemplate.execute(connection -> connection.getCatalog())
```

### 方法引用

函数式接口也可以使用方法引用来实现，引用方法或构造函数但并不调用它们。方法引用和 Lambda 表达式是类似的，但方法引用是指现有类的方法，而 Lambda 定义了一个匿名方法，并将其作为函数式接口的实例。

在 Java 8 中一个新增包中包含了常用于 Lambda 表达式和方法引用的函数式接口：java.util.function。

Date Time API

在 Java 中现有的 Date 和 Time 类存在多个问题。Date 和 Calendar 类的最大问题之一是它们不是线程安全的。在编写日期处理代码时开发人员不得不特别小心并发问题。Date 类也不支持国际化，因此不支持时区。开发人员必须编写大量的代码来支持不同的时区。

Date 和 Time 类也显现出不佳的 API 设计。java.util.Date 中的月从0，日从1，年从1900开始。缺少一致性。现在这些与 Date 和 Time 类的其它几个问题在 Java 8 中的新 Date 和 Time API 中已解决。

在 java.time 包下新的 Date 和 Time API 的重要的类是 LocalDate，LocalTime 和 ZonedDateTime。

LocalDate 和 LocalTime

LocalDate 表示日期时的默认格式为 YYYY-MM-DD，并没有时间。这是一个不可变类。我们可以使用 now() 方法获得的当前日期。

新建 LocalDate 实例的例子：

//获取当前日期

![img](http://img1.tuicool.com/2eE3qaN.png!web)

我们也可以通过对年，月，日的输入参数来新建 LocalDate 实例。

// 2016年4月1日

![img](http://img0.tuicool.com/zaUFb2a.png!web)

LocalTime 表示无日期的时间，是不变的。时间的默认格式为 hh:mm:ss.zzz。

新建 LocalTime 实例的例子：

//获取当前时间

// 18:30:30

![img](http://img2.tuicool.com/nuqi2qm.png!web)

默认情况下，LocalDate 和 LocalTime 类使用默认时区的系统时钟。这些类还提供了通过重载 new() 方法对修改时区的支持。可以通过传递 zoneid 来获得一个特定时区中的日期。

例子：

// 当前本地日期加尔各答（印度）

![img](http://img1.tuicool.com/ErYBVn6.png!web)

此外，还有一个类，LocalDateTime 组合了日期和时间，默认格式为 yyyy-MM-ddTHH:MM:ss.zzz·。

//当前日期和时间

![img](http://img1.tuicool.com/6byaq2N.png!web)

// 2016-04-01 13:30

![img](http://img1.tuicool.com/F7Nbmu2.png!web)

ZonedDateTime

这是一个不可变的类，用于表示包括时区信息的日期和时间。我们可以使用这个类的一个实例来表示特定事件，如在世界的某些地区一个会议。

// 当前时间使用系统的时间和默认区域

![img](http://img1.tuicool.com/222amqB.png!web)

![img](http://img0.tuicool.com/eA3Q7fb.png!web)

// 当前时间使用特定时区的系统时钟

![img](http://img0.tuicool.com/zaIZV3R.png!web)

Spring 4 提供了一个转换框架，支持做为 Java 8 日期和时间 API 一部分的所有类。Spring 4 可以使用一个 2016-9-10 的字符串，并把它转换成 Java 8 LocalDate 的一个实例。Spring 4 还支持通过 @DateTimeFormat 注解格式化 Java 8 Date-Time 字段。@DateTimeFormat 声明一个字段应该格式化为日期时间。

### 重复注解

在 Java 8 之前，将相同类型的多个注释加到声明或类型（例如一个类或方法）中是不允许的。作为一种变通方法，开发人员不得不将它们组合在一起成为单个容器注解。

例：

![img](http://img1.tuicool.com/aENvAnv.png!web)

重复注解允许我们重写相同的代码并不需显式地使用容器注解。虽然容器注解没有在这里使用的，Java 编译器负责将两个注解封装成一个容器：

例：

![img](http://img1.tuicool.com/2Mziyej.png!web)

### 定义重复注解

定义一个重复注解，通过可重复使用的 @Repeatable 注解来进行标注，或创建一个具有重复注解类型系列属性的注解。

第1步：声明重复注解类型：

![img](http://img2.tuicool.com/zUBRv23.png!web)

第2步：声明容器注解类型。

![img](http://img0.tuicool.com/EBZvYnm.png!web)

全部的实现如下所示：

![img](http://img1.tuicool.com/vaYBraQ.png!web)

为了获得在运行时的注解信息，通过 @Retention(RetentionPolicy.RUNTIME) 注释即可。

### 检索注解

getAnnotationsByType() 或 getDeclaredAnnotationsByType() 是用于访问注解反射 API 中的新方法。

注解还可以通过它们的容器注解用 getAnnotation() 或 getDeclaredAnnotation() 进行访问。

结论

Spring 4 还可运行在 Java 6 和 Java 7 中。由于 Spring 使用了很多的函数式接口，用 Java 8 和 Spring 4，你将能够使用 Lambda 表达式和函数式接口，并可写出更干净、紧凑的代码。

新的 Date 和 Time API 解决了 java.Util.Date 类中长期存在的问题，并引入了像 LocalDate，LocalTime 等许多新类，使日期和时间的编程变得更有意思了。你已经采用 Java 8 + Spring 4 了吗？请在 Twitter 上 @MyEclipseIDE 或在MyEclipse 论坛跟我们分享您的反馈。