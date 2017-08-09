系列文章摘自:http://www.cnblogs.com/tiger-xc/category/990815.html

# Akka（0）：聊聊对Akka的初步了解和想法

前一段时间一直沉浸在函数式编程模式里，主要目的之一是掌握一套安全可靠的并发程序编程方法（concurrent programming），最终通过开源项目[FunDA](http://www.github.com/bayakala/funda)实现了单机多核CPU上程序的并行运算。但是，虽然通过在终端实现并行运算能充分利用多核CPU的计算能力把数据处理运算分布到前台可以大大减轻后台服务器的压力，提高系统整体效率，对现今大数据普遍盛行的系统计算要求还是远远不足的，只有通过硬件平行拓展（scale-out）形成机群并在之上实现分布式运算才能正真符合新环境对软件程序的要求。那么，下一个阶段目标应该是分布式运算了。众所周知，Akka应该是目前最著名和通用的分布式软件开发工具了，加上是scala语言的开源项目。由于Akka已经是一个在现实中被大量使用的成熟软件工具，网上各方面的参考资料比较丰富，感觉应该是一个比较理想的选择。

花了几天时间研究了一下Akka官方网站上的资料，先在这里把了解的情况在下面做个小结：

Akka程序是由多个Actor组成的。它的工作原理是把一项大运算分割成许多小任务然后把这些任务托付给多个Actor去运算。Actor不单可以在当前JVM中运行，也可以跨JVM在任何机器上运行，这基本上就是Akka程序实现分布式运算的关键了。当然，这也有赖于Akka提供的包括监管、监视各种Actor角色，各式运算管理策略和方式包括容错机制、内置线程管理、远程运行管理（remoting）等，以及一套分布式的消息系统来协调、控制整体运算的安全进行。

Actor是Akka系统中的最小运算单元。每个Actor只容许单一线程，这样来说Actor就是一种更细小单位的线程。Akka的编程模式和其内置的线程管理功能使用户能比较自然地实现多线程并发编程。Actor的主要功能就是在单一线程里运算维护它的内部状态，那么它的内部状态肯定是可变的（mutable state），但因为每个Actor都是独立的单一线程运算单元，加上运算是消息驱动的（message-driven），只容许线性流程，Actor之间运算结果互不影响，所以从Akka整体上来讲Actor又好像是纯函数不可变性的（pure immutable）。Actor的内部状态（internal state）与函数式编程不可变集合（immutable collection）的元素差不多，都是包嵌在一个类型内，即F[A] >>> Actor[A]从类型款式来讲很相像，那么我们可否对Actor进行函数组合（functional composition），然后实现函数式编程模式的Akka编程呢？应该是不可能的，因为我们无法对Actor的运算结果进行固定。一是我们无法防止Actor的运算产生副作用，再就是Actor的运算结果是无法预料的，例如它可能把结果发送给任何其它Actor，这样对同样的输入就可以产生不同的结果。我们可以把Actor视作不纯函数（impure function），对同样的输入可能会产生不同的输出结果，如此就无法把对Actor的编程归类为函数式编程了，但Actor编程的确是一种有别于其它编程模式、别具风格的编程模式，而且Akka还有一套领域特定语言DSL，是一种独立的编程模式，即Actor编程模式了。这是一种需要掌握的崭新编程模式。

Akka程序具备了以下的优点：

1、Responsive 快速响应

   以最快时间对用户请求进行回复（响应）

2、Resilient 高容错性

   可以通过对Actor的：

   复制（replication)、

   封闭（containment）、

   分离（isolation）、

   托管（delegation）来应对解决Actor产生的任何程度的错误和异常

3、Elastic 可伸缩性

   通过提升计算机配置的垂直扩展（scale-up）、添加网络中计算机数量的水平扩展（scale-out）等系统拓展能力

   实现在任何负载压力情况下的快速响应

4、Message-driven 消息驱动

   - 异步通信（asynchronous communication）

   - 松散耦合（loosely coupled）

   - 位置透明的Actor定位方式

   - 负载均衡（load management）、流程控制（flow control）、back-pressure

上面所述特点之一的消息驱动模式中提供了位置透明的Actor定位方式，可以简单的通过设定消息接收方地址来实现程序的分布式运算。这点倒是很有趣。

