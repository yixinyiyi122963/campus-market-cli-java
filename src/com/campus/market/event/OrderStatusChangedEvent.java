package com.campus.market.event;

import com.campus.market.model.Order;
import com.campus.market.model.OrderStatus;

/**
 * 订单状态变更事件
 * 当订单状态发生变化时触发此事件
 */
public class OrderStatusChangedEvent implements Event {
    private final Order order;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return "OrderStatusChanged";
    }

    public Order getOrder() {
        return order;
    }

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }
}
