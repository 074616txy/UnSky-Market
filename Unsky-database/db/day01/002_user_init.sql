-- ============================================================
-- UnSky Market - Day01 测试数据
-- 说明：密码均为 BCrypt 加密后的值，明文密码为 123456
-- 生成方式：在 Java 中使用 new BCryptPasswordEncoder().encode("123456")（后续学习中）
-- ============================================================

USE unsky_market;

-- 插入测试用户（密码明文：123456）
INSERT INTO sys_user (nickname, phone, password, avatar, school, student_id, auth_status, credit_score)
VALUES
    -- 普通用户

    ('小明同学',  '13800138001', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL, '清华大学',     '20230002', 1, 100),
    ('张三丰',    '13800138002', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL, '武当大学',     '20230003', 0, 100),
    -- 认证中用户
    ('李四光',    '13800138003', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL, '少林大学',     '20230004', 2, 95),
    -- 信用分异常用户
    ('王五爷',    '13800138004', '$2a$10$pg5wDP41Awqpipb13o7uUefagUIo2zgDG2NiMPNs8N8YtvJvh5KhS', NULL, '华山大学',     '20230005', 1, 70);
