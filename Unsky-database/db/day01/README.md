# Day01 - 用户数据库说明

## 文件清单

| 文件 | 说明 |
|------|------|
| `001_schema.sql.sql` | 创建数据库 `unsky_market` 和用户表 `sys_user` |
| `002_init_data.sql.sql` | 插入 5 条测试用户数据（密码均为 `123456`） |


## 字段与后端对照

```
数据库字段       →  Java 字段         →  User.java
─────────────────────────────────────────────────
id              →  id               →  Long id
nickname        →  nickname         →  String nickname
phone           →  phone            →  String phone
password        →  password         →  String password
avatar          →  avatar           →  String avatar
school          →  school           →  String school
student_id      →  studentId        →  String studentId
auth_status     →  authStatus       →  Byte authStatus
credit_score    →  creditScore      →  Integer creditScore
create_time     →  createTime       →  LocalDateTime createTime
```

## 认证状态值说明

| 值 | 说明 |
|----|------|
| 0 | 未认证 |
| 1 | 已认证 |
| 2 | 审核中 |

## 使用方法

在 DataGrip 或 MySQL CLI 中按顺序执行：

```sql
-- 1. 执行建库建表
source 001_schema.sql.sql;

-- 2. 插入测试数据
source 002_init_data.sql.sql;
```

## 注意事项

- `student_id`（数据库下划线）→ `studentId`（Java 驼峰）由 MyBatis-Plus 的 `map-underscore-to-camel-case: true` 自动映射
- `password` 字段存储 BCrypt 加密后的值，**不要存明文**
- `auth_status` 使用 `TINYINT` 而非 `BOOLEAN`，因为有三种状态值
- `create_time` 使用 `DATETIME` 而非 `DATE`，保留时分秒
