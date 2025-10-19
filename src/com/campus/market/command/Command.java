package com.campus.market.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令注解
 * 用于标注命令处理方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    /**
     * 命令名称
     * @return 命令名称
     */
    String name();
    
    /**
     * 命令描述
     * @return 命令描述
     */
    String description() default "";
}
