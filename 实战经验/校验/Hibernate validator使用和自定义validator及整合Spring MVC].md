## Hibernate validator使用和自定义validator及整合Spring MVC]

## Hibernate validator使用

### 导入validation-api-xxx.jar 以及  hibernate-validator-xxx.Final.jar

需要检查的java bean 

Entity.java


```java
1. import javax.validation.constraints.Max;  
2. import org.hibernate.validator.constraints.Length;  
3. public class Entity {  
4.     @Max(value=3)//最大值为3  
5.     private int age;  
6.     @Length(max=1) //字符串长度最大为1,hibernate 扩展的  
7.     private String name;  
8.     public int getAge() {  
9.         return age;  
10.     }  
11.     public void setAge(int age) {  
12.         this.age = age;  
13.     }  
14.     public String getName() {  
15.         return name;  
16.     }  
17.     public void setName(String name) {  
18.         this.name = name;  
19.     }  
20. }  
```

```java
1. import java.util.Set;  
2. import javax.validation.ConstraintViolation;  
3. import javax.validation.Validation;  
4. import javax.validation.Validator;  
5. import javax.validation.ValidatorFactory;  
6. public class Tv {  
7.     public static void main(String[] args) {  
8.         ValidatorFactory factory = Validation.buildDefaultValidatorFactory();  
9.         Validator validator = factory.getValidator();  
10.   
11.         Entity entity = new Entity();  
12.         entity.setAge(12);  
13.         entity.setName("admin");  
14.         Set<ConstraintViolation<Entity>> constraintViolations = validator.validate(entity);  
15.         for (ConstraintViolation<Entity> constraintViolation : constraintViolations) {  
16.             System.out.println("对象属性:"+constraintViolation.getPropertyPath());  
17.             System.out.println("国际化key:"+constraintViolation.getMessageTemplate());  
18.             System.out.println("错误信息:"+constraintViolation.getMessage());  
19.         }  
20.   
21.     }  
22. }  
```
![img](http://img.blog.csdn.net/20150228113318323?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbHdwaGs=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
这里有一个国际化的key值,国际化文件在org.hibernate.validator下面的一系列的properites文件里面,如果需要自定义那么可以拷贝出来放在src目录下

![img](http://img.blog.csdn.net/20150228113627288?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbHdwaGs=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

这里我们拷贝一个出来,新增一个key为maxlength=字符串长度最大不能超过{max} ,可以使用动态参数,这里的max值就是注解里面设定的值

然后修改Entity.java,name属性的message="{maxlength}"


```java
1. @Length(max=1,message="{maxlength}") //{maxlength}对应配置文件中的key. 必须有{}  
2.     private String name;  
```
![img](http://img.blog.csdn.net/20150228113845692?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbHdwaGs=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

## 自定义validator


```java
1. import java.lang.annotation.Documented;  
2. import java.lang.annotation.Retention;  
3. import java.lang.annotation.Target;  
4. import javax.validation.Constraint;  
5. import javax.validation.Payload;  
6.   
7. @Constraint(validatedBy = CannotContainSpacesValidator.class) //具体的实现  
8. @Target( { java.lang.annotation.ElementType.METHOD,  
9.     java.lang.annotation.ElementType.FIELD })  
10. @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)  
11. @Documented  
12. public @interface CannotContainSpaces {  
13.     String message() default "{Cannot.contain.Spaces}"; //提示信息,可以写死,可以填写国际化的key  
14.       
15.     int length() default 5;  
16.       
17.     //下面这两个属性必须添加  
18.     Class<?>[] groups() default {};  
19.     Class<? extends Payload>[] payload() default {};  
20.       
21. }  
```

```java
1. import javax.validation.ConstraintValidator;  
2. import javax.validation.ConstraintValidatorContext;  
3.   
4. public class CannotContainSpacesValidator implements ConstraintValidator<CannotContainSpaces, String> {  
5.     private int len;  
6.     /** 
7.      * 初始参数,获取注解中length的值 
8.      */  
9.     @Override  
10.     public void initialize(CannotContainSpaces arg0) {  
11.         this.len = arg0.length();  
12.     }  
13.   
14.     @Override  
15.     public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {  
16.         if(str != null){  
17.             if(str.indexOf(" ") < 0){  
18.                 return true;  
19.             }  
20.         }else{  
21.             constraintValidatorContext.disableDefaultConstraintViolation();//禁用默认的message的值  
22.             //重新添加错误提示语句  
23.             constraintValidatorContext  
24.             .buildConstraintViolationWithTemplate("字符串不能为空").addConstraintViolation();  
25.         }  
26.         return false;  
27.     }  
28.   
29. }  
```

```java
1. @CannotContainSpaces  
2.     private String name;  
```
![img](http://img.blog.csdn.net/20150228122703740?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbHdwaGs=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

当name为null的时候

![img](http://img.blog.csdn.net/20150228122740509?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbHdwaGs=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

## 整合Spring MVC

首先新增配置文件内容(实体类里面的注解与上面完全相同)


```java
1. <!-- 国际化配置 -->  
2. <bean id="localeResolver"  
3. ​    class="org.springframework.web.servlet.i18n.CookieLocaleResolver" />  
4. <bean id="messageSource"  
5. ​    class="org.springframework.context.support.ReloadableResourceBundleMessageSource">  
6. ​    <property name="basenames">  
7. ​        <list>  
8. ​            <value>classpath:messages/messages</value>  
9. ​            <value>classpath:messages/Validation</value>  
10. ​        </list>  
11. ​    </property>  
12. ​    <property name="useCodeAsDefaultMessage" value="true" />  
13. </bean>  
14. <!-- 注册验证器 -->  
15. <mvc:annotation-driven validator="validator" />    
16.   
17. <bean id="validator" class="org.springframework.validation.beanvalidation.LocalValidatorFactoryBean">    
18. ​      <property name="providerClass" value="org.hibernate.validator.HibernateValidator"/>    
19. ​      <!-- 这里配置将使用上面国际化配置的messageSource -->  
20. ​      <property name="validationMessageSource" ref="messageSource"/>    
21.   </bean>  
```

```java
1. /** 
2.          * 这里的@Valid必须书写, bindingResult参数也必须书写在后面,否则验证不通过就会返回400 
3.          * @param entity 
4.          * @param result 
5.          * @return 
6.          */  
7.         @RequestMapping(value="/valid")  
8.         public String validator(@Valid Entity entity,BindingResult result){  
9.             if(result.hasErrors()){  
10.                 //如果严重没有通过,跳转提示  
11.                 return "error";  
12.             }else{  
13.                 //继续业务逻辑  
14.             }  
15.             return "success";  
16.         }  
```
导入spring标签库


```html
1. <!-- commandName 控制器参数中对象名称 -->  
2. <form:form commandName="entity">  
3.     <!-- 显示全部错误信息用* -->  
4.     <form:errors path="*"/>  
5. </form:form>  
6. <hr/>  
7. <!-- 对象名称.属性名称    如果该对象的指定属性没有通过校验那么显示错误信息(根据当前语言显示不同国家的文字) -->  
8. <form:errors path="entity.name"/>  
```
校验注解说明


```
1. Bean Validation 中内置的 constraint     
2. @Null   被注释的元素必须为 null     
3. @NotNull    被注释的元素必须不为 null     
4. @AssertTrue     被注释的元素必须为 true     
5. @AssertFalse    被注释的元素必须为 false     
6. @Min(value)     被注释的元素必须是一个数字，其值必须大于等于指定的最小值     
7. @Max(value)     被注释的元素必须是一个数字，其值必须小于等于指定的最大值     
8. @DecimalMin(value)  被注释的元素必须是一个数字，其值必须大于等于指定的最小值     
9. @DecimalMax(value)  被注释的元素必须是一个数字，其值必须小于等于指定的最大值     
10. @Size(max=, min=)   被注释的元素的大小必须在指定的范围内     
11. @Digits (integer, fraction)     被注释的元素必须是一个数字，其值必须在可接受的范围内     
12. @Past   被注释的元素必须是一个过去的日期     
13. @Future     被注释的元素必须是一个将来的日期     
14. @Pattern(regex=,flag=)  被注释的元素必须符合指定的正则表达式     
15. Hibernate Validator 附加的 constraint     
16. @NotBlank(message =)   验证字符串非null，且长度必须大于0     
17. @Email  被注释的元素必须是电子邮箱地址     
18. @Length(min=,max=)  被注释的字符串的大小必须在指定的范围内     
19. @NotEmpty   被注释的字符串的必须非空     
20. @Range(min=,max=,message=)  被注释的元素必须在合适的范围内  
```
## 注意

①:在整合Spring MVC的时候,ValidationMessages_zh_CN.properties文件如果不是放在src目录下(如上面放在src/messages/下面) 那么在属性文件里面不能使用动态参数获取了(如${length} ${max}这些). 必须将hibernate validation的国际化属性全部放到src目录下面才可以(不晓得为什么,如果你能解决顺便留个言)

②:我这里使用的是spring 4.1 + hibernate validation 5.1 ,如果你使用的是spring 3.2 需要对于的hibernate validation版本是 4.x的 不然在配置**[html]** [view plain](http://blog.csdn.net/lwphk/article/details/43983669#) [copy](http://blog.csdn.net/lwphk/article/details/43983669#)org.springframework.validation.beanvalidation.LocalValidatorFactoryBean  