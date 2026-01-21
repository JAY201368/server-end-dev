package com.student.system.controller;

import com.student.system.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于验证权限控制
 *
 * @author Student System
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 测试密码编码和匹配 - 仅用于调试
     */
    @PostMapping("/password")
    public Result<Map<String, Object>> testPassword(@RequestBody Map<String, String> params) {
        String rawPassword = params.get("rawPassword");
        String encodedPassword = params.get("encodedPassword");

        Map<String, Object> data = new HashMap<>();
        data.put("rawPassword", rawPassword);
        data.put("encodedPassword", encodedPassword);
        data.put("matches", passwordEncoder.matches(rawPassword, encodedPassword));
        data.put("newHash", passwordEncoder.encode(rawPassword));

        log.info("密码测试 - Raw: {}, Encoded: {}, Matches: {}",
                rawPassword, encodedPassword, passwordEncoder.matches(rawPassword, encodedPassword));

        return Result.success("密码测试完成", data);
    }

    /**
     * 测试管理员权限
     * 只有拥有 ROLE_ADMIN 角色的用户才能访问
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> testAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Map<String, Object> data = new HashMap<>();
        data.put("message", "恭喜！您拥有管理员权限");
        data.put("username", username);
        data.put("authorities", authentication.getAuthorities());

        log.info("管理员用户 {} 访问了 /test/admin", username);
        return Result.success("管理员权限验证成功", data);
    }

    /**
     * 测试教师权限
     */
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<Map<String, Object>> testTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Map<String, Object> data = new HashMap<>();
        data.put("message", "您拥有教师权限");
        data.put("username", username);
        data.put("authorities", authentication.getAuthorities());

        log.info("教师用户 {} 访问了 /test/teacher", username);
        return Result.success("教师权限验证成功", data);
    }

    /**
     * 测试学生权限
     */
    @GetMapping("/student")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Map<String, Object>> testStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Map<String, Object> data = new HashMap<>();
        data.put("message", "您拥有学生权限");
        data.put("username", username);
        data.put("authorities", authentication.getAuthorities());

        log.info("学生用户 {} 访问了 /test/student", username);
        return Result.success("学生权限验证成功", data);
    }

    /**
     * 测试任意认证用户都可访问
     */
    @GetMapping("/authenticated")
    public Result<Map<String, Object>> testAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Map<String, Object> data = new HashMap<>();
        data.put("message", "您已认证，可以访问此接口");
        data.put("username", username);
        data.put("authorities", authentication.getAuthorities());

        return Result.success("认证成功", data);
    }

}
