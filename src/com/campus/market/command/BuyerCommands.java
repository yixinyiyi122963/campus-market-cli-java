package com.campus.market.command;

import com.campus.market.auth.RequiresRole;
import com.campus.market.auth.SessionManager;
import com.campus.market.event.EventBus;
import com.campus.market.event.OrderStatusChangedEvent;
import com.campus.market.model.*;
import com.campus.market.repository.Repository;
import com.campus.market.service.DataStore;
import com.campus.market.util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * 买家命令处理器
 * 处理买家相关的所有命令
 */
public class BuyerCommands {
    private final Repository<Product> productRepo;
    private final Repository<Order> orderRepo;
    private final Repository<Review> reviewRepo;
    private final SessionManager sessionManager;
    private final EventBus eventBus;
    private final Scanner scanner;

    public BuyerCommands(Repository<Product> productRepo, Repository<Order> orderRepo,
                        Repository<Review> reviewRepo, Scanner scanner) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.reviewRepo = reviewRepo;
        this.sessionManager = SessionManager.getInstance();
        this.eventBus = EventBus.getInstance();
        this.scanner = scanner;
    }

    /**
     * 搜索商品
     * 支持按关键词、价格范围搜索
     */
    @Command(name = "search", description = "搜索商品 [关键词] [最低价] [最高价]")
    @RequiresRole({UserRole.BUYER, UserRole.SELLER, UserRole.ADMIN})
    public void searchProducts(String[] args) {
        String keyword = args.length > 0 ? args[0] : "";
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        try {
            if (args.length > 1) {
                minPrice = new BigDecimal(args[1]);
            }
            if (args.length > 2) {
                maxPrice = new BigDecimal(args[2]);
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ 价格格式错误");
            return;
        }

        // 使用Lambda表达式过滤商品
        final BigDecimal finalMinPrice = minPrice;
        final BigDecimal finalMaxPrice = maxPrice;
        
        List<Product> results = productRepo.findBy(p -> 
            p.getStatus() == ProductStatus.AVAILABLE &&
            (keyword.isEmpty() || 
             p.getName().contains(keyword) || 
             p.getDescription().contains(keyword) ||
             p.getCategory().contains(keyword)) &&
            (finalMinPrice == null || p.getPrice().compareTo(finalMinPrice) >= 0) &&
            (finalMaxPrice == null || p.getPrice().compareTo(finalMaxPrice) <= 0)
        );

        if (results.isEmpty()) {
            System.out.println("✓ 没有找到符合条件的商品");
            return;
        }

        System.out.println("\n=== 搜索结果 ===");
        System.out.println(String.format("%-15s %-20s %-15s %-10s %-15s", 
            "商品ID", "商品名称", "分类", "价格", "状态"));
        System.out.println("-".repeat(75));
        
        results.forEach(p -> {
            System.out.println(String.format("%-15s %-20s %-15s %-10s %-15s",
                p.getProductId(), 
                truncate(p.getName(), 20),
                truncate(p.getCategory(), 15),
                "¥" + p.getPrice(),
                p.getStatus().getDisplayName()));
        });
        
        System.out.println("\n找到 " + results.size() + " 件商品");
    }

    /**
     * 商品管理（买家视角）
     */
    @Command(name = "product", description = "商品管理")
    @RequiresRole({UserRole.BUYER, UserRole.SELLER, UserRole.ADMIN})
    public void productManagement(String[] args) {
        if (args.length == 0) {
            System.out.println("✗ 用法: product [detail] [商品ID]");
            return;
        }

        String action = args[0];
        if (action.equals("detail")) {
            productDetail(args);
        } else {
            System.out.println("✗ 未知操作: " + action);
        }
    }

    /**
     * 查看商品详情
     */
    private void productDetail(String[] args) {
        if (args.length < 2) {
            System.out.println("✗ 用法: product detail [商品ID]");
            return;
        }

        String productId = args[1];
        Optional<Product> productOpt = productRepo.findById(productId);
        
        if (!productOpt.isPresent()) {
            System.out.println("✗ 商品不存在");
            return;
        }

        Product product = productOpt.get();
        System.out.println("\n=== 商品详情 ===");
        System.out.println("商品ID: " + product.getProductId());
        System.out.println("商品名称: " + product.getName());
        System.out.println("商品描述: " + product.getDescription());
        System.out.println("商品分类: " + product.getCategory());
        System.out.println("商品价格: ¥" + product.getPrice());
        System.out.println("商品状态: " + product.getStatus().getDisplayName());
        System.out.println("卖家ID: " + product.getSellerId());
        System.out.println("发布时间: " + product.getCreatedAt());
        
        // 显示该商品的评价
        List<Review> reviews = reviewRepo.findBy(r -> r.getProductId().equals(productId));
        if (!reviews.isEmpty()) {
            System.out.println("\n=== 商品评价 ===");
            reviews.forEach(r -> {
                System.out.println("评分: " + "★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating()));
                System.out.println("评价: " + r.getComment());
                System.out.println("时间: " + r.getCreatedAt());
                System.out.println("-".repeat(50));
            });
        }
    }

    /**
     * 创建订单
     */
    @Command(name = "order", description = "订单管理")
    @RequiresRole({UserRole.BUYER, UserRole.SELLER, UserRole.ADMIN})
    public void orderManagement(String[] args) {
        if (args.length == 0) {
            System.out.println("✗ 用法: order [create|my|confirm-receive] ...");
            return;
        }

        String action = args[0];
        switch (action) {
            case "create":
                createOrder(args);
                break;
            case "my":
                viewMyOrders(args);
                break;
            case "confirm-receive":
                confirmReceive(args);
                break;
            default:
                System.out.println("✗ 未知操作: " + action);
        }
    }

    /**
     * 创建订单
     */
    private void createOrder(String[] args) {
        if (args.length < 2) {
            System.out.println("✗ 用法: order create [商品ID]");
            return;
        }

        String productId = args[1];
        Optional<Product> productOpt = productRepo.findById(productId);
        
        if (!productOpt.isPresent()) {
            System.out.println("✗ 商品不存在");
            return;
        }

        Product product = productOpt.get();
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            System.out.println("✗ 商品不可购买，当前状态: " + product.getStatus().getDisplayName());
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        if (product.getSellerId().equals(currentUser.getUserId())) {
            System.out.println("✗ 不能购买自己的商品");
            return;
        }

        // 创建订单
        String orderId = IdGenerator.generate("ORD");
        Order order = new Order(orderId, productId, currentUser.getUserId(), 
                               product.getSellerId(), product.getPrice());
        orderRepo.save(order);

        // 更新商品状态
        product.setStatus(ProductStatus.PENDING);
        productRepo.save(product);

        // 发布订单状态变更事件
        eventBus.publish(new OrderStatusChangedEvent(order, null, OrderStatus.PENDING_SHIP));

        System.out.println("✓ 订单创建成功！");
        System.out.println("订单ID: " + orderId);
        System.out.println("商品: " + product.getName());
        System.out.println("金额: ¥" + product.getPrice());
        System.out.println("请等待卖家发货");
    }

    /**
     * 查看我的订单
     */
    private void viewMyOrders(String[] args) {
        User currentUser = sessionManager.getCurrentUser();
        List<Order> orders = orderRepo.findBy(o -> o.getBuyerId().equals(currentUser.getUserId()));

        if (orders.isEmpty()) {
            System.out.println("✓ 您还没有订单");
            return;
        }

        System.out.println("\n=== 我的订单 ===");
        System.out.println(String.format("%-15s %-15s %-12s %-15s", 
            "订单ID", "商品ID", "金额", "状态"));
        System.out.println("-".repeat(60));
        
        orders.forEach(o -> {
            System.out.println(String.format("%-15s %-15s %-12s %-15s",
                o.getOrderId(),
                o.getProductId(),
                "¥" + o.getPrice(),
                o.getStatus().getDisplayName()));
        });
        
        System.out.println("\n共 " + orders.size() + " 个订单");
    }

    /**
     * 确认收货
     */
    private void confirmReceive(String[] args) {
        if (args.length < 2) {
            System.out.println("✗ 用法: order confirm-receive [订单ID]");
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
        
        if (!order.getBuyerId().equals(currentUser.getUserId())) {
            System.out.println("✗ 这不是您的订单");
            return;
        }

        if (order.getStatus() != OrderStatus.SHIPPED) {
            System.out.println("✗ 订单状态不正确，当前状态: " + order.getStatus().getDisplayName());
            return;
        }

        // 更新订单状态
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        order.setReceivedAt(LocalDateTime.now());
        orderRepo.save(order);

        // 更新商品状态
        Optional<Product> productOpt = productRepo.findById(order.getProductId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStatus(ProductStatus.SOLD);
            productRepo.save(product);
        }

        // 发布事件
        eventBus.publish(new OrderStatusChangedEvent(order, oldStatus, OrderStatus.COMPLETED));

        System.out.println("✓ 已确认收货！");
        System.out.println("您可以对此订单进行评价: review add " + orderId + " [评分1-5] [评价内容]");
    }

    /**
     * 添加评价
     */
    @Command(name = "review", description = "评价管理")
    @RequiresRole({UserRole.BUYER})
    public void reviewManagement(String[] args) {
        if (args.length == 0) {
            System.out.println("✗ 用法: review [add] ...");
            return;
        }

        String action = args[0];
        if (action.equals("add")) {
            addReview(args);
        } else {
            System.out.println("✗ 未知操作: " + action);
        }
    }

    /**
     * 添加评价
     */
    private void addReview(String[] args) {
        if (args.length < 4) {
            System.out.println("✗ 用法: review add [订单ID] [评分1-5] [评价内容]");
            return;
        }

        String orderId = args[1];
        int rating;
        try {
            rating = Integer.parseInt(args[2]);
            if (rating < 1 || rating > 5) {
                System.out.println("✗ 评分必须在1-5之间");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ 评分格式错误");
            return;
        }

        String comment = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (!orderOpt.isPresent()) {
            System.out.println("✗ 订单不存在");
            return;
        }

        Order order = orderOpt.get();
        User currentUser = sessionManager.getCurrentUser();
        
        if (!order.getBuyerId().equals(currentUser.getUserId())) {
            System.out.println("✗ 这不是您的订单");
            return;
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            System.out.println("✗ 只能评价已完成的订单");
            return;
        }

        // 检查是否已评价
        List<Review> existing = reviewRepo.findBy(r -> r.getOrderId().equals(orderId));
        if (!existing.isEmpty()) {
            System.out.println("✗ 该订单已评价");
            return;
        }

        // 创建评价
        String reviewId = IdGenerator.generate("REV");
        Review review = new Review(reviewId, orderId, order.getProductId(),
                                  currentUser.getUserId(), order.getSellerId(),
                                  rating, comment);
        reviewRepo.save(review);

        System.out.println("✓ 评价添加成功！");
        System.out.println("评分: " + "★".repeat(rating) + "☆".repeat(5 - rating));
        System.out.println("评价: " + comment);
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
