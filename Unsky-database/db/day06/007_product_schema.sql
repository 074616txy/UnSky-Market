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