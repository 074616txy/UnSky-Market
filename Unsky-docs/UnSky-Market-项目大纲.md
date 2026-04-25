# UnSky Market — 校园二手交易平台

> 项目定位：校园专属二手交易平台，服务于在校师生
> 技术栈：Spring Boot + MyBatis-Plus + Redis + MySQL + JWT
> 目标：独立设计、手搓开发、完整上线

---

## Maven 多模块结构（不断迭代升级）

### ⭐ 当前结构（V2）

- 这里会持续存放最新项目完整模块框架！便于后期迭代版本查找 ˗ˏˋ ★ ˎˊ˗ !!!

```
Maven 项目结构（当前版本）

UnSky Market Project/（根目录）
│
├── UnSky-backend/（核心后端模块，Spring Boot 启动模块）
│   ├── src/main/
│   │   ├── java/com/Market/
│   │   │   ├── user/                         （用户模块）
│   │   │   │   ├── controller/              （接口层）
│   │   │   │   │   ├── TestController.java
│   │   │   │   │   └── UserController.java   // 用户登录、注册接口
│   │   │   │   ├── mapper/                  （数据访问层）
│   │   │   │   │   └── UserMapper.java       // 用户数据库操作
│   │   │   │   └── service/                 （业务接口层）
│   │   │   │       ├── UserService.java      // 用户业务接口定义
│   │   │   │       └── impl/
│   │   │   │          └── UserServiceImpl.java // 用户业务逻辑实现
│   │   │   │
│   │   │   └── UnSkyApplication.java        （启动类）
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   │
│   └── pom.xml
│
├── UnSky-common/（通用模块）
│   ├── src/main/
│   │   └── java/com/Market/common/
│   │       ├── entity/                   （通用实体类，如 User）
│   │       │   └── User.java
│   │       ├── exception/                  （全局异常处理）
│   │       │   └── GlobalExceptionHandler.java
│   │       └── result/                     （统一返回结构）
│   │           └── Result.java
│   │
│   └── pom.xml
│
├── UnSky-database/（数据库脚本目录，非 Maven 模块）
│   └── day01/
│
└── pom.xml（父工程）
```
### 【V1】单体结构（Day01：项目跑通----2026/04/20 

```
Maven 项目结构【V1】

UnSky Market Project/（根父 POM，packaging=pom）
│
├── UnSky Market-backend/（后端父 POM，packaging=pom）
│   ├── UnSky Market/（Spring Boot 应用）
│   └── Common/（通用模块，Day 02 新增）
│
└── Unsky Market-database/（数据库脚本模块，packaging=pom）
    └── day01/（SQL 脚本）
```

> **IDEA 打开方式**：打开项目根目录 `D:/Develop/UnSky Market Project/`（有根 `pom.xml` 的一层），而非单独打开后端或数据库子目录。

### 【V2】初步按照业务分模块（Day02：结构拆分）----2026/04/22

```
Maven 项目结构【V2】

UnSky Market Project/（根目录）
│
├── UnSky-backend/（核心后端模块，Spring Boot 启动模块）
│   ├── src/main/
│   │   ├── java/com/Market/
│   │   │   ├── user/
│   │   │   │   ├── controller/        （接口层）
│   │   │   │   │   └── TestController.java  （测试类）
│   │   │   │   ├── mapper/            （数据访问层）
│   │   │   │   │   └── UserMapper.java
│   │   │   │   └── service/           （业务层）
│   │   │   │
│   │   │   └── UnSkyApplication.java  （启动类）
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   │
│   └── pom.xml
│
├── UnSky-common/（通用模块）
│   ├── src/main/
│   │   └── java/com/Market/common/
│   │       ├── entity/        （通用实体类，如 User）
│   │       ├── exception/     （全局异常处理）
│   │       └── result/        （统一返回结构 Result）
│   │   
│   └── pom.xml
│
├── UnSky-database/（数据库脚本目录，非 Maven 模块）
│    └── day01/
│    └──......
└── pom.xml（父工程）
```

> There 确定了后续开发要**按照业务模块分类**，这里面新增user用户模块来示例，并用这个模块成功进行了测试，后续开发其他模块直接在`com/Market/`下面进行同层级扩展即可。

### 【V3】用户体系模块（Day03：注册登录接口实现）----2026/04/22

