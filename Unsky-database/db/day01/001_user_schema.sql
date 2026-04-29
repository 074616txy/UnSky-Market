-- ============================================================
-- UnSky Market - Day01 数据库初始化脚本
-- 数据库：unsky_market
-- 对应后端实体：com.Market.Common.Entity.User
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS unsky_market
    DEFAULT CHARSET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE unsky_market;

-- ============================================================
-- 创建用户表 sys_user
-- 字段与 User.java 一一对应：
--   数据库下划线命名  →  Java 驼峰命名（由 MyBatis-Plus 自动映射）
-- ============================================================
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    nickname     VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    phone        VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号（登录账号）',
    password     VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密存储）',
    avatar       VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
    school       VARCHAR(100) DEFAULT NULL COMMENT '学校',
    student_id   VARCHAR(50)  DEFAULT NULL COMMENT '学号',
    auth_status  TINYINT      DEFAULT 0 COMMENT '认证状态（0=未认证 1=已认证 2=审核中）',
    credit_score INT          DEFAULT 100 COMMENT '信用分',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 字段说明对照表
-- ============================================================
-- | 数据库字段       | Java 字段        | 类型           | 说明                    |
-- |----------------|-----------------|--------------|-----------------------|
-- | id             | id              | Long         | 主键自增                 |
-- | nickname       | nickname        | String       | 昵称                    |
-- | phone          | phone           | String       | 手机号，唯一约束             |
-- | password       | password        | String       | BCrypt加密后的密码          |
-- | avatar         | avatar          | String       | 头像URL路径               |
-- | school         | school          | String       | 学校名称                  |
-- | student_id     | studentId       | String       | 学号（MyBatis-Plus自动映射） |
-- | auth_status    | authStatus      | Byte         | 认证状态 0/1/2            |
-- | credit_score   | creditScore     | Integer      | 信用分数                  |
-- | create_time    | createTime      | LocalDateTime| 注册时间（自动填充）          |
