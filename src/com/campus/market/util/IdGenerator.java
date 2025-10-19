package com.campus.market.util;

import java.util.UUID;

/**
 * ID生成器工具类
 * 生成唯一的ID用于各类实体
 */
public class IdGenerator {
    
    /**
     * 生成UUID字符串
     * @return UUID字符串
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成带前缀的ID
     * @param prefix ID前缀
     * @return 带前缀的ID
     */
    public static String generate(String prefix) {
        return prefix + "-" + generate().substring(0, 8);
    }
}
