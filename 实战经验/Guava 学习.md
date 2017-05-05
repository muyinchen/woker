# Guava base

## Guava中ComparisonChain类的学习和使用

在日常的工作中，我们经常需要对两个对象进行比较，以找出其中的异同， Java中提供了`compare/compareTo`，我们需要实现一个比较器[`Comparator`]，或者直接实现`Comparable`接口，不过当 对象的属性很多的时候，我们需要写大量的if else代码，代码不够优雅，Guava为我们简化了这一点，我们可以使用`ComparisonChain`类来优雅的实现对象之间的比较，接下来，我们通 过一个实例进行学习。

年纪也不小了，到了相亲的年纪，家里找了两个媒婆：媒婆1和媒婆2，给了钱，媒婆挺办事，都介绍了一个好姑娘，居然还都叫lisa，那么就需要比较一下两个姑娘是不是同一个人了。

我们唯一知道的就是两个姑娘的名字、身高和长相，我们也是通过这三点来进行比较，首先我们构造一个`Girl`对象，实现`Comparable`接口，来进行比较，传统方法，我们经常这样做：

```java
class Girl implements Comparable<Girl> {

    private String name;//名称
    private double height;//身高
    private String face;//长相

    Girl(String name, double height, String face) {
        this.name = name;
        this.height = height;
        this.face = face;
    }

    //传统方法我们这样比较
    @Override
    public int compareTo(Girl girl) {
        int c1 = name.compareTo(girl.name);
        if (c1 != 0){
            System.out.println("两个girl的name不相同");
            return c1;
        }
        int c2 = Double.compare(height, girl.height);
        if (c2 != 0){
            System.out.println("两个girl的height不相同");
            return c2;
        }
        int c3 = face.compareTo(girl.face);
        if(c3 !=0)
            System.out.println("两个girl的face不相同");
        return c3;
    }
}
```

然后，我们测试一下，居然不是一个人，该选择哪一个好呢？

```java
@Test
public void testCompareTo() {
    Girl g1 = new Girl("lisa", 175.00, "nice");
    Girl g2 = new Girl("lisa", 175.00, "beauty");
    //两个girl的face不相同
    System.out.println(g1.compareTo(g2) == 0);//false
}
```

而使用`Guava`提供的`ComparisonChain`类，我们可以这样进行比较，如下：

```java
//使用Guava提供的ComparisonChain我们这样比较
@Override
public int compareTo(Girl girl) {
    return ComparisonChain.start()
            .compare(name, girl.name)
            .compare(height, girl.height)
            .compare(face, girl.face)
            .result();
}
```

翻开`ComparisonChain`类的源码，我们发现，它其实是一个抽象类，其提供了主要有三个抽象方法，`start()`用于返回内部的一个 `ComparisonChain`实现；重载了许多`compare()`方法，用于接收各种类型的参数，`compare`方法返回的仍然是 `ComparisonChain`对象；result()方法用于返回比较后的结果。

总结：通过对`ComparisonChain`的学习，以及前面[Guava中Obejects实用工具类的学习]()等，我们发现，`Guava`中大量存在这种类似的链式编码，熟悉设计模式的不难理解，这正是Builder建造者模式的应用。

##  MoreObjects

Guava全文介绍地址:Google Guava

这次主要介绍是的是com.google.common.base.MoreObjects,官网是对它的介绍就一句话。Helper functions that operate on any Object, and are not already provided in Objects.具有非常有用的功能，包括已经没有被Obejcts提供的方法。

它的所有方法如下:

```java
static <T> T  firstNonNull(T first, T second)  
依次判断First,second,返回第一个不为空的对象。如果都为空抛出空NullPointerException  
  
static MoreObjects.ToStringHelper   toStringHelper(Class<?> clazz)  
创建一个MoreObjects.ToStringHelper的实例，就像Objects.toStringHelper(Object)一样。但是用一个普通的名字clazz代替用Object.getClass()的实例。  
  
static MoreObjects.ToStringHelper   toStringHelper(Object self)  
创建一个MoreObjects.ToStringHelper的实例  
  
static MoreObjects.ToStringHelper   toStringHelper(String className)  
创建一个MoreObjects.ToStringHelper的实例，就像Objects.toStringHelper(Object)一样。但是用className代替用Object.getClass()的实例。  
```

然后大家就可以对照下面的例子来理解这个类的用法了。

```java
public class MoreObjectsTest {  
  
    @Test  
    public void testFirstNonNullBothNotNull(){  
        String value = "foo";  
        String returned = firstNonNull(value,"bar");  
        assertThat(returned,is(value));  
    }  
  
    @Test  
    public void testFirstNonNullFirstNull(){  
        String value = "bar";  
        String returned = firstNonNull(null,value);  
        assertThat(returned,is(value));  
    }  
  
    @Test  
    public void testFirstNonNullSecondNull(){  
        String value = "bar";  
        String returned = firstNonNull(value,null);  
        assertThat(returned,is(value));  
    }  
  
    @Test(expected = NullPointerException.class)  
    public void testBothNull(){  
        //Never do this  
        firstNonNull(null,null);  
    }  
}  
```
## Preconditions

