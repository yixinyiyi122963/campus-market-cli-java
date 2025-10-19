# 校园二手交易系统 - 设计文档

## 系统概述

校园二手交易系统是一个轻量级的命令行交易平台，专为校园师生设计。系统采用纯Java实现，无需数据库，使用内存存储配合文件序列化实现数据持久化。

## 核心功能

### 1. 用户管理
- **注册与登录**：支持用户注册（买家/卖家）和登录认证
- **角色管理**：三种角色（管理员、买家、卖家）具有不同权限
- **账号管理**：管理员可以封禁/解封用户

### 2. 商品管理
- **商品发布**：卖家可以发布二手商品（名称、描述、分类、价格）
- **商品编辑**：卖家可以编辑自己发布的商品
- **商品下架**：卖家可以下架自己的商品
- **商品搜索**：支持按关键词、价格范围搜索
- **商品详情**：查看商品详细信息和评价

### 3. 订单管理
- **创建订单**：买家对在售商品下单
- **订单追踪**：查看订单状态（待发货、已发货、已完成）
- **确认发货**：卖家确认并发货
- **确认收货**：买家确认收货

### 4. 评价管理
- **添加评价**：买家对已完成订单进行评价（1-5星 + 评论）
- **查看评价**：在商品详情中查看评价
- **评价管理**：管理员可以查看所有评价

## 设计模式与特性

### 1. 仓储模式（Repository Pattern）

**目的**：抽象数据访问层，将数据操作与业务逻辑分离

**实现**：
```java
// 泛型接口定义
public interface Repository<T> {
    void save(T entity);
    Optional<T> findById(String id);
    List<T> findAll();
    List<T> findBy(Predicate<T> predicate);
    boolean delete(String id);
}

// 内存实现
public class InMemoryRepository<T extends Serializable> implements Repository<T> {
    private final Map<String, T> storage = new ConcurrentHashMap<>();
    // ... 实现细节
}
```

**优点**：
- 数据访问逻辑集中管理
- 支持泛型，可复用于不同实体
- 便于测试和替换存储方案

### 2. 命令模式（Command Pattern）

**目的**：将请求封装为对象，实现命令的参数化和解耦

**实现**：
```java
@FunctionalInterface
public interface CommandHandler {
    void execute(String[] args);
}

public class CommandRegistry {
    private final Map<String, List<CommandInfo>> commands;
    
    public void register(String name, CommandHandler handler, 
                        String description, UserRole... roles) {
        // 注册命令
    }
    
    public void execute(String input) {
        // 解析并执行命令
    }
}
```

**优点**：
- 命令注册和执行解耦
- 支持Lambda表达式简化命令定义
- 易于扩展新命令

### 3. 观察者模式（Observer Pattern）

**目的**：定义对象间的一对多依赖关系，当一个对象状态改变时，所有依赖者都会收到通知

**实现**：
```java
public class EventBus {
    private final Map<Class<? extends Event>, 
                      List<EventListener<? extends Event>>> listeners;
    
    public <T extends Event> void subscribe(Class<T> eventType, 
                                           EventListener<T> listener) {
        // 订阅事件
    }
    
    public <T extends Event> void publish(T event) {
        // 发布事件
    }
}
```

**应用场景**：
- 订单状态变更时更新商品状态
- 系统事件的解耦处理

### 4. 单例模式（Singleton Pattern）

**目的**：确保一个类只有一个实例，并提供全局访问点

**实现类**：
- `SessionManager`：管理当前登录会话
- `EventBus`：全局事件总线
- `DataStore`：数据持久化管理
- `CommandRegistry`：命令注册器

```java
public class SessionManager {
    private static SessionManager instance;
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
}
```

### 5. 泛型（Generics）

**应用**：
```java
// 仓储接口和实现
Repository<User> userRepo;
Repository<Product> productRepo;
InMemoryRepository<Order> orderRepo;

// 事件监听器
EventListener<OrderStatusChangedEvent> listener;
```

**优点**：
- 类型安全
- 代码复用
- 编译时类型检查

### 6. Lambda表达式

**应用场景**：

1. **命令处理器注册**：
```java
CommandHandler handler = args -> {
    // 命令处理逻辑
};
```

2. **事件监听器**：
```java
eventBus.subscribe(OrderStatusChangedEvent.class, event -> {
    System.out.println("订单状态已更新: " + event.getNewStatus());
});
```

3. **数据过滤**：
```java
List<Product> results = productRepo.findBy(p -> 
    p.getStatus() == ProductStatus.AVAILABLE &&
    p.getName().contains(keyword)
);
```

### 7. 注解（Annotations）

**自定义注解**：

1. **命令注解**：
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String name();
    String description() default "";
}
```

2. **角色权限注解**：
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresRole {
    UserRole[] value();
}
```

**使用示例**：
```java
@Command(name = "search", description = "搜索商品")
@RequiresRole({UserRole.BUYER, UserRole.SELLER})
public void searchProducts(String[] args) {
    // 搜索逻辑
}
```

### 8. 函数式接口

**定义**：
```java
@FunctionalInterface
public interface CommandHandler {
    void execute(String[] args);
}

@FunctionalInterface
public interface EventListener<T extends Event> {
    void onEvent(T event);
}
```

