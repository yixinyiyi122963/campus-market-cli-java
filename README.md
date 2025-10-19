# 校园二手交易系统（命令行版）

## 系统简介

校园二手交易系统是一个面向校园师生的轻量级命令行交易平台，支持二手商品发布、查询、购买、管理等操作。系统采用纯Java实现，无需数据库，使用内存存储配合文件序列化实现数据持久化。

### 主要特性

- **无数据库设计**：内存存储 + 文件序列化持久化
- **三种用户角色**：买家（Buyer）、卖家（Seller）、管理员（Admin）
- **完整交易流程**：发布 → 查询 → 下单 → 确认（卖家确认发货/买家确认收货） → 评价
- **功能完善**：用户管理、商品管理、订单管理、评价管理
- **设计优秀**：仓储模式、命令模式、事件总线、注解鉴权、泛型、Lambda、内部类等

### 技术亮点

- ✅ 泛型（Repository<T>）
- ✅ Lambda表达式（命令处理、事件监听、数据过滤）
- ✅ 注解（@Command、@RequiresRole）
- ✅ 函数式接口（CommandHandler、EventListener）
- ✅ 内部类（CommandInfo、DataSnapshot）
- ✅ 设计模式（仓储、命令、观察者、单例）
- ✅ Stream API（数据处理）
- ✅ 集合框架（ConcurrentHashMap、ArrayList）

详细设计文档请查看 [DESIGN.md](DESIGN.md)

## 快速开始

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

### 首次使用

1. 启动程序后，输入 `login` 使用默认账号登录
2. 输入 `help` 查看所有可用命令
3. 根据角色使用相应功能

## 默认账号

- 管理员：admin / 123456
- 买家：buyer1 / 123456
- 卖家：seller1 / 123456

可在登录后使用 `help` 查看所有命令。

## 核心命令

### 通用命令

- `help`：显示命令列表
- `login`：用户登录
- `register`：用户注册
- `logout`：退出登录
- `save`：保存数据到文件
- `load`：从文件加载数据
- `exit`：退出程序（可选择保存）

### 买家命令

- `search [keyword] [minPrice] [maxPrice]`：搜索商品（参数可省略）
- `product detail [productId]`：查看商品详情
- `order create [productId]`：对某商品下单
- `order my`：查看我的订单
- `order confirm-receive [orderId]`：确认收货
- `review add [orderId] [rating 1-5] [comment]`：对完成订单进行评价

### 卖家命令

- `product add`：发布商品（交互式输入）
- `product my`：查看我发布的商品
- `product edit [productId]`：编辑商品（交互式输入）
- `product remove [productId]`：下架商品
- `order for-me`：查看买家对我商品的订单
- `order confirm-ship [orderId]`：确认并发货

### 管理员命令

- `user list`：查看所有用户
- `user ban [userId]`：封禁用户
- `user unban [userId]`：解封用户
- `product all`：查看所有商品
- `order all`：查看所有订单
- `review all`：查看所有评价

## 使用示例

### 买家操作流程

```
1. 登录
[游客] > login
用户名: buyer1
密码: 123456

2. 搜索商品
[buyer1@买家] > search 笔记本
# 显示搜索结果

3. 查看商品详情
[buyer1@买家] > product detail PRD-xxx

4. 创建订单
[buyer1@买家] > order create PRD-xxx

5. 查看订单状态
[buyer1@买家] > order my

6. 确认收货（等待卖家发货后）
[buyer1@买家] > order confirm-receive ORD-xxx

7. 添加评价
[buyer1@买家] > review add ORD-xxx 5 非常好的商品！
```

### 卖家操作流程

```
1. 登录
[游客] > login
用户名: seller1
密码: 123456

2. 发布商品
[seller1@卖家] > product add
商品名称: 笔记本电脑
商品描述: 联想ThinkPad，i5处理器
商品分类: 电子产品
商品价格: 1500

3. 查看我的商品
[seller1@卖家] > product my

4. 查看订单
[seller1@卖家] > order for-me

5. 确认发货
[seller1@卖家] > order confirm-ship ORD-xxx
```

### 管理员操作

