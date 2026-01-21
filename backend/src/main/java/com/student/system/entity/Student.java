package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生实体类
 * 对应数据库表: student
 *
 * @author Student System
 * @since 2024
 */
@Data
@TableName("student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学生ID - 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学号 - 唯一标识
     */
    @TableField("student_no")
    private String studentNo;

    /**
     * 学生姓名
     */
    @TableField("name")
    private String name;

    /**
     * 性别: 0-女, 1-男
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 出生日期
     */
    @TableField("birth_date")
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱地址
     */
    @TableField("email")
    private String email;

    /**
     * 头像URL - 用于前端图片展示
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 班级ID
     */
    @TableField("class_id")
    private Long classId;

    /**
     * 入学日期
     */
    @TableField("enrollment_date")
    private LocalDate enrollmentDate;

    /**
     * 状态: 0-休学, 1-在读, 2-毕业, 3-退学
     */
    @TableField("status")
    private Integer status;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     * 使用MyBatis-Plus逻辑删除功能
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

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