Preconditions是guava提供的用于进行代码校验的工具类，其中提供了许多重要的静态校验方法，用来简化我们工作或开发中对代码的校验或预 处理，能够确保代码符合我们的期望，并且能够在不符合校验条件的地方，准确的为我们显示出问题所在，接下来，我们就来学习使用Preconditions 进行代码校验。

我们可以轻松的写出我们自己的先决条件，如下：

```java
public static Object checkNotNull(Object object,String message){
        if(object == null){
            throw  new IllegalArgumentException(message);
        }//if
        return object;
    }
```

但是使用先决条件（静态导入）来修改上述检查参数是否为null更为简洁：

```java
checkNotNull(object,"not be null");
```

Guava为我们提供了更好的封装，使用起来更加简洁，出错率更低。

下面我们局一个简单的例子：

```java
package com.qunar.test;
import static com.google.common.base.Preconditions.*;
/**
 * Created by xiaosi on 16-3-6.
 */
public class PreconditionExample {
    private String str;
    private int[] values = new int[5];
    private int currentIndex;
    public PreconditionExample(String str){
        this.str = checkNotNull(str,"str cant not be null");
    }
    public void updateCurrentIndexValue(int index,int value){
        this.currentIndex = checkElementIndex(index,values.length,"Index out of bounds for values");
        checkArgument(value <= 100,"value cant not be more than 100");
        values[index] = value;
    }
    public void doOperation(){
        checkState(validateObjectState(),"cant not perform operation");
    }
    private boolean validateObjectState(){
        return this.str.equalsIgnoreCase("open") && values[this.currentIndex] == 10;
    }
}
```

Guava进行了大量方法的重载，组成了Preconditions工具类，下面我们先简单的了解一下静态方法。

（1）用来校验表达式是否为真，一般用作方法中校验参数

```java
public static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }
```

例如上面例子中，检验参数是否小于等于100：

```java
public void updateCurrentIndexValue(int index,int value){
        this.currentIndex = checkElementIndex(index,values.length,"Index out of bounds for values");
        checkArgument(value <= 100,"value cant not be more than 100");
        values[index] = value;
    }
```

（2）校验表达式是否为真，不为真时显示指定的错误信息。

```java
public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(String.valueOf(errorMessage));
    }
  }
```

（3）校验表达式是否为真，不为真时为你指定的错误信息模板，并且可以使用可变长参数。

```java
public static void checkArgument(boolean expression,
      @Nullable String errorMessageTemplate,
      @Nullable Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
    }
  }
```

这个方法调用了format方法，根据异常信息模板生成异常信息。

（4）检查对象的一些状态，不包括方法参数 （不是很理解）

```java
public static void checkState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }
```

例如上面例子：

```java
public void doOperation(){
        checkState(validateObjectState(),"cant not perform operation");
    }
    private boolean validateObjectState(){
        return this.str.equalsIgnoreCase("open") && values[this.currentIndex] == 10;
    }
```

（5）校验对象是否为空

```java
public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }
```

（6）checkElementIndex( int index, int size, @Nullable String desc)

校验元素的索引值是否有效，index大于等于0小于size，在无效时显示给定的错误描述信息。

```java
public static int checkElementIndex(
      int index, int size, @Nullable String desc) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(badElementIndex(index, size, desc));
    }
    return index;
  }
```

具体异常信息生成函数：

```java
private static String badElementIndex(int index, int size, String desc) {
    if (index < 0) {
      return format("%s (%s) must not be negative", desc, index);
    } else if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    } else { // index >= size
      return format("%s (%s) must be less than size (%s)", desc, index, size);
    }
  }
```

（7）checkPositionIndex

检验index作为位置值对某个列表、字符串或数组是否有效。index>=0 && index<=size

```java
public static int checkPositionIndex(int index, int size) {
    return checkPositionIndex(index, size, "index");
  }
```

重载函数 提供异常描述信息

```java
public static int checkPositionIndex(int index, int size, @Nullable String desc) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(badPositionIndex(index, size, desc));
    }
    return index;
  }
```

重载函数 检验[start, end]表示的位置范围对某个列表、字符串或数组是否有效

```java
public static void checkPositionIndexes(int start, int end, int size) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (start < 0 || end < start || end > size) {
      throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
    }
  }
```

检验函数 检验index下标是否有效

```java
/**
     * 下标异常信息
     * @param index 当前下标
     * @param size 下标长度
     * @param desc 描述
     * @return 合成下标异常信息
     */
    private static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return format("%s (%s) must not be negative", desc, index);
        }//if
        else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        }//else
        else { // index > size
            return format("%s (%s) must not be greater than size (%s)", desc, index, size);
        }//else
    }
```

重载函数 检验[start, end]表示的位置范围是否有效

```java
private static String badPositionIndexes(int start, int end, int size) {
    if (start < 0 || start > size) {
      return badPositionIndex(start, size, "start index");
    }
    if (end < 0 || end > size) {
      return badPositionIndex(end, size, "end index");
    }
    // end < start
    return format("end index (%s) must not be less than start index (%s)", end, start);
  }
```

（8）format

格式化字符串，将template中的每一个"%s"占位符用args参数替换。第一个%s使用args[0]替换，以此类推。

