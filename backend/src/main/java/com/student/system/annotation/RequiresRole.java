package com.student.system.annotation;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * 用于方法级别的角色控制
 * 
 * 使用示例：
 * @RequiresRole("ROLE_ADMIN")
 * @RequiresRole(value = {"ROLE_ADMIN", "ROLE_TEACHER"}, logical = Logical.OR)
 *
 * @author Student System
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /**
     * 需要的角色代码
     */
    String[] value();

    /**
     * 逻辑关系：AND(与) 或 OR(或)
     * 默认为 AND，即需要拥有所有角色
     */
    Logical logical() default Logical.AND;

    /**
     * 逻辑枚举
     */
    enum Logical {
        /**
         * 与关系：必须拥有所有角色
         */
        AND,
        
        /**
         * 或关系：拥有任意一个角色即可
         */
        OR
    }

}
