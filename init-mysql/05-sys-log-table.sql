-- 系统日志表
CREATE TABLE IF NOT EXISTS `sys_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型（如：DELETE_STUDENT, UPDATE_STUDENT等）',
    `operator` VARCHAR(100) NOT NULL COMMENT '操作人',
    `target_id` BIGINT COMMENT '操作目标ID',
    `target_info` VARCHAR(500) COMMENT '操作目标信息（JSON格式）',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `result` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILURE',
    `remark` TEXT COMMENT '备注信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_operator` (`operator`),
    INDEX `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';