举例：

```java
template：%s (%s) must not be greater than size (%s)

args："array index"，8， 5

result：array index (8) must not be greater than size (5)
```

源码：

```java
public static String format(String template, Object... args) {
        template = String.valueOf(template); // null -> "null"
        // StringBuilder builder = new StringBuilder() ?
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        // 用参数替换"%s"占位符
        while (i < args.length) {
            // 寻找"%s"下标位置
            int placeholderStart = template.indexOf("%s", templateStart);
            // 未找到
            if (placeholderStart == -1) {
                break;
            }//if
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
            // 跳过"%s"占位符
            templateStart = placeholderStart + 2;
        }//while
        builder.append(template.substring(templateStart));
        // 多余参数 添加在方括号内 [args1,args2,...]
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }//while
            builder.append(']');
        }//if
        return builder.toString();
    }
```

我们之所以选择Guava的Preconditions作为首选：

在静态导入后，Guava方法非常清楚明了。checkNotNull清楚地描述做了什么，会抛出什么异常；

checkNotNull直接返回检查的参数，让你可以在构造函数中保持字段的单行赋值风格：this.field = checkNotNull(field)

简单的、参数可变的printf风格异常信息。鉴于这个优点，在JDK7已经引入Objects.requireNonNull的情况下，我们仍然建议你使用checkNotNull。

## Ordering

Ordering类提供了一些链式的排序方法，相比JDK自带的排序方法更加简练、方便。

Ordering中有3种静态方法用于创建Ordering排序器：

