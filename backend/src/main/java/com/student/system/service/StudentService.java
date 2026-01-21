package com.student.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.system.entity.Student;

/**
 * 学生服务接口
 * 继承MyBatis-Plus的IService接口，提供基础CRUD服务
 *
 * @author Student System
 * @since 2024
 */
public interface StudentService extends IService<Student> {

    /**
     * 根据学号查询学生
     *
     * @param studentNo 学号
     * @return 学生对象
     */
    Student getByStudentNo(String studentNo);

    /**
     * 检查学号是否已存在
     *
     * @param studentNo 学号
     * @return true-存在, false-不存在
     */
    boolean existsByStudentNo(String studentNo);
}
