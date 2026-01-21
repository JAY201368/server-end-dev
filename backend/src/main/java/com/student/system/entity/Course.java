package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体类
 * 对应数据库表: course
 *
 * @author Student System
 * @since 2024
 */
@Data
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID - 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程编号 - 唯一标识
     */
    @TableField("course_code")
    private String courseCode;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 学分
     */
    @TableField("credit")
    private BigDecimal credit;

    /**
     * 任课教师
     */
    @TableField("teacher")
    private String teacher;

    /**
     * 课程描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态: 0-停用, 1-启用
     */
    @TableField("status")
    private Integer status;

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