除了普通功能的Actor之外，Akka还提供了几种具有特殊功能的Actor，包括：路由（routingActer）、有限状态机（FSMActor）、持久式（persistenceActor）。其中persistenceActor很有吸引力，它可以通过CQRS模式帮助实现新的数据库操作模式ES（Event-Sourcing）。CQRS模式的基本原理是对数据库的读和写进行分离操作，目的是提高大数据类型网络应用程序的响应。当然，从另一个方面来讲，Event-Sourcing作为一种新的数据库操作模式，应该能解决任何数据库应用软件所普遍面对的数据重演功能缺失，以及数据库使用压力等问题。

初步打算下面的主攻方向是基于persistenceActor的ES模式数据库应用和基于Actor-http的Microservice工具库。当然，希望通过各种努力最终实现让那些不精通Akka的朋友们能方便的编写Actor模式的分布式应用程序。这可能会涉及到对Akka功能的再组合，搭建新的更高层次的抽象框架、提供API等。当然，这些还是需要对Akka进行详细的研究学习后才能有所定论。

刚好，发现在网上的基于scala的Akka讨论示范并不多，那在下面一段时间的讨论里我们就开始从头学习Akka吧。。。

# Akka（1）：Actor - 靠消息驱动的运算器

Akka是由各种角色和功能的Actor组成的，工作的主要原理是把一项大的计算任务分割成小环节，再按各环节的要求构建相应功能的Actor，然后把各环节的运算托付给相应的Actor去独立完成。Akka是个工具库（Tools-Library），不是一个软件架构（Software-Framework），我们不需要按照Akka的框架格式去编写程序，而是直接按需要构建Actor去异步运算一项完整的功能，这样让用户在不知不觉中自然的实现了多线程并发软件编程（concurrent programming）。按这样的描述，Actor就是一种靠消息驱动（Message-driven）的运算器，我们可以直接调用它来运算一段程序。消息驱动模式的好处是可以实现高度的松散耦合（loosely-coupling）,因为系统部件之间不用软件接口，而是通过消息来进行系统集成的。消息驱动模式支持了每个Actor的独立运算环境，又可以在运行时按需要灵活的对系统Actor进行增减，伸缩自如，甚至可以在运行时（runtime）对系统部署进行调配。Akka的这些鲜明的特点都是通过消息驱动来实现的。

曾经看到一个关于Actor模式的观点：认为Actor并不适合并发（concurrency）编程，更应该是维护内部状态的运算工具。听起来好像很无知，毕竟Actor模式本身就是并发模式，如果不适合并发编程，岂不与Akka的发明意愿相左。再仔细研究了一下这个观点的论述后就完全认同了这种看法。在这里我们分析一下这种论述，先看看下面这段Actor用法伪代码：

```scala
 class QueryActor extends Actor {
    override def receive: Receive = {
      case GetResult(query) => 
        val x = db.RunQuery(query)
        val y = getValue(x)
        sender() ! computeResult(x,y)
    }
  }

  val result: Future[Any] = QueryActor ? GetResult(...)
```

这段代码中QueryActor没有任何内部状态。通过Future传递计算结果能实现不阻塞（non-blocking）运算。下面我们用QueryActor来实现并发运算：

```scala
  val r1 = QueryActor ! request1
  val r2 = QueryActor ! request2
  for {
    x <- r1
    y <- r2
  } yield combineValues(x,y)
```

乍眼看r1和r2貌似能实现并行运算，但不要忘记Actor运算环境是单线程的，而Actor信箱又是按序的（Ordered），所以这两个运算只能按顺序运行，最多也就是能在另一个线程里异步进行而已，r1运算始终会阻塞r2的运行。如此还不如直接使用Future，能更好的实现并发程序的并行运算。同样的要求如果用Future来实现的话可以用下面的伪代码：

```scala
  def fuQuery(query: DBQuery): Future[FResult] = Future {
    val x = db.RunQuery(query)
    val y = getValue(x)
    computeResults(x,y)
  } 

  val r1 = fuQuery(query1)
  val r2 = fuQuery(query2)
  for {
    x <- r1
    y <- r2
  } yield combineValues(x,y)
```

