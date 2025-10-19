package com.campus.market.event;

/**
 * 事件监听器接口（函数式接口）
 * 用于处理特定类型的事件
 * 
 * @param <T> 事件类型
 */
@FunctionalInterface
public interface EventListener<T extends Event> {
    /**
     * 处理事件
     * @param event 事件对象
     */
    void onEvent(T event);
}
