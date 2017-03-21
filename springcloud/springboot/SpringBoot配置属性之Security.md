# SpringBoot配置属性之Security

## 序

spring security是springboot支持的权限控制系统。

- security.basic.authorize-mode要使用权限控制模式.
- security.basic.enabled是否开启基本的鉴权，默认为true
- security.basic.path需要鉴权的path，多个的话以逗号分隔，默认为[/**]
- security.basic.realmHTTP basic realm 的名字，默认为Spring
- security.enable-csrf是否开启cross-site request forgery校验，默认为false.
- security.filter-orderSecurity filter chain的order，默认为0
- security.headers.cache是否开启http头部的cache控制，默认为false.
- security.headers.content-type是否开启X-Content-Type-Options头部，默认为false.
- security.headers.frame是否开启X-Frame-Options头部，默认为false.
- security.headers.hsts指定HTTP Strict Transport Security (HSTS)模式(none, domain, all).
- security.headers.xss是否开启cross-site scripting (XSS) 保护，默认为false.
- security.ignored指定不鉴权的路径，多个的话以逗号分隔.
- security.oauth2.client.access-token-uri指定获取access token的URI.
- security.oauth2.client.access-token-validity-seconds指定access token失效时长.
- security.oauth2.client.additional-information.[key]设定要添加的额外信息.
- security.oauth2.client.authentication-scheme指定传输不记名令牌(bearer token)的方式(form, header, none,query)，默认为header
- security.oauth2.client.authorities指定授予客户端的权限.
- security.oauth2.client.authorized-grant-types指定客户端允许的grant types.
- security.oauth2.client.auto-approve-scopes对客户端自动授权的scope.
- security.oauth2.client.client-authentication-scheme传输authentication credentials的方式(form, header, none, query)，默认为header方式
- security.oauth2.client.client-id指定OAuth2 client ID.
- security.oauth2.client.client-secret指定OAuth2 client secret. 默认是一个随机的secret.
- security.oauth2.client.grant-type指定获取资源的access token的授权类型.
- security.oauth2.client.id指定应用的client ID.
- security.oauth2.client.pre-established-redirect-uri服务端pre-established的跳转URI.
- security.oauth2.client.refresh-token-validity-seconds指定refresh token的有效期.
- security.oauth2.client.registered-redirect-uri指定客户端跳转URI，多个以逗号分隔.
- security.oauth2.client.resource-ids指定客户端相关的资源id，多个以逗号分隔.
- security.oauth2.client.scopeclient的scope
- security.oauth2.client.token-name指定token的名称
- security.oauth2.client.use-current-uri是否优先使用请求中URI，再使用pre-established的跳转URI. 默认为true
- security.oauth2.client.user-authorization-uri用户跳转去获取access token的URI.
- security.oauth2.resource.id指定resource的唯一标识.
- security.oauth2.resource.jwt.key-uriJWT token的URI. 当key为公钥时，或者value不指定时指定.
- security.oauth2.resource.jwt.key-valueJWT token验证的value. 可以是对称加密或者PEMencoded RSA公钥. 可以使用URI作为value.
- security.oauth2.resource.prefer-token-info是否使用token info，默认为true
- security.oauth2.resource.service-id指定service ID，默认为resource.
- security.oauth2.resource.token-info-uritoken解码的URI.
- security.oauth2.resource.token-type指定当使用userInfoUri时，发送的token类型.
- security.oauth2.resource.user-info-uri指定user info的URI
- security.oauth2.sso.filter-order如果没有显示提供WebSecurityConfigurerAdapter时指定的Filter order.
- security.oauth2.sso.login-path跳转到SSO的登录路径默认为/login.
- security.require-ssl是否对所有请求开启SSL，默认为false.
- security.sessions指定Session的创建策略(always, never, if_required, stateless).
- security.user.name指定默认的用户名，默认为user.
- security.user.password默认的用户密码.
- security.user.role默认用户的授权角色.