```
Maven 项目结构【V3】

UnSky Market Project/（根目录）
│
├── UnSky-backend/（核心后端模块，Spring Boot 启动模块）
│   ├── src/main/
│   │   ├── java/com/Market/
│   │   │   ├── user/                         （用户模块）
│   │   │   │   ├── controller/              （接口层）
│   │   │   │   │   ├── TestController.java
│   │   │   │   │   └── UserController.java   // 用户登录、注册接口
│   │   │   │   ├── mapper/                  （数据访问层）
│   │   │   │   │   └── UserMapper.java       // 用户数据库操作
│   │   │   │   ├── service/                 （业务接口层）
│   │   │   │   │    ├── UserService.java      // 用户业务接口定义
│   │   │   │   │    └── impl/
│   │   │   │   │       └── UserServiceImpl.java // 用户业务逻辑实现
│   │   │   │   └── vo/
│	│	│	│      └── LoginVO.java
│   │   │   └── UnSkyApplication.java        （启动类）
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   │
│   └── pom.xml
│
├── UnSky-common/（通用模块）
│   ├── src/main/
│   │   └── java/com/Market/common/
│   │       ├── entity/                     （通用实体类，如 User）
│   │       │   └── User.java
│   │       ├── exception/                  （全局异常处理）
│   │       │   └── GlobalExceptionHandler.java
│   │       ├── result/                     （统一返回结构）
│   │       │   └── Result.java
│   │       └── util/
│   │           └── JwtUtil.java
│   └── pom.xml
│
├── UnSky-database/（数据库脚本目录，非 Maven 模块）
│   └── day01  
│   └── ......
└── pom.xml（父工程）

```

>  在原有项目基础结构上，完成了第一个完整业务模块user的落地，标志着系统从“框架搭建”正式进入“业务开发阶段”,后续内容仍然会在分功能模块的基础架构上盖楼房，user楼房的基础搭建正式完成。

--- 

## 一、项目概述

### 核心用户场景

- 学生出售闲置教材、电子设备、生活用品
- 买家快速找到校内二手商品，线下交易
- 平台提供商品发布、搜索、下单、评价全流程

### 核心功能模块

```
① 用户模块    ② 商品模块    ③ 搜索模块
④ 交易模块    ⑤ 缓存与优化  ⑥ 个人中心
⑦ 评价模块    ⑧ 系统管理    ⑨ 部署上线
```


## 二、需求分析与功能设计

### 2.1 用户模块

**功能清单：**

- [ ] 用户注册（手机号 + 验证码）
- [ ] 用户登录（手机号 + 密码 / 验证码登录）
- [ ] JWT Token 鉴权（登录令牌 + 刷新机制 + 黑名单）
- [ ] 个人信息管理（头像、昵称、学校、学院、学号）
- [ ] 校园身份认证（学号绑定，审核状态）
- [ ] 密码修改 / 重置
- [ ] 退出登录

**关键设计：**

- Token 存入 Redis，设置过期时间，支持主动失效
- 校园认证状态：待审核 / 已通过 / 未通过

---

### 2.2 商品模块（核心）

**功能清单：**

- [ ] 商品发布（标题、描述、定价、分类、图片上传）
- [ ] 商品分类管理（数码、教材、生活、衣物、虚拟物品、其他）
- [ ] 商品图片管理（支持多图、本地存储或 OSS）
- [ ] 商品列表（分页查询）
- [ ] 商品筛选（分类 / 价格区间 / 新旧程度 / 学校）
- [ ] 商品详情（浏览量 +1）
- [ ] 商品上下架（卖家自主操作）
- [ ] 商品编辑 / 删除
- [ ] 我发布的商品列表
- [ ] 商品浏览量、收藏量统计

**关键设计：**

- 浏览量存入 Redis，定时写回 MySQL（减少数据库压力）
- 商品状态：草稿 / 上架 / 已售 / 下架
- 图片压缩存储，控制大小

---

### 2.3 搜索模块

**功能清单：**

- [ ] 关键词搜索（MySQL 全文索引或 LIKE）
- [ ] 搜索历史记录（当前用户，存入 Redis，过期 7 天）
- [ ] 热门搜索排行（Redis ZSet，按搜索次数排序）
- [ ] 搜索建议（联想词）
- [ ] 空结果推荐（随机热门商品兜底）

**关键设计：**

- 搜索词去重后写入 ZSet，每次搜索 +1
- 搜索历史限制最近 20 条，超出删除最旧记录

---

### 2.4 交易模块（核心）

**功能清单：**

