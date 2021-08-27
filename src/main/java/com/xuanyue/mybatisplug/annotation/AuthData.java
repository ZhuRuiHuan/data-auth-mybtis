package com.xuanyue.mybatisplug.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * 将此注解标注在需要进行数据权限控制的 Mybatis-Dao 接口的方法上<br>
 *
 * value传入需要用到的 AuthData 数据
 * 没有参数时,则调用所有IAuthData.getDataAuth()去获取数据
 * @author Administrator
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthData {
	String[] value();
}
