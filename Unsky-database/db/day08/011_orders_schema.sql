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
