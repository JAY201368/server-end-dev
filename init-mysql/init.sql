-- 学生信息管理系统初始化SQL脚本
-- 数据库: student_sys

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 创建表结构
-- ----------------------------

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(加密)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色代码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限代码',
  `resource_type` VARCHAR(20) DEFAULT NULL COMMENT '资源类型: menu/button/api',
  `url` VARCHAR(200) DEFAULT NULL COMMENT 'URL',
  `method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 学生表
CREATE TABLE IF NOT EXISTS `student` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `student_no` VARCHAR(20) NOT NULL COMMENT '学号',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `gender` TINYINT DEFAULT NULL COMMENT '性别: 0-女 1-男',
  `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `class_id` BIGINT DEFAULT NULL COMMENT '班级ID',
  `enrollment_date` DATE DEFAULT NULL COMMENT '入学日期',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-休学 1-在读 2-毕业 3-退学',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_code` VARCHAR(20) NOT NULL COMMENT '课程编号',
  `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `credit` DECIMAL(3,1) DEFAULT NULL COMMENT '学分',
  `teacher` VARCHAR(50) DEFAULT NULL COMMENT '任课教师',
  `description` TEXT COMMENT '课程描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 成绩表
CREATE TABLE IF NOT EXISTS `score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `score` DECIMAL(5,2) DEFAULT NULL COMMENT '成绩',
  `semester` VARCHAR(20) DEFAULT NULL COMMENT '学期',
  `exam_date` DATE DEFAULT NULL COMMENT '考试日期',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_course_semester` (`student_id`, `course_id`, `semester`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
  `operation` VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '操作地点',
  `execute_time` INT DEFAULT NULL COMMENT '执行时长(ms)',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
  `error_msg` TEXT COMMENT '错误信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 接口访问统计表
CREATE TABLE IF NOT EXISTS `api_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `api_path` VARCHAR(200) NOT NULL COMMENT 'API路径',
  `method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
  `total_count` BIGINT DEFAULT 0 COMMENT '总访问次数',
  `success_count` BIGINT DEFAULT 0 COMMENT '成功次数',
  `fail_count` BIGINT DEFAULT 0 COMMENT '失败次数',
  `avg_time` BIGINT DEFAULT 0 COMMENT '平均耗时(ms)',
  `max_time` BIGINT DEFAULT 0 COMMENT '最大耗时(ms)',
  `min_time` BIGINT DEFAULT 0 COMMENT '最小耗时(ms)',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_date` (`api_path`, `method`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口访问统计表';

-- ----------------------------
-- 插入初始数据
-- ----------------------------

-- 插入默认角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限'),
('教师', 'ROLE_TEACHER', '教师角色，可以管理学生和成绩'),
('学生', 'ROLE_STUDENT', '学生角色，可以查看自己的信息和成绩')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- 插入默认用户 (密码: admin123, 需要在应用中使用BCrypt加密)
-- 这里使用明文，实际应用中需要使用加密后的密码
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname);

-- 插入示例课程
INSERT INTO `course` (`course_code`, `course_name`, `credit`, `teacher`, `description`) VALUES
('CS101', '计算机基础', 3.0, '张教授', '计算机科学与技术基础课程'),
('MATH101', '高等数学', 4.0, '李教授', '数学分析基础课程'),
('ENG101', '大学英语', 2.0, '王老师', '英语基础课程'),
('PHYS101', '大学物理', 3.5, '赵教授', '物理学基础课程')
ON DUPLICATE KEY UPDATE course_name=VALUES(course_name);

SET FOREIGN_KEY_CHECKS = 1;

-- 完成初始化
SELECT 'Database initialization completed successfully!' AS message;
