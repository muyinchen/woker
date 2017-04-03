# dubbo源码分析系列

[知秋](https://github.com/muyinchen)整理

原文博客:https://my.oschina.net/ywbrj042/home

更多精彩学习内容，请关注本人[github](https://github.com/muyinchen)

# 一 项目工程结构介绍

> 摘要: dubbo是目前国内最流行的分布式服务框架，已经俨然成为行业的标准了，多数无自研能力的公司都在使用这个框架，而且这个框架本身非常具有代表性，即使很多公司自研的分布式服务框架也都是对dubbo的扩展或者借鉴了该框架，因此研究它的源码意义还是非常大的，对于掌握分布式服务框架的原理和实现细节有着非常好的帮助。
>

## 项目源码地址

本系列文章是基于当当网维护的dubbox版本进行分析的，源码地址参考：[https://github.com/dangdangdotcom/dubbox](https://github.com/dangdangdotcom/dubbox)

## 项目源码结构

![img](http://static.oschina.net/uploads/space/2016/0529/075812_9BOB_113011.png)![img](http://static.oschina.net/uploads/space/2016/0529/075836_dizN_113011.png)

![img](http://static.oschina.net/uploads/space/2016/0529/075851_QqDb_113011.png)

我们下载源码后导入到ide中可以看到如此之多的项目组成，真的是非常之多的项目组成，而且用到的技术也非常多，redis、zookeeper，netty等等，不禁让人心生畏惧，这么大的工程源码该如何下手阅读呢？我们必须理清楚这些工程的职责和关系，才能够抽丝剥茧的把dubbo搞清楚。

## 核心项目间关系

![img](http://static.oschina.net/uploads/space/2016/0529/081455_uMiG_113011.png)

### 结构图说明

为了让图保持清晰明了，该图特意忽略了一些具体的api实现模块，只保留了核心的一些借口定义模块，而具体技术实现的模块忽略了。通过分析dubbo的各项目及组成关系分析出该项目如下特点。

1. duubo的项目划分得非常细。因此导致工程数目达到几十个之多，这样的好处就是开发者可以根据自己的需求去编译符合需求的包，只保留自己需要用到的模块。缺点是导致工程接口的非常复杂。
2. 强大的可扩展性和灵活性。dubbo抽象出了很多核心接口，而针对这些接口都提供了不同的实现了，开发者可以根据自己的需要选择各种实现，也可以根据需要扩展开发自己的实现，具有非常强大的扩展和定制能力。

### 核心模块职责介绍

#### dubbo-common

通用模块，定义了几乎所有dubbo模块都会使用到的一些通用与业务领域无关的工具类（io处理、日志处理、配置处理、类处理等等），线程池扩展、二进制代码处理、class编译处理、json处理、数据存储接口，系统版本号等等通用的类和接口。

#### dubbo-rpc-api

分布式服务框架的核心是rpc，这是最基本的功能，这个模块定义了rpc的一些抽象的rpc接口和实现类，包括服务发布，服务调用代理，远程调用结果及异常，rpc调用网络协议，rpc调用监听器和过滤器等等。该模块提供了默认的基于dubbo协议的实现模块，还提供了hessian、http、rest、rmi、thrift和webservice等协议的实现，还实现了injvm的本地调用实现，灵活性强，非常通用，能够满足绝大多数项目的使用需求，而且还可以自行实现rpc协议。

#### dubbo-registry-api

注册中心也是最重要的组成部分，它是rpc中的consumer和provider两个重要角色的协调者。该项目定义了核心的注册中心接口和实现。具体实现留给了其它项目。有一个默认的实现模块，组册中心提供了mutilcast、redis和zookeeper等多种方式的注册中心实现，用于不同的使用场景。

#### dubbo-remoting-api

该模块是dubbo中的远程通讯模块。rpc的实现基础就是远程通讯，consmer要调用provider的远程方法必须通过网络远程通讯实现。该模块定义了远程传输器、终端（endpoint）、客户端、服务端、编码解码器、数据交换、缓冲区、通讯异常定义等等核心的接口及类构成。他是对于远程网络通讯的抽象。提供了诸如netty、mina、grizzly、http、p2p和zookeeper的协议和技术框架的实现方式。

#### dubbo-monitor-api

该模块是dubbo的监控模块，通过该模块可以监控服务调用的各种信息，例如调用耗时、调用量、调用结果等等，监控中心在调用过程中收集调用的信息，发送到监控服务，在监控服务中可以存储这些信息，对这些数据进行统计分析，最终可以产生各种维护的调用监控信息。dubbo默认提供了一个实现，该实现非常简单，只是作为默认的实现范例，生产环境使用价值不高，需要自行实现自己的监控。

#### dubbo-container-api

dubbo服务运行容器api模块。定义了启动容器列表的包含应用程序入口main方法的类Main；定义了容器接口Container，该接口包含了启动和停止方法定义；还有一些通用的分页功能的相关类。dubbo内置了javaconfig、jetty、log4j、logback和spring几种容器的实现。

#### dubbo-config-api

从图中可以看出改模块依赖了几乎所有的其它模块，他是dubbo的配置模块，通过它的配置和组装将dubbo组件的多个模块整合在一起给最终的开发者提供有价值的分布式服务框架。通过它的配置可以让开发者选择符合自己需求和使用场景的模块和技术，它定义了面向dubbo使用者的各种信息配置，比如服务发布配置、方法发布配置、服务消费配置、应用程序配置、注册中心配置、协议配置、监控配置等等。另外还有一个spring的配置模块，定义了一些spring的XML Schema，能够大大简化使用dubbo的配置，可以大大降低spring使用场景的学习和配置成本。

#### dubbo-cluster

该模块是dubbo实现的集群模块。支持远程服务的集群，支持多种集群调用策略，包括failover,failsafe,failfast,failback,forking等。并且支持目录服务，注册中心就是目录服务的一种实现，支持负载均衡，该模块还实现了路由器特性，此外还包括合并技术，当将调用请求分发给所有的服务提供者，则会返回多个结果，则将多个结果合并需要用到合并器的实现，该模块也是非常重要的一个模块。

#### dubbo-admin

该项目是一个web应用，可以独立部署，它可以管理dubbo服务，通过该管理应用可以连接注册中心，重点是读取注册中心中的信息，也可以通过该应用改写注册中心的信息，从而实现动态的管控服务。该模块的功能也非常简单，对于实际的生产使用场景，还需要对该应用的功能进行扩展和定制，以满足实际的使用场景。

## 总结

通过分析项目结构，让我们对dubbo有了一个全面系统的了解，尤其是对于它的组成结构，但是这只是一个开始，对于细节的掌握，我们还需要逐层深入，进入到每个具体的项目和类中去探索它的实现细节。

我们可以看出来dubbo是一个设计比较精良的项目，它的项目和代码组织结构合理；并且它又是一个非常庞大的项目，涉及的具体技术包含方方面面，如果不进行良好的设计，那项目的质量也无法保障，也要求它必须进行良好的设计；同时它又是一个非常通用和灵活的框架项目，该项目定义了大量的api和配置，也提供了很多框架和技术的实现方案，因此它能够适应绝大多数的使用场景，另外又具有非常灵活的配置和扩展能力，因此也能够非常方便地扩展实现以满足自己个性化的需求，它的广泛流行也是有其必然原因的。

从它的设计中又印证了我一直比较认同的一种设计模式。就是分层设计模型：

底层是一些通用和支撑性的通用模块，比如dubbo-commom、dubbo-remoting-api，它们是非常通用的，不解决任何领域业务问题，可使用于全部或者多数业务领域模块；

中间层是核心业务领域模块。它们相互组合协作完成一个完整的对使用者有价值的功能特性，分布式服务框架里面就是dubbo-rpc-api远程调用、dubbo-registry-api是注册中心、dubbo-monitor-api是监控中心和dubbo-container-api等。模块化设计降低了代码复杂度和缩小了关注焦点，也有利于分工协作，还方便各模块能够独立演变；结构化良好的中间层才能够灵活应对各种技术和业务需求的演变。

上层是用户界面模块。该层会将结构化良好的中间层服务进行配置和组装，整合成为一个对用户有价值的打包服务，在该层要考虑的是用户体验，降低用户的学习和使用成本，这就需要面向用户类型、用户使用场景进行设计，用户类型需要了解用户掌握的知识，环境和使用场景，依据这些特性进行设计，面向原生java环境使用dubbo-config-api模块去配置使用dubbo，面向使用spring环境则使用dubbo-config-spring模块，该层是主要是为了降低用户的学习使用成本，填补内部结构和用户之间的知识、技能方面的鸿沟。

## dubbo整体架构和流程图

![img](http://static.oschina.net/uploads/space/2016/0610/074655_xt6O_113011.jpg)![img](http://static.oschina.net/uploads/space/2016/0610/074707_jEcU_113011.jpg)

## next

dubbo-rpc-api是最为核心的模块，我们将从该模块入手来研究。



# 二 dubbo-rpc-api模块源码分析

> 摘要: dubbo-rpc-api模块是dubbo最为核心的一个模块，它定义了dubbo作为一个rpc框架最核心的一些接口和抽象实现，因此掌握这些内容对于学习其它部分源码有着极其重要的意义。
>

## 简化的类图

![img](http://static.oschina.net/uploads/space/2016/0529/204734_9Xv8_113011.png)

该图是经过简化后的rpc-api模块的类图，去除了一些非关键的属性和方法定义，也去除了一些非核心的类和接口，只是一个简化了的的示意图，这样大家能够去除干扰看清楚该模块的核心接口极其关系，**请点击看大图更清晰一些**。

## 核心类说明

### Protocol

服务协议。这是rpc模块中最核心的一个类，它定义了rpc的最主要的两个行为即：1、provider暴露远程服务，即将调用信息发布到服务器上的某个URL上去，可以供消费者连接调用，一般是将某个service类的全部方法整体发布到服务器上。2、consumer引用远程服务，即根据service的服务类和provider发布服务的URL转化为一个Invoker对象，消费者可以通过该对象调用provider发布的远程服务。这其实概括了rpc的最为核心的职责，提供了多级抽象的实现、包装器实现等。

### AbstractProtocol

Protocol的顶层抽象实现类，它定义了这些属性：1、exporterMap表示发布过的serviceKey和Exporter（远程服务发布的引用）的映射表；2、invokers是一个Invoker对象的集合，表示层级暴露过远程服务的服务执行体对象集合。还提供了一个通用的服务发布销毁方法destroy，该方法是一个通用方法，它清空了两个集合属性，调用了所有invoker的destroy方法，也调用所有exporter对象的unexport方法。

### AbstractProxyProtocol

继承自AbstractProtoco的一个抽象代理协议类。它聚合了代理工厂ProxyFactory对象来实现服务的暴露和引用。它的源码如下。

```java
/*
 * Copyright 1999-2012 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.dubbo.rpc.protocol;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * AbstractProxyProtocol
 * 
 * @author william.liangf
 */
public abstract class AbstractProxyProtocol extends AbstractProtocol {

    private final List<Class<?>> rpcExceptions = new CopyOnWriteArrayList<Class<?>>();;

    private ProxyFactory proxyFactory;

    public AbstractProxyProtocol() {
    }

    public AbstractProxyProtocol(Class<?>... exceptions) {
        for (Class<?> exception : exceptions) {
            addRpcException(exception);
        }
    }

    public void addRpcException(Class<?> exception) {
        this.rpcExceptions.add(exception);
    }

    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    @SuppressWarnings("unchecked")
	public <T> Exporter<T> export(final Invoker<T> invoker) throws RpcException {
        final String uri = serviceKey(invoker.getUrl());//获得Url对应的serviceKey值。
        Exporter<T> exporter = (Exporter<T>) exporterMap.get(uri);//根据url获取对应的exporter。
        if (exporter != null) {//如果已经存在，则直接返回，实现接口支持幂等调用。该处难道无须考虑线程安全问题吗？
        	return exporter;
        }
        //执行抽放方法暴露服务。runnable方法的行为有什么约束没有？该处不明确。
        final Runnable runnable = doExport(proxyFactory.getProxy(invoker), invoker.getInterface(), invoker.getUrl());
        //调用proxyFactory.getProxy(invoker)来获得invoker的代理对象。
        exporter = new AbstractExporter<T>(invoker) {
            public void unexport() {
                super.unexport();
                exporterMap.remove(uri);
                if (runnable != null) {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        logger.warn(t.getMessage(), t);
                    }
                }
            }
        };
        exporterMap.put(uri, exporter);
        return exporter;
    }

    public <T> Invoker<T> refer(final Class<T> type, final URL url) throws RpcException {
        //先调用doRefer获得服务服务对象，再调用proxyFactory.getInvoker获得invoker对象。
        final Invoker<T> tagert = proxyFactory.getInvoker(doRefer(type, url), type, url);
        Invoker<T> invoker = new AbstractInvoker<T>(type, url) {
            @Override
            protected Result doInvoke(Invocation invocation) throws Throwable {
                try {
                    Result result = tagert.invoke(invocation);
                    Throwable e = result.getException();
                    if (e != null) {
                        for (Class<?> rpcException : rpcExceptions) {
                            if (rpcException.isAssignableFrom(e.getClass())) {
                                throw getRpcException(type, url, invocation, e);
                            }
                        }
                    }
                    return result;
                } catch (RpcException e) {
                    if (e.getCode() == RpcException.UNKNOWN_EXCEPTION) {
                        e.setCode(getErrorCode(e.getCause()));
                    }
                    throw e;
                } catch (Throwable e) {
                    throw getRpcException(type, url, invocation, e);
                }
            }
        };
        invokers.add(invoker);
        return invoker;
    }

    protected RpcException getRpcException(Class<?> type, URL url, Invocation invocation, Throwable e) {
        RpcException re = new RpcException("Failed to invoke remote service: " + type + ", method: "
                + invocation.getMethodName() + ", cause: " + e.getMessage(), e);
        re.setCode(getErrorCode(e));
        return re;
    }

    protected int getErrorCode(Throwable e) {
        return RpcException.UNKNOWN_EXCEPTION;
    }

    /**
     **留给子类实现的真正将类发布到URL上的抽象方法定义，由具体的协议来实现。 
    **/
    protected abstract <T> Runnable doExport(T impl, Class<T> type, URL url) throws RpcException;

    /**
     **留给子类实现的引用远程服务的抽象方法定义，该方法是将URL和type接口类应用到一个可以远程调用代理对象。
     **/
    protected abstract <T> T doRefer(Class<T> type, URL url) throws RpcException;

}
```

### ProtocolFilterWrapper

是一个Protocol的支持过滤器的装饰器。通过该装饰器的对原始对象的包装使得Protocol支持可扩展的过滤器链，已经支持的包括ExceptionFilter、ExecuteLimitFilter和TimeoutFilter等多种支持不同特性的过滤器。

```
  private static <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker, String key, String group) {
        Invoker<T> last = invoker;
        //通过该句获得扩展配置的过滤器列表，具体机制需要研究该类的实现。
        List<Filter> filters = ExtensionLoader.getExtensionLoader(Filter.class).getActivateExtension(invoker.getUrl(), key, group);
        if (filters.size() > 0) {
            //循环将过滤器列表组装成为过滤器链，目标invoker是最后一个执行的。
            for (int i = filters.size() - 1; i >= 0; i --) {
                final Filter filter = filters.get(i);
                final Invoker<T> next = last;
                last = new Invoker<T>() {

                    public Class<T> getInterface() {
                        return invoker.getInterface();
                    }

                    public URL getUrl() {
                        return invoker.getUrl();
                    }

                    public boolean isAvailable() {
                        return invoker.isAvailable();
                    }

                    public Result invoke(Invocation invocation) throws RpcException {
                        return filter.invoke(next, invocation);
                    }

                    public void destroy() {
                        invoker.destroy();
                    }

                    @Override
                    public String toString() {
                        return invoker.toString();
                    }
                };
            }
        }java
        return last;
    }
```

### ProtocolListenerWrapper

一个支持监听器特性的Protocal的包装器。支持两种监听器的功能扩展，分别是：ExporterListener是远程服务发布监听器，可以监听服务发布和取消发布两个事件点；InvokerListener是服务消费者引用调用器的监听器，可以监听引用和销毁两个事件方法。支持可扩展的事件监听模型，目前只提供了一些适配器InvokerListenerAdapter、ExporterListenerAdapter以及简单的过期服务调用监听器DeprecatedInvokerListener。开发者可自行扩展自己的监听器。该类源码如下。

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.protocol;

import java.util.Collections;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.ExporterListener;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.listener.ListenerExporterWrapper;
import com.alibaba.dubbo.rpc.listener.ListenerInvokerWrapper;

/**
 * ListenerProtocol
 * 
 * @author william.liangf
 */
public class ProtocolListenerWrapper implements Protocol {

    private final Protocol protocol;

    public ProtocolListenerWrapper(Protocol protocol){
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    public int getDefaultPort() {
        return protocol.getDefaultPort();
    }

    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        //特殊协议，跳过监听器触发。
        if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
            return protocol.export(invoker);
        }
        //调用原始协议的发布方法，触发监听器链事件。
        return new ListenerExporterWrapper<T>(protocol.export(invoker), 
                Collections.unmodifiableList(ExtensionLoader.getExtensionLoader(ExporterListener.class)
                        .getActivateExtension(invoker.getUrl(), Constants.EXPORTER_LISTENER_KEY)));
    }

    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
            return protocol.refer(type, url);
        }
        return new ListenerInvokerWrapper<T>(protocol.refer(type, url), 
                Collections.unmodifiableList(
                        ExtensionLoader.getExtensionLoader(InvokerListener.class)
                        .getActivateExtension(url, Constants.INVOKER_LISTENER_KEY)));
    }

    public void destroy() {
        protocol.destroy();
    }

}
```

### ProxyFactory

dubbo的代理工厂。定义了两个接口分别是：getProxy根据invoker目标接口的代理对象，一般是消费者获得代理对象触发远程调用；getInvoker方法将代理对象proxy、接口类type和远程服务的URL获取执行对象Invoker，往往是提供者获得目标执行对象执行目标实现调用。AbstractProxyFactory是其抽象实现，提供了getProxy的模版方法实现，使得可以支持多接口的映射。dubbo最终内置了两种动态代理的实现，分别是jdkproxy和javassist。默认的实现使用javassist。为什么选择javassist，梁飞选型的时候做过性能测试对比分析，参考：[http://javatar.iteye.com/blog/814426/](http://javatar.iteye.com/blog/814426/)

### Invoker

该接口是服务的执行体。它有获取服务发布的URL，服务的接口类等关键属性的行为；还有核心的服务执行方法invoke，执行该方法后返回执行结果Result，而传递的参数是调用信息Invocation。该接口有大量的抽象和具体实现类。AbstractProxyInvoker是基于代理的执行器抽象实现，AbstractInvoker是通用的抽象实现。

## 服务发布流程

![img](http://static.oschina.net/uploads/space/2016/0530/182814_TFkv_113011.jpg)
首先ServiceConfig类拿到对外提供服务的实际类ref(如：HelloWorldImpl),然后通过ProxyFactory类的getInvoker方法使用ref生成一个AbstractProxyInvoker实例，到这一步就完成具体服务到Invoker的转化。接下来就是Invoker转换到Exporter的过程。
Dubbo处理服务暴露的关键就在Invoker转换到Exporter的过程(如上图中的红色部分)，下面我们以Dubbo和RMI这两种典型协议的实现来进行说明：

## 服务引用流程

![img](http://static.oschina.net/uploads/space/2016/0530/182914_kvl0_113011.jpg)

上图是服务消费的主过程：
首先ReferenceConfig类的init方法调用Protocol的refer方法生成Invoker实例(如上图中的红色部分)，这是服务消费的关键。接下来把Invoker转换为客户端需要的接口(如：HelloWorld)。
关于每种协议如RMI/Dubbo/Web service等它们在调用refer方法生成Invoker实例的细节和上一章节所描述的类似。

## 总结

该模块下设计较为复杂，在设计中可以看出来应用了大量的设计模式，包括模版方法、职责链、装饰器和动态代理等设计模式。掌握该模块下的核心概念对于后续阅读其它部分代码至关重要，后面的其它模块要么是它的实现，要么是由它衍生出来的，要么与它的关系非常紧密。

## next

接下来我们看看rpc的默认实现模块——dubbo-rpc-default。该模块提供了默认的dubbo协议的实现，也是默认使用的协议。



# 三 dubbo-rpc-default模块源码分析

> 摘要: dubbo-rpc-default模块是dubbo-rpc-api模块的默认实现，他提供了默认的dubbo协议的实现，它是所有模块中最为复杂的一个模块，因为底层的协议都是它自己实现的，因此是最值得学习和研究的一个模块，因此从该模块入手学习rpc的协议实现。
>

## 简化类图

![img](http://static.oschina.net/uploads/space/2016/0531/161337_2KlD_113011.jpg)

从图中可以看出该模块下的类主要是实现了dubbo-rpc-api和dubbo-remoting-api两个模块中定义的一些接口和抽象类。扩展了一种duubo框架自定义的dubbo协议，包括编解码和方法调用处理等。

## DubboProtocol

该类是抽象协议实现类AbstractProtocol的具体的dubbo协议的实现，从该类开始着手分析。

### 发布服务方法export的实现

```java
public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl();
        
        // export service. 通过url获得该服务的key。格式如：{serviceGroup}/{serviceName}:{serviceVersion}:{port}
        String key = serviceKey(url);
        //Dubbo协议实现的服务发布器。
        DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, exporterMap);
        exporterMap.put(key, exporter);
        
        //export an stub service for dispaching event
        //参数STUB_EVENT_KEY和IS_CALLBACK_SERVICE的含义不太清楚，需要后续深究。
        Boolean isStubSupportEvent = url.getParameter(Constants.STUB_EVENT_KEY,Constants.DEFAULT_STUB_EVENT);
        Boolean isCallbackservice = url.getParameter(Constants.IS_CALLBACK_SERVICE, false);
        if (isStubSupportEvent && !isCallbackservice){
            String stubServiceMethods = url.getParameter(Constants.STUB_EVENT_METHODS_KEY);
            if (stubServiceMethods == null || stubServiceMethods.length() == 0 ){
                if (logger.isWarnEnabled()){
                    logger.warn(new IllegalStateException("consumer [" +url.getParameter(Constants.INTERFACE_KEY) +
                            "], has set stubproxy support event ,but no stub methods founded."));
                }
            } else {
                stubServiceMethodsMap.put(url.getServiceKey(), stubServiceMethods);
            }
        }

        //调用打开服务器绑定url的方法，这个地方是核心，需要进入深究。
        openServer(url);
        
        return exporter;
    }
```

该方法实现了dubbo协议的服务发布，显示构造一个DubboExporter实现类的Exporter，用于返回。最核心的是调用内部方法openServer(url);将该url发布到dubbo服务器上。我们进入该方法看看。

```java
private void openServer(URL url) {
        // find server.
        String key = url.getAddress();
        //client 也可以暴露一个只有server可以调用的服务。
        boolean isServer = url.getParameter(Constants.IS_SERVER_KEY,true);
        if (isServer) {
        	ExchangeServer server = serverMap.get(key);
        	if (server == null) {
        		serverMap.put(key, createServer(url));
        	} else {
        		//server支持reset,配合override功能使用
        		server.reset(url);
        	}
        }
    }
```

该方法是获得url的地址，通过地址找到对应的server，若已经有相同的地址则无需构造新的server，只需要直接使用，只就起到了缓存server的作用，避免重复构建server。若已经找到了该地址，则会调用server.reset(url)重置一下。url中的参数Constants.IS_SERVER_KEY参数可以禁止发布远程服务，只能本地调用。具体意义不是十分清楚。继续进入方法：createServer(url)

```java
    private ExchangeServer createServer(URL url) {
        //默认开启server关闭时发送readonly事件
        url = url.addParameterIfAbsent(Constants.CHANNEL_READONLYEVENT_SENT_KEY, Boolean.TRUE.toString());
        //默认开启heartbeat
        url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));
        String str = url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_SERVER);

        if (str != null && str.length() > 0 && ! ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str))
            throw new RpcException("Unsupported server type: " + str + ", url: " + url);

        url = url.addParameter(Constants.CODEC_KEY, Version.isCompatibleVersion() ? COMPATIBLE_CODEC_NAME : DubboCodec.NAME);
        ExchangeServer server;
        try {
            server = Exchangers.bind(url, requestHandler);
        } catch (RemotingException e) {
            throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), e);
        }
        str = url.getParameter(Constants.CLIENT_KEY);
        if (str != null && str.length() > 0) {
            Set<String> supportedTypes = ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions();
            if (!supportedTypes.contains(str)) {
                throw new RpcException("Unsupported client type: " + str);
            }
        }
        return server;
    }
