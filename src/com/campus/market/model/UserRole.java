package com.campus.market.model;

/**
 * 用户角色枚举
 * 定义系统中的三种用户角色：管理员、买家、卖家
 */
public enum UserRole {
    ADMIN("管理员"),      // 管理员角色
    BUYER("买家"),        // 买家角色
    SELLER("卖家");       // 卖家角色

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