**优点**：
- 支持Lambda表达式
- 代码简洁
- 函数式编程风格

### 9. 内部类

**应用**：

1. **命令信息封装**：
```java
private static class CommandInfo {
    CommandHandler handler;
    UserRole[] requiredRoles;
    String description;
}
```

2. **数据快照**：
```java
private static class DataSnapshot implements Serializable {
    Map<String, User> users;
    Map<String, Product> products;
    Map<String, Order> orders;
    Map<String, Review> reviews;
}
```

### 10. Stream API

**应用场景**：
```java
// 数据过滤
List<Product> results = productRepo.findBy(predicate);

// 查找特定元素
Optional<User> user = userRepo.findBy(u -> u.getUsername().equals(username))
                              .stream().findFirst();

// 遍历处理
products.forEach(p -> System.out.println(p.getName()));
```

## 实体关系

```
User (用户)
  ├─ ADMIN (管理员)
  ├─ BUYER (买家)
  └─ SELLER (卖家)

Product (商品)
  ├─ belongsTo: User (Seller)
  └─ hasMany: Review

Order (订单)
  ├─ belongsTo: User (Buyer)
  ├─ belongsTo: User (Seller)
  └─ belongsTo: Product

Review (评价)
  ├─ belongsTo: User (Buyer)
  ├─ belongsTo: Order
  └─ belongsTo: Product
```

## 交易流程

```
1. 卖家发布商品 (AVAILABLE)
   ↓
2. 买家搜索并查看商品
   ↓
3. 买家创建订单 (PENDING_SHIP)
   → 商品状态变更为 PENDING
   ↓
4. 卖家确认发货 (SHIPPED)
   ↓
5. 买家确认收货 (COMPLETED)
   → 商品状态变更为 SOLD
   ↓
6. 买家添加评价 (1-5星 + 评论)
```

## 权限控制

### 管理员 (ADMIN)
- 查看所有用户
- 封禁/解封用户
- 查看所有商品
- 查看所有订单
- 查看所有评价

### 买家 (BUYER)
- 搜索商品
- 查看商品详情
- 创建订单
- 查看我的订单
- 确认收货
- 添加评价

### 卖家 (SELLER)
- 发布商品
- 编辑商品
- 下架商品
- 查看我的商品
- 查看销售订单
- 确认发货

## 数据持久化

**实现方式**：Java原生序列化

**存储内容**：
- 所有用户数据
- 所有商品数据
- 所有订单数据
- 所有评价数据

**文件名**：`campus-market-data.ser`

**操作命令**：
- `save`：保存数据到文件
- `load`：从文件加载数据
- `exit`：退出时可选择保存

## 扩展功能

### 1. 多角色命令路由
系统支持同一命令名称对应不同角色的不同实现。例如：
- `product` 命令对买家显示商品详情
- `product` 命令对卖家显示商品管理
- `product` 命令对管理员显示所有商品

### 2. 事件驱动架构
使用EventBus实现事件发布-订阅模式，支持：
- 订单状态变更事件
- 可扩展添加更多事件类型

### 3. 密码加密
使用SHA-256算法对用户密码进行哈希加密存储，保证安全性

### 4. 并发支持
使用`ConcurrentHashMap`实现线程安全的数据存储

## 代码质量

### 注释规范
- 所有类都有详细的JavaDoc注释
- 所有公共方法都有注释说明
- 关键逻辑都有中文注释

### 命名规范
- 类名：大驼峰（PascalCase）
- 方法名：小驼峰（camelCase）
- 常量：全大写下划线分隔（UPPER_SNAKE_CASE）
- 变量：小驼峰（camelCase）

### 错误处理
- 使用Optional处理可能为空的情况
- 友好的错误提示信息
- 异常捕获和处理

## 系统优势

1. **轻量级**：无需数据库，启动快速
2. **易用性**：清晰的命令行交互，友好的提示信息
3. **扩展性**：基于设计模式，易于添加新功能
4. **安全性**：密码加密、权限控制、用户封禁
5. **可维护性**：代码结构清晰，职责分离明确
6. **学习价值**：综合应用多种Java特性和设计模式

## 运行说明

### 编译
```bash
# Mac/Linux
javac -encoding UTF-8 -d out $(find src -name "*.java")

# Windows PowerShell
$files = Get-ChildItem -Path src -Filter *.java -Recurse
javac -encoding UTF-8 -d out $files.FullName
```

### 运行
```bash
java -cp out com.campus.market.App
```

### 默认账号
- 管理员：admin / 123456
- 买家：buyer1 / 123456
- 卖家：seller1 / 123456

## 总结

本系统充分展示了Java高级特性的应用，包括：
- ✅ 泛型（Repository<T>）
- ✅ Lambda表达式（命令处理、事件监听、数据过滤）
- ✅ 注解（@Command、@RequiresRole）
- ✅ 函数式接口（CommandHandler、EventListener）
- ✅ 内部类（CommandInfo、DataSnapshot）
- ✅ 设计模式（仓储、命令、观察者、单例）
- ✅ Stream API（数据处理）
- ✅ 集合框架（ConcurrentHashMap、ArrayList）
- ✅ 反射（注解处理）
- ✅ 序列化（数据持久化）

系统设计思想清晰，代码结构优雅，注释完善，是一个优秀的Java命令行应用示例。