```

该方法先增加了一些默认的参数，比如heartbeat、server等。检查参数的合法性。最后调用Exchangers.bind(url, requestHandler)将url绑定到requestHandler上获得绑定的服务器，Exchangers是网络通讯模块dubbo-remoting-api中定义的，详细的含义，等我们分析该模块再了解。我们猜测该方法的含义是绑定url的处理器为requestHandler，并返回服务器。requestHandler就是如何处理接收的请求，这个地方是核心，我们进入该对象的定义看看。

```java
private ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {
        
        public Object reply(ExchangeChannel channel, Object message) throws RemotingException {
            if (message instanceof Invocation) {
                Invocation inv = (Invocation) message;
                Invoker<?> invoker = getInvoker(channel, inv);
                //如果是callback 需要处理高版本调用低版本的问题
                if (Boolean.TRUE.toString().equals(inv.getAttachments().get(IS_CALLBACK_SERVICE_INVOKE))){
                    String methodsStr = invoker.getUrl().getParameters().get("methods");
                    boolean hasMethod = false;
                    if (methodsStr == null || methodsStr.indexOf(",") == -1){
                        hasMethod = inv.getMethodName().equals(methodsStr);
                    } else {
                        String[] methods = methodsStr.split(",");
                        for (String method : methods){
                            if (inv.getMethodName().equals(method)){
                                hasMethod = true;
                                break;
                            }
                        }
                    }
                    if (!hasMethod){
                        logger.warn(new IllegalStateException("The methodName "+inv.getMethodName()+" not found in callback service interface ,invoke will be ignored. please update the api interface. url is:" + invoker.getUrl()) +" ,invocation is :"+inv );
                        return null;
                    }
                }
                RpcContext.getContext().setRemoteAddress(channel.getRemoteAddress());
                return invoker.invoke(inv);
            }
            throw new RemotingException(channel, "Unsupported request: " + message == null ? null : (message.getClass().getName() + ": " + message) + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress());
        }

        @Override
        public void received(Channel channel, Object message) throws RemotingException {
            if (message instanceof Invocation) {
                reply((ExchangeChannel) channel, message);
            } else {
                super.received(channel, message);
            }
        }

        @Override
        public void connected(Channel channel) throws RemotingException {
            invoke(channel, Constants.ON_CONNECT_KEY);
        }

        @Override
        public void disconnected(Channel channel) throws RemotingException {
            if(logger.isInfoEnabled()){
                logger.info("disconected from "+ channel.getRemoteAddress() + ",url:" + channel.getUrl());
            }
            invoke(channel, Constants.ON_DISCONNECT_KEY);
        }
        
        private void invoke(Channel channel, String methodKey) {
            Invocation invocation = createInvocation(channel, channel.getUrl(), methodKey);
            if (invocation != null) {
                try {
                    received(channel, invocation);
                } catch (Throwable t) {
                    logger.warn("Failed to invoke event method " + invocation.getMethodName() + "(), cause: " + t.getMessage(), t);
                }
            }
        }
        
        private Invocation createInvocation(Channel channel, URL url, String methodKey) {
            String method = url.getParameter(methodKey);
            if (method == null || method.length() == 0) {
                return null;
            }
            RpcInvocation invocation = new RpcInvocation(method, new Class<?>[0], new Object[0]);
            invocation.setAttachment(Constants.PATH_KEY, url.getPath());
            invocation.setAttachment(Constants.GROUP_KEY, url.getParameter(Constants.GROUP_KEY));
            invocation.setAttachment(Constants.INTERFACE_KEY, url.getParameter(Constants.INTERFACE_KEY));
            invocation.setAttachment(Constants.VERSION_KEY, url.getParameter(Constants.VERSION_KEY));
            if (url.getParameter(Constants.STUB_EVENT_KEY, false)){
                invocation.setAttachment(Constants.STUB_EVENT_KEY, Boolean.TRUE.toString());
            }
            return invocation;
        }
    };
```

该对象是一个匿名类对象，实现了接口ExchangeHandler，应该就是一个远程通讯的抽象，是一个通讯处理类，处理接收到信息。其中方法reply是响应客户端请求信息，它根据Invocation对象获得invoker，最后再调用invoker.invoke方法执行目标对象的方法，将返回结果发回给客户端。其它的几个事件的方法也做了响应的处理，包括：received、connected和disconnected等事件。

### 引用服务方法refer的实现

```java
public <T> Invoker<T> refer(Class<T> serviceType, URL url) throws RpcException {
        // create rpc invoker.
        DubboInvoker<T> invoker = new DubboInvoker<T>(serviceType, url, getClients(url), invokers);
        invokers.add(invoker);
        return invoker;
    }
    
    private ExchangeClient[] getClients(URL url){
        //是否共享连接
        boolean service_share_connect = false;
        int connections = url.getParameter(Constants.CONNECTIONS_KEY, 0);
        //如果connections不配置，则共享连接，否则每服务每连接
        if (connections == 0){
            service_share_connect = true;
            connections = 1;
        }
        
        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; i++) {
            if (service_share_connect){
                clients[i] = getSharedClient(url);
            } else {
                clients[i] = initClient(url);
            }
        }
        return clients;
    }
    
    /**
     *获取共享连接 
     */
    private ExchangeClient getSharedClient(URL url){
        String key = url.getAddress();
        ReferenceCountExchangeClient client = referenceClientMap.get(key);
        if ( client != null ){
            if ( !client.isClosed()){
                client.incrementAndGetCount();
                return client;
            } else {
//                logger.warn(new IllegalStateException("client is closed,but stay in clientmap .client :"+ client));
                referenceClientMap.remove(key);
            }
        }
        ExchangeClient exchagneclient = initClient(url);
        
        client = new ReferenceCountExchangeClient(exchagneclient, ghostClientMap);
        referenceClientMap.put(key, client);
        ghostClientMap.remove(key);
        return client; 
    }

    /**
     * 创建新连接.
     */
    private ExchangeClient initClient(URL url) {
        
        // client type setting.
        String str = url.getParameter(Constants.CLIENT_KEY, url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_CLIENT));

        String version = url.getParameter(Constants.DUBBO_VERSION_KEY);
        boolean compatible = (version != null && version.startsWith("1.0."));
        url = url.addParameter(Constants.CODEC_KEY, Version.isCompatibleVersion() && compatible ? COMPATIBLE_CODEC_NAME : DubboCodec.NAME);
        //默认开启heartbeat
        url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));
        
        // BIO存在严重性能问题，暂时不允许使用
        if (str != null && str.length() > 0 && ! ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str)) {
            throw new RpcException("Unsupported client type: " + str + "," +
                    " supported client type is " + StringUtils.join(ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions(), " "));
        }
        
        ExchangeClient client ;
        try {
            //设置连接应该是lazy的 
            if (url.getParameter(Constants.LAZY_CONNECT_KEY, false)){
                client = new LazyConnectExchangeClient(url ,requestHandler);
            } else {
                client = Exchangers.connect(url ,requestHandler);
            }
        } catch (RemotingException e) {
            throw new RpcException("Fail to create remoting client for service(" + url
                    + "): " + e.getMessage(), e);
        }
        return client;
    }
```

该方法先直接构造一个DubboInvoker类型的对象，其中获取客户端的参数调用了方法getClients(url)。看是否配置了参数connections，若未配置或配置为0则表示共享客户端连接，如果不共享则直接创建一个新的客户端对象，否则获得已经共享的连接，并且返回一个创建包装器ReferenceCountExchangeClient的客户端实例，该实例会记录被引用次数，最终的方法还是调用目标的client对象。

初始化一个全新的Client对象的方法是核心，它也是先配置一些默认的参数，如果配置参数lazy则表示延迟创建客户端连接，则直接返回一个LazyConnectExchangeClient对象，该对象也是ExchangeClient的包装器对象，它会在请求的时候先检查连接，若未创建连接则会先创建连接。最后调用client = Exchangers.connect(url ,requestHandler);将url绑定到请求处理器requestHandler上。

## DubboInvoker

该类是消费者dubbo协议的执行器。它处理了dubbo协议在客户端调用远程接口的逻辑实现。核心方法是doInvoke，我们重点看看这个方法的实现。

```java
 @Override
    protected Result doInvoke(final Invocation invocation) throws Throwable {
        RpcInvocation inv = (RpcInvocation) invocation;
        final String methodName = RpcUtils.getMethodName(invocation);
        inv.setAttachment(Constants.PATH_KEY, getUrl().getPath());
        inv.setAttachment(Constants.VERSION_KEY, version);
        
        ExchangeClient currentClient;
        if (clients.length == 1) {
            currentClient = clients[0];
        } else {
            currentClient = clients[index.getAndIncrement() % clients.length];
        }
        try {
            boolean isAsync = RpcUtils.isAsync(getUrl(), invocation);
            boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);
            int timeout = getUrl().getMethodParameter(methodName, Constants.TIMEOUT_KEY,Constants.DEFAULT_TIMEOUT);
            if (isOneway) {
            	boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
                currentClient.send(inv, isSent);
                RpcContext.getContext().setFuture(null);
                return new RpcResult();
            } else if (isAsync) {
            	ResponseFuture future = currentClient.request(inv, timeout) ;
                RpcContext.getContext().setFuture(new FutureAdapter<Object>(future));
                return new RpcResult();
            } else {
            	RpcContext.getContext().setFuture(null);
                return (Result) currentClient.request(inv, timeout).get();
            }
        } catch (TimeoutException e) {
            throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        } catch (RemotingException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }
```

首先客户端支持多个连接调用服务，这个可以通过参数设置，会轮询连接去调用服务。支持三种调用方式，分别是oneway（单向调用）、async（异步）和sync（同步），这个都是可以通过url参数指定，通过客户端对应的方法去调用服务端的服务。

## next

可以看出来，我们有大量的接口和抽象类来自于dubbo-remoting-api模块，我们的疑问都在这里，接下来我们研究该模块才能解决我们的很多疑惑。



# 四 dubbo的SPI机制源码分析

> 摘要: dubbo是一个具有非常强大扩展能力的分布式服务框架，而让它具备强大扩展能力的主要手段就是通过它的SPI机制的。因此掌握dubbo的SPI机制对于使用、扩展和阅读源码都大有帮助。本文将重点介绍dubbo的SPI机制的实现源码，看源码能够将很多文档中未说得非常详细的内容展现出来。
>

## dubbo的SPI机制

关于dubbo的SPI机制请参阅dubbo开发者文档 -> [http://dubbo.io/Developer+Guide-zh.htm#DeveloperGuide-zh-%E6%89%A9%E5%B1%95%E7%82%B9%E5%8A%A0%E8%BD%BD](http://dubbo.io/Developer+Guide-zh.htm#DeveloperGuide-zh-%E6%89%A9%E5%B1%95%E7%82%B9%E5%8A%A0%E8%BD%BD)

我只把我自己理解的SPI与大家分享一下：

SPI是面向dubbo开发者角色的扩展接口，开发者可以通过它来扩展dubbo的功能和技术实现。相对而言API是面向dubbo使用者角色编程接口。

SPI解决的是扩展内容配置和动态加载的问题。在java中解决相同或者类似问题的技术有OSGI，JDK自带的SPI，以及IOC框架Spring也能够解决类似的问题，各种解决方案各有特点，我们不展开讲。而dubbo的SPI是从JDK标准的SPI(Service Provider Interface)扩展点发现机制加强而来，它做了如下改进：（这些内容引用自dubbo官方开发者手册）

- JDK标准的SPI会一次性实例化扩展点所有实现，如果有扩展实现初始化很耗时，但如果没用上也加载，会很浪费资源。
- 如果扩展点加载失败，连扩展点的名称都拿不到了。比如：JDK标准的ScriptEngine，通过getName();获取脚本类型的名称，但如果RubyScriptEngine因为所依赖的jruby.jar不存在，导致RubyScriptEngine类加载失败，这个失败原因被吃掉了，和ruby对应不起来，当用户执行ruby脚本时，会报不支持ruby，而不是真正失败的原因。
- 增加了对扩展点IoC和AOP的支持，一个扩展点可以直接setter注入其它扩展点。

那么dubbo的SPI是如何实现的呢？让我们直接看源码一探究竟，SPI的源码位于工程dubbo-common的包com.alibaba.dubbo.common.extension下。

## dubbo的SPI源码实现分析

### ExtensionLoader

该类是dubbo的SPI机制实现的最为核心的一个类，绝大多数实现逻辑均位于该类中，因此我们从该类入手研究。为了简化，该类是一个将使用api及核心逻辑实现都封装同一个类中。通常获得一个扩展接口的实例使用如下接口方法获得。

```java
ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(DubboProtocol.NAME); 
```

### 静态工厂方法getExtensionLoader

该方法是一个静态工厂方法，是一个编程api，通过该方法获得一个某个参数制定的扩展类的ExtensionLoader对象。

```java
    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }
   
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if(!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if(!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type + 
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }
        
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }
```

从看源码我们得出几个结论。

1. dubbo的SPI扩展点必须是接口。
2. dubbo的SPI扩展点接口必须用注解SPI标注。
3. 某个扩展点的ExtensionLoader是获取的时候延迟生成，并且会进行缓存。

### 获得扩展实例方法getExtension

```java
 /**
     * 返回指定名字的扩展。如果指定名字的扩展不存在，则抛异常 {@link IllegalStateException}.
     *
     * @param name
     * @return
     */
	@SuppressWarnings("unchecked")
	public T getExtension(String name) {
		if (name == null || name.length() == 0)
		    throw new IllegalArgumentException("Extension name == null");
		if ("true".equals(name)) {
		    return getDefaultExtension();
		}
		Holder<Object> holder = cachedInstances.get(name);
		if (holder == null) {
		    cachedInstances.putIfAbsent(name, new Holder<Object>());
		    holder = cachedInstances.get(name);
		}
		Object instance = holder.get();
		if (instance == null) {
		    synchronized (holder) {
	            instance = holder.get();
	            if (instance == null) {
	                instance = createExtension(name);
	                holder.set(instance);
	            }
	        }
		}
		return (T) instance;
	}
```

1. 扩展点名称不能空，否则抛出异常。
2. 扩展点名称传入true则表示获取默认扩展点实例。
3. 扩展点支持缓存，说明扩展点对象在SPI容器中单例的，需要考虑线程安全。
4. 扩展点对象生成时延迟创建的，实现了对jdk的spi的改进。

#### 获取默认扩展对象方法getDefaultExtension

```java
/**
	 * 返回缺省的扩展，如果没有设置则返回<code>null</code>。 
	 */
	public T getDefaultExtension() {
	    getExtensionClasses();
        if(null == cachedDefaultName || cachedDefaultName.length() == 0
                || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
	}

```

获得默认的扩展对象，属性cachedDefaultName的值并不是参数传递进来的，它是在方法获得扩展类getExtensionClasses()中赋值的。稍后我们一起看看该方法的实现。如果没有加载到默认的扩展点实现，则返回null。

创建某种扩展对象方法createExtension

```java
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, (T) clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            injectExtension(instance);
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (wrapperClasses != null && wrapperClasses.size() > 0) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }
    
