# Java 8系列之重构和定制收集器



前面我们已经了解到了Collector类库中各种收集器的强大，可是，它们也只是能满足常用的场景。既然开放了Collector接口，我们当然可以根据自已意愿去定制，实际操作起来还是比较简单的。

# Collectors.joining源码解析

从前面，我们已经了解到一个Collector是由四部分组成的：

- Supplier<A> supplier(): 创建新的结果结
- BiConsumer<A, T> accumulator(): 将元素添加到结果容器
- BinaryOperator<A> combiner(): 将两个结果容器合并为一个结果容器
- Function<A, R> finisher(): 对结果容器作相应的变换

我们先Collectors.joining是怎么实现的：

```java
String strJoin = Stream.of("1", "2", "3", "4")
        .collect(Collectors.joining(",", "[", "]"));
System.out.println("strJoin: " + strJoin);
// 打印结果
// strJoin: [1,2,3,4]
```

这里，我们跟踪代码，看看Collectors.joining的源码：

```java
public static Collector<CharSequence, ?, String> joining(CharSequence delimiter,
                                                         CharSequence prefix,
                                                         CharSequence suffix) {
    return new CollectorImpl<>(
            () -> new StringJoiner(delimiter, prefix, suffix),
            StringJoiner::add, StringJoiner::merge,
            StringJoiner::toString, CH_NOID);
}
```

Collectors.joining实际上返回的是一个CollectorImpl对象，而其是Collector接口的实现类。在创建CollectorImpl对象时，通过方法引用，将StringJoiner的add()、merge()、toString()方法分别传递给accumulator()、combiner()及finisher()等四部分。

```java
static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
    private final Supplier<A> supplier;
    private final BiConsumer<A, T> accumulator;
    private final BinaryOperator<A> combiner;
    private final Function<A, R> finisher;
    private final Set<Characteristics> characteristics;

    CollectorImpl(Supplier<A> supplier,
                  BiConsumer<A, T> accumulator,
                  BinaryOperator<A> combiner,
                  Function<A,R> finisher,
                  Set<Characteristics> characteristics) {
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.combiner = combiner;
        this.finisher = finisher;
        this.characteristics = characteristics;
    }

    CollectorImpl(Supplier<A> supplier,
                  BiConsumer<A, T> accumulator,
                  BinaryOperator<A> combiner,
                  Set<Characteristics> characteristics) {
        this(supplier, accumulator, combiner, castingIdentity(), characteristics);
    }

    @Override
    public BiConsumer<A, T> accumulator() {
        return accumulator;
    }

    @Override
    public Supplier<A> supplier() {
        return supplier;
    }

    @Override
    public BinaryOperator<A> combiner() {
        return combiner;
    }

    @Override
    public Function<A, R> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }
}
```

首先要明确的是参数类型，

- 待收集元素的类型：String；
- 累加器的类型：StringCombiner；
- 最终结果的类型：String。

然后，我们一边阅读代码， 一边看图， 这样就能看清到底是怎么Collector工作的。由于Collector可以并行收集，为了可以了解清楚Collector的四部分的作用，我们这里以Collector在两个容器上并行执行。

Collector的每一个组件都是函数，因此我们使用箭头表示，Stream中的值用圆圈表示，最终生成的值用椭圆表示。Collector的一开始的工作就是创建一个容器。这里我们是实现了Supplier，这是一个工厂方法。

```java
public Supplier<StringJoiner> supplier() {
    return () -> new StringJoiner(delim, prefix, suffix);
}
```