- [ ] 收藏商品（买家收藏商品列表）
- [ ] 购物车（加入购物车、数量修改、删除）
- [ ] 生成订单（买家发起，锁定库存）
- [ ] 订单列表（按状态筛选：待付款 / 待发货 / 待收货 / 已完成 / 已取消）
- [ ] 卖家发货（填写物流信息，可选）
- [ ] 买家确认收货
- [ ] 订单取消（买家未付款可取消 / 超时自动取消）
- [ ] 订单超时自动关闭（定时任务，30分钟未付款自动取消）
- [ ] 交易完成后库存扣减

**关键设计：**

- Redis 分布式锁防止超卖
- 订单号生成规则：时间戳 + 用户ID + 随机数
- 订单状态机：待付款 → 待发货 → 待收货 → 已完成
- 超时取消用定时任务（@Scheduled）扫描过期订单

---

### 2.5 缓存与性能优化

**功能清单：**

- [ ] 商品分类列表缓存（Redis，过期 1 小时）
- [ ] 商品详情页缓存（商品ID → 详情，热门商品预热）
- [ ] 热门商品缓存（ZSet，按浏览量排序）
- [ ] 搜索结果缓存（关键词 → 结果集，过期 5 分钟）
- [ ] Redis 分布式锁（防重复下单、防刷接口）
- [ ] 接口限流（基于 IP + UserId，滑动窗口算法）
- [ ] 定时任务框架整合（xxl-job 或 Spring @Scheduled）

---

### 2.6 个人中心

**功能清单：**

- [ ] 个人信息查看 / 编辑
- [ ] 我卖出的订单（作为卖家的订单）
- [ ] 我买到的订单（作为买家的订单）
- [ ] 我的收藏列表
- [ ] 我的购物车
- [ ] 校园认证状态查看

---

### 2.7 评价模块

**功能清单：**

- [ ] 交易完成后，买家对卖家评分（1~5星）
- [ ] 交易完成后，卖家对买家评分（1~5星）
- [ ] 评价内容（文字，限200字）
- [ ] 匿名评价选项
- [ ] 评价展示（商品详情页 / 个人主页展示信用评分）
- [ ] 恶意差评申诉入口（可选）

**关键设计：**

- 交易完成后才可评价，评价后不可修改
- 用户信用分 = 平均评分 × 20（满分 100）

---

### 2.8 系统管理与运营

**功能清单：**

- [ ] 商品类目管理（后台增删改分类）
- [ ] 用户状态管理（封禁 / 解封）
- [ ] 敏感词过滤（商品标题 / 描述内容审核）
- [ ] 数据统计看板（用户量、订单量、GMV）
- [ ] 举报商品（买家举报商品信息不实）
- [ ] 举报处理（管理员查看、处理）

**关键设计：**

- 管理员账号与普通用户账号分离
- 敏感词库本地加载，匹配即替换

---

### 2.9 部署上线

**功能清单：**

- [ ] 项目打包（mvn package）
- [ ] application.yml 生产环境配置
- [ ] 云服务器购买与配置（或阿里云/腾讯云学生机）
- [ ] MySQL 生产库部署
- [ ] Redis 生产部署
- [ ] JAR 部署与进程守护（supervisor 或 systemd）
- [ ] 域名解析 + HTTPS 配置
- [ ] knife4j 接口文档整理输出
- [ ] GitHub 仓库整理（代码 + README + 项目文档）

---

## 三、数据库设计

### 3.1 核心表结构

**用户表（sys_user）** — 对应 `com.Market.entity.User`

| 字段 | Java属性 | 类型 | 说明 |
|------|---------|------|------|
| id | id | bigint | 主键（自增） |
| phone | phone | varchar(20) | 手机号（UNIQUE） |
| password | password | varchar(255) | 密码（BCrypt加密） |
| nickname | nickname | varchar(50) | 昵称 |
| avatar | avatar | varchar(255) | 头像URL |
| school | school | varchar(100) | 学校 |
| student_id | studentId | varchar(50) | 学号 |
| auth_status | authStatus | tinyint | 认证状态（0/1/2） |
| credit_score | creditScore | int | 信用分 |
| create_time | createTime | datetime | 注册时间 |

**商品表（product）** — 对应 `com.Market.Common.Entity.Product`

| 字段 | Java属性 | 类型 | 说明 |
|------|---------|------|------|
| id | id | bigint | 主键（自增） |
| seller_id | sellerId | bigint | 卖家ID |
| title | title | varchar(128) | 标题 |
| description | description | text | 描述 |
| price | price | decimal(10,2) | 价格 |
| original_price | originalPrice | decimal(10,2) | 原价 |
| category_id | categoryId | bigint | 分类ID |
| condition_level | conditionLevel | tinyint | 新旧程度（1~5） |
| images | images | text | 图片JSON数组 |
| view_count | viewCount | int | 浏览量 |
| favorite_count | favoriteCount | int | 收藏量 |
| status | status | tinyint | 状态（上架/下架/已售） |
| create_time | createTime | datetime | 发布时间 |

