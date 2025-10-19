package com.campus.market.command;

import com.campus.market.auth.SessionManager;
import com.campus.market.model.User;
import com.campus.market.model.UserRole;
import com.campus.market.repository.Repository;
import com.campus.market.service.DataStore;
import com.campus.market.util.IdGenerator;
import com.campus.market.util.PasswordHasher;

import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * 系统命令处理器
 * 处理登录、登出、帮助等系统级命令
 */
public class SystemCommands {
    private final Repository<User> userRepo;
    private final SessionManager sessionManager;
    private final CommandRegistry commandRegistry;
    private final Scanner scanner;

    public SystemCommands(Repository<User> userRepo, CommandRegistry commandRegistry, 
                         Scanner scanner) {
        this.userRepo = userRepo;
        this.sessionManager = SessionManager.getInstance();
        this.commandRegistry = commandRegistry;
        this.scanner = scanner;
    }

    /**
     * 登录命令
     */
    @Command(name = "login", description = "用户登录")
    public void login(String[] args) {
        if (sessionManager.isLoggedIn()) {
            System.out.println("✗ 您已登录，请先登出");
            return;
        }

        System.out.print("用户名: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("密码: ");
        String password = scanner.nextLine().trim();

        // 查找用户
        Optional<User> userOpt = userRepo.findBy(u -> u.getUsername().equals(username))
                                         .stream().findFirst();
        
        if (!userOpt.isPresent()) {
            System.out.println("✗ 用户名或密码错误");
            return;
        }

        User user = userOpt.get();
        
        // 验证密码
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            System.out.println("✗ 用户名或密码错误");
            return;
        }

        // 检查是否被封禁
        if (user.isBanned()) {
            System.out.println("✗ 您的账号已被封禁，无法登录");
            return;
        }

        // 登录成功
        sessionManager.login(user);
        System.out.println("✓ 登录成功！");
        System.out.println("欢迎, " + user.getUsername() + " (" + user.getRole().getDisplayName() + ")");
        System.out.println("输入 'help' 查看可用命令");
    }

    /**
     * 注册命令
     */
    @Command(name = "register", description = "用户注册")
    public void register(String[] args) {
        if (sessionManager.isLoggedIn()) {
            System.out.println("✗ 请先登出再注册新用户");
            return;
        }

        System.out.println("\n=== 用户注册 ===");
        
        System.out.print("用户名: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("✗ 用户名不能为空");
            return;
        }

        // 检查用户名是否已存在
        Optional<User> existing = userRepo.findBy(u -> u.getUsername().equals(username))
                                          .stream().findFirst();
        if (existing.isPresent()) {
            System.out.println("✗ 用户名已存在");
            return;
        }

        System.out.print("密码: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("✗ 密码不能为空");
            return;
        }

        System.out.print("确认密码: ");
        String confirmPassword = scanner.nextLine().trim();
        if (!password.equals(confirmPassword)) {
            System.out.println("✗ 两次密码输入不一致");
            return;
        }

        System.out.println("选择角色:");
        System.out.println("1. 买家");
        System.out.println("2. 卖家");
        System.out.print("请选择 (1-2): ");
        String roleChoice = scanner.nextLine().trim();
        
        UserRole role;
        if (roleChoice.equals("1")) {
            role = UserRole.BUYER;
        } else if (roleChoice.equals("2")) {
            role = UserRole.SELLER;
        } else {
            System.out.println("✗ 无效选择");
            return;
        }

        // 创建用户
        String userId = IdGenerator.generate("USR");
        String passwordHash = PasswordHasher.hash(password);
        User user = new User(userId, username, passwordHash, role);
        
        System.out.print("邮箱 (可选): ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) {
            user.setEmail(email);
        }

        System.out.print("电话 (可选): ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) {
            user.setPhone(phone);
        }

        userRepo.save(user);

        System.out.println("✓ 注册成功！");
        System.out.println("用户ID: " + userId);
        System.out.println("请使用 'login' 命令登录");
    }