![Supplier](http://img.blog.csdn.net/20170210144611958?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvSU9fRmllbGQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

Collector的accumulator函数的作用就是，它结合之前操作的结果和当前值，生成并返回新的值。 这一逻辑是通过StringJoiner::add方法实现的。

```java
public StringJoiner add(CharSequence newElement) {
    prepareBuilder().append(newElement);
    return this;
}
```

这里的accumulator用来将流中的值叠加入容器中.

![accumulator](http://img.blog.csdn.net/20170210150828625?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvSU9fRmllbGQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

combiner方法与reduce方法类似，将两个容器合并。由于Collector支持并发操作，如果不将多的容器合并，必然会导致数据的混乱。如果仅仅在串行执行，此步骤可以省略。这里，使用了StringJoiner::merge来实现，最后返回的是

```java
public StringJoiner merge(StringJoiner other) {
    Objects.requireNonNull(other);
    if (other.value != null) {
        final int length = other.value.length();、
        StringBuilder builder = prepareBuilder();
        builder.append(other.value, other.prefix.length(), length);
    }
    return this;
}
```

在收集阶段，Collector被combiner方法成对合并进一个容器，直到最后只剩一个容器为止.

![combiner](http://img.blog.csdn.net/20170210151449221?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvSU9fRmllbGQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

最后，finisher方法将StringJoiner转换为最后的结果，将toString方法内联到方法链的末端，这就将 StringCombiners转换成了我们想要的字符串。

```java
public String toString() {
    if (value == null) {
        return emptyValue;
    } else {
        if (suffix.equals("")) {
            return value.toString();
        } else {
            int initialLength = value.length();
            String result = value.append(suffix).toString();
            // reset value to pre-append initialLength
            value.setLength(initialLength);
            return result;
        }
    }
}
```

这样，我们就完成了Collector的自定义，好像还一点我们忽略掉了，那就是Collector的特征。正是忽略了这点，再自定义时，给自己挖了一个坑。关于Characteristics这个Enum看下官方文档吧，前面已经提到这里不再多述。

Collector自定义起来，也不是特别的麻烦，不过要明确以下几点：

1. 参数类型：这里最重要的是指定累加器的类型，一般都是自定义的过度类 
   待收集元素的类型：T；累加器的类型：A；最终结果的类型：R。
2. 累加器的逻辑
3. 最终结果的转换
4. Collector特征的选择

# 自定义Collector

现在有个简单的需求，求一段数字的和，如果是奇数，直接相加；如果是偶数，乘以2后在相加。这样的场景下，Collector类库中的收集器不能满足我们的需求，我们只能够自己定义了。

## 1.自定义类作为过渡容器

我们先定义一个类IntegerSum作为过渡容器。这里所说的容器并不一定是集合，只是对数据的临时存储，称之为过渡容器。在IntegerSum类内，定义了3个方法：

- doSum:作为累加器，实现求和操作

- doCombine:作为combine，将两个容器合并

- toValue:作为finisher，将IntegerSum转为所需要的结果Integer

  public class IntegerSum { 
  Integer sum;

  ```java
  public IntegerSum(Integer sum) {
      this.sum = sum;
  }

  public IntegerSum doSum(Integer item) {
      if (item % 2 == 0) {
          this.sum += item * 2;
      } else {
          this.sum += item;
      }
      return this;

  }

  public IntegerSum doCombine(IntegerSum it) {
      this.sum += it.sum;
      return this;
  }

  public Integer toValue() {
      return this.sum;
  }
  ```

  }

## 明确参数类型

- 待收集元素的类型：Integer
- 累加器的类型：IntegerSum
- 最终结果的类型：IntegerR

## 实现Collector接口

```java
Integer integerSum = Stream.of(1, 2, 3, 4)
        .collect(new Collector<Integer, IntegerSum, Integer>() {
            @Override
            public Supplier<IntegerSum> supplier() {
                return () -> new IntegerSum(2);
            }

            @Override
            public BiConsumer<IntegerSum, Integer> accumulator() {
                return IntegerSum::doSum;
            }

            @Override
            public BinaryOperator<IntegerSum> combiner() {
                return IntegerSum::doCombine;
            }

            @Override
            public Function<IntegerSum, Integer> finisher() {
                return IntegerSum::toValue;
            }

            @Override
            public Set<Characteristics> characteristics() {
                Set<Collector.Characteristics> CH_NOID = Collections.emptySet();
                return CH_NOID;
            }
        });
System.out.println("integerSum: " + integerSum); // 打印结果：integerSum: 18
```

在实现Collector接口时，我们通过方法引用的方式，指定了Collector的四部分的实现形式，见代码。对于Characteristics，并未对Collecotor设置特征。

这样一个简单的自定义Collector，就实现了。如果有兴趣，你可以试一下。