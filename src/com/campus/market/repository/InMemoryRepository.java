package com.campus.market.repository;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 内存仓储实现（泛型）
 * 使用ConcurrentHashMap在内存中存储数据，支持并发访问
 * 
 * @param <T> 实体类型，必须实现Serializable接口
 */
public class InMemoryRepository<T extends Serializable> implements Repository<T> {
    // 使用ConcurrentHashMap存储数据，键为实体ID
    private final Map<String, T> storage = new ConcurrentHashMap<>();
    // 用于从实体中提取ID的函数
    private final Function<T, String> idExtractor;

    /**
     * 构造函数
     * @param idExtractor 从实体中提取ID的函数
     */
    public InMemoryRepository(Function<T, String> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public void save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("实体不能为空");
        }
        String id = idExtractor.apply(entity);
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("实体ID不能为空");
        }
        storage.put(id, entity);
    }

    @Override
    public Optional<T> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        if (predicate == null) {
            return findAll();
        }
        // 使用Stream API和Lambda表达式进行过滤
        return storage.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public int count() {
        return storage.size();
    }

    /**
     * 获取内部存储映射（用于序列化）
     * @return 存储映射
     */
    public Map<String, T> getStorage() {
        return storage;
    }

    /**
     * 设置内部存储映射（用于反序列化）
     * @param data 存储映射
     */
    public void setStorage(Map<String, T> data) {
        storage.clear();
        if (data != null) {
            storage.putAll(data);
        }
    }
}
