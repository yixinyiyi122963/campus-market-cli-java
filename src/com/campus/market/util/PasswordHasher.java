package com.campus.market.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码哈希工具类
 * 使用SHA-256算法对密码进行哈希处理
 */
public class PasswordHasher {
    
    /**
     * 对密码进行SHA-256哈希
     * @param password 明文密码
     * @return 哈希后的密码（十六进制字符串）
     */
    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * 验证密码是否匹配
     * @param password 明文密码
     * @param hash 存储的哈希值
     * @return 密码是否匹配
     */
    public static boolean verify(String password, String hash) {
        return hash(password).equals(hash);
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
