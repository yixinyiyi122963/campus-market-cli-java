package com.campus.market.command;

/**
 * 命令处理器接口（函数式接口）
 * 用于处理命令执行
 */
@FunctionalInterface
public interface CommandHandler {
    /**
     * 执行命令
     * @param args 命令参数
     */
    void execute(String[] args);
}
