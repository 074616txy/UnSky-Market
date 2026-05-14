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
    status       TINYINT DEFAULT 1 COMMENT '账号状态（0=封禁 1=正常）',
    auth_status  TINYINT      DEFAULT 0 COMMENT '认证状态（0=未认证 1=已认证 2=审核中）',
    credit_score INT          DEFAULT 100 COMMENT '信用分',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 字段说明对照表
-- ============================================================
-- | 数据库字段       | Java 字段        | 类型           | 说明                    |
-- |----------------|-----------------|--------------|-----------------------  |
-- | id             | id              | Long         | 主键自增                  |
-- | nickname       | nickname        | String       | 昵称                     |
-- | phone          | phone           | String       | 手机号，唯一约束            |
-- | password       | password        | String       | BCrypt加密后的密码         |
-- | avatar         | avatar          | String       | 头像URL路径               |
-- | school         | school          | String       | 学校名称                  |
-- | student_id     | studentId       | String       | 学号（MyBatis-Plus自动映射）|
-- | status         | status          | Byte         | 帐号状态 0/1/2            |
-- | auth_status    | authStatus      | Byte         | 认证状态 0/1/2            |
-- | credit_score   | creditScore     | Integer      | 信用分数                  |
-- | create_time    | createTime      | LocalDateTime| 注册时间（自动填充）         |

-- ============================================================
-- UnSky Market - Day01 测试数据
-- 说明：密码均为 BCrypt 加密后的值，明文密码为 123456
-- 生成方式：在 Java 中使用 new BCryptPasswordEncoder().encode("123456")（后续学习中）
-- ============================================================

USE unsky_market;

-- 插入测试用户（密码明文：123456）
INSERT INTO sys_user (nickname, phone, password, avatar, school, student_id,status, auth_status, credit_score)
VALUES
    -- 普通用户
    ('天下云', '13800138000', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL,'bilibili大学', '20230001', 1, 1,100),
    ('小明同学',  '13800138001', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL, '清华大学',  '20230002', 1,1, 100),
    ('张三丰',    '13800138002', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL, '武当大学',  '20230003', 1,0, 100),
    ('李四光',    '13800138003', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL, '少林大学',  '20230004', 1,2, 95),
    ('王五爷',    '13800138004', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL, '华山大学',  '20230005', 1,1, 70);

-- ============================================================
-- UnSky Market - Day04 学生身份认证建表脚本
-- 数据库：unsky_market
-- 对应后端实体：StudentCert
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================================

USE unsky_market;
DROP TABLE IF EXISTS student_cert;

