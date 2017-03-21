# SpringBoot配置属性之其他

## aop

- spring.aop.auto是否支持@EnableAspectJAutoProxy，默认为: true
- spring.aop.proxy-target-classtrue为使用CGLIB代理，false为JDK代理，默认为false

## application

- spring.application.admin.enabled是否启用admin特性，默认为: false
- spring.application.admin.jmx-name指定admin MBean的名称，默认为: org.springframework.boot:type=Admin,name=SpringApplication

## autoconfig

- spring.autoconfigure.exclude配置要排除的Auto-configuration classes.

## batch

- spring.batch.initializer.enabled是否在必要时创建batch表，默认为true
- spring.batch.job.enabled是否在启动时开启batch job，默认为true
- spring.batch.job.names指定启动时要执行的job的名称，逗号分隔，默认所有job都会被执行
- spring.batch.schema指定要初始化的sql语句路径，默认:classpath:org/springframework/batch/core/schema-@@platform@@.sql)
- spring.batch.table-prefix指定批量处理的表的前缀.

## jmx

- spring.jmx.default-domain指定JMX domain name.
- spring.jmx.enabled是否暴露jmx，默认为true
- spring.jmx.server指定MBeanServer bean name. 默认为: mbeanServer)

## mail

- spring.mail.default-encoding指定默认MimeMessage的编码，默认为: UTF-8
- spring.mail.host指定SMTP server host.
- spring.mail.jndi-name指定mail的jndi名称
- spring.mail.password指定SMTP server登陆密码.
- spring.mail.port指定SMTP server port.
- spring.mail.properties指定JavaMail session属性.
- spring.mail.protocol指定SMTP server使用的协议，默认为: smtp
- spring.mail.test-connection指定是否在启动时测试邮件服务器连接，默认为false
- spring.mail.username指定SMTP server的用户名.

## sendgrid

- spring.sendgrid.password指定SendGrid password.
- spring.sendgrid.proxy.host指定SendGrid proxy host.
- spring.sendgrid.proxy.port指定SendGrid proxy port.
- spring.sendgrid.username指定SendGrid username.

## social

- spring.social.auto-connection-views是否开启连接状态的视图，默认为false
- spring.social.facebook.app-id指定应用id
- spring.social.facebook.app-secret指定应用密码
- spring.social.linkedin.app-id指定应用id
- spring.social.linkedin.app-secret指定应用密码
- spring.social.twitter.app-id指定应用ID.
- spring.social.twitter.app-secret指定应用密码