**订单表（order）** — 对应 `com.Market.Common.Entity.Order`

| 字段 | Java属性 | 类型 | 说明 |
|------|---------|------|------|
| id | id | bigint | 主键（自增） |
| order_no | orderNo | varchar(32) | 订单号 |
| buyer_id | buyerId | bigint | 买家ID |
| seller_id | sellerId | bigint | 卖家ID |
| product_id | productId | bigint | 商品ID |
| total_price | totalPrice | decimal(10,2) | 订单金额 |
| status | status | tinyint | 状态 |
| logistics_no | logisticsNo | varchar(64) | 物流单号 |
| expire_time | expireTime | datetime | 订单过期时间 |
| create_time | createTime | datetime | 下单时间 |
| update_time | updateTime | datetime | 更新时间 |

**收藏表（favorite）** — 对应 `com.Market.Common.Entity.Favorite`

| 字段 | Java属性 | 类型 | 说明 |
|------|---------|------|------|
| id | id | bigint | 主键 |
| user_id | userId | bigint | 用户ID |
| product_id | productId | bigint | 商品ID |
| create_time | createTime | datetime | 收藏时间 |

**购物车表（cart）** — 对应 `com.Market.Common.Entity.Cart`

| 字段 | Java属性 | 类型 | 说明 |
|------|---------|------|------|
| id | id | bigint | 主键 |
| user_id | userId | bigint | 用户ID |
| product_id | productId | bigint | 商品ID |
| quantity | quantity | int | 数量 |
| create_time | createTime | datetime | 加入时间 |

**评价表（review）** — 对应 `com.Market.Common.Entity.Review`

| 字段 | Java属性 | 类型 | 说明 |
|------|---------|------|------|
| id | id | bigint | 主键 |
| order_id | orderId | bigint | 订单ID |
| from_user_id | fromUserId | bigint | 评价方 |
| to_user_id | toUserId | bigint | 被评价方 |
| score | score | tinyint | 评分 1~5 |
| content | content | varchar(200) | 评价内容 |
| is_anonymous | isAnonymous | tinyint | 是否匿名 |
| create_time | createTime | datetime | 评价时间 |

**商品分类表（category）** — 对应 `com.Market.Common.Entity.Category`

| 字段 | Java属性 | 类型 | 说明 |
|------|---------|------|------|
| id | id | bigint | 主键 |
| name | name | varchar(32) | 分类名 |
| sort | sort | int | 排序 |
| status | status | tinyint | 状态 |

---

## 四、技术要点清单

### 4.1 必须掌握的核心技术

- [ ] Spring Boot 自动配置原理
- [ ] MyBatis-Plus CRUD 封装 + 分页插件
- [ ] JWT Token 颁发、验证、刷新机制
- [ ] Redis String / Hash / Set / ZSet / List 全操作
- [ ] Redis 缓存三问题（穿透 / 击穿 / 雪崩）
- [ ] Redis 分布式锁（SET NX EX 实现）
- [ ] Spring @Scheduled 定时任务
- [ ] 统一返回格式（ResultBody）
- [ ] 全局异常处理器（@RestControllerAdvice）
- [ ] 文件上传本地存储
- [ ] MySQL 索引优化（商品搜索字段加索引）

### 4.2 项目亮点设计

> 面试时重点讲这些

1. **Redis 分布式锁** — 解决重复下单、超卖问题
2. **定时任务** — 订单超时自动取消
3. **缓存策略** — 多层缓存（分类 / 商品 / 搜索）
4. **商品状态机** — 清晰的状态流转设计
5. **搜索历史 + 热门排行** — ZSet 的典型应用场景
6. **信用评分体系** — 交易评价累积信用分

---

## 五、开发路线图

> 每个 Day = 解决一个大问题，前面的 Day 是后面 Day 的基础。
> ✓ = 已完成

### Day 00  开工准备 ✓ 已完成

**目标：** 工具安装完毕，能打开 IDEA，看到完整的空项目树

> **记录方式说明：Day 00 采用单篇记录结构。**
> - 主篇：[[项目日记-day00-工具配置 & 开工准备]]
>
> 本阶段主要完成开发环境准备、工具安装、项目目录认知与基础运行环境确认，重点在于“能否顺利开工”，暂不涉及需要单独拆分的增强内容。

