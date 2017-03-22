# Predicate和Consumer接口– Java 8中java.util.function包下的接口



早先我写了一篇《函数式接口》，探讨了部分Java 8中函数式接口的用法。我也提及了Predicate接口属于java.util.function包，在这篇文章中，我将展示如何应用Predicate接口和Consumer接口。

一起看一下Predicate的官方文档：

> Determines if the input object matches some criteria.

即判断输入的对象是否符合某个条件。

在Predicate接口中，有以下5个方法（你肯定好奇为何此接口属于函数式接口。如果你这么想，在使用接口前应该好好研读方法的注释）:

```java

//Returns a predicate which evaluates to true only if this predicate
//and the provided predicate both evaluate to true.
and(Predicate<? super T> p) 

//Returns a predicate which negates the result of this predicate.
negate() 

//Returns a predicate which evaluates to true if either
//this predicate or the provided predicate evaluates to true
or(Predicate<? super T> p) 

//Returns true if the input object matches some criteria
test(T t) 

//Returns a predicate that evaluates to true if both or neither
//of the component predicates evaluate to true
xor(Predicate<? super T> p)
```
除了test()方法是抽象方法以外，其他方法都是默认方法（译者注：在Java 8中，接口可以包含带有实现代码的方法，这些方法称为default方法）。可以使用匿名内部类提供test()方法的实现，也可以使用lambda表达式实现test()。

Consumer接口的文档声明如下：

> An operation which accepts a single input argument and returns no result. Unlike most other functional interfaces, Consumer is expected to operate via side-effects.

即接口表示一个接受单个输入参数并且没有返回值的操作。不像其他函数式接口，Consumer接口期望执行带有副作用的操作（译者注：Consumer的操作可能会更改输入参数的内部状态）。

Consumer接口中有2个方法，有且只有一个声明为accept(T t)的方法，接收一个输入参数并且没有返回值。为了详细说明Predicate和Consumer接口，我们来考虑一下学生的例子：Student类包含姓名，分数以及待付费用，每个学生可根据分数获得不同程度的费用折扣。

```java
class Student{

    String firstName;

    String lastName;

    Double grade;

    Double feeDiscount = 0.0;

    Double baseFee = 20000.0;

    public Student(String firstName, String lastName, Double grade) {

        this.firstName = firstName;

        this.lastName = lastName;

        this.grade = grade;
    }

    public void printFee(){

        Double newFee = baseFee - ((baseFee * feeDiscount) / 100);

        System.out.println("The fee after discount: " + newFee);

    }

}
```

我们分别声明一个接受Student对象的Predicate接口以及Consumer接口的实现类。如果你还不熟悉Function接口，那么你需要花几分钟阅读一下[这篇文章](http://blog.sanaulla.info/2013/03/27/function-interface-a-functional-interface-in-the-java-util-function-package-in-java-8/)。这个例子使用Predicate接口实现类的test()方法判断输入的Student对象是否拥有费用打折的资格，然后使用Consumer接口的实现类更新输入的Student对象的折扣。

```java
public class PreidcateConsumerDemo {

   public static Student updateStudentFee(Student student, Predicate<Student> predicate, Consumer<Student> consumer){

        //Use the predicate to decide when to update the discount.

        if ( predicate.test(student)){

            //Use the consumer to update the discount value.

            consumer.accept(student);
        }

        return student;

    }

}
```

Predicate和Consumer接口的test()和accept()方法都接受一个泛型参数。不同的是test()方法进行某些逻辑判断并返回一个boolean值，而accept()接受并改变某个对象的内部值。updateStudentFee方法的调用如下所示：

```java
public static void main(String[] args) {

    Student student1 = new Student("Ashok","Kumar", 9.5);

    student1 = updateStudentFee(student1,
                                //Lambda expression for Predicate interface
                                student -> student.grade > 8.5,
                                //Lambda expression for Consumer inerface
                                student -> student.feeDiscount = 30.0);

    student1.printFee();

    Student student2 = new Student("Rajat","Verma", 8.0);

    student2 = updateStudentFee(student2,
                                student -> student.grade >= 8,
                                student -> student.feeDiscount = 20.0);

    student2.printFee();

}
```

