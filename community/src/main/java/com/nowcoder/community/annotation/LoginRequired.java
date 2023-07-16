package com.nowcoder.community.annotation;

import java.lang.annotation.*;

//@interface 表示这是一个注解。
@Target(ElementType.METHOD)// 表示这个注解可以用在什么地方，这里是用在方法上。
@Retention(RetentionPolicy.RUNTIME)// 表示这个注解在什么时候还有效，这里是在运行时有效。
//@Inherited 表示这个注解可以被子类继承。如果某个类使用@Inherited修饰，该类的子类将自动使用@Inherited修饰。
//@Documented 表示当执行javadoc的时候，本注解会生成相关文档。
public @interface LoginRequired {
}