**操作清单：**

- [x] 安装 IDEA（配置 JDK 11 环境）
- [x] 安装 MySQL 8.x
- [x] 安装 DataGrip / IDEA Database 插件，连接 MySQL
- [x] 安装 Git，在项目根目录初始化仓库
- [x] 认识项目目录结构（src/main/java、src/main/resources、pom.xml）
- [x] 在 IDEA 中打开项目，验证项目能被正确识别

---

### Day 01  骨架跑通 & 数据库设计 ✓ 已完成

**目标：** 从 Java 到 前端的完整链路跑通，能在前端发送请求，在后端接受请求

**前置依赖：** Day 00

> **记录方式说明：Day 01 采用单篇记录结构。**
> - 主篇：[[项目日记/项目日记-day01-骨架搭建 & 数据库连接]]
>
> 本阶段主要围绕项目骨架跑通、数据库连接建立与基础测试接口验证展开，目标是完成“项目可以启动、请求可以进入、数据库可以连通”的最初始链路确认。

**操作清单：**
> 【注意：在Day02中我已经对整个项目结构进行修改，新项目结构在Day02展示，所以下方日记中有些与Day02不符合的，皆以Day02为准】

- [x] 新建 `D:/Develop/UnSky Market Project/` 作为项目根目录
- [x] 创建根父 `pom.xml`（`D:/Develop/UnSky Market Project/pom.xml`），声明 `<packaging>pom</packaging>` + `<modules>`
- [x] 创建后端父模块 `UnSky Market-backend/` 及其 `pom.xml`（继承根父 POM）
- [x] 创建 Spring Boot 子模块 `UnSky Market-backend/UnSky Market/` 及其 `pom.xml`（继承 Spring Boot Parent 2.7.18）
- [x] 在子模块 `pom.xml` 中引入依赖：spring-boot-starter-web、mybatis-plus-boot-starter、mysql-connector-j、lombok、spring-boot-starter-validation
- [x] 创建 `UnSky Market-backend/.idea/modules.xml`，引用 `UnSkyMarket.iml`
- [x] 在 MySQL 中执行建库建表 SQL（数据库 `unsky_market`，表 `sys_user`）
- [x] 编写 `application.yml`（端口 8080、数据库连接、MyBatis-Plus 配置、`map-underscore-to-camel-case: true`）
- [x] 创建实体类 `User.java`（加 `@Data`、`@TableName`、`@TableId` 注解）
- [x] 创建启动类 `UnSkyApplication.java`（加 `@SpringBootApplication`）
- [x] Maven Reload，验证依赖全部下载成功
- [x] 右键启动类 → Run，验证 Spring Boot 正常启动（检验JDK与Spring Boot版本问题是否兼容）
- [x] 写一个 `@GetMapping("/test")` 接口，返回 "Hello"，验证 HTTP 能通

**交付物：** 项目能跑通、数据库能查到数据、实体类注解完整

---

### Day 02  项目工程化 & 技术基建 ✓ 已完成

**目标：** Maven 多模块结构完成，统一返回格式和全局异常处理上线

**前置依赖：** Day 01

> **记录方式说明：Day 02 采用单篇记录结构。**
> - 主篇：[[项目日记/项目日记-day02-项目工程化 & 技术基建]]
>
> 本阶段主要完成项目工程化升级，包括 Maven 多模块拆分、通用模块抽离、统一返回结构接入与全局异常处理落地，重点是为后续业务模块开发提供稳定、清晰、可复用的技术基建。

**操作清单：**

> ✅ 注：Day 01 已完成 `UnSky-backend/.idea/modules.xml` 的创建和子模块 `.iml` 的引用，无需重复操作。

