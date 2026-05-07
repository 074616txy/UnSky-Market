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
