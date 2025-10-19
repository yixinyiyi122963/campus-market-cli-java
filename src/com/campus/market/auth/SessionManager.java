package com.campus.market.auth;

import com.campus.market.model.User;

/**
 * 会话管理类（单例模式）
 * 管理当前登录用户的会话信息
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
    }

    /**
     * 获取SessionManager单例实例
     * @return SessionManager实例
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * 登录用户
     * @param user 用户对象
     */
    public void login(User user) {
        this.currentUser = user;
    }

    /**
     * 登出用户
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * 获取当前登录用户
     * @return 当前用户，如果未登录则返回null
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 检查是否已登录
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * 检查当前用户是否被封禁
     * @return 是否被封禁
     */
    public boolean isCurrentUserBanned() {
        return currentUser != null && currentUser.isBanned();
    }
}
