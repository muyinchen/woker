# js高级程序设计--阅读笔记

# 一 面向对象的程序设计

ECMAScript 中又两种属性:数据属性(包含一个数据值的位置叫做数据属性)和访问器属性(`getter()` 和 `setter()`就是访问器属性)

1. 数据属性而数据属性又有四个这样的特性:

   - `[Configurable]` 是否可配置,编辑,删除属性,默认true
   - `[Enumberable]`是否可以被枚举,即可被for-in,默认true
   - `[Writable]` 是否可写,默认true,写不代表删除,这里仅限修改
   - `[Value]` 属性的数据值,默认是undefined

   ```javascript
       var person = {};//设置了一个空对象
     //定义了对象的默认属性 name 的一些属性的特性
     Object.defineProperty(person,"name",{
         writable:false,// 不可以编辑
         value:"niconico" //默认的值就是 niconico
     });

     console.log(person.name);//返回niconico,因为默认值,不用设置也有值
     person.name = "gggg";//即使设置了name 新值,因为不可编辑,所以没变化
     console.log(person.name);//返回niconico
   ```

> 需要注意的是[Configurable]被改 false 之后没办法改回来 true

1. 访问器属性(getter() 和 setter()),而他们又有这样的特性:

   - [Configurable] 跟之前一样
   - [Enumberable] 跟之前一样
   - [Get] 在读取属性时调用的函数,默认是 undefined
   - [Set] 在写入属性时调用的函数,默认是 undefined

   ```javascript
       var book = {
         _year: 2004, //常用语法,代表只能通过对象方法访问的属性
         edition: 1
     };

     Object.defineProperty(book, "year", {
         get: function () { //定义一个 getter
             return this._year; //直接读取属性
         },
         //如果不设置 setter的话那么这个对象的属性就没办法修改
         set: function (newValue) { //定义一个 setter
             if (newValue > 2004) {
                 this._year = newValue; //如果注释掉,那么_ year 不会改变
                 this.edition += newValue - 2004;
             }
         }
     });

     book.year = 2005;
     //因为这个函数的 setter 里面也一起把_year修改了,所以能够看到被修改
     console.log(book.year); //返回2005
     console.log(book.edition);//返回2
   ```

数据属性和访问器属性的区分

```javascript
 var book = {};

    Object.defineProperties(book, { //这里用了defineProperties定义多个属性
        _year: { //数据属性
            value: 2004
        },
        edition: { //数据属性
            value: 1
        },
        year: {//访问器属性,判断的标准就是是否有 getter 或者 setter
            get: function () {
                return this._year;
            },
            set: function (newValue) {
                if (newValue > 2004) {
                    this._year = newValue;
                    this.edition += newValue - 2004;
                }
            }
        }
    })
    
    
    //获取属性的特性
    var descriptor = Object.getOwnPropertyDescriptor(book,"_year");
    console.log(descriptor.value); //获取值这个特性
    console.log(descriptor.configurable); //获取 configurable 这个特性    
```

## 创建对象

- 工厂模式:用函数封装以特定的接口创建对象,没法创建特定类型的对象
- 构造函数模式: 构造函数可以用来创建特定类型的对象,但是每个成员无法复用
- 原型模式:使用构造函数的 prototype 属性来指定那些应该共享的属性和方法
- 组合继承: 使用构造函数模式和原型模式时,使用构造函数定义实例属性,而使用原型定义共享的属性和方法动态原型模式:可以在不必预先定义构造函数的情况下实现继承,其本质是执行给指定对象的浅复制
- 寄生构造函数模式:基于某个对象或某些信息创建一个对象,然后增强对象,最后返回对象
- 稳妥构造函数模式:集寄生式继承和组合继承的优点

------

### 工厂模式

这种模式抽象了创建具体对象的过程,因为 ECMAScript 中无法创建类,所以用函数封装以特定的接口创建对象

```javascript
    function createPerson(name,age,job) {
        var o = new Object(); //代替创建对象
        o.name = name;//代替设置属性
        o.age = age;
        o.job = job;
        o.sayName = function () { // 代替设置方法
            console.log(this.name);
        };
        return o; //返回是一个对象
    }

    var person1 = createPerson("nico",29,"soft");
    var person2 = createPerson("gg",30,"dog");
    console.log(person1);//返回Object {name: "nico", age: 29, job: "soft"}
    console.log(person2);
```

> 优点:
> 1.创建对象的方式简单了
> 缺点:
> 1.没有解决对象类型识别的问题,因为都是直接new Object, 都是 Object,所以没法区分是具体哪一个对象类型

### 构造函数模式

实际上构造函数经历了以下过程:

1. 创建一个新对象
2. 将构造函数的作用域赋给新对象(因此this指向了这个新对象)
3. 执行构造函数中的代码(为这个新对象添加属性)
4. 返回新对象

```javascript
    function Person(name,age,job) {  //标准写法,构造函数名第一个大写
        this.name = name; 
        this.age = age;
        this.job = job;
        this.sayName = function () {
            console.log(this.name);
        }
        //不需要return,因为会自动返回新对象,如果使用了 return 就会改变了返回的内容
    }

        var person1 = new Person("nico",29,"soft");//用new
        var person2 = new Person("gg",30,"dog");
        console.log(person1);//返回Person {name: "nico", age: 29, job: "soft"}
        console.log(person2);
        
        
    //这些实例都是Object 对象,也是Person 对象
    console.log(person1 instanceof Object);//返回 true
    console.log(person1 instanceof Person);//返回 true
    //person1和 person2分别保存着Person 一个不同的实例,这两个实例都有一个constructor(构造函数)属性,都指向Person, 说明他们都是同一个构造函数创建的
    console.log(person1.constructor == Person);//返回 true
    console.log(person2.constructor == Person);//返回        
```

> 构造函数与其他函数的唯一区别就在于调用他们的方式不同,任何函数,只要通过 new 来调用,那他就可以作为构造函数,而任何函数,如果不通过 new 来调用,那他就跟普通函数也不会有两样.

构造函数也可以作为普通函数使用

```javascript
    function Person(name, age, job) {
        this.name = name; //直接赋值给 this, 即直接设置当前对象的属性
        this.age = age;
        this.job = job;
        this.sayName = function () {
            console.log(this.name);
        }
        //不需要return, 也不需要返回对象
    }
    // 作为构造函数调用
    var person = new Person("pp", 10, "kk");
    person.sayName();//返回 pp
    //作为普通函数调用
    Person("yy", 20, "gg");//这里添加到 window 对象了,因为默认全局作用域
    window.sayName();//返回 yy
    //在另外一个对象的作用域中调用
    var o = new Object();
    Person.call(o, "aa", 25, "bb"); //因为是被 o 调用,所以 this 指向 o
    o.sayName();//返回 aa
```

> 优点:
> 1.可以知道对象实例是是哪个对象类型,即构造函数是谁(通过 instanceOf() 或者 constructor 来验证)
> 缺点:
> 1.每个方法都要在每个实例上重新创建一遍,会导致不同的作用域链和标示符解析问题,例如两个实例之间的方法并不能用== 来判断,例如 person1.sayName == person2.sayName 是返回 false 的,因为都是新创建的实例,都是独立的

### 原型模式

- 我们创建的每个函数都有一个 prototype(原型)属性,这个属性是一个指针,指向一个对象,而这个对象的用途是包含可以由特定类型的所有实例共享的共享的属性和方法,
- 换句话说,不必再构造函数中定义对象实例的信息,而是可以将这些信息直接添加到原型对象中

```javascript
    function Person() {} //初始化一个空对象

    Person.prototype.name = "nico"; //直接将属性写到原型里面
    Person.prototype.sayName = function () {//直接将方法写到原型里面
        console.log(this.name);
    };

    //原型的所有属性和方法被所有实例共享
    var person1 = new Person();
    person1.sayName();//返回 nico

    var person2 = new Person();
    person2.sayName();//返回 nico

    //他们其实都指向同一个原型的同一个方法,所以 true
    console.log(person1.sayName() == person2.sayName());//返回true
```

> 优点:
> 1.可以让所有对象实例共享它所包含的属性和方法
> 缺点:
> 1.实例都需要有只属于自己的属性,而原型对象是完全共享的,所以很少有人单独使用原型模式

#### 理解原型对象