```

1. 根据扩展名称获得扩展实现类。先会调用方法getExtensionClasses加载扩展类列表，然后获取指定名称对应的扩展类。
2. 若找不到扩展类则抛出异常。
3. 从扩展实例缓存中找到该扩展类的实例。若找到则直接返回。
4. 若未找到缓存对象则产生新对象。调用的方法是clazz.newInstance())。
5. 给扩展实例执行依赖注入。调用了方法injectExtension，从而实现了对jdk的spi机制的ioc和aop功能扩展。

### 获取扩展实现类方法getExtensionClasse()

该方法实现了获取某个名称扩展点的实现类，该方法是被几乎所有的方法调用之前先调用该方法，实现了扩展实现类的加载。

```java
private Class<?> getExtensionClass(String name) {
	    if (type == null)
	        throw new IllegalArgumentException("Extension type == null");
	    if (name == null)
	        throw new IllegalArgumentException("Extension name == null");
	    Class<?> clazz = getExtensionClasses().get(name);
	    if (clazz == null)
	        throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
	    return clazz;
	}


	private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
	}

 // 此方法已经getExtensionClasses方法同步过。
    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if(defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if(value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if(names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if(names.length == 1) cachedDefaultName = names[0];
            }
        }
        
        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        loadFile(extensionClasses, DUBBO_DIRECTORY);
        loadFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }


   private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<java.net.URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                final int ci = line.indexOf('#');
                                if (ci >= 0) line = line.substring(0, ci);
                                line = line.trim();
                                if (line.length() > 0) {
                                    try {
                                        String name = null;
                                        int i = line.indexOf('=');
                                        if (i > 0) {
                                            name = line.substring(0, i).trim();
                                            line = line.substring(i + 1).trim();
                                        }
                                        if (line.length() > 0) {
                                            Class<?> clazz = Class.forName(line, true, classLoader);
                                            if (! type.isAssignableFrom(clazz)) {
                                                throw new IllegalStateException("Error when load extension class(interface: " +
                                                        type + ", class line: " + clazz.getName() + "), class " 
                                                        + clazz.getName() + "is not subtype of interface.");
                                            }
                                            if (clazz.isAnnotationPresent(Adaptive.class)) {
                                                if(cachedAdaptiveClass == null) {
                                                    cachedAdaptiveClass = clazz;
                                                } else if (! cachedAdaptiveClass.equals(clazz)) {
                                                    throw new IllegalStateException("More than 1 adaptive class found: "
                                                            + cachedAdaptiveClass.getClass().getName()
                                                            + ", " + clazz.getClass().getName());
                                                }
                                            } else {
                                                try {
                                                    clazz.getConstructor(type);
                                                    Set<Class<?>> wrappers = cachedWrapperClasses;
                                                    if (wrappers == null) {
                                                        cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
                                                        wrappers = cachedWrapperClasses;
                                                    }
                                                    wrappers.add(clazz);
                                                } catch (NoSuchMethodException e) {
                                                    clazz.getConstructor();
                                                    if (name == null || name.length() == 0) {
                                                        name = findAnnotationName(clazz);
                                                        if (name == null || name.length() == 0) {
                                                            if (clazz.getSimpleName().length() > type.getSimpleName().length()
                                                                    && clazz.getSimpleName().endsWith(type.getSimpleName())) {
                                                                name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - type.getSimpleName().length()).toLowerCase();
                                                            } else {
                                                                throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + url);
                                                            }
                                                        }
                                                    }
                                                    String[] names = NAME_SEPARATOR.split(name);
                                                    if (names != null && names.length > 0) {
                                                        Activate activate = clazz.getAnnotation(Activate.class);
                                                        if (activate != null) {
                                                            cachedActivates.put(names[0], activate);
                                                        }
                                                        for (String n : names) {
                                                            if (! cachedNames.containsKey(clazz)) {
                                                                cachedNames.put(clazz, n);
                                                            }
                                                            Class<?> c = extensionClasses.get(n);
                                                            if (c == null) {
                                                                extensionClasses.put(n, clazz);
                                                            } else if (c != clazz) {
                                                                throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Throwable t) {
                                        IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                        exceptions.put(line, e);
                                    }
                                }
                            } // end of while read lines
                        } finally {
                            reader.close();
                        }
                    } catch (Throwable t) {
                        logger.error("Exception when load extension class(interface: " +
                                            type + ", class file: " + url + ") in " + url, t);
                    }
                } // end of while urls
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }
```

1. 先从加载的缓冲中获取扩展点实现类对象。若获取到则直接返回，否则需要加载。
2. 缓存有击穿的风险。即当扩展点无实现类的情况下会每次都进行扩展点记载，该操作是非常耗时的操作，因此要避免这种情况。
3. 加载扩展点实现类。通过调用方法loadExtensionClasses实现。
4. 扩展点接口上的SPI注解可以指定默认的扩展点名称。而且只能设置一个默认扩展点，不允许多个。这个很好理解。代码可以看出此时会将SPI注解的value值赋值给属性cachedDefaultName。
5. 扫描classpath下的三个目录加载SPI配置信息。分别类路径下的：META-INF/services/、META-INF/dubbo/ 和 META-INF/dubbo/internal/，加载顺序与该顺序正好相反，META-INF/dubbo/internal/ 最早开始加载。
6. 加载类路径下所有SPI配置文件。文件命名是：目录名+{扩展接口的类名}，比如扩展接口Protocol则文件名为："META-INF/dubbo/com.alibaba.dubbo.rpc.Protocol"。加载这些配置文件中的内容。
7. 逐行读取配置文件中的内容。
8. 配置文件支持'#'后面的均为注视，可以忽略。
9. 配置文件类似properties文件格式。内容格式是： {name}={className}。其中等号左边是扩展点实现名称，右边是扩展点实现的全局类名，需要包含在类路径中，否则类加载会抛出异常。可以写多行，每行表示一个实现类。
10. 找到类名后会使用当前的classloader加载该类。若加载失败则会记录异常。
11. 检查该扩展实现类的合法性。该类必须是扩展接口类的实现类。若不是则会抛出异常。
12. 检查扩展实现类是否为Adaptive标注类。如果是注解Adaptive标注的类（在类上注解，方法注解不算），则会将改类赋值给cachedAdaptiveClass类，表示该类为一个适配器类，然后退出。
13. 否则尝试获取包含参数为type的构造函数。如果有包含参数为type的构造函数，则说明该实现类是一个包装器类，会将cachedWrapperClasses的属性设置为该值，然后退出。
14. 否则继续获得无参构造函数。尝试获取无参数构造函数，若无则会抛出异常。
15. 检查扩展类名称。如果没有名称则会尝试自动生成，即若扩展点定义为com.alibaba.dubbo.rpc.Protocol，则扩展类名必须为com.alibaba.dubbo.rpc.XxxProtocol的才可以生成，自动生成的名称为xxx。即将前缀替换为小写。否则会抛出异常。
16. 扩展名称支持多个。名称如果是以','隔开，则表示名称是多个，多个名称都会被记录下来，引用相同的扩展实现类。
17. 如果扩展类有Activate注解，则会将注解实例activate放入集合属性cachedActivates中。
18. 将扩展实现的名称和类分别放入缓存中。以备后续使用。

### 获得自适应扩展getAdaptiveExtension

ExtensionLoader注入的依赖扩展点是一个Adaptive实例，直到扩展点方法执行时才决定调用是一个扩展点实现。

Dubbo使用URL对象（包含了Key-Value）传递配置信息。

扩展点方法调用会有URL参数（或是参数有URL成员）

这样依赖的扩展点也可以从URL拿到配置信息，所有的扩展点自己定好配置的Key后，配置信息从URL上从最外层传入。URL在配置传递上即是一条**总线**。

本方法实现了这一特性。

```java
 @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if(createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                        }
                    }
                }
            }
            else {
                throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
            }
        }

        return (T) instance;
    }

 @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extenstion " + type + ", cause: " + e.getMessage(), e);
        }
    }

 private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }


  private Class<?> createAdaptiveExtensionClass() {
        String code = createAdaptiveExtensionClassCode();
        ClassLoader classLoader = findClassLoader();
        com.alibaba.dubbo.common.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.common.compiler.Compiler.class).getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }


 private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuidler = new StringBuilder();
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for(Method m : methods) {
            if(m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // 完全没有Adaptive方法，则不需要生成Adaptive类
        if(! hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");
        
        codeBuidler.append("package " + type.getPackage().getName() + ";");
        codeBuidler.append("\nimport " + ExtensionLoader.class.getName() + ";");
        codeBuidler.append("\npublic class " + type.getSimpleName() + "$Adpative" + " implements " + type.getCanonicalName() + " {");
        
        for (Method method : methods) {
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(URL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }
                // 有类型为URL的参数
                if (urlTypeIndex != -1) {
                    // Null Point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");",
                                    urlTypeIndex);
                    code.append(s);
                    
                    s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex); 
                    code.append(s);
                }
                // 参数没有URL类型
                else {
                    String attribMethod = null;
                    
                    // 找到参数的URL属性
                    LBL_PTS:
                    for (int i = 0; i < pts.length; ++i) {
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            if ((name.startsWith("get") || name.length() > 3)
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && m.getReturnType() == URL.class) {
                                urlTypeIndex = i;
                                attribMethod = name;
                                break LBL_PTS;
                            }
                        }
                    }
                    if(attribMethod == null) {
                        throw new IllegalStateException("fail to create adative class for interface " + type.getName()
                        		+ ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }
                    
                    // Null point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
                                    urlTypeIndex, pts[urlTypeIndex].getName());
                    code.append(s);
                    s = String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
                                    urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
                    code.append(s);

                    s = String.format("%s url = arg%d.%s();",URL.class.getName(), urlTypeIndex, attribMethod); 
                    code.append(s);
                }
                
                String[] value = adaptiveAnnotation.value();
                // 没有设置Key，则使用“扩展点接口名的点分隔 作为Key
                if(value.length == 0) {
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if(Character.isUpperCase(charArray[i])) {
                            if(i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));
                        }
                        else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[] {sb.toString()};
                }
                
                boolean hasInvocation = false;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].getName().equals("com.alibaba.dubbo.rpc.Invocation")) {
                        // Null Point check
                        String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"invocation == null\");", i);
                        code.append(s);
                        s = String.format("\nString methodName = arg%d.getMethodName();", i); 
                        code.append(s);
                        hasInvocation = true;
                        break;
                    }
                }
                
                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; --i) {
                    if(i == value.length - 1) {
                        if(null != defaultExtName) {
                            if(!"protocol".equals(value[i]))
                                if (hasInvocation) 
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                        }
                        else {
                            if(!"protocol".equals(value[i]))
                                if (hasInvocation) 
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                            else
                                getNameCode = "url.getProtocol()";
                        }
                    }
                    else {
                        if(!"protocol".equals(value[i]))
                            if (hasInvocation) 
                                getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                        else
                            getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                    }
                }
                code.append("\nString extName = ").append(getNameCode).append(";");
                // check extName == null?
                String s = String.format("\nif(extName == null) " +
                		"throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(s);
                
                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);
                
                // return statement
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }
            
            codeBuidler.append("\npublic " + rt.getCanonicalName() + " " + method.getName() + "(");
            for (int i = 0; i < pts.length; i ++) {
                if (i > 0) {
                    codeBuidler.append(", ");
                }
                codeBuidler.append(pts[i].getCanonicalName());
                codeBuidler.append(" ");
                codeBuidler.append("arg" + i);
            }
            codeBuidler.append(")");
            if (ets.length > 0) {
                codeBuidler.append(" throws ");
                for (int i = 0; i < ets.length; i ++) {
                    if (i > 0) {
                        codeBuidler.append(", ");
                    }
                    codeBuidler.append(ets[i].getCanonicalName());
                }
            }
            codeBuidler.append(" {");
            codeBuidler.append(code.toString());
            codeBuidler.append("\n}");
        }
        codeBuidler.append("\n}");
        if (logger.isDebugEnabled()) {
            logger.debug(codeBuidler.toString());
        }
        return codeBuidler.toString();
    }
