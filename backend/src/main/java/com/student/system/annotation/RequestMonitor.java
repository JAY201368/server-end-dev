package com.student.system.annotation;

import java.lang.annotation.*;

/**
 * 接口请求监控注解
 * 用于标记需要监控性能和统计调用次数的接口方法
 *
 * 使用方式：在Controller方法上添加此注解
 *
 * @author Student System
 * @since 2024
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMonitor {

    /**
     * 接口名称/描述
     * 默认为空，将使用方法签名
     */
    String value() default "";

    /**
     * 接口分类
     * 用于对接口进行分组统计
     */
    String category() default "default";
}
