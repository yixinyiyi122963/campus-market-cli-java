package com.campus.market.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 表示买家对商品的购买订单
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;          // 订单ID
    private String productId;        // 商品ID
    private String buyerId;          // 买家ID
    private String sellerId;         // 卖家ID
    private BigDecimal price;        // 订单金额
    private OrderStatus status;      // 订单状态
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime shippedAt;  // 发货时间
    private LocalDateTime receivedAt; // 收货时间

    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING_SHIP;
    }

    public Order(String orderId, String productId, String buyerId, 
                String sellerId, BigDecimal price) {
        this();
        this.orderId = orderId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.price = price;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return String.format("Order[id=%s, productId=%s, buyerId=%s, status=%s, price=%s]", 
            orderId, productId, buyerId, status, price);
    }
}
