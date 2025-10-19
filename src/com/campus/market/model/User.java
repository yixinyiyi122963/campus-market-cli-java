package com.campus.market.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 表示系统中的用户，包括买家、卖家和管理员
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;           // 用户ID
    private String username;         // 用户名
    private String passwordHash;     // 密码哈希值
    private UserRole role;          // 用户角色
    private String email;           // 邮箱
    private String phone;           // 电话
    private boolean banned;         // 是否被封禁
    private LocalDateTime createdAt; // 创建时间

    public User() {
        this.createdAt = LocalDateTime.now();
        this.banned = false;
    }

    public User(String userId, String username, String passwordHash, UserRole role) {
        this();
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, username=%s, role=%s, banned=%s]", 
            userId, username, role, banned);
    }
}
