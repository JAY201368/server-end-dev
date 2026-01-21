package com.student.system.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成绩录入DTO
 * 用于接收前端提交的成绩数据
 *
 * @author Student System
 * @since 2024
 */
@Data
public class ScoreDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成绩ID（更新时需要）
     */
    private Long id;

    /**
     * 学生ID
     */
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 成绩分数（0-100）
     */
    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0.00", message = "成绩不能低于0分")
    @DecimalMax(value = "100.00", message = "成绩不能超过100分")
    private BigDecimal score;

    /**
     * 学期（例如: 2024-1）
     */
    @NotBlank(message = "学期不能为空")
    private String semester;

    /**
     * 考试日期
     */
    private LocalDate examDate;

    /**
     * 备注
     */
    private String remark;
}
