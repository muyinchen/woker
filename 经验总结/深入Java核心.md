# 深入Java核心

当JVM运行起来的时候就会给内存划分空间，那么这块空间称之为运行时数据区。

( `备注：` 当一个Java源程序编译成class字节码文件之后，字节码文件里存放的都是二进制的汇编命令，当程序运行的时候，JVM会将这个二进制的命令逐行解释，交给CPU去执行)

- 运行时数据区将划分为以下几块内容：

  1)栈

  - 每一个线程运行起来的时候就会对应一个栈（线程栈），栈当中存放的数据是被当前线程所独有的。而栈当中存放的是栈帧，当线程调用一个方法的时候，就会形成一个栈帧，并将这个栈帧进行压栈操作，当方法执行完之后就会将这个栈帧进行出栈操作。这个栈帧里面包括（局部变量、操作数栈、指向当前方法对应类的常量池引用、方法的返回地址等信息）。

    **备注：**


    ( `备注：` 由于局部变量都是存放在栈中，而每一个线程都对应自己的线程栈，因此局部变量是线程安全的，不会才产生资源共享的情况。)

![](http://img2.tuicool.com/3UBJzeM.png!web)

  2)本地方法栈

  - 本地方法栈的机制和栈的机制类似，区别在于，栈是运行Java所实现的方法，而本地方法栈是运行的本地方法(Native Method)。所谓的本地方法指的是在本地jvm中需要调用非Java语言所实现的方法，例如c语言。在JVM的规范中，其实没有强制性要求实现方一定要划分出本地方法栈的和具体的实现，这一部分可以根据实现方具体要求来实现。因此在HotSport虚拟机的实现中就将方法栈和本地方法栈二合为一。

    ![](http://img0.tuicool.com/QrIVbqu.png!web)

  3)程序计数器

  - 程序计数器也可以称之为PC寄存器。它主要用于存放当前程序下一条将要执行的指令地址。CPU会根据这个地址找到对应的指令来执行。通俗的讲就是指令缓存。这个寄存器是有JVM内部实现的，并不是物理概念上的寄存器，但是JVM在实现功能的逻辑上是相同的。

  4)堆

  - 堆内存中主要存放创建的对象以及数组。 堆内存是可以被多个线程所共享的一块区域,因此多个线程栈都可以去访问同一块堆的内存区域。堆里面的每一对象都存放了该实例的实例变量。

  - 当在方法中定义了一个局部变量，如果这个变量是基本数据类型，那么这个变量的值就直接存放在栈中，如果这个变量是引用数据类型，那么这个对象变量就存放在堆内存中，而栈中存放的是一个指向堆内存中这个对象的首地址。

    ( `备注：` Java中除了8个基本数据类型以外的所有类型都是引用数据类型)


  - 引用

    ![](http://img2.tuicool.com/UBneQnN.png!web)

  - 更改

    ![](http://img0.tuicool.com/JNfuMzZ.png!web)

  - 数组

    ![](http://img2.tuicool.com/I3YBZrE.png!web)

  - 循环

    ![](http://img2.tuicool.com/7vYfMnN.png!web)

> 实例变量和静态变量的区别：
>
>
>   - `实例变量：`实例变量是随着对象的创建而创建，而实例是存放在堆中，所以实例变量自然也就跟实例一并保存在堆内存。只要创建多少个实例，就会有多少份实例变量。当实例被回收的时候，实例变量也随之而销毁。
>
>   - `静态变量：`静态变量也叫类变量，它是在类加载的时候就已经初始化好，并存放在方法区，并且只有一份，所以它是被多个实例所共享的一个变量。
>
>

  5)方法区

  - 方法区在JVM中也是一个非常重要的一块内存区域，它和堆一样，是可以被多个线程所共享的一块区域。这个区域中主要存放了每一个加载的class文件信息。

    在一个class文件中主要包含 `魔数` (代码中出现但没有解释的数字常量或字符串)（用来确定是否是一个class文件）、常量池（常量池在下面会有完整说明）、访问标志（当前的class是类还是接口，是否是抽象类，

    是否是public修饰，是否使用了 `final` 修饰等描述信息…）、字段表集合信息（使用什么访问修饰符、是实例变量还是静态变量，是否用 `final` 修饰等描述信息…）、

    方法表集合信息（访问修饰符，是否静态方法，是否用final修饰，是否用了 `synchronized`修饰，是否是 `native` 方法…）等内容。当一个类加载器加载一个class文件的时候，

    会根据这个class文件的内容创建一个Class对象，而这个Class对象就包括了上述的这些内容。后续要创建这个类的所有实例，都是通过这个Class对象创建出来的。

    ![](http://img0.tuicool.com/r2qEN3A.png!web)

  6)常量池

  - 常量池也是方法区中的一部分，它存放的内容是class文件中最重要的资源，JVM为每一个class对象都维护一个常量池。它主要存储两种类型的常量。
    1. 字面常量
       - 字面常量通常就是在Java中定义的字面量值，如：int i =1,这个1就是字面量；String s = (“abc”)，这个abc就是字面量。或者使用final修饰的常量值等等。
    2. 符号引用
       - 符号引用主要包括类和接口的完整类名、属性字段的名称和描述符、方法名称和描述符等信息

  ![img](http://img2.tuicool.com/JN3AjaZ.jpg!web)

- 在Java当中，8个基本数据类型都有对应的包装类型，而大部分包装类型都实现了常量池的技术，除了 `Double` 和 `Float` 类。 
  ( `备注说明：` 在JDK8之后，方法区已经取消，方法区被一个叫MetaSpace，它和堆合并到一起管理)

- 内存运行时数据区 
  ![img](http://img1.tuicool.com/mi2YbuV.png!web)

- `扯了好多Java虚拟机的内容，也没讲多深，因为这里主要的目的是为了大家方便理解Java反射机制，下面正式进入正题`

## Class对象

当ClassLoader加载一个class文件到JVM的时候，会自动创建一个该类的Class对象，并且这个对象是唯一的，后续要创建这个类的任何实例，都会根据这个Class对象来创建。因此每当加载一个class文件的时候，都会创建一个与之对应的Class对象。

- 解析一个类的各个部分，形成一个对象。 
  ![img](http://img1.tuicool.com/iUj2uaF.png!web)

- 外存中的类，加载到内存中，会形成该对象的Class类，例如：String类，加载到内存中，就是StringClass对象。


  ​

  也就是说类是java.lang.Class类的实例对象，而Class是所有类的类

   

  ​

  对于普通的对象，一般都的创建方式：

  ```java
  String s = new String();
  ```


- 既然类都是Class的对象，那么能否像普通对象一样创建呢，当看源码时，是这样写的 ：

  ```java
  private Class(ClassLoader loader){
      classLoader = loader;
  }
  ```

- 源码里构造器是私有的，只有JVM可以创建Class的对象，虽然我们不能new一个Class对象，但是可以从已有的类得到一个Class对象，共有三种方式，如下：

  ```java
  // 类名.class 通过获取类的静态成员变量class得到(任何类都有一个隐含的静态成员变量class)
  Class<?> clazz = String.class;
  // 对象.getClass
  Class<?> clazz2 = new String().getClass();
  // Class.forName("全量限定名")
  Class<?> clazz3 = Class.forName("java.lang.String");
  ```

  - ( `注意：` 这三种方式都是利用反射获取的都是同一个Class对象，这也叫做String的类类型，也就是描述何为类，一个类都有哪些东西，所以可以通过类类型知道一个类的属性和方法，并可以调用一个类的属性和方法，这就是反射的基础。)

## 反射

反射是指 `在程序的运行期间动态的去操作某个Class对象里面的成员` （包括类信息、属性信息、方法信息等元素）。它可以让Java这种静态语言具备一定的动态性。目前大部分的开源框架实现都是基于反射的机制实现。

JVM → 类加载 → class文件 → 创建 → Class对象 → 构建类的实例 → instance(实例)；

重点在运行时动态的操作Class对象。

### 反射机制的利与弊

为何要用反射机制？直接new对象不ok了吗，这就涉及到了动态与静态的概念

- 静态编译：在编译时确定类型，绑定对象,即通过。
- 动态编译：运行时确定类型，绑定对象。动态编译最大限度发挥了java的灵活性，体现了多态的应用，有利于降低类之间的藕合。

#### 优点：

- 可以实现动态创建对象和编译。比如，一个软件，不可能第一个版本就把它设计的很完美，当这个程序编译成功，发布后，当发现某些功能需要更新时，我们不可能要用户把旧版的卸载，再重新安装新的版本。采用静态的话，需要把整个程序重新编译一次才可以实现功能的更新，而采用反射机制的话，它就可以不用卸载，只需要在运行时才动态的创建和编译，就可以实现该功能。

  一句话总结： `运行期类型的判断，动态类加载，动态代理就使用了反射`

#### 缺点：

1.对性能有影响。反射相当于一系列解释操作，通知JVM要做的事情。性能比直接的java代码执行相同的操作要慢很多。

2.由于反射允许代码执行一些在正常情况下不被允许的操作（比如访问私有的属性和方法），所以使用反射可能会导致意料之外的副作用，反射代码破坏了抽象性，因此当平台发生改变的时候，代码的行为就有可能也随着变化。

### 反射机制的相关操作

#### 创建实例

```java
// 在反射操作之前的第一步，就是要先获取Class对象
Class<?> clazz = Class.forName("org.demo.bean.People");
// 根据Class对象创建一个实例
clazz.newInstance();
```

#### 动态操作属性

- 通过Class对象可以动态的获取和操作类中的属性，属性在JDK中有一个类来进行封装，就是Field,Field提供了一些常用的API方法让我们去访问和操作类中的属性

  `getField()` // 获取所有公开的属性字段（包括继承父类的公有属性）

  `getDeclaredField()` // 获取本类所有（包括公有和私有，但是不包括父类的）的属性字段（注意：如果要访问和操作私有属性，必须调用setAccessible方法，打开访问开关）

  `getFields()` // 获取所有公有的属性（包括继承自父类的公有属性）

  `getDeclaredFields()` // 获取本类所有的属性（包括共有和私有的，但是不包括父类的）

  `set()` // 给属性赋值，需要传入两个参数，第一个参数是当前类的一个实例，第二个参数是具体要赋予的值

  `get()` // 获取属性的值，需要传入一个当前类的实例作为参数

  `getName()` // 获取属性的名称

  `getType()` // 获取属性的类型

  `isAnnotationPresent()` // 判断该属性上是否定义了指定的注解，需要传入一个注解的Class对象作为参数

  `getAnnotation()` // 获取当前属性上的注解对象，需要传入一个注解的Class对象作为参数

  ```java
  public static void main(String[] args)throwsException{
     
      // 在反射操作之前的第一步，就是要先获取Class对象
      Class<?> clazz = Class.forName("org.demo.bean.People");
      // 根据Class对象创建一个实例
      Object instance = clazz.newInstance();
      // 获取指定的属性
      Field f1 = clazz.getField("userName");
      // 获取属性的值,get方法需要传入一个当前类的实例
      Object value = f1.get(instance);
      System.out.println(value);
      
      // 通过反射给属性赋值
      // 第一个参数是当前类的实例，第二个参数是要赋予的值
      f1.set(instance, "godql");
      value = f1.get(instance);
      System.out.println(value);
      
      // 获取一个私有的属性
      // 如果需要访问和操作私有的成员，必须打开访问开关
      // 打开访问开关其实就是破坏封装
      Field f2 = clazz.getDeclaredField("age");
      // 强制打开访问权限
      f2.setAccessible(true);
      Object value2 = f2.get(instance);
      System.out.println(value2);
      f2.set(instance, 30);
      value2 = f2.get(instance);
      System.out.println(value2);
      
      // 获取属性的名称
      System.out.println(f1.getName());
      System.out.println(f2.getName());
      
      // 获取属性的类型
      System.out.println(f1.getType());
      System.out.println(f2.getType());
      
      // 获取所有公有的属性(包括继承自父类的公有属性)
      Field[] fs1 = clazz.getFields();
      // 获取本类所有的属性（包括共有和私有的，但是不包括父类的）
      Field[] fs2 = clazz.getDeclaredFields();
      
      // 判断当前属性上是否定义了注解
      System.out.println(f1.isAnnotationPresent(MyAnno.class));
      System.out.println(f2.isAnnotationPresent(MyAnno.class));
      
      // 获取属性上定义的注解
      MyAnno anno = f1.getAnnotation(MyAnno.class);
      // 获取注解上的属性值
      System.out.println(anno.name());
  }
  ```

#### 动态操作方法

- 对于Class中的方法，API也提供了相应的类来进行封装，就是Method

  `getMethod()` // 获取指定的公共的方法（包括继承自父类公共的），需要传递两个参数，第一个参数是方法名称，第二个参数是一个可变参数，传递的是方法参数的类型

  `getMethods()` // 获取所有的公共的方法（包括继承父类的公共方法）。

  `getDeclaredMethod()` // 获取本类中指定的方法（包括私有和共有的，不包括父类的），需要传递两个参数，第一个参数是方法名称，第二个参数是一个可变参数，传递的是方法参数的类型。如果是私有方法，同样需要先打开访问开关(setAccessible(true))。

  `getDeclaredMethods()` // 获取本地中所有的方法（包括私有和公共的，不包括父类）

  `getName()` // 获取方法名称

  `getReturnType()` // 获取方法的返回值类型

  `getParameterTypes()` // 获取方法中所有的参数类型

  `getParameterCount()` // 获取方法中参数的总个数

  `getParameters()` // (JDK1.8新特性)获取方法中所有的参数信息，每一个参数信息都是一个Parameter类的对象。可以通过这个对象获取各个参数的类型以及名称(注意：如果要获取参数名，在编译的时候需要加上一个parameters参数，如：javac -parameters Xxx.java。或者是在开发环境中设置相应的编译选项)。

  `invoke()` // 回调当前方法,需要传递两个参数，第一个是当前类的实例，第二个是一个可变参数，需要传入调用方法是所需的参数值。

  ```java
  public static void main(String[] args)throwsException{
     
      Class<?> clazz = Class.forName("org.demo.bean.People");
      Object instance = clazz.newInstance();
      // 获取指定的Method
      Method m1 = clazz.getMethod("say", String.class, int.class);
      // 获取方法名
      System.out.println(m1.getName());
      // 获取方法的返回值类型
      System.out.println(m1.getReturnType());
      // 获取方法的所有参数类型
      Class<?>[] paramsType = m1.getParameterTypes();
      for (Class<?> c : paramsType) {
          System.out.println(c);
      }
      // 获取参数名称（JDK1.8开始支持）
      Parameter[] params = m1.getParameters();
      for (Parameter p : params) {
          System.out.println("参数类型:"+p.getType());
          System.out.println("参数名称:"+p.getName());
      }
      // 通过当前的方法，获取定义这个方法的类
      Class<?> c = m1.getDeclaringClass();
      System.out.println(c.getName());

      // 方法回调，目的就是通过反射去调用一个方法
      m1.invoke(instance, "godql", 21);
  }
  ```

#### 动态操作构造方法

- Constructor是在反射API中用于封装构造方法的一个类，因此通过这个类可以获取构造方法的一些信息，以及通过这个对象来实例化一个类的实例。

  `getConstructor()` // 获取无参并且公共的构造方法

  `getDeclaredConstructor()` // 获取一个构造方法可以是私有的也可以是公共的，需要传入一个可变参数，就是构造方法的参数类型（注意：如果是私有的，必须先打开访问开关）

  `newInstance()` // 通过构造方法创建实例，也需要传入一个可变参数，传入的是具体的值

  `getConstructors()` // 获取所有公共的构造方法，返回的是一个Constructor数组

  `getDeclaredConstructors()` // 获取所有的构造方法(包括私有和共有的),同样返回的是一个数组

  `getParameters()` // 获取所有的参数对象，和Method一样

  `getParameterTypes()` // 获取所有的参数类型，同Method一样

  ```java
  public static void main(String[] args)throwsException{
      
      Class<?> clazz = People.class;
      // 获取无参的构造方法
      Constructor<?> c1 = clazz.getConstructor();
      // 获取构造方法的名称
      System.out.println(c1.getName());
      // 获取一个私有并且带参数的构造方法
      Constructor<?> c2 = clazz.getDeclaredConstructor(String.class);

      // 可以通过构造方法实例化一个对象
      //（注意：如果默认有一个无参并且是公共的构造方法，
      // 那么可以直接使用class.newInstance()方法创建实例，
      // 如果构造方法是私有的，或者是带参数的，就必须先获取
      // Constructor对象，在通过这个对象来创建类实例）

      // 1.适用于无参并且是公共的构造方法
      /*
        Object instance = clazz.newInstance();
        System.out.println(instance);
      */

      // 2.适用于带参数或是私有的构造方法
      // 由于构造方法也可以私有化，所以必须先打开访问开关
      c2.setAccessible(true);
      Object instance = c2.newInstance("godql");
      System.out.println(instance);
    
      // 获取所有public修饰的构造方法
      Constructor<?>[] cons = clazz.getConstructors();
      // 获取所有构造方法（包括私有的）
      Constructor<?>[] cons2 = clazz.getDeclaredConstructors();
      }
  ```

#### Class中的一些API

- Class对象本身提供了很多的API方法用于获取和操作Class对象。

  `getPackage()` // 获取当前类所在的包，使用Package对象进行封装，可以从中获取包的信息，例如：包名

  `getSimpleName()` // 获取当前类的简单类名（不包括包名）

  `getName()` // 获取当前类的完整类名(包括包名)

  `getSuperclass()` // 获取当前类的父类，返回的也是一个Class对象

  `getInterfaces()` // 获取当前类所实现的所有接口，返回的是一个Class数组

  `isAnnotationPresent()` // 判断当前类上是否定义了注解

  `getAnnotation()` // 获取类上定义的注解

### 通过反射了解集合泛型的本质

- Java中集合的泛型，是防止错误输入的，只在编译阶段有效，绕过编译到了运行期就无效了。

  ```java
  public static void main(String[] args){
      
      List list = new ArrayList(); 
      List<String> list1 = new ArrayList<>(); 
    
      list.add("godql");
      // list1.add(20); 错误的
      
      Class c1 = list.getClass();
      Class c2 = list1.getClass();
    
      System.out.println(c1 == c2); // 结果：true，说明类类型完全相同
      // 反射的操作都是编译之后的操作(运行时)
    
      /*
       * 以上说明编译之后集合的泛型是泛型擦除的
       * Java中集合的泛型，是防止错误输入的，只在编译阶段有效，绕过编译就无效了。
       * 验证: 通过方法的反射来操作，绕过编译 
       */
       try {
           // 通过动态操作方法的反射得到add方法
           Method m = c2.getMethod("add", Object.class);
           // 方法回调 给list1添加一个int型的，这是在运行时的操作，所以编译器编译时没有泛型检查，所以不会报错
           // 绕过编译操作
           m.invoke(list1, 20);
           // 验证是否有添加进list集合里
           System.out.println(list1.size()); 
           // 这时候不能使用foreach遍历，否则集合会认为集合里边全是String类型的值
           // 且有类型转换错误，因为这个集合里面有int类型、String类
           System.out.println(list1); 
       } catch (Exception e) {
           e.printStackTrace();
       }
  }
  ```