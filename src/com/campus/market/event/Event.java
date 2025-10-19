package com.campus.market.event;

/**
 * 事件接口
 * 所有事件必须实现此接口
 */
public interface Event {
    /**
     * 获取事件类型
     * @return 事件类型字符串
     */
    String getEventType();
}
