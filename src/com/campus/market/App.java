package com.campus.market;

import com.campus.market.auth.SessionManager;
import com.campus.market.command.*;
import com.campus.market.event.EventBus;
import com.campus.market.event.OrderStatusChangedEvent;
import com.campus.market.model.*;
import com.campus.market.repository.InMemoryRepository;
import com.campus.market.repository.Repository;
import com.campus.market.service.DataStore;
import com.campus.market.util.IdGenerator;
import com.campus.market.util.PasswordHasher;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * 校园二手交易系统主应用类
 * 
 * 系统特性：
 * 1. 无数据库，使用内存存储 + 文件序列化实现持久化
 * 2. 三种用户角色：买家、卖家、管理员，具有不同权限
 * 3. 完整的交易流程：发布 → 查询 → 下单 → 发货 → 收货 → 评价
 * 4. 支持用户管理、商品管理、订单管理、评价管理
 * 
 * 设计模式和特性应用：
 * - 仓储模式（Repository Pattern）：抽象数据访问层
 * - 命令模式（Command Pattern）：命令注册和执行
 * - 观察者模式（Observer Pattern）：事件总线和监听器
 * - 单例模式（Singleton Pattern）：SessionManager、DataStore、EventBus
 * - 泛型（Generics）：Repository<T>、InMemoryRepository<T>
 * - Lambda表达式：命令处理器、事件监听器、数据过滤
 * - 注解（Annotations）：@Command、@RequiresRole用于权限控制
 * - 函数式接口：CommandHandler、EventListener
 * - 内部类：CommandRegistry.CommandInfo、DataStore.DataSnapshot
 * - Stream API：数据过滤和处理
 */
public class App {
    // 仓储实例
    private static Repository<User> userRepo;
    private static Repository<Product> productRepo;
    private static Repository<Order> orderRepo;
    private static Repository<Review> reviewRepo;

    // 管理器实例
    private static CommandRegistry commandRegistry;
    private static SessionManager sessionManager;
    private static EventBus eventBus;
    
    // 扫描器
    private static Scanner scanner;

    /**
     * 程序入口
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   校园二手交易系统 - 命令行版");
        System.out.println("========================================");
        System.out.println();

        // 初始化系统
        initialize();

        // 显示欢迎信息
        showWelcomeMessage();

        // 主循环
        mainLoop();
    }

    /**
     * 初始化系统
     */
    private static void initialize() {
        // 初始化扫描器
        scanner = new Scanner(System.in);

        // 初始化仓储（使用泛型和Lambda表达式）
        userRepo = new InMemoryRepository<>(User::getUserId);
        productRepo = new InMemoryRepository<>(Product::getProductId);
        orderRepo = new InMemoryRepository<>(Order::getOrderId);
        reviewRepo = new InMemoryRepository<>(Review::getReviewId);

        // 初始化管理器
        sessionManager = SessionManager.getInstance();
        commandRegistry = CommandRegistry.getInstance();
        eventBus = EventBus.getInstance();

        // 初始化数据存储
        DataStore.initialize(userRepo, productRepo, orderRepo, reviewRepo);

        // 尝试从文件加载数据
        boolean loaded = DataStore.getInstance().load();
        
        // 如果没有加载到数据，初始化默认数据
        if (!loaded || userRepo.count() == 0) {
            initializeDefaultData();
        }

        // 注册事件监听器（使用Lambda表达式）
        registerEventListeners();

        // 注册命令
        registerCommands();

        System.out.println("✓ 系统初始化完成\n");
    }

