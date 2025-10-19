package com.campus.market.command;

import com.campus.market.auth.RequiresRole;
import com.campus.market.model.*;
import com.campus.market.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * 管理员命令处理器
 * 处理管理员相关的所有命令
 */
public class AdminCommands {
    private final Repository<User> userRepo;
    private final Repository<Product> productRepo;
    private final Repository<Order> orderRepo;
    private final Repository<Review> reviewRepo;
    private final Scanner scanner;

    public AdminCommands(Repository<User> userRepo, Repository<Product> productRepo,
                        Repository<Order> orderRepo, Repository<Review> reviewRepo,
                        Scanner scanner) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.reviewRepo = reviewRepo;
        this.scanner = scanner;
    }

    /**
     * 用户管理
     */
    @Command(name = "user", description = "用户管理")
    @RequiresRole({UserRole.ADMIN})
    public void userManagement(String[] args) {
        if (args.length == 0) {
            System.out.println("✗ 用法: user [list|ban|unban] ...");
            return;
        }

        String action = args[0];
        switch (action) {
            case "list":
                listUsers(args);
                break;
            case "ban":
                banUser(args);
                break;
            case "unban":
                unbanUser(args);
                break;
            default:
                System.out.println("✗ 未知操作: " + action);
        }
    }

    /**
     * 列出所有用户
     */
    private void listUsers(String[] args) {
        List<User> users = userRepo.findAll();

        if (users.isEmpty()) {
            System.out.println("✓ 系统中没有用户");
            return;
        }

        System.out.println("\n=== 用户列表 ===");
        System.out.println(String.format("%-15s %-20s %-10s %-10s %-20s", 
            "用户ID", "用户名", "角色", "状态", "创建时间"));
        System.out.println("-".repeat(75));
        
        users.forEach(u -> {
            System.out.println(String.format("%-15s %-20s %-10s %-10s %-20s",
                u.getUserId(), 
                u.getUsername(),
                u.getRole().getDisplayName(),
                u.isBanned() ? "已封禁" : "正常",
                u.getCreatedAt().toString().substring(0, 19)));
        });
        
        System.out.println("\n共 " + users.size() + " 个用户");
    }

    /**
     * 封禁用户
     */
    private void banUser(String[] args) {
        if (args.length < 2) {
            System.out.println("✗ 用法: user ban [用户ID]");
            return;
        }

        String userId = args[1];
        Optional<User> userOpt = userRepo.findById(userId);
        
        if (!userOpt.isPresent()) {
            System.out.println("✗ 用户不存在");
            return;
        }

        User user = userOpt.get();
        
        if (user.getRole() == UserRole.ADMIN) {
            System.out.println("✗ 不能封禁管理员");
            return;
        }

        if (user.isBanned()) {
            System.out.println("✗ 用户已被封禁");
            return;
        }

        user.setBanned(true);
        userRepo.save(user);

        System.out.println("✓ 用户已封禁: " + user.getUsername());
    }

    /**
     * 解封用户
     */
    private void unbanUser(String[] args) {
        if (args.length < 2) {
            System.out.println("✗ 用法: user unban [用户ID]");
            return;
        }

        String userId = args[1];
        Optional<User> userOpt = userRepo.findById(userId);
        
        if (!userOpt.isPresent()) {
            System.out.println("✗ 用户不存在");
            return;
        }

        User user = userOpt.get();
        
        if (!user.isBanned()) {
            System.out.println("✗ 用户未被封禁");
            return;
        }

        user.setBanned(false);
        userRepo.save(user);

        System.out.println("✓ 用户已解封: " + user.getUsername());
    }

    /**
     * 商品管理
     */
    @Command(name = "product", description = "商品管理")
    @RequiresRole({UserRole.ADMIN})
    public void productManagement(String[] args) {
        if (args.length == 0 || !args[0].equals("all")) {
            System.out.println("✗ 用法: product all");
            return;
        }

        List<Product> products = productRepo.findAll();

        if (products.isEmpty()) {
            System.out.println("✓ 系统中没有商品");
            return;
        }

        System.out.println("\n=== 所有商品 ===");
        System.out.println(String.format("%-15s %-20s %-15s %-10s %-15s %-15s", 
            "商品ID", "商品名称", "分类", "价格", "卖家ID", "状态"));
        System.out.println("-".repeat(90));
        
        products.forEach(p -> {
            System.out.println(String.format("%-15s %-20s %-15s %-10s %-15s %-15s",
                p.getProductId(), 
                truncate(p.getName(), 20),
                truncate(p.getCategory(), 15),
                "¥" + p.getPrice(),
                p.getSellerId(),
                p.getStatus().getDisplayName()));
        });
        
        System.out.println("\n共 " + products.size() + " 件商品");
    }

    /**
     * 订单管理
     */
    @Command(name = "order", description = "订单管理")
    @RequiresRole({UserRole.ADMIN})
    public void orderManagement(String[] args) {
        if (args.length == 0 || !args[0].equals("all")) {
            System.out.println("✗ 用法: order all");
            return;
        }

        List<Order> orders = orderRepo.findAll();

        if (orders.isEmpty()) {
            System.out.println("✓ 系统中没有订单");
            return;
        }

        System.out.println("\n=== 所有订单 ===");
        System.out.println(String.format("%-15s %-15s %-15s %-15s %-12s %-15s", 
            "订单ID", "商品ID", "买家ID", "卖家ID", "金额", "状态"));
        System.out.println("-".repeat(90));
        
        orders.forEach(o -> {
            System.out.println(String.format("%-15s %-15s %-15s %-15s %-12s %-15s",
                o.getOrderId(),
                o.getProductId(),
                o.getBuyerId(),
                o.getSellerId(),
                "¥" + o.getPrice(),
                o.getStatus().getDisplayName()));
        });
        
        System.out.println("\n共 " + orders.size() + " 个订单");
    }

    /**
     * 评价管理
     */
    @Command(name = "review", description = "评价管理")
    @RequiresRole({UserRole.ADMIN})
    public void reviewManagement(String[] args) {
        if (args.length == 0 || !args[0].equals("all")) {
            System.out.println("✗ 用法: review all");
            return;
        }

        List<Review> reviews = reviewRepo.findAll();

        if (reviews.isEmpty()) {
            System.out.println("✓ 系统中没有评价");
            return;
        }

        System.out.println("\n=== 所有评价 ===");
        System.out.println(String.format("%-15s %-15s %-15s %-8s %-30s", 
            "评价ID", "订单ID", "买家ID", "评分", "评价内容"));
        System.out.println("-".repeat(90));
        
        reviews.forEach(r -> {
            String stars = "★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating());
            System.out.println(String.format("%-15s %-15s %-15s %-8s %-30s",
                r.getReviewId(),
                r.getOrderId(),
                r.getBuyerId(),
                stars,
                truncate(r.getComment(), 30)));
        });
        
        System.out.println("\n共 " + reviews.size() + " 条评价");
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }
}