在这个例子里r1和r2就真正是并行运算的。从这个案例中我的结论是尽量把Akka Actor使用在需要维护内部状态的应用中。如果为了实现non-blocking只需要把程序分布到不同的线程里运行的话就应该直接用Future，这样自然的多。但使用Future是完全无法维护内部状态的。

好了，回到正题：从功能上Actor是由实例引用（ActorRef），消息邮箱（Mailbox），内部状态（State），运算行为（Behavior），子类下属（Child-Actor），监管策略（Supervision/Monitoring）几部分组成。Actor的物理结构由ActorRef、Actor Instance（runtime实例）、Mailbox、dispatcher（运算器）组成。我们在本篇先介绍一下ActorRef,Mailbox,State和Behavior。

1、ActorRef：Akka系统是一个树形层级式的结构，每个节点由一个Actor代表。每一个Actor在结构中都可以用一个路径（ActorPath）来代表它在系统结构里的位置。我们可以重复用这个路径来构建Actor，但每次构建都会产生新的ActorRef。所以ActorRef是唯一的，代表了某个路径指向位置上的一个运行时的Actor实例，我们只能用ActorRef来向Actor发送消息

2、Mailbox：可以说成是一个运算指令队列（command queque）。Actor从外部接收的消息都是先存放在Mailbox里的。系统默认Mailbox中无限数量的消息是按时间顺序排列的，但用户可以按照具体需要定制Mailbox，比如有限容量信箱、按消息优先排序信箱等。

3、Behavior：简单来说就是对Mailbox里消息的反应方式。Mailbox中临时存放了从外界传来的指令，如何运算这些指令、产生什么结果都是由这些指令的运算函数来确定。所以这些函数的功能就代表着Actor的行为模式。Actor的运算行为可以通过become来替换默认的receive函数，用unbecome来恢复默认行为。

4、State：Actor内部状态，由一组变量值表示。当前内部状态即行为函数最后一次运算所产生的变量值

下面我们就用个例子来示范Actor：模拟一个吝啬人的钱包，他总是会把付出放在最次要的位置。如此我们可以用消息优先排序信箱UnboundedPriorityMailbox来实现。按照Akka程序标准格式，我们先把每个Actor所需要处理的消息和Props构建放在它的伴生对象里：

```scala
  object Wallet {
    sealed trait WalletMsg
    case object ZipUp extends WalletMsg    //锁钱包
    case object UnZip extends WalletMsg    //开钱包
    case class PutIn(amt: Double) extends WalletMsg   //存入
    case class DrawOut(amt: Double) extends WalletMsg //取出 
    case object CheckBalance extends WalletMsg  //查看余额

    def props = Props(new Wallet)   
  }
```

下面是Actor wallet的定义，必须继承Actor以及override receive函数：

```scala
    class Wallet extends Actor {
      import Wallet._
      var balance: Double = 0
      var zipped: Boolean = true

      override def receive: Receive = {
        case ZipUp =>
          zipped = true
          println("Zipping up wallet.")
        case UnZip =>
          zipped = false
          println("Unzipping wallet.")
        case PutIn(amt) =>
          if (zipped) {         
            self ! UnZip         //无论如何都要把钱存入
            self ! PutIn(amt)
          }
          else {
            balance += amt
            println(s"$amt put-in wallet.")
          }

        case DrawOut(amt) =>
          if (zipped)  //如果钱包没有打开就算了
            println("Wallet zipped, Cannot draw out!")
          else
            if ((balance - amt) < 0)
              println(s"$amt is too much, not enough in wallet!")
            else {
            balance -= amt
            println(s"$amt drawn out of wallet.")
          }

        case CheckBalance => println(s"You have $balance in your wallet.")
      }
    }
```

我们可以看到这个Actor的内部状态分别是：var balance, var zipped。下面是定制Mailbox定义：

```scala
  class PriorityMailbox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox (
    PriorityGenerator {
      case Wallet.ZipUp => 0        
      case Wallet.UnZip => 0
      case Wallet.PutIn(_) => 0
      case Wallet.DrawOut(_) => 2
      case Wallet.CheckBalance => 4
      case PoisonPill => 4
      case otherwise => 4
     }
    )
```