- 在脚本中没有标准的方式访问[prototype],但是firefox,safari,chrome在每个对象上都支持一个`_proto_`
- 创建了自定义的构造函数之后,其原型对象默认只会取得constructor属性,当调用构造函数创建一个新实例后,该实例的内部将包含一个指针指向构造函数的运行对象.

￼![img](https://segmentfault.com/img/bVHR7B?w=655&h=285)

1. Person 是构造函数,Person.prototype是原型对象,person1 和 person2 是实例

2. Person.prototype的constructor指向Person,因为原型对象是构造函数创建的,所以 constructor 指向Person

3. Person的prototype 指向了原型对象,而又因为默认情况下,所有的原型对象的 constructor 都是在被创建的时候指向构造函数

4. person1和person2 有一个内部属性`[prototype]`,指向Person.prototype,实例的prototype 指向原型对象很正常

5. 通过isPrototypeOf()来确定对象之间是否存在实例和原型对象的关联关系

   ```javascript
   //如果[prototype]指向调用isPrototypeOf方法的对象的话,那么就会返回 true
   console.log(Person.prototype.isPrototypeOf(person1)); //返回 true
   console.log(Person.prototype.isPrototypeOf(person2)); //返回 true
   ```

6. 通过 getPrototypeOf 方法来获取原型的属性

   ```javascript
   //getPrototypeOf返回的对象是原型对象
   console.log(Object.getPrototypeOf(person1) == Person.prototype);//返回 true
   console.log(Object.getPrototypeOf(person1).name); //即使这个实例没有设置属性 name, 也可以获取原型对象的属性 name
   ```

7. 用 hasOwnProperty() 方法检测一个属性是否存在实例中(返回 true),还是存在与原型中(返回 false)

```javascript
    function Person() {} //初始化一个空对象
    Person.prototype.name = "nico";
    var person1 = new Person();
    var person2 = new Person();
    //没有这个属性也会返回 false
    console.log(person1.hasOwnProperty("name"));//返回 false

    person1.name="bbb";//设置 person1的name
    console.log(person1.name); //返回 bbb
    console.log(person1.hasOwnProperty("name"));//返回true

    //没有设置,使用的是原型的 name,即使不存在实例中的时候
    console.log(person2.name);//返回 nico
    console.log(person2.hasOwnProperty("name"));//返回 false
```

> 每当代码读取某个对象的某个属性的时候,都会执行一次搜搜,搜索搜索对象实例本身,如果没有,就去搜索原型对象

1. 同时使用 in 和hasOwnProperty就能确定该属性是存在对象中还是存在原型中
2. in只能确定是否存在实例中,但区分不了是对象还是原型,hasOwnProperty只能确认是否存在实例中,所以两者结合可以实现判断

```javascript
    function hasPrototypeProperty(object,name) {
        //属性不存在于实例中 并且属性存在于对象中就返回 true    
        return !object.hasOwnProperty(name) && (name in object); //写&&的顺序：先放范围小的，再放范围大的
    }
```

1. 在 for-in 循环时,返回的是所有能够通过对象访问的,可枚举的属性,其中包括实例中的属性和原型中的属性
2. 用 Object.keys() 方法返回所有可枚举的属性, Object.getOwnPropertyNames可以返回所有属性,包括不可枚举的属性

```javascript
    function Person() {
    }
    Person.age = 19;
    Person.prototype.name = "nico";
    var keys1 = Object.keys(Person);//Person 的属性
    console.log(keys1); //返回["age"],数组
    var keys2 = Object.keys(Person.prototype);//Person的原型对象属性
    console.log(keys2);//返回["name"],数组
    
     //getOwnPropertyNames可以返回所有属性,包括不可枚举的属性,例如constructor
    var keys3 = Object.getOwnPropertyNames(Person);
    console.log(keys3); //返回["length", "name", "arguments", "caller", "prototype", "age"]
    var keys4 = Object.getOwnPropertyNames(Person.prototype);
    console.log(keys4); //返回["constructor", "name"]
```

> Object.keys()和Object.getOwnPropertyNames()都可不同程度的代替for-in, 不过需要比较新的浏览器

- 更简单的原型语法,封装原型

```javascript
    function Person() {
    }
//字面量创建对象语法
    Person.prototype = {
        constructor: Person, 
        name: "nico",
        age: 18,
        sayName: function () {
            console.log(this.name);
        }
    }
```

> 需要注意的是这种写法的话, constructor属性不再指向Person,因为每创建一个函数,就会同时创建他的 prototype 对象,这个对象自动获得 constructor 属性,而字面量语法会重写这个 prototype 对象,因此 constructor 属性也就变成了新的对象的 constructor 属性(指向 Object),所以需要另外指定一个 constructor

#### 原型的动态性

由于在原型中查找值的过程是一次搜索,因此我们队原型对象所做的任何修改都能够立即从实例上反映出来

```javascript
    function Person1() {};
    var friend = new Person1(); //先与修改原型前创建了实例,但也能使用这个原型方法
    Person1.prototype.sayHi = function () { 
        console.log("hi");
    };
    //先找自己,然后找原型
    friend.sayHi();//返回 hi
```

> 原因:实例与原型之间的松散链接关系当我们调用 friend.sayHi( )时,首先会在实例中搜索名为 sayHi 的属性,没找到之后会继续搜索原型,因为实例与原型之间的链接只不过是一个指针,而非一个副本,因此就可以在原型中找到新的 sayHi 属性并返回保存在那里的函数

- 重写原型切断了现有原型与任何之前已经存在的对象实例之间的联系,调用构造函数时会为实例添加一个指向最初原型的[prototype]指针,而把原型修改为另外一个对象就等于切断了构造函数与最初原型之间的联系

```javascript
    function Person() {
    }
    //重写原型之前
    var friend = new Person();

    Person.prototype.sayName = function () {
        console.log("hi");
    };
     friend.sayName();//返回 hi,
    //重写原型之后(注释了),内存地址发生了改变，所以在此之后调用会报错，所以，若是在重写之后再建对象会正常，见下例
//    Person.prototype = {
//        constructor: Person,
//        name: "nico",
//        age: 18,
//        sayName: function () {
//            console.log(this.name);
//        }
//    };

    friend.sayName();//返回 hi,
```



```javascript
function Person() {
    }
   // Person.prototype.sayName = function () {
  //      console.log("hi");
 //   };
  //   friend.sayName();//返回 hi,
    //重写原型之后(注释了)
    Person.prototype = {
        constructor: Person,
        name: "nico",
        age: 18,
       sayName: function () {
            console.log(this.name);
        }
    };
 //重写原型之前
    var friend = new Person();
    friend.sayName();//nico
```
![img](https://segmentfault.com/img/bVHR7Y?w=660&h=498)
￼

> 1.字面量写法修改原型对象会重写这个原型对象
> 2.实例中的指针仅指向原型,而不指向构造函数
> 3.因为他会创建一个新的原型对象,原有的实例会继续指向原来的原型,但是所有的属性和方法都存在于新的原型对象里面,所以没办法使用这些属性和方法
> 4.并不推荐在产品化得程序中修改原生对象的原型,可能会影响了其他使用原生对象的原型的代码

### 组合使用构造函数模式和原型模式(常用)

- 构造函数用于定义实例属性,原型模式用于定义方法和共享的属性.
- 每个实例都会有自己的一份实例属性的副本,但同时又共享着对方法的引用,这种模式还支持向构造函数传递参数

```javascript
   function Person(name, age, job) {
        //实例属性在构造函数中定义
        this.name = name;
        this.age = age;
        this.job = job;
        this.friends = ["tom", "jim"];
    }

    Person.prototype = {
        //共享的方法在原型中定义
        constructor: Person,
        sayName: function () {
            console.log(this.name);
        }
    };

    var person1 = new Person("Nico", 29, "software eng");
    var person2 = new Person("Greg", 30, "doctor");

    person1.friends.push("Vivi");//单独添加 person1实例的数组数据
    console.log(person1.friends);//返回["tom", "jim", "Vivi"]
    console.log(person2.friends);//返回["tom", "jim"]
    console.log(person1.friends === person2.friends); //返回 false,没共享 friends 数组
    console.log(person1.sayName === person2.sayName); //返回 true ,共享了其他方法
```





### 动态原型模式

- 通过检查某个应该存在的方法是否有效,来决定是否需要初始化原型.
- 把所有信息都封装在构造函数,通过在构造函数中初始化原型

```javascript
    function Person(name, age, job) {
        //实例属性在构造函数中定义
        this.name = name;
        this.age = age;
        this.job = job;
        //只在sayName方法不存在的时候才添加原型中
        if (typeof this.sayName != "function") {
            Person.prototype.sayName = function () {
                console.log(this.name);
            }
        }
    }

    var friend = new Person("jack", 29, "soft ware eng");
    friend.sayName();
```

> 1. 对原型修改的话,不能使用字面量重写,因为会断开跟原型的关联

### 寄生构造函数模式(parasitic)(不建议使用)

- 创建一个函数,该函数的作用仅仅是封装创建对象的代码,然后再返回新创建的对象.
- 这个代码几乎跟工厂模式一样,唯一区别是如何调用,工厂模式没有 new, 这个有 new

```javascript
    function Person(name, age, job) {
        var o = new Object();
        o.name = name;
        o.age = age;
        o.job = job;
        o.sayName = function () {
            console.log(this.name);
        };
        // let abc='aaaa'; 故意添加此属性，不return的话，可以访问到，有return的话，就只能访问o的东西了
        //在构造函数里面添加一个 return 会重写调用构造函数时返回的值
        //不写 return 的话,默认会返回新对象实例
        return o;
    }
    //用 new 方式来调用
    var friend = new Person("jack", 29, "soft ware eng"); 
	//这里我在此添加原型里的，这样就可以增加friend的公有函数，所以对象o和Person构造函数生成的对象完全两码事！
	Object.prototype.sayName1=function () {
                console.log(this.name+"我是原型里的");
            }
    friend.sayName1();
    friend.sayName(); //返回的实例就是 Person 函数里面新创建的那个指定实例,所以有这个实例的所有属性和方法
```

> 1. 返回的对象与构造函数或者构造函数原型属性之间没有关系,所以不能使用 instanceof 来确定对象类型,不建议使用这种模式

### 稳妥构造函数模式(较少使用)

- 稳妥对象durable object指的是没有公共属性,而且其方法也不引用 this 的对象,主要用在一些安全的环境中,禁止使用this 和 new 之类的,或者在防止数据被其他应用程序改动时使用(类似vue的组件属性方法)

```javascript
    function Person(name, age, job) {//这是一个vue
        //创建要返回的对象
        var o = new Object(); // 这个就是一个稳妥对象,因为单独独立，这是具体的组件
        //可以在这里定义私有变量和函数
        //添加方法
        o.sayName = function () {
            console.log(name);
        };
        //返回对象，假如是vue的话，这里不需要返回
        return o;
    }

    var friend = Person("nico", 29, "software eng");
    friend.sayName(); //返回 nico
```

## 继承

- 实现继承：表示一个类型派生于一个基类型，拥有该基类型的所有成员字段和函数。
- 接口继承：表示一个类型只继承了函数的签名，没有继承任何实现代码。
- 一个函数由这么几部分组成，函数名、参数个数、参数类型、返回值，函数签名由参数个数与其类型组成。函数在重载时，利用函数签名的不同（即参数个数与类型的不同）来区别调用者到底调用的是那个方法！函数签名由函数的名称和它的每一个形参（按从左到右的顺序）的类型和种类（值、引用或输出）组成。
- 因为 ECMAScript 中函数没有签名,所以无法实现接口继承
- ECMAScript 只支持实现继承,而且其实现继承主要是依靠原型链来实现.

### 原型链

- 实现继承主要是利用原型让一个引用类型继承另一个引用类型的属性和方法
- 构造函数,原型和实例的关系:
  - 每个构造函数都有一个原型对象
  - 原型对象都包含一个指向构造函数的指针
  - 实例都包含一个指向原型对象的内部指针
- 假如我们让原型对象等于另外一个类型的实例,此时,原型对象将包含一个指向另一个原型的指针,相应地,另一个原型中也包含着一个指向另外一个构造函数的指针,如此类推￼

(我把 SuperType 的 prototype 属性换了一个名字testprototype,方便理解)

> 1. instance 指向 SubType 的原型, SubType 的原型又指向了 SuperType 的原型, getSuperValue 方法仍然还在 SuperType.prototype 中,但是property(testprototype) 则位于 SubType.prototype 中,这是因为 prototype(testprototype)是一个实例属性,而 getSuperValue() 则是一个原型方法.既然 SubType.prototype 现在是 SuperType 的实例,那么 property(testprototype)就位于该实例中.
> 2. instance.constructor 现在指向SuperType, 这是因为 SubType 的原型指向了另外一个对象-- SuperType 的原型,而这个原型对象的 constructor 属性指向 SuperType

```javascript
//假设这个类型要被继承方
    function SuperType() {
        //属性
        this.testSuperprototype = true;
    }
    //原型的方法
    SuperType.prototype.getSuperValue = function () {
        return this.testSuperprototype;
    };
    //假设这个类型是继承方
    function SubType() {
        //属性
        this.subproperty = false;
    }

    //SubType继承于SuperType,将实例赋给SubType.prototype(Subtype的原型),
    //实现的本质就是重写了SubType的原型对象
	//SubType.prototype指向了方法SuperType()，也就是指向这个方法所在的内存地址即可！
    SubType.prototype = new SuperType();
    //集成之后,设置SubType的原型的方法
    SubType.prototype.getSubValue = function () {
        return this.subproperty;//获取subproperty属性,如果没有继承的话,那么这里是 false
                                //继承之后就改变了
    };
    var instance = new SubType();
    console.log(instance.getSuperValue()); //返回 true
```

> 1. 继承通过创建 SuperType 的实例,然后赋给 Subtype.prototype 原型实现的,原来存在于 SuperType 的实例的所有属性和方法,现在也存在于 SubType.prototype 中了
> 2. 确立继承关系之后,我们给 Subtype.prototype 添加了一个方法,这样就在继承了 SuperType 的属性和方法的基础上有添加了一个新方法

这是完整的原型链图,因为还要包含 Object, 不过总的来说基本差不多,例如,如果调用 instance的 toString()方法,其实就是调用 Object 的 toString()￼

#### 确定原型和实例的关系

```javascript
  //因为原型链的关系, instance都是Object 或者SuperType 或者SubType 任何一个类型的实例
    console.log(instance instanceof Object);//true
    console.log(instance instanceof SuperType);//true
    console.log(instance instanceof SubType);//true
    //只要在原型链出现过的原型,都可以说是该原型链所派生的实例的原型
    console.log(Object.prototype.isPrototypeOf(instance));//true
    console.log(SuperType.prototype.isPrototypeOf(instance));//true
    console.log(SubType.prototype.isPrototypeOf(instance));//true
```

#### 谨慎地定义方法

> 给原型添加方法的代码一定要放在替换原型的语句之后,不然就会覆盖了超类中的方法了.

```javascript
 //假设这个类型要被继承方
        function SuperType() {
            //属性
            this.testSuperprototype = true;
        }
        //原型的方法
        SuperType.prototype.getSuperValue = function () {
            return this.testSuperprototype;
        };
        //假设这个类型是继承方
        function SubType() {
            //属性
            this.subproperty = false;
        }

        //SubType继承于SuperType,将实例赋给SubType.prototype(Subtype的原型)
        SubType.prototype = new SuperType();
        //继承之后,设置SubType的原型的方法
        SubType.prototype.getSubValue = function () {
            return this.subproperty;//获取subproperty属性,如果没有继承的话,那么这里是 false
                                    //继承之后就改变了
        };
        //重写超类型(被继承的类型)中的方法
        SubType.prototype.getSuperValue = function () {
          return false;   //返回的是这个,而不是 true(被继承的类型中是 true)
        };
        var instance = new SubType();
        console.log(instance.getSuperValue()); //返回 false
```

> 在通过原型链实现继承时,不能使用对象字面量创建原型方法,因为这样会重写原型链的

### 借用构造函数constructor stealing(很少用)

基本思想:在子类型构造函数的内部调用超类型构造函数.

```javascript
    function SuperType() {
        this.colors = ["red", "blue", "green"];
    }
    function SubType() {
        ///call 的方式以SubType的身份来调用SuperType的构造函数,
        //这么做可以将SuperType的构造函数的属性传到SubType上,
      	//是不是有点像Java里的静态内部类的调用，直接初始化内部类的构造函数即可，可以用来创造单例模式，或者相当于Java里的静态代理也就是调用代理对象			的相应方法和属性，即 装饰者模式！
        SuperType.call(this); 
    }
 	SuperType.prototype.getSubValue = "我是原型";
    var instance1 = new SubType();
    instance1.colors.push("black");
	SuperType.prototype.getSuperValue = function () {
          return false;   //返回的是这个,而不是 true(被继承的类型中是 true)
        };
	//报错:Uncaught TypeError: Cannot read property 'getSubValue' of undefined 说明了原型是私有空间，而且Sub的prototype指向的内存区域和
	//SuperType的原型指向的内存区域地址完全不同好吧！ 可以类似理解为Java中的私有属性和公有属性的区别，但还是有区别的，看下例，类似于Java中的反射获		取属性
 	console.log(instance1.prototype.getSubValue);
	console.log(instance1.prototype.getSubValue=SuperType.prototype.getSuperValue);
	instance1.getSubValue="我是原型1";
    console.log(instance1.getSubValue);
    console.log(instance1.colors);//返回["red", "blue", "green", "black"]

    var instance2 = new SubType();
    console.log(instance2.colors);//返回["red", "blue", "green"]
```

变通：将super里的原型属性或者方法赋值于SuperType的原型空间，使后者的这个属性或者方法指向super相应所在地

```javascript
 function SuperType() {
        this.colors = ["red", "blue", "green"];
    }
    function SubType() {
        ///call 的方式以SubType的身份来调用SuperType的构造函数,
        //这么做可以将SuperType的构造函数的属性传到SubType上,
      	//是不是有点像Java里的静态内部类的调用，直接初始化内部类的构造函数即可，可以用来创造单例模式，或者相当于Java里的静态代理也就是调用代理对象			的相应方法和属性，即 装饰者模式！
        SuperType.call(this); 
    }

	SuperType.prototype.getSuperValue = function () {
          return false;   //返回的是这个,而不是 true(被继承的类型中是 true)
        };
 	SuperType.prototype.getSubValue = "我是原型";
    var instance1 = new SubType();
    instance1.colors.push("black");
	SubType.prototype.getSubValue=SuperType.prototype.getSuperValue;
 	console.log(instance1.getSubValue());//false
	instance1.getSubValue="我是原型1";
    console.log(instance1.getSubValue);
    console.log(instance1.colors);//返回["red", "blue", "green", "black"]

    var instance2 = new SubType();
    console.log(instance2.colors);//返回["red", "blue", "green"]
```



> 优点:
> 1.能实现继承
> 缺点:
> 1.因为使用 call 的方式即使可以调用超类来实现继承,但是超类的原型属性和方法都不能使用,因为 call 只是改变 this, 没有改变 constructor 指向

### 组合继承combination inheritance(常用)

- 将原型链和借用构造函数技术组合到一起
- 基本思想是:使用原型链实现对原型属性和方法的继承,而通过借用构造函数来实现对实例属性的继承
- 既通过原型上定义方法实现了函数复用,又能够保证每个实例都有它自己的属性

```javascript
//设置一个超类,即SuperType的构造函数,里面有2个属性
    function SuperType(name) {
        this.name = name;
        this.colors = ["red", "blue"];
    }
    //设置一个超类,即SuperType的原型方法 sayName()
    SuperType.prototype.sayName = function () {
        console.log(this.name);
    };
    //设置一个子类,即SubType的构造函数
    function SubType(name, age) {
        //call 的方式以SubType的身份来调用SuperType的构造函数,
        //这么做可以将SuperType的构造函数的属性传到SubType上,但是因为call 只能改变 this 指向,改变不了constructor, 所以没办法获得超类的原型方法
        //这样的话就将超类的属性放到子类里面,所以在实例化子类之后,即使改变了其中一个子类实例的属性,也不会影响其他的子类实例
        SuperType.call(this, name);////第二次调用超类SuperType
        this.age = age; //也设置了自己本身的属性(方便区分)
    }
    //将超类SuperType实例化,并赋值给SubType的原型
    //SubType的原型被改写了,现在就是SuperType实例了,这样就可以获取到SuperType的原型方法了
    SubType.prototype = new SuperType();//第一次调用超类SuperType
    //定义一个自己的原型方法(方便区分)
    //这个需要在原型被改写完成后才能做,不然的话会被覆盖
    SubType.prototype.sayAge = function () {
        console.log(this.age);
    };

    var instance1 = new SubType("nico", 20); 
    instance1.colors.push("black"); //instance1改变了,不过 instance2不会改变
    console.log(instance1.colors); //返回["red", "blue", "black"]
    instance1.sayName();//返回 nico,这是超类的原型方法,拿到子类用
    instance1.sayAge();//返回20,这是子类自定义的原型方法,一样可以用

    var instance2 = new SubType("greg", 29);
    console.log(instance2.colors);//返回["red", "blue"]
    instance2.sayName();//返回 greg
    instance2.sayAge();//返回29
```

备注:

1. 需要理解原型链的知识
2. 需要理解构造函数的执行过程
3. 使用这种方式实现继承, 子类能够调用超类的方法和属性,因为超类的原型也赋值给子类了,真正实现了复用和继承,而且也能够保证各自实例的属性互不干涉,因为属性都在new 构建的时候生成,每个实例都单独生成
4. 第一次调用超类会得到两个属性 name 和 colors,他们是超类 SuperType 的属性,不过现在位于子类 SubType 的原型中,第二次调用超类会创建新的两个属性 name 和 colors, 他们会覆盖掉子类 SubType原型中的两个同名属性

> 缺点:
> 1.会调用两次超类型构造函数
> 2.不得不在调用子类型构造函数时重写属性

### 原型式继承

- 必须有一个对象作为另外一个对象的基础
- 在没必要创建构造函数,只想让一个对象与另外一个对象保持类似的情况下,可以使用这个方式,需要注意的就是共享的问题

```javascript
function object(o) {
        function F() { //创建一个临时性的构造函数
        }
        F.prototype = o;//将传入的对象作为这个构造函数的原型
        return new F();//返回这个临时构造函数的新实例
    }

    var person = {
        name: "nico",
        friends: ["a", "b"]
    };
    //传入 person 对象,返回一个新的实例,这个实例也是传入的 person 对象作为原型的
    //所以可以使用它的属性和方法
    var anotherPerson = object(person);
    anotherPerson.name = "gg";
    anotherPerson.friends.push("rr");

    //因为是使用同一个对象作为原型,所以跟原型链差不多,会共享这个原型对象的东西
    var yetAnotherPerson = object(person);
    yetAnotherPerson.name = "ll";
    yetAnotherPerson.friends.push("kk");

    console.log(person.friends);//返回["a", "b", "rr", "kk"]
    console.log(person.name);//返回nico, 因为基本类型值是不会变化的
```

在 ECMAScript 5下有一个 Object.create 方法跟他差不多

```javascript
var person = {
            name: "nico",
            friends: ["a", "b"]
        };
        
        var anotherPerson = Object.create(person);
        anotherPerson.name = "gg";
        anotherPerson.friends.push("rr");
    
        var yetAnotherPerson = Object.create(person);
        yetAnotherPerson.name = "ll";
        yetAnotherPerson.friends.push("kk");
        //结果一样
        console.log(person.friends);//返回["a", "b", "rr", "kk"]
```

另外Object.create 支持第二个参数,可以指定任何属性覆盖原型对象的同名属性

```javascript
    var person = {
        name: "nico",
        friends: ["a", "b"]
    };

    var anotherPerson = Object.create(person, {
        name: { //以传入一个对象的方式, key 就是属性名
            value: "lala"
        }
    });
    //没有在民政局注册的小孩名字默认就是社会给其的初始名字：毛孩儿，毛妞儿
    console.log(person.name);
    console.log(anotherPerson.name);//返回 lala
```

### 寄生式继承

- 寄生式继承的思路与寄生构造函数和工厂模式类似,即创建一个仅用于封装继承过程的函数,该函数在内部以某种方式来增强对象,最后再像真的是他做了所有工作一样返回对象
- 任何能够返回新对象的函数都适用于此模式
- 跟构造函数模式类似,不能做到函数复用.

```javascript
    function object(o) {
        function F() { //创建一个临时性的构造函数
        }

        F.prototype = o;//将传入的对象作为这个构造函数的原型
        return new F();//返回这个临时构造函数的新实例
    }
    //相当于扔了两次,第一次扔给一个临时的构造函数,生成一个实例
    //第二次再扔给一个固定变量,然后在这里去给予属性和方法
    function createAnother(original) {
        var clone = object(original);
        clone.sayHi = function () { //可以自己添加方法
            console.log("hi");
        };
        return clone;
    }

    var person = {
        name: "nico",
        friends: ["a", "b"]
    };

    var anotherPerson = createAnother(person);
    anotherPerson.sayHi();//返回 hi
```

### 寄生组合式继承

- 通过借用构造函数来继承属性,通过原型链的混成形式来继承方法
- 基本思路,使用寄生式继承来继承超类型的原型,然后再将结果指定给予子类型的原型
- 此处涉及到父子容器思想！！

```javascript
 function object(o) {
        function F() { //创建一个临时性的构造函数
        }

        F.prototype = o;//将传入的对象作为这个构造函数的原型
        return new F();//返回这个临时构造函数的新实例
    }
    //两个参数,一个是子类型函数,一个是超类型构造函数
    function inheritPrototype(subType, superType) {
        //创建一个超类型原型的一个副本
        var prototype = object(superType.prototype);//创建对象
        //为创建的副本添加 constructor 属性,弥补因重写原型而失去默认的 constructor 属性
        prototype.constructor = subType;//增强对象
        //将新创建的对象赋值给子类型的原型,这样子类型就完成了继承了
        subType.prototype = prototype;//指定对象
    }


    function SuperType(name) {
        this.name = name;
        this.colors = ["red", "blue"];
    }
    SuperType.prototype.sayName = function () {
        console.log(this.name);
    };
    function SubType(name, age) {
        SuperType.call(this, name);
        this.age = age;
    }
    //可以看到,这里少调用了一次超类的构造函数
    inheritPrototype(SubType, SuperType);
    SubType.prototype.sayAge = function () {
        console.log(this.age);
    };

    var test = new SubType("aa", 100);
    test.colors.push("white");
    console.log(test.colors); //["red", "blue", "white"]
    test.sayName();//aa
    test.sayAge();//100

    var test1 = new SubType("pp", 1);
    test1.colors.push("black");
    console.log(test1.colors);//["red", "blue", "black"]
    test1.sayName();//pp
    test1.sayAge();//1
```

# 二 函数表达式

## 关于函数声明

他的一个重要特征就是函数声明提升,就是在执行代码之前会先读取函数声明,这意味着可以把函数声明放到调用他的语句的后面

```javascript
sayHi(); //将声明放到了后面
function sayHi(){
    console.log("hi");
}
```

## 关于函数表达式

创建一个匿名函数然后赋值给一个变量

```javascript
var functionName = function(arg0,arg1){
    //函数体
}
```

可以返回一个匿名函数,返回的函数可以赋值给一个变量,也可以被其他方式调用

```javascript
    function test(name,age){
        //这样可以返回一个匿名函数
        return function (name,age) {
            console.log(name);
            console.log(age);
        }
    }
```

## 递归

```javascript
    //递归,不断调用自身
    function factorial(num) {
        if (num <= 1) {
            return 1;
        } else {
            //arguments.callee指向正在执行的函数指针,代替函数名
            //所以即使后面将函数设置为 null, 不影响递归
            return num * arguments.callee(num - 1);
        }
    }
    //将函数赋值给变量
    var anotherFactorial = factorial;
    factorial = null;//设置函数factorial 为 null
    console.log(anotherFactorial(4));// 返回24
```

不过这种方式在严格模式不能使用arguments.callee,需要借助命名函数表达式

```javascript
//创建一个f 命名的函数表达式,然后将它赋值给变量factorial
    var factorial = (function f(num) {
        if (num <= 1) {
            return 1;
        } else {
            return num * f(num - 1); //函数f 依然有效
        }
    });
    console.log(factorial(4));//返回24
```

> 因为函数是引用类型,即使赋值给变量,也只是赋值了一个引用指针,所以函数本身还是可以直接使用的
>
> 更多见[arguments.callee详解](http://www.cnblogs.com/lijinwen/p/5727550.html)

## 闭包

- 闭包是指有权访问另一个函数作用域的变量的函数
- 创建闭包的常见方式,就是在一个函数内部创建另一个函数
- 闭包也可以理解为一些被返回的匿名函数,这些匿名函数可以访问另一个函数作用域
- 闭包的作用域包含着他自己的作用域,包含函数的作用域和全局作用域

一般函数作用链细节:

- 当某个函数第一次被调用时,会创建一个执行环境,以及相应的作用域链,并把作用域链赋值给一个特殊的内部属性`[Scope]`
- 然后,使用 this,arguments 和其他命名参数的值来初始化函数的活动对象
- 在作用域链中,外部函数的活动对象始终处于第二位,外部函数的外部函数的活动对象处于第三位,如此类推,直至作为作用域链终点的全局执行环境
- 作用域链本质上是一个指向变量对象的指针列表,他只引用但不实际包含变量的对象

这个是一般的函数,非闭包

```javascript
    function compare(value1, value2) {
        if (value1 < value2) {
            return -1;
        } else if (value1 > value2) {
            return 1;
        } else {
            return 0;
        }
    }
```

￼![img](https://segmentfault.com/img/bVIf7q?w=759&h=317)

> 1.每个执行环境都有一个表示变量的对象-变量对象
> 2.全局环境的变量对象始终存在
> 3.像 compare 函数这样的局部环境的变量对象只会在函数执行过程中存在
> 4.在创建 compare 函数时,会创建一个预先包含全局变量对象的作用域链,这个作用域链被保存在内部的`[Scope]`属性中
> 5.如果又有新的活动对象(函数执行),那么就会被推入作用域链的最前端
> 6.无论什么时候在函数中访问一个变量,就会从作用域链中搜索具有相应名字的变量

闭包作用链细节:

- 在另一个函数内部定义的函数会将包含函数(即外部函数)的活动对象添加到他的作用域链中
- 内部定义的匿名函数的作用域链实际上会包含外部函数的活动对象
- 即使外部函数执行完毕,内部匿名函数依然不会被销毁,然后他依然引用这个活动对象,直至这个匿名函数被销毁后才会全部销毁

```javascript
    function createComparisonFunction(propertyName) {
      
        return function (object1, object2) {
            var value1 = object1[propertyName];
            var value2 = object2[propertyName];
            if (value1 < value2) {
                return -1;
            } else if (value1 > value2) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    var compare = createComparisonFunction("name");
    var result = compare({name:"gg"},{name:"bb"});
```

￼

> 1.首先:内部的匿名函数会添加到createComparisonFunction的作用域链中,()即执行正常的一般函数作用域链流程,只是全局活动对象改成了局部活动对象
> 2.然后:被返回后,这个匿名函数的作用域链被初始化为包含createComparisonFunction函数的活动对象和全局活动对象,因为被返回到外部,外部是一个全局活动对象,所以他的作用域链就有了全局活动对象
> 3.那么,现在如果createComparisonFunction执行完毕了,但是因为他这个活动对象还被匿名函数引用,所以即使他执行完毕了,依然不会销毁,除非引用的匿名函数销毁后,才会销毁

可以手动将被返回的匿名函数(这就是一个闭包)设置 null 来销毁

```javascript
    var compare = createComparisonFunction("name");
    var result = compare({name:"gg"},{name:"bb"});
    compare = null;
```

> 将compare(他是被返回的那个匿名函数)的引用解除,就可以通知 gc 进行回收

### 闭包与变量

闭包只能取得包含函数中任何变量的最后一个值,因为闭包保存的是整个变量对象,而不是某个特殊的变量

```javascript
    function createFunctions() {
        var result = new Array();
        for (var i = 0; i < 10; i++) {
          //也就是将下面一个匿名对象重复赋给不同的名字的引用，但引用指向的地址，都是这个匿名对象所在的内存地址
            result[i] = function () { //这是一个闭包
                //因为闭包保存的是整个createFunctions变量对象,所以当他执行完成的时候(for循环结束),
                //i是等于10的,所以就会是10,由始至终,闭包里面引用的都是一整个变量对象,而不是一个变量
              	
                return i;
            };
        }
        return result; //返回的是一个数组,数组里面每一个项目都是一个function
    }
    var test = createFunctions();
    for (var i = 0; i < 10; i++) {
        //需要执行这个 function 才能够获取里面的值
        console.log(test[i]());//都是10 
    }
```

如果想达到我们的效果,返回的 i 是0-9的话:

```javascript
    function createFunctions() {
        var result = new Array();
        for (var i = 0; i < 10; i++) {
            result[i] = function (num) {//这是一个匿名函数,参数是 num
                return function () {// 这是一个闭包,不过这个闭包访问的num是我们传入的num,即使闭包保存一整个变量对象,但是我们将变量对象改成了外面这个匿名函数
                    return num;     //相当于在闭包外面包了一层活动对象,将活动对象改变成能够改变值 num的活动对象
                };
            }(i);//这个匿名函数会立即执行,并且传入了 i 作为 num
        }
        return result; //返回的是一个数组,数组里面每一个项目都是一个function
    }
    var test = createFunctions();
    for (var i = 0; i < 10; i++) {
        //需要执行这个 function 才能够获取里面的值
        console.log(test[i]());//返回0-9
    }
```

还可以改下:

```javascript
function createFunctions() {
        var result = new Array();
        for (var i = 0; i < 10; i++) {
            result[i] = function (num) {//这是一个匿名函数,参数是 num            
                    return num;     
                
            }(i);//这个匿名函数会立即执行,并且传入了 i 作为 num,所以，这里就是返回一个i值即： result[i]=i
        }
        return result; //返回的是一个数组,数组里面每一个项目都是一个数字
    }
    var test = createFunctions();
    for (var i = 0; i < 10; i++) {
        //需要执行这个 function 才能够获取里面的值
        console.log(test[i]);//返回0-9
    }
```



## 关于 this 对象

- this 对象是在运行时基于函数的执行环境绑定的,在全局函数中, this 指向 window,而当函数被作为某个对象的方法调用时,this 指向那个对象
- 在通过 call() 或 apply()改变函数执行环境的时候,也会改变 this 指向

```javascript
    var name = "the window";
    var object = {
        name: "my object",
        //返回一个匿名函数
        getNameFunc: function () {
            //匿名函数里面又返回一个匿名函数(闭包)
            //当返回的时候,当前环境已经变成了全局环境了,搜索的话直接在当前活动对象(全局)找到了
            return function () {
                return this.name; //所以返回的是全局环境的 name
            }
        }
    };
    //object.getNameFunc()返回一个函数,然后再加上(),这个函数就会执行
    console.log(object.getNameFunc()());//返回 the window
```

如果对 this 进行保存的话,那么可以将当前对象的引用保存起来,赋值到一个变量上来使用

**无论怎么考虑，只需要去想指针指向哪里即可，无须被概念所混淆，指哪打哪，专注于内存地址！！**

```javascript
    var name = "the window";
    var object = {
        name: "my object",
        //返回一个匿名函数
        getNameFunc: function () {
            //将当前对象(当前这个function)引用保存起来
            var that = this;
            //匿名函数里面又返回一个匿名函数(闭包)
            //当返回的时候,使用的是保存起来的这个对象引用,所以会返回这个对象的属性
            return function () {
                return that.name;
            }
        }
    };
    //object.getNameFunc()返回一个函数,然后再加上(),这个函数就会执行
    console.log(object.getNameFunc()());//返回 my object
```

> **很多时候很有用,在 this 可能会被改变的情况下(尤其在闭包环境下),先保存起来会很方便**

下面例子是想说明语法的细微变化,也可能改变this的值

```javascript
    var name = "the window";
    var object = {
        name: "my object",
        getName: function () {
            return this.name;
        }
    };
    //正常执行,返回正常
    console.log(object.getName());//返回 my object
    //加了括号,但是this没有改变
    console.log((object.getName)());//返回 my object
    //加了赋值运算,this被改变了,因为赋值表达式的值是函数本身,处于全局环境下
    console.log((object.getName = object.getName)());//返回 the window
```

## 模仿块级作用域

- javascript 没有块级作用域(私有作用域)的概念
- 匿名函数可以模仿块级作用域

```javascript
    (function () {
      //块级作用域
    }());
---------分解一下
    ( //第一对圆括号
    function () {
      //块级作用域  
    }
    ()//第二对圆括号
    );
    //将函数声明包含在第一对中,表示他实际上是一个函数表达式
    //而紧随其后的第二对圆括号会立即调用这个函数
```

- 无论什么地方,只要临时需要一些变量,就可以使用,这种技术经常在全局作用域中被用在函数外部,从而限制响全局作用域中添加过多的变量和函数
- 这种做法可以减少闭包占用内存的问题,因为没有指向匿名函数的引用,只要函数执行完毕,就可以立即销毁其作用域链

## 私有变量

- 把有权访问私有变量和私有函数的共有方法称为特权方法第一种创建方法:

```javascript
    function MyObject() {
        //私有变量和私有函数
        var privateVariable = 10; 
        function privateFunction() {
            return false;
        }
        //特权方法
        //因为这里作为闭包有权访问在构造函数定义的所有变量和函数
        //另外设置了this可以被调用
        this.publicMethod = function () {//外部只能通过这个方法访问私有属性和函数
            privateVariable++;
            return privateFunction();
        }
    }
```

第二种是:

```javascript
    function Person(name) {
        //只有方法暴露,里面的属性是只能由方法访问
        //因为这些方法都是在构造函数内部定义的,他们作为闭包能够通过作用域链访问属性
        this.getName = function () {
            return name;
        };
        this.setName = function (value) {
            name = value;
        };
    }
    //每一个实例都不一样,每次new都会调用构造函数重新创建方法
    var person = new Person("nico");
    console.log(person.getName()); //返回nico
    person.setName("ggg");
    console.log(person.getName());//返回ggg
```

> 使用构造函数模式的缺点就是针对每个实例都会创建同样一组新方法

## 静态私有变量

通过在私有作用域中定义私有变量或函数,同样可以创建特权方法

1. 这种方式
   - 私有变量和函数是由实例共享(因为被闭包引用着这个块级作用域)
   - 由于特权方法是在构造函数的原型上定义的,所以所有实例都使用同一个特权方法

```javascript
(function () {
        //私有变量和私有函数
        var privateVariable = 10;

        function privateFunction() {
            return false;
        }

        //构造函数
        MyObject = function () { //没有函数声明,使用函数表达式创建
            //因为函数声明只能构建局部函数,现在创建了一个全局函数
            //因为这是全局函数,所以也相对来说就是一个静态了.
        };
        //公有/特权方法
        //在原型上定义方法,所有这个原型的实例都能共享
        //特权方法是一个闭包,保存着对包含作用域的引用(当前这个块级作用域)
        MyObject.prototype.publicMethod = function () { //这个是一个闭包
            privateVariable++;
            return privateFunction();
        }
    })();
```

1. 这种方式,
   - 私有(静态)变量变成了由所有实例共享的属性(在当前这个块级作用域)
   - 构造函数能够静态变量,因为所有实例共享,所以每次调用构造函数的时候也会改变
   - 特权方法写在构造函数的原型上,所有实例共享

```javascript
    (function () {
        //"静态"变量,对于每个实例来说都是共享的
        var name = "";

        //构造函数这里也是全局的,而且每次调用构造函数就会创建一个新的name值
        Person = function (value) {
            name = value;
        };
        //在原型上写方法,方法在实例之间共享
        Person.prototype.getName = function () {
            return name;
        };
        Person.prototype.setName = function (value) {
            name = value;
        };
    })();

    var person1 = new Person("mico");
    console.log(person1.getName());//返回mico
    person1.setName("oo");
    console.log(person1.getName());//返回oo

    //因为静态变量共享的关系,name被改了之后,全部实例的name都被改了
    var person2 = new Person("gg");
    console.log(person1.getName());//返回gg
    console.log(person2.getName());//返回gg
```

## 模块模式

- 模块模式: 为单例创建私有变量和特权方法
- 单例(singleton),指的是只有一个实例的对象
- 如果必须创建一个对象并以某些数据对其进行初始化,同时还要公开一些能够访问的这些私有数据的方法,就可以使用模块模式
- 模块模式创建的单例都是Object的实例,因为都是字面量创建,一般来说,单例都是全局对象,一般不会将他传递给一个函数,所以也没必要检验对象类型了

```javascript
//单例用字面量方式创建
    var singleton = {
        name: value,
        method: function () {
            //方法代码
        }
    };
```

```javascript
//模块模式
    var singleton = function () { //这是一个匿名函数
        //私有变量和私有函数
        var privateVariable = 10;

        function privateFunction() {
            return false;
        }

        //特权/公有方法和属性
        //返回的这个对象是在匿名函数内部定义的,因此它的公有方法有权访问私有变量和函数
        //从本质上来讲,定义的是单例的公共接口
        return { //返回了一个单例对象,{}是对象写法
            publicProperty: true, //只包含可以公开的属性和方法
            publicMethod: function () {
                privateVariable++;
                return privateFunction();
            }
        };
    }();
```

模块模式的应用例子

```javascript
    //需要一个单例来管理应用程序级的信息
    //创建一个application对象(返回单例对象)
    var application = function () {
        //初始化一个数组
        var components = new Array();
        components.push(new BaseComponent());
        //返回的单例可以使用方法,然后获取数组的长度或者添加数组内容
        return {
            getComponentCount: function () {
                return components.length;
            },
            registerComponent: function (component) {
                if (typeof component == "object") {
                    components.push(component);
                }
            }
        }
    }();
```

### 增强的模块模式

改进的模块模式:在返回对象之前加入对其增强的代码.例如必须是某种类型的实例,同时还必须添加某些属性或方法对其加以增强

```javascript
    var singleton = function () {
        //私有变量和私有函数
        var privateVariable = 10;

        function privateFunction() {
            return false;
        }

        //创建对象
        var object = new CustomType();

        //添加特权/公有属性和方法
        object.publicProperty = true;
        object.publicMethod = function () {
            privateVariable++;
            return privateFunction();
        };
        //返回这个对象
        return object;
    }();
```
# 四 事件处理

### 事件流

事件流是指从页面接受事件的顺序
![img](https://segmentfault.com/img/bVIf9T?w=540&h=486)
￼

> 一般考虑兼容性问题,会使用冒泡而不适用捕获

#### 事件冒泡event bubbling

事件开始时由具体的元素(嵌套层次最深的元素)接受,然后逐级向上传播到文档document

￼![img](https://segmentfault.com/img/bVIf9U?w=319&h=271)

#### 事件捕获

基本跟事件冒泡相反,事件捕获用于在于事件到达预定目标之前捕获他,document首先接收到事件,然后事件依次向里传递,一直到传播事件的实际目标,例如这个div是我们点击的那个div

￼![img](https://segmentfault.com/img/bVIf91?w=253&h=215)
事件捕获用的不多

#### dom事件流

DOM2级事件,规定的事件流包括三个阶段

- 事件捕获阶段
- 处于目标阶段
- 事件冒泡阶段

￼![img](https://segmentfault.com/img/bVIf92?w=401&h=275)

> 其实每一个事件都是有这么一个阶段转换过程的

## 事件处理程序

### html事件处理 (不建议使用)

即在html中写的事件两种使用方式

```
<input typeof="button" value="click me" onclick="alert('click')"/>
```

```
<script>
function showMessage() {
    alert("Hello");
}
</script>
<input typeof="button" value="click me" onclick="showMessage()"/>
```

> 缺点:不方便mvc分离,代码混乱

### DOM0级事件处理程序

- 将一个函数赋值给一个事件处理程序属性
- 主动式的事件处理

```
    var btn = document.getElementById("myBtn");
    //主动赋值给onclick属性
    btn.onclick = function () {
        console.log("Clicked");
    }
    btn.onclick = null; // 删除事件处理程序
```

### DOM2级事件处理程序

- addEventListener() 添加事件处理程序监听
- removeEventListener() 移除事件处理程序监听
- 所有DOM节点中都包含这两个方法
- 被动式的事件处理
- 可以添加多个多个事件处理程序,他们会按照添加的顺序触发
- 大多数情况下,都是将时间处理程序添加到事件流的冒泡阶段,这样可以最大限度的兼容各种浏览器.
- 最好只在需要在事件到达目标之前截获他的时候将时间处理程序添加到捕获阶段

```
        var btn = document.getElementById("myBtn");
        //三个参数,要处理的事件名,作为事件处理程序的函数和一个布尔值
        btn.addEventListener("click",function () {
            alert(this.id);
            //true表示捕获阶段调用事件处理程序,false表示在冒泡阶段调用事件处理程序
        },false)
```

removeEventListener只能移除添加时传入的函数,所以匿名函数是没办法移除的(匿名函数没有办法确定是同一个)

```
    var btn = document.getElementById("myBtn");
    //将匿名函数改成有名函数
    var handler = function () {
        alert(this.id);
    };
    btn.addEventListener("click",handler,false);
    //可以移除
    btn.removeEventListener("click",handler,false);
```

## 事件对象

在触发DOM上的某个事件时,会产生一个事件对象event,这个对象包含着所有与事件相关的信息. [事件属性查看](https://developer.mozilla.org/en-US/docs/Web/API/Event)

```
    var btn = document.getElementById("myBtn");
    //这个event就是事件对象,里面的属性可以直接调用查看
    btn.onclick = function (event) {
        alert(event.type);//返回click
    };
    btn.addEventListener("click",function (event) {
        alert(event.type);//返回click
    },false)
```

如果点击一个在body里面的myBtn元素:

```
    document.body.onclick = function (event) {
        console.log(event.currentTarget === document.body); //返回true
        console.log(this === document.body); //返回true
        console.log(event.target === document.getElementById("myBtn")); //返回true
    }
```

> 1.currentTarget代表事件处理程序当前正在处理的事件的那个元素,那么本例子的话就是body,另外this也是body,因为事件处理程序是注册到body 上的
> 2.target是事件的目标,即点击的那个元素,就是myBtn
> 3.需要注意的是即使点击了一个元素,但是因为最终都会冒泡到body那里,所以事件处理处理会在冒泡到最外层进行处理

通过判断事件对象的type来触发不同的事件处理程序,用于需要通过一个函数处理多个事件时.

```
    var btn = document.getElementById("myBtn");
    var handler = function (event) {
        switch (event.type) {
            case "click":
                alert("Clicked");
                break;
            case "mouseover":
                event.target.style.backgroundColor = "red";
                break;
            case "mouseout":
                event.target.style.backgroundColor = "";
                break;
        }
    };
    btn.onclick = handler;
    btn.onmouseover = handler;
    btn.onmouseout = handler;
```

### 阻止特定事件的默认行为preventDefault()

默认行为可以使点击a标签的时候跳转到另外一个页面,或者内容超出一定大小的滚动事件

```
//假设myLink是一个a标签
    var link = document.getElementById("myLink");
    link.onclick = function (event) {
        //阻止了url 默认行为跳转
        event.preventDefault();
    };
```

> 需要注意的是只有cancelable属性设置为true的事件,才可以使用preventDefault(),这个属性可以在浏览器调试模式里面看,也可以打印出来

### 停止事件在DOM层次中传播,例如事件冒泡

```
    var btn = document.getElementById("myBtn");
    btn.onclick = function (event) {
        alert("mgBtn click");
        //如果没有屏蔽传播的话,会出现两次alert,因为事件会传播到body,也会被触发然后执行
        event.stopPropagation();
    };
    document.body.onclick = function (event) {
        alert("body click");
    }
```

### 事件对象的eventPhase属性

用来确定事件当前正位于事件流的哪个阶段

- 捕获阶段调用的事件处理程序 值为1
- 事件处理程序处于目标对象上 值为2
- 在冒泡阶段调用的事件处理程序 值为3

```
    var btn = document.getElementById("myBtn");
    btn.onclick = function (event) {
        alert("btn:" + event.eventPhase); //返回2
    };
    document.body.addEventListener("click", function (event) {
        alert("body-1:" + event.eventPhase);//返回1
    }, true); // 这里是true代表在捕获阶段

    document.body.onclick = function (event) {
        alert("body-2:" + event.eventPhase);//返回3
    }
```

> 1. 首先是返回1,因为是在捕获阶段触发,按照流程上是先捕获然后再到目标再到冒泡的,而捕获阶段是从最外层往里传播的,所以是body
> 2. 然后是到达了目标myBtn,所以返回23.再到返回3,冒泡阶段,是从最里往外传播,最里的是myBtn,最外是body,所以触发了最后的body事件处理程序

### ie事件(暂时不看)

## 事件类型

### ui事件

- load 当页面完全加载后在window上面触发(包括所有img,js,css等)

```
    window.addEventListener("load",function (event) {
        alert("loaded");
    })
```

> 在DOM2级事件规范,应该在document而非window上面触发load事件,但是所有浏览器都在window上面实现了该事件,所以这样用确保兼容性

也可以单独对某个元素的加载进行触发

```
    var img = document.getElementById("myImg");
    img.addEventListener("load",function (event) {
        //这个加载是指图片完全加载完成,包括下载成功
        alert(event.src);
    });
```

```
    window.addEventListener("load", function (event) {
        //创建了img元素
        var img = document.createElement("img");
        //添加load事件监听
        img.addEventListener("load", function (event) {
            alert("aa");
        });
        //输出到dom树
        document.body.appendChild(img);
        //设置src后,img才会开始加载
        img.src = "https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo/bd_logo1_31bdc765.png";
    })
```

> img只有设置了src属性才会开始下载script文件的话,只有设置了src并且放到dom树里面才会开始下载

确定js文件是否加载成功

```
    window.addEventListener("load", function (event) {
        //创建了script元素
        var script = document.createElement("script");
        //添加load事件监听
        script.addEventListener("load", function (event) {
            alert("Loaded");
        });
        //script设置src后,并且添加到dom树后才会开始加载
        script.src = "https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo/bd_logo1_31bdc765.js";
        //输出到dom树
        document.body.appendChild(script);
    })
```

- unload与load事件对应,这个事件在文档被完全卸载后触发,例如用户从一个页面切换到另外一个页面,一般用于清楚引用,避免内存泄露

```
    window.addEventListener("unload", function (event) {
        alert("aaa");
    })
```

- abort在用户停止下载过程时,如果嵌入的内容没有加载完,则在对象元素上触发
- error
  - 当发生js错误时在window上面触发
  - 当无法加载图像时,在img触发
  - 如果嵌入的内容没有加载完,则在对象元素上触发
- select当用户选择文本框 input或texterea中的一个或多个字符时触发
- resize调整浏览器窗口的高度或者宽度的时候触发
- scroll当用户滚动带滚动条的元素中的内容时,在该元素上触发

### 焦点事件

- blur 在元素失去焦点时触发
- DOMFocusIn 用下面的
- DOMFocusOut 用下面的
- focus 在元素获得焦点时触发,这个事件不会冒泡
- focusin 在元素获得焦点时触发,会冒泡
- focusout 在元素失去焦点时触发

> 即使focus和blur不冒泡,但是也可以在捕获阶段侦听到

### 鼠标事件

- click 单击
- dblclick 双击
- mousedown 用户按下了鼠标按钮
- mouseenter 鼠标从元素外部首次移动到元素范围之内
- mouseleave 鼠标从元素范围内移动到元素范围外
- mousemove 鼠标在元素内部移动时重复触发
- mouseout 鼠标在元素上方
- mouseover 当鼠标指针位于一个元素外部,然后用户将其首次移入另外一个元素边界之内触发
- mouseup 用户释放鼠标按钮时触发

> 除了mouseenter 和mouseleave,所有鼠标事件都会冒泡

### 触摸设备

- 轻击可单击元素会触发mousemove事件,如果此操作会导致内容变化,将不再有其他事件发生,如果屏幕没有因此变化,那么会依次发生mousedown,mouseup和click事件
- 轻击不可单击元素不会触发任何事件
- 两个手指放在屏幕上且页面随手指移动而滚动时会触发mousewheel和scroll事件

### 滚轮事件

mousewheel,值是event.wheelDelta,方向是用正负判断

### 文本事件和键盘事件

- keydown 当用户按下减胖上的任意键时触发,而且如果按住不放的话会重复触发
- keypress 当用户按下键盘的字符键时触发,而且如果按住不放的话会重复触发
- keyup 当用户释放键盘上的按键时触发首先会触发keydown,然后keypress,然后keyup
- 需要知道键码,对应不同的按键
- 字符编码(ASCII编码),用户按键后输入的屏幕文本的显示内容,用charCode属性,用String.fromCharCode()转换为实际的字符
- 只有一个文本事件,textInput,用户按下能够输入实际字符的键时才会触发
- textInput的事件的对象里面有一个属性inputMethod,用来分辨文本输入到文本框的方式

```
0   不确定是怎么输入
1   键盘输入
2   文本粘贴
3   拖放进来的
4   IME输入
5   通过表单中选择某一项输入
6   手写输入
7   语音输入
8   通过几种方式组合输入
9   通过脚本输入
```

### 变动事件

dom变动的时候触发

- 删除节点replaceChild的时候
- 插入节点appendChild(),replaceChild()或insertBefor()的时候

### html5事件

- DOMContentLoaded事件,相对于load事件会在页面中的一切都加载完成后触发,这个的话,就只会在dom树加载完成后触发,不理会其他img,js文件,css文件之类的jquery的document.ready()的加载方式就是使用跟这个
- pageshow和pagehide事件在页面显示触发(在load之后)和在页面隐藏时触发(在unload之前)
- hashchange事件url参数列表的锚点!!!变化的时候通知开发人员,锚点是#这样的

```
    window.addEventListener("hashchange", function (event) {
        console.log(event); //返回一个对象
//        console.log("old url:" + event.oldURL + "new url:" + event.newURL);
    },false);
```

返回的对象里面有很多有用的信息,例如

```
newURL
:
"http://localhost:63342/test-webstorm/test5.html?_ijt=dhn30q02o026mo2upn01tav7jn#dsad"
oldURL
:
"http://localhost:63342/test-webstorm/test5.html?_ijt=dhn30q02o026mo2upn01tav7jn"
```

### 设备事件

跳过

### 触摸手势事件

- touchstart 当手指触摸屏幕时触发
- touchmove 当手指滑动时连续触发
- touchend 当手指离开屏幕时触发
- touchcancel 当系统停止跟中触摸时触发每个触摸事件都有一些属性
- clientX,clientY(视口),screenX,screenY(屏幕),pageX,pageY(页面) 一些坐标属性
- target 触摸的dom节点目标
- touches 表示当前跟踪的触摸操作的touch对象数组,每次触摸都会生成一个对象数组,这个对象数组只有一个对象,所以用[0]调用
- targetTouches 特定于事件目标的touch对象的数组
- changeTouches 一个 TouchList 对象，包含了代表所有从上一次触摸事件到此次事件过程中，状态发生了改变的触点的 Touch 对象,每次触摸都会生成一个对象数组,这个对象数组只有一个对象,所以用[0]调用

```
    function handleTouchEvent(event) {
        //只跟踪一次触摸
        if (event.touches.length == 1) {
            switch (event.type) {
                case "touchstart":
                    console.log("touch stared(" + event.touches[0].clientX + "," + event.touches[0].clientY + ")");
                    break;
                case "touchend":
                    console.log("touch end(" + event.changedTouches[0].clientX + "," + event.changedTouches[0].clientY + ")");
                    break;
                case "touchmove":
                    //阻止默认滚动行为
                    event.preventDefault();
                    console.log("touch moved(" + event.changedTouches[0].clientX + "," + event.changedTouches[0].clientY + ")");
                    break;
            }
        }
    }
    window.addEventListener("touchstart", handleTouchEvent);
    window.addEventListener("touchend", handleTouchEvent);
    window.addEventListener("touchmove", handleTouchEvent);
```

## 内存与性能

### 事件委托

解决事件处理程序过多的问题

```
<ul id="myLinks">
    <li id="test1">test1</li>
    <li id="test2">test2</li>
</ul>
```

通过添加一次事件可以绑定所有的li,利用冒泡的特性

```
    var list = document.getElementById("myLinks");
    list.addEventListener("click",function (event) {
        //event事件对象有target属性,里面还有id
        //根据里面的不同的id来触发不同的事件处理逻辑
        //每一个li都能够冒泡到上层的ul,所以可以得到被处理
        switch (event.target.id){
            case "test1":
                console.log("test111111");
                break;
            case "test2":
                console.log("test22222");
                break;
        }
    });
```

### 移除事件处理程序

空事件处理程序过多也会导致性能不好例如手动设置null

```
btn.onclick = null;
```

但是没说有对于addEventListener那种处理.

## 模拟事件

模拟事件在测试web应用程序时候是一种极其有用的技术