    /**
     * 初始化默认数据
     */
    private static void initializeDefaultData() {
        System.out.println("正在初始化默认数据...");

        // 创建默认管理员账号
        String adminId = IdGenerator.generate("USR");
        User admin = new User(adminId, "admin", PasswordHasher.hash("123456"), UserRole.ADMIN);
        admin.setEmail("admin@campus.edu");
        userRepo.save(admin);

        // 创建默认买家账号
        String buyer1Id = IdGenerator.generate("USR");
        User buyer1 = new User(buyer1Id, "buyer1", PasswordHasher.hash("123456"), UserRole.BUYER);
        buyer1.setEmail("buyer1@campus.edu");
        userRepo.save(buyer1);

        // 创建默认卖家账号
        String seller1Id = IdGenerator.generate("USR");
        User seller1 = new User(seller1Id, "seller1", PasswordHasher.hash("123456"), UserRole.SELLER);
        seller1.setEmail("seller1@campus.edu");
        userRepo.save(seller1);

        // 创建示例商品
        String product1Id = IdGenerator.generate("PRD");
        Product product1 = new Product(product1Id, "二手教材 - Java编程", 
            "计算机专业教材，9成新，无笔记", 
            new BigDecimal("50.00"), "图书", seller1Id);
        productRepo.save(product1);

        String product2Id = IdGenerator.generate("PRD");
        Product product2 = new Product(product2Id, "自行车", 
            "校园代步自行车，骑行2年，车况良好", 
            new BigDecimal("200.00"), "交通工具", seller1Id);
        productRepo.save(product2);

        String product3Id = IdGenerator.generate("PRD");
        Product product3 = new Product(product3Id, "台灯", 
            "护眼台灯，LED光源，可调亮度", 
            new BigDecimal("80.00"), "生活用品", seller1Id);
        productRepo.save(product3);

        System.out.println("✓ 默认数据初始化完成");
        System.out.println("  管理员账号: admin / 123456");
        System.out.println("  买家账号: buyer1 / 123456");
        System.out.println("  卖家账号: seller1 / 123456");
    }

    /**
     * 注册事件监听器
     */
    private static void registerEventListeners() {
        // 监听订单状态变更事件，使用Lambda表达式
        eventBus.subscribe(OrderStatusChangedEvent.class, event -> {
            // 这里可以添加更多的事件处理逻辑
            // 例如：通知用户、记录日志、更新统计数据等
            System.out.println("📢 订单状态已更新: " + event.getOrder().getOrderId() + 
                             " -> " + event.getNewStatus().getDisplayName());
        });
    }

    /**
     * 注册命令
     */
    private static void registerCommands() {
        // 创建命令处理器实例
        SystemCommands systemCommands = new SystemCommands(userRepo, commandRegistry, scanner);
        AdminCommands adminCommands = new AdminCommands(userRepo, productRepo, orderRepo, reviewRepo, scanner);
        SellerCommands sellerCommands = new SellerCommands(productRepo, orderRepo, userRepo, scanner);
        BuyerCommands buyerCommands = new BuyerCommands(productRepo, orderRepo, reviewRepo, scanner);

        // 先注册系统命令
        commandRegistry.registerFromObject(systemCommands);
        
        // 然后注册角色相关命令（后注册的会覆盖先注册的）
        commandRegistry.registerFromObject(buyerCommands);
        commandRegistry.registerFromObject(sellerCommands);
        commandRegistry.registerFromObject(adminCommands);
    }

    /**
     * 显示欢迎信息
     */
    private static void showWelcomeMessage() {
        System.out.println("欢迎使用校园二手交易系统！");
        System.out.println();
        System.out.println("系统功能：");
        System.out.println("  • 用户注册与登录");
        System.out.println("  • 商品发布与搜索");
        System.out.println("  • 在线下单购买");
        System.out.println("  • 订单管理与追踪");
        System.out.println("  • 交易评价系统");
        System.out.println("  • 管理员后台管理");
        System.out.println();
        System.out.println("输入 'help' 查看所有可用命令");
        System.out.println("输入 'login' 登录现有账号");
        System.out.println("输入 'register' 注册新账号");
        System.out.println("输入 'exit' 退出系统");
        System.out.println();
    }

    /**
     * 主循环
     */
    private static void mainLoop() {
        while (true) {
            // 显示提示符
            if (sessionManager.isLoggedIn()) {
                User currentUser = sessionManager.getCurrentUser();
                System.out.print("[" + currentUser.getUsername() + "@" + 
                               currentUser.getRole().getDisplayName() + "] > ");
            } else {
                System.out.print("[游客] > ");
            }

            // 读取用户输入
            String input = scanner.nextLine().trim();

            // 跳过空输入
            if (input.isEmpty()) {
                continue;
            }

            // 执行命令
            try {
                commandRegistry.execute(input);
            } catch (Exception e) {
                System.err.println("✗ 命令执行出错: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println(); // 空行分隔
        }
    }
}