PriorityMailbox需要继承UnboundedPriorityMailbox并且提供对比函数PriorityGenerator。ZipUp,UnZip和PutIn都是最优先的。然后在application.conf登记dispatcher的配置：

```scala
prio-dispatcher {
  mailbox-type = "PriorityMailbox"
}
```

下面的代码可以用来试运行Actor wallet：

```scala
object Actor101 extends App {
  val system = ActorSystem("actor101-demo",ConfigFactory.load)
  val wallet = system.actorOf(Wallet.props.withDispatcher(
    "prio-dispatcher"),"mean-wallet")

  wallet ! Wallet.UnZip
  wallet ! Wallet.PutIn(10.50)
  wallet ! Wallet.PutIn(20.30)
  wallet ! Wallet.DrawOut(10.00)
  wallet ! Wallet.ZipUp
  wallet ! Wallet.PutIn(100.00)
  wallet ! Wallet.CheckBalance

  Thread.sleep(1000)
  system.terminate()
  
}
```

由于需要解析application.conf里的配置，所以使用了ActorSystem(name, config)方式。构建Actor时用.withDispatcher把application.conf里的dispatcher配置prio-dispatcher传入。

运算的结果如下：

```scala
Unzipping wallet.
10.5 put-in wallet.
20.3 put-in wallet.
100.0 put-in wallet.
Zipping up wallet.
Wallet zipped, Cannot draw out!
You have 130.8 in your wallet.

Process finished with exit code 0
```

下面是本次示范的完整代码：

application.conf：

```scala
prio-dispatcher {
  mailbox-type = "PriorityMailbox"
}
```

Actor101.scala：

```scala
import akka.actor._
import akka.dispatch.PriorityGenerator
import akka.dispatch.UnboundedPriorityMailbox
import com.typesafe.config._

  object Wallet {
    sealed trait WalletMsg
    case object ZipUp extends WalletMsg    //锁钱包
    case object UnZip extends WalletMsg    //开钱包
    case class PutIn(amt: Double) extends WalletMsg   //存入
    case class DrawOut(amt: Double) extends WalletMsg //取出
    case object CheckBalance extends WalletMsg  //查看余额

    def props = Props(new Wallet)
  }


  class PriorityMailbox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox (
    PriorityGenerator {
      case Wallet.ZipUp => 0
      case Wallet.UnZip => 0
      case Wallet.PutIn(_) => 0
      case Wallet.DrawOut(_) => 2
      case Wallet.CheckBalance => 4
      case PoisonPill => 4
      case otherwise => 4
     }
    )

    class Wallet extends Actor {
      import Wallet._
      var balance: Double = 0
      var zipped: Boolean = true

      override def receive: Receive = {
        case ZipUp =>
          zipped = true
          println("Zipping up wallet.")
        case UnZip =>
          zipped = false
          println("Unzipping wallet.")
        case PutIn(amt) =>
          if (zipped) {
            self ! UnZip         //无论如何都要把钱存入
            self ! PutIn(amt)
          }
          else {
            balance += amt
            println(s"$amt put-in wallet.")
          }

        case DrawOut(amt) =>
          if (zipped)  //如果钱包没有打开就算了
            println("Wallet zipped, Cannot draw out!")
          else
            if ((balance - amt) < 0)
              println(s"$amt is too much, not enough in wallet!")
            else {
            balance -= amt
            println(s"$amt drawn out of wallet.")
          }

        case CheckBalance => println(s"You have $balance in your wallet.")
      }
    }

object Actor101 extends App {
  val system = ActorSystem("actor101-demo",ConfigFactory.load)
  val wallet = system.actorOf(Wallet.props.withDispatcher(
    "prio-dispatcher"),"mean-wallet")

  wallet ! Wallet.UnZip
  wallet ! Wallet.PutIn(10.50)
  wallet ! Wallet.PutIn(20.30)
  wallet ! Wallet.DrawOut(10.00)
  wallet ! Wallet.ZipUp
  wallet ! Wallet.PutIn(100.00)
  wallet ! Wallet.CheckBalance

  Thread.sleep(1000)
  system.terminate()

}
```