CREATE TABLE student_cert (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    user_id        BIGINT       NOT NULL COMMENT '关联用户ID',
    student_name   VARCHAR(50)  NOT NULL COMMENT '学生姓名',
    school         VARCHAR(100) NOT NULL COMMENT '学校名称',
    student_id     VARCHAR(50)  NOT NULL COMMENT '学号',
    id_card_front  VARCHAR(255) NOT NULL COMMENT '证件正面图片路径',
    id_card_back   VARCHAR(255) NOT NULL COMMENT '证件反面图片路径',
    status         TINYINT      DEFAULT 0 COMMENT '认证状态（0=待审核，1=审核通过，2=审核拒绝）',
    remark         VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生认证表';

-- ============================================================
-- 字段说明对照表
-- ============================================================
-- | 数据库字段       | Java 字段       | 类型           | 说明                         |
-- |----------------|-----------------|--------------|----------------------------|
-- | id             | id              | Long         | 主键自增                     |
-- | user_id        | userId          | Long         | 关联用户ID                   |
-- | student_name   | studentName     | String       | 学生姓名                     |
-- | school         | school          | String       | 学校名称                     |
-- | student_id     | studentId       | String       | 学号（MyBatis-Plus自动映射） |
-- | id_card_front  | idCardFront     | String       | 证件正面图片路径             |
-- | id_card_back   | idCardBack      | String       | 证件反面图片路径             |
-- | status         | status          | Byte         | 认证状态：0/1/2              |
-- | remark         | remark          | String       | 审核备注                     |
-- | create_time    | createTime      | LocalDateTime| 申请时间（自动填入）         |

-- ============================================================
-- UnSky Market - Day04 学生认证初始化数据
-- 表：student_cert
-- 说明：用于测试认证流程的模拟数据
-- ============================================================

USE unsky_market;

-- 清空表（开发阶段推荐）
TRUNCATE TABLE student_cert;

-- ============================================================
-- 插入测试数据
-- 状态说明：
-- 0 = 审核中
-- 1 = 已通过
-- 2 = 已拒绝
-- ============================================================

INSERT INTO student_cert
(user_id, student_name, school, student_id, id_card_front, id_card_back, status, remark, create_time)
VALUES
-- 审核中
(1, '天下云', 'bilibili大学', '20230001', 'front-test-1.png', 'back-test-1.png', 0, '首次提交认证', NOW()),

-- 已通过
(2, '小明同学', '清华大学', '20230002', 'front-test-2.png', 'back-test-2.png', 1, '审核通过', NOW()),

-- 已拒绝
(3, '张三丰', '武当大学', '20230003', 'front-test-3.png', 'back-test-3.png', 2, '信息不清晰，已拒绝', NOW()),

-- 审核中
(4, '李四光', '少林大学', '20230004', 'front-test-4.png', 'back-test-4.png', 0, '等待审核', NOW()),

-- 已通过
(5, '王五爷', '华山大学', '20230005', 'front-test-5.png', 'back-test-5.png', 1, '认证成功', NOW());
-- =============================================
-- UnSky Market - Day06 分类列表建表脚本
-- 数据库：unsky_market
-- 对应后端实体：ProductCategory
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- =============================================
CREATE TABLE product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',

    name VARCHAR(50) NOT NULL COMMENT '分类名称',

    sort INT DEFAULT 0 COMMENT '排序字段（值越小越靠前）',

    status TINYINT DEFAULT 1 COMMENT '状态（1=启用，0=禁用）',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='商品分类表';
-- =============================================
-- UnSky Market - Day06 分类列表初始化数据
-- 表：product_category
-- 说明：用于测试分类列表的模拟数据
-- =============================================
INSERT INTO product_category (name, sort, status)
VALUES
('数码', 1, 1),
('教材', 2, 1),
('生活', 3, 1),
('衣物', 4, 1),
('虚拟物品', 5, 1),
('其他', 6, 1);
-- ============================================
-- UnSky Market - Day06 商品信息建表脚本
-- 数据库：unsky_market
-- 对应后端实体：product
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================

DROP TABLE IF EXISTS product;

CREATE TABLE product (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  seller_id BIGINT NOT NULL COMMENT '卖家ID（关联sys_user.id）',

  title VARCHAR(128) NOT NULL COMMENT '商品标题',
  description TEXT COMMENT '商品描述',

  price DECIMAL(10,2) NOT NULL COMMENT '售价',
  original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',

  category_id BIGINT NOT NULL COMMENT '分类ID（关联product_category.id）',

  condition_level TINYINT DEFAULT 5 COMMENT '新旧程度（1~5）',

  images TEXT COMMENT '商品图片（JSON数组）',

  view_count INT DEFAULT 0 COMMENT '浏览量',
  favorite_count INT DEFAULT 0 COMMENT '收藏量',

  status TINYINT DEFAULT 1 COMMENT '状态（1-上架 2-下架 3-已售）',

  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',

 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (id),

  INDEX idx_category_id (category_id),
  INDEX idx_seller_id (seller_id),
  INDEX idx_status (status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
-- ============================================
-- UnSky Market - Day06 商品信息初始化数据
-- 表：product
-- 说明：用于测试商品信息的模拟数据
-- ============================================

INSERT INTO product
    (seller_id, title, description, price, original_price, category_id, condition_level, images, view_count, favorite_count, status)
VALUES

(1, '二手 iPhone 18 128G', '成色良好，无拆修，正常使用痕迹', 3200.00, 5999.00, 1, 4, '["img/iphone1.png","img/iphone2.png"]', 120, 15, 1),

(2, '小米笔记本 Pro', '轻薄办公本，性能稳定，适合学生使用', 2800.00, 4999.00, 2, 4, '["img/laptop1.png"]', 85, 10, 1),

(3, '二手山地自行车', '骑行顺畅，适合校园代步', 300.00, 800.00, 3, 3, '["img/bike1.png"]', 60, 5, 1),

(1, '卖葱机械键盘（青轴）', '手感清脆，灯光正常，无损坏', 150.00, 399.00, 4, 4, '["img/keyboard1.png"]', 45, 8, 1),

(4, '小燕考研英语资料全套', '包含历年真题+解析，几乎全新', 80.00, 200.00, 5, 5, '["img/book1.png"]', 30, 6, 1),

(2, '二手kfc显示器 24寸', '1080P高清，办公/游戏均可', 400.00, 899.00, 2, 4, '["img/monitor1.png"]', 70, 9, 1),

(3, '闲置滑板', '适合新手练习，带护具', 120.00, 300.00, 3, 3, '["img/skate1.png"]', 25, 3, 2),

(5, '游戏手柄YBOX', '支持PC/手机，蓝牙连接', 90.00, 199.00, 4, 4, '["img/controller1.png"]', 40, 7, 1);
-- ============================================
-- UnSky Market - Day07 收藏表建表脚本
-- 数据库：unsky_market
-- 对应后端实体：favorite
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================

CREATE TABLE favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',

    UNIQUE KEY uk_user_product (user_id, product_id),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id)
) COMMENT = '商品收藏表';

-- ============================================
-- UnSky Market - Day07 购物车建表脚本
-- 数据库：unsky_market
-- 对应后端实体：cart
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================

CREATE TABLE cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入购物车时间',

    UNIQUE KEY uk_user_product (user_id, product_id),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id)
) COMMENT = '购物车表';

-- ============================================
-- UnSky Market - Day08 订单表建表脚本
-- 数据库：unsky_market
-- 对应后端实体：orders
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',

    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',

    buyer_id BIGINT NOT NULL COMMENT '买家ID',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',

    total_price DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',

    status INT NOT NULL DEFAULT 0 COMMENT '订单状态：0待付款，1待发货，2待收货，3已完成，-1已取消',

    logistics_no VARCHAR(100) DEFAULT NULL COMMENT '物流单号',

    expire_time DATETIME DEFAULT NULL COMMENT '订单过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_order_no (order_no),
    KEY idx_buyer_id (buyer_id),
    KEY idx_seller_id (seller_id),
    KEY idx_product_id (product_id),
    KEY idx_status (status)
) COMMENT = '订单表';

-- ============================================
-- UnSky Market - Day09 评价表建表脚本
-- 数据库：unsky_market
-- 对应后端实体：review
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================

CREATE TABLE review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',

    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',

    from_user_id BIGINT NOT NULL COMMENT '评价人ID',
    to_user_id BIGINT NOT NULL COMMENT '被评价人ID',

    score INT NOT NULL COMMENT '评分：1-5分',
    content VARCHAR(500) DEFAULT NULL COMMENT '评价内容',

    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名：0不匿名，1匿名',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_order_from_user (order_id, from_user_id),
    KEY idx_order_id (order_id),
    KEY idx_product_id (product_id),
    KEY idx_from_user_id (from_user_id),
    KEY idx_to_user_id (to_user_id)
) COMMENT = '交易评价表';

-- ============================================
-- UnSky Market - Day10 管理员表建表脚本
-- 数据库：unsky_market
-- 对应后端实体：admin
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================

CREATE TABLE admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL COMMENT '管理员账号',
    password VARCHAR(100) NOT NULL COMMENT '管理员密码',
    role VARCHAR(50) DEFAULT 'ADMIN' COMMENT '管理员角色',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_username (username)
) COMMENT = '管理员表';
-- ============================================
-- UnSky Market - Day10 管理员账号初始化数据
-- 表：admin
-- 说明：用于插入一个管理员账号
-- ============================================

INSERT INTO admin (username, password, role)
VALUES ('Skyron', '$2a$10$VqTvnmKUemB..abPsQRDxeOLK08DwXKjk3HlMDakgvak6Wo4r7kY.', 'ADMIN');

