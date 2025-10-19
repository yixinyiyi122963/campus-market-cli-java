package com.campus.market.service;

import com.campus.market.model.*;
import com.campus.market.repository.InMemoryRepository;
import com.campus.market.repository.Repository;

import java.io.*;
import java.util.Map;

/**
 * 数据存储服务（单例模式）
 * 负责数据的持久化和恢复
 * 使用Java原生序列化机制
 */
public class DataStore {
    private static DataStore instance;
    private static final String DATA_FILE = "campus-market-data.ser";

    private final Repository<User> userRepo;
    private final Repository<Product> productRepo;
    private final Repository<Order> orderRepo;
    private final Repository<Review> reviewRepo;

    private DataStore(Repository<User> userRepo, Repository<Product> productRepo,
                     Repository<Order> orderRepo, Repository<Review> reviewRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.reviewRepo = reviewRepo;
    }

    /**
     * 初始化DataStore单例
     * @param userRepo 用户仓储
     * @param productRepo 商品仓储
     * @param orderRepo 订单仓储
     * @param reviewRepo 评价仓储
     */
    public static void initialize(Repository<User> userRepo, Repository<Product> productRepo,
                                  Repository<Order> orderRepo, Repository<Review> reviewRepo) {
        instance = new DataStore(userRepo, productRepo, orderRepo, reviewRepo);
    }

    /**
     * 获取DataStore实例
     * @return DataStore实例
     */
    public static DataStore getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DataStore未初始化");
        }
        return instance;
    }

    /**
     * 保存所有数据到文件
     * @return 是否保存成功
     */
    public boolean save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            
            // 保存各个仓储的数据
            DataSnapshot snapshot = new DataSnapshot();
            
            if (userRepo instanceof InMemoryRepository) {
                snapshot.users = ((InMemoryRepository<User>) userRepo).getStorage();
            }
            if (productRepo instanceof InMemoryRepository) {
                snapshot.products = ((InMemoryRepository<Product>) productRepo).getStorage();
            }
            if (orderRepo instanceof InMemoryRepository) {
                snapshot.orders = ((InMemoryRepository<Order>) orderRepo).getStorage();
            }
            if (reviewRepo instanceof InMemoryRepository) {
                snapshot.reviews = ((InMemoryRepository<Review>) reviewRepo).getStorage();
            }
            
            oos.writeObject(snapshot);
            System.out.println("✓ 数据已保存到文件: " + DATA_FILE);
            return true;
        } catch (IOException e) {
            System.err.println("✗ 保存数据失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从文件加载数据
     * @return 是否加载成功
     */
    public boolean load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("✓ 数据文件不存在，使用默认数据");
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {
            
            DataSnapshot snapshot = (DataSnapshot) ois.readObject();
            
            // 恢复各个仓储的数据
            if (userRepo instanceof InMemoryRepository && snapshot.users != null) {
                ((InMemoryRepository<User>) userRepo).setStorage(snapshot.users);
            }
            if (productRepo instanceof InMemoryRepository && snapshot.products != null) {
                ((InMemoryRepository<Product>) productRepo).setStorage(snapshot.products);
            }
            if (orderRepo instanceof InMemoryRepository && snapshot.orders != null) {
                ((InMemoryRepository<Order>) orderRepo).setStorage(snapshot.orders);
            }
            if (reviewRepo instanceof InMemoryRepository && snapshot.reviews != null) {
                ((InMemoryRepository<Review>) reviewRepo).setStorage(snapshot.reviews);
            }
            
            System.out.println("✓ 数据已从文件加载: " + DATA_FILE);
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("✗ 加载数据失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 数据快照内部类
     * 用于序列化存储所有仓储数据
     */
    private static class DataSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<String, User> users;
        Map<String, Product> products;
        Map<String, Order> orders;
        Map<String, Review> reviews;
    }
}
