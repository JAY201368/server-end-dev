package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成绩实体类
 * 对应数据库表: score
 *
 * @author Student System
 * @since 2024
 */
@Data
@TableName("score")
public class Score implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成绩ID - 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID - 外键关联student表
     */
    @TableField("student_id")
    private Long studentId;

    /**
     * 课程ID - 外键关联course表
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 成绩分数
     * 使用BigDecimal保证精度
     */
    @TableField("score")
    private BigDecimal score;

    /**
     * 学期 (例如: 2024-1, 2024-2)
     */
    @TableField("semester")
    private String semester;

    /**
     * 考试日期
     */
    @TableField("exam_date")
    private LocalDate examDate;

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

    /**
     * 更新时间 - 自动填充
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
