-- 权限初始化SQL脚本
-- 插入系统权限和角色数据

SET NAMES utf8mb4;

-- ----------------------------
-- 插入权限数据
-- ----------------------------

-- 用户管理权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `url`, `method`, `description`) VALUES
('查看用户', 'system:user:view', 'api', '/user/**', 'GET', '查看用户信息'),
('添加用户', 'system:user:add', 'api', '/user/add', 'POST', '添加新用户'),
('编辑用户', 'system:user:edit', 'api', '/user/update', 'PUT', '编辑用户信息'),
('删除用户', 'system:user:delete', 'api', '/user/{id}', 'DELETE', '删除用户'),
('分配角色', 'system:user:assign-role', 'api', '/role/assign-to-user', 'POST', '为用户分配角色')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 角色管理权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `url`, `method`, `description`) VALUES
('查看角色', 'system:role:view', 'api', '/role/**', 'GET', '查看角色信息'),
('添加角色', 'system:role:add', 'api', '/role/add', 'POST', '添加新角色'),
('编辑角色', 'system:role:edit', 'api', '/role/update', 'PUT', '编辑角色信息'),
('删除角色', 'system:role:delete', 'api', '/role/{id}', 'DELETE', '删除角色'),
('分配权限', 'system:role:assign', 'api', '/role/assign-permissions', 'POST', '为角色分配权限')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 权限管理权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `url`, `method`, `description`) VALUES
('查看权限', 'system:permission:view', 'api', '/permission/**', 'GET', '查看权限信息'),
('添加权限', 'system:permission:add', 'api', '/permission/add', 'POST', '添加新权限'),
('编辑权限', 'system:permission:edit', 'api', '/permission/update', 'PUT', '编辑权限信息'),
('删除权限', 'system:permission:delete', 'api', '/permission/{id}', 'DELETE', '删除权限')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 学生管理权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `url`, `method`, `description`) VALUES
('查看学生', 'student:view', 'api', '/student/**', 'GET', '查看学生信息'),
('添加学生', 'student:add', 'api', '/student/add', 'POST', '添加新学生'),
('编辑学生', 'student:edit', 'api', '/student/update', 'PUT', '编辑学生信息'),
('删除学生', 'student:delete', 'api', '/student/{id}', 'DELETE', '删除学生')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 成绩管理权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `url`, `method`, `description`) VALUES
('查看成绩', 'score:view', 'api', '/score/**', 'GET', '查看成绩信息'),
('录入成绩', 'score:add', 'api', '/score/save', 'POST', '录入新成绩'),
('编辑成绩', 'score:edit', 'api', '/score/update', 'PUT', '编辑成绩信息'),
('删除成绩', 'score:delete', 'api', '/score/{id}', 'DELETE', '删除成绩'),
('查看排行榜', 'score:ranking', 'api', '/score/ranking', 'GET', '查看成绩排行榜')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 监控管理权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `url`, `method`, `description`) VALUES
('查看监控', 'monitor:view', 'api', '/monitor/**', 'GET', '查看系统监控信息'),
('清空统计', 'monitor:clear', 'api', '/monitor/stats/clear', 'GET', '清空监控统计数据')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- ----------------------------
-- 为角色分配权限
-- ----------------------------

-- 管理员角色拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM sys_role WHERE role_code = 'ROLE_ADMIN'),
    id
FROM sys_permission
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 教师角色权限（学生管理、成绩管理、查看监控）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM sys_role WHERE role_code = 'ROLE_TEACHER'),
    id
FROM sys_permission
WHERE permission_code IN (
    'student:view', 'student:add', 'student:edit',
    'score:view', 'score:add', 'score:edit', 'score:ranking',
    'monitor:view'
)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 学生角色权限（查看学生信息、查看成绩、查看排行榜）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM sys_role WHERE role_code = 'ROLE_STUDENT'),
    id
FROM sys_permission
WHERE permission_code IN (
    'student:view',
    'score:view', 'score:ranking'
)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- ----------------------------
-- 为默认管理员分配角色
-- ----------------------------

-- 为 admin 用户分配管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT 
    (SELECT id FROM sys_user WHERE username = 'admin'),
    (SELECT id FROM sys_role WHERE role_code = 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- 完成权限初始化
SELECT 'Permission initialization completed successfully!' AS message;
