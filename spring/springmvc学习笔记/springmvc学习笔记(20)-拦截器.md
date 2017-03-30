# springmvc学习笔记(20)-拦截器

标签： springmvc

---

**Contents**

  - [拦截定义](#拦截定义)
  - [拦截器配置](#拦截器配置)
    - [针对HandlerMapping配置](#针对handlermapping配置)
    - [类似全局的拦截器](#类似全局的拦截器)
  - [拦截测试](#拦截测试)
  - [拦截器应用(实现登陆认证)](#拦截器应用实现登陆认证)
    - [需求](#需求)
    - [登陆controller方法](#登陆controller方法)
    - [登陆认证拦截实现](#登陆认证拦截实现)



---


本文主要介绍springmvc中的拦截器，包括拦截器定义和的配置，然后演示了一个链式拦截的测试示例，最后通过一个登录认证的例子展示了拦截器的应用


## 拦截定义

定义拦截器，实现`HandlerInterceptor`接口。接口中提供三个方法。


```java
public class HandlerInterceptor1 implements HandlerInterceptor{
    //进入 Handler方法之前执行
    //用于身份认证、身份授权
    //比如身份认证，如果认证通过表示当前用户没有登陆，需要此方法拦截不再向下执行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //return false表示拦截，不向下执行
        //return true表示放行
        return false;
    }

    //进入Handler方法之后，返回modelAndView之前执行
    //应用场景从modelAndView出发：将公用的模型数据(比如菜单导航)在这里传到视图，也可以在这里统一指定视图
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    //执行Handler完成执行此方法
    //应用场景：统一异常处理，统一日志处理
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
```

可以从名称和参数看出各个接口的顺序和作用:

- `public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception`
   - 参数最少，只有三个
   - 进入 Handler方法之前执行
   - 用于身份认证、身份授权。比如身份认证，如果认证通过表示当前用户没有登陆，需要此方法拦截不再向下执行
- `public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception`
  - 多了一个modelAndView参数
  - 进入Handler方法之后，返回modelAndView之前执行 
  - 应用场景从modelAndView出发：将公用的模型数据(比如菜单导航)在这里传到视图，也可以在这里统一指定视图
- `public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception`
  - 多了一个Exception的类型的参数
  - 执行Handler完成执行此方法
  - 应用场景：统一异常处理，统一日志处理

## 拦截器配置

### 针对HandlerMapping配置

springmvc拦截器针对HandlerMapping进行拦截设置，如果在某个HandlerMapping中配置拦截，经过该HandlerMapping映射成功的handler最终使用该拦截器。

```xml
<bean
	class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping">
	<property name="interceptors">
		<list>
			<ref bean="handlerInterceptor1"/>
			<ref bean="handlerInterceptor2"/>
		</list>
	</property>
</bean>
	<bean id="handlerInterceptor1" class="springmvc.intercapter.HandlerInterceptor1"/>
	<bean id="handlerInterceptor2" class="springmvc.intercapter.HandlerInterceptor2"/>
```

一般不推荐使用。

### 类似全局的拦截器

springmvc配置类似全局的拦截器，springmvc框架将配置的类似全局的拦截器注入到每个HandlerMapping中。

```xml
 <!--拦截器 -->
<mvc:interceptors>
    <!--多个拦截器,顺序执行 -->
    <mvc:interceptor>
        <!-- /**表示所有url包括子url路径 -->
        <mvc:mapping path="/**"/>
        <bean class="com.iot.learnssm.firstssm.interceptor.HandlerInterceptor1"></bean>
    </mvc:interceptor>
    <mvc:interceptor>
        <mvc:mapping path="/**"/>
        <bean class="com.iot.learnssm.firstssm.interceptor.HandlerInterceptor2"></bean>
    </mvc:interceptor>
</mvc:interceptors>
```

## 拦截测试

测试多个拦截器各个方法执行时机

访问`/items/queryItems.action`

- 1.两个拦截器都放行

```
DEBUG [http-apr-8080-exec-1] - DispatcherServlet with name 'springmvc' processing GET request for [/ssm1/items/queryItems.action]
DEBUG [http-apr-8080-exec-1] - Looking up handler method for path /items/queryItems.action
DEBUG [http-apr-8080-exec-1] - Returning handler method [public org.springframework.web.servlet.ModelAndView com.iot.learnssm.firstssm.controller.ItemsController.queryItems(javax.servlet.http.HttpServletRequest,com.iot.learnssm.firstssm.po.ItemsQueryVo) throws java.lang.Exception]
DEBUG [http-apr-8080-exec-1] - Returning cached instance of singleton bean 'itemsController'
DEBUG [http-apr-8080-exec-1] - Last-Modified value for [/ssm1/items/queryItems.action] is: -1
HandlerInterceptor1...preHandle
HandlerInterceptor2...preHandle
DEBUG [http-apr-8080-exec-1] - Fetching JDBC Connection from DataSource
DEBUG [http-apr-8080-exec-1] - Registering transaction synchronization for JDBC Connection
DEBUG [http-apr-8080-exec-1] - Returning JDBC Connection to DataSource
HandlerInterceptor2...postHandle
HandlerInterceptor1...postHandle
DEBUG [http-apr-8080-exec-1] - Rendering view [org.springframework.web.servlet.view.JstlView: name 'items/itemsList'; URL [/WEB-INF/jsp/items/itemsList.jsp]] in DispatcherServlet with name 'springmvc'
DEBUG [http-apr-8080-exec-1] - Added model object 'itemtypes' of type [java.util.HashMap] to request in view with name 'items/itemsList'
DEBUG [http-apr-8080-exec-1] - Added model object 'itemsQueryVo' of type [com.iot.learnssm.firstssm.po.ItemsQueryVo] to request in view with name 'items/itemsList'
DEBUG [http-apr-8080-exec-1] - Added model object 'org.springframework.validation.BindingResult.itemsQueryVo' of type [org.springframework.validation.BeanPropertyBindingResult] to request in view with name 'items/itemsList'
DEBUG [http-apr-8080-exec-1] - Added model object 'itemsList' of type [java.util.ArrayList] to request in view with name 'items/itemsList'
DEBUG [http-apr-8080-exec-1] - Forwarding to resource [/WEB-INF/jsp/items/itemsList.jsp] in InternalResourceView 'items/itemsList'
HandlerInterceptor2...afterCompletion
HandlerInterceptor1...afterCompletion
DEBUG [http-apr-8080-exec-1] - Successfully completed request

```

总结：preHandle方法按顺序执行，postHandle和afterCompletion按拦截器配置的逆向顺序执行。


2.拦截器1放行，拦截器2不放行

```
DEBUG [http-apr-8080-exec-8] - DispatcherServlet with name 'springmvc' processing GET request for [/ssm1/items/queryItems.action]
DEBUG [http-apr-8080-exec-8] - Looking up handler method for path /items/queryItems.action
DEBUG [http-apr-8080-exec-8] - Returning handler method [public org.springframework.web.servlet.ModelAndView com.iot.learnssm.firstssm.controller.ItemsController.queryItems(javax.servlet.http.HttpServletRequest,com.iot.learnssm.firstssm.po.ItemsQueryVo) throws java.lang.Exception]
DEBUG [http-apr-8080-exec-8] - Returning cached instance of singleton bean 'itemsController'
DEBUG [http-apr-8080-exec-8] - Last-Modified value for [/ssm1/items/queryItems.action] is: -1
HandlerInterceptor1...preHandle
HandlerInterceptor2...preHandle
HandlerInterceptor1...afterCompletion
DEBUG [http-apr-8080-exec-8] - Successfully completed request
```

总结：

- 拦截器1放行，拦截器2 preHandle才会执行。
- 拦截器2 preHandle不放行，拦截器2 postHandle和afterCompletion不会执行。
- 只要有一个拦截器不放行，postHandle不会执行。



3.两个拦截器都不放

```
DEBUG [http-apr-8080-exec-9] - DispatcherServlet with name 'springmvc' processing GET request for [/ssm1/items/queryItems.action]
DEBUG [http-apr-8080-exec-9] - Looking up handler method for path /items/queryItems.action
DEBUG [http-apr-8080-exec-9] - Returning handler method [public org.springframework.web.servlet.ModelAndView com.iot.learnssm.firstssm.controller.ItemsController.queryItems(javax.servlet.http.HttpServletRequest,com.iot.learnssm.firstssm.po.ItemsQueryVo) throws java.lang.Exception]
DEBUG [http-apr-8080-exec-9] - Returning cached instance of singleton bean 'itemsController'
DEBUG [http-apr-8080-exec-9] - Last-Modified value for [/ssm1/items/queryItems.action] is: -1
HandlerInterceptor1...preHandle
DEBUG [http-apr-8080-exec-9] - Successfully completed request
```

总结：

- 拦截器1 preHandle不放行，postHandle和afterCompletion不会执行。
- 拦截器1 preHandle不放行，拦截器2不执行。


4.拦截器1不放行，拦截器2放行

```
DEBUG [http-apr-8080-exec-8] - DispatcherServlet with name 'springmvc' processing GET request for [/ssm1/items/queryItems.action]
DEBUG [http-apr-8080-exec-8] - Looking up handler method for path /items/queryItems.action
DEBUG [http-apr-8080-exec-8] - Returning handler method [public org.springframework.web.servlet.ModelAndView com.iot.learnssm.firstssm.controller.ItemsController.queryItems(javax.servlet.http.HttpServletRequest,com.iot.learnssm.firstssm.po.ItemsQueryVo) throws java.lang.Exception]
DEBUG [http-apr-8080-exec-8] - Returning cached instance of singleton bean 'itemsController'
DEBUG [http-apr-8080-exec-8] - Last-Modified value for [/ssm1/items/queryItems.action] is: -1
HandlerInterceptor1...preHandle
DEBUG [http-apr-8080-exec-8] - Successfully completed request
```

和两个拦截器都不行的结果一致，因为拦截器1先执行，没放行


- 小结

根据测试结果，对拦截器应用。

比如：统一日志处理拦截器，需要该拦截器preHandle一定要放行，且将它放在拦截器链接中第一个位置。

比如：登陆认证拦截器，放在拦截器链接中第一个位置。权限校验拦截器，放在登陆认证拦截器之后。（因为登陆通过后才校验权限，当然登录认证拦截器要放在统一日志处理拦截器后面）


## 拦截器应用(实现登陆认证)

### 需求

- 1.用户请求url
- 2.拦截器进行拦截校验
	- 如果请求的url是公开地址（无需登陆即可访问的url），让放行
	- 如果用户session 不存在跳转到登陆页面
	- 如果用户session存在放行，继续操作。


### 登陆controller方法

```java
@Controller
public class LoginController {
    // 登陆
    @RequestMapping("/login")
    public String login(HttpSession session, String username, String password)
            throws Exception {

        // 调用service进行用户身份验证
        // ...

        // 在session中保存用户身份信息
        session.setAttribute("username", username);
        // 重定向到商品列表页面
        return "redirect:/items/queryItems.action";
    }

    // 退出
    @RequestMapping("/logout")
    public String logout(HttpSession session) throws Exception {

        // 清除session
        session.invalidate();

        // 重定向到商品列表页面
        return "redirect:/items/queryItems.action";
    }
}
```


###	登陆认证拦截实现

- 代码实现

```java
/**
 * Created by brian on 2016/3/8.
 * 登陆认证拦截器
 */

public class LoginInterceptor implements HandlerInterceptor {


    //进入 Handler方法之前执行
    //用于身份认证、身份授权
    //比如身份认证，如果认证通过表示当前用户没有登陆，需要此方法拦截不再向下执行
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        //获取请求的url
        String url = request.getRequestURI();
        //判断url是否是公开 地址（实际使用时将公开 地址配置配置文件中）
        //这里公开地址是登陆提交的地址
        if(url.indexOf("login.action")>=0){
            //如果进行登陆提交，放行
            return true;
        }

        //判断session
        HttpSession session  = request.getSession();
        //从session中取出用户身份信息
        String username = (String) session.getAttribute("username");

        if(username != null){
            //身份存在，放行
            return true;
        }

        //执行这里表示用户身份需要认证，跳转登陆页面
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);

        //return false表示拦截，不向下执行
        //return true表示放行
        return false;
    }

    //进入Handler方法之后，返回modelAndView之前执行
    //应用场景从modelAndView出发：将公用的模型数据(比如菜单导航)在这里传到视图，也可以在这里统一指定视图
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        System.out.println("LoginInterceptor...postHandle");

    }

    //执行Handler完成执行此方法
    //应用场景：统一异常处理，统一日志处理
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        System.out.println("LoginInterceptor...afterCompletion");
    }

}
```

- 拦截器配置

```xml
<!--拦截器 -->
<mvc:interceptors>
    <!--多个拦截器,顺序执行 -->
    <!-- 登陆认证拦截器 -->
    <mvc:interceptor>
        <mvc:mapping path="/**"/>
        <bean class="com.iot.learnssm.firstssm.interceptor.LoginInterceptor"></bean>
    </mvc:interceptor>
    
    ...省略
```



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
