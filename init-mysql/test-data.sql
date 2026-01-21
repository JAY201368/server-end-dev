-- 插入测试数据脚本
-- 运行此脚本之前，确保数据库已初始化

USE student_sys;

-- 清空现有测试数据（可选）
-- DELETE FROM sys_user_role;
-- DELETE FROM sys_role_permission;
-- DELETE FROM sys_user WHERE username != 'admin';

-- 插入管理员用户（密码: admin123）
-- BCrypt 加密后的密码
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$WBGOV0gR8L6KF7Kg37VaKePAT1GhXPbIJA9hAz2a1k.t1eUksAqam', '系统管理员', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE password='$2a$10$WBGOV0gR8L6KF7Kg37VaKePAT1GhXPbIJA9hAz2a1k.t1eUksAqam';

-- 插入测试教师用户（密码: teacher123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('teacher', '$2a$10$XFOcETEAvjkAu.t8OdhSVONPejCMkipZIRdALJ6y9Kr3kep7Gp6wa', '张教师', 'teacher@example.com', 1)
ON DUPLICATE KEY UPDATE password='$2a$10$XFOcETEAvjkAu.t8OdhSVONPejCMkipZIRdALJ6y9Kr3kep7Gp6wa';

-- 插入测试学生用户（密码: student123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('student', '$2a$10$cLZQ.fekHR4RzZka9x/9UOTN4s865UBie18JJcnqZaB3ByGug45gC', '李同学', 'student@example.com', 1)
ON DUPLICATE KEY UPDATE password='$2a$10$cLZQ.fekHR4RzZka9x/9UOTN4s865UBie18JJcnqZaB3ByGug45gC';

-- 获取用户ID
SET @admin_id = (SELECT id FROM sys_user WHERE username = 'admin');
SET @teacher_id = (SELECT id FROM sys_user WHERE username = 'teacher');
SET @student_id = (SELECT id FROM sys_user WHERE username = 'student');

-- 获取角色ID
SET @admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'ROLE_ADMIN');
SET @teacher_role_id = (SELECT id FROM sys_role WHERE role_code = 'ROLE_TEACHER');
SET @student_role_id = (SELECT id FROM sys_role WHERE role_code = 'ROLE_STUDENT');

-- 插入用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(@admin_id, @admin_role_id)
ON DUPLICATE KEY UPDATE user_id=@admin_id;

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(@teacher_id, @teacher_role_id)
ON DUPLICATE KEY UPDATE user_id=@teacher_id;

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(@student_id, @student_role_id)
ON DUPLICATE KEY UPDATE user_id=@student_id;

-- 插入权限数据
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `url`, `method`, `description`) VALUES
('查看用户', 'user:view', 'api', '/user/**', 'GET', '查看用户信息'),
('新增用户', 'user:add', 'api', '/user', 'POST', '新增用户'),
('修改用户', 'user:edit', 'api', '/user', 'PUT', '修改用户信息'),
('删除用户', 'user:delete', 'api', '/user/*', 'DELETE', '删除用户'),
('查看学生', 'student:view', 'api', '/student/**', 'GET', '查看学生信息'),
('新增学生', 'student:add', 'api', '/student', 'POST', '新增学生'),
('修改学生', 'student:edit', 'api', '/student', 'PUT', '修改学生信息'),
('删除学生', 'student:delete', 'api', '/student/*', 'DELETE', '删除学生'),
('管理成绩', 'score:manage', 'api', '/score/**', 'ALL', '管理成绩')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 获取权限ID
SET @perm_user_view = (SELECT id FROM sys_permission WHERE permission_code = 'user:view');
SET @perm_user_add = (SELECT id FROM sys_permission WHERE permission_code = 'user:add');
SET @perm_user_edit = (SELECT id FROM sys_permission WHERE permission_code = 'user:edit');
SET @perm_user_delete = (SELECT id FROM sys_permission WHERE permission_code = 'user:delete');
SET @perm_student_view = (SELECT id FROM sys_permission WHERE permission_code = 'student:view');
SET @perm_student_add = (SELECT id FROM sys_permission WHERE permission_code = 'student:add');
SET @perm_student_edit = (SELECT id FROM sys_permission WHERE permission_code = 'student:edit');
SET @perm_student_delete = (SELECT id FROM sys_permission WHERE permission_code = 'student:delete');
SET @perm_score_manage = (SELECT id FROM sys_permission WHERE permission_code = 'score:manage');

-- 管理员角色拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(@admin_role_id, @perm_user_view),
(@admin_role_id, @perm_user_add),
(@admin_role_id, @perm_user_edit),
(@admin_role_id, @perm_user_delete),
(@admin_role_id, @perm_student_view),
(@admin_role_id, @perm_student_add),
(@admin_role_id, @perm_student_edit),
(@admin_role_id, @perm_student_delete),
(@admin_role_id, @perm_score_manage)
ON DUPLICATE KEY UPDATE role_id=@admin_role_id;

-- 教师角色拥有查看、新增、编辑学生和管理成绩权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(@teacher_role_id, @perm_student_view),
(@teacher_role_id, @perm_student_add),
(@teacher_role_id, @perm_student_edit),
(@teacher_role_id, @perm_score_manage)
ON DUPLICATE KEY UPDATE role_id=@teacher_role_id;

-- 学生角色只有查看自己信息的权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(@student_role_id, @perm_student_view)
ON DUPLICATE KEY UPDATE role_id=@student_role_id;

SELECT '测试数据插入完成！' AS message;
SELECT '测试账号:' AS info;
SELECT 'admin / admin123' AS '管理员账号';
SELECT 'teacher / teacher123' AS '教师账号';
SELECT 'student / student123' AS '学生账号';
