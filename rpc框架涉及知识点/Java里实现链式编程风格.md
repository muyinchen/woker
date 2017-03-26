# Java里实现链式编程风格

#### 链式编程

所谓的链式编程，则是类似与StringBuffer的append方法的写法：

```java
StringBuffer buffer = new StringBuffer();
// 链式编程
buffer.append("aaa").append("bbb").append("ccc");
```

#### 如何实现

那么问题来了，怎么实现这种炫酷的连写的代码呢？ 
其实很简单，那就是在方法的最后写上`return this`; 
如果大家去看看`StringBuffer`的`append`的源代码，也可以发现，它里面也是这么写的，我们除了在拼接字符串的时候这么用，在什么时候也可以用链式编程呢？ 
我认为在创建实体`bean`对象的时候，可以这么写。 
如下：

```java
public class Dog {
    
    private int weight;
    private String color;
    private String dogTye;

   public Dog setWegith(int weight) {
       this.weight = weight;
       return this;
   }
   
   public Dog setColor(String color) {
        this.color = color;
        return this;
   }
  
   public Dog setDogType(String dogType) {
    this.dogType = dogType;
    return this;
   }
}
```

我们在创建这个实体的时候就可以这么写：

```java
Dog dog = new Dog();
// 常规赋值风格
dog.setWeght(20);
dog.setColor("金黄色");
dog.setDogType("金毛犬");
// 链式编程风格
dog.setWeght(20).setColor("金黄色").setDogType("金毛犬");
```

这样子看起来是不是很简洁呢？？？反正我觉得这么些挺不错的。