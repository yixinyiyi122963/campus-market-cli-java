package com.campus.market.auth;

import com.campus.market.model.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 用于标注命令所需的用户角色
 * 支持指定多个允许的角色
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresRole {
    /**
     * 允许执行该命令的角色数组
     * @return 角色数组
     */
    UserRole[] value();
}
