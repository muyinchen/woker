# Hibernate Validation使用示例及讲解

在项目开发过程中，后台在很多场景都需要进行校验操作，比如：前台表单提交到后台，系统接口调用，数据传输等等。而且很多项目都采用MVC分层式设计，每层还有需要进行相应地校验，这样在项目较大，多人协作开发的时候，会造成大量重复校验代码，且出错率高。

针对这个问题， [JCP](http://zh.wikipedia.org/wiki/JCP) 出台一个 [JSR 303-Bean Validation规范](https://www.jcp.org/en/jsr/detail?id=303) ，而Hibernate Validator 作为Bean Validation的参考实现，提供了JSR 303规范中所有内置constraint的实现，除此之外还有一些附加的constraint。

Hibernate Validation的使用非常简单，只用在相应的实体类中加上注解，再调用对应的校验API方法即可。

Hibernate Validation目前最新的稳定版本是：5.1.3。 [下载地址](http://sourceforge.net/projects/hibernate/files/hibernate-validator/5.1.3.Final/hibernate-validator-5.1.3.Final-dist.zip/download)

[官网地址](http://hibernate.org/validator/)

[官方英文使用手册](http://docs.jboss.org/hibernate/validator/5.1/reference/en-US/html_single/)

[官方中文使用手册地址](http://docs.jboss.org/hibernate/validator/4.2/reference/zh-CN/html_single/) (中文版目前最新的是4.3版本)

具体使用方法请查看上面的官方使用手册地址，每个注解对应的含义在官方手册2.4章节有详细介绍，内容太多我就不贴过来了。下面直接上最常用情况（实体类校验）的示例代码。

## 一、依赖包

J2SE环境下除了需要引入Hibernate Validation包外，还需要额外引入两个实现表达式语言的包。J2EE环境如果容器提供不需要再引入。下面是J2SE环境下的依赖包：

```java
<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-validator</artifactId>
			<version>5.1.3.Final</version>
		</dependency>
		<dependency>
			<groupId>javax.el</groupId>
			<artifactId>javax.el-api</artifactId>
			<version>2.2.4</version>
		</dependency>
		<dependency>
			<groupId>org.glassfish.web</groupId>
			<artifactId>javax.el</artifactId>
			<version>2.2.4</version>
		</dependency>
```

## 二、校验工具类

工具类提供了校验实体类、实体字段的方法，返回一个自定义的校验对象。

```java
/**
 * 校验工具类
 * @author wdmcygah
 *
 */
public class ValidationUtils {

	private static Validator validator =  Validation.buildDefaultValidatorFactory().getValidator();
	
	public static <T> ValidationResult validateEntity(T obj){
		ValidationResult result = new ValidationResult();
		 Set<ConstraintViolation<T>> set = validator.validate(obj,Default.class);
		 if( CollectionUtils.isNotEmpty(set) ){
			 result.setHasErrors(true);
			 Map<String,String> errorMsg = new HashMap<String,String>();
			 for(ConstraintViolation<T> cv : set){
				 errorMsg.put(cv.getPropertyPath().toString(), cv.getMessage());
			 }
			 result.setErrorMsg(errorMsg);
		 }
		 return result;
	}
	
	public static <T> ValidationResult validateProperty(T obj,String propertyName){
		ValidationResult result = new ValidationResult();
		 Set<ConstraintViolation<T>> set = validator.validateProperty(obj,propertyName,Default.class);
		 if( CollectionUtils.isNotEmpty(set) ){
			 result.setHasErrors(true);
			 Map<String,String> errorMsg = new HashMap<String,String>();
			 for(ConstraintViolation<T> cv : set){
				 errorMsg.put(propertyName, cv.getMessage());
			 }
			 result.setErrorMsg(errorMsg);
		 }
		 return result;
	}
}
```

## 三、校验返回对象

```java
/**
 * 校验结果
 * @author wdmcygah
 *
 */
public class ValidationResult {

	//校验结果是否有错
	private boolean hasErrors;
	
	//校验错误信息
	private Map<String,String> errorMsg;

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	public Map<String, String> getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(Map<String, String> errorMsg) {
		this.errorMsg = errorMsg;
	}

	@Override
	public String toString() {
		return "ValidationResult [hasErrors=" + hasErrors + ", errorMsg="
				+ errorMsg + "]";
	}

}
```

## 四、被校验实体

```java
public class SimpleEntity {

	@NotBlank(message="名字不能为空或者空串")
	@Length(min=2,max=10,message="名字必须由2~10个字组成")
	private String name;
	
	@Past(message="时间不能晚于当前时间")
	private Date date;
	
	@Email(message="邮箱格式不正确")
	private String email;
	
	@Pattern(regexp="(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{5,10}",message="密码必须是5~10位数字和字母的组合")
	private String password;
	
	@AssertTrue(message="字段必须为真")
	private boolean valid;
  
       //get set方法省略，自己添加
}
```

有些情况下，Hibernate Validation自带的注解不能够满足需求，我们想定制一个注解进行使用，此时可以参考下面的示例（自定义密码注解及校验规则）。

## 一、密码注解

```java
package research.hibernate.validation.extend;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface Password {

	String message() default "{密码必须是5~10位数字和字母组合}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

## 二、密码校验类

```java
/**
 * 自定义密码校验类
 * @author wdmcygah
 *
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

	//5~10位的数字与字母组合
	private static Pattern pattern = Pattern.compile("(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{5,10}");
	
	public void initialize(Password constraintAnnotation) {
		//do nothing
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		if( value==null ){
			return false;
		}
		Matcher m = pattern.matcher(value);
		return m.matches();
	}	
}
```

## 三、被校验实体

```java
public class ExtendEntity {

	@Password
	private String password;
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
```

对应的测试类如下：

```java
public class ValidationUtilsTest extends TestCase{

  public void validateSimpleEntity() {
	  SimpleEntity se = new SimpleEntity();
	  se.setDate(new Date());
	  se.setEmail("123");
	  se.setName("123");
	  se.setPassword("123");
	  se.setValid(false);
	  ValidationResult result = ValidationUtils.validateEntity(se);
	  System.out.println("--------------------------");
	  System.out.println(result);
	  Assert.assertTrue(result.isHasErrors());
  }
  
  public void validateSimpleProperty() {
	  SimpleEntity se = new SimpleEntity();
	  ValidationResult result = ValidationUtils.validateProperty(se,"name");
	  System.out.println("--------------------------");
	  System.out.println(result);
	  Assert.assertTrue(result.isHasErrors());
  }
  
  public void validateExtendEntity() {
	  ExtendEntity ee = new ExtendEntity();
	  ee.setPassword("212");
	  ValidationResult result = ValidationUtils.validateEntity(ee);
	  System.out.println("--------------------------");
	  System.out.println(result);
	  Assert.assertTrue(result.isHasErrors());
  }
}
```

代码在JDK1.8下测试通过。 **完整代码可查看我的Github仓库：** [https://github.com/wdmcygah/research-J2SE](https://github.com/wdmcygah/research-J2SE)

备注：

（1）上述示例只是展示了Hibernate Validation比较常用的示例，框架其实还支持方法返回值、方法参数校验，另外也可以通过XML进行配置，校验还可以分组、合并等等。这些内容请查阅官方使用手册。

（2）另外还有一个也还不错的校验框架：OVAL。 [OVAL源码地址](http://sourceforge.net/projects/oval/)