```
1. 登录
[游客] > login
用户名: admin
密码: 123456

2. 查看所有用户
[admin@管理员] > user list

3. 封禁用户
[admin@管理员] > user ban USR-xxx

4. 查看所有商品
[admin@管理员] > product all

5. 查看所有订单
[admin@管理员] > order all

6. 查看所有评价
[admin@管理员] > review all
```

## 系统架构

### 模块结构

```
com.campus.market
├── model/              # 实体模型
│   ├── User.java       # 用户实体
│   ├── Product.java    # 商品实体
│   ├── Order.java      # 订单实体
│   ├── Review.java     # 评价实体
│   └── *Status.java    # 状态枚举
├── repository/         # 数据访问层
│   ├── Repository.java           # 仓储接口（泛型）
│   └── InMemoryRepository.java   # 内存实现
├── service/            # 服务层
│   └── DataStore.java  # 数据持久化
├── command/            # 命令层
│   ├── CommandHandler.java       # 命令处理器接口
│   ├── CommandRegistry.java      # 命令注册器
│   ├── SystemCommands.java       # 系统命令
│   ├── BuyerCommands.java        # 买家命令
│   ├── SellerCommands.java       # 卖家命令
│   └── AdminCommands.java        # 管理员命令
├── auth/               # 认证授权
│   ├── SessionManager.java       # 会话管理
│   └── RequiresRole.java         # 角色注解
├── event/              # 事件系统
│   ├── Event.java                # 事件接口
│   ├── EventBus.java             # 事件总线
│   ├── EventListener.java        # 事件监听器
│   └── OrderStatusChangedEvent.java  # 订单状态变更事件
├── util/               # 工具类
│   ├── IdGenerator.java          # ID生成器
│   └── PasswordHasher.java       # 密码哈希
└── App.java            # 应用入口
```

### 核心设计模式

1. **仓储模式（Repository Pattern）**：抽象数据访问层
2. **命令模式（Command Pattern）**：命令注册和执行
3. **观察者模式（Observer Pattern）**：事件总线和监听器
4. **单例模式（Singleton Pattern）**：SessionManager、DataStore、EventBus

详细设计说明请查看 [DESIGN.md](DESIGN.md)

## 扩展功能

### 1. 智能命令路由
系统支持同一命令名称对应不同角色的不同实现，根据用户角色自动路由到正确的处理器。

### 2. 事件驱动架构
订单状态变更自动触发事件，系统通过事件监听器更新相关数据，实现模块解耦。

### 3. 安全性保障
- 密码SHA-256加密存储
- 基于注解的权限控制
- 用户封禁机制

### 4. 数据持久化
使用Java原生序列化，支持数据的保存和加载，退出时可选择保存。

## 注意事项

1. **编码问题**：编译时请使用 `-encoding UTF-8` 参数
2. **数据保存**：重要数据请及时使用 `save` 命令保存
3. **账号安全**：首次使用后建议修改默认密码（当前版本需手动编辑代码实现）
4. **权限控制**：每个命令都有角色限制，请使用对应角色登录

## 项目特点

### 设计优势
- **职责分离**：清晰的分层架构
- **代码复用**：泛型和设计模式的应用
- **易于扩展**：基于接口和注解的设计
- **注释完善**：所有类和方法都有详细注释

### 学习价值
- 深入理解Java高级特性
- 掌握常用设计模式
- 学习命令行应用开发
- 理解事件驱动架构

## 常见问题

### Q1: 如何添加新的用户角色？
A: 在 `UserRole` 枚举中添加新角色，并创建对应的命令处理器类。

### Q2: 如何扩展新的命令？
A: 在相应的命令处理器类中添加带 `@Command` 和 `@RequiresRole` 注解的方法。

### Q3: 数据存在哪里？
A: 运行时在内存中，使用 `save` 命令后保存到 `campus-market-data.ser` 文件。

### Q4: 如何重置数据？
A: 删除 `campus-market-data.ser` 文件，重新启动程序即可。

## 开发者信息

本项目是一个教学示例，展示了Java高级特性和设计模式的综合应用。

### 联系方式
- 项目地址：https://github.com/yixinyiyi122963/campus-market-cli-java

## 许可证

本项目仅供学习交流使用。
