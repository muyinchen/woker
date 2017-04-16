# 你真的开始用JDK8了吗？

JDK8正式版已经发布三年了，JDK9预计将于今年9月发布。很多应用都已经升级到了jdk8，java的大部分开源框架也早已支持jdk8，但是你真正开始使用jdk8了吗？jdk8给你的代码带来哪些改变？今天我们来梳理下，JDK8的特性给我们的代码带来哪些改变？

### Optional

NullPointerExceptions是在调用其他接口的时候，必须要考虑的问题。在业务代码中充斥着很多if(user!=null)..这样的判断空的代码。在Jdk8中提供了Optional来帮助我们优雅的解决NullPointerExceptions问题。

##### 使用示例

我们来看下面一个示例：

一个用户包含手机号和固定电话，输入一个用户对象，获取这个用户的联系方式，如果手机号不为空，则为手机号，否则如果固定电话不为空，则返回固定电话，否则默认返回“000000”

我们先来看下在jdk8以前的实现：

```java
User user = getUser();

if (user == null) {
    return "000000";

if (user.getMobilePhone() != null 
    && !user.getMobilePhone().isEmpty()) {
    return user.getMobilePhone();
}

if (user.getPhone() != null 
    && !user.getPhone().isEmpty()) {
    return user.getPhone();
}

return "000000";
```

我们接着看下使用Optional以后的用法：

```java
Optional<User> user = Optional.ofNullable(getUser());

Optional<String> mobilePhone = 
         user.flatMap((u) -> Optional.ofNullable(u.getMobilePhone()))
        .filter(s->!s.isEmpty());

Optional<String> phone = 
        user.flatMap((u) -> Optional.ofNullable(u.getPhone()))
        .filter(s->!s.isEmpty());

return mobilePhone.orElse(phone.orElse("000000"));
```

相信大多数开发者觉得第一种方式更容易看懂，而且对于这个需求来说，大部分开发者的实现也是第一种方式。因为第一种方式是大家从刚学编程语言开发，最熟悉的方式。从第一种方式转向使用Optional来实现本需求时，是一种思维的转变。从以往的null的方式转向Optional这种新的方式。那么这两种方式的思维有哪些改变呢？

第一种方式，是一种命令式的思维方式，这是我们从学编程的第一天起就熟悉的过程式的思维，程序执行了什么样的步骤，得到了什么样的结果。我们可以通过设计优秀的算法来优化执行的步骤，提高程序的性能。

第二种方式，是一种函数式的思维方式，在函数式的思维方式里，结果比过程更重要，不需要关注执行的细节。程序的具体执行由编译器来决定。这种情况下提高程序的性能是一个不容易的事情。

我们再回到Optional，来说说使用它容易陷入的误区

##### 使用isPresent&get来“优雅”的避免空值判断

我们知道了Optional为我们提供了可以优雅的避免空指针的方案，我们以上的代码还可以这样写

```java
Optional<User> user = Optional.ofNullable(getUser());

if (user.isPresent()) {
    return "000000";
}

if (Optional.ofNullable(user.get().getMobilePhone()).isPresent() 
        && !user.get().getMobilePhone().isEmpty()) {
    return user.get().getMobilePhone();
}
```

以上代码我们避免了像user==null这样的代码，我们使用了Optional的isPresent方法进行优雅的判空，但是这不是最好的方案，也不是使用Optional的初衷，所以当你的代码里出现了Optional的isPresent或者get方法你就要注意了，看下是否是正确的使用了Optional，另外在你使用get时，必须要使用isPresent做判断

##### 使用Optional类型做参数或者属性

在设计类或者方法时，也没有必要使用Optional作为参数和属性，可以使用它作为返回值，这是推荐的。参数和属性的本质是做数据的传递，Optional是作为一种避免使用null的容器类。

### Lambda Expressions

Lambda表达式也是jdk8中新增的一大特性，也是JDK8中最火的一个特性，在上节中，我们已经使用了Lambda表达式。并且Lambda表达式也是函数式编程思维的一种体现，上面也已经提到过，函数式编程思维。

示例如下，有一个名字集合，我们来对名字进行排序、查找能操作

```java
List<String> names = Arrays.asList("Name1", "Name2", "S4", 
                                   "Name3", "Name4", "S1", "S2");
```

#### 排序：使用()->{}替代匿名类

```java
//jdk8 before
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
        if (o1.startsWith("N") && !o2.startsWith("N")) {
           return -1;
        }
        return 1;
    }
});

//Lambda
Collections.sort(names, (o1, o2) -> o1.startsWith("N") 
                 && !o2.startsWith("N") ? -1 : 1);
```

#### 查找：循环

```java
//jdk8 before
List<String> startWithN = new ArrayList<>();
for (String name : names) {
    if (name.startsWith("N")) {
        startWithN.add(name);
    }
}

//Lambda
List<String> startWithN2 = names.stream()
        .filter((name) -> name.startsWith("N"))
        .collect(Collectors.toList());
```

在以上的示例中我们使用了stream，其中stream也是jdk8的一大特性这个将在下篇讲。

#### 函数式编程

在java8中为了支持函数式的编程，java8引入了java.util.function的包，其中Predicate接口是支持Lambda的函数式的编程。

```java
private static List<String> filter(List<User> users, 
                                   Function<User, String> function) {
    List<String> pros = new ArrayList<>();
    users.forEach((user) -> {
        pros.add(function.apply(user));
    });
    return pros;
}

List<User> users = new ArrayList<>();
users.add(new User("1232","2323"));
List<String> phones=filter(users,(user)->user.getPhone());
phones.forEach(System.out::println);
```

