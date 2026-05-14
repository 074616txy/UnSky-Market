# UnSky Market

UnSky Market 是一个面向校园场景的二手交易平台，围绕“学生可信交易”完成了用户注册登录、学生身份认证、商品发布与浏览、收藏与购物车、订单流转、交易评价、信用分、后台管理和 Docker 部署等核心能力。

项目采用前后端分离架构：后端为 Spring Boot 多模块工程，数据库脚本按开发日程分阶段维护；前端为 Vue 3 + Vite + Element Plus 管理与用户双端页面；文档区保留了 Day00-Day11 的完整开发日记，可用于复盘项目从搭建、开发、调试到部署包装的全过程。

## 项目结构

```text
UnSky Market Project
├─ Unsky-backend/          # Spring Boot 后端服务
├─ Unsky-common/           # 公共实体、统一返回、异常处理、工具类
├─ Unsky-database/db/      # MySQL 建表与初始化脚本
├─ Unsky-docs/             # 项目大纲、数据库说明、开发日记与截图附件
├─ deploy/                 # MySQL 初始化与 Nginx 配置
├─ docker-compose.yml      # MySQL + Redis + Backend + Frontend 编排
└─ pom.xml                 # Maven 父工程
```

前端工程位于同级目录 `D:\Develop\UnSky-Market`，构建产物由 `docker-compose.yml` 挂载到 Nginx 容器中。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | Java 11, Spring Boot 2.7.18, Spring Web, MyBatis-Plus 3.5.3.1 |
| 安全 | BCrypt 密码加密, JWT 登录态认证, 全局异常处理 |
| 数据库 | MySQL 8.0, Redis 7 |
| 前端 | Vue 3, Vite, TypeScript, Vue Router, Pinia, Axios, Element Plus |
| 部署 | Docker, Docker Compose, Nginx |
| 文档 | Markdown 项目日记、数据库脚本说明、接口与业务复盘 |

## 功能概览

### 用户端

- 用户注册、登录、JWT 鉴权、当前用户信息查询
- 学生认证提交、认证状态查询与审核结果展示
- 商品分类、商品列表、关键词搜索、商品详情
- 商品发布、修改、删除、我的发布
- 收藏商品、取消收藏、收藏列表
- 加入购物车、移出购物车、购物车列表
- 创建订单、结算、取消、发货、确认收货、订单列表
- 买卖双方交易评价、收到/发出的评价列表

### 管理端

- 管理员登录
- 用户列表、封禁、解封
- 商品列表、违规商品下架
- 学生认证申请列表与审核
- 后台仪表盘与基础管理页面

## 后端模块

后端使用 Maven 聚合工程：

- `Unsky-backend`：业务接口层，包含 `user`、`cert`、`product`、`order`、`review`、`admin` 等模块。
- `Unsky-common`：公共实体类、统一响应 `Result`、JWT 工具、全局异常处理等跨模块能力。

核心包结构：

```text
com.Market
├─ admin      # 管理员登录、用户/商品/认证管理
├─ cert       # 学生身份认证
├─ order      # 订单创建、结算、状态流转
├─ product    # 商品、分类、收藏、购物车
├─ review     # 交易评价与信用分
├─ user       # 用户注册、登录、信息查询
└─ config     # MyBatis-Plus 配置
```

## 接口概览

| 模块 | 基础路径 | 主要接口 |
| --- | --- | --- |
| 用户 | `/api/user` | `POST /register`, `POST /login`, `GET /info` |
| 学生认证 | `/api/cert` | `POST /submit`, `GET /status`, `POST /audit` |
| 商品分类 | `/api/category` | `GET /list` |
| 商品 | `/api/product` | `GET /list`, `GET /detail/{id}`, `POST /publish`, `PUT /update`, `DELETE /delete/{id}`, `GET /my`, `GET /hot` |
| 收藏 | `/api/favorite` | `POST /add/{productId}`, `DELETE /cancel/{productId}`, `GET /list` |
| 购物车 | `/api/cart` | `POST /add/{productId}`, `DELETE /remove/{productId}`, `GET /list` |
| 订单 | `/api/order` | `POST /create`, `POST /checkout`, `GET /list`, `PUT /cancel/{orderId}`, `PUT /ship`, `PUT /confirm/{orderId}` |
| 评价 | `/api/review` | `POST /add`, `GET /received/{userId}`, `GET /sent` |
| 后台 | `/api/admin` | `POST /login`, `GET /users`, `PUT /users/ban/{userId}`, `PUT /users/unban/{userId}`, `GET /products`, `PUT /products/off/{productId}`, `GET /certifications`, `PUT /certifications/audit` |

## 数据库设计

数据库脚本位于 `Unsky-database/db`，按开发日程组织，覆盖建表与初始化数据。

