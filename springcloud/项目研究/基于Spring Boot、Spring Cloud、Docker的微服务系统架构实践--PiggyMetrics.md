# 基于Spring Boot、Spring Cloud、Docker的微服务系统架构实践--**PiggyMetrics**

原文/项目 地址：[https://github.com/sqshq/PiggyMetrics](https://github.com/sqshq/PiggyMetrics)

翻译转自:http://blog.csdn.net/rickiyeat/article/details/60792925

这个项目的名字叫：Piggy Metrics，一个供个人处理财务的解决方案。

## **简介** 

这是一款概念性的应用程序，基于Spring Boot，Spring Cloud和[Docker](http://lib.csdn.net/base/docker) 简单演示了微服务的[架构](http://lib.csdn.net/base/architecture)模式，顺便说一句，它还有一个非常漂亮整洁的用户界面。下面是它的界面演示： 
![这里写图片描述](https://cloud.githubusercontent.com/assets/6069066/13830155/572e7552-ebe4-11e5-918f-637a49dff9a2.gif)

## 功能服务

PiggyMetrics被分解为三个核心微服务。这些服务都是围绕某些业务能力组织的可独立部署的应用程序。 
![这里写图片描述](https://cloud.githubusercontent.com/assets/6069066/13900465/730f2922-ee20-11e5-8df0-e7b51c668847.png)

### **账户服务** 

包含一般用户输入逻辑和验证：收入/费用项目，储蓄和帐户设置。

| Method | Path                | Description          | User authenticated | Available from UI |
| ------ | ------------------- | -------------------- | ------------------ | ----------------- |
| GET    | /accounts/{account} | 获取指定的帐户数据            |                    |                   |
| GET    | /accounts/current   | 获取当前帐户数据             | ×                  | ×                 |
| GET    | /accounts/demo      | 获取模拟账户数据（预填收入/费用项目等） |                    | ×                 |
| PUT    | /accounts/current   | 保存当前帐户数据             | ×                  | ×                 |
| POST   | /accounts/          | 注册新帐号                |                    | ×                 |

### **统计服务** 

对主要统计参数执行计算，并为每个帐户的时间序列。数据点包含基准货币和时间段的值。此数据用于跟踪帐户生命周期中的现金流动动态（尚未在UI中实现的花式图表）。

| Method | Path                  | Description       | User authenticated | Available from UI |
| ------ | --------------------- | ----------------- | ------------------ | ----------------- |
| GET    | /statistics/{account} | 获取指定的帐户统计信息       |                    |                   |
| GET    | /statistics/current   | 获取当前帐户统计信息        | ×                  | ×                 |
| GET    | /statistics/demo      | 获取模拟帐户统计信息        |                    | ×                 |
| PUT    | /statistics/{account} | 创建或更新指定帐户的时间序列数据点 |                    |                   |

### **通知服务** 

存储用户联系信息和通知设置（如提醒和备份频率）。计划工作人员从其他服务收集所需的信息，并向订阅的客户发送电子邮件。

| Method | Path                            | Description | User authenticated | Available from UI |
| ------ | ------------------------------- | ----------- | ------------------ | ----------------- |
| GET    | /notifications/settings/current | 获取当前的帐户通知设置 | ×                  | ×                 |
| PUT    | /notifications/settings/current | 保存当前帐户通知设置  | ×                  | ×                 |

### ***\*小结：**

- 每个微服务都有自己的数据库，因此没有办法绕过API和直接访问数据库。
- 在这个项目中，使用MongoDB作为每个服务的主数据库。它是支持多种编程语言持久性架构（包括最适合服务需求的数据库类型）。
- Service-to-service的通信是相当简单的：各个微服务之间的通信只使用同步的REST API。在现实世界中通常的做法是使用交互风格的组合。例如，执行同步GET请求以检索数据，并通过消息代理使用异步方法进行创建/更新操作，以便分离服务和缓冲消息，这为我们带来了一致性。

## 基础服务设施

在分布式系统中有一些常见的架构，这可以帮助我们理解核心服务的工作原理。Spring Cloud提供了强大的工具来增强基于Spring Boot的应用程序，以此来实现这些架构。 
![这里写图片描述](https://cloud.githubusercontent.com/assets/6069066/13906840/365c0d94-eefa-11e5-90ad-9d74804ca412.png)

### **Config service** 

Spring Cloud Config是用于分布式系统的水平可扩展的集中式配置服务。支持本地存储、[Git](http://lib.csdn.net/base/git)和Subversion。

在这个项目中，使用`native profile`，它从本地类路径加载配置文件。可以查看`shared`在Config服务资源中的目录。现在，当通知服务请求其配置时，配置服务以`shared/notification-service.yml`和`shared/application.yml`响应（在所有客户端应用程序之间共享）。

#### *客户端使用* 

只需构建具有`spring-cloud-starter-config`依赖的Spring Boot应用程序，自动配置将完成其余所有工作。

现在，不需要在应用程序中使用任何[嵌入式](http://lib.csdn.net/base/embeddeddevelopment)属性。只需提供`bootstrap.yml`应用程序名称和配置服务url：

```yaml
spring：
   application：
     name：notification-service 
  cloud：
     config：
       uri：http：// config：8888 
      fail-fast：true
```

*使用Spring Cloud Config，可以动态地更新配置。* 
例如，EmailService bean已注释`@RefreshScope`。这意味着，可以更改电子邮件文本和主题，而不需要重新部署启动通知服务。

首先，在Config服务器中更改所需的属性。然后，对Notification服务执行刷新请求：`curl -H "Authorization: Bearer #token#" -XPOST http://127.0.0.1:8000/notifications/refresh`

此外，也可以使用Repository webhooks自动执行此过程

#### **小结：**

- 动态更新有一些限制。`@RefreshScope`不与`@Configuration`类一起使用，并且不影响`@Scheduled`方法
- `fail-fast`属性意味着Spring Boot如果它无法连接到Config 
  Service就将启动失败，这在批量启动时非常有用。
- 安全注意事项请往下看

### **Auth service** 

授权的责任完全抽取到单独的服务器，它为后端资源服务授予OAuth2令牌。Auth服务器用于用户授权以及在外围内的安全的机器对机器通信。

在这个项目中，我使用Password credentials授权类型的用户授权（因为它只由本地PiggyMetrics UI使用）和Client Credentials授予微服务权限。

Spring云安全提供了方便的注释和自动配置，使得从服务器端和客户端端都很容易实现。您可以在文档中了解更多信息，并在Auth Server代码中检查配置详细信息。

从客户端，一切工作与传统的基于会话的授权完全相同。您可以Principal从请求中检索对象，检查用户的角色和其他使用基于表达式的访问控制和@PreAuthorize注释的东西。

PiggyMetrics（帐户服务，统计服务，通知服务和浏览器）中的每个客户端都有一个范围：server用于后端服务，以及ui- 用于浏览器。因此，我们还可以保护控制器免受外部访问，例如：

```yaml
@PreAuthorize("#oauth2.hasScope('server')")
@RequestMapping(value = "accounts/{name}", method = RequestMethod.GET)
public List<DataPoint> getStatisticsByAccountName(@PathVariable String name) {
    return statisticsService.findByAccountName(name);
}
```

### **API Gateway** 

可以看到，有三个核心服务，它们向客户端公开外部API。在现实世界的系统中，这个数字可以快速增长以及整个系统的复杂性。实际上，上百个服务可能涉及到渲染一个复杂的网页。

在理论上，客户端可以直接向每个微服务器发出请求。但是显然，这个选项有挑战和限制，如需要知道所有端点地址，分别执行每个信息的http请求，在客户端合并结果。另一个问题是非web友好的协议，可能在后端使用。

通常一个更好的方法是使用API网关。它是进入系统的单个入口点，用于通过将它们路由到适当的后端服务来处理请求，或者通过调用多个后端服务并聚合结果。此外，它可以用于身份验证，洞察，压力和金丝雀[测试](http://lib.csdn.net/base/softwaretest)，服务迁移，静态响应处理，主动流量管理。

Netflix打开了这样一个边缘服务，现在使用Spring Cloud，我们可以使用一个@EnableZuulProxy注释启用它。在这个项目中，我使用Zuul来存储静态内容（ui应用程序），并将请求路由到适当的微服务。这是一个简单的基于前缀的路由配置Notification服务：

```yaml
zuul：
   routes：
     notification-service：
         path：/ notifications / ** 
        serviceId：notification-service 
        stripPrefix：false
```

这意味着所有开始的请求/notifications都将路由到Notification服务。没有硬编码的地址，你可以看到。Zuul使用服务发现机制来定位通知服务实例，以及断路器和负载平衡器，如下所述。

### **Service discovery** 

另一个公知的架构模式是服务发现。它允许自动检测服务实例的网络位置，由于自动扩展，故障和升级，可能会动态分配地址。

服务发现的关键部分是注册表。我在这个项目中使用Netflix Eureka。当客户端负责确定可用服务实例（使用注册表服务器）和负载平衡请求的位置时，Eureka是客户端发现模式的一个很好的例子。

使用Spring Boot，您可以轻松地使用spring-cloud-starter-eureka-server依赖关系，@EnableEurekaServer注释和简单配置属性来构建Eureka注册表。

支持@EnableDiscoveryClient注释的客户端支持bootstrap.yml应用程序名称：

```yaml
spring：
   application：
     name：notification-service
```

现在，在应用程序启动时，它将注册到Eureka服务器并提供元数据，如主机和端口，运行状况指示器URL，主页等。Eureka从属于一个服务的每个实例接收心跳消息。如果心跳故障切换到可配置的时间表，则实例将从注册表中删除。

此外，Eureka提供了一个简单的界面，您可以跟踪运行的服务和可用实例数： [http://localhost:8761](http://localhost:8761/)

### **负载均衡器，断路器和Http客户端** 

Netflix OSS提供了另一个伟大的工具集。

#### *Ribbon* 

Ribbon是一个客户端负载均衡器，它为您提供了对HTTP和TCP客户端行为的大量控制。与传统的负载均衡器相比，每个线上调用不需要额外的跳跃 - 您可以直接联系所需的服务。

开箱即用，它与Spring Cloud和服务发现本身集成。Eureka Client提供了可用服务器的动态列表，以便Ribbon可以在它们之间进行平衡。

#### *Hystrix* 

Hystrix是断路器模式的实现，它提供了对通过网络访问的依赖性的延迟和故障的控制。主要思想是在具有大量微服务的分布式环境中停止级联故障。这有助于快速失败，并尽快恢复 - 自愈的容错系统的重要方面。

除了断路器控制，使用Hystrix，您可以添加一个后备方法，在主命令失败的情况下调用该方法以获取默认值。

此外，Hystrix生成每个命令的执行结果和延迟的指标，我们可以用它来监视系统行为。

#### *Feign* 

Feign是一个声明式Http客户端，它与Ribbon和Hystrix无缝集成。实际上，使用一个spring-cloud-starter-feign依赖项和@EnableFeignClients注释，您拥有一组完整的负载平衡器，断路器和Http客户端，并具有合理的即用型默认配置。

以下是帐户服务的示例：

```java
@FeignClient(name = "statistics-service")
public interface StatisticsServiceClient {

    @RequestMapping(method = RequestMethod.PUT, value = "/statistics/{accountName}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void updateStatistics(@PathVariable("accountName") String accountName, Account account);

}
```

- 你需要的只是一个接口
- 你可以用@RequestMapping在Spring MVC控制器和Feign方法之间共享部分
- 上面的示例指定只需要的服务id - 
  statistics-service，由于通过Eureka自动发现（但显然，您可以访问任何资源与特定的网址）

### **监视仪表板** 

在这个项目配置中，Hystrix的每个微服务通过Spring Cloud Bus（使用AMQP代理）将指标推送到Turbine。监控项目只是一个小的Spring启动应用程序与涡轮和Hystrix仪表板。

看下面如何让它运行。

让我们看看我们的系统在负载下的行为：帐户服务调用统计服务，它的响应具有不同的模仿延迟。响应超时阈值设置为1秒。

| [![img](https://cloud.githubusercontent.com/assets/6069066/14127349/21e90026-f628-11e5-83f1-60108cb33490.gif)](https://cloud.githubusercontent.com/assets/6069066/14127349/21e90026-f628-11e5-83f1-60108cb33490.gif) | [![img](https://cloud.githubusercontent.com/assets/6069066/14127348/21e6ed40-f628-11e5-9fa4-ed527bf35129.gif)](https://cloud.githubusercontent.com/assets/6069066/14127348/21e6ed40-f628-11e5-9fa4-ed527bf35129.gif) | [![img](https://cloud.githubusercontent.com/assets/6069066/14127346/21b9aaa6-f628-11e5-9bba-aaccab60fd69.gif)](https://cloud.githubusercontent.com/assets/6069066/14127346/21b9aaa6-f628-11e5-9bba-aaccab60fd69.gif) | [![img](https://cloud.githubusercontent.com/assets/6069066/14127350/21eafe1c-f628-11e5-8ccd-a6b6873c046a.gif)](https://cloud.githubusercontent.com/assets/6069066/14127350/21eafe1c-f628-11e5-8ccd-a6b6873c046a.gif) |
| ---------------------------------------- | ---------------------------------------- | ---------------------------------------- | ---------------------------------------- |
| `0 ms delay`                             | `500 ms delay`                           | `800 ms delay`                           | `1100 ms delay`                          |
| 表现良好的系统。吞吐量约为22请求/秒。统计服务中的活动线程数较少。中位服务时间约为50 ms。 | 活动线程的数量在增加。我们可以看到紫色线程池拒绝的数量，因此约30-40％的错误，但电路仍然关闭。 | 半开状态：故障命令的比率大于50％，断路器启动。在睡眠窗口的时间后，下一个请求被允许通过。 | 100％的请求失败。电路现在永久打开。在睡眠时间后重试不会再次闭合电路，因为单个请求太慢。 |

### **日志分析** 

当尝试在分布式环境中识别问题时，集中式日志记录可能非常有用。Elasticsearch，Logstash和Kibana堆栈使您可以轻松搜索和分析您的日志，利用率和网络活动数据。我的其他项目中描述的即开即用 Docker配置。

### **安全** 

高级安全配置超出了此概念验证项目的范围。对于真实系统的更真实的模拟，考虑使用https，JCE密钥库加密微服务密码和配置服务器属性内容（有关详细信息，请参阅文档）。

## 基建自动化

部署微服务及其相互依赖性，比部署单片应用程序要复杂得多。拥有完全自动化的基础设施非常重要。我们可以通过持续交付方法实现以下优势：

- 随时释放软件的能力
- 任何构建可能最终都是发布
- 构建工件一次 - 根据需要部署

这里是一个简单的连续交付工作流，在这个项目中实现： 
![这里写图片描述](https://cloud.githubusercontent.com/assets/6069066/14159789/0dd7a7ce-f6e9-11e5-9fbb-a7fe0f4431e3.png)

在此配置中，Travis CI为每个成功的git push建立标记的映像。因此，latest对于Docker Hub上的每个微服务总有图像，并且用git提交哈希标记的旧图像。它很容易部署任何一个，并快速回滚，如果需要。

#### **如何运行所有的东西？** 

记住，你要启动8个Spring Boot应用程序，4个[MongoDB](http://lib.csdn.net/base/mongodb)实例和RabbitMq。确保您4 Gb的计算机上有可用的RAM。您可以始终运行重要的服务，虽然：网关，注册表，配置，Auth服务和帐户服务。

#### **在你开始之前** 

\- 安装Docker和Docker Compose。 
\- 出口环境变量：CONFIG_SERVICE_PASSWORD，NOTIFICATION_SERVICE_PASSWORD，STATISTICS_SERVICE_PASSWORD，ACCOUNT_SERVICE_PASSWORD，MONGODB_PASSWORD

#### **生产模式** 

在这种模式下，所有最新的图像将从Docker Hub中提取。只需复制docker-compose.yml和打docker-compose up -d。

#### **开发模式** 

如果你想自己构建图像（例如在代码中有一些变化），你必须使用maven克隆所有的仓库和构建工件。然后，运行docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

docker-compose.dev.yml继承docker-compose.yml具有在本地构建映像的额外可能性，并公开所有容器端口以方便开发。

#### **重要端口**

```yaml
http://DOCKER-HOST:80 - Gateway
http://DOCKER-HOST:8761 - Eureka Dashboard
http://DOCKER-HOST:9000/hystrix - Hystrix Dashboard
http://DOCKER-HOST:8989 - Turbine stream (source for the Hystrix Dashboard)
http://DOCKER-HOST:15672 - RabbitMq management (default login/password: guest/guest)1234512345
```

#### **小结**

所有Spring Boot应用程序都需要运行Config Server进行启动。但是我们可以同时启动所有容器，因为fail-fastSpring Boot属性和docker restart: always-compose选项。这意味着所有依赖的容器将尝试重新启动，直到Config Server启动并运行。

此外，在所有应用程序启动后，服务发现机制需要一些时间。任何服务都不可用于客户端发现，直到实例，Eureka服务器和客户端都在其本地缓存中具有相同的元数据，因此可能需要3个心跳。默认心跳周期为30秒。