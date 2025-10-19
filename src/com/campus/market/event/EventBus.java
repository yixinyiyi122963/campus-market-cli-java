package com.campus.market.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线（单例模式）
 * 负责事件的发布和订阅
 * 使用观察者模式实现解耦
 */
public class EventBus {
    private static EventBus instance;
    // 事件类型到监听器列表的映射
    private final Map<Class<? extends Event>, List<EventListener<? extends Event>>> listeners;

    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }

    /**
     * 获取EventBus单例实例
     * @return EventBus实例
     */
    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * 订阅事件
     * @param eventType 事件类型
     * @param listener 事件监听器
     * @param <T> 事件类型
     */
    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * 发布事件
     * @param event 事件对象
     * @param <T> 事件类型
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        List<EventListener<? extends Event>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            // 使用Lambda表达式遍历并触发所有监听器
            eventListeners.forEach(listener -> 
                ((EventListener<T>) listener).onEvent(event)
            );
        }
    }

    /**
     * 清空所有监听器
     */
    public void clear() {
        listeners.clear();
    }
}
