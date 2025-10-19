package com.campus.market.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体类
 * 表示买家对已完成订单的评价
 */
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reviewId;         // 评价ID
    private String orderId;          // 订单ID
    private String productId;        // 商品ID
    private String buyerId;          // 买家ID（评价者）
    private String sellerId;         // 卖家ID（被评价者）
    private int rating;              // 评分（1-5星）
    private String comment;          // 评价内容
    private LocalDateTime createdAt; // 创建时间

    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    public Review(String reviewId, String orderId, String productId, 
                 String buyerId, String sellerId, int rating, String comment) {
        this();
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Review[id=%s, orderId=%s, rating=%d, comment=%s]", 
            reviewId, orderId, rating, comment);
    }
}
