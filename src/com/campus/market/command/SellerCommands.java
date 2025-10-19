package com.campus.market.command;

import com.campus.market.auth.RequiresRole;
import com.campus.market.auth.SessionManager;
import com.campus.market.event.EventBus;
import com.campus.market.event.OrderStatusChangedEvent;
import com.campus.market.model.*;
import com.campus.market.repository.Repository;
import com.campus.market.util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * 卖家命令处理器
 * 处理卖家相关的所有命令
 */
public class SellerCommands {
    private final Repository<Product> productRepo;
    private final Repository<Order> orderRepo;
    private final Repository<User> userRepo;
    private final SessionManager sessionManager;
    private final EventBus eventBus;
    private final Scanner scanner;

    public SellerCommands(Repository<Product> productRepo, Repository<Order> orderRepo,
                         Repository<User> userRepo, Scanner scanner) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.sessionManager = SessionManager.getInstance();
        this.eventBus = EventBus.getInstance();
        this.scanner = scanner;
    }

    /**
     * 商品管理
     */
    @Command(name = "product", description = "商品管理")
    @RequiresRole({UserRole.SELLER})
    public void productManagement(String[] args) {
        if (args.length == 0) {
            System.out.println("✗ 用法: product [add|my|edit|remove] ...");
            return;
        }

        String action = args[0];
        switch (action) {
            case "add":
                addProduct(args);
                break;
            case "my":
                viewMyProducts(args);
                break;
            case "edit":
                editProduct(args);
                break;
            case "remove":
                removeProduct(args);
                break;
            default:
                System.out.println("✗ 未知操作: " + action);
        }
    }

    /**
     * 添加商品
     */
    private void addProduct(String[] args) {
        System.out.println("\n=== 发布新商品 ===");
        
        System.out.print("商品名称: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("✗ 商品名称不能为空");
            return;
        }

        System.out.print("商品描述: ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            System.out.println("✗ 商品描述不能为空");
            return;
        }

        System.out.print("商品分类: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) {
            System.out.println("✗ 商品分类不能为空");
            return;
        }

        System.out.print("商品价格: ");
        String priceStr = scanner.nextLine().trim();
        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("✗ 价格必须大于0");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ 价格格式错误");
            return;
        }

        // 创建商品
        User currentUser = sessionManager.getCurrentUser();
        String productId = IdGenerator.generate("PRD");
        Product product = new Product(productId, name, description, price, category, 
                                     currentUser.getUserId());
        productRepo.save(product);

        System.out.println("✓ 商品发布成功！");
        System.out.println("商品ID: " + productId);
        System.out.println("商品名称: " + name);
        System.out.println("商品价格: ¥" + price);
    }

    /**
     * 查看我发布的商品
     */
    private void viewMyProducts(String[] args) {
        User currentUser = sessionManager.getCurrentUser();
        List<Product> products = productRepo.findBy(p -> 
            p.getSellerId().equals(currentUser.getUserId()) && 
            p.getStatus() != ProductStatus.REMOVED
        );

        if (products.isEmpty()) {
            System.out.println("✓ 您还没有发布商品");
            return;
        }

        System.out.println("\n=== 我的商品 ===");
        System.out.println(String.format("%-15s %-20s %-15s %-10s %-15s", 
            "商品ID", "商品名称", "分类", "价格", "状态"));
        System.out.println("-".repeat(75));
        
        products.forEach(p -> {
            System.out.println(String.format("%-15s %-20s %-15s %-10s %-15s",
                p.getProductId(), 
                truncate(p.getName(), 20),
                truncate(p.getCategory(), 15),
                "¥" + p.getPrice(),
                p.getStatus().getDisplayName()));
        });
        
        System.out.println("\n共 " + products.size() + " 件商品");
    }

    /**
     * 编辑商品
     */
    private void editProduct(String[] args) {
        if (args.length < 2) {
            System.out.println("✗ 用法: product edit [商品ID]");
            return;
        }

        String productId = args[1];
        Optional<Product> productOpt = productRepo.findById(productId);
        
        if (!productOpt.isPresent()) {
            System.out.println("✗ 商品不存在");
            return;
        }

        Product product = productOpt.get();
        User currentUser = sessionManager.getCurrentUser();
        
        if (!product.getSellerId().equals(currentUser.getUserId())) {
            System.out.println("✗ 您不能编辑别人的商品");
            return;
        }

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            System.out.println("✗ 只能编辑在售商品");
            return;
        }

        System.out.println("\n=== 编辑商品 ===");
        System.out.println("留空表示不修改");
        
        System.out.print("商品名称 [" + product.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            product.setName(name);
        }

        System.out.print("商品描述 [" + product.getDescription() + "]: ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) {
            product.setDescription(description);
        }

        System.out.print("商品分类 [" + product.getCategory() + "]: ");
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) {
            product.setCategory(category);
        }

        System.out.print("商品价格 [" + product.getPrice() + "]: ");
        String priceStr = scanner.nextLine().trim();
        if (!priceStr.isEmpty()) {
            try {
                BigDecimal price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("✗ 价格必须大于0");
                    return;
                }
                product.setPrice(price);
            } catch (NumberFormatException e) {
                System.out.println("✗ 价格格式错误");
                return;
            }
        }

        product.setUpdatedAt(LocalDateTime.now());
        productRepo.save(product);

        System.out.println("✓ 商品更新成功！");
    }

    /**
     * 下架商品
     */
    private void removeProduct(String[] args) {
        if (args.length < 2) {
            System.out.println("✗ 用法: product remove [商品ID]");
            return;
        }

        String productId = args[1];
        Optional<Product> productOpt = productRepo.findById(productId);
        
        if (!productOpt.isPresent()) {
            System.out.println("✗ 商品不存在");
            return;
        }

        Product product = productOpt.get();
        User currentUser = sessionManager.getCurrentUser();
        
        if (!product.getSellerId().equals(currentUser.getUserId())) {
            System.out.println("✗ 您不能下架别人的商品");
            return;
        }

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            System.out.println("✗ 只能下架在售商品");
            return;
        }

        product.setStatus(ProductStatus.REMOVED);
        productRepo.save(product);

        System.out.println("✓ 商品已下架");
    }

    /**
     * 订单管理
     */
    @Command(name = "order", description = "订单管理")
    @RequiresRole({UserRole.SELLER})
    public void orderManagement(String[] args) {
        if (args.length == 0) {
            System.out.println("✗ 用法: order [for-me|confirm-ship] ...");
            return;
        }

        String action = args[0];
        switch (action) {
            case "for-me":
                viewOrdersForMe(args);
                break;
            case "confirm-ship":
                confirmShip(args);
                break;
            default:
                System.out.println("✗ 未知操作: " + action);
        }
    }

    /**
     * 查看买家对我商品的订单
     */
    private void viewOrdersForMe(String[] args) {
        User currentUser = sessionManager.getCurrentUser();
        List<Order> orders = orderRepo.findBy(o -> 
            o.getSellerId().equals(currentUser.getUserId())
        );

        if (orders.isEmpty()) {
            System.out.println("✓ 暂无订单");
            return;
        }

        System.out.println("\n=== 我的销售订单 ===");
        System.out.println(String.format("%-15s %-15s %-15s %-12s %-15s", 
            "订单ID", "商品ID", "买家ID", "金额", "状态"));
        System.out.println("-".repeat(75));
        
        orders.forEach(o -> {
            System.out.println(String.format("%-15s %-15s %-15s %-12s %-15s",
                o.getOrderId(),
                o.getProductId(),
                o.getBuyerId(),
                "¥" + o.getPrice(),
                o.getStatus().getDisplayName()));
        });
        
        System.out.println("\n共 " + orders.size() + " 个订单");
    }

    /**
     * 确认发货
     */
    private void confirmShip(String[] args) {
        if (args.length < 2) {
            System.out.println("✗ 用法: order confirm-ship [订单ID]");
            return;
        }

        String orderId = args[1];
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        
        if (!orderOpt.isPresent()) {
            System.out.println("✗ 订单不存在");
            return;
        }

        Order order = orderOpt.get();
        User currentUser = sessionManager.getCurrentUser();
        
        if (!order.getSellerId().equals(currentUser.getUserId())) {
            System.out.println("✗ 这不是您的订单");
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING_SHIP) {
            System.out.println("✗ 订单状态不正确，当前状态: " + order.getStatus().getDisplayName());
            return;
        }

        // 更新订单状态
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        orderRepo.save(order);

        // 发布事件
        eventBus.publish(new OrderStatusChangedEvent(order, oldStatus, OrderStatus.SHIPPED));

        System.out.println("✓ 已确认发货！");
        System.out.println("请等待买家确认收货");
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
