-- ============================================================
-- UnSky Market - Day04 学生身份认证建表脚本
-- 数据库：unsky_market
-- 对应后端实体：StudentCert
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================================

USE unsky_market;

-- ============================================================
-- 创建学生认证表 student_cert
-- 字段与学生认证业务一一对应：
--   数据库下划线命名  ->  Java 驼峰命名（由 MyBatis-Plus 自动映射）
-- ============================================================
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
