# 12 NIO的Selector机制 之 Selector.open()

## Selector机制之Selector.open()函数的解析
在NIO中我们一般都是Channel与Selector配合使用的，一般情况下使用的方法如下：

``` java
//打开Selector来处理channel
Selector selector = Selector.open();
//将channel注册到selector中,并将channel设置成等待新的连接
serverChannel.register(selector,SelectionKey.OP_ACCEPT);
//等待处理新的事件;一直阻塞直到下一个事件到来才唤醒.此方法执行处于阻塞模式的选择操作。仅在至少选择一个通道、调用此选择器的 wakeup 方法，或者当前的线程已中断（以先到者为准）后此方法才返回。
selector.select();
```
这篇文章我们主要从Selector.open()函数开始，一步步分析Selector机制。

首先我们进入Selector.open();函数，在JDK源码中定义如下：
``` java
public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }
```
查看这个函数的doc文档注释： 
The new selector is created by invoking the openSelector method of the system-wide default SelectorProvider object. 
意思是：通过调用系统默认的SelectorProvider(这里不同的系统会有不同的SelectorProvider实现类)的openSelector()方法来创建新的selector。

这里我们首先分析一下： 
SelectorProvider.provider(); 
进入SelectorProvider这个类的静态方法provider()，JDK的源码如下：

``` java
public static SelectorProvider provider() {
        /**
          *加锁，锁的定义是：private static final Object lock = new Object();
          */
        synchronized (lock) {
            /**
              *provider的定义是：private static SelectorProvider provider = null;
              */
            if (provider != null)
                return provider;
            return AccessController.doPrivileged(
                new PrivilegedAction<SelectorProvider>() {
                    public SelectorProvider run() {
                            if (loadProviderFromProperty())
                                return provider;
                            if (loadProviderAsService())
                                return provider;
                            provider = sun.nio.ch.DefaultSelectorProvider.create();
                            return provider;
                        }
                    });
        }
    }
```
还是先来阅读一下Javadoc中关于这个函数的功能描述：通过Java虚拟机的调用返回系统默认的selector provider。 
注意：我们发现同步代码段中首先的就是一个判断：
``` java
if (provider != null)
    return provider;
```
这个地方判断provider在当前进程是否已经被实例化过了，如果已经被实例化过了，那么就直接返回当前provider，不再执行后面的代码；否者就执行后面的代码实例化provider。这里就是一个最简单的 单例模式 的应用。

继续分析后面的代码：后面是一个AccessController.doPrivileged()这个貌似是与权限相关的，不是本文的重点，暂时不去分析这个。我们分析后面的run()方法里面的内容：
``` java
if (loadProviderFromProperty())
   return provider;
if (loadProviderAsService())
   return provider;
provider = sun.nio.ch.DefaultSelectorProvider.create();
return provider;
```
loadProviderFromProperty()这个函数判断如果系统属性java.nio.channels.spi.SelectorProvider 已经被定义了，则该属性名看作具体提供者类的完全限定名。加载并实例化该类；如果此进程失败，则抛出未指定的错误。 
loadProviderAsService()这个函数判断：如果在对系统类加载器可见的 jar 文件中安装了提供者类，并且该 jar 文件包含资源目录 META-INF/services 中名为 java.nio.channels.spi.SelectorProvider 的提供者配置文件，则采用在该文件中指定的第一个类名称。加载并实例化该类；如果此进程失败，则抛出未指定的错误。 
最后，如果未通过上述的方式制定任何provider，则实例化系统默认的provider并返回该结果(一般情况下，都是这种情况。)

这个地方需要注意的是：这里系统默认的provider在不同系统上是不一样的，下面用一个表格来表示：

| 系统    | provider               | 备注 |
| ------- | ---------------------- | ---- |
| MacOSX  | KQueueSelectorProvider | 无   |
| Linux   | 。                     | 无   |
| Windows | 。                     | 无   |

进入sun.nio.ch.DefaultSelectorProvider.create(); 
这里系统会根据不同的操作系统返回不同的provider；我们来看一下系统默认的provider；由于我用的是Mac-OSX 的系统进入之后源码如下：
``` java
public static SelectorProvider create() {
    return new KQueueSelectorProvider();
}
```
所以OSX默认的provider是KQueueSelectorProvider。继续进入KQueueSelectorProvider的构造函数，源码如下：
``` java
public KQueueSelectorProvider() {
}
```
这是一个空的构造函数，所以SelectorProvider.provider(); 
最后返回的就是KQueueSelectorProvider；下面给个示图显示类的继承关系： 

![](https://github.com/muyinchen/woker/blob/master/mypics/KQueueSelectorProvider.jpg?raw=true)

我们继续看SelectorProvider.provider().openSelector(); 
后面调用的也就是KQueueSelectorProvider.openSelector();源码如下：

``` java
public AbstractSelector openSelector() throws IOException {
    return new KQueueSelectorImpl(this);
}
```
所以最后返回的就是KQueueSelectorImpl这个实现类的实例。我们看一下这个类的继承关系图： 

![](https://github.com/muyinchen/woker/blob/master/mypics/20161205164730067.png?raw=true)


下面就是分析KQueueSelectorImpl的构造函数，来窥探一下selector的运行原理：
```java
//构造函数
KQueueSelectorImpl(SelectorProvider var1) {
    //调用父类的构造函数，传入的是一个SelectorProvider，在Mac上是KQueueSelectorProvider
    super(var1);
    long var2 = IOUtil.makePipe(false);//native方法
    this.fd0 = (int)(var2 >>> 32);//高32位存放的是Pipe管道的读端的文件描述符
    this.fd1 = (int)var2;//低32位存放的是Pipe管道的写端的文件描述符
    this.kqueueWrapper = new KQueueArrayWrapper();
    this.kqueueWrapper.initInterrupt(this.fd0, this.fd1);
    this.fdMap = new HashMap();
    this.totalChannels = 1;
}
```
IOUtil.makePipe(false); 是一个static native方法，所以我们没办法查看源码。但是我们可以知道该函数返回了一个非堵塞的管道(pipe),底层是通过Linux的pipe系统调用实现的；创建了一个管道pipe并返回了一个64为的long型整数，该数的高32位存放了该管道读端的文件描述符，低32位存放了该pipe的写端的文件描述符。

下面又创建了一个KQueueArrayWrapper对象，我们把重点放在下面的this.kqueueWrapper.initInterrupt(this.fd0, this.fd1);函数，这里把管道的读写两端的文件描述符作为参数传入，我们先来看看源码：
``` java
void initInterrupt(int var1, int var2) {
//private int outgoingInterruptFD;
//private int incomingInterruptFD;
    this.outgoingInterruptFD = var2;//写端
    this.incomingInterruptFD = var1;//读端
    this.register0(this.kq, var1, 1, 0);
```
其中的register0(this.kq, var1, 1, 0);又是一个native方法。initInterrup方法，从该方法初步判断，管道应该和中断有关系；

最后定义了一个map类型的变量fdToKey,将channel的文件描述符和SelectionKey建立映射关系；

最后总结一下Selector.open()干了啥： 
主要完成建立Pipe，并把pipe的读写文件描述符放入pollArray中,这个pollArray是Selector的枢纽。Linux下则是直接使用系统的pipe。 
下面附图展示Selector工作原理： 
![](https://github.com/muyinchen/woker/blob/master/mypics/timg.jpg?raw=true)

![](https://github.com/muyinchen/woker/blob/master/mypics/selector%20%E6%95%B4%E4%BD%93%E5%9B%BE.jpg?raw=true)

