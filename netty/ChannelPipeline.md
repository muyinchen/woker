## ChannelPipeline

Netty的ChannelPipeline和ChannelHandler机制类似于Servlet和Filter过滤器，这类拦截器实际上是职责链模式的一种变形，主要是为了方便事件的拦截和用户业务逻辑的定制。Netty的Channel过滤器实现原理与Servlet Filter机制一致，它将Channel的数据管道抽象为ChannelPipeline，消息在ChannelPipeline中流动和传递。ChannelPipeline持有I/O事件拦截器ChannelHandler的链表，由ChannelHandler对I/O事件进行拦截和处理，可以方便地通过新增和删除ChannelHandler来实现不同的业务逻辑定制，不需要对已有的ChannelHandler进行修改，能够实现对修改封闭和对扩展的支持。

 ChannelPipeline功能说明

ChannelPipeline是ChannelHandler的容器，它负责ChannelHandler的管理和事件拦截与调度。

### ChannelPipeline的事件处理

一个消息被ChannelPipeline的ChannelHandler链拦截和处理的全过程：

（1）底层的SocketChannel read()方法读取ByteBuf，触发ChannelRead事件，由I/O线程NioEventLoop调用ChannelPipeline的fireChannelRead(Object msg)方法，将消息（ByteBuf）传输到ChannelPipeline中；

（2）消息依次被HeadHandler、ChannelHandler1、ChannelHandler2……TailHandler拦截和处理，在这个过程中，任何ChannelHandler都可以中断当前的流程，结束消息的传递；

（3）调用ChannelHandlerContext的write方法发送消息，消息从TailHandler开始，途经ChannelHandlerN……ChannelHandler1、HeadHandler，最终被添加到消息发送缓冲区中等待刷新和发送，在此过程中也可以中断消息的传递，例如当编码失败时，就需要中断流程，构造异常的Future返回。

Netty中的事件分为inbound事件和outbound事件。inbound事件通常由I/O线程触发，例如TCP链路建立事件、链路关闭事件、读事件、异常通知事件等。

触发inbound事件的方法如下。

（1）ChannelHandlerContext.fireChannelRegistered()：Channel注册事件；

（2）ChannelHandlerContext.fireChannelActive()：TCP链路建立成功，Channel激活事件；

（3）ChannelHandlerContext.fireChannelRead(Object)：读事件；

（4）ChannelHandlerContext.fireChannelReadComplete()：读操作完成通知事件；

（5）ChannelHandlerContext.fireExceptionCaught(Throwable)：异常通知事件；

（6）ChannelHandlerContext.fireUserEventTriggered(Object)：用户自定义事件；

（7）ChannelHandlerContext.fireChannelWritabilityChanged()：Channel的可写状态变化通知事件；

（8）ChannelHandlerContext.fireChannelInactive()：TCP连接关闭，链路不可用通知事件。

Outbound事件通常是由用户主动发起的网络I/O操作，例如用户发起的连接操作、绑定操作、消息发送等操作，它对应图17-1的右半部分。

触发outbound事件的方法如下：

（1）ChannelHandlerContext.bind(SocketAddress, ChannelPromise)：绑定本地地址事件；

（2）ChannelHandlerContext.connect(SocketAddress, SocketAddress, ChannelPromise)：连接服务端事件；

（3）ChannelHandlerContext.write(Object, ChannelPromise)：发送事件；

（4）ChannelHandlerContext.flush()：刷新事件；

（5）ChannelHandlerContext.read()：读事件；

（6）ChannelHandlerContext.disconnect(ChannelPromise)：断开连接事件；

（7）ChannelHandlerContext.close(ChannelPromise)：关闭当前Channel事件。

### 自定义拦截器

ChannelPipeline通过ChannelHandler接口来实现事件的拦截和处理，由于ChannelHandler中的事件种类繁多，不同的ChannelHandler可能只需要关心其中的某一个或者几个事件，所以，通常ChannelHandler只需要继承ChannelHandlerAdapter类覆盖自己关心的方法即可。

例如，下面的例子展示了拦截Channel Active事件，打印TCP链路建立成功日志，代码如下：



```java
public class MyInboundHandler extends ChannelHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("TCP connected!");
        ctx.fireChannelActive();
    }
}
```



### 构建pipeline

事实上，用户不需要自己创建pipeline，因为使用ServerBootstrap或者Bootstrap启动服务端或者客户端时，Netty会为每个Channel连接创建一个独立的pipeline。对于使用者而言，只需要将自定义的拦截器加入到pipeline中即可。

```java
pipeline = ch.pipeline();
pipeline.addLast("decoder", new MyProtocolDecoder());
pipeline.addLast("encoder", new MyProtocolEncoder());
```

对于类似编解码这样的ChannelHandler，它存在先后顺序，例如MessageToMessageDecoder，在它之前往往需要有ByteToMessageDecoder将ByteBuf解码为对象，然后对对象做二次解码得到最终的POJO对象。

### ChannelPipeline的主要特性

ChannelPipeline支持运行态动态的添加或者删除ChannelHandler，在某些场景下这个特性非常实用。例如当业务高峰期需要对系统做拥塞保护时，就可以根据当前的系统时间进行判断，如果处于业务高峰期，则动态地将系统拥塞保护ChannelHandler添加到当前的ChannelPipeline中，当高峰期过去之后，就可以动态删除拥塞保护ChannelHandler了。

ChannelPipeline是线程安全的，这意味着N个业务线程可以并发地操作ChannelPipeline而不存在多线程并发问题。但是，ChannelHandler却不是线程安全的，这意味着尽管ChannelPipeline是线程安全的，但是用户仍然需要自己保证ChannelHandler的线程安全。