| Day | 脚本 | 内容 |
| --- | --- | --- |
| Day01 | `001_user_schema.sql`, `002_user_init.sql` | 用户表 `sys_user` 与测试用户 |
| Day04 | `003_student_cert_schema.sql`, `004_student_cert_init.sql` | 学生认证表 `student_cert` |
| Day06 | `005_product_category_schema.sql`, `006_product_category_init.sql`, `007_product_schema.sql`, `008_product_init.sql` | 商品分类与商品表 |
| Day07 | `009_favorite_schema.sql`, `010_cart_schema.sql` | 收藏与购物车 |
| Day08 | `011_orders_schema.sql` | 订单表 `orders` |
| Day09 | `012_review_schema.sql` | 评价表 `review` |
| Day10 | `013_admin_schema.sql`, `014_admin_init.sql` | 管理员表 `admin` 与初始化账号 |

主要业务表：

- `sys_user`：用户基础资料、认证状态、信用分、账号状态。
- `student_cert`：学生认证申请、证件图片、审核状态与备注。
- `product_category`：商品分类、排序和启用状态。
- `product`：商品标题、描述、价格、图片、卖家、分类、状态。
- `favorite`：用户收藏商品关系。
- `cart`：用户购物车商品关系。
- `orders`：买家、卖家、商品、金额、订单状态与交易时间。
- `review`：订单评价、评分、评价内容、评价方向。
- `admin`：后台管理员账号、密码和角色。

## 前端页面

前端位于 `D:\Develop\UnSky-Market`，使用 Vue Router 划分用户端和后台端。

用户端页面：

- `/` 首页
- `/product/:id` 商品详情
- `/login` 登录/注册
- `/cert` 学生认证
- `/publish` 发布商品
- `/favorites` 我的收藏
- `/cart` 购物车
- `/orders` 我的订单
- `/reviews` 我的评价

后台页面：

- `/admin/login` 管理员登录
- `/admin` 后台仪表盘
- `/admin/users` 用户管理
- `/admin/products` 商品管理
- `/admin/certifications` 认证审核

## 本地运行

### 后端

准备 MySQL 8.0，并创建数据库 `unsky_market`。开发配置默认连接：

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/unsky_market
    username: root
    password: 123456
```

按顺序执行 `Unsky-database/db` 下的 SQL 脚本后，启动后端：

```bash
mvn clean package
mvn -pl Unsky-backend spring-boot:run
```

### 前端

```bash
cd ../UnSky-Market
npm install
npm run dev
```

### Docker Compose

项目提供 MySQL、Redis、后端和 Nginx 前端的编排配置：

```bash
docker compose up -d --build
```

默认端口：

- 后端：`http://localhost:8081`
- 前端 Nginx：`http://localhost:8082`
- MySQL：`localhost:3307`
- Redis：`localhost:6379`

## 开发日记摘要

完整项目日记位于 `Unsky-docs/UnSky Market项目日记`，总量约 25 万字，记录了需求拆解、工程搭建、接口设计、数据库演进、调试过程、错误修复和阶段复盘。README 仅保留压缩版时间线：

| 阶段 | 主题 | 产出 |
| --- | --- | --- |
| Day00 | 工具配置与开工准备 | 开发环境、Git、IDE、数据库工具准备 |
| Day01 | 骨架搭建与数据库连接 | Maven 多模块、Spring Boot 启动、MySQL 连通 |
| Day02 | 工程化与技术基建 | 统一响应、异常处理、配置整理、工程规范 |
| Day03 | 用户体系 | 注册、登录、密码加密、JWT、用户信息接口 |
| Day03 补强 | 用户认证与安全补强 | 登录态、异常场景、安全细节完善 |
| Day04 | 学生身份认证 | 认证提交、状态查询、审核流、认证数据表 |
| Day06 | 商品浏览与搜索过滤 | 分类、商品列表、详情、筛选与基础查询 |
| Day06 补强 | 商品模块扩展 | 发布、修改、删除、我的商品、热门商品 |
| Day07 | 购物车与收藏 | 收藏关系、购物车关系、重复操作处理 |
| Day08 | 订单流程 | 创建订单、结算、取消、发货、确认收货 |
| Day09 | 信用体系与评价 | 交易评价、评分、信用管理逻辑 |
| Day10 | 后台管理 | 管理员登录、用户管理、商品下架、认证审核 |
| Day11 | 部署规划与项目包装 | Docker Compose、Nginx、项目收尾说明 |

## 提交历程

项目主线共 15 次提交，按功能逐步推进：

- 初始化 Day00-Day02 工程框架
- 完成用户注册登录与 JWT 认证
- 完成学生认证模块
- 完成商品模块与补强
- 完成收藏、购物车、订单、评价
- 完成后台管理模块
- 补充数据库脚本、部署配置和项目日记

## 当前状态

项目已经形成较完整的校园二手交易平台闭环：用户从注册、认证、发布商品、下单交易到评价信用均有对应后端接口、数据库结构和前端页面；管理员可对用户、商品与学生认证进行基础治理。后续可继续扩展真实图片上传、支付沙箱、消息通知、搜索排序优化、权限拦截器和接口自动化测试。
