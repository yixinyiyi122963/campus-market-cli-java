package com.campus.market.model;

/**
 * 商品状态枚举
 * 定义商品在交易流程中的各种状态
 */
public enum ProductStatus {
    AVAILABLE("在售"),       // 商品可购买
    PENDING("待发货"),       // 商品已下单，等待卖家发货
    SOLD("已售出"),         // 商品已售出
    REMOVED("已下架");      // 商品已下架

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