## ChannelPipeline源码分析

ChannelPipeline的代码相对比较简单，它实际上是一个ChannelHandler的容器，内部维护了一个ChannelHandler的链表和迭代器，可以方便地实现ChannelHandler查找、添加、替换和删除。

### ChannelPipeline对ChannelHandler的管理

ChannelPipeline是ChannelHandler的管理容器，负责ChannelHandler的查询、添加、替换和删除，它与Map等容器的实现非常类似。

由于ChannelPipeline支持运行期动态修改，在调用类似addBefore（ChannelHandlerInvoker invoker, String baseName, final String name, ChannelHandler handler）方法时，存在两种潜在的多线程并发访问场景。

1. I/O线程和用户业务线程的并发访问；
2. 用户多个线程之间的并发访问。

为了保证ChannelPipeline的线程安全性，需要通过线程安全容器或者锁来保证并发访问的安全，此处Netty直接使用了synchronized关键字，保证同步块内的所有操作的原子性。首先根据baseName获取它对应的DefaultChannelHandlerContext，ChannelPipeline维护了ChannelHandler名和ChannelHandlerContext实例的映射关系。  新增的ChannelHandler名进行重复性校验，如果已经有同名的ChannelHandler存在，则不允许覆盖，抛出IllegalArgumentException("Duplicate handler name: " + name)异常。校验通过之后，使用新增的ChannelHandler等参数构造一个新的DefaultChannelHandlerContext实例。将新创建的DefaultChannelHandlerContext添加到当前的pipeline中(首先需要对添加的ChannelHandlerContext做重复性校验,如果ChannelHandler不是可以在多个ChannelPipeline中共享的，且已经被添加到ChannelPipeline中，则抛出ChannelPipelineException异常。)，加入成功之后，缓存ChannelHandlerContext，发送新增ChannelHandlerContext通知消息。

![img](990532-20161224164013807-193335423.png)

### ChannelPipeline的inbound事件

当发生某个I/O事件的时候，例如链路建立、链路关闭、读取操作完成等，都会产生一个事件，事件在pipeline中得到传播和处理，它是事件处理的总入口。由于网络I/O相关的事件有限，因此Netty对这些事件进行了统一抽象，Netty自身和用户的ChannelHandler会对感兴趣的事件进行拦截和处理。

pipeline中以fireXXX命名的方法都是从IO线程流向用户业务Handler的inbound事件，它们的实现因功能而异，但是处理步骤类似，总结如下。

（1）调用HeadHandler对应的fireXXX方法；

（2）执行事件相关的逻辑操作。

以fireChannelActive方法为例，调用head.fireChannelActive()之后，判断当前的Channel配置是否自动读取，如果为真则调用Channel的read方法



```
    DefaultChannelPipeline

    @Override
    public ChannelPipeline fireChannelActive() {
        head.fireChannelActive();

        if (channel.config().isAutoRead()) {
            channel.read();
        }

        return this;
    }
```



### ChannelPipeline的outbound事件

由用户线程或者代码发起的I/O操作被称为outbound事件，事实上inbound和outbound是Netty自身根据事件在pipeline中的流向抽象出来的术语，在其他NIO框架中并没有这个概念。

Pipeline本身并不直接进行I/O操作，最终都是由Unsafe和Channel来实现真正的I/O操作的。Pipeline负责将I/O事件通过HeadHandler进行调度和传播，最终调用Unsafe的I/O方法进行I/O操作。最终由TailHandler调用Unsafe的connect方法发起真正的连接，pipeline仅仅负责事件的调度。



```java
DefaultChannelPipeline

    @Override
    public ChannelPipeline fireChannelRegistered() {
        head.fireChannelRegistered();
        return this;
    }

    /**
     * Removes all handlers from the pipeline one by one from tail (exclusive) to head (inclusive) to trigger
     * handlerRemoved().  Note that the tail handler is excluded because it's neither an outbound handler nor it
     * does anything in handlerRemoved().
     */
    private void teardownAll() {
        tail.prev.teardown();
    }

    @Override
    public ChannelPipeline fireChannelActive() {
        head.fireChannelActive();

        if (channel.config().isAutoRead()) {
            channel.read();
        }

        return this;
    }

    @Override
    public ChannelPipeline fireChannelInactive() {
        head.fireChannelInactive();
        teardownAll();
        return this;
    }

    @Override
    public ChannelPipeline fireExceptionCaught(Throwable cause) {
        head.fireExceptionCaught(cause);
        return this;
    }

    @Override
    public ChannelPipeline fireUserEventTriggered(Object event) {
        head.fireUserEventTriggered(event);
        return this;
    }

    @Override
    public ChannelPipeline fireChannelRead(Object msg) {
        head.fireChannelRead(msg);
        return this;
    }

    @Override
    public ChannelPipeline fireChannelReadComplete() {
        head.fireChannelReadComplete();
        if (channel.config().isAutoRead()) {
            read();
        }
        return this;
    }

    @Override
    public ChannelPipeline fireChannelWritabilityChanged() {
        head.fireChannelWritabilityChanged();
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return tail.bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return tail.connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return tail.connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect() {
        return tail.disconnect();
    }

    @Override
    public ChannelFuture close() {
        return tail.close();
    }

    @Override
    public ChannelPipeline flush() {
        tail.flush();
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return tail.bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return tail.connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return tail.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return tail.disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return tail.close(promise);
    }

    @Override
    public ChannelPipeline read() {
        tail.read();
        return this;
    }

    @Override
    public ChannelFuture write(Object msg) {
        return tail.write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return tail.write(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return tail.writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return tail.writeAndFlush(msg);
    }
```