package com.campus.market.command;

import com.campus.market.auth.RequiresRole;
import com.campus.market.auth.SessionManager;
import com.campus.market.model.User;
import com.campus.market.model.UserRole;

import java.lang.reflect.Method;
import java.util.*;


/**
 * 命令注册器（单例模式）
 * 使用命令模式管理所有命令的注册和执行
 * 支持Lambda表达式注册命令处理器
 */
public class CommandRegistry {
    private static CommandRegistry instance;
    // 命令名称到处理器的映射（支持多个处理器对应不同角色）
    private final Map<String, List<CommandInfo>> commands;
    private final SessionManager sessionManager;

    /**
     * 命令信息内部类
     * 存储命令处理器和所需角色信息
     */
    private static class CommandInfo {
        CommandHandler handler;
        UserRole[] requiredRoles;
        String description;

        CommandInfo(CommandHandler handler, UserRole[] requiredRoles, String description) {
            this.handler = handler;
            this.requiredRoles = requiredRoles;
            this.description = description;
        }
    }

    private CommandRegistry() {
        this.commands = new HashMap<>();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * 获取CommandRegistry单例实例
     * @return CommandRegistry实例
     */
    public static synchronized CommandRegistry getInstance() {
        if (instance == null) {
            instance = new CommandRegistry();
        }
        return instance;
    }

    /**
     * 注册命令（使用Lambda表达式）
     * @param name 命令名称
     * @param handler 命令处理器
     * @param description 命令描述
     * @param roles 所需角色
     */
    public void register(String name, CommandHandler handler, String description, UserRole... roles) {
        String key = name.toLowerCase();
        commands.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new CommandInfo(handler, roles, description));
    }

    /**
     * 从对象中注册带注解的方法作为命令
     * @param commandObject 包含命令方法的对象
     */
    public void registerFromObject(Object commandObject) {
        Method[] methods = commandObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(com.campus.market.command.Command.class)) {
                com.campus.market.command.Command cmdAnnotation = 
                    method.getAnnotation(com.campus.market.command.Command.class);
                RequiresRole roleAnnotation = method.getAnnotation(RequiresRole.class);
                
                UserRole[] roles = roleAnnotation != null ? roleAnnotation.value() : new UserRole[0];
                
                // 使用Lambda表达式创建命令处理器
                CommandHandler handler = args -> {
                    try {
                        method.setAccessible(true);
                        method.invoke(commandObject, (Object) args);
                    } catch (Exception e) {
                        Throwable cause = e.getCause();
                        if (cause != null) {
                            System.err.println("✗ 命令执行失败: " + cause.getMessage());
                        } else {
                            System.err.println("✗ 命令执行失败: " + e.getMessage());
                        }
                    }
                };
                
                register(cmdAnnotation.name(), handler, cmdAnnotation.description(), roles);
            }
        }
    }

    /**
     * 执行命令
     * @param input 用户输入的命令字符串
     */
    public void execute(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+");
        String commandName = parts[0].toLowerCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        List<CommandInfo> cmdInfoList = commands.get(commandName);
        if (cmdInfoList == null || cmdInfoList.isEmpty()) {
            System.out.println("✗ 未知命令: " + commandName + "，输入 'help' 查看可用命令");
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        
        // 查找最匹配的命令处理器
        CommandInfo selectedCmd = null;
        for (CommandInfo cmdInfo : cmdInfoList) {
            if (checkPermission(cmdInfo.requiredRoles)) {
                // 如果有多个匹配，优先选择角色最匹配的
                if (selectedCmd == null) {
                    selectedCmd = cmdInfo;
                } else if (currentUser != null && cmdInfo.requiredRoles != null) {
                    // 优先选择精确匹配当前用户角色的命令
                    for (UserRole role : cmdInfo.requiredRoles) {
                        if (role == currentUser.getRole()) {
                            selectedCmd = cmdInfo;
                            break;
                        }
                    }
                }
            }
        }

        if (selectedCmd == null) {
            System.out.println("✗ 权限不足");
            return;
        }

        // 执行命令
        try {
            selectedCmd.handler.execute(args);
        } catch (Exception e) {
            System.err.println("✗ 命令执行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查当前用户是否有权限执行命令
     * @param requiredRoles 所需角色
     * @return 是否有权限
     */
    private boolean checkPermission(UserRole[] requiredRoles) {
        if (requiredRoles == null || requiredRoles.length == 0) {
            return true; // 无角色限制
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            return false; // 未登录
        }

        if (currentUser.isBanned()) {
            System.out.println("✗ 您的账号已被封禁");
            return false;
        }

        // 检查用户角色是否在所需角色列表中
        for (UserRole role : requiredRoles) {
            if (currentUser.getRole() == role) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取所有可用命令列表
     * @return 命令列表
     */
    public Map<String, String> getAvailableCommands() {
        Map<String, String> available = new HashMap<>();
        User currentUser = sessionManager.getCurrentUser();
        
        for (Map.Entry<String, List<CommandInfo>> entry : commands.entrySet()) {
            for (CommandInfo info : entry.getValue()) {
                // 只显示用户有权限的命令
                if (info.requiredRoles == null || info.requiredRoles.length == 0) {
                    available.put(entry.getKey(), info.description);
                    break; // 只添加一次
                } else if (currentUser != null && !currentUser.isBanned()) {
                    for (UserRole role : info.requiredRoles) {
                        if (currentUser.getRole() == role) {
                            available.put(entry.getKey(), info.description);
                            break;
                        }
                    }
                }
            }
        }
        
        return available;
    }
}
