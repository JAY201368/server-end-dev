package com.student.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 学生数据传输对象
 * 用于接收前端提交的学生数据
 *
 * @author Student System
 * @since 2024
 */
@Data
public class StudentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学生ID（更新时需要）
     */
    private Long id;

    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    private String studentNo;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 性别: 0-女, 1-男
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 入学日期
     */
    private LocalDate enrollmentDate;

    /**
     * 状态: 0-休学, 1-在读, 2-毕业, 3-退学
     */
    private Integer status;
}
