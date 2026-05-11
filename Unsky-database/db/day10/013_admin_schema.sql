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