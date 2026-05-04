# UnSky Market - 数据库脚本说明

## 📌 项目说明

本目录用于存放 UnSky Market 项目的数据库脚本，按开发阶段（Day）进行组织，涵盖用户体系、认证体系以及商品模块的基础数据结构。

---

## 📦 脚本总览

| Day | 文件 | 说明 |
|-----|------|------|
| Day01 | 001_user_schema.sql | 创建数据库与用户表 |
| Day01 | 002_user_init.sql | 初始化用户测试数据 |
| Day04 | 003_student_cert_schema.sql | 创建学生认证表 |
| Day04 | 004_student_cert_init.sql | 初始化认证测试数据 |
| Day06 | 005_product_category_schema.sql | 创建商品分类表 |
| Day06 | 006_product_category_init.sql | 初始化商品分类数据 |

---

## 🧩 模块说明

---

### 👤 一、用户模块（Day01）

#### 📄 表：`sys_user`

用于存储用户基础信息，支撑注册、登录及认证状态管理。

#### 字段映射关系
| 数据库字段 | Java字段 | 类型 | 说明 |
|------------|----------|------|------|
| id | id | Long | 主键 |
| nickname | nickname | String | 用户昵称 |
| phone | phone | String | 手机号 |
| password | password | String | 加密密码 |
| avatar | avatar | String | 头像 |
| school | school | String | 学校 |
| student_id | studentId | String | 学号 |
| auth_status | authStatus | Byte | 认证状态 |
| credit_score | creditScore | Integer | 信用分 |
| create_time | createTime | LocalDateTime | 创建时间 |


#### 认证状态说明

| 值 | 说明 |
|----|------|
| 0 | 未认证 |
| 1 | 已认证 |
| 2 | 审核中 |

#### 注意事项

- 使用 MyBatis-Plus 自动驼峰映射
- `password` 存储 BCrypt 加密值（不可明文）
- `auth_status` 使用 TINYINT 支持多状态
- `create_time` 使用 DATETIME 保留完整时间

---

### 🎓 二、学生认证模块（Day04）

#### 📄 表：`student_cert`

用于记录用户提交的学生认证申请信息及审核状态。

#### 核心字段

| 字段 | 说明 |
|------|------|
| user_id | 关联用户ID |
| student_name | 学生姓名 |
| school | 学校名称 |
| student_id | 学号 |
| id_card_front | 证件正面 |
| id_card_back | 证件反面 |
| status | 认证状态（0待审核 / 1通过 / 2拒绝） |
| remark | 审核备注 |
| create_time | 申请时间 |

#### 服务场景

- 提交认证申请  
- 查询认证状态  
- 管理员审核  

---

### 🛒 三、商品分类模块（Day06）

#### 📄 表：`product_category`

用于管理商品分类，为商品浏览与筛选提供基础数据。

#### 核心字段

| 字段 | 说明 |
|------|------|
| id | 分类ID |
| name | 分类名称 |
| sort | 排序（越小越靠前） |
| status | 状态（0禁用 / 1启用） |
| create_time | 创建时间 |

#### 服务场景

- 分类列表展示  
- 商品按分类筛选  
- 商品发布选择分类  

---

## ⚙️ 使用方法

在 DataGrip 或 MySQL CLI 中按顺序执行脚本：

```sql
source 001_user_schema.sql;
source 002_user_init.sql;

source 003_student_cert_schema.sql;
source 004_student_cert_init.sql;

source 005_product_category_schema.sql;
source 006_product_category_init.sql;