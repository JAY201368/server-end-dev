package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志实体类
 * 对应数据库表: sys_log
 *
 * @author Student System
 * @since 2024
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID - 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作类型（如：DELETE_STUDENT, UPDATE_STUDENT等）
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作人
     */
    @TableField("operator")
    private String operator;

    /**
     * 操作目标ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 操作目标信息（JSON格式）
     */
    @TableField("target_info")
    private String targetInfo;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 操作结果：SUCCESS/FAILURE
     */
    @TableField("result")
    private String result;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间 - 自动填充
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
