# Hibernate-validator校验框架使用

可以有两种使用方法：

## 第一种：在要检验的Dto对象之前加@Valid注解，

这种方法必须配合BindingResult参数一起使用，否则验证不通过就会返回400，并且抛出"org.springframework.validation.BindException"异常，举例如下。这种交互不利于前端获取校验信息，因此需要配合BindingResult对校验结果进行封装之后再返回给前端。

```java
{
  "timestamp": 1489024472175,
  "status": 400,
  "error": "Bad Request",
  "exception": "org.springframework.validation.BindException",
  "errors": [
    {
      "codes": [
        "Range.userDto.age",
        "Range.age",
        "Range.int",
        "Range"
      ],
      "arguments": [
        {
          "codes": [
            "userDto.age",
            "age"
          ],
          "arguments": null,
          "defaultMessage": "age",
          "code": "age"
        },
        150,
        1
      ],
      "defaultMessage": "年龄必须介于1到150之间",
      "objectName": "userDto",
      "field": "age",
      "rejectedValue": 152,
      "bindingFailure": false,
      "code": "Range"
    }
  ],
  "message": "Validation failed for object='userDto'. Error count: 1",
  "path": "/test/login2"
}
```

[具体可以参考这个例子](https://github.com/muyinchen/woker/blob/master/%E5%AE%9E%E6%88%98%E7%BB%8F%E9%AA%8C/%E6%A0%A1%E9%AA%8C/Hibernate%20validator%E4%BD%BF%E7%94%A8%E5%92%8C%E8%87%AA%E5%AE%9A%E4%B9%89validator%E5%8F%8A%E6%95%B4%E5%90%88Spring%20MVC%5D.md)

## 第二种：将validation逻辑封装成工具类，

使用工具类对dto进行校验，然后根据校验结果做响应的处理。封装好的一个工具类如下。



```java
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

[具体实现可以参考这个例子](https://github.com/muyinchen/woker/blob/master/%E5%AE%9E%E6%88%98%E7%BB%8F%E9%AA%8C/%E6%A0%A1%E9%AA%8C/Hibernate%20Validation%E4%BD%BF%E7%94%A8%E7%A4%BA%E4%BE%8B%E5%8F%8A%E8%AE%B2%E8%A7%A3.md)