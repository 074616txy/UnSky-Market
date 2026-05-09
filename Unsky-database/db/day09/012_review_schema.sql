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
