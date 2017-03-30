# Java８新特性总览

本文主要介绍 Java 8 的新特性，包括 Lambda 表达式、方法引用、流(Stream API)、默认方法、Optional、组合式异步编程、新的时间 API，等等各个方面。

## 写在前面

> - 本文是《Java 8 in Action》的读书笔记，主要提炼了概念性的知识/观点性的结论，对推导和阐释没有摘录
> - 文中涉及到的源码请参考我在 GitHub 上的项目 [java-learning](https://github.com/brianway/java-learning) (地址为 [https://github.com/brianway/java-learning](https://github.com/brianway/java-learning))的 Java 8 模块部分，比书中参考源码分类更清晰

## 基础知识

Java 8 的主要想法：

- stream API
- 向方法传递代码的技巧(方法引用、Lambda)
- 接口中的默认方法

三个编程概念：

- 流处理（好处：更高抽象，免费并行）
- 行为参数化（通过 API 来传递代码）
- 并行与共享的可变数据

函数式编程范式的基石：

- 没有共享的可变数据
- 将方法和函数即代码传递给其它方法的能力

Java 8 使用 Stream API 解决了两个问题：

- 集合处理时的套路和晦涩
- 难以利用多核

Collection 主要是为了存储和访问数据，而 Stream 则主要用于描述对数据的计算。

### 通过行为参数化来传递代码

行为参数化：类似于策略设计模式

`类 -> 匿名类 -> Lambda 表达式`，代码越来越简洁

### Lambda 表达式

Lambda 表达式：简洁地表示可传递的匿名函数的一种方式

重点留意这四个关键词：匿名、函数、传递、简洁

三个部分：

- 参数列表
- 箭头
- Lambda 主体

Lambda 基本语法，下面两者之一：

- `(parameters) -> expression`
- `(parameters) -> { statements; }`

函数式接口：只定义一个**抽象方法**的接口。函数式接口的抽象方法的签名称为 *函数描述符*

Lambda 表达式允许你以内联的形式为函数式接口的抽象方法提供实现，并把整个表达式作为函数式接口(一个具体实现)的实例。

常用函数式接口有：Predicate, Consumer, Function, Supplier 等等。

Lambda 的类型是从使用 Lambda 的上下文推断出来的。上下文中 Lambda 表达式需要的类型称为目标类型。

### 方法引用

方法引用主要有三类：

- (1) 指向静态方法的方法引用
  - Lambda: `(args) -> ClassName.staticMethod(args)`
  - 方法引用：`ClassName::staticMethod`
- (2) 指向任意类型实例方法的方法引用
  - Lambda: `(arg0, rest) -> arg0.instanceMethod(rest)`
  - 方法引用：`ClassName.instanceMethod`(arg0 是 ClassName 类型的)
- (3) 指向现有对象的实例方法的方法引用
  - Lambda: `(args) -> expr.instanceMethod(args)`
  - 方法引用：`expr::intanceMethod`

方法引用就是替代那些转发参数的 Lambda 表达式的语法糖

## 流(Stream API)

引入的原因：

- 声明性方式处理数据集合
- 透明地并行处理，提高性能

**流** 的定义：从支持数据处理操作的源生成的元素序列

两个重要特点：

- 流水线
- 内部迭代

流与集合：

- 集合与流的差异就在于什么时候进行计算
  - 集合是内存中的数据结构，包含数据结构中目前所有的值
  - 流的元素则是按需计算/生成
- 另一个关键区别在于遍历数据的方式
  - 集合使用 Collection 接口，需要用户去做迭代，称为外部迭代
  - 流的 Streams 库使用内部迭代

流操作主要分为两大类：

- 中间操作：可以连接起来的流操作
- 终端操作：关闭流的操作，触发流水线执行并关闭它

流的使用：

- 一个数据源（如集合）来执行一个查询；
- 一个中间操作链，形成一条流的流水线；
- 一个终端操作，执行流水线，并能生成结果。

流的流水线背后的理念类似于构建器模式。常见的中间操作有 `filter`,`map`,`limit`,`sorted`,`distinct`；常见的终端操作有 `forEach`,`count`,`collect`。

### 使用流

- 筛选
  - 谓词筛选：filter
  - 筛选互异的元素：distinct
  - 忽略头几个元素：limit
  - 截短至指定长度：skip
- 映射
  - 对流中每个元素应用函数：map
  - 流的扁平化：flatMap
- 查找和匹配
  - 检查谓词是否至少匹配一个元素：anyMatch
  - 检查谓词是否匹配所有元素：allMatch/noneMatch
  - 查找元素：findAny
  - 查找第一个元素：findFirst
- 归约（折叠）：`reduce(初值，结合操作)`元素求和最大值和最小值

`anyMatch`,`allMatch`,`noneMatch` 都用到了短路；`distinct`,`sorted`是有状态且无界的，`skip`,`limit`,`reduce`是有状态且有界的。

原始类型流特化：`IntStream`,`DoubleStream`,`LongStream`，避免暗含的装箱成本。

- 映射到数值流：`mapToInt`,`mapToDouble`,`mapToLong`
- 转换回流对象：`boxed`
- 默认值：`OptionalInt`,`OptionalDouble`,`OptionalLong`

数值范围：

- `range`:`[起始值，结束值)`
- `rangeClosed`:`[起始值，结束值]`

### 构建流

- 由值创建流：`Stream.of`,`Stream.empty`
- 由数组创建流：`Arrays.stream(数组变量)`
- 由文件生成流：`Files.lines`
- 由函数生成流：创建无限流，
  - 迭代： `Stream.iterate`
  - 生成：`Stream.generate`

### 用流收集数据

对流调用 `collect` 方法将对流中的元素触发归约操作（由 `Collector` 来参数化）。

Collectors 实用类提供了许多静态工厂方法，用来创建常见收集器的实例，主要提供三大功能：

- 将流元素归约和汇总为一个值
- 元素分组
- 元素分区

归约和汇总(`Collectors` 类中的工厂方法)：

- 统计个数：`Collectors.counting`
- 查找流中最大值和最小值：`Collectors.maxBy`,`Collectors.minBy`
- 汇总：`Collectors.summingInt`,`Collectors.averagingInt`,`summarizingInt`/`IntSummaryStatistics`。还有对应的 long 和 double 类型的函数
- 连接字符串：`joining`
- 广义的归约汇总：`Collectors.reducing(起始值，映射方法，二元结合)`/`Collectors.reducing(二元结合)`。`Collectors.reducing` 工厂方法是所有上述特殊情况的一般化。

`collect vs. reduce`，两者都是 `Stream` 接口的方法，区别在于：

- 语意问题
  - reduce 方法旨在把两个值结合起来生成一个新值，是不可变的归约；
  - collect 方法设计就是要改变容器，从而累积要输出的结果
- 实际问题
  - 以错误的语义使用 reduce 会导致归约过程不能并行工作

分组和分区

- 分组：`Collectors.groupingBy`多级分组按子数组收集数据: `maxBy`把收集器的结果转换为另一种结果 `collectingAndThen`与 groupingBy 联合使用的其他收集器例子：`summingInt`,`mapping`
- 分区：是分组的特殊情况，由一个谓词作为分类函数(分区函数)

收集器接口：Collector，部分源码如下：

```java
public interface Collector<T, A, R> {
    Supplier<A> supplier();
    BiConsumer<A, T> accumulator();
    Function<A, R> finisher();
    BinaryOperator<A> combiner();
    Set<Characteristics> characteristics();
}
```

其中 T、A、R 分别是流中元素的类型、用于累积部分结果的对象类型，以及 collect 操作最终结果的类型。

- 建立新的结果容器：`supplier` 方法
- 将元素添加到结果容器：`accumulator` 方法，累加器是原位更新
- 对结果容器应用最终转换： `finisher` 方法
- 合并两个结果容器：`combiner` 方法
- 定义收集器的行为： `characteristics` 方法，Characteristics 包含 `UNORDERED`,`CONCURRENT`,`IDENTITY_FINISH`

前三个方法已经足以对流进行顺序归约，实践中实现会复杂点，一是因为流的延迟性质，二是理论上可能要进行并行归约。

`Collectors.toList` 的源码实现：

```java
public static <T> Collector<T, ?, List<T>> toList() {
        return new CollectorImpl<>(
            (Supplier<List<T>>) ArrayList::new,
            List::add,
            (left, right) -> { left.addAll(right); return left; },
            CH_ID);
}
// static final Set<Collector.Characteristics> CH_ID = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
```

### 并行流

并行流就是一个把内容分成多个数据块，并用不同的线程分别处理每个数据块的流。

关于并行流的几点说明：

- 选择适当的数据结构往往比并行化算法更重要，比如避免拆箱装箱的开销，使用便于拆分的方法而非 iterate。
- 同时，要保证在内核中并行执行工作的时间比在内核之间传输数据的时间长。
- 使用并行流时要注意避免共享可变状态。
- 并行流背后使用的基础架构是 Java 7 中引入的分支/合并框架。

分支/合并框架

分支/合并框架的目的是以递归的方式将可以并行的任务拆分成更小的任务，然后将每个子任务的结果合并起来生成整体结果。

- `RecursiveTast<R>` 有一个抽象方法 compute，该方法同时定义了：将任务拆分成子任务的逻辑无法/不方便再拆分时，生成单个子任务结果的逻辑
- 对任务调用 fork 方法可以把它排进 ForkJoinPool，同时对左边和右边的子任务调用 fork 的效率要比直接对其中一个调用 compute 低，因为可以其中一个子任务可以重用同一线程，减少开销

工作窃取：用于池中的工作线程之间重新分配和平衡任务。

Spliterator 代表“可分迭代器”，用于遍历数据源中的元素。可以延迟绑定。

## 高效 Java 8 编程

### 重构、测试、调试

- 改善代码的可读性
  - 用 Lambda 表达式取代匿名类
  - 用方法引用重构 Lambda 表达式
  - 用 Stream API 重构命令式的数据处理
- 增加代码的灵活性
  - 采用函数接口
    - 有条件的延迟执行
    - 环绕执行

使用 Lambda 重构面向对象的设计模式：

- 策略模式
  - 一个代表某个算法的接口
  - 一个或多个该接口的具体实现，它们代表的算法的多种实现
  - 一个或多个使用策略对象的客户
- 模版方法
  - 传统：继承抽象类，实现抽象方法
  - Lambda：添加一个参数，直接插入不同的行为，无需继承
- 观察者模式
  - 执行逻辑较简单时，可以用 Lambda 表达式代替类
- 责任链模式
- 工厂模式
  - 传统：switch case 或者 反射
  - Lambda：创建一个 Map，将名称映射到对应的构造函数

调试的方法：

- 查看栈跟踪：无论 Lambda 表达式还是方法引用，都无法显示方法名，较难调试
- 输出日志：`peek` 方法，设计初衷就是在流的每个元素恢复运行之前，插入执行一个动作

### 默认方法

Java 8 中的接口现在支持在声明方法的同时提供实现，通过以下两种方式可以完成：

1. Java 8 允许在接口内声明 静态方法
2. Java 8 引入了一个新功能：默认方法

默认方法的引入就是为了以兼容的方式解决像 Java API 这样的类库的演进问题的。它让类可以自动地继承接口的一个默认实现。

向接口添加新方法是 **二进制兼容** 的，即如果不重新编译该类，即使不实现新的方法，现有类的实现依旧可以运行。默认方法 是一种以 **源码兼容** 方式向接口内添加实现的方法。

抽象类和抽象接口的区别：

- 一个类只能继承一个抽象类，但一个类可以实现多个接口
- 一个抽象类可以通过实例变量保存一个通用状态，而接口不能有实例变量

默认方法的两种用例：

- 可选方法：提供默认实现，减少空方法等无效的模版代码
- 行为的多继承
  - 类型的多继承
  - 利用正交方法的精简接口
  - 组合接口

如果一个类使用相同的函数签名从多个地方继承了方法，解决冲突的三条规则：

1. **类**中的方法优先级最高
2. 若 1 无法判断，那么子接口的优先级更高，即优先选择拥有最具体实现的默认方法的接口
3. 若 2 还无法判断，那么继承了多个接口的类必须通过显示覆盖和调用期望的方法，显示地选择使用哪一个默认方法的实现。

### Optional 取代 null

null 的问题：

- 错误之源：NullPointerException 问题
- 代码膨胀：各种 null 检查
- 自身无意义
- 破坏了 Java 的哲学: null 指针
- 在 Java 类型系统上开了个口子：null 不属于任何类型

`java.util.Optional<T>` 对可能缺失的值建模,引入的目的并非是要消除每一个 null 引用，而是帮助你更好地设计出普适的 API。

创建 Optional 对象,三个静态工厂方法：

- `Optional.empty`：创建空的 Optional 对象
- `Optional.of`：依据非空值创建 Optional 对象，若传空值会抛 NPE
- `Optianal.ofNullable`：创建 Optional 对象，允许传空值

使用 map 从 Optional 对象提取和转换值,Optional 的 map 方法：

- 若 Optional 包含值，将该值作为参数传递给 map，对该值进行转换后包装成 Optional
- 若 Optional 为空，什么也不做，即返回 Optional.empty

使用 flatMap 链接 Optional 对象：

由于 Optional 的 map 方法会将转换结果生成 Optional，对于返回值已经为 Optional 的，就会出现 `Optional<Optional<T>>` 的情况。类比 Stream API 的 flatMap，Optional 的 flapMap 可以将两层的 Optional 对象转换为单一的 Optional 对象。

**简单来说，返回值是 T 的，就用 map 方法；返回值是 Optional<T> 的，就用 flatMap 方法。这样可以使映射完返回的结果均为 Optional<T>**

- 参数为 null 时，会由 `Objects.requireNonNull` 抛出 NPE；参数为空的 Optional 对象时，返回 `Optional.empty`
- 参数非 null/空的 Optional 对象时，map 返回 Optional；flatMap 返回对象本身

原因可以参考这两个方法的源码：

```java
public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent())
        return empty();
    else {
        return Optional.ofNullable(mapper.apply(value));
    }
}

public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent())
        return empty();
    else {
        return Objects.requireNonNull(mapper.apply(value));
    }
}
```

另外，Optional 类设计的初衷仅仅是要支持能返回 Optional 对象的方法。设计时并未考虑将其作为类的字段，所以并未实现 `Serializable` 接口。

默认行为及解引用 Optional 对象：

- `get()`: 返回封装的变量值，或者抛出 `NoSuchElementException`
- `orElse(T other)`: 提供默认值
- `orElseGet(Supplier<? extends T> other)`: `orElse` 方法的延迟调用版
- `orElseThrow(Supplier<> extends X> exceptionSupplier)`: 类似 `get`，但可以定制希望抛出的异常类型
- `ifPresent(Consumer<? super T>)`: 变量存在时可以执行一个方法

### CompletableFuture:组合式异步编程

Future 接口有一定的局限性。CompletableFuture 和 Future 的关系就跟 Stream 和 Collection 的关系一样。

同步 API 与 异步 API

- 同步 API：调用方需要等待被调用方结束运行，即使两者是在不同的线程中运行
- 异步 API：直接返回，被调用方完成之前是将任务交给另一个线程去做，该线程和调用方是异步的，返回方式有如下两种：
  - 要么通过回调函数
  - 要么由调用方再执行一个“等待，直到计算完成”的方法调用

使用工厂方法 supplyAsync 创建 CompletableFuture 比较方便，该方法会抛出 CompletableFuture 内发生问题的异常。

代码的阻塞问题的解决方案及如何选择：

- 使用并行流对请求进行并行操作：适用于计算密集型的操作，且没有 I/O ，此时推荐使用 Stream 接口
- 使用 CompletableFuture 发起异步请求(可以使用定制的执行器)：若涉及等待 I/O 的操作，使用 CompletableFuture 灵活性更好

*注意，CompletableFuture 类中的 join 方法和 Future 接口中的 get 有相同的含义，join 不抛出检测异常。另外，需要使用两个不同的 Stream 流水线而不是同一个，来避免 Stream的延迟特性引起顺序执行*

构造同步和异步操作：

- `thenApply` 方法不会阻塞代码的执行
- `thenCompose` 方法允许你对两个异步操作进行流水线，第一个操作完成时，将其结果作为参数传递给第二个操作
- `thenCombine` 方法将两个完全不相干的 CompletableFuture 对象的结果整合起来

调用 get 或者 join 方法只会造成阻塞，响应 CompletableFuture 的 completion 事件可以实现等所有数据都完备之后再呈现。`thenAccept` 方法在每个 CompletableFuture 上注册一个操作，该操作会在 CompletableFuture 完成执行后使用它的返回值，即 `thenAccept` 定义了如何处理 CompletableFuture 返回的结果，一旦 CompletableFuture 计算得到结果，它就返回一个 `CompletableFuture<Void>`。

### 新的时间和日期 API

原来的 `java.util.Date` 类的缺陷：

- 这个类无法表示日期，只能以毫秒的精度表示时间
- 易用性差：年份起始 1900 年，月份从 0 起始
- toString 方法误导人：其实并不支持时区

相关类同样缺陷很多：

- `java.util.Calender` 类月份依旧从 0 起始
- 同时存在 `java.util.Date` 和 `java.util.Calender`，徒添困惑
- 有的特性只在某一个类提供，如 `DateFormat` 方法
- `DateFormat` 不是线程安全的
- `java.util.Date` 和 `java.util.Calender` 都是可变的

一些新的 API（`java.time` 包）

- `LocalDate`: 该类实例是一个**不可变对象**，只提供简单的日期，**并不含当天的时间信息**，也不附带任何和时区相关的信息
- `LocalTime`: 时间(时、分、秒)
- `LocalDateTime`: 是 `LocalDate` 和 `LocalTime` 的合体，不含时区信息
- `Instant`: 机器的日期和时间则使用 `java.time.Instant` 类对时间建模，以 Unix 元年时间开始所经历的秒数进行计算
- `Temporal`: 上面四个类都实现了该接口，该接口定义了如何读取和操纵为时间建模的对象的值
- `Duration`: 创建两个 Temporal 对象之间的 duration。`LocalDateTime` 和 `Instant` 是为不同目的设计的，不能混用，且不能传递 `LocalDate` 当参数。
- `Period`: 得到两个 `LocalDate` 之间的时长

`LocalDate`，`LocalTime`，`LocalDateTime` 三个类的实例创建都有三种工厂方法：`of`,`parse`,`now`

`Duration`，`Period` 有很多工厂方法：`between`,`of`,还有 ofArribute 之类的

以上日期－时间对象都是不可修改的，这是为了更好地支持函数式编程，确保线程安全

操纵时间：

- `withArribute` 创建一个对象的副本，并按照需要修改它的属性。更一般地，`with` 方法。但注意，**该方法并不是修改原值，而是返回一个新的实例**。类似的方法还有 `plus`,`minus` 等
- 使用 `TemporalAdjuster` 接口: 用于定制化处理日期，函数式接口，只含一个方法 `adjustInto`
- `TemporalAdjusters`: 对应的工具类，有很多自带的工厂方法。（如果想用 Lamda 表达式定义 TemporalAdjuster 对象，推荐使用 TemporalAdjusters 类的静态工厂方法 `ofDateAdjuster`）

打印输出及解析日期－时间对象：主要是 `java.time.format` 包，最重要的类是 `DateTimeFormatter` 类，所有该类的实例都是 **线程安全** 的，所以可以单例格式创建格式器实例。

处理不同的时区和历法使用 `java.time.ZoneId` 类，该类无法修改。

```java
// ZoneDateTime 的组成部分
ZonedDateTime = LocalDateTime + ZoneId
              = (LocalDate + LocalTime) + ZoneId
```

## 结语

本文主要对 Java 8 新特性中的 Lambda 表达式、Stream API、流(Stream API)、默认方法、Optional、组合式异步编程、新的时间 API，等方面进行了简单的介绍和罗列，至于更泛化的概念，譬如函数式编程、Java 语言以外的东西没有介绍。当然，很多细节和设计思想还需要进一步阅读官方文档/源码，在实战中去体会和运用。

## 参考资料

> - [《Java 8 in Action》(中文版)](https://book.douban.com/subject/26772632/)／[(英文版)](https://book.douban.com/subject/25912747/)
> - [Java 8新特性：全新的Stream API](http://www.infoq.com/cn/articles/java8-new-features-new-stream-api)
> - [Java 8 中的 Streams API 详解](https://www.ibm.com/developerworks/cn/java/j-lo-java8streamapi/)

另外附上 lucida 的几篇译文：

> - [深入理解Java 8 Lambda（语言篇——lambda，方法引用，目标类型和默认方法）](http://lucida.me/blog/java-8-lambdas-insideout-language-features/)
> - [深入理解Java 8 Lambda（类库篇——Streams API，Collectors和并行）](http://lucida.me/blog/java-8-lambdas-inside-out-library-features/)
> - 深入理解 Java 8 Lambda（原理篇——Java 编译器如何处理 lambda）暂时还没