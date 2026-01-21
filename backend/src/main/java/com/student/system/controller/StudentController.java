package com.student.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.common.Result;
import com.student.system.dto.StudentDTO;
import com.student.system.entity.Student;
import com.student.system.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 学生管理控制器
 * 提供学生信息的CRUD操作
 *
 * @author Student System
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 添加学生
     *
     * @param studentDTO 学生数据传输对象
     * @return 响应结果
     */
    @PostMapping("/add")
    public Result<String> addStudent(@Valid @RequestBody StudentDTO studentDTO) {
        log.info("开始添加学生 - 学号: {}, 姓名: {}", studentDTO.getStudentNo(), studentDTO.getName());

        try {
            // 检查学号是否已存在
            if (studentService.existsByStudentNo(studentDTO.getStudentNo())) {
                return Result.error("学号已存在");
            }

            // DTO转Entity
            Student student = new Student();
            BeanUtils.copyProperties(studentDTO, student);

            // 保存学生信息
            boolean success = studentService.save(student);

            if (success) {
                return Result.success("添加学生成功");
            } else {
                return Result.error("添加学生失败");
            }

        } catch (Exception e) {
            log.error("添加学生异常", e);
            return Result.error("添加学生失败: " + e.getMessage());
        }
    }

    /**
     * 更新学生信息
     *
     * @param studentDTO 学生数据传输对象
     * @return 响应结果
     */
    @PutMapping("/update")
    public Result<String> updateStudent(@Valid @RequestBody StudentDTO studentDTO) {
        log.info("开始更新学生信息 - ID: {}, 姓名: {}", studentDTO.getId(), studentDTO.getName());

        try {
            if (studentDTO.getId() == null) {
                return Result.error("学生ID不能为空");
            }

            // 检查学生是否存在
            Student existingStudent = studentService.getById(studentDTO.getId());
            if (existingStudent == null) {
                return Result.error("学生不存在");
            }

            // 如果修改了学号，检查新学号是否已被其他学生使用
            if (!existingStudent.getStudentNo().equals(studentDTO.getStudentNo())) {
                if (studentService.existsByStudentNo(studentDTO.getStudentNo())) {
                    return Result.error("学号已被其他学生使用");
                }
            }

            // DTO转Entity
            Student student = new Student();
            BeanUtils.copyProperties(studentDTO, student);

            // 更新学生信息
            boolean success = studentService.updateById(student);

            if (success) {
                return Result.success("更新学生信息成功");
            } else {
                return Result.error("更新学生信息失败");
            }

        } catch (Exception e) {
            log.error("更新学生信息异常", e);
            return Result.error("更新学生信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除学生
     * 使用逻辑删除（MyBatis-Plus的@TableLogic）
     *
     * @param id 学生ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteStudent(@PathVariable Long id) {
        log.info("开始删除学生 - ID: {}", id);

        try {
            // 检查学生是否存在
            Student student = studentService.getById(id);
            if (student == null) {
                return Result.error("学生不存在");
            }

            // 逻辑删除学生
            // 注：这里可以在后续阶段集成Kafka，发送删除事件到消息队列
            boolean success = studentService.removeById(id);

            if (success) {
                log.info("学生删除成功 - ID: {}, 学号: {}, 姓名: {}", id, student.getStudentNo(), student.getName());
                return Result.success("删除学生成功");
            } else {
                return Result.error("删除学生失败");
            }

        } catch (Exception e) {
            log.error("删除学生异常", e);
            return Result.error("删除学生失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询学生详情
     *
     * @param id 学生ID
     * @return 学生详情
     */
    @GetMapping("/{id}")
    public Result<Student> getStudent(@PathVariable Long id) {
        log.info("查询学生详情 - ID: {}", id);

        try {
            Student student = studentService.getById(id);

            if (student == null) {
                return Result.error("学生不存在");
            }

            return Result.success("查询成功", student);

        } catch (Exception e) {
            log.error("查询学生详情异常", e);
            return Result.error("查询学生详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询学生列表
     * 支持按姓名、学号模糊查询
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param name 姓名（模糊查询）
     * @param studentNo 学号（模糊查询）
     * @param status 状态
     * @return 分页数据
     */
    @GetMapping("/list")
    public Result<IPage<Student>> listStudents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) Integer status) {

        log.info("分页查询学生列表 - page: {}, size: {}, name: {}, studentNo: {}, status: {}",
                page, size, name, studentNo, status);

        try {
            // 构建分页对象
            Page<Student> pageParam = new Page<>(page, size);

            // 构建查询条件
            LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();

            // 按姓名模糊查询
            if (StringUtils.hasText(name)) {
                queryWrapper.like(Student::getName, name);
            }

            // 按学号模糊查询
            if (StringUtils.hasText(studentNo)) {
                queryWrapper.like(Student::getStudentNo, studentNo);
            }

            // 按状态精确查询
            if (status != null) {
                queryWrapper.eq(Student::getStatus, status);
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc(Student::getCreateTime);

            // 执行分页查询
            IPage<Student> result = studentService.page(pageParam, queryWrapper);

            return Result.success("查询成功", result);

        } catch (Exception e) {
            log.error("分页查询学生列表异常", e);
            return Result.error("查询学生列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据学号查询学生
     *
     * @param studentNo 学号
     * @return 学生信息
     */
    @GetMapping("/no/{studentNo}")
    public Result<Student> getByStudentNo(@PathVariable String studentNo) {
        log.info("根据学号查询学生 - 学号: {}", studentNo);

        try {
            Student student = studentService.getByStudentNo(studentNo);

            if (student == null) {
                return Result.error("学生不存在");
            }

            return Result.success("查询成功", student);

        } catch (Exception e) {
            log.error("根据学号查询学生异常", e);
            return Result.error("查询学生失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除学生
     *
     * @param ids 学生ID列表
     * @return 响应结果
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteStudents(@RequestBody java.util.List<Long> ids) {
        log.info("批量删除学生 - IDs: {}", ids);

        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("学生ID列表不能为空");
            }

            boolean success = studentService.removeByIds(ids);

            if (success) {
                return Result.success("批量删除学生成功");
            } else {
                return Result.error("批量删除学生失败");
            }

        } catch (Exception e) {
            log.error("批量删除学生异常", e);
            return Result.error("批量删除学生失败: " + e.getMessage());
        }
    }
}
