package com.library.auth.annotation;

import java.lang.annotation.*;

/**
 * 自定义日志注解
 * 可标注在类或方法上
 ** 注解参数说明：
 * - value: 接口的操作描述（例如：新增用户、导出报表），默认为空字符串
 * - saveResponse: 是否保存响应内容（有些大文件接口可以设为 false），默认为 true
 */
@Target({ElementType.METHOD, ElementType.TYPE}) //允许在方法和类上使用
@Retention(RetentionPolicy.RUNTIME)           // 运行时有效
@Documented
public @interface AutoLog {
    /**
     * 接口的操作描述（例如：新增用户、导出报表）
     * 
     * @return 操作描述字符串，默认为空字符串
     */
    String value() default "";

    /**
     * 是否保存响应内容（有些大文件接口可以设为 false）
     * 
     * @return 布尔值，表示是否保存响应内容，默认为 true
     */
    boolean saveResponse() default true;
}