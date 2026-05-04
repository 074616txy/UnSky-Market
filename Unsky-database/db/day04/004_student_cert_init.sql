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