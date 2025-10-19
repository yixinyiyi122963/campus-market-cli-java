package com.campus.market.model;

/**
 * 订单状态枚举
 * 定义订单在交易流程中的各种状态
 */
public enum OrderStatus {
    PENDING_SHIP("待发货"),      // 等待卖家发货
    SHIPPED("已发货"),          // 卖家已发货，等待买家确认收货
    COMPLETED("已完成"),        // 买家已确认收货，订单完成
    CANCELLED("已取消");        // 订单已取消

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