```

1. 先从缓存中获得扩展点适配实例。如果有则直接获得，否则需要创建。
2. 若有类通过属性cachedAdaptiveClass的类创建。否则需要获取类。
3. 如果获取类失败，则需要调用方法createAdaptiveExtensionClass动态创建适配器类。
4. 如果扩展点类type没有方法被注解Adaptive注释过，则无需创建适配器类，则抛出异常。
5. 动态生成一个适配器实现类。生成的类名是： type.getSimpleName() + "$Adpative"。
6. 生成的类要实现每一个type中包含Adaptive注释过方法。若没有则该方法被抛出不支持的异常。
7. 实现包含Adaptive注释过方法。先通过type参数类型为URL或者getXXX返回值为URL的获得url对象值。并且会检查url值是否为空，如果为空则会抛出异常。
8. 若方法上的Adaptive注解的value是空，则使用“扩展点接口名的点分隔" 作为Key。
9. 以value作为key，从url对象中获得值，然后将获得的值调用方法getExtension获得扩展点实现对象。这样就实现了动态注入扩展实现。

相关内容的进一步分析请参考另外一篇文章：[http://my.oschina.net/ywbrj042/blog/688042](http://my.oschina.net/ywbrj042/blog/688042)

### 获得自动激活扩展列表方法getActivateExtension

对于集合类扩展点，比如：Filter, InvokerListener, ExportListener, TelnetHandler, StatusChecker等，
可以同时加载多个实现，此时，可以用自动激活来简化配置。本方法实现了这一特性。

```java
 /**
     * This is equivalent to <pre>
     *     getActivateExtension(url, key, null);
     * </pre>
     *
     * @param url url
     * @param key url parameter key which used to get extension point names
     * @return extension list which are activated.
     * @see #getActivateExtension(com.alibaba.dubbo.common.URL, String, String)
     */
    public List<T> getActivateExtension(URL url, String key) {
        return getActivateExtension(url, key, null);
    }

    /**
     * This is equivalent to <pre>
     *     getActivateExtension(url, values, null);
     * </pre>
     *
     * @see #getActivateExtension(com.alibaba.dubbo.common.URL, String[], String)
     * @param url url
     * @param values extension point names
     * @return extension list which are activated
     */
    public List<T> getActivateExtension(URL url, String[] values) {
        return getActivateExtension(url, values, null);
    }

    /**
     * This is equivalent to <pre>
     *     getActivateExtension(url, url.getParameter(key).split(","), null);
     * </pre>
     *
     * @see #getActivateExtension(com.alibaba.dubbo.common.URL, String[], String)
     * @param url url
     * @param key url parameter key which used to get extension point names
     * @param group group
     * @return extension list which are activated.
     */
    public List<T> getActivateExtension(URL url, String key, String group) {
        String value = url.getParameter(key);
        return getActivateExtension(url, value == null || value.length() == 0 ? null : Constants.COMMA_SPLIT_PATTERN.split(value), group);
    }

    /**
     * Get activate extensions.
     *
     * @see com.alibaba.dubbo.common.extension.Activate
     * @param url url
     * @param values extension point names
     * @param group group
     * @return extension list which are activated
     */
    public List<T> getActivateExtension(URL url, String[] values, String group) {
        List<T> exts = new ArrayList<T>();
        List<String> names = values == null ? new ArrayList<String>(0) : Arrays.asList(values);
        if (! names.contains(Constants.REMOVE_VALUE_PREFIX + Constants.DEFAULT_KEY)) {
            getExtensionClasses();
            for (Map.Entry<String, Activate> entry : cachedActivates.entrySet()) {
                String name = entry.getKey();
                Activate activate = entry.getValue();
                if (isMatchGroup(group, activate.group())) {
                    T ext = getExtension(name);
                    if (! names.contains(name)
                            && ! names.contains(Constants.REMOVE_VALUE_PREFIX + name) 
                            && isActive(activate, url)) {
                        exts.add(ext);
                    }
                }
            }
            Collections.sort(exts, ActivateComparator.COMPARATOR);
        }
        List<T> usrs = new ArrayList<T>();
        for (int i = 0; i < names.size(); i ++) {
        	String name = names.get(i);
            if (! name.startsWith(Constants.REMOVE_VALUE_PREFIX)
            		&& ! names.contains(Constants.REMOVE_VALUE_PREFIX + name)) {
            	if (Constants.DEFAULT_KEY.equals(name)) {
            		if (usrs.size() > 0) {
	            		exts.addAll(0, usrs);
	            		usrs.clear();
            		}
            	} else {
	            	T ext = getExtension(name);
	            	usrs.add(ext);
            	}
            }
        }
        if (usrs.size() > 0) {
        	exts.addAll(usrs);
        }
        return exts;
    }

 private boolean isMatchGroup(String group, String[] groups) {
        if (group == null || group.length() == 0) {
            return true;
        }
        if (groups != null && groups.length > 0) {
            for (String g : groups) {
                if (group.equals(g)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isActive(Activate activate, URL url) {
        String[] keys = activate.value();
        if (keys == null || keys.length == 0) {
            return true;
        }
        for (String key : keys) {
            for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if ((k.equals(key) || k.endsWith("." + key))
                        && ConfigUtils.isNotEmpty(v)) {
                    return true;
                }
            }
        }
        return false;
    }
```

源码解析：

1. 有多个重载方法可以获得自动激活的扩展实例列表。
2. 如果values（即扩展点名称）列表中不包含默认值“-default”则开始遍历map对象cachedActivates中的所有activate。
3. 若扩展点被Activate注解标准，则检查activate中的group是否匹配





# 五  dubbo的SPI机制自适应Adpative类分析

> 摘要: dubbo的SPI机制中为了可以根据URL中的参数灵活选择扩展实现类，设计了一种Adpative机制，通过类似于：ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension() 这种方法获得的是一个动态自适应的扩展对象，该对象不是真正的扩展实现，它只是代理了扩展点实现，将真正的接口调用还是转交给自适应的目标扩展点实现方法。
>

## 使用介绍

扩展点的Adpative类可以有两种方式实现，一种方式是人工实现Adpative类，然后配置成为该类型的自适应类；另外一种方法是如果没有人工指定的Adpative类，则dubbo的SPI机制会自动生成和编译一个动态的Adpative类。

## 人工设置

人工设置扩展点自适应实现类会非常灵活，可以由开发者灵活控制，但是缺点是如果有很多扩展点，自适应逻辑相同或者相似则会出现类爆炸的问题。

我们以编译器扩展点Compiler为例。

扩展点的源码如下：

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.common.compiler;


import com.alibaba.dubbo.common.extension.SPI;

/**
 * Compiler. (SPI, Singleton, ThreadSafe)
 * 
 * @author william.liangf
 */
@SPI("javassist")
public interface Compiler {

	/**
	 * Compile java source code.
	 * 
	 * @param code Java source code
	 * @param classLoader TODO
	 * @return Compiled class
	 */
	Class<?> compile(String code, ClassLoader classLoader);

}
```

它的自适应扩展点实现类AdaptiveCompiler源码如下：

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.common.compiler.support;


import com.alibaba.dubbo.common.compiler.Compiler;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.ExtensionLoader;

/**
 * AdaptiveCompiler. (SPI, Singleton, ThreadSafe)
 * 
 * @author william.liangf
 */
@Adaptive
public class AdaptiveCompiler implements Compiler {

    private static volatile String DEFAULT_COMPILER;

    public static void setDefaultCompiler(String compiler) {
        DEFAULT_COMPILER = compiler;
    }

    public Class<?> compile(String code, ClassLoader classLoader) {
        Compiler compiler;
        ExtensionLoader<Compiler> loader = ExtensionLoader.getExtensionLoader(Compiler.class);
        String name = DEFAULT_COMPILER; // copy reference
        if (name != null && name.length() > 0) {
            compiler = loader.getExtension(name);
        } else {
            compiler = loader.getDefaultExtension();
        }
        return compiler.compile(code, classLoader);
    }

}
```

可以看出自适应实现类本身并没有实现compie方法，它是由参数DEFAULT_COMPILER指定一个默认的扩展点名称，因此是可以动态调整的。

## 自动生成

由于大部分的扩展点自适应实现类的代码逻辑都相似，因此自动生成动态的自适应扩展类则会给开发者带来很大的便利，省去了很多冗余代码。但是由于dubbo的实现方式是通过代码来自动生成自适应实现的代码，代码可读性非常差。这也是缺点。我们通过调试将其生成的代码打印出来将大大提高代码可读性。

### Protocol扩展点

我们将以扩展点Protocol为例来展示生成的代码。

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

/**
 * Protocol. (API/SPI, Singleton, ThreadSafe)
 * 
 * @author william.liangf
 */
@SPI("dubbo")
public interface Protocol {
    
    /**
     * 获取缺省端口，当用户没有配置端口时使用。
     * 
     * @return 缺省端口
     */
    int getDefaultPort();

    /**
     * 暴露远程服务：<br>
     * 1. 协议在接收请求时，应记录请求来源方地址信息：RpcContext.getContext().setRemoteAddress();<br>
     * 2. export()必须是幂等的，也就是暴露同一个URL的Invoker两次，和暴露一次没有区别。<br>
     * 3. export()传入的Invoker由框架实现并传入，协议不需要关心。<br>
     * 
     * @param <T> 服务的类型
     * @param invoker 服务的执行体
     * @return exporter 暴露服务的引用，用于取消暴露
     * @throws RpcException 当暴露服务出错时抛出，比如端口已占用
     */
    @Adaptive
    <T> Exporter<T> export(Invoker<T> invoker) throws RpcException;

    /**
     * 引用远程服务：<br>
     * 1. 当用户调用refer()所返回的Invoker对象的invoke()方法时，协议需相应执行同URL远端export()传入的Invoker对象的invoke()方法。<br>
     * 2. refer()返回的Invoker由协议实现，协议通常需要在此Invoker中发送远程请求。<br>
     * 3. 当url中有设置check=false时，连接失败不能抛出异常，并内部自动恢复。<br>
     * 
     * @param <T> 服务的类型
     * @param type 服务的类型
     * @param url 远程服务的URL地址
     * @return invoker 服务的本地代理
     * @throws RpcException 当连接服务提供方失败时抛出
     */
    @Adaptive
    <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException;

    /**
     * 释放协议：<br>
     * 1. 取消该协议所有已经暴露和引用的服务。<br>
     * 2. 释放协议所占用的所有资源，比如连接和端口。<br>
     * 3. 协议在释放后，依然能暴露和引用新的服务。<br>
     */
    void destroy();

}
```

它生成的扩展自适应实现类源码如下：

```java
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.extension.ExtensionLoader;

public class Protocol$Adpative implements com.alibaba.dubbo.rpc.Protocol {
	public void destroy() {
		throw new UnsupportedOperationException(
				"method public abstract void com.alibaba.dubbo.rpc.Protocol.destroy() of interface com.alibaba.dubbo.rpc.Protocol is not adaptive method!");
	}

	public int getDefaultPort() {
		throw new UnsupportedOperationException(
				"method public abstract int com.alibaba.dubbo.rpc.Protocol.getDefaultPort() of interface com.alibaba.dubbo.rpc.Protocol is not adaptive method!");
	}

	public com.alibaba.dubbo.rpc.Exporter export(
			com.alibaba.dubbo.rpc.Invoker arg0)
			throws com.alibaba.dubbo.rpc.RpcException {
		if (arg0 == null)
			throw new IllegalArgumentException(
					"com.alibaba.dubbo.rpc.Invoker argument == null");
		if (arg0.getUrl() == null)
			throw new IllegalArgumentException(
					"com.alibaba.dubbo.rpc.Invoker argument getUrl() == null");
		com.alibaba.dubbo.common.URL url = arg0.getUrl();
		String extName = (url.getProtocol() == null ? "dubbo" : url
				.getProtocol());
		if (extName == null)
			throw new IllegalStateException(
					"Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url("
							+ url.toString() + ") use keys([protocol])");
		com.alibaba.dubbo.rpc.Protocol extension = (com.alibaba.dubbo.rpc.Protocol) ExtensionLoader
				.getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class)
				.getExtension(extName);
		return extension.export(arg0);
	}

	public com.alibaba.dubbo.rpc.Invoker refer(java.lang.Class arg0,
			com.alibaba.dubbo.common.URL arg1)
			throws com.alibaba.dubbo.rpc.RpcException {
		if (arg1 == null)
			throw new IllegalArgumentException("url == null");
		com.alibaba.dubbo.common.URL url = arg1;
		String extName = (url.getProtocol() == null ? "dubbo" : url
				.getProtocol());
		if (extName == null)
			throw new IllegalStateException(
					"Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url("
							+ url.toString() + ") use keys([protocol])");
		com.alibaba.dubbo.rpc.Protocol extension = (com.alibaba.dubbo.rpc.Protocol) ExtensionLoader
				.getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class)
				.getExtension(extName);
		return extension.refer(arg0, arg1);
	}
}
```

从生成的源码可以看出来这些特点：

1. 只会代理扩展点接口上有@Adaptive标注的方法。没有标注的方法调用会抛出不支持该方法的异常。
2. 从类型为url的参数或者参数的url属性中获得url对象，从url中获得value作为扩展点的名称。
3. 从扩展点加载器中获得对应名称的扩展点。
4. 再调用扩展点的方法。

### Transporter扩展点

扩展点源码如下。

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.remoting;

import javax.sound.midi.Receiver;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

/**
 * Transporter. (SPI, Singleton, ThreadSafe)
 * 
 * <a href="http://en.wikipedia.org/wiki/Transport_Layer">Transport Layer</a>
 * <a href="http://en.wikipedia.org/wiki/Client%E2%80%93server_model">Client/Server</a>
 * 
 * @see com.alibaba.dubbo.remoting.Transporters
 * @author ding.lid
 * @author william.liangf
 */
@SPI("netty")
public interface Transporter {

    /**
     * Bind a server.
     * 
     * @see com.alibaba.dubbo.remoting.Transporters#bind(URL, Receiver, ChannelHandler)
     * @param url server url
     * @param handler
     * @return server
     * @throws RemotingException 
     */
    @Adaptive({Constants.SERVER_KEY, Constants.TRANSPORTER_KEY})
    Server bind(URL url, ChannelHandler handler) throws RemotingException;

    /**
     * Connect to a server.
     * 
     * @see com.alibaba.dubbo.remoting.Transporters#connect(URL, Receiver, ChannelListener)
     * @param url server url
     * @param handler
     * @return client
     * @throws RemotingException 
     */
    @Adaptive({Constants.CLIENT_KEY, Constants.TRANSPORTER_KEY})
    Client connect(URL url, ChannelHandler handler) throws RemotingException;

}
```

生成的自适应扩展实现源码如下。

```java
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.extension.ExtensionLoader;

public class Transporter$Adpative implements
		com.alibaba.dubbo.remoting.Transporter {
	public com.alibaba.dubbo.remoting.Client connect(
			com.alibaba.dubbo.common.URL arg0,
			com.alibaba.dubbo.remoting.ChannelHandler arg1)
			throws com.alibaba.dubbo.remoting.RemotingException {
		if (arg0 == null)
			throw new IllegalArgumentException("url == null");
		com.alibaba.dubbo.common.URL url = arg0;
		String extName = url.getParameter("client", url.getParameter(
				"transporter", "netty"));
		if (extName == null)
			throw new IllegalStateException(
					"Fail to get extension(com.alibaba.dubbo.remoting.Transporter) name from url("
							+ url.toString()
							+ ") use keys([client, transporter])");
		com.alibaba.dubbo.remoting.Transporter extension = (com.alibaba.dubbo.remoting.Transporter) ExtensionLoader
				.getExtensionLoader(
						com.alibaba.dubbo.remoting.Transporter.class)
				.getExtension(extName);
		return extension.connect(arg0, arg1);
	}

	public com.alibaba.dubbo.remoting.Server bind(
			com.alibaba.dubbo.common.URL arg0,
			com.alibaba.dubbo.remoting.ChannelHandler arg1)
			throws com.alibaba.dubbo.remoting.RemotingException {
		if (arg0 == null)
			throw new IllegalArgumentException("url == null");
		com.alibaba.dubbo.common.URL url = arg0;
		String extName = url.getParameter("server", url.getParameter(
				"transporter", "netty"));
		if (extName == null)
			throw new IllegalStateException(
					"Fail to get extension(com.alibaba.dubbo.remoting.Transporter) name from url("
							+ url.toString()
							+ ") use keys([server, transporter])");
		com.alibaba.dubbo.remoting.Transporter extension = (com.alibaba.dubbo.remoting.Transporter) ExtensionLoader
				.getExtensionLoader(
						com.alibaba.dubbo.remoting.Transporter.class)
				.getExtension(extName);
		return extension.bind(arg0, arg1);
	}
}
```

区别点是调用了String extName = url.getParameter("server", url.getParameter( "transporter", "netty"));来获得扩展名称。





# 六  dubbo-cluster模块源码分析

> 摘要: dubbo-cluster也是dubbo中最为重要的一个模块，该模块实现了多种集群容错特性（支持包括failover, failsafe， failback, froking, boradcast多种集群通错特性），还实现了目录服务，负载均衡，路由策略和服务治理配置等特性。因此dubbo除了rpc之外另外一个非常重要的特性——服务治理特性也是主要通过该模块来实现的，研究该模块的源码对于我们理解和自行扩展服务治理都非常有帮助。
>
> ## 模块功能介绍
>

该模块的使用介绍请参考dubbo官方用户手册如下章节内容。

- [集群容错](http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E9%9B%86%E7%BE%A4%E5%AE%B9%E9%94%99)
- [负载均衡](http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E8%B4%9F%E8%BD%BD%E5%9D%87%E8%A1%A1)
- [路由规则](http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E8%B7%AF%E7%94%B1%E8%A7%84%E5%88%99)
- [配置规则](http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E9%85%8D%E7%BD%AE%E8%A7%84%E5%88%99)
- [注册中心参考手册](http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E6%B3%A8%E5%86%8C%E4%B8%AD%E5%BF%83%E5%8F%82%E8%80%83%E6%89%8B%E5%86%8C)

其中注册中心其实是对于目录服务的一种实现方式，本文不会对注册中心进行详细讲解。

 

## 核心类图

![img](http://static.oschina.net/uploads/space/2016/0612/113048_SN1P_113011.jpg)

## 核心源码分析

### 核心接口概念及关系

![img](http://static.oschina.net/uploads/space/2016/0612/151439_EONF_113011.jpg)

各节点关系：

- 这里的Invoker是Provider的一个可调用Service的抽象，Invoker封装了Provider地址及Service接口信息。
- Directory代表多个Invoker，可以把它看成List<Invoker>，但与List不同的是，它的值可能是动态变化的，比如注册中心推送变更。
- Cluster将Directory中的多个Invoker伪装成一个Invoker，对上层透明，伪装过程包含了容错逻辑，调用失败后，重试另一个。
- Router负责从多个Invoker中按路由规则选出子集，比如读写分离，应用隔离等。
- LoadBalance负责从多个Invoker中选出具体的一个用于本次调用，选的过程包含了负载均衡算法，调用失败后，需要重选。

由于每种接口都有多种实现类，篇幅和时间有限，我们选择其中最为典型的一种来进行源码分析。

### Cluster

#### 扩展接口介绍

集群的源码如下。

```java
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.support.FailoverCluster;

/**
 * Cluster. (SPI, Singleton, ThreadSafe)
 * 
 * <a href="http://en.wikipedia.org/wiki/Computer_cluster">Cluster</a>
 * <a href="http://en.wikipedia.org/wiki/Fault-tolerant_system">Fault-Tolerant</a>
 * 
 * @author william.liangf
 */
@SPI(FailoverCluster.NAME)
public interface Cluster {

    /**
     * Merge the directory invokers to a virtual invoker.
     * 
     * @param <T>
     * @param directory
     * @return cluster invoker
     * @throws RpcException
     */
    @Adaptive
    <T> Invoker<T> join(Directory<T> directory) throws RpcException;

}
```

该接口只有一个方法，就是将directory对象中的多个invoker的集合整合成一个invoker对象。该方法被ReferenceConfig类的createProxy方法调用，调用它的代码如下。

```java
 // 对有注册中心的Cluster 只用 AvailableCluster
                    URL u = registryURL.addParameter(Constants.CLUSTER_KEY, AvailableCluster.NAME); 
                    invoker = cluster.join(new StaticDirectory(u, invokers));
```

Cluster内置有9个扩展实现类，都实现了不同的集群容错策略，我们只分析默认的自动故障转移的扩展实现FailoverCluster。

#### FailoverCluster

源码如下，只是构造了一个类型为FailoverClusterInvoker的invoker对象。

```java
public class FailoverCluster implements Cluster {

    public final static String NAME = "failover";

    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new FailoverClusterInvoker<T>(directory);
    }

}
```

我们进入看看FailoverClusterInvoker的源码。

```java
/**
 * 失败转移，当出现失败，重试其它服务器，通常用于读操作，但重试会带来更长延迟。
 * 
 * <a href="http://en.wikipedia.org/wiki/Failover">Failover</a>
 * 
 * @author william.liangf
 * @author chao.liuc
 */
public class FailoverClusterInvoker<T> extends AbstractClusterInvoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(FailoverClusterInvoker.class);

    public FailoverClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Result doInvoke(Invocation invocation, final List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
    	List<Invoker<T>> copyinvokers = invokers;
    	checkInvokers(copyinvokers, invocation);
        int len = getUrl().getMethodParameter(invocation.getMethodName(), Constants.RETRIES_KEY, Constants.DEFAULT_RETRIES) + 1;
        if (len <= 0) {
            len = 1;
        }
        // retry loop.
        RpcException le = null; // last exception.
        List<Invoker<T>> invoked = new ArrayList<Invoker<T>>(copyinvokers.size()); // invoked invokers.
        Set<String> providers = new HashSet<String>(len);
        for (int i = 0; i < len; i++) {
        	//重试时，进行重新选择，避免重试时invoker列表已发生变化.
        	//注意：如果列表发生了变化，那么invoked判断会失效，因为invoker示例已经改变
        	if (i > 0) {
        		checkWheatherDestoried();
        		copyinvokers = list(invocation);
        		//重新检查一下
        		checkInvokers(copyinvokers, invocation);
        	}
            Invoker<T> invoker = select(loadbalance, invocation, copyinvokers, invoked);
            invoked.add(invoker);
            RpcContext.getContext().setInvokers((List)invoked);
            try {
                Result result = invoker.invoke(invocation);
                if (le != null && logger.isWarnEnabled()) {
                    logger.warn("Although retry the method " + invocation.getMethodName()
                            + " in the service " + getInterface().getName()
                            + " was successful by the provider " + invoker.getUrl().getAddress()
                            + ", but there have been failed providers " + providers 
                            + " (" + providers.size() + "/" + copyinvokers.size()
                            + ") from the registry " + directory.getUrl().getAddress()
                            + " on the consumer " + NetUtils.getLocalHost()
                            + " using the dubbo version " + Version.getVersion() + ". Last error is: "
                            + le.getMessage(), le);
                }
                return result;
            } catch (RpcException e) {
                if (e.isBiz()) { // biz exception.
                    throw e;
                }
                le = e;
            } catch (Throwable e) {
                le = new RpcException(e.getMessage(), e);
            } finally {
                providers.add(invoker.getUrl().getAddress());
            }
        }
        throw new RpcException(le != null ? le.getCode() : 0, "Failed to invoke the method "
                + invocation.getMethodName() + " in the service " + getInterface().getName() 
                + ". Tried " + len + " times of the providers " + providers 
                + " (" + providers.size() + "/" + copyinvokers.size() 
                + ") from the registry " + directory.getUrl().getAddress()
                + " on the consumer " + NetUtils.getLocalHost() + " using the dubbo version "
                + Version.getVersion() + ". Last error is: "
                + (le != null ? le.getMessage() : ""), le != null && le.getCause() != null ? le.getCause() : le);
    }

}
```

该类又继承自抽象实现类AbstractClusterInvoker，使用该类的一些方法，因此也要结合该类的源码一起看。

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.support.RpcUtils;

/**
 * AbstractClusterInvoker
 * 
 * @author william.liangf
 * @author chao.liuc
 */
public abstract class AbstractClusterInvoker<T> implements Invoker<T> {

    private static final Logger                logger                            = LoggerFactory
                                                                                         .getLogger(AbstractClusterInvoker.class);
    protected final Directory<T>               directory;

    protected final boolean                    availablecheck;
    
    private volatile boolean                   destroyed = false;

    private volatile Invoker<T>                stickyInvoker                     = null;

    public AbstractClusterInvoker(Directory<T> directory) {
        this(directory, directory.getUrl());
    }
    
    public AbstractClusterInvoker(Directory<T> directory, URL url) {
        if (directory == null)
            throw new IllegalArgumentException("service directory == null");
        
        this.directory = directory ;
        //sticky 需要检测 avaliablecheck 
        this.availablecheck = url.getParameter(Constants.CLUSTER_AVAILABLE_CHECK_KEY, Constants.DEFAULT_CLUSTER_AVAILABLE_CHECK) ;
    }

    public Class<T> getInterface() {
        return directory.getInterface();
    }

    public URL getUrl() {
        return directory.getUrl();
    }

    public boolean isAvailable() {
        Invoker<T> invoker = stickyInvoker;
        if (invoker != null) {
            return invoker.isAvailable();
        }
        return directory.isAvailable();
    }

    public void destroy() {
        directory.destroy();
        destroyed = true;
    }

    /**
     * 使用loadbalance选择invoker.</br>
     * a)先lb选择，如果在selected列表中 或者 不可用且做检验时，进入下一步(重选),否则直接返回</br>
     * b)重选验证规则：selected > available .保证重选出的结果尽量不在select中，并且是可用的 
     * 
     * @param availablecheck 如果设置true，在选择的时候先选invoker.available == true
     * @param selected 已选过的invoker.注意：输入保证不重复
     * 
     */
    protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.size() == 0)
            return null;
        String methodName = invocation == null ? "" : invocation.getMethodName();
        
        boolean sticky = invokers.get(0).getUrl().getMethodParameter(methodName,Constants.CLUSTER_STICKY_KEY, Constants.DEFAULT_CLUSTER_STICKY) ;
        {
            //ignore overloaded method
            if ( stickyInvoker != null && !invokers.contains(stickyInvoker) ){
                stickyInvoker = null;
            }
            //ignore cucurrent problem
            if (sticky && stickyInvoker != null && (selected == null || !selected.contains(stickyInvoker))){
                if (availablecheck && stickyInvoker.isAvailable()){
                    return stickyInvoker;
                }
            }
        }
        Invoker<T> invoker = doselect(loadbalance, invocation, invokers, selected);
        
        if (sticky){
            stickyInvoker = invoker;
        }
        return invoker;
    }
    
    private Invoker<T> doselect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.size() == 0)
            return null;
        if (invokers.size() == 1)
            return invokers.get(0);
        // 如果只有两个invoker，退化成轮循
        if (invokers.size() == 2 && selected != null && selected.size() > 0) {
            return selected.get(0) == invokers.get(0) ? invokers.get(1) : invokers.get(0);
        }
        Invoker<T> invoker = loadbalance.select(invokers, getUrl(), invocation);
        
        //如果 selected中包含（优先判断） 或者 不可用&&availablecheck=true 则重试.
        if( (selected != null && selected.contains(invoker))
                ||(!invoker.isAvailable() && getUrl()!=null && availablecheck)){
            try{
                Invoker<T> rinvoker = reselect(loadbalance, invocation, invokers, selected, availablecheck);
                if(rinvoker != null){
                    invoker =  rinvoker;
                }else{
                    //看下第一次选的位置，如果不是最后，选+1位置.
                    int index = invokers.indexOf(invoker);
                    try{
                        //最后在避免碰撞
                        invoker = index <invokers.size()-1?invokers.get(index+1) :invoker;
                    }catch (Exception e) {
                        logger.warn(e.getMessage()+" may because invokers list dynamic change, ignore.",e);
                    }
                }
            }catch (Throwable t){
                logger.error("clustor relselect fail reason is :"+t.getMessage() +" if can not slove ,you can set cluster.availablecheck=false in url",t);
            }
        }
        return invoker;
    } 
    
    /**
     * 重选，先从非selected的列表中选择，没有在从selected列表中选择.
     * @param loadbalance
     * @param invocation
     * @param invokers
     * @param selected
     * @return
     * @throws RpcException
     */
    private Invoker<T> reselect(LoadBalance loadbalance,Invocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected ,boolean availablecheck)
            throws RpcException {
        
        //预先分配一个，这个列表是一定会用到的.
        List<Invoker<T>> reselectInvokers = new ArrayList<Invoker<T>>(invokers.size()>1?(invokers.size()-1):invokers.size());
        
        //先从非select中选
        if( availablecheck ){ //选isAvailable 的非select
            for(Invoker<T> invoker : invokers){
                if(invoker.isAvailable()){
                    if(selected ==null || !selected.contains(invoker)){
                        reselectInvokers.add(invoker);
                    }
                }
            }
            if(reselectInvokers.size()>0){
                return  loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        }else{ //选全部非select
            for(Invoker<T> invoker : invokers){
                if(selected ==null || !selected.contains(invoker)){
                    reselectInvokers.add(invoker);
                }
            }
            if(reselectInvokers.size()>0){
                return  loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        }
        //最后从select中选可用的. 
        {
            if(selected != null){
                for(Invoker<T> invoker : selected){
                    if((invoker.isAvailable()) //优先选available 
                            && !reselectInvokers.contains(invoker)){
                        reselectInvokers.add(invoker);
                    }
                }
            }
            if(reselectInvokers.size()>0){
                return  loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        }
        return null;
    }
    
    public Result invoke(final Invocation invocation) throws RpcException {

        checkWheatherDestoried();

        LoadBalance loadbalance;
        
        List<Invoker<T>> invokers = list(invocation);
        if (invokers != null && invokers.size() > 0) {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl()
                    .getMethodParameter(invocation.getMethodName(),Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
        } else {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(Constants.DEFAULT_LOADBALANCE);
        }
        RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
        return doInvoke(invocation, invokers, loadbalance);
    }

    protected void checkWheatherDestoried() {

        if(destroyed){
            throw new RpcException("Rpc cluster invoker for " + getInterface() + " on consumer " + NetUtils.getLocalHost()
                    + " use dubbo version " + Version.getVersion()
                    + " is now destroyed! Can not invoke any more.");
        }
    }

    @Override
    public String toString() {
        return getInterface() + " -> " + getUrl().toString();
    }
    
    protected void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) {
        if (invokers == null || invokers.size() == 0) {
            throw new RpcException("Failed to invoke the method "
                    + invocation.getMethodName() + " in the service " + getInterface().getName() 
                    + ". No provider available for the service " + directory.getUrl().getServiceKey()
                    + " from registry " + directory.getUrl().getAddress() 
                    + " on the consumer " + NetUtils.getLocalHost()
                    + " using the dubbo version " + Version.getVersion()
                    + ". Please check if the providers have been started and registered.");
        }
    }

    protected abstract Result doInvoke(Invocation invocation, List<Invoker<T>> invokers,
                                       LoadBalance loadbalance) throws RpcException;
    
    protected  List<Invoker<T>> list(Invocation invocation) throws RpcException {
    	List<Invoker<T>> invokers = directory.list(invocation);
    	return invokers;
    }
}
```

源码实现分析。

1. AbstractClusterInvoker的invoke方法提供了一个骨架实现。逻辑是检查对象是否销毁状态，从directory获得invoker列表，获得loadbalance扩展实现对象，然后调用抽象方法doInvoke去执行真正的逻辑，交由具体子类实现。
2. AbstractClusterInvoker实现了一个公共的protected的方法select，该方法实现了使用loadbalance选择合适的invoker对象。在选择方法的实现中支持  [粘滞连接](http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E7%B2%98%E6%BB%9E%E8%BF%9E%E6%8E%A5) 特性。作为一个公共特性，所有的子类都支持。最后再调用private方法doselect实现进一步选择逻辑。
3. AbstractClusterInvoker的doselect方法实现了真正的选择invoker逻辑。首先检查可选invoker，若没有则返回null；如果有2个可选invoker则退化为轮询；否则继续调用loadbalance的select方法选择一个invoker；然后在检查选中的invoker是否已经使用过或者不可用，如果不可用则会调用reselect重新选择，若重新选择成功则使用它，否则则使用invoker列表中当前index+1的invoker，如果已经是最后一个则直接使用当前的invoker。
4. AbstractClusterInvoker的reselect方法的实现逻辑是：如果availablecheck标志为true，则只将未被selected的可用状态的invoker交给loadbalance进行选择，否则将所有的未被selected的invoker交给loadbalance选择，若可重新选择的invoker为空，则将selected的invoker列表交给loadbalance进行选择。
5. FailoverClusterInvoker的doInvoke方法的实现逻辑为：检查invoker列表状态；获得参数中重试次数，默认次数是2；从directory获得invoker列表；调用select方法选择一个invoker；将选择的invoker加入到invoked集合，表示已经选择和使用的；调用invoker.invoke()方法，若成功则返回result，若抛出的是业务异常则抛出，否则继续重试选择并调用下一个invoker；

### LoadBalance

负载均衡器

#### 扩展接口定义

```java
@SPI(RandomLoadBalance.NAME)
public interface LoadBalance {

	/**
	 * select one invoker in list.
	 * 
	 * @param invokers invokers.
	 * @param url refer url
	 * @param invocation invocation.
	 * @return selected invoker.
	 */
    @Adaptive("loadbalance")
	<T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;

}
```

上述源码所示，负载均衡只定义了一个方法，就是在候选的invokers中选择一个invoker对象出来。默认的扩展实现是random。那我么就分析RandomLoadBalance的源码。

#### RandomLoadBalance

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.util.List;
import java.util.Random;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * random load balance.
 *
 * @author qianlei
 * @author william.liangf
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "random";

    private final Random random = new Random();

    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size(); // 总个数
        int totalWeight = 0; // 总权重
        boolean sameWeight = true; // 权重是否都一样
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            totalWeight += weight; // 累计总权重
            if (sameWeight && i > 0
                    && weight != getWeight(invokers.get(i - 1), invocation)) {
                sameWeight = false; // 计算所有权重是否一样
            }
        }
        if (totalWeight > 0 && ! sameWeight) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offset = random.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (int i = 0; i < length; i++) {
                offset -= getWeight(invokers.get(i), invocation);
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }
        // 如果权重相同或权重为0则均等随机
        return invokers.get(random.nextInt(length));
    }

}
```

该类继承了抽象类AbstractLoadBalance，因此我们也要结合该类一起分析。

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

/**
 * AbstractLoadBalance
 * 
 * @author william.liangf
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        if (invokers == null || invokers.size() == 0)
            return null;
        if (invokers.size() == 1)
            return invokers.get(0);
        return doSelect(invokers, url, invocation);
    }

    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation);

    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT);
        if (weight > 0) {
	        long timestamp = invoker.getUrl().getParameter(Constants.TIMESTAMP_KEY, 0L);
	    	if (timestamp > 0L) {
	    		int uptime = (int) (System.currentTimeMillis() - timestamp);
	    		int warmup = invoker.getUrl().getParameter(Constants.WARMUP_KEY, Constants.DEFAULT_WARMUP);
	    		if (uptime > 0 && uptime < warmup) {
	    			weight = calculateWarmupWeight(uptime, warmup, weight);
	    		}
	    	}
        }
    	return weight;
    }
    
    static int calculateWarmupWeight(int uptime, int warmup, int weight) {
    	int ww = (int) ( (float) uptime / ( (float) warmup / (float) weight ) );
    	return ww < 1 ? 1 : (ww > weight ? weight : ww);
    }

}
```