- [x] 在根父 `pom.xml` 中声明 `<modules><module>UnSky-backend</module>
- [x] 验证 IDEA 打开 `D:/Develop/UnSky Market Project` 后项目树完整，两个模块均可见
- [x] 在 `UnSky Market Project/` 下新建 `Unsky-Common` 模块（`UnSky Market Project/Unsky-Common/pom.xml`）
- [x] 在根父 `pom.xml` 的 `<modules>` 中加入 `UnSky-backend` 后，在其 `<modules>` 中加入 ``Unsky-Common`` 模块
- [x] 将统一返回类 `Result<T>`（code、msg、data）放入 `Unsky-Common` 模块
- [x] 将全局异常处理器 `GlobalExceptionHandler`（`@RestControllerAdvice`）放入 `Unsky-Common` 模块
- [x] 将通用工具类（如 `JwtUtil`、`DateUtil`）放入 `Unsky-Common` 模块
- [x] 在 UnSky Market 子模块的 `pom.xml` 中添加对 `Unsky-Common` 模块的依赖
- [x] 所有 Controller 接口统一返回 `Result.success(data)` 格式
- [x] 验证异常（除零、NullPointer 等）能被全局处理器拦截，返回统一错误格式

**交付物：** Maven 多模块结构稳定、统一返回格式全站生效、全局异常处理上线

---

### Day 03  用户体系 — 注册 & 登录 ✓ 已完成

**目标：** 用户能注册、能登录，拿到 JWT Token

**前置依赖：** Day 02（需要统一返回结构）

> **记录方式说明：Day 03 采用“主篇 + 补强篇”的双篇记录结构。**
> - 主篇：[[项目日记/项目日记-day03-用户体系 — 注册 & 登录]]
> - 补强篇：[[项目日记-day03🌿-用户体系 — 用户认证与安全补强]]
>
> 其中主篇优先记录注册与登录基础链路的搭建、联调与跑通；补强篇则集中补充重复注册校验、密码加密、JWT 认证等与用户认证和安全相关的增强内容。

**操作清单：**

- [x] 实现注册接口 `/api/user/register`（手机号 + 密码，基础链路已跑通）
- [x] 实现登录接口 `/api/user/login`（手机号 + 密码，基础校验已完成）
- [x] 重复注册校验（避免手机号重复写入）
- [x] 注册密码加密存储（BCrypt 等方案）
- [x] 登录密码加密比对
- [x] 实现 JWT Token 颁发（用户ID + 过期时间，签名加密）
- [x] 实现登录拦截器（`HandlerInterceptor`），验证 Token 有效性
- [x] 将拦截器注册到 `WebMvcConfigurer`，放行登录/注册接口
- [x] 实现修改个人信息接口（昵称、头像、学校）
- [x] 实现退出登录接口（服务端 Token 黑名单 or 前端删除 Token）
- [x] 用 Apifox 测试所有接口，验证注册→登录→访问受保护接口的完整链路

**交付物：** 用户注册/登录基础链路跑通，用户认证与安全补强方向明确，并逐步完成 Token 鉴权相关能力接入

---

### Day 04  用户认证 — 学生身份认证

**目标：** 只有认证通过的学生才能发布商品

**前置依赖：** Day 03（登录后）

**操作清单：**

- [ ] 实现个人中心接口 `/api/user/info`（需带 Token，返回用户基本信息）
- [ ] 在 MySQL 中新建 `student_cert` 表（id、user_id、student_name、school、student_id、id_card_front、id_card_back、status、remark、create_time）
- [ ] 创建 `StudentCert.java` 实体类
- [ ] 实现提交认证申请接口（填学校+学号+上传证件照片）
- [ ] 实现查询认证状态接口（用户查看自己的认证进度）
- [ ] 实现管理员审核接口（通过/拒绝认证申请，更新 `auth_status`）
- [ ] 修改商品发布接口，增加认证状态校验（未认证用户不能发布）
- [ ] 在 User 实体中补充 `authStatus` 和 `creditScore` 字段逻辑
- [ ] 验证：未认证用户调用发布商品接口 → 返回"请先完成学生认证"

**交付物：** 认证申请提交 → 管理员审核 → 认证状态查询 全链路跑通

---

### Day 06  商品体系 — 商品浏览 & 搜索过滤

**目标：** 用户能浏览商品列表、查看详情、按条件筛选

**前置依赖：** Day 04（需要认证后才能发布，商品才能上架）

> Day 05 图片上传暂时跳过，商品先用文字描述上线

**操作清单：**

- [ ] 在 MySQL 中新建 `category` 表（id、name、sort、status）
- [ ] 初始化分类数据（数码、教材、生活、衣物、虚拟物品、其他）
- [ ] 创建 `Category.java` 实体类
- [ ] 实现分类列表接口 `/api/category/list`（返回所有启用的分类）
- [ ] 在 MySQL 中新建 `product` 表（id、seller_id、title、description、price、original_price、category_id、condition_level、view_count、status、create_time）
- [ ] 创建 `Product.java`、`ProductVO.java` 实体类（VO 用于接口返回，脱敏不必要字段）
- [ ] 实现商品发布接口 `/api/product/publish`（需认证，需登录）
- [ ] 实现商品列表接口 `/api/product/list`（分页 + 分类筛选 + 价格排序）
- [ ] 实现商品详情接口 `/api/product/detail/{id}`（返回商品完整信息）
- [ ] 实现商品编辑/删除接口（只能操作自己的商品）
- [ ] 实现我发布的商品列表接口 `/api/product/my`
- [ ] 用 Postman 测试：发布 → 列表 → 详情 → 编辑 → 删除 全链路

**交付物：** 商品发布/列表/详情/编辑/删除 跑通，分页和筛选生效

---

### Day 07  交易准备 — 购物车 & 收藏

**目标：** 用户能收藏商品、能加入购物车

**前置依赖：** Day 06（需要先有商品）

**操作清单：**

- [ ] 在 MySQL 中新建 `favorite` 表（id、user_id、product_id、create_time）
- [ ] 创建 `Favorite.java`、`FavoriteVO.java`
- [ ] 实现收藏商品接口 `/api/favorite/add/{productId}`（一个用户对同一商品只能收藏一次）
- [ ] 实现取消收藏接口 `/api/favorite/cancel/{productId}`
- [ ] 实现我的收藏列表接口 `/api/favorite/list`（分页）
- [ ] 在 MySQL 中新建 `cart` 表（id、user_id、product_id、quantity、create_time）
- [ ] 创建 `Cart.java`、`CartVO.java`
- [ ] 实现加入购物车接口 `/api/cart/add`
- [ ] 实现修改数量接口 `/api/cart/update`
- [ ] 实现删除购物车商品接口 `/api/cart/remove`
- [ ] 实现我的购物车列表接口 `/api/cart/list`
- [ ] 验证重复收藏/重复加购物车的幂等性处理

**交付物：** 收藏和购物车的增删改查全通，幂等性处理正确

---

### Day 08  交易流程 — 订单流程

**目标：** 用户能下单、能管理订单全生命周期

**前置依赖：** Day 07

**操作清单：**

- [ ] 在 MySQL 中新建 `order` 表（id、order_no、buyer_id、seller_id、product_id、total_price、status、logistics_no、expire_time、create_time）
- [ ] 创建 `Order.java`、`OrderVO.java`
- [ ] 设计订单状态机：待付款(0) → 待发货(1) → 待收货(2) → 已完成(3)，取消(-1)
- [ ] 实现立即购买接口 `/api/order/create`（下单时检查商品是否仍可售）
- [ ] 实现购物车结算接口 `/api/order/checkout`（批量下单）
- [ ] 实现订单列表接口 `/api/order/list`（按状态筛选，分页）
- [ ] 实现取消订单接口（买家未付款可取消，超时自动取消）
- [ ] 实现发货接口 `/api/order/ship`（卖家填写物流单号）
- [ ] 实现确认收货接口 `/api/order/confirm`（买家确认，状态流转）
- [ ] 实现超时自动取消（`@Scheduled` 定时任务，扫描 `expire_time` < 当前时间的待付款订单）
- [ ] 验证：订单状态只能按状态机流转，不能跨状态跳跃

**交付物：** 订单从创建到完成的全部接口，状态机流转正确，定时任务生效

---

### Day 09  信用体系 — 交易评价 & 信用管理

**目标：** 交易完成后能互相评价，信用分动态调整

**前置依赖：** Day 08（需要订单完成后才能评价）

**操作清单：**

- [ ] 在 MySQL 中新建 `review` 表（id、order_id、from_user_id、to_user_id、score、content、is_anonymous、create_time）
- [ ] 创建 `Review.java`、`ReviewVO.java`
- [ ] 实现评价接口 `/api/review/add`（订单状态为已完成才能评价）
- [ ] 实现查询评价接口（查看某用户的全部评价 / 某商品的评价）
- [ ] 实现信用分计算逻辑：每次评价后更新被评价方的 `creditScore`
- [ ] 在用户详情接口中返回信用评分和评价统计
- [ ] 验证：未完成订单不能评价，同一订单不能重复评价

**交付物：** 评价接口上线，信用分动态更新，评价展示正确

---

### Day 10  后台管理 — 管理员模块

**目标：** 管理员能管理用户、商品、认证审核

**前置依赖：** Day 03（参考登录逻辑）+ Day 04 + Day 06

**操作清单：**

- [ ] 在 MySQL 中新建 `admin` 表（id、username、password、role、create_time）
- [ ] 创建 `Admin.java` 实体类和 `AdminController`
- [ ] 实现管理员登录接口（独立账号体系，不与用户表混用）
- [ ] 实现用户管理接口（查看用户列表、封禁/解封账号）
- [ ] 实现商品管理接口（下架违规商品、查看所有商品列表）
- [ ] 实现认证审核接口（Day 04 未完成的另一半：管理员审核）
- [ ] 实现数据统计接口（用户总数、今日新增订单、GMV）
- [ ] 配置管理员拦截器（普通用户不能访问 admin 接口）
- [ ] 验证：普通用户 Token 无法访问所有管理员接口

**交付物：** 管理员后台可用，用户和商品风控功能上线

---

### Day 11  项目上线 — 部署 & 发布

**目标：** 项目打包部署到云服务器，能通过域名访问

**前置依赖：** 前 10 天所有功能基本跑通

**操作清单：**

- [ ] 在 `application.yml` 中配置生产环境数据库连接
- [ ] 修改 `pom.xml` 的打包配置（指定最终 jar 包名）
- [ ] 执行 `mvn clean package -DskipTests`，验证 jar 包生成
- [ ] 购买云服务器（或使用学生机），安装 JDK 11 + MySQL 8 + Redis
- [ ] 将 jar 包上传至服务器，用 `nohup java -jar xxx.jar &` 后台运行
- [ ] 配置 Nginx 反向代理（80 端口 → 8080）
- [ ] 申请域名并配置 DNS 解析
- [ ] 申请 SSL 证书，配置 HTTPS
- [ ] 用 knife4j 或 Swagger 生成在线接口文档
- [ ] GitHub 仓库整理（代码 + README.md + 项目文档）
- [ ] 验证：通过域名访问接口，全部功能正常

**交付物：** 线上可访问的完整系统 + 接口文档 + 代码仓库

---

## 六、技术要点对照表

| Day | 技术要点 |
|-----|---------|
| Day 01 | Spring Boot 自动配置、MyBatis-Plus CRUD、`map-underscore-to-camel-case` |
| Day 02 | Maven 多模块（父 pom + 子模块）、统一返回格式、全局异常处理 |
| Day 03 | 重复注册校验、BCrypt 密码加密、JWT Token 颁发与验证、拦截器 |
| Day 04 | 文件上传基础、表单校验 |
| Day 06 | MyBatis-Plus QueryWrapper 分页查询、动态条件筛选 |
| Day 07 | 幂等性处理（唯一约束） |
| Day 08 | 订单状态机、`@Scheduled` 定时任务 |
| Day 09 | 信用评分算法 |
| Day 10 | RBAC 权限控制、Spring Security 基础 |
| Day 11 | 容器化部署、Nginx 反向代理、HTTPS |

---

## 七、开发里程碑

| 阶段  | 目标              | 产出               | 状态               |
| --- | --------------- | ---------------- | ---------------- |
| M1  | Day 00 + Day 01 | 项目骨架跑通，数据库连接正常   | 已完成 (･ω･)✧       |
| M2  | Day 02          | Maven 多模块 + 技术基建 | 已完成 ٩(˃̶͈̀௰˂̶͈́) |
| M3  | Day 03 + Day 04 | 用户注册登录 + 身份认证    | 进行中（启动验证待完成）     |
| M4  | Day 06 + Day 07 | 商品浏览 + 购物车收藏     | 待完成              |
| M5  | Day 08          | 完整订单流程           | 待完成              |
| M6  | Day 09 + Day 10 | 评价信用体系 + 管理员后台   | 待完成              |
| M7  | Day 11          | 部署上线             | 待完成              |

---

## 八、简历项目描述模板

```
项目名称：UnSky Market 校园二手交易平台
项目描述：面向高校师生的二手物品交易平台，支持商品发布、搜索、交易全流程。
技术栈：Spring Boot + MyBatis-Plus + Redis + MySQL + JWT
核心功能：
  - 商品发布与浏览（Redis 浏览量缓存）
  - 交易流程（Redis 分布式锁防超卖）
  - 搜索系统（关键词搜索 + 搜索历史 + 热门排行）
  - 订单管理（定时任务超时取消）
个人亮点：
  - 设计并实现 Redis 分布式锁，解决重复下单问题
  - 构建缓存体系，覆盖分类/商品详情/搜索结果三层
  - 完成完整的订单状态机设计与实现
```

---

## 九、执行原则

1. **每天推进一个模块**，不堆积
2. **先跑通再优化**，不要在细节上死磕
3. **遇到问题直接问我**，不自己耗时间
4. **笔记同步整理**，每个模块完成后输出笔记
5. **代码写完测完再继续**，不留下次再改
