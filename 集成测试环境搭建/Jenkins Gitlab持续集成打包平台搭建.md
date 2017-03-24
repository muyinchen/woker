# Jenkins Gitlab持续集成打包平台搭建

# 相关概念

## Jenkins

- [Jenkins](https://jenkins.io/index.html)，一个用Java编写的开源的持续集成工具，提供了软件开发的持续集成服务，可监控并触发持续重复的工作，具有开源，支持多平台和插件扩展，安装简单，界面化管理等特点。更多介绍参考[维基](https://en.wikipedia.org/wiki/Jenkins_(software)介绍.

## Gitlab

- GitLab是一个利用Ruby on Rails开发的开源应用程序，实现一个自托管的Git项目仓库，可通过Web界面进行访问公开的或者私人项目，更多介绍参考[维基](https://en.wikipedia.org/wiki/GitLab)介绍.

## CI

- 持续集成, 简称CI（continuous integration）.
- CI作为敏捷开发重要的一步，其目的在于让产品快速迭代的同时，尽可能保持高质量.
- CI一种可以增加项目可见性，降低项目失败风险的开发实践。其每一次代码更新，都要通过自动化测试来检测代码和功能的正确性，只有通过自动测试的代码才能进行后续的交付和部署.
- CI 是团队成员间（产研测）更好地协调工作，更好的适应敏捷迭代开发，自动完成减少人工干预，保证每个时间点上团队成员提交的代码都能成功集成的，可以很好的用于对Android/iOS项目的打包.

## OTA

- OTA（Over-the-Air Technology）空中下载技术,具体参[考此文介绍](https://support.apple.com/en-us/HT201435).

## pgyer

- [蒲公英(pgyer)](https://www.pgyer.com/apps)为移动开发者提供App免费测试分发应用的服务平台，支持iOS与Android，简单两步分发应用。类似的还有fir.im等.

# 流程结构

简单绘制了下Jenkins的一个流程，如下图：

[![img](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E6%B5%81%E7%A8%8B-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E6%B5%81%E7%A8%8B-min.png)

[IBM Developer](http://www.ibm.com/developerworks/cn/devops/d-continuous-delivery-framework-jenkins/)上也有一个看似更复杂一点的图，如下图
[![img](http://www.ibm.com/developerworks/cn/devops/d-continuous-delivery-framework-jenkins/image007.png)](http://www.ibm.com/developerworks/cn/devops/d-continuous-delivery-framework-jenkins/image007.png)

持续交互流程图：
[![img](http://www.ibm.com/developerworks/cn/devops/d-continuous-delivery-framework-jenkins/image006.png)](http://www.ibm.com/developerworks/cn/devops/d-continuous-delivery-framework-jenkins/image006.png)

# 平台搭建

## Jenkins安装和启动

[官网](https://jenkins.io/index.html)： [https://jenkins.io/index.html](https://jenkins.io/index.html)
[下载](http://mirrors.jenkins-ci.org/war/latest/jenkins.war)： [http://mirrors.jenkins-ci.org/war/latest/jenkins.war](http://mirrors.jenkins-ci.org/war/latest/jenkins.war)

> 安装：

- 依赖于Java环境，首先安装和配置[Java环境](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- 到[官网](https://jenkins.io/index.html)下载Jenkins,双击安装，如果是Mac电脑，会自动生成全局变量jenkins
- 修改参数： jenkins + 相关参数，如
  jenkins –httpPort=8888 #更换端口号，当默认端口8080被占用，或指定特定端口时。

> 启动

- 手动启动： java -jar jenkins.war
- 后台启动(默认端口)： nohup java -jar jenkins.war &
- 后台启动(指定端口)： nohup java -jar jenkins.war -httpPort=88 &
- 后台启动(HTTPS)： nohup java -jar jenkins.war -httpsPort=88 &
- 浏览：[http://localhost:8080/](http://localhost:8080/) , localhost可配置

## Jenkins插件安装

> 插件安装

操作： Manage Jenkins -> Manage Plugins -> Available -> Search -> Click to install，如下图所示

[![img](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E6%8F%92%E4%BB%B6%E5%AE%89%E8%A3%851-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E6%8F%92%E4%BB%B6%E5%AE%89%E8%A3%851-min.png)

[![img](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E6%8F%92%E4%BB%B6%E5%AE%89%E8%A3%852-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E6%8F%92%E4%BB%B6%E5%AE%89%E8%A3%852-min.png)

> 实用插件

- iOS专用：Xcode integration
- Android专用：Gradle plugin
- Gitlab插件：GitLab Plugin 和 Gitlab Hook Plugin
- Git插件： Git plugin
- GitBuckit插件： GitBuckit plugin
- 签名证书管理插件: Credentials Plugin 和Keychains and Provisioning Profiles Management
- FTP插件: Publish over FTP
- 脚本插件: Post-Build Script Plug-in
- 修改Build名称/描述(二维码)： build-name-setter / description setter plugin
- 获取仓库提交的commit log： Git Changelog Plugin
- 自定义全局变量: Environment Injector Plugin
- 自定义邮件插件： Email Extension Plugin
- 获取当前登录用户信息： build-user-vars-plugin
- 显示代码测试覆盖率报表： Cobertura Plugin
- 来展示生成的单元测试报表，支持一切单测框架，如junit、nosetests等： Junit Plugin
- 其它： GIT plugin / SSH Credentials Plugin

## Jenkins系统设置

操作： Manage Jenkins -> Configure System

- Jenkins内部shell UTF-8 编码设置，如下图所示

[![img](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E8%AE%BE%E7%BD%AE1-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E8%AE%BE%E7%BD%AE1-min.png)

- Jenkins Location和Email设置，如下图所示

[![img](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E8%AE%BE%E7%BD%AE2-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E8%AE%BE%E7%BD%AE2-min.png)

- E-mail Notification，设置如下如所示

[![Jenkis系统设置3](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkis%E7%B3%BB%E7%BB%9F%E8%AE%BE%E7%BD%AE3-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkis%E7%B3%BB%E7%BB%9F%E8%AE%BE%E7%BD%AE3-min.png)Jenkis系统设置3

SMTP详细配置请参考 [How to send Email at every build with Jenkins](http://www.nailedtothex.org/roller/kyle/entry/articles-jenkins-email)

## Jenkins Jobs配置

### Jobs基础配置

> 配置编译参数

例如，如果需要打包者自行选择打包类型，如需要编译Release/Debug/Test等不同版本的包，那需要配置Jobs的编译参数，配置方法如下图所示：

[![Jenkins编译设置-参数设置2](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E7%BC%96%E8%AF%91%E8%AE%BE%E7%BD%AE-%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE2-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E7%BC%96%E8%AF%91%E8%AE%BE%E7%BD%AE-%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE2-min.png)Jenkins编译设置-参数设置2

你还可以配置一些其它参数，例如：

[![Jenkins编译设置-参数设置3](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E7%BC%96%E8%AF%91%E8%AE%BE%E7%BD%AE-%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE3-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E7%BC%96%E8%AF%91%E8%AE%BE%E7%BD%AE-%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE3-min.png)Jenkins编译设置-参数设置3

配置完后，build界面中就会出现，如下如所示：

[![Jenkins编译设置-参数设置1](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E7%BC%96%E8%AF%91%E8%AE%BE%E7%BD%AE-%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE1-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E7%BC%96%E8%AF%91%E8%AE%BE%E7%BD%AE-%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE1-min.png)Jenkins编译设置-参数设置1

[How to configure a single Jenkins job to make the release process from trunk or branches?](http://stackoverflow.com/questions/7751735/how-to-configure-a-single-jenkins-job-to-make-the-release-process-from-trunk-or)

> 配置匿名用户权限

后面打包的应用发布时，如果懒得自己搭建服务器，就用Jenkins的，但发布出去的链接需要登录才能访问，这时候你可以设置匿名用户的访问权限，这样匿名用户可以下载访问你提供的应用链接了，非常取巧的方法，如下图：

[![Jenkins编译设置-参数设置4.png](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E7%BC%96%E8%AF%91%E8%AE%BE%E7%BD%AE-%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE4-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E7%BC%96%E8%AF%91%E8%AE%BE%E7%BD%AE-%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE4-min.png)Jenkins编译设置-参数设置4.png

### Jobs源码库配置(Gitlab为例)

> 配置SSH

操作： Manage Jenkins -> Credentials -> Global credentials (unrestricted) -> Add Credentials

1. 本机生成SSH：ssh-keygen -t rsa -C “Your email” ， 生成过程中需设置密码，最终生成id_rsa和id_rsa.pub(公钥)
2. 本机添加秘钥到SSH：ssh-add 文件名（需输入管理密码）
3. Gitlab上添加公钥：复制id_rsa.pub里面的公钥添加到Gitlab
4. Jenkins上配置密钥到SSH：复制id_rsa.pub里面的公钥添加到Jenkins（private key选项）

> 新建Job

在Jenkins中，所有的任务都是以”Job”为单位的。在进行操作前，你需要新建一个Job，Job新建比较简单，只需要在Jenkins管理的首页左侧，点击“New Job”，一般选择free-style software project，再输入Job的名字即可。

> 配置Gitlab

在新建的任务（Jobs）中，Gitlab源码配置如下图：需要输入git仓库和build分支，公钥使用上面配置SSH生成的公钥。

[![img](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E9%A1%B9%E7%9B%AE%E8%AE%BE%E7%BD%AE1-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins-%E9%A1%B9%E7%9B%AE%E8%AE%BE%E7%BD%AE1-min.png)

### Jobs触发条件配置

Jenkins支持多种触发器配置，包括：

- 定期进行构建（Build periodically），定时器使用示例如下：

  H(25-30) 18 **1-5： 工作日下午6点25到30分之间进行build
  H 23 **1-5：工作日每晚23:00至23:59之间的某一时刻进行build
  H(0-29)/15 ****：前半小时内每隔15分钟进行build（开始时间不确定）
  H/20 ****：每隔20分钟进行build（开始时间不确定）

- 根据提交进行构建（Build when a change is pushed to GitHub）

- 定期检测代码更新，如有更新则进行构建（Poll SCM）

### Jobs构建方式/编译 配置

Jenkins支持多种编译配置方式，包括：

- Xcode: iOS编译配置（安装Xcode integration插件）
- Invoke Gradle script： Android编译配置(安装Gradle plugin插件)
- Exceute Shell： 脚本方式

对于iOS应用的构建，如果选择Xcode方式构建，需要配置好开发者证书，具体参考后面签名和整数问题。
推荐使用Exceute Shell方式，简单有效。

### Jobs构建后处理

> Artifacts和邮件通知配置，参考下图

[![Jenkins项目设置-Archive-min](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E9%A1%B9%E7%9B%AE%E8%AE%BE%E7%BD%AE-Archive-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E9%A1%B9%E7%9B%AE%E8%AE%BE%E7%BD%AE-Archive-min.png)Jenkins项目设置-Archive-min

可借助Email Extension Plugin 插件进行详细配置，具体可参考[此文](http://blog.csdn.net/fengshi_sh/article/details/50669754)

[![Jenkins项目设置-邮件-min](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E9%A1%B9%E7%9B%AE%E8%AE%BE%E7%BD%AE-%E9%82%AE%E4%BB%B6-min.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/Jenkins%E9%A1%B9%E7%9B%AE%E8%AE%BE%E7%BD%AE-%E9%82%AE%E4%BB%B6-min.png)Jenkins项目设置-邮件-min

> 发布

- FTP服务器, 最传统的方式，可助蒲公英或者fir.im（安装对应的Jenkins插件）。
- 专业的Artifacts存储仓库, 比如Nexus, Artifactory等。
- 对象存储服务：比如阿里云OSS，AWS S3等，以阿里云为例，具体请参考此文 [在Jenkins持续集成方案中使用阿里云OSS作为Artifacts仓库](http://blog.fit2cloud.com/2015/01/20/aliyun-oss-jenkins-plugin.html)

当然，如果不想自己的应用发布到三方网站，只希望在自己的内网上托管，这样需要在自己内网上搭建服务器，服务器搭建方式有很多种，Mac上可以用自带的Apache服务，也可以用其它服务。

iOS的发布可能希望用到OTA，可参考[此文](http://blog.csdn.net/close_marty/article/details/38559673) 还有这篇[一步一步实现无线安装iOS应用(内网OTA)](http://www.jianshu.com/p/35ca63ec0d8e)

这里分享一个我写的shell脚本模板([已开源](https://github.com/skyseraph/PlistAutoCreate))，可以用于iOS的plist文件自动创建以及OTA简单发布页面的自动创建，参考[此链接获取源码](https://github.com/skyseraph/PlistAutoCreate), 欢迎Star.

自动生成一个简单HTML界面，如下图，点击Install即可安装：

[![Jenkis发布1](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/jenkins_index.png)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/jenkins_index.png)Jenkis发布1

注意，这里iOS7.1以后限定必须要要用https，所以需要对jenkins设置下https，参考下面”后记” 中的Jenkins Https设置

> Last Show

构建成功后最终的结果如下如所示：

[![img](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/jenkins_finish.jpg)](http://7xo4q8.com1.z0.glb.clouddn.com/skyseraph/2016/jenkins_finish.jpg)

# 后记

## 签名和证书问题(iOS)

- [Set up code signing for iOS projects](https://circleci.com/docs/ios-code-signing/)
- [安装JENKINS到发布IPA中的那些坑](http://eqi.cc/2016/05/17/%E5%AE%89%E8%A3%85jenkins%E5%88%B0%E5%8F%91%E5%B8%83ipa%E4%B8%AD%E7%9A%84%E9%82%A3%E4%BA%9B%E5%9D%91/)

## 邮件发送失败

- 实际搭建过程中有遇到此问题，折腾了小会，还以为是公司邮箱地址为题，后面发现仅仅是一个小配置问题。
- Extended E-mail Notification中也需要和E-mail Notification一样，点击Advanced，然后选择Use SMTP Authentication，配置同E-mail Notification的参数。

## Jenkins Https Support

查询Jenkins Https相关命令：

```bash
java -jar jenkins.war --help | grep -i https
```

说明：下面以Mac为例.

> KeyStore方式

- 生成：

```bash
keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass 密码 -dname "cn=WEB"
```

- 使用：

```bash
java -jar jenkins.war --httpPort=-1 --httpsPort=8080 --httpsKeyStore=/目录/keystore.jks --httpsKeyStorePassword=密码
```

- 注意： 第一次使用时需要将”WEB”证书导入，导入步骤为：Chrome导出证书 -> 安装证书 -> 设置证书”全部允许”， 图解步骤可参考下面实用参考中的第5篇文章.

> Certificate方式

- 生成：

```bash
sudo openssl genrsa -out server.key 2048  
sudo openssl req -new -key server.key -out server.csr  
sudo openssl genrsa  -out ca.key 1024  
sudo openssl req  -new -x509 -days 365 -key ca.key -out ca.crt  
sudo openssl ca -in server.csr -out server.crt -cert ca.crt -keyfile ca.key
```

- 使用：

启动Jenkins:

```bash
java -jar jenkins.war --httpsPort=8088 --httpsCertificate=/path/server.crt --httpsPrivateKey=/path/server.key
```

- 注意/说明：
  1 同上
  2 iOS手机需要导入cer证书(ca.crt)
  3 Common Name 填写IP地址或域名地址
  4 第5步骤如果提提示“I am unable to access the ./demoCA/newcerts directory” 错误，解决方法为：
  在当前操作目录，新建demoCA\newcerts2层文件夹
  然后再demoCA文件夹下新建一个空的index.txt文件
  再新建一个serial文件，没有后缀。里面填入01

> 实用参考

- [Starting and Accessing Jenkins](https://wiki.jenkins-ci.org/display/JENKINS/Starting+and+Accessing+Jenkins) 官网说明
- [Jenkins: Switch to SSL/ HTTPS mode](http://balodeamit.blogspot.hk/2014/03/jenkins-switch-to-ssl-https-mode.html) 支持HTTPS的两种方式
- [Installing and Configuring Jenkins](https://wiki.wocommunity.org/display/documentation/Installing+and+Configuring+Jenkins) KeyStore方式
- [Generating a self-signed SSL certificate using the Java keytool command (2004193)](https://discuss.zendesk.com/hc/en-us/articles/202652748-Generating-a-self-signed-SSL-certificate-using-the-Java-keytool-command-2004193-) KeyStore方式
- [Setup Self-Signed Certificates & Trusting them on OS X](http://www.andrewconnell.com/blog/setup-self-signed-certificates-trusting-them-on-os-x#ZAUu13DJEy2AYEYM.99) Certificate方式
- [免费openssl生成ssl证书](http://nassir.iteye.com/blog/1983613) Certificate方式
- [howto-jenkins-ssl](https://github.com/hughperkins/howto-jenkins-ssl)
- [Jenkins fails to Start due to HttpsConnectorFactory](https://support.cloudbees.com/hc/en-us/articles/226744608-Jenkins-fails-to-Start-due-to-HttpsConnectorFactory)
- [openssl 生成服务端证书所遇到的问题](http://yuur369.iteye.com/blog/1720321)

# Refs

- [Using Jenkins](https://wiki.jenkins-ci.org/display/JENKINS/Use+Jenkins)
- [Building a software project](https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project)
- [Auto build and deploy iOS apps using Jenkins](https://danielbeard.wordpress.com/2013/04/23/auto-build-and-deploy-ios-apps-using-jenkins/)
- [Continuous Integration for iOS with Jenkins](http://savvyapps.com/blog/continuous-integration-ios-jenkins)
- [GitLab Documentation](http://docs.gitlab.com/ee/integration/jenkins.html#jenkins-ci-deprecated-service)
- [Setup Jenkins + TestFlight for iOS apps](https://felixha.wordpress.com/2014/04/24/luu-bi/)
- [Installing Jenkins on OS X Yosemite](https://nickcharlton.net/posts/installing-jenkins-osx-yosemite.html)
- [基于 Jenkins 快速搭建持续集成环境](https://www.ibm.com/developerworks/cn/java/j-lo-jenkins/)
- [Jenkins入门](http://files.cnblogs.com/files/itech/Jenkins%E5%85%A5%E9%97%A8.pdf)
- [jenkins中集成OTA发布](http://blog.csdn.net/close_marty/article/details/38559673)