在以上的示例中，就实现了一个获取一个user的集合中，所有的电话号码的需求，当然以上的filter方法在stream中已经存在，直接使用即可，在上面我们只作为一个示例。

以上是直观的感受了Lambda表达式的写法的改变，下篇中，我们继续探讨Streams的用法以及接口的默认方法的支持。

#### Stream

Stream也是JAVA8的一大特点，这里的Stream和IO的那个Stream不同，它提供了对集合操作的增强，极大的提高了操作集合对象的便利性。下面我们就通过一个示例来看，使用Stream给我们带来哪些改变。

有一批学生考试数据，包括学生ID,班级ID,学科,分数。来计算如下指标

1. 找出所有语文科目，分数大于60分的学生
2. 找出语文科目，排名第一的学生
3. 计算学生ID为10的学生的总分
4. 所有学生按照总分从大到小

在JDK8之前，实现以上功能也非常简单的，相信对于一个刚刚学习Java的工程师来说，也很容易实现。不妨大家给自己设定一个限制，如何使用最少的代码实现以上功能。这里留作一个问题思考，下面我们使用Stream API来实现这些需求.

##### 筛选：filter用法

找出所有语文科目，分数大于60分的学生

```java
studentScores.stream()
        .filter(s -> "语文".equals(s.getSubject()) && s.getScore() >= 60f)
        .collect(Collectors.toList())
        .forEach(System.out::println);
```

##### 排序：sorted用法

找出语文科目，排名第一的学生

```java
Optional<StudentScore> studentScore = studentScores.stream()
        .filter(s -> "语文".equals(s.getSubject()))
        .sorted((s1, s2) -> s1.getScore() > s2.getScore() ? -1 : 1)
        .findFirst();
if (studentScore.isPresent()) {
    System.out.println(studentScore.get());
}
```

##### 统计计算：reduce

计算学生ID为10的学生的总分

```java
Double total = studentScores.stream()
        .filter(s -> s.getStudentId() == 10)
        .mapToDouble(StudentScore::getScore)
        .reduce(0, Double::sum);
System.out.println(total);
```

##### 分组统计：Collectors

所有学生按照总分从大到小

```java
studentScores.stream()
        .collect(Collectors.groupingBy(StudentScore::getStudentId
                , Collectors.summingDouble(StudentScore::getScore)))
        .entrySet()
        .stream()
        .sorted((o1, o2) -> o1.getValue() < o2.getValue() ? 1 : -1)
        .forEach(System.out::println);
```

以上的示例已经包括了Stream API的大部分的功能。从以上可以看到，在进行计算时，总是需要使用在集合对象中使用stream()方法，先转成Stream然后在进行后面的操作，为什么不直接在集合类下直接实现如下操作呢？Stream和集合类有哪些区别？Oracle给出了如下说明

1. Stream没有存储，它不是数据结构，并不保存数据。它可以像数组、生成器等数据源获取数据，通过一个计算流进行操作
2. 在功能性质上，通过流的操作，不会修改数据源，比如，filter操作，是从集合的流上获取一个新的流，而不是将过滤掉的元素从集合上删除
3. 延迟计算，许多流式的计算像filter、map等，是通过懒式的实现。一个数据流操作包括三个基本步骤，数据源->数据转换->执行获取结果。每次转换原有 Stream 对象不改变，返回一个新的 Stream 对象。数据转换的操作都是lazy的
4. 可以支持无限的大小。虽然集合是有限的，但是流是可以支持无限大小的，像limit(n)或者findFirst可以让无限的流操作在有限的时间内完成
5. 流的元素只能在一次创建中被访问到一次，像Iterator一样，必须生成一个新的流来访问新的元素

#### Interface default method

在JDK8中，使用forEach方法可以直接遍历数组，当然们进入查看forEach查看源代码时，我们在Iterable接口可以看到如下代码：

```java
default void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
    for (T t : this) {
        action.accept(t);
    }
}
```

这不是方法的实现么？是的，在接口里可以写实现了。在JDK8中为了支持新特性，必须对原有接口进行改造。需要在不破坏现有的架构情况下在接口里增加新方法。这也是JAVA8引入Default method的原因。但是引入Default method之后，需要思考两个问题：

##### 和抽象类区别

当接口有了default method 之后，接口看起来和抽象性是一样的，但是他们两个在Java8中还是有区别的。

抽象类，有自己的构造方法，并且可以定义自己的数据结构，但是默认方法只能调用接口时使用，并不会涉及到特定接口实现类的状态。具体使用接口还是抽象类还需要根据具体业务场景来界定

##### 接口的多继承问题

在java中可以支持多继承，如果两个接口实现了同样的默认方法，那么应该使用哪个呢？

比如：

```java
public interface DemoA {

    default void test() {
        System.out.println("I'm DemoA.");
    }
}

public interface DemoB {

    default void test() {
        System.out.println("I'm DemoB.");
    }
}
```

如果一个DemoImpl继承以上两个接口，代码如下：

```java
public class DemoImpl implements DemoA, DemoB {
}
```

这时，IDE会在DemoImpl下面有一条红线，提示不能继承在DemoA和DemoB中的test方法，需要实现该方法

```java
public class DemoImpl implements DemoA, DemoB {
    @Override
    public void test() {
        DemoB.super.test();
        DemoA.super.test();;
    }
}
```

实现该方法，和其他方式类似，你可以调用父类中的方法，也可以直接自己实现

#### Other features

除了以上的一些特性，JDK8中还支持了其他的一些特性值得关注

1. 引入了新的Date-Time API(JSR 310)来改进时间、日期的处理。
2. 引入新的Nashorn JavaScript引擎，使得我们可以在JVM上开发和运行JS应用。
3. 引入了Base64编码的支持
4. 新增了支持数组的并行处理的parallelSort方法等等