package com.campus.market.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 仓储接口（泛型）
 * 定义数据访问层的通用操作
 * 
 * @param <T> 实体类型
 */
public interface Repository<T> {
    /**
     * 保存或更新实体
     * @param entity 实体对象
     */
    void save(T entity);

    /**
     * 根据ID查找实体
     * @param id 实体ID
     * @return Optional包装的实体对象
     */
    Optional<T> findById(String id);

    /**
     * 查找所有实体
     * @return 所有实体列表
     */
    List<T> findAll();

    /**
     * 根据条件查找实体
     * @param predicate 筛选条件
     * @return 符合条件的实体列表
     */
    List<T> findBy(Predicate<T> predicate);

    /**
     * 删除实体
     * @param id 实体ID
     * @return 是否删除成功
     */
    boolean delete(String id);

    /**
     * 清空所有实体
     */
    void clear();

    /**
     * 获取实体总数
     * @return 实体数量
     */
    int count();
}
