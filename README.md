# 校园二手交易系统（命令行版）

- 无数据库，内存存储 + 文件序列化持久化
- 角色：买家（Buyer）、卖家（Seller）、管理员（Admin）
- 流程：发布 → 查询 → 下单 → 确认（卖家确认发货/买家确认收货） → 评价
- 功能：用户管理、商品管理、订单管理、评价管理
- 设计：仓储模式、命令模式、事件总线、注解鉴权、泛型、Lambda、内部类等

## 运行

```bash
# 编译（Mac/Linux）
javac -encoding UTF-8 -d out $(find src -name "*.java")

# 运行
java -cp out com.campus.market.App
```

Windows 可在 PowerShell 使用类似命令（或用 IDE 运行）。

## 默认账号

- 管理员：admin / 123456
- 买家：buyer1 / 123456
- 卖家：seller1 / 123456

可在登录后使用 `help` 查看所有命令。

## 核心命令

通用：
- help：显示命令列表
- logout：退出登录
- exit：退出程序（可选择保存）
- save：保存数据到文件
- load：从文件加载数据

买家：
- search [keyword] [minPrice] [maxPrice]：搜索商品（参数可省略）
- product detail [productId]：查看商品详情
- order create [productId]：对某商品下单
- order my：查看我的订单
- order confirm-receive [orderId]：确认收货
- review add [orderId] [rating 1-5] [comment]：对完成订单进行评价

卖家：
- product add：发布商品（交互式输入）
- product my：查看我发布的商品
- product edit [productId]：编辑商品（交互式输入）
- product remove [productId]：下架商品
- order for-me：查看买家对我商品的订单
- order confirm-ship [orderId]：确认并发货

管理员：
- user list：查看所有用户
- user ban [userId]：封禁用户
- user unban [userId]：解封用户
- product all：查看所有商品
- order all：查看所有订单
- review all：查看所有评价

## 设计说明

- 注解 `@RequiresRole` 用于命令权限控制
- `CommandRegistry` 使用函数式接口 + Lambda 注册与执行命令
- `Repository<T>` + `InMemoryRepository<T>` 抽象数据访问层
- `EventBus` 分发 `OrderStatusChangedEvent`，监听器更新商品状态
- `DataStore` 使用原生序列化保存/加载仓储快照
- 密码用 `PasswordHasher` 做 SHA-256 哈希