    /**
     * 登出命令
     */
    @Command(name = "logout", description = "退出登录")
    public void logout(String[] args) {
        if (!sessionManager.isLoggedIn()) {
            System.out.println("✗ 您还未登录");
            return;
        }

        String username = sessionManager.getCurrentUser().getUsername();
        sessionManager.logout();
        System.out.println("✓ 已退出登录，再见 " + username);
    }

    /**
     * 帮助命令
     */
    @Command(name = "help", description = "显示帮助信息")
    public void help(String[] args) {
        System.out.println("\n=== 可用命令 ===");
        
        Map<String, String> commands = commandRegistry.getAvailableCommands();
        
        if (commands.isEmpty()) {
            System.out.println("没有可用命令（请先登录）");
            return;
        }

        // 按类别分组显示
        System.out.println("\n通用命令:");
        commands.entrySet().stream()
            .filter(e -> isSystemCommand(e.getKey()))
            .forEach(e -> System.out.println("  " + padRight(e.getKey(), 20) + " - " + e.getValue()));

        if (sessionManager.isLoggedIn()) {
            User currentUser = sessionManager.getCurrentUser();
            
            if (currentUser.getRole() == UserRole.BUYER) {
                System.out.println("\n买家命令:");
                commands.entrySet().stream()
                    .filter(e -> isBuyerCommand(e.getKey()))
                    .forEach(e -> System.out.println("  " + padRight(e.getKey(), 20) + " - " + e.getValue()));
            }
            
            if (currentUser.getRole() == UserRole.SELLER) {
                System.out.println("\n卖家命令:");
                commands.entrySet().stream()
                    .filter(e -> isSellerCommand(e.getKey()))
                    .forEach(e -> System.out.println("  " + padRight(e.getKey(), 20) + " - " + e.getValue()));
            }
            
            if (currentUser.getRole() == UserRole.ADMIN) {
                System.out.println("\n管理员命令:");
                commands.entrySet().stream()
                    .filter(e -> isAdminCommand(e.getKey()))
                    .forEach(e -> System.out.println("  " + padRight(e.getKey(), 20) + " - " + e.getValue()));
            }
        }
    }

    /**
     * 保存数据命令
     */
    @Command(name = "save", description = "保存数据到文件")
    public void save(String[] args) {
        DataStore.getInstance().save();
    }

    /**
     * 加载数据命令
     */
    @Command(name = "load", description = "从文件加载数据")
    public void load(String[] args) {
        DataStore.getInstance().load();
    }

    /**
     * 退出程序命令
     */
    @Command(name = "exit", description = "退出程序")
    public void exit(String[] args) {
        System.out.print("是否保存数据? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        
        if (choice.equals("y") || choice.equals("yes")) {
            DataStore.getInstance().save();
        }
        
        System.out.println("再见！");
        System.exit(0);
    }

    /**
     * 判断是否为系统命令
     */
    private boolean isSystemCommand(String cmd) {
        return cmd.equals("help") || cmd.equals("login") || cmd.equals("register") || 
               cmd.equals("logout") || cmd.equals("save") || cmd.equals("load") || 
               cmd.equals("exit");
    }

    /**
     * 判断是否为买家命令
     */
    private boolean isBuyerCommand(String cmd) {
        return cmd.equals("search") || cmd.equals("product") || 
               cmd.equals("order") || cmd.equals("review");
    }

    /**
     * 判断是否为卖家命令
     */
    private boolean isSellerCommand(String cmd) {
        return cmd.equals("product") || cmd.equals("order");
    }

    /**
     * 判断是否为管理员命令
     */
    private boolean isAdminCommand(String cmd) {
        return cmd.equals("user") || cmd.equals("product") || 
               cmd.equals("order") || cmd.equals("review");
    }

    /**
     * 字符串右填充
     */
    private String padRight(String str, int length) {
        if (str.length() >= length) {
            return str;
        }
        return str + " ".repeat(length - str.length());
    }
}