源码分析如下：

1. AbstractLoadBalance的select方法实现，只是做了参数校验，invoker列表若0个则返回null，1个元素则直接返回；否则调用抽象方法doSelect交给子类实现。
2. AbstractLoadBalance定义了公共方法getWeight。该方法是获取invoker的权重的方法，公式是：(int) ( (float) uptime / ( (float) warmup / (float) weight ) );
3. 如果未设置权重或者权重值都一样，则直接调用random.nextInt()随机获得一个invoker；若设置了权重并且不一样，则在总权重中随机，分布在哪个invoker的分片上，则选择该invoker对象，实现了按照权重随机。

### Router

#### 接口定义

```java
public interface Router extends Comparable<Router> {

    /**
     * get the router url.
     * 
     * @return url
     */
    URL getUrl();

    /**
     * route.
     * 
     * @param invokers
     * @param url refer url
     * @param invocation
     * @return routed invokers
     * @throws RpcException
     */
	<T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;

}
```

路由器就定义了上述2个方法，核心方法是route，从大的invoker列表结合中根据规则过滤出一个子集合。我们这里只分析实现类ConditionRouter的源码。

#### ConditionRouter

```java
/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.cluster.router.condition;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;

/**
 * ConditionRouter
 * 
 * @author william.liangf
 */
public class ConditionRouter implements Router, Comparable<Router> {
    
    private static final Logger logger = LoggerFactory.getLogger(ConditionRouter.class);

    private final URL url;
    
    private final int priority;

    private final boolean force;

    private final Map<String, MatchPair> whenCondition;
    
    private final Map<String, MatchPair> thenCondition;

    public ConditionRouter(URL url) {
        this.url = url;
        this.priority = url.getParameter(Constants.PRIORITY_KEY, 0);
        this.force = url.getParameter(Constants.FORCE_KEY, false);
        try {
            String rule = url.getParameterAndDecoded(Constants.RULE_KEY);
            if (rule == null || rule.trim().length() == 0) {
                throw new IllegalArgumentException("Illegal route rule!");
            }
            rule = rule.replace("consumer.", "").replace("provider.", "");
            int i = rule.indexOf("=>");
            String whenRule = i < 0 ? null : rule.substring(0, i).trim();
            String thenRule = i < 0 ? rule.trim() : rule.substring(i + 2).trim();
            Map<String, MatchPair> when = StringUtils.isBlank(whenRule) || "true".equals(whenRule) ? new HashMap<String, MatchPair>() : parseRule(whenRule);
            Map<String, MatchPair> then = StringUtils.isBlank(thenRule) || "false".equals(thenRule) ? null : parseRule(thenRule);
            // NOTE: When条件是允许为空的，外部业务来保证类似的约束条件
            this.whenCondition = when;
            this.thenCondition = then;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation)
            throws RpcException {
        if (invokers == null || invokers.size() == 0) {
            return invokers;
        }
        try {
            if (! matchWhen(url)) {
                return invokers;
            }
            List<Invoker<T>> result = new ArrayList<Invoker<T>>();
            if (thenCondition == null) {
            	logger.warn("The current consumer in the service blacklist. consumer: " + NetUtils.getLocalHost() + ", service: " + url.getServiceKey());
                return result;
            }
            for (Invoker<T> invoker : invokers) {
                if (matchThen(invoker.getUrl(), url)) {
                    result.add(invoker);
                }
            }
            if (result.size() > 0) {
                return result;
            } else if (force) {
            	logger.warn("The route result is empty and force execute. consumer: " + NetUtils.getLocalHost() + ", service: " + url.getServiceKey() + ", router: " + url.getParameterAndDecoded(Constants.RULE_KEY));
            	return result;
            }
        } catch (Throwable t) {
            logger.error("Failed to execute condition router rule: " + getUrl() + ", invokers: " + invokers + ", cause: " + t.getMessage(), t);
        }
        return invokers;
    }

    public URL getUrl() {
        return url;
    }

    public int compareTo(Router o) {
        if (o == null || o.getClass() != ConditionRouter.class) {
            return 1;
        }
        ConditionRouter c = (ConditionRouter) o;
        return this.priority == c.priority ? url.toFullString().compareTo(c.url.toFullString()) : (this.priority > c.priority ? 1 : -1);
    }

    public boolean matchWhen(URL url) {
        return matchCondition(whenCondition, url, null);
    }

    public boolean matchThen(URL url, URL param) {
        return thenCondition != null && matchCondition(thenCondition, url, param);
    }
    
    private boolean matchCondition(Map<String, MatchPair> condition, URL url, URL param) {
        Map<String, String> sample = url.toMap();
        for (Map.Entry<String, String> entry : sample.entrySet()) {
            String key = entry.getKey();
            MatchPair pair = condition.get(key);
            if (pair != null && ! pair.isMatch(entry.getValue(), param)) {
                return false;
            }
        }
        return true;
    }
    
    private static Pattern ROUTE_PATTERN = Pattern.compile("([&!=,]*)\\s*([^&!=,\\s]+)");
    
    private static Map<String, MatchPair> parseRule(String rule)
            throws ParseException {
        Map<String, MatchPair> condition = new HashMap<String, MatchPair>();
        if(StringUtils.isBlank(rule)) {
            return condition;
        }        
        // 匹配或不匹配Key-Value对
        MatchPair pair = null;
        // 多个Value值
        Set<String> values = null;
        final Matcher matcher = ROUTE_PATTERN.matcher(rule);
        while (matcher.find()) { // 逐个匹配
            String separator = matcher.group(1);
            String content = matcher.group(2);
            // 表达式开始
            if (separator == null || separator.length() == 0) {
                pair = new MatchPair();
                condition.put(content, pair);
            }
            // KV开始
            else if ("&".equals(separator)) {
                if (condition.get(content) == null) {
                    pair = new MatchPair();
                    condition.put(content, pair);
                } else {
                    condition.put(content, pair);
                }
            }
            // KV的Value部分开始
            else if ("=".equals(separator)) {
                if (pair == null)
                    throw new ParseException("Illegal route rule \""
                            + rule + "\", The error char '" + separator
                            + "' at index " + matcher.start() + " before \""
                            + content + "\".", matcher.start());

                values = pair.matches;
                values.add(content);
            }
            // KV的Value部分开始
            else if ("!=".equals(separator)) {
                if (pair == null)
                    throw new ParseException("Illegal route rule \""
                            + rule + "\", The error char '" + separator
                            + "' at index " + matcher.start() + " before \""
                            + content + "\".", matcher.start());

                values = pair.mismatches;
                values.add(content);
            }
            // KV的Value部分的多个条目
            else if (",".equals(separator)) { // 如果为逗号表示
                if (values == null || values.size() == 0)
                    throw new ParseException("Illegal route rule \""
                            + rule + "\", The error char '" + separator
                            + "' at index " + matcher.start() + " before \""
                            + content + "\".", matcher.start());
                values.add(content);
            } else {
                throw new ParseException("Illegal route rule \"" + rule
                        + "\", The error char '" + separator + "' at index "
                        + matcher.start() + " before \"" + content + "\".", matcher.start());
            }
        }
        return condition;
    }

    private static final class MatchPair {
        final Set<String> matches = new HashSet<String>();
        final Set<String> mismatches = new HashSet<String>();
        public boolean isMatch(String value, URL param) {
            for (String match : matches) {
                if (! UrlUtils.isMatchGlobPattern(match, value, param)) {
                    return false;
                }
            }
            for (String mismatch : mismatches) {
                if (UrlUtils.isMatchGlobPattern(mismatch, value, param)) {
                    return false;
                }
            }
            return true;
        }
    }
}
```

该源码实现了如下条件路由器功能。

基于条件表达式的路由规则，如：

规则：

- "=>"之前的为消费者匹配条件，所有参数和消费者的URL进行对比，当消费者满足匹配条件时，对该消费者执行后面的过滤规则。
- "=>"之后为提供者地址列表的过滤条件，所有参数和提供者的URL进行对比，消费者最终只拿到过滤后的地址列表。
- 如果匹配条件为空，表示对所有消费方应用，如：=> host != 10.20.153.11
- 如果过滤条件为空，表示禁止访问，如：host = 10.20.153.10 =>

表达式：

- 参数支持：
  - 服务调用信息，如：method, argument 等 (暂不支持参数路由)
  - URL本身的字段，如：protocol, host, port 等
  - 以及URL上的所有参数，如：application, organization 等
- 条件支持：
  - 等号"="表示"匹配"，如：host = 10.20.153.10
  - 不等号"!="表示"不匹配"，如：host != 10.20.153.10
- 值支持：
  - 以逗号","分隔多个值，如：host != 10.20.153.10,10.20.153.11
  - 以星号"*"结尾，表示通配，如：host != 10.20.*
  - 以美元符"$"开头，表示引用消费者参数，如：host = $host

### Directory

#### 接口定义

```java
public interface Directory<T> extends Node {
    
    /**
     * get service type.
     * 
     * @return service type.
     */
    Class<T> getInterface();

    /**
     * list invokers.
     * 
     * @return invokers
     */
    List<Invoker<T>> list(Invocation invocation) throws RpcException;
    
}
```

目录服务定义了一个核心接口list，就是列举出某个接口在目录中的所有服务列表。

#### 抽象实现AbstractDirectory

提供了一个抽象的目录实现类，源码如下。

```java
/**
 * 增加router的Directory
 * 
 * @author chao.liuc
 */
public abstract class AbstractDirectory<T> implements Directory<T> {

    // 日志输出
    private static final Logger logger = LoggerFactory.getLogger(AbstractDirectory.class);

    private final URL url ;
    
    private volatile boolean destroyed = false;

    private volatile URL consumerUrl ;
    
	private volatile List<Router> routers;
    
    public AbstractDirectory(URL url) {
        this(url, null);
    }
    
    public AbstractDirectory(URL url, List<Router> routers) {
    	this(url, url, routers);
    }
    
    public AbstractDirectory(URL url, URL consumerUrl, List<Router> routers) {
        if (url == null)
            throw new IllegalArgumentException("url == null");
        this.url = url;
        this.consumerUrl = consumerUrl;
        setRouters(routers);
    }
    
    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        if (destroyed){
            throw new RpcException("Directory already destroyed .url: "+ getUrl());
        }
        List<Invoker<T>> invokers = doList(invocation);
        List<Router> localRouters = this.routers; // local reference
        if (localRouters != null && localRouters.size() > 0) {
            for (Router router: localRouters){
                try {
                    if (router.getUrl() == null || router.getUrl().getParameter(Constants.RUNTIME_KEY, true)) {
                        invokers = router.route(invokers, getConsumerUrl(), invocation);
                    }
                } catch (Throwable t) {
                    logger.error("Failed to execute router: " + getUrl() + ", cause: " + t.getMessage(), t);
                }
            }
        }
        return invokers;
    }
    
    public URL getUrl() {
        return url;
    }
    
    public List<Router> getRouters(){
        return routers;
    }

	public URL getConsumerUrl() {
		return consumerUrl;
	}

	public void setConsumerUrl(URL consumerUrl) {
		this.consumerUrl = consumerUrl;
	}

    protected void setRouters(List<Router> routers){
        // copy list
        routers = routers == null ? new  ArrayList<Router>() : new ArrayList<Router>(routers);
        // append url router
    	String routerkey = url.getParameter(Constants.ROUTER_KEY);
        if (routerkey != null && routerkey.length() > 0) {
            RouterFactory routerFactory = ExtensionLoader.getExtensionLoader(RouterFactory.class).getExtension(routerkey);
            routers.add(routerFactory.getRouter(url));
        }
        // append mock invoker selector
        routers.add(new MockInvokersSelector());
        Collections.sort(routers);
    	this.routers = routers;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void destroy(){
        destroyed = true;
    }

    protected abstract List<Invoker<T>> doList(Invocation invocation) throws RpcException ;

}
```

list方法的实现逻辑是：先检查目录是否销毁状态，若已经销毁则抛出异常；调用抽象方法doList实现真正的从目录服务中获取invoker列表，该方法需要子类实现；循环对象中的路由器列表，若路由器url为null或者参数runtime为true则调用该路由器的route方法进行路由，将返回的invoker列表替换为路由后的结果； 返回最终的invoker列表。

setRouters方法是设置路由器列表，除了参数参入的routers之外，还会追加2个默认的路由器，一个是参数router指定的routerFactory获得的router，另外一个是MockInvokersSelector对象；

#### 默认实现StaticDirectory

模块还提供了一个默认目录实现类StaticDirectory，它是一个静态的内存缓存目录服务实现。源码如下：

```java
public class StaticDirectory<T> extends AbstractDirectory<T> {
    
    private final List<Invoker<T>> invokers;
    
    public StaticDirectory(List<Invoker<T>> invokers){
        this(null, invokers, null);
    }
    
    public StaticDirectory(List<Invoker<T>> invokers, List<Router> routers){
        this(null, invokers, routers);
    }
    
    public StaticDirectory(URL url, List<Invoker<T>> invokers) {
        this(url, invokers, null);
    }

    public StaticDirectory(URL url, List<Invoker<T>> invokers, List<Router> routers) {
        super(url == null && invokers != null && invokers.size() > 0 ? invokers.get(0).getUrl() : url, routers);
        if (invokers == null || invokers.size() == 0)
            throw new IllegalArgumentException("invokers == null");
        this.invokers = invokers;
    }

    public Class<T> getInterface() {
        return invokers.get(0).getInterface();
    }

    public boolean isAvailable() {
        if (isDestroyed()) {
            return false;
        }
        for (Invoker<T> invoker : invokers) {
            if (invoker.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void destroy() {
        if(isDestroyed()) {
            return;
        }
        super.destroy();
        for (Invoker<T> invoker : invokers) {
            invoker.destroy();
        }
        invokers.clear();
    }
    
    @Override
    protected List<Invoker<T>> doList(Invocation invocation) throws RpcException {

        return invokers;
    }

}
```

它的doList方法的实现是直接将属性invokers的值返回，非常简单。

此外还有一个RegistryDirectory的实现类，该类是整合了注册中心和目录服务。

## NEXT

因为考虑到本模块与dubbo-registry相关性较大，接下来我们将研究dubbo-registry-api和dubbo-registry-default模块的源码。



# 七 dubbo-register-api模块源码分析



