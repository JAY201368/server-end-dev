package com.student.system.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.annotation.RequestMonitor;
import com.student.system.annotation.RequiresPermission;
import com.student.system.common.Result;
import com.student.system.dto.StudentDTO;
import com.student.system.dto.SystemLogMessage;
import com.student.system.entity.Student;
import com.student.system.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String SYSTEM_LOG_TOPIC = "system-log-topic";

    /**
     * 添加学生
     * 需要权限：student:add
     *
     * @param studentDTO 学生数据传输对象
     * @return 响应结果
     */
    @PostMapping("/add")
    @RequestMonitor(value = "添加学生", category = "student")
    @RequiresPermission("student:add")
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
     * 需要权限：student:edit
     *
     * @param studentDTO 学生数据传输对象
     * @return 响应结果
     */
    @PutMapping("/update")
    @RequiresPermission("student:edit")
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
     * 删除成功后发送消息到 Kafka
     * 需要权限：student:delete
     *
     * @param id 学生ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    @RequestMonitor(value = "删除学生", category = "student")
    @RequiresPermission("student:delete")
    public Result<String> deleteStudent(@PathVariable Long id, HttpServletRequest request) {
        log.info("开始删除学生 - ID: {}", id);

        try {
            // 检查学生是否存在
            Student student = studentService.getById(id);
            if (student == null) {
                return Result.error("学生不存在");
            }

            // 逻辑删除学生
            boolean success = studentService.removeById(id);

            if (success) {
                log.info("学生删除成功 - ID: {}, 学号: {}, 姓名: {}", id, student.getStudentNo(), student.getName());

                // 发送删除事件到 Kafka
                sendDeleteLogToKafka(student, request);

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
     * 发送学生删除日志到 Kafka
     *
     * @param student 被删除的学生信息
     * @param request HTTP请求对象
     */
    private void sendDeleteLogToKafka(Student student, HttpServletRequest request) {
        try {
            // 获取当前操作人
            String operator = getCurrentUsername();

            // 获取IP地址
            String ipAddress = getClientIpAddress(request);

            // 构建日志消息
            SystemLogMessage logMessage = SystemLogMessage.builder()
                    .operationType("DELETE_STUDENT")
                    .operator(operator)
                    .targetId(student.getId())
                    .targetInfo(JSON.toJSONString(student))
                    .operationTime(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .remark("删除学生: " + student.getName() + " (学号: " + student.getStudentNo() + ")")
                    .build();

            // 发送到 Kafka
            String messageJson = JSON.toJSONString(logMessage);
            kafkaTemplate.send(SYSTEM_LOG_TOPIC, messageJson);

            log.info("已发送删除日志到Kafka - 学生ID: {}, 操作人: {}", student.getId(), operator);

        } catch (Exception e) {
            log.error("发送Kafka消息失败", e);
            // 不影响主业务流程，仅记录错误
        }
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("获取当前用户信息失败", e);
        }
        return "SYSTEM";
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 根据ID查询学生详情
     * 需要权限：student:view
     *
     * @param id 学生ID
     * @return 学生详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("student:view")
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
     * 需要权限：student:view
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param name 姓名（模糊查询）
     * @param studentNo 学号（模糊查询）
     * @param status 状态
     * @return 分页数据
     */
    @GetMapping("/list")
    @RequestMonitor(value = "查询学生列表", category = "student")
    @RequiresPermission("student:view")
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
     * 需要权限：student:view
     *
     * @param studentNo 学号
     * @return 学生信息
     */
    @GetMapping("/no/{studentNo}")
    @RequiresPermission("student:view")
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
     * 需要权限：student:delete
     *
     * @param ids 学生ID列表
     * @return 响应结果
     */
    @DeleteMapping("/batch")
    @RequiresPermission("student:delete")
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