![img](http://img.blog.csdn.net/20160601211455806)

根据上面的方法创建Ordering后，可以继续调用如下方法创建组合功能的排序器：

![img](http://img.blog.csdn.net/20160601211517947)

创建完Ordering排序器后，即可使用它对集合或元素进行操作

![img](http://img.blog.csdn.net/20160601211536276)

下面介绍常用方法的使用。

以下示例代码中使用到的People类包含name和age两个属性。

1.natural方法

该方法使用自然排序规则生成排序器，如从小到大、日期先后顺序。使用这个方法之前先介绍一下onResultOf 方法，这个方法接收一个Function函数，该函数的返回值可以用于natural方法排序的依据，即根据这个返回值来进行自然排序，示例代码如下：

```java
@Test  
public void testNatural() {  
    List<People> peopleList = new ArrayList<People>() {{  
        add(new People("A", 33));  
        add(new People("B", 11));  
        add(new People("C", 18));  
    }};  
   
    Ordering<People> ordering = Ordering.natural().onResultOf(new Function<People, Comparable>() {  
        @Override  
        public Comparable apply(People people) {  
            return people.getAge();  
        }  
    });  
   
    for (People p : ordering.sortedCopy(peopleList)) {  
        System.out.println(MoreObjects.toStringHelper(p)  
                        .add("name", p.getName())  
                        .add("age", p.getAge())  
        );  
    }  
}  
```

sortedCopy方法会使用创建的排序器排序并生成一个新的List。对于Ordering.natural().onResultOf方法，阅读顺序是从后往前，即根据onResultOf 方法的返回值按照自然规则创建一个Ordering，然后调用sortedCopy方法排序并生成新List。输出结果如下：

```java
People{name=B, age=11}  
People{name=C, age=18}  
People{name=A, age=33}  
```

2.reverse方法

这个方法使用反向的排序规则来排序，即若使用natural规则创建Ordering后，再接着调用reverse方法，则按照自然规则的反向，从大到小的规则排序，示例代码如下：

```java


1. @Test  
2. public void testReverse() {  
3.    List<People> peopleList = new ArrayList<People>() {{  
4.        add(new People("A", 33));  
5.        add(new People("B", 11));  
6.        add(new People("C", 18));  
7.    }};  
8.    
9.    Ordering<People> ordering = Ordering.natural().reverse().onResultOf(new Function<People, Comparable>() {  
10.        @Override  
11.        public Comparable apply(People people) {  
12.            return people.getAge();  
13.        }  
14.    });  
15.    
16.    for (People p : ordering.sortedCopy(peopleList)) {  
17.        System.out.println(MoreObjects.toStringHelper(p)  
18.                        .add("name", p.getName())  
19.                        .add("age", p.getAge())  
20.        );  
21.    }  
22. }  
```

```java

1. People{name=A, age=33}  
2. People{name=C, age=18}  
3. People{name=B, age=11}  
```
 3.usingToString方法

该方法创建Ordering，并根据排序依据值的toString方法值来使用natural规则排序，示例代码如下：


```java

1. @Test  
2. public void testUsingToString() {  
3.    List<People> peopleList = new ArrayList<People>() {{  
4.        add(new People("A", 33));  
5.        add(new People("B", 11));  
6.        add(new People("C", 18));  
7.    }};  
8.    
9.    Ordering<People> ordering = Ordering.usingToString().onResultOf(new Function<People, Comparable>() {  
10.        @Override  
11.        public Comparable apply(People people) {  
12.            return people.getName();  
13.        }  
14.    });  
15.    
16.    for (People p : ordering.sortedCopy(peopleList)) {  
17.        System.out.println(MoreObjects.toStringHelper(p)  
18.                        .add("name", p.getName())  
19.                        .add("age", p.getAge())  
20.        );  
21.    }  
22. }  
```

```java

1. People{name=A, age=33}  
2. People{name=B, age=11}  
3. People{name=C, age=18}  

```
同时使用usingToString和reverse方法示例代码如下：


```java

1. @Test  
2. public void testUsingToStringAndReverse() {  
3.    List<People> peopleList = new ArrayList<People>() {{  
4.        add(new People("A", 33));  
5.        add(new People("B", 11));  
6.        add(new People("C", 18));  
7.    }};  
8.    
9.    Ordering<People> ordering = Ordering.usingToString().reverse().onResultOf(new Function<People, Comparable>() {  
10.        @Override  
11.        public Comparable apply(People people) {  
12.            return people.getName();  
13.        }  
14.    });  
15.    
16.    for (People p : ordering.sortedCopy(peopleList)) {  
17.        System.out.println(MoreObjects.toStringHelper(p)  
18.                        .add("name", p.getName())  
19.                        .add("age", p.getAge())  
20.        );  
21.    }  
22. }  
```

```java

1. People{name=C, age=18}  
2. People{name=B, age=11}  
3. People{name=A, age=33}  

```
4.from方法

该方法接收一个自定义的Comparator比较器来创建Ordering，根据Comparator中的自定义规则排序，示例代码如下：

```java

1. @Test  
2. public void testFrom() {  
3.    List<People> peopleList = new ArrayList<People>() {{  
4.        add(new People("A", 33));  
5.        add(new People("B", 11));  
6.        add(new People("C", 18));  
7.    }};  
8.    
9.    Ordering<People> ordering = Ordering.from(new Comparator<People>() {  
10.        @Override  
11.        public int compare(People o1, People o2) {  
12.            return o1.getAge() - o2.getAge();  
13.        }  
14.    });  
15.    
16.    for (People p : ordering.sortedCopy(peopleList)) {  
17.        System.out.println(MoreObjects.toStringHelper(p)  
18.                        .add("name", p.getName())  
19.                        .add("age", p.getAge())  
20.        );  
21.    }  
22. }  

```
```java


1. People{name=B, age=11}  
2. People{name=C, age=18}  
3. People{name=A, age=33}  

```
同时使用from和reverse方法，示例代码如下：

```java


1. @Test  
2. public void testFromAndReverse() {  
3.     List<People> peopleList = new ArrayList<People>() {{  
4.         add(new People("A", 33));  
5.         add(new People("B", 11));  
6.         add(new People("C", 18));  
7.     }};  
8.    
9.     Ordering<People> ordering = Ordering.from(new Comparator<People>() {  
10.         @Override  
11.         public int compare(People o1, People o2) {  
12.             return o1.getAge() - o2.getAge();  
13.         }  
14.     }).reverse();  
15.    
16.     for (People p : ordering.sortedCopy(peopleList)) {  
17.         System.out.println(MoreObjects.toStringHelper(p)  
18.                         .add("name", p.getName())  
19.                         .add("age", p.getAge())  
20.         );  
21.     }  
22. }  
```
```java
1. People{name=A, age=33}  
2. People{name=C, age=18}  
3. People{name=B, age=11}  
```

## Optional优雅的使用null

在我们学习和使用Guava的Optional之前，我们需要来了解一下Java中null。因为，只有我们深入的了解了null的相关知识，我们才能更加深入体会领悟到Guava的Optional设计和使用上的优雅和简单。

------

 　 **null代表不确定的对象：**

　　Java中，null是一个关键字，用来标识一个不确定的对象。因此可以将null赋给引用类型变量，但不可以将null赋给基本类型变量。

　　Java中，变量的使用都遵循一个原则：先定义，并且初始化后，才可以使用。例如如下代码中，我们不能定义int age后，不给age指定值，就去打印age的值。这条对对于引用类型变量也是适用的（String name也同样适用），在编译的时候就会提示为初始化。



```java
public class NullTest {
    public static void testNull(){
        int age;
        System.out.println("user age:"+age);
        
        long money;
        money=10L;
        System.out.println("user money"+money);
        
        String name;
        System.out.println("user name:"+name);
    }
}
```



　　在Java中，Java默认给变量赋值：在定义变量的时候，如果定义后没有给变量赋值，则Java在运行时会自动给变量赋值。赋值原则是整数类型int、byte、short、long的自动赋值为0，带小数点的float、double自动赋值为0.0，boolean的自动赋值为false，其他各供引用类型变量自动赋值为null。上面代码会变为如下可运行代码：



```java
public class NullTest {
    public static void testNull(){
        int age = 0;
        System.out.println("user Age:"+age);
        
        long money;
        money=10L;
        System.out.println("user money"+money);
        
        String name = null;
        System.out.println("user name:"+name);
    }
}
```



　**null本身不是对象，也不是Objcet的实例：**

　　null只是一个关键字，用来标识一个不确定的对象，他既不是对象，也不是Objcet对象的实例。下面我们通过代码确定一下null是不是Object对象实例：



```java
public class NullTest {
    public static void main(String[] args) {
        testNullObject();
    }
    
    public static void testNullObject() {
        if (null instanceof java.lang.Object) {
            System.out.println("null属于java.lang.Object类型");
        } else {
            System.out.println("null不属于java.lang.Object类型");
        }
    }
}
```



　　运行上面代码，输出：**null不属于java.lang.Object类型，可见，null对象不是Object对象的实例**。

------

 　　**null对象的使用：**

　　1.**常见使用场景：**

　　有时候，我们定义一个引用类型变量，在刚开始的时候，无法给出一个确定的值，但是不指定值，程序可能会在try语句块中初始化值。这时候，我们下面使用变量的时候就会报错。这时候，可以先给变量指定一个null值，问题就解决了。例如：  



```java
 Connection conn = null;
 try {
　　conn = DriverManager.getConnection("url", "user", "password");
 } catch (SQLException e) {
　　 e.printStackTrace();
 }
 String catalog = conn.getCatalog();
```



　　如果刚开始的时候不指定conn = null，则最后一句就会报错。

　　**2.容器类型与null：**
​     List：允许重复元素，可以加入任意多个null。
​     Set：不允许重复元素，最多可以加入一个null。
​     Map：Map的key最多可以加入一个null，value字段没有限制。
​     数组：基本类型数组，定义后，如果不给定初始值，则java运行时会自动给定值。引用类型数组，不给定初始值，则所有的元素值为null。
​    ** 3.null的其他作用**
​     1>、判断一个引用类型数据是否null。 用==来判断。
​     2>、释放内存，让一个非null的引用类型变量指向null。这样这个对象就不再被任何对象应用了。等待JVM垃圾回收机制去回收。

　　**4.null的使用建议：**

　　1>. 在Set或者Map中使用null作为键值指向的value，千万别这么用。很显然，在Set和Map的查询操作中，将null作为特殊的例子可以使查询结果更浅显易懂。
　　2>. 在Map中包含value是null值的键值对，你应该把这种键值对移出map，使用一个独立的Set来包含所有null或者非null的键。很容易混淆的是，一个Map是不是包含value是　null的key，还是说这个Map中没有这样的键值对。最好的办法就是把这类key值分立开来，并且好好想想到底一个value是null的键值对对于你的程序来说到底意味着什么。
　　3>. 在列表中使用null，并且这个列表的数据是稀疏的，或许你最好应该使用一个Map<Integer,E>字典来代替这个列表。因为字典更高效，并且也许更加精确的符合你潜意识里对程序的需求。
　　4>. 想象一下如果有一种自然的“空对象”可以使用，比方说对于枚举类型，添加一个枚举常数实例，这个实例用来表示你想用null值所表示的情形。比如：Java.math.RoundingMode有一个常数实例UNNECESSARY来表示“不需要四舍五入”，任何精度计算的方法若传以RoundingMode.UNNECESSARY为参数来计算，必然抛出一个异常来表示不需要舍取精度。

　　**5.问题和困惑:**

　　首先，对于null的随意使用会一系列难以预料的问题。通过对大量代码的研究和分析，我们发现大概95%以上的集合类默认并不接受null值，如果有null值将被放入集合中，代码会立刻中断并报错而不是默认存储null值，对于开发来说，这样能够更加容易的定位程序出错的地方。
　　另外，null值是一种令人不满的模糊含义。有的时候会产生二义性，这时候我们就很难搞清楚具体的意思，如果程序返回一个null值，其代表的含义到底是什么，例如：Map.get(key)若返回value值为null，其代表的含义可能是该键指向的value值是null，亦或者该键在map中并不存在。null值可以表示失败，可以表示成功，几乎可以表示任何情况。用其它一些值(而不是null值)可以让你的代码表述的含义更清晰。
　　反过来说，使用null值在有些情况下是一种正确的选择，因为从内存消耗和效率方面考虑，使用null更加廉价，而且在对象数组中出现null也是不可避免的。但是在程序代码中，比方说在函数库中，null值的使用会变成导致误解的元凶，也会导致一些莫名的，模糊的，很难修正的问题。就像上述map的例子，字典返回null可以代表的是该键指向的值存在且为空，或者也可以代表字典中没有这个键。关键在于，null值不能指明到底null代表了什么含义。

------

　　**Guava的Optional：**

​	大多数情况下程序员使用null是为了表示某种不存在的意思，也许应该有一个value，但是这个value是空或者这个value找不到。比方说，在用不存在的key值从map中取　　value，Map.get返回null表示没有该map中不包含这个key。　

　　若T类型数据可以为null，Optional<T>是用来以非空值替代T数据类型的一种方法。一个Optional对象可以包含一个非空的T引用（这种情况下我们称之为“存在的”）或者不包含任何东西（这种情况下我们称之为“空缺的”）。但Optional从来不会包含对null值的引用。



```java
import com.google.common.base.Optional;

public class OptionalTest {
    
    public void testOptional() throws Exception { 
        Optional<Integer> possible=Optional.of(6);
        if(possible.isPresent()){
            System.out.println("possible isPresent:"+possible.isPresent());
            System.out.println("possible value:"+possible.get());
        }
    } 
}
```



　　由于这些原因，Guava库设计了Optional来解决null的问题。许多Guava的工具被设计成如果有null值存在即刻报错而不是只要上下文接受处理null值就默认使用null值继续运行。而且，Guava提供了Optional等一些工具让你在不得不使用null值的时候，可以更加简便的使用null并帮助你避免直接使用null。
　　Optional<T>的最常用价值在于，例如，假设一个方法返回某一个数据类型，调用这个方法的代码来根据这个方法的返回值来做下一步的动作，若该方法可以返回一个null值表示成功，或者表示失败，在这里看来都是意义含糊的，所以使用Optional<T>作为返回值，则后续代码可以通过isPresent()来判断是否返回了期望的值（原本期望返回null或者返回不为null，其意义不清晰），并且可以使用get()来获得实际的返回值。

------

　**Optional方法说明和使用实例：**

　　**1.常用静态方法：**

　　Optional.of(T)：获得一个Optional对象，其内部包含了一个非null的T数据类型实例，若T=null，则立刻报错。
　　Optional.absent()：获得一个Optional对象，其内部包含了空值
　　Optional.fromNullable(T)：将一个T的实例转换为Optional对象，T的实例可以不为空，也可以为空[Optional.fromNullable(null)，和Optional.absent()等价。

　　使用实例如下：



```java
import com.google.common.base.Optional;


public class OptionalTest {
    
    @Test
    public void testOptional() throws Exception { 
        Optional<Integer> possible=Optional.of(6);
        Optional<Integer> absentOpt=Optional.absent();
        Optional<Integer> NullableOpt=Optional.fromNullable(null);
        Optional<Integer> NoNullableOpt=Optional.fromNullable(10);
        if(possible.isPresent()){
            System.out.println("possible isPresent:"+possible.isPresent());
            System.out.println("possible value:"+possible.get());
        }
        if(absentOpt.isPresent()){
            System.out.println("absentOpt isPresent:"+absentOpt.isPresent()); ;
        }
        if(NullableOpt.isPresent()){
            System.out.println("fromNullableOpt isPresent:"+NullableOpt.isPresent()); ;
        }
        if(NoNullableOpt.isPresent()){
            System.out.println("NoNullableOpt isPresent:"+NoNullableOpt.isPresent()); ;
        }
    } 
}
```



　　**2.实例方法：**

　　1>. boolean isPresent()：如果Optional包含的T实例不为null，则返回true；若T实例为null，返回false
　　2>. T get()：返回Optional包含的T实例，该T实例必须不为空；否则，对包含null的Optional实例调用get()会抛出一个IllegalStateException异常
　　3>. T or(T)：若Optional实例中包含了传入的T的相同实例，返回Optional包含的该T实例，否则返回输入的T实例作为默认值
　　4>. T orNull()：返回Optional实例中包含的非空T实例，如果Optional中包含的是空值，返回null，逆操作是fromNullable()
　　5>. Set<T> asSet()：返回一个不可修改的Set，该Set中包含Optional实例中包含的所有非空存在的T实例，且在该Set中，每个T实例都是单态，如果Optional中没有非空存在的T实例，返回的将是一个空的不可修改的Set。

使用实例如下：

```java
import java.util.Set;
import com.google.common.base.Optional;

public class OptionalTest {
    
    public void testMethodReturn() {
        Optional<Long> value = method();
        if(value.isPresent()==true){
            System.out.println("获得返回值: " + value.get());     
        }else{
                
            System.out.println("获得返回值: " + value.or(-12L));    
        }
        
        System.out.println("获得返回值 orNull: " + value.orNull());
        
        Optional<Long> valueNoNull = methodNoNull();
        if(valueNoNull.isPresent()==true){
            Set<Long> set=valueNoNull.asSet();
            System.out.println("获得返回值 set 的 size : " + set.size());    
            System.out.println("获得返回值: " + valueNoNull.get());     
        }else{
            System.out.println("获得返回值: " + valueNoNull.or(-12L));    
        }
        
        System.out.println("获得返回值 orNull: " + valueNoNull.orNull());
    }

    private Optional<Long> method() {
        return Optional.fromNullable(null);
    }
    private Optional<Long> methodNoNull() {
        return Optional.fromNullable(15L);
    }
    
}
```

　　输出结果：

```
获得返回值: -12
获得返回值 orNull: null
获得返回值 set 的 size : 1
获得返回值: 15
获得返回值 orNull: 15
```

**Optional**除了给null值命名所带来的代码可阅读性的提高，最大的好处莫过于Optional是傻瓜式的。Optional对象的使用强迫你去积极的思考这样一种情况，如果你想让你的程序返回null值，这null值代表的含义是什么，因为你想要取得返回值，必然从Optional对象内部去获得，所以你必然会这么去思考。但是只是简单的使用一个Null值会很轻易的让人忘记去思索代码所要表达的含义到底是什么，尽管FindBugs有些帮助，但是我们还是认为它并没有尽可能的解决好帮助程序员去思索null值代表的含义这个问题。
　　这种思考会在你返回某些存在的值或者不存在的值的时候显得特别相关。和其他人一样，你绝对很可能会忘记别人写的方法method(a,b)可能会返回一个null值，就好像当你去写method(a,b)的实现时，你也很可能忘记输入参数a也可以是null。如果返回的是Optional对象，对于调用者来说，就可以忘却怎么去度量null代表的是什么含义，因为他们始终要从optional对象中去获得真正的返回值。

# Guava cache

　缓存，在我们日常开发中是必不可少的一种解决性能问题的方法。简单的说，cache 就是为了提升系统性能而开辟的一块内存空间。

　　缓存的主要作用是暂时在内存中保存业务系统的数据处理结果，并且等待下次访问使用。在日常开发的很多场合，由于受限于硬盘IO的性能或者我们自身业务系统的数据处理和获取可能非常费时，当我们发现我们的系统这个数据请求量很大的时候，频繁的IO和频繁的逻辑处理会导致硬盘和CPU资源的瓶颈出现。缓存的作用就是将这些来自不易的数据保存在内存中，当有其他线程或者客户端需要查询相同的数据资源时，直接从缓存的内存块中返回数据，这样不但可以提高系统的响应时间，同时也可以节省对这些数据的处理流程的资源消耗，整体上来说，系统性能会有大大的提升。

　　缓存在很多系统和架构中都用广泛的应用,例如：

　　1.CPU缓存
　　2.操作系统缓存
　　3.本地缓存
　　4.分布式缓存
　　5.HTTP缓存
　　6.数据库缓存
　　等等，可以说在计算机和网络领域，缓存无处不在。可以这么说，只要有硬件性能不对等，涉及到网络传输的地方都会有缓存的身影。

　　Guava Cache是一个全内存的本地缓存实现，它提供了线程安全的实现机制。整体上来说Guava cache 是本地缓存的不二之选，简单易用，性能好。

　　**Guava Cache有两种创建方式：**

  　　1. cacheLoader
  　　2. callable callback

　　通过这两种方法创建的cache，和通常用map来缓存的做法比，不同在于，这两种方法都实现了一种逻辑——从缓存中取key X的值，如果该值已经缓存过了，则返回缓存中的值，如果没有缓存过，可以通过某个方法来获取这个值。但不同的在于cacheloader的定义比较宽泛，是针对整个cache定义的，可以认为是统一的根据key值load value的方法。而callable的方式较为灵活，允许你在get的时候指定。

　　**cacheLoader方式实现实例：**



```java
    @Test
    public void TestLoadingCache() throws Exception{
        LoadingCache<String,String> cahceBuilder=CacheBuilder
        .newBuilder()
        .build(new CacheLoader<String, String>(){
            @Override
            public String load(String key) throws Exception {        
                String strProValue="hello "+key+"!";                
                return strProValue;
            }
            
        });        
        
        System.out.println("jerry value:"+cahceBuilder.apply("jerry"));
        System.out.println("jerry value:"+cahceBuilder.get("jerry"));
        System.out.println("peida value:"+cahceBuilder.get("peida"));
        System.out.println("peida value:"+cahceBuilder.apply("peida"));
        System.out.println("lisa value:"+cahceBuilder.apply("lisa"));
        cahceBuilder.put("harry", "ssdded");
        System.out.println("harry value:"+cahceBuilder.get("harry"));
    }
```



　　输出：



```java
jerry value:hello jerry!
jerry value:hello jerry!
peida value:hello peida!
peida value:hello peida!
lisa value:hello lisa!
harry value:ssdded
```



　　**callable callback的实现：**



```
    @Test
    public void testcallableCache()throws Exception{
        Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(1000).build();  
        String resultVal = cache.get("jerry", new Callable<String>() {  
            public String call() {  
                String strProValue="hello "+"jerry"+"!";                
                return strProValue;
            }  
        });  
        System.out.println("jerry value : " + resultVal);
        
        resultVal = cache.get("peida", new Callable<String>() {  
            public String call() {  
                String strProValue="hello "+"peida"+"!";                
                return strProValue;
            }  
        });  
        System.out.println("peida value : " + resultVal);  
    }

　　输出：
　　jerry value : hello jerry!
　　peida value : hello peida!
```



 　　**cache的参数说明：**

　　回收的参数：
  　　1. 大小的设置：CacheBuilder.maximumSize(long)  CacheBuilder.weigher(Weigher)  CacheBuilder.maxumumWeigher(long)
  　　2. 时间：expireAfterAccess(long, TimeUnit) expireAfterWrite(long, TimeUnit)
  　　3. 引用：CacheBuilder.weakKeys() CacheBuilder.weakValues()  CacheBuilder.softValues()
  　　4. 明确的删除：invalidate(key)  invalidateAll(keys)  invalidateAll()
  　　5. 删除监听器：CacheBuilder.removalListener(RemovalListener)

　　refresh机制：
  　　1. LoadingCache.refresh(K)  在生成新的value的时候，旧的value依然会被使用。
  　　2. CacheLoader.reload(K, V) 生成新的value过程中允许使用旧的value
  　　3. CacheBuilder.refreshAfterWrite(long, TimeUnit) 自动刷新cache

 　　基于泛型的实现：



```
    /**
     * 不需要延迟处理(泛型的方式封装)
     * @return
     */
    public  <K , V> LoadingCache<K , V> cached(CacheLoader<K , V> cacheLoader) {
          LoadingCache<K , V> cache = CacheBuilder
          .newBuilder()
          .maximumSize(2)
          .weakKeys()
          .softValues()
          .refreshAfterWrite(120, TimeUnit.SECONDS)
          .expireAfterWrite(10, TimeUnit.MINUTES)        
          .removalListener(new RemovalListener<K, V>(){
            @Override
            public void onRemoval(RemovalNotification<K, V> rn) {
                System.out.println(rn.getKey()+"被移除");  
                
            }})
          .build(cacheLoader);
          return cache;
    }
    
    /**
     * 通过key获取value
     * 调用方式 commonCache.get(key) ; return String
     * @param key
     * @return
     * @throws Exception
     */
  
    public  LoadingCache<String , String> commonCache(final String key) throws Exception{
        LoadingCache<String , String> commonCache= cached(new CacheLoader<String , String>(){
                @Override
                public String load(String key) throws Exception {
                    return "hello "+key+"!";    
                }
          });
        return commonCache;
    }
    
    @Test
    public void testCache() throws Exception{
        LoadingCache<String , String> commonCache=commonCache("peida");
        System.out.println("peida:"+commonCache.get("peida"));
        commonCache.apply("harry");
        System.out.println("harry:"+commonCache.get("harry"));
        commonCache.apply("lisa");
        System.out.println("lisa:"+commonCache.get("lisa"));
    }
```



　　输出：

```
peida:hello peida!
harry:hello harry!
peida被移除
lisa:hello lisa!
```

　　基于泛型的Callable Cache实现：



```
    private static Cache<String, String> cacheFormCallable = null; 

    
    /**
     * 对需要延迟处理的可以采用这个机制；(泛型的方式封装)
     * @param <K>
     * @param <V>
     * @param key
     * @param callable
     * @return V
     * @throws Exception
     */
    public static <K,V> Cache<K , V> callableCached() throws Exception {
          Cache<K, V> cache = CacheBuilder
          .newBuilder()
          .maximumSize(10000)
          .expireAfterWrite(10, TimeUnit.MINUTES)
          .build();
          return cache;
    }

    
    private String getCallableCache(final String userName) {
           try {
             //Callable只有在缓存值不存在时，才会调用
             return cacheFormCallable.get(userName, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    System.out.println(userName+" from db");
                    return "hello "+userName+"!";
               }
              });
           } catch (ExecutionException e) {
              e.printStackTrace();
              return null;
            } 
    }
    
    @Test
    public void testCallableCache() throws Exception{
         final String u1name = "peida";
         final String u2name = "jerry"; 
         final String u3name = "lisa"; 
         cacheFormCallable=callableCached();
         System.out.println("peida:"+getCallableCache(u1name));
         System.out.println("jerry:"+getCallableCache(u2name));
         System.out.println("lisa:"+getCallableCache(u3name));
         System.out.println("peida:"+getCallableCache(u1name));
         
    }
```



　　输出：



```
peida from db
peida:hello peida!
jerry from db
jerry:hello jerry!
lisa from db
lisa:hello lisa!
peida:hello peida!
```



　　说明：Callable只有在缓存值不存在时，才会调用，比如第二次调用getCallableCache(u1name)直接返回缓存中的值

　　**guava Cache数据移除：**

　　guava做cache时候数据的移除方式，在guava中数据的移除分为被动移除和主动移除两种。
　　被动移除数据的方式，guava默认提供了三种方式：
　　1.基于大小的移除:看字面意思就知道就是按照缓存的大小来移除，如果即将到达指定的大小，那就会把不常用的键值对从cache中移除。
　　定义的方式一般为 CacheBuilder.maximumSize(long)，还有一种一种可以算权重的方法，个人认为实际使用中不太用到。就这个常用的来看有几个注意点，
　　　　其一，这个size指的是cache中的条目数，不是内存大小或是其他；
　　　　其二，并不是完全到了指定的size系统才开始移除不常用的数据的，而是接近这个size的时候系统就会开始做移除的动作；
　　　　其三，如果一个键值对已经从缓存中被移除了，你再次请求访问的时候，如果cachebuild是使用cacheloader方式的，那依然还是会从cacheloader中再取一次值，如果这样还没有，就会抛出异常
　　2.基于时间的移除：guava提供了两个基于时间移除的方法
　　　　expireAfterAccess(long, TimeUnit)  这个方法是根据某个键值对最后一次访问之后多少时间后移除
　　　　expireAfterWrite(long, TimeUnit)  这个方法是根据某个键值对被创建或值被替换后多少时间移除
　　3.基于引用的移除：
　　这种移除方式主要是基于java的垃圾回收机制，根据键或者值的引用关系决定移除
　　**主动移除数据方式，主动移除有三种方法：**
　　1.单独移除用 Cache.invalidate(key)
　　2.批量移除用 Cache.invalidateAll(keys)
　　3.移除所有用 Cache.invalidateAll()
　　如果需要在移除数据的时候有所动作还可以定义Removal Listener，但是有点需要注意的是默认Removal Listener中的行为是和移除动作同步执行的，如果需要改成异步形式，可以考虑使用RemovalListeners.asynchronous(RemovalListener, Executor)