> 摘要: dubbo中的注册中心是dubbo中重要的组成部分，dubbo的服务信息存储，服务治理等特性都是基于它实现的。本文将带着大家一起来看看dubbo-register-api和dubbo-register-default两个模块的源码。

## 核心类图

![img](http://static.oschina.net/uploads/space/2016/0618/143811_Izev_113011.jpg)

该图是包含了dubbo-registry-api和dubbo-registry-default两个模块整合的简化类图，只描述了核心的类与类的关系，为了清晰明了，去除了方法和属性的描述，也忽略了类所处的包，将其放在一个类图中。

注册中心核心的职责就是注册和注销URL，订阅和取消订阅URL，还有包括查询符合条件的已注册的URL列表等职责，类图中的接口和类就是围绕核心职责的实现。

## 核心源码分析

### RegistryProtocol

该类是注册中心的协议，即协议名称为registry的协议。这是整个注册中心启用的入口，通过该类整合了Protocol、Cluster、Directory 和Registry这几个组件。因此它作为入口我们非常有必有学习它的源码。

当我们在配置发布服务和引用服务的时候，若配置使用了注册中心，则会将原来的URL替换为一个protocol名称为registry的URL，并且会保留原始的URL在新的URL参数中。则通过dubbo的SPI机制获取到的Protocol接口的实现类便是RegistryProtocol，因此变回进入到该类的核心方法中，我们一起来看看这些核心方法的源码实现。

#### 发布服务方法export

```java
public <T> Exporter<T> export(final Invoker<T> originInvoker) throws RpcException {
        //export invoker
        final ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker);
        //registry provider
        final Registry registry = getRegistry(originInvoker);
        final URL registedProviderUrl = getRegistedProviderUrl(originInvoker);
        registry.register(registedProviderUrl);
        // 订阅override数据
        // FIXME 提供者订阅时，会影响同一JVM即暴露服务，又引用同一服务的的场景，因为subscribed以服务名为缓存的key，导致订阅信息覆盖。
        final URL overrideSubscribeUrl = getSubscribedOverrideUrl(registedProviderUrl);
        final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl);
        overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
        registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);
        //保证每次export都返回一个新的exporter实例
        return new Exporter<T>() {
            public Invoker<T> getInvoker() {
                return exporter.getInvoker();
            }
            public void unexport() {
            	try {
            		exporter.unexport();
            	} catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
                try {
                	registry.unregister(registedProviderUrl);
                } catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
                try {
                	overrideListeners.remove(overrideSubscribeUrl);
                	registry.unsubscribe(overrideSubscribeUrl, overrideSubscribeListener);
                } catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
            }
        };
    }

@SuppressWarnings("unchecked")
    private <T> ExporterChangeableWrapper<T>  doLocalExport(final Invoker<T> originInvoker){
        String key = getCacheKey(originInvoker);
        ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
        if (exporter == null) {
            synchronized (bounds) {
                exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
                if (exporter == null) {
                    final Invoker<?> invokerDelegete = new InvokerDelegete<T>(originInvoker, getProviderUrl(originInvoker));
                    exporter = new ExporterChangeableWrapper<T>((Exporter<T>)protocol.export(invokerDelegete), originInvoker);
                    bounds.put(key, exporter);
                }
            }
        }
        return (ExporterChangeableWrapper<T>) exporter;
    }

    /**
     * 根据invoker的地址获取registry实例
     * @param originInvoker
     * @return
     */
    private Registry getRegistry(final Invoker<?> originInvoker){
        URL registryUrl = originInvoker.getUrl();
        if (Constants.REGISTRY_PROTOCOL.equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_DIRECTORY);
            registryUrl = registryUrl.setProtocol(protocol).removeParameter(Constants.REGISTRY_KEY);
        }
        return registryFactory.getRegistry(registryUrl);
    }
```

该方法的实现逻辑是：

1. 会先调用private方法protocol实现本地服务发布。该方法是调用了protocol.export()方法实现真正的服务发布，而这个protocol属性是SPI机制中自动注入进来的，它注入的是原始的网络协议的实现类，比如默认的DubboProtocol。并且支持缓存，避免发布多次。
2. 根据url获得Registry对象。会调用registryFactory.getRegistry(registryUrl)获得对象。而registryFactory属性也是通过SPI机制自动注入进来的。比如注入默认的DubboRegistryFactory对象。
3. 注册提供者URL到registry中。调用registry.register(registedProviderUrl)实现提供者URL的注册。原始服务的URL被编码并作为名为export的参数加入到RegistryURL中。
4. 订阅协议替换为provider的URL。调用方法registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener)订阅URL，并添加监听器OverrideListener。该监听器主要监听变化，当检测到URL变化的时候会调用方法doChangeLocalExport更新服务发布。
5. 最后返回一个代理原始exporter对象的代理Exporter对象。该对象在unexport方法同时会注销订阅，删除监听器等操作。

#### 服务引用方法refer

```java
 @SuppressWarnings("unchecked")
	public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        url = url.setProtocol(url.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_REGISTRY)).removeParameter(Constants.REGISTRY_KEY);
        Registry registry = registryFactory.getRegistry(url);
        if (RegistryService.class.equals(type)) {
        	return proxyFactory.getInvoker((T) registry, type, url);
        }

        // group="a,b" or group="*"
        Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded(Constants.REFER_KEY));
        String group = qs.get(Constants.GROUP_KEY);
        if (group != null && group.length() > 0 ) {
            if ( ( Constants.COMMA_SPLIT_PATTERN.split( group ) ).length > 1
                    || "*".equals( group ) ) {
                return doRefer( getMergeableCluster(), registry, type, url );
            }
        }
        return doRefer(cluster, registry, type, url);
    }


 private Cluster getMergeableCluster() {
        return ExtensionLoader.getExtensionLoader(Cluster.class).getExtension("mergeable");
    }
    
    private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
        RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
        directory.setRegistry(registry);
        directory.setProtocol(protocol);
        URL subscribeUrl = new URL(Constants.CONSUMER_PROTOCOL, NetUtils.getLocalHost(), 0, type.getName(), directory.getUrl().getParameters());
        if (! Constants.ANY_VALUE.equals(url.getServiceInterface())
                && url.getParameter(Constants.REGISTER_KEY, true)) {
            registry.register(subscribeUrl.addParameters(Constants.CATEGORY_KEY, Constants.CONSUMERS_CATEGORY,
                    Constants.CHECK_KEY, String.valueOf(false)));
        }
        directory.subscribe(subscribeUrl.addParameter(Constants.CATEGORY_KEY, 
                Constants.PROVIDERS_CATEGORY 
                + "," + Constants.CONFIGURATORS_CATEGORY 
                + "," + Constants.ROUTERS_CATEGORY));
        return cluster.join(directory);
    }
```

发布服务流程

1. 将原始URL的协议转为registry协议。
2. 获得Registry对象。
3. 调用doRefer()方法引用服务。
4. 创建RegistryDirectory对象，并设置相关值。
5. 构造消费者subscribeUrl。协议名称设置为consumer。
6. 注册中心注册URL。代码为registry.register(subscribeUrl)。
7. 目录服务注册URL。RegistryDirectory.subscribe();
8. 调用cluster.join()方法返回一个支持集群功能的invoker。cluster.join(directory)；

这里用到了RegistryDirectory，因此我们要继续探究一下该类的源码。

### RegistryDirectory

#### Registry

该接口注册中心，它继承自接口Node和RegistryService，它本身没有定义任何方法。

```java
public interface Registry extends Node, RegistryService {
}
```

#### RegistryService

该接口是注册中心服务，定义了注册中心的核心行为。

```java
/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.registry;

import java.util.List;

import com.alibaba.dubbo.common.URL;

/**
 * RegistryService. (SPI, Prototype, ThreadSafe)
 * 
 * @see com.alibaba.dubbo.registry.Registry
 * @see com.alibaba.dubbo.registry.RegistryFactory#getRegistry(URL)
 * @author william.liangf
 */
public interface RegistryService {

    /**
     * 注册数据，比如：提供者地址，消费者地址，路由规则，覆盖规则，等数据。
     * 
     * 注册需处理契约：<br>
     * 1. 当URL设置了check=false时，注册失败后不报错，在后台定时重试，否则抛出异常。<br>
     * 2. 当URL设置了dynamic=false参数，则需持久存储，否则，当注册者出现断电等情况异常退出时，需自动删除。<br>
     * 3. 当URL设置了category=routers时，表示分类存储，缺省类别为providers，可按分类部分通知数据。<br>
     * 4. 当注册中心重启，网络抖动，不能丢失数据，包括断线自动删除数据。<br>
     * 5. 允许URI相同但参数不同的URL并存，不能覆盖。<br>
     * 
     * @param url 注册信息，不允许为空，如：dubbo://10.20.153.10/com.alibaba.foo.BarService?version=1.0.0&application=kylin
     */
    void register(URL url);

    /**
     * 取消注册.
     * 
     * 取消注册需处理契约：<br>
     * 1. 如果是dynamic=false的持久存储数据，找不到注册数据，则抛IllegalStateException，否则忽略。<br>
     * 2. 按全URL匹配取消注册。<br>
     * 
     * @param url 注册信息，不允许为空，如：dubbo://10.20.153.10/com.alibaba.foo.BarService?version=1.0.0&application=kylin
     */
    void unregister(URL url);

    /**
     * 订阅符合条件的已注册数据，当有注册数据变更时自动推送.
     * 
     * 订阅需处理契约：<br>
     * 1. 当URL设置了check=false时，订阅失败后不报错，在后台定时重试。<br>
     * 2. 当URL设置了category=routers，只通知指定分类的数据，多个分类用逗号分隔，并允许星号通配，表示订阅所有分类数据。<br>
     * 3. 允许以interface,group,version,classifier作为条件查询，如：interface=com.alibaba.foo.BarService&version=1.0.0<br>
     * 4. 并且查询条件允许星号通配，订阅所有接口的所有分组的所有版本，或：interface=*&group=*&version=*&classifier=*<br>
     * 5. 当注册中心重启，网络抖动，需自动恢复订阅请求。<br>
     * 6. 允许URI相同但参数不同的URL并存，不能覆盖。<br>
     * 7. 必须阻塞订阅过程，等第一次通知完后再返回。<br>
     * 
     * @param url 订阅条件，不允许为空，如：consumer://10.20.153.10/com.alibaba.foo.BarService?version=1.0.0&application=kylin
     * @param listener 变更事件监听器，不允许为空
     */
    void subscribe(URL url, NotifyListener listener);

    /**
     * 取消订阅.
     * 
     * 取消订阅需处理契约：<br>
     * 1. 如果没有订阅，直接忽略。<br>
     * 2. 按全URL匹配取消订阅。<br>
     * 
     * @param url 订阅条件，不允许为空，如：consumer://10.20.153.10/com.alibaba.foo.BarService?version=1.0.0&application=kylin
     * @param listener 变更事件监听器，不允许为空
     */
    void unsubscribe(URL url, NotifyListener listener);

    /**
     * 查询符合条件的已注册数据，与订阅的推模式相对应，这里为拉模式，只返回一次结果。
     * 
     * @see com.alibaba.dubbo.registry.NotifyListener#notify(List)
     * @param url 查询条件，不允许为空，如：consumer://10.20.153.10/com.alibaba.foo.BarService?version=1.0.0&application=kylin
     * @return 已注册信息列表，可能为空，含义同{@link com.alibaba.dubbo.registry.NotifyListener#notify(List<URL>)}的参数。
     */
    List<URL> lookup(URL url);

}
```

#### RegistryFactory

注册中心工厂类，用于获得注册中心对象。

```java
@SPI("dubbo")
public interface RegistryFactory {

    /**
     * 连接注册中心.
     * 
     * 连接注册中心需处理契约：<br>
     * 1. 当设置check=false时表示不检查连接，否则在连接不上时抛出异常。<br>
     * 2. 支持URL上的username:password权限认证。<br>
     * 3. 支持backup=10.20.153.10备选注册中心集群地址。<br>
     * 4. 支持file=registry.cache本地磁盘文件缓存。<br>
     * 5. 支持timeout=1000请求超时设置。<br>
     * 6. 支持session=60000会话超时或过期设置。<br>
     * 
     * @param url 注册中心地址，不允许为空
     * @return 注册中心引用，总不返回空
     */
    @Adaptive({"protocol"})
    Registry getRegistry(URL url);

}
```

该类是一个支持SPI扩展的类，默认的扩展实现是名称为dubbo的扩展实现类DubboRegistryFactory。

#### AbstractRegistry

该类是Registry接口的抽象实现。



# 八 dubbo典型协议、传输组件、序列化方式组合性能对比测试



> 摘要: dubbo默认提供了很多对于网络协议、网络组件、和序列化组件的多种扩展，而且开发者还可以自行根据自己扩展自己需要的实现。这些扩展的差异主要体现在性能上，当然每种扩展都有它的适用场景，本文将记录各种扩展点组合的实际性能表现情况，作者将自己亲自进行实测，测试结果仅供参考。 文章模板参考了：http://www.cnblogs.com/lengfo/p/4293399.html?utm_source=tuicool&utm_medium=referral，在该文的基础上做了一些调整，测试数据是本人亲自测试的实际结果。

## 前言

Dubbo作为一个扩展能力极强的分布式服务框架，在实现rpc特性的时候，给传输协议、传输框架和序列化方式提供了多种扩展实现，供开发者根据实际场景进行选择。

1、支持常见的传输协议：RMI、Dubbo、Hessain、WebService、Http等，其中Dubbo和RMI协议基于TCP实现，Hessian和WebService基于HTTP实现。

2、传输框架：Netty、Mina、grizzly以及基于servlet等方式。

