package com.student.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志消息 DTO
 * 用于 Kafka 消息传递
 *
 * @author Student System
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作目标ID
     */
    private Long targetId;

    /**
     * 操作目标信息
     */
    private String targetInfo;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 备注信息
     */
    private String remark;
}
