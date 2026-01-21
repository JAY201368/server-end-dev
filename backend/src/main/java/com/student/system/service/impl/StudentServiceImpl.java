package com.student.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.system.entity.Student;
import com.student.system.mapper.StudentMapper;
import com.student.system.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 学生服务实现类
 * 继承MyBatis-Plus的ServiceImpl，提供基础CRUD实现
 *
 * @author Student System
 * @since 2024
 */
@Slf4j
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Override
    public Student getByStudentNo(String studentNo) {
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentNo, studentNo);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean existsByStudentNo(String studentNo) {
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentNo, studentNo);
        return this.count(queryWrapper) > 0;
    }
}