3、序列化方式：Hessian2、dubbo、JSON（ [fastjson](https://github.com/AlibabaTech/fastjson) 实现）、JAVA、SOAP、kryo和fst 等。

本文主要基于dubbox框架下的通讯协议进行性能测试对比。

文章模板参考了：[http://www.cnblogs.com/lengfo/p/4293399.html?utm_source=tuicool&utm_medium=referral](http://www.cnblogs.com/lengfo/p/4293399.html?utm_source=tuicool&utm_medium=referral)，在该文的基础上做了一些调整，测试数据是本人亲自测试的实际结果。

## 测试方案

基于dubbox 2.8.4框架，使用zookeeper作为注册中心，分别以单线程和多线程的方式测试以下组合。

| 分组名  | **Protocol** | **Transporter  ** | **Serialization    ** | **Remark**               |
| ---- | ------------ | ----------------- | --------------------- | ------------------------ |
| A    | dubbo        | netty             | hessian2              |                          |
| B    | dubbo        | netty             | dubbo                 |                          |
| C    | dubbo        | netty             | java                  |                          |
| D    | dubbo        | netty             | fst                   |                          |
| E    | dubbo        | netty             | kryo                  |                          |
| F    | dubbo        | mina              | hessian2              |                          |
| G    | rmi          | netty             | java                  |                          |
| H    | rmi          | netty             | hessian2              |                          |
| I    | Hessian      | servlet           | hessian2              | Hessian，基于tomcat8.0嵌入式容器 |
| J    | WebService   | servlet           | SOAP                  | CXF，基于tomcat8.0嵌入式容器     |
| K    | http         | servlet           | json                  | 基于tomcat8.0嵌入式容器         |

## 测试环境

本次测试是所有组件都部署在同一台PC机上进行的，包括zookeeper，消费者和生产者都是在本机上。

机器的配置如下：

内存12G；

CPU是intel i5 2.5ghz 4核；

操作系统Windows visita6.1；

jdk1.6.0.25和jdk1.7.0_79；

JVM配置是默认值，最大堆64M,

## 传输测试数据

1、单POJO对象，嵌套复杂集合类型

2、POJO集合，包含100个单POJO对象

3、1K字符串

4、100K字符串

5、1M字符串 

## 服务接口和实现

1、服务接口相关代码： 

```java
public interface DemoService {
    public Object sendRequest(Object requestObject);
}
```

2、服务实现相关代码，测试数据在服务器端不做任何处理原样返回：

```java
public class DemoServiceImpl implements DemoService{
    ResponseObject responseObject = new ResponseObject(100);

    public Object sendRequest(Object request) {
        return request;
    }
}
```

3、测试框架介绍

本文是基于dubbo自带的benchmark性能测试框架进行测试的，对该框架做了简单的调整和修改，框架代码见：[https://github.com/dangdangdotcom/dubbox/tree/master/dubbo-test/dubbo-test-benchmark](https://github.com/dangdangdotcom/dubbox/tree/master/dubbo-test/dubbo-test-benchmark)

## 单线程测试

1、测试仅记录rpc调用时间，测试数据的读取组装以及首次建立连接等相关耗时时间不作统计，循环执行60秒钟取平均响应时间值。

2、服务消费方测试代码

输入复杂POJO测试代码

```java
private static BidRequest request = new BidRequest();
    private static int size = 100;
    private static List<BidRequest> requests = new ArrayList<BidRequest>();
    
    static{
    	request.setId("ssss");
    	Geo geo = new Geo();
    	geo.setCity("beijing");
    	geo.setCountry("china");
    	geo.setLat(1.0f);
    	geo.setLon(5.3f);
    	
    	Device device = new Device();
    	device.setLang("中文");
    	device.setOs("windows 7");
    	device.setVersion("1.0.0");
    	device.setModel("dddddd");
    	device.setGeo(geo);
    	
    	request.setDevice(device);
    	
    	List<Impression> impressions = new ArrayList<Impression>();
    	for(int i=0; i<3; i++){
    		Impression impression = new Impression();
    		impression.setId("2333"+i);
    		impression.setBidFloor(2223.3333d);
    		impressions.add(impression);
    	}
    	request.setImpressions(impressions);
    	
    	
    	for(int i=0; i<size; i++){
    		requests.add(request);
    	}
    }

 @SuppressWarnings({ "unchecked"})
    @Override
    public Object invoke(ServiceFactory serviceFactory) {
        DemoService demoService = (DemoService) serviceFactory.get(DemoService.class);
        Object result = demoService.sendRequest(requests);
        return result;
    }
```

输入字符串测试代码

```java
private static String message = null;
    private static int length = 1024000;
    static{
        message = new String(new byte[length]);
    }

public Object invoke(ServiceFactory serviceFactory) {
        DemoService demoService = (DemoService) serviceFactory.get(DemoService.class);
        Object result = demoService.sendRequest(requestObject);
        return result;
    }
```

3、测试数据耗时记录

A、dubbo 协议、netty 传输、hessian2 序列化

<dubbo:protocol name="dubbo" server="netty" port="20885" serialization="hessian2"  />

jdk1.6测试数据

| 单个POJO       | 0.369ms   |
| ------------ | --------- |
| POJO集合 (100) | 3.326ms   |
| 1K String    | 0.354ms   |
| 100K String  | 17.804ms  |
| 1M String    | 154.178ms |

jdk1.7测试数据

| 单个POJO       | 0.195ms  |
| ------------ | -------- |
| POJO集合 (100) | 1.207ms  |
| 1K String    | 0.203ms  |
| 100K String  | 4.901ms  |
| 1M String    | 47.691ms |

B、dubbo 协议、netty 传输、dubbo 序列化

<dubbo:protocol name="dubbo" server="netty" port="20885" serialization="dubbo" /> 

jdk1.6测试数据

| 单个POJO       | 0.345ms   |
| ------------ | --------- |
| POJO集合 (100) | 6.663ms   |
| 1K String    | 0.325ms   |
| 100K String  | 17.756ms  |
| 1M String    | 128.241ms |

jdk1.7测试数据

| 单个POJO       | 0.203ms  |
| ------------ | -------- |
| POJO集合 (100) | 2.47ms   |
| 1K String    | 0.193ms  |
| 100K String  | 3.917ms  |
| 1M String    | 36.186ms |

C、dubbo 协议、netty 传输、java 序列化

<dubbo:protocol name="dubbo" server="netty" port="20885" serialization="java" />

jdk1.6测试数据

| 单个POJO       | 0.541ms   |
| ------------ | --------- |
| POJO集合 (100) | 4.194ms   |
| 1K String    | 0.391ms   |
| 100K String  | 26.561ms  |
| 1M String    | 151.337ms |

jdk1.7测试数据

| 单个POJO       | 0.375ms  |
| ------------ | -------- |
| POJO集合 (100) | 1.839ms  |
| 1K String    | 0.237ms  |
| 100K String  | 5.691ms  |
| 1M String    | 54.462ms |

D、dubbo 协议、netty 传输、fst序列化

<dubbo:protocol name="dubbo" server="netty" port="20885" serialization="fst" />

jdk1.6测试数据

| 单个POJO       | 0.273ms   |
| ------------ | --------- |
| POJO集合 (100) | 0.768ms   |
| 1K String    | 0.31ms    |
| 100K String  | 20.048ms  |
| 1M String    | 353.594ms |

jdk1.7测试数据

| 单个POJO       | 0.187ms  |
| ------------ | -------- |
| POJO集合 (100) | 0.429ms  |
| 1K String    | 0.281ms  |
| 100K String  | 2.861ms  |
| 1M String    | 30.599ms |

E、dubbo 协议、netty 传输、kryo序列化

<dubbo:protocol name="dubbo" server="netty" port="20885" serialization="kryo" />

jdk1.6测试数据

| 单个POJO       | 3.087ms   |
| ------------ | --------- |
| POJO集合 (100) | 3.749ms   |
| 1K String    | 3.017ms   |
| 100K String  | 33.283ms  |
| 1M String    | 353.851ms |

jdk1.7测试数据

| 单个POJO       | 2.767ms  |
| ------------ | -------- |
| POJO集合 (100) | 2.869ms  |
| 1K String    | 2.636ms  |
| 100K String  | 5.804ms  |
| 1M String    | 33.501ms |

F、dubbo 协议、mina 传输、hessian2序列化

<dubbo:protocol name="dubbo" server="mina" port="20885" serialization="hessian2" />

jdk1.6测试数据

| 单个POJO       | 0.378ms    |
| ------------ | ---------- |
| POJO集合 (100) | 3.47ms     |
| 1K String    | 6002.945ms |
| 100K String  | 6061.22ms  |
| 1M String    | 5067.535ms |

jdk1.7测试数据

| 单个POJO       | 0.213ms    |
| ------------ | ---------- |
| POJO集合 (100) | 6004.22ms  |
| 1K String    | 6003.739ms |
| 100K String  | 6013.998ms |
| 1M String    | 3779.117ms |

G、RMI 协议、netty 传输、java 序列化 

<dubbo:protocol name="rmi" server="netty" port="20885" serialization="java" />

jdk1.6测试数据

| 单个POJO       | 0.349ms   |
| ------------ | --------- |
| POJO集合 (100) | 2.874ms   |
| 1K String    | 0.203ms   |
| 100K String  | 7.129ms   |
| 1M String    | 136.697ms |

jdk1.7测试数据

| 单个POJO       | 0.203ms  |
| ------------ | -------- |
| POJO集合 (100) | 1.515ms  |
| 1K String    | 0.138ms  |
| 100K String  | 3.609ms  |
| 1M String    | 35.188ms |

H、RMI 协议、netty 传输、hessian2 序列化 

<dubbo:protocol name="rmi" server="netty" port="20885" serialization="hessian2"  /> 

jdk1.6测试数据

| 单个POJO       | 0.338ms   |
| ------------ | --------- |
| POJO集合 (100) | 2.815ms   |
| 1K String    | 0.196ms   |
| 100K String  | 8.509ms   |
| 1M String    | 134.098ms |

jdk1.7测试数据

| 单个POJO       | 0.213ms  |
| ------------ | -------- |
| POJO集合 (100) | 1.511ms  |
| 1K String    | 0.139ms  |
| 100K String  | 3.589ms  |
| 1M String    | 36.322ms |

I、Hessian协议、servlet（tomcat容器）、hessian2 序列化 

用到了tomcat-embed-core-8.0.11，需要用jdk1.7以上版本，所以本次测试使用jdk1.7

<dubbo:protocol name="hessian" port="20885" server="tomcat" serialization="hessian2" /> 

jdk1.7测试数据

| 单个POJO       | 0.482ms  |
| ------------ | -------- |
| POJO集合 (100) | 3.201ms  |
| 1K String    | 0.413ms  |
| 100K String  | 6.893ms  |
| 1M String    | 59.805ms |

J、WebService协议、servlet（tomcat容器）、SOAP序列化

<dubbo:protocol name="webservice" port="20885" server="tomcat" />

jdk1.7测试数据

| 单个POJO       | 0.598ms  |
| ------------ | -------- |
| POJO集合 (100) | 0.731ms  |
| 1K String    | 11.603ms |
| 100K String  | 71.991ms |
| 1M String    | 81.596ms |

H、http协议、servlet（tomcat容器）、json序列化

<dubbo:protocol name="http" port="20885" server="tomcat"  serialization="json"/>

jdk1.7测试数据

| 单个POJO       | 0.586ms  |
| ------------ | -------- |
| POJO集合 (100) | 2.179ms  |
| 1K String    | 0.722ms  |
| 100K String  | 6.982ms  |
| 1M String    | 59.848ms |

4、性能对比

## 多线程测试

1、由于测试机器配置较低，为了避免达到CPU瓶颈，测试设定服务消费方Consumer并发10个线程，每个线程连续对远程方法执行60秒钟，超时时间设置为2000ms，要求所有事务都能正确返回没有异常。

## 性能分析

影响性能测试结果的因素太多了，本文只是做了非常局限的测试，测试用例覆盖率不够，测试环境和条件制约较大，因此本文的测试结果绝对是**仅供参考**。不能完全信任本文的测试数据和结论。

通过上述的对比测试分析可以看出：

1.内部应用之间的小尺寸的对象和字符串调用的场景推荐使用**dubbo+netty+fst**的组合较优。fst的性能要由于hessian2。

2.rmi协议在传输大尺寸字符串对象的时候表现更优，这超出了我们一般的认知。

3.**mina框架不推荐使用**，则测试中的表现非常差，深层次的原因还没有深究，大量的调用超时失败，dubbo的测试报告也指出它存在一些问题，因此该框架不建议使用。

4.kryo序列化组件的性能表现较差，这与其它的测试报告的出入较大，具体原因需要深层次的探究。

5.有部分测试失败，具体原因有待考究。

6.jdk1.6 和 jdk1.7对性能测试结果差异较大，jdk1.7测试性能好于1.6. **推荐使用jdk1.7**. 以dubbo协议，默认的传输组件和序列化组件为例，jdk1.7的环境下性能提升接近于90%。



# 九  九九归一之dubbo核心流程分析

摘要: 我认为dubbo核心核心的流程就是由2个组成，分别是服务发布流程和服务调用流程。那么这2个流程几乎把所有的dubbo的类和相关技术组件组合而成对外提供完整的服务的，因此我们通过分析它的核心流程有助于我们理解dubbo各组件之间的关系和通讯。那么本文将会以调试代码的方式来分析dubbo核心的流程。

## dubbo流程图

首先我们将引用dubbo官方文档和其它一些博客中发布的dubbo的流程图来对dubbo的流程有个概括的了解。

参考博客：[http://shiyanjun.cn/archives/325.html](http://shiyanjun.cn/archives/325.html)

### dubbo的总体架构图

![img](http://static.oschina.net/uploads/space/2016/0629/101417_ENqQ_113011.jpg)

![img](http://static.oschina.net/uploads/space/2016/0629/101620_0mjU_113011.jpg)

### 服务发布及取消时序

![img](http://static.oschina.net/uploads/space/2016/0629/101816_0xvy_113011.png)

### 服务引用及销毁时序

![img](http://static.oschina.net/uploads/space/2016/0629/102016_9NOz_113011.jpg)

## 源代码调试流程追踪

接下来我们将通过调试源代码的方式来追踪核心流程，让我们更加细致和深入的了解dubbo的核心流程。

### 源代码调试环境

接口ProcessService定义

```java
package com.alibaba.dubbo.test;

public interface ProcessService {
	
	public Object test(Object object);

}
```

接口实现类ProcessServiceImpl

```java
package com.alibaba.dubbo.test;

public class ProcessServiceImpl implements ProcessService {

	public Object test(Object object) {
		 //测试方法，直接返回参数值。
		return object;
	}

}
```

服务发布入口类ServerMain

```java
package com.alibaba.dubbo.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerMain {
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("SpringApplicationContext.xml");
        ctx.start();
        synchronized (ServerMain.class) {
            try {
            	ServerMain.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
	}

}
```

服务发布配置文件SpringApplicationContext.xml

```java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://code.alibabatech.com/schema/dubbo
        http://code.alibabatech.com/schema/dubbo/dubbo.xsd
        ">
        
	<bean id="processServiceImpl" class="com.alibaba.dubbo.test.ProcessServiceImpl" />

	<dubbo:registry address="zookeeper://127.0.0.1:2181"/>

	<!-- 服务应用配置 -->
	<dubbo:application name="dubbo_provider" />

	<!-- 服务提供者全局配置 -->
	<dubbo:protocol name="dubbo" port="20885"/>

	<!-- 服务提供者暴露服务配置 -->
	<dubbo:service id="processService" interface="com.alibaba.dubbo.test.ProcessService"
		ref="processServiceImpl"/>

</beans>
```

客户应用及调用主类ClientMain

```java
package com.alibaba.dubbo.test;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;

public class ClientMain {
	
	public static void main(String[] args) {
		
		RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
		
		ApplicationConfig application = new ApplicationConfig();
        application.setName("dubbo_consumer");
		
		ReferenceConfig<ProcessService> referenceConfig = new ReferenceConfig<ProcessService>();
		referenceConfig.setRegistry(registryConfig);
		referenceConfig.setApplication(application);
		
		referenceConfig.setInterface(ProcessService.class);
		
		ProcessService processService = referenceConfig.get();
		
		System.out.println(processService.test("hello, dubbo"));
		
		
	}

}
```

这里用了一个最简单的接口和方法，目的是将我们的关注点放在dubbo本身的实现及流程上，而无须关注业务逻辑上。

### 服务发布流程调试

服务发布首先进入的是dubbo-config-api模块的ServiceConfig类，进入该类的export方法。

![img](http://static.oschina.net/uploads/space/2016/0629/114857_xXJE_113011.jpg)

从图中可以看出，我们在Spring容器中发布dubbo，在Spring容器启动的时候发布事件的时候，因为ServiceBean作为ApplicationListener的一个实现类，能够监听到容器应用的事件，在处理事件的时候会调用父类ServiceConfig的export方法，而该方法真正实现了服务的发布。

该方法源码如下：

```java
    public synchronized void export() {
        if (provider != null) {
            if (export == null) {
                export = provider.getExport();
            }
            if (delay == null) {
                delay = provider.getDelay();
            }
        }
        if (export != null && ! export.booleanValue()) {
            return;
        }
        if (delay != null && delay > 0) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(delay);
                    } catch (Throwable e) {
                    }
                    doExport();
                }
            });
            thread.setDaemon(true);
            thread.setName("DelayExportServiceThread");
            thread.start();
        } else {
            doExport();
        }
    }
```

可以看出发布发布是支持延迟异步发布服务的，这样可以用于当我们发布的服务非常多，影响到应用启动的问题，前提是应用允许服务发布的延迟特性。

接下来就进入到内部方法doExport。

```java
    protected synchronized void doExport() {
        if (unexported) {
            throw new IllegalStateException("Already unexported!");
        }
        if (exported) {
            return;
        }
        exported = true;
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<dubbo:service interface=\"\" /> interface not allow null!");
        }
        checkDefault();
        if (provider != null) {
            if (application == null) {
                application = provider.getApplication();
            }
            if (module == null) {
                module = provider.getModule();
            }
            if (registries == null) {
                registries = provider.getRegistries();
            }
            if (monitor == null) {
                monitor = provider.getMonitor();
            }
            if (protocols == null) {
                protocols = provider.getProtocols();
            }
        }
        if (module != null) {
            if (registries == null) {
                registries = module.getRegistries();
            }
            if (monitor == null) {
                monitor = module.getMonitor();
            }
        }
        if (application != null) {
            if (registries == null) {
                registries = application.getRegistries();
            }
            if (monitor == null) {
                monitor = application.getMonitor();
            }
        }
        if (ref instanceof GenericService) {
            interfaceClass = GenericService.class;
            if (StringUtils.isEmpty(generic)) {
                generic = Boolean.TRUE.toString();
            }
        } else {
            try {
                interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                        .getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            checkInterfaceAndMethods(interfaceClass, methods);
            checkRef();
            generic = Boolean.FALSE.toString();
        }
        if(local !=null){
            if(local=="true"){
                local=interfaceName+"Local";
            }
            Class<?> localClass;
            try {
                localClass = ClassHelper.forNameWithThreadContextClassLoader(local);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            if(!interfaceClass.isAssignableFrom(localClass)){
                throw new IllegalStateException("The local implemention class " + localClass.getName() + " not implement interface " + interfaceName);
            }
        }
        if(stub !=null){
            if(stub=="true"){
                stub=interfaceName+"Stub";
            }
            Class<?> stubClass;
            try {
                stubClass = ClassHelper.forNameWithThreadContextClassLoader(stub);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            if(!interfaceClass.isAssignableFrom(stubClass)){
                throw new IllegalStateException("The stub implemention class " + stubClass.getName() + " not implement interface " + interfaceName);
            }
        }
        checkApplication();
        checkRegistry();
        checkProtocol();
        appendProperties(this);
        checkStubAndMock(interfaceClass);
        if (path == null || path.length() == 0) {
            path = interfaceName;
        }
        doExportUrls();
    }
```

我们可以看出该方法的实现的逻辑包含了根据配置的优先级将ProviderConfig，ModuleConfig，MonitorConfig，ApplicaitonConfig等一些配置信息进行组装和合并。还有一些逻辑是检查配置信息的合法性。最后又调用了doExportUrls方法。

```java
  private void doExportUrls() {
        List<URL> registryURLs = loadRegistries(true);
        for (ProtocolConfig protocolConfig : protocols) {
            doExportUrlsFor1Protocol(protocolConfig, registryURLs);
        }
    }
```

该方法第一步是加载注册中心列表，第二部是将服务发布到多种协议的url上，并且携带注册中心列表的参数，从这里我们可以看出dubbo是支持同时将一个服务发布成为多种协议的，这个需求也是很正常的，客户端也需要支持多协议，根据不同的场景选择合适的协议。

进入方法loadRegistries看看实现逻辑。

```java
  protected List<URL> loadRegistries(boolean provider) {
        checkRegistry();
        List<URL> registryList = new ArrayList<URL>();
        if (registries != null && registries.size() > 0) {
            for (RegistryConfig config : registries) {
                String address = config.getAddress();
                if (address == null || address.length() == 0) {
                	address = Constants.ANYHOST_VALUE;
                }
                String sysaddress = System.getProperty("dubbo.registry.address");
                if (sysaddress != null && sysaddress.length() > 0) {
                    address = sysaddress;
                }
                if (address != null && address.length() > 0 
                        && ! RegistryConfig.NO_AVAILABLE.equalsIgnoreCase(address)) {
                    Map<String, String> map = new HashMap<String, String>();
                    appendParameters(map, application);
                    appendParameters(map, config);
                    map.put("path", RegistryService.class.getName());
                    map.put("dubbo", Version.getVersion());
                    map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
                    if (ConfigUtils.getPid() > 0) {
                        map.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
                    }
                    if (! map.containsKey("protocol")) {
                        if (ExtensionLoader.getExtensionLoader(RegistryFactory.class).hasExtension("remote")) {
                            map.put("protocol", "remote");
                        } else {
                            map.put("protocol", "dubbo");
                        }
                    }
                    List<URL> urls = UrlUtils.parseURLs(address, map);
                    for (URL url : urls) {
                        url = url.addParameter(Constants.REGISTRY_KEY, url.getProtocol());
                        url = url.setProtocol(Constants.REGISTRY_PROTOCOL);
                        if ((provider && url.getParameter(Constants.REGISTER_KEY, true))
                                || (! provider && url.getParameter(Constants.SUBSCRIBE_KEY, true))) {
                            registryList.add(url);
                        }
                    }
                }
            }
        }
        return registryList;
    }
```

该方法先检查了注册中心地址的合法性，然后将注册中心配置转化为URL列表，其中若地址为'N/A' 则表示无效注册中心地址，则会跳过。最后返回的URL列表的协议名称为registry，并且会在URL参数registry的值中保存原始的注册中心地址协议名称值。后面会使用到该值。

然后我们回到方法doExportUrls，下一步是将得到注册中心URL列表作为一个参数，另外一个参数是协议配置信息，调用方法doExportUrlsFor1Protocol继续实现发布服务逻辑。

```java
  private void doExportUrlsFor1Protocol(ProtocolConfig protocolConfig, List<URL> registryURLs) {
        String name = protocolConfig.getName();
        if (name == null || name.length() == 0) {
            name = "dubbo";
        }

        String host = protocolConfig.getHost();
        if (provider != null && (host == null || host.length() == 0)) {
            host = provider.getHost();
        }
        boolean anyhost = false;
        if (NetUtils.isInvalidLocalHost(host)) {
            anyhost = true;
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.warn(e.getMessage(), e);
            }
            if (NetUtils.isInvalidLocalHost(host)) {
                if (registryURLs != null && registryURLs.size() > 0) {
                    for (URL registryURL : registryURLs) {
                        try {
                            Socket socket = new Socket();
                            try {
                                SocketAddress addr = new InetSocketAddress(registryURL.getHost(), registryURL.getPort());
                                socket.connect(addr, 1000);
                                host = socket.getLocalAddress().getHostAddress();
                                break;
                            } finally {
                                try {
                                    socket.close();
                                } catch (Throwable e) {}
                            }
                        } catch (Exception e) {
                            logger.warn(e.getMessage(), e);
                        }
                    }
                }
                if (NetUtils.isInvalidLocalHost(host)) {
                    host = NetUtils.getLocalHost();
                }
            }
        }

        Integer port = protocolConfig.getPort();
        if (provider != null && (port == null || port == 0)) {
            port = provider.getPort();
        }
        final int defaultPort = ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(name).getDefaultPort();
        if (port == null || port == 0) {
            port = defaultPort;
        }
        if (port == null || port <= 0) {
            port = getRandomPort(name);
            if (port == null || port < 0) {
                port = NetUtils.getAvailablePort(defaultPort);
                putRandomPort(name, port);
            }
            logger.warn("Use random available port(" + port + ") for protocol " + name);
        }

        Map<String, String> map = new HashMap<String, String>();
        if (anyhost) {
            map.put(Constants.ANYHOST_KEY, "true");
        }
        map.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);
        map.put(Constants.DUBBO_VERSION_KEY, Version.getVersion());
        map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        if (ConfigUtils.getPid() > 0) {
            map.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        }
        appendParameters(map, application);
        appendParameters(map, module);
        appendParameters(map, provider, Constants.DEFAULT_KEY);
        appendParameters(map, protocolConfig);
        appendParameters(map, this);
        if (methods != null && methods.size() > 0) {
            for (MethodConfig method : methods) {
                appendParameters(map, method, method.getName());
                String retryKey = method.getName() + ".retry";
                if (map.containsKey(retryKey)) {
                    String retryValue = map.remove(retryKey);
                    if ("false".equals(retryValue)) {
                        map.put(method.getName() + ".retries", "0");
                    }
                }
                List<ArgumentConfig> arguments = method.getArguments();
                if (arguments != null && arguments.size() > 0) {
                    for (ArgumentConfig argument : arguments) {
                        //类型自动转换.
                        if(argument.getType() != null && argument.getType().length() >0){
                            Method[] methods = interfaceClass.getMethods();
                            //遍历所有方法
                            if(methods != null && methods.length > 0){
                                for (int i = 0; i < methods.length; i++) {
                                    String methodName = methods[i].getName();
                                    //匹配方法名称，获取方法签名.
                                    if(methodName.equals(method.getName())){
                                        Class<?>[] argtypes = methods[i].getParameterTypes();
                                        //一个方法中单个callback
                                        if (argument.getIndex() != -1 ){
                                            if (argtypes[argument.getIndex()].getName().equals(argument.getType())){
                                                appendParameters(map, argument, method.getName() + "." + argument.getIndex());
                                            }else {
                                                throw new IllegalArgumentException("argument config error : the index attribute and type attirbute not match :index :"+argument.getIndex() + ", type:" + argument.getType());
                                            }
                                        } else {
                                            //一个方法中多个callback
                                            for (int j = 0 ;j<argtypes.length ;j++) {
                                                Class<?> argclazz = argtypes[j];
                                                if (argclazz.getName().equals(argument.getType())){
                                                    appendParameters(map, argument, method.getName() + "." + j);
                                                    if (argument.getIndex() != -1 && argument.getIndex() != j){
                                                        throw new IllegalArgumentException("argument config error : the index attribute and type attirbute not match :index :"+argument.getIndex() + ", type:" + argument.getType());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }else if(argument.getIndex() != -1){
                            appendParameters(map, argument, method.getName() + "." + argument.getIndex());
                        }else {
                            throw new IllegalArgumentException("argument config must set index or type attribute.eg: <dubbo:argument index='0' .../> or <dubbo:argument type=xxx .../>");
                        }

                    }
                }
            } // end of methods for
        }

        if (ProtocolUtils.isGeneric(generic)) {
            map.put("generic", generic);
            map.put("methods", Constants.ANY_VALUE);
        } else {
            String revision = Version.getVersion(interfaceClass, version);
            if (revision != null && revision.length() > 0) {
                map.put("revision", revision);
            }

            String[] methods = Wrapper.getWrapper(interfaceClass).getMethodNames();
            if(methods.length == 0) {
                logger.warn("NO method found in service interface " + interfaceClass.getName());
                map.put("methods", Constants.ANY_VALUE);
            }
            else {
                map.put("methods", StringUtils.join(new HashSet<String>(Arrays.asList(methods)), ","));
            }
        }
        if (! ConfigUtils.isEmpty(token)) {
            if (ConfigUtils.isDefault(token)) {
                map.put("token", UUID.randomUUID().toString());
            } else {
                map.put("token", token);
            }
        }
        if ("injvm".equals(protocolConfig.getName())) {
            protocolConfig.setRegister(false);
            map.put("notify", "false");
        }
        // 导出服务
        String contextPath = protocolConfig.getContextpath();
        if ((contextPath == null || contextPath.length() == 0) && provider != null) {
            contextPath = provider.getContextpath();
        }
        URL url = new URL(name, host, port, (contextPath == null || contextPath.length() == 0 ? "" : contextPath + "/") + path, map);

        if (ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
                .hasExtension(url.getProtocol())) {
            url = ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
                    .getExtension(url.getProtocol()).getConfigurator(url).configure(url);
        }

        String scope = url.getParameter(Constants.SCOPE_KEY);
        //配置为none不暴露
        if (! Constants.SCOPE_NONE.toString().equalsIgnoreCase(scope)) {

            //配置不是remote的情况下做本地暴露 (配置为remote，则表示只暴露远程服务)
            if (!Constants.SCOPE_REMOTE.toString().equalsIgnoreCase(scope)) {
                exportLocal(url);
            }
            //如果配置不是local则暴露为远程服务.(配置为local，则表示只暴露远程服务)
            if (! Constants.SCOPE_LOCAL.toString().equalsIgnoreCase(scope) ){
                if (logger.isInfoEnabled()) {
                    logger.info("Export dubbo service " + interfaceClass.getName() + " to url " + url);
                }
                if (registryURLs != null && registryURLs.size() > 0
                        && url.getParameter("register", true)) {
                    for (URL registryURL : registryURLs) {
                        url = url.addParameterIfAbsent("dynamic", registryURL.getParameter("dynamic"));
                        URL monitorUrl = loadMonitor(registryURL);
                        if (monitorUrl != null) {
                            url = url.addParameterAndEncoded(Constants.MONITOR_KEY, monitorUrl.toFullString());
                        }
                        if (logger.isInfoEnabled()) {
                            logger.info("Register dubbo service " + interfaceClass.getName() + " url " + url + " to registry " + registryURL);
                        }
                        Invoker<?> invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, registryURL.addParameterAndEncoded(Constants.EXPORT_KEY, url.toFullString()));

                        Exporter<?> exporter = protocol.export(invoker);
                        exporters.add(exporter);
                    }
                } else {
                    Invoker<?> invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, url);

                    Exporter<?> exporter = protocol.export(invoker);
                    exporters.add(exporter);
                }
            }
        }
        this.urls.add(url);
    }
```

![img](http://static.oschina.net/uploads/space/2016/0703/154534_XWpK_113011.jpg)

该方法的逻辑是先根据服务配置、协议配置、发布服务的服务器信息、方法列表、dubbo版本等等信息组装成一个发布的URL对象。

若没有配置协议的host，则会自动生成一个host，这对于有多个网卡的系统会，比如本机上部署了虚拟机的情况下，生成的host可能不是开发者期望的地址，这种情况下需要开发者自己指定一个期望的host地址。

服务配置的scope是发布范围，配置为“none”表示不发布服务，则会停止发布操作；

若配置为非“remote”（包括null），则会调用exportLocal（）方法继续发布服务；

若配置为非“local”（包含null），则会将服务发布到远程协议上，这里又2种情况：

第一种是有效的注册中心列表和无注册中心列表，如果有注册中心列表则会逐一将url的协议替换为regitrsy，而且dubbo支持多个注册中心，会注册到多个注册中心去，根据SPI会调用实现类RegistryProtocol的export方法发布服务；RegistryProtocol中主要的逻辑请参考另外一篇博文：[http://my.oschina.net/ywbrj042/blog/690342](http://my.oschina.net/ywbrj042/blog/690342)

 

第二种是无有效的注册中心则会调用配置的协议类型对应的实现类，比如本例中使用的dubbo，则会调用实现类DubboProtocol的export方法方法服务。该协议的详细实现逻辑请参考博文：[http://my.oschina.net/ywbrj042/blog/684718](http://my.oschina.net/ywbrj042/blog/684718)

exportLocal方法源码如下：

```java
    private void exportLocal(URL url) {
        if (!Constants.LOCAL_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
            URL local = URL.valueOf(url.toFullString())
                    .setProtocol(Constants.LOCAL_PROTOCOL)
                    .setHost(NetUtils.LOCALHOST)
                    .setPort(0);

            // modified by lishen
            ServiceClassHolder.getInstance().pushServiceClass(getServiceClass(ref));

            Exporter<?> exporter = protocol.export(
                    proxyFactory.getInvoker(ref, (Class) interfaceClass, local));
            exporters.add(exporter);
            logger.info("Export dubbo service " + interfaceClass.getName() +" to local registry");
        }
    }
```

![img](http://static.oschina.net/uploads/space/2016/0703/155537_s0yz_113011.jpg)

该方法的实现时将url发布在jvm中，在本地同一个jvm中调用服务，由于url的协议改为injvm，则我们根据SPI机制可以知道最终会调用实现类InjvmProtocol的export方法实现发布服务在injvm中。该协议的实现类非常简单，仅仅是用一个map记录了serviceKey和service bean的对应关系，则调用的时候直接拿到服务对应的bean调用目标方法即可。

### 服务引用流程调试

我们通过启动客户端应用远程服务的代码进入调试，进入ReferenceConfig.get()方法。

![img](http://static.oschina.net/uploads/space/2016/0707/070101_R3xl_113011.jpg)

通过代码可以看出来，先检查状态，然后看是否已经有“ref”，如果已经说明已经引用过，则直接返回，否则会调用init()方法进行初始化。

我们进入该方法看看它的源码和流程。

```java
private void init() {
	    if (initialized) {
	        return;
	    }
	    initialized = true;
    	if (interfaceName == null || interfaceName.length() == 0) {
    	    throw new IllegalStateException("<dubbo:reference interface=\"\" /> interface not allow null!");
    	}
    	// 获取消费者全局配置
    	checkDefault();
        appendProperties(this);
        if (getGeneric() == null && getConsumer() != null) {
            setGeneric(getConsumer().getGeneric());
        }
        if (ProtocolUtils.isGeneric(getGeneric())) {
            interfaceClass = GenericService.class;
        } else {
            try {
				interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
				        .getContextClassLoader());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
            checkInterfaceAndMethods(interfaceClass, methods);
        }
        String resolve = System.getProperty(interfaceName);
        String resolveFile = null;
        if (resolve == null || resolve.length() == 0) {
	        resolveFile = System.getProperty("dubbo.resolve.file");
	        if (resolveFile == null || resolveFile.length() == 0) {
	        	File userResolveFile = new File(new File(System.getProperty("user.home")), "dubbo-resolve.properties");
	        	if (userResolveFile.exists()) {
	        		resolveFile = userResolveFile.getAbsolutePath();
	        	}
	        }
	        if (resolveFile != null && resolveFile.length() > 0) {
	        	Properties properties = new Properties();
	        	FileInputStream fis = null;
	        	try {
	        	    fis = new FileInputStream(new File(resolveFile));
					properties.load(fis);
				} catch (IOException e) {
					throw new IllegalStateException("Unload " + resolveFile + ", cause: " + e.getMessage(), e);
				} finally {
				    try {
                        if(null != fis) fis.close();
                    } catch (IOException e) {
                        logger.warn(e.getMessage(), e);
                    }
				}
	        	resolve = properties.getProperty(interfaceName);
	        }
        }
        if (resolve != null && resolve.length() > 0) {
        	url = resolve;
        	if (logger.isWarnEnabled()) {
        		if (resolveFile != null && resolveFile.length() > 0) {
        			logger.warn("Using default dubbo resolve file " + resolveFile + " replace " + interfaceName + "" + resolve + " to p2p invoke remote service.");
        		} else {
        			logger.warn("Using -D" + interfaceName + "=" + resolve + " to p2p invoke remote service.");
        		}
    		}
        }
        if (consumer != null) {
            if (application == null) {
                application = consumer.getApplication();
            }
            if (module == null) {
                module = consumer.getModule();
            }
            if (registries == null) {
                registries = consumer.getRegistries();
            }
            if (monitor == null) {
                monitor = consumer.getMonitor();
            }
        }
        if (module != null) {
            if (registries == null) {
                registries = module.getRegistries();
            }
            if (monitor == null) {
                monitor = module.getMonitor();
            }
        }
        if (application != null) {
            if (registries == null) {
                registries = application.getRegistries();
            }
            if (monitor == null) {
                monitor = application.getMonitor();
            }
        }
        checkApplication();
        checkStubAndMock(interfaceClass);
        Map<String, String> map = new HashMap<String, String>();
        Map<Object, Object> attributes = new HashMap<Object, Object>();
        map.put(Constants.SIDE_KEY, Constants.CONSUMER_SIDE);
        map.put(Constants.DUBBO_VERSION_KEY, Version.getVersion());
        map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        if (ConfigUtils.getPid() > 0) {
            map.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        }
        if (! isGeneric()) {
            String revision = Version.getVersion(interfaceClass, version);
            if (revision != null && revision.length() > 0) {
                map.put("revision", revision);
            }

            String[] methods = Wrapper.getWrapper(interfaceClass).getMethodNames();
            if(methods.length == 0) {
                logger.warn("NO method found in service interface " + interfaceClass.getName());
                map.put("methods", Constants.ANY_VALUE);
            }
            else {
                map.put("methods", StringUtils.join(new HashSet<String>(Arrays.asList(methods)), ","));
            }
        }
        map.put(Constants.INTERFACE_KEY, interfaceName);
        appendParameters(map, application);
        appendParameters(map, module);
        appendParameters(map, consumer, Constants.DEFAULT_KEY);
        appendParameters(map, this);
        String prifix = StringUtils.getServiceKey(map);
        if (methods != null && methods.size() > 0) {
            for (MethodConfig method : methods) {
                appendParameters(map, method, method.getName());
                String retryKey = method.getName() + ".retry";
                if (map.containsKey(retryKey)) {
                    String retryValue = map.remove(retryKey);
                    if ("false".equals(retryValue)) {
                        map.put(method.getName() + ".retries", "0");
                    }
                }
                appendAttributes(attributes, method, prifix + "." + method.getName());
                checkAndConvertImplicitConfig(method, map, attributes);
            }
        }
        //attributes通过系统context进行存储.
        StaticContext.getSystemContext().putAll(attributes);
        ref = createProxy(map);
    }
```

程序的流程是这样的：

1.先检查状态，是否已经销毁过。

2.配置各种参数，将默认参数值、generic值、cosumerConfig参数值进行合并。

3.dubbo支持通用远程服务GenericService接口。应该是无须发布服务，可以通过该接口远程调用动态方法。

4.加载接口类并检查。若接口类不存在，则会抛出异常。检查类和方法，主要是检查接口类的合法性，接口方法的合法性等。

5.dubbo支持通过resolveFile的方式直接制定调用URL，实现p2p直接调用，当注册中心出现故障的时候这样直接调用可以容错。

6.合并application、consumerConfig和module等参数配置。

7.生成合并之后的参数map对象。将引用需要的各项参数进行构造、合并。

8.调用方法ref = createProxy(map);创建代理的引用对象。

我们要进入createProxy方法继续跟踪。

```java
private T createProxy(Map<String, String> map) {
		URL tmpUrl = new URL("temp", "localhost", 0, map);
		final boolean isJvmRefer;
        if (isInjvm() == null) {
            if (url != null && url.length() > 0) { //指定URL的情况下，不做本地引用
                isJvmRefer = false;
            } else if (InjvmProtocol.getInjvmProtocol().isInjvmRefer(tmpUrl)) {
                //默认情况下如果本地有服务暴露，则引用本地服务.
                isJvmRefer = true;
            } else {
                isJvmRefer = false;
            }
        } else {
            isJvmRefer = isInjvm().booleanValue();
        }
		
		if (isJvmRefer) {
			URL url = new URL(Constants.LOCAL_PROTOCOL, NetUtils.LOCALHOST, 0, interfaceClass.getName()).addParameters(map);
			invoker = refprotocol.refer(interfaceClass, url);
            if (logger.isInfoEnabled()) {
                logger.info("Using injvm service " + interfaceClass.getName());
            }
		} else {
            if (url != null && url.length() > 0) { // 用户指定URL，指定的URL可能是对点对直连地址，也可能是注册中心URL
                String[] us = Constants.SEMICOLON_SPLIT_PATTERN.split(url);
                if (us != null && us.length > 0) {
                    for (String u : us) {
                        URL url = URL.valueOf(u);
                        if (url.getPath() == null || url.getPath().length() == 0) {
                            url = url.setPath(interfaceName);
                        }
                        if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
                            urls.add(url.addParameterAndEncoded(Constants.REFER_KEY, StringUtils.toQueryString(map)));
                        } else {
                            urls.add(ClusterUtils.mergeUrl(url, map));
                        }
                    }
                }
            } else { // 通过注册中心配置拼装URL
            	List<URL> us = loadRegistries(false);
            	if (us != null && us.size() > 0) {
                	for (URL u : us) {
                	    URL monitorUrl = loadMonitor(u);
                        if (monitorUrl != null) {
                            map.put(Constants.MONITOR_KEY, URL.encode(monitorUrl.toFullString()));
                        }
                	    urls.add(u.addParameterAndEncoded(Constants.REFER_KEY, StringUtils.toQueryString(map)));
                    }
            	}
            	if (urls == null || urls.size() == 0) {
                    throw new IllegalStateException("No such any registry to reference " + interfaceName  + " on the consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", please config <dubbo:registry address=\"...\" /> to your spring config.");
                }
            }

            if (urls.size() == 1) {
                invoker = refprotocol.refer(interfaceClass, urls.get(0));
            } else {
                List<Invoker<?>> invokers = new ArrayList<Invoker<?>>();
                URL registryURL = null;
                for (URL url : urls) {
                    invokers.add(refprotocol.refer(interfaceClass, url));
                    if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
                        registryURL = url; // 用了最后一个registry url
                    }
                }
                if (registryURL != null) { // 有 注册中心协议的URL
                    // 对有注册中心的Cluster 只用 AvailableCluster
                    URL u = registryURL.addParameter(Constants.CLUSTER_KEY, AvailableCluster.NAME); 
                    invoker = cluster.join(new StaticDirectory(u, invokers));
                }  else { // 不是 注册中心的URL
                    invoker = cluster.join(new StaticDirectory(invokers));
                }
            }
        }

        Boolean c = check;
        if (c == null && consumer != null) {
            c = consumer.isCheck();
        }
        if (c == null) {
            c = true; // default true
        }
        if (c && ! invoker.isAvailable()) {
            throw new IllegalStateException("Failed to check the status of the service " + interfaceName + ". No provider available for the service " + (group == null ? "" : group + "/") + interfaceName + (version == null ? "" : ":" + version) + " from the url " + invoker.getUrl() + " to the consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion());
        }
        if (logger.isInfoEnabled()) {
            logger.info("Refer dubbo service " + interfaceClass.getName() + " from url " + invoker.getUrl());
        }
        // 创建服务代理
        return (T) proxyFactory.getProxy(invoker);
    }
```

首先判断是否要做本地jvm应用，即直接调用本地jvm中对象的方法。直接制定了url地址不做本地应用，没有设置为inJvm协议不做本地引用。

如果做本地应用则会构造一个协议为injvm的URL，然后最终会调用实现类InjvmProtocol的refer方法获得调用本地jvm对象方法的代理对象。返回的是InjvmInvoker的对象。该方法会直接从map中找到目标对象，然后可以直接调用该对象的方法。

如果不做本地应用则会构造一个registry协议的URL，然后会加载注册中心列表，这个与发布服务相似，不做详细描述了。这里有3种情况。

如果没有注册中心列表。因为又没有配置url，则无法找到服务发布，则会抛出异常信息。

如果有一个注册中心url，则会直接通过该url的注册中心获得带来。

如果是多个则会循环每一个注册中心，则会获得多个invokers列表，最后将多个invoker列表使用cluster.join(new StaticDirectory(u, invokers));聚合成一个集群。

每个注册中心都会调用refprotocol.refer(interfaceClass, url)应用远程服务，由于url是registry协议，则最终会调用RegistryProtocol实现类的refer方法。详细说明参考[http://my.oschina.net/ywbrj042/blog/690342](http://my.oschina.net/ywbrj042/blog/690342)。

### 服务调用流程调试

得到服务的代理对象后，我们要调用该代理对象的方法了，如图所示。

![img](http://static.oschina.net/uploads/space/2016/0707/140652_gbS0_113011.jpg)

我们看到得到的是一个动态代理类，对象名称为：com.alibaba.dubbo.common.bytecode.proxy0@69fb6037的动态代理对象。F5进入到代理对象的方法，发现进入了InvokerInvocationHandler.invoke方法开始执行代理方法。

![img](http://static.oschina.net/uploads/space/2016/0707/141149_9LgW_113011.jpg)

如果是在Object类中定义的方法则直接调用invoker的该方法；

如果是toString，hashCode和equals等特殊方法，则也直接调用invoker对象的该方法。

否则其它方法则调用return invoker.invoke(new RpcInvocation(method, args)).recreate();执行。正常情况下都不是那些特殊方法，一般都是自定义接口的自定义方法，走这条路径。

我们进入了实现类MockClusterInvoker的invoke方法，该类事一个支持模拟特性和集群特性的Invoker实现类。

先检查方法参数是否开启模拟，如果开启模拟则进入执行模拟调用方法的分之；

否则进入调用FailoverClusterInvoker实现类的invoke方法，因为我们没有配置集群策略，模拟就是自动故障转移模式的集群。

![img](http://static.oschina.net/uploads/space/2016/0707/143144_4e0q_113011.jpg)

 

然后进入抽象类AbstractClusterInvoker的invoke方法，实际类型是FailoverClusterInvoker；如下图所示：

![img](http://static.oschina.net/uploads/space/2016/0707/145406_SpVv_113011.jpg)

最后会调用执行的代码是：return doInvoke(invocation, invokers, loadbalance);

其中三个参数的值如下：

invocation：参数调用信息，封装了调用的类、方法，参数类型及值等信息。

invokers：从directory中获取到的可用的invoker列表，实际值是size＝1，是一个被多个过滤器、装饰器封装的一个invoker对象。

loadbalance：是类型为RandomLoadBalance的负载均衡器，这是默认的负载均衡策略。

然后进入到FailoverClusterInvoker类的doInvoke方法，如下图。

![img](http://static.oschina.net/uploads/space/2016/0707/150332_CZCG_113011.jpg)

执行的是自动故障转移的集群执行器，先会获得重试次数，然后从列表中选择一个invoker对象，使用了负载均衡器，会排出掉已经执行过的invoker，因为我们这个例子中自由一个invoker对象，因此会一直重试改对象，当某个一个invoker对象执行抛出了非业务的RpcException异常后，则会重新调用下一个。

我们这里调用的是类名为com.alibaba.dubbo.registry.integration.RegistryDirectory$InvokerDelegete@5552768b的对象。进入该方法，最后进入的是类InvokerWrapper.invoke方法。