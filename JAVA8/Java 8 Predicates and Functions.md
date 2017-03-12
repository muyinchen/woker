# Java 8 Predicates and Functions



In this article we will cover Java 8 Predicate and Function interfaces.

## Introduction

Google Guava library users are already familiar with the concepts that we will cover in this article. **Predicate** and **Function** are a couple of useful Functional Interfaces introduced in Java 8 (more information about Functional Interfaces in the following article: [**Java 8 Lambda expressions example**](http://www.byteslounge.com/tutorials/java-8-lambda-expressions-example)).

Let us see which features each one of these interfaces provide.

## Predicate

Predicates represent single argument functions that return a boolean value:

Simple predicate

```java
Predicate<Integer> greaterThanTen = (i) -> i > 10;

// Will print true
greaterThanTen.test(14);

```

Predicates may also be chained together by the means of **and**, **or** and **negate**. Following next is a simple example but one may write complex evaluation rules by chaining predicates:

Predicate chaining

```java
Predicate<Integer> greaterThanTen = (i) -> i > 10;
Predicate<Integer> lowerThanTwenty = (i) -> i < 20;

// Will print true
greaterThanTen.and(lowerThanTwenty).test(15);

// Will print false
greaterThanTen.and(lowerThanTwenty).negate().test(15)

```

Predicates may also be passed into functions:

Passing predicates into functions

```java
// Will print "Number 10 was accepted!"
process(10, (i) -> i > 7);

void process(int number, Predicate<Integer> predicate) {
  if (predicate.test(number)) {
    System.out.println("Number " + number + " was accepted!");
  }
}

```

Another example:

Filtering list elements with a predicate

```java
List<User> users = new ArrayList<>();
users.add(new User("John", "admin"));
users.add(new User("Peter", "member"));
List<User> admins = process(users, (u) -> u.getRole().equals("admin"));

List<User> process(List<User> users, Predicate<User> predicate) {
  List<User> result = new ArrayList<>();
  for (User user : users) {
    if (predicate.test(user)) {
      result.add(user);
    }
  }
  return result;
}

```

## Function

Functions also represent a single argument function but they return a result of an arbitrary type:

Simple function

```java
Function<String, Integer> stringLength = (s) -> s.length();

// Will print 11
stringLength.apply("Hello world");

```

Functions may also be chained:

Function chaining

```java
Function<String, Integer> stringLength = (s) -> s.length();
Function<Integer, Boolean> greaterThanFive = (i) -> i > 5;

// Will print true
stringLength.andThen(greaterThanFive).apply("Hello world");

```

Another function chaining example:

Another function chaining example

```java
Function<String, Integer> stringLength = (s) -> s.length();
Function<Integer, Boolean> lowerThanTen = (i) -> i < 10;
Function<String, Boolean> function = stringLength.andThen(lowerThanTen);

// Will print false
function.apply("Hello world");
```

## Reference

[**Lambda Expressions (The Java(TM) Tutorials, Learning the Java Language, Classes and Objects)**](http://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html)