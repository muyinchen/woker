# 手把手教你搭建Jenkins+Github持续集成环境

## 1.安装jenkins

### 环境：`CentOS 7.0`

### 安装方式：

```bash
$ yum install yum-fastestmirror -y  #安装自动选择最快源的插件
#添加Jenkins源:
$ sudo wget -O /etc/yum.repos.d/jenkins.repo http://jenkins-ci.org/redhat/jenkins.repo
$ sudo rpm --import http://pkg.jenkins-ci.org/redhat/jenkins-ci.org.key
$ yum install jenkins               #安装jenkins
```

### 启动方式：

`$ sudo service jenkins start`

### 访问方式：

浏览器输入`http://your server ip:8080/`

### 更改配置（如端口）方式：

```bash
$ vim /etc/sysconfig/jenkins
$ sudo service jenkins restart
```

## 2.jenkins基础配置

### Unlock

经过上面的配置，你可以访问你的Jenkins了，在浏览器中输入：`http://your server ip:8080/`，效果如下：

![img](http://upload-images.jianshu.io/upload_images/2518611-d93724fadf9ab855.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

按照提示我们执行`cat /var/lib/jenkins/secrets/initialAdminPassword`得到`Administrator password`，输入后点击Continue，如下：

![img](http://upload-images.jianshu.io/upload_images/2518611-47a485249b5741b3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

选择`install suggested plugins`，等待安装完毕，如果有安装失败的可以跳过，之后可以手动根据需求安装。

![img](http://upload-images.jianshu.io/upload_images/2518611-72114d3f8f7a42c9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 设置初始账户和密码

![img](http://upload-images.jianshu.io/upload_images/2518611-7e1e1d4a0317292e.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

设置完成后进入界面：

![img](http://upload-images.jianshu.io/upload_images/2518611-6a2a1d6ab190eca4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 3.github配置

### sercret text

注：此处需要一个对项目有写权限的账户

> 进入github --> setting --> Personal Access Token --> Generate new token

![img](http://upload-images.jianshu.io/upload_images/2518611-6c844d8a6bb58800.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![img](http://upload-images.jianshu.io/upload_images/436630-943711ff2a74919d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

自己先保存此`token`，如果丢失，之后再也无法找到这个`token`。

### GitHub webhooks 设置

> 进入GitHub上指定的项目 --> setting --> WebHooks&Services --> add webhook --> 输入刚刚部署jenkins的服务器的IP

![img](http://upload-images.jianshu.io/upload_images/436630-1dbb649d8ae063b3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 4.jenkins的github配置

### 安装GitHub Plugin

> 系统管理-->插件管理-->可选插件

直接安装Github Plugin, jenkins会自动帮你解决其他插件的依赖，直接安装该插件Jenkins会自动帮你安装plain-credentials 、[Git](http://lib.csdn.net/base/git) 、 credentials 、 github-api

![img](http://upload-images.jianshu.io/upload_images/436630-ff8c8744ed7ade0d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 配置GitHub Plugin

> 系统管理 --> 系统设置 --> GitHub --> Add GitHub Sever

如下图所示

![img](http://upload-images.jianshu.io/upload_images/2518611-df2c88b65c841fa6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

API URL 输入 `https://api.github.com`，Credentials点击Add添加，Kind选择Secret Text,具体如下图所示。

![img](http://upload-images.jianshu.io/upload_images/2518611-547c6e295e263296.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

设置完成后，点击`TestConnection`,提示`Credentials
 verified for user UUserName, rate limit: xxx`,则表明有效。

### 创建一个freestyle任务

\- General 设置
填写GitHub project URL, 也就是你的项目主页
eg. `https://github.com/your_name/your_repo_name`

![img](http://upload-images.jianshu.io/upload_images/2518611-7c250beb46759edf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

\- 配置源码管理

![img](http://upload-images.jianshu.io/upload_images/2518611-9d7836236cbf989b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

1. 填写项目的git地址, eg. `https://github.com/your_name/your_repo_name.git`
2. 添加github用户和密码
3. 选择githubweb源码库浏览器，并填上你的项目URL，这样每次构建都会生成对应的changes，可直接链到github上看变更详情

\- 构建触发器，构建环境

![img](http://upload-images.jianshu.io/upload_images/2518611-9906f0e72e95a468.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

\- 构建

![img](http://upload-images.jianshu.io/upload_images/2518611-a84115bff915a637.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

\- 构建后操作

![img](http://upload-images.jianshu.io/upload_images/2518611-e8678da6c93bc25c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

最后点击保存即可

## 5.测试效果

[测试](http://lib.csdn.net/base/softwaretest)效果1

![img](http://upload-images.jianshu.io/upload_images/2518611-57dca991dbff5808.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

测试效果2

![img](http://upload-images.jianshu.io/upload_images/2518611-a10aefcc8035bb5e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 6.参考：

> [Jenkins+Github持续集成](http://www.jianshu.com/p/b2ed4d23a3a9)
> [Jenkins入门总结](http://www.cnblogs.com/itech/archive/2011/11/23/2260009.html)