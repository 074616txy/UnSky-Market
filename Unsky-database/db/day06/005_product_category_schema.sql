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