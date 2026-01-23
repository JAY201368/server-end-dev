package com.student.system.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于方法级别的权限控制
 * 
 * 使用示例：
 * @RequiresPermission("student:add")
 * @RequiresPermission(value = {"student:add", "student:edit"}, logical = Logical.OR)
 *
 * @author Student System
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 需要的权限代码
     */
    String[] value();

    /**
     * 逻辑关系：AND(与) 或 OR(或)
     * 默认为 AND，即需要拥有所有权限
     */
    Logical logical() default Logical.AND;

    /**
     * 逻辑枚举
     */
    enum Logical {
        /**
         * 与关系：必须拥有所有权限
         */
        AND,
        
        /**
         * 或关系：拥有任意一个权限即可
         */
        OR
    }

}
