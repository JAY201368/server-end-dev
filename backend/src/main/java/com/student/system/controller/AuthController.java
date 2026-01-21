package com.student.system.controller;

import com.student.system.common.Result;
import com.student.system.dto.LoginRequest;
import com.student.system.dto.RegisterRequest;
import com.student.system.service.AuthService;
import com.student.system.vo.LoginResponse;
import com.student.system.vo.RegisterResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author Student System
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        LoginResponse response = authService.login(loginRequest);
        return Result.success("登录成功", response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: {}", registerRequest.getUsername());
        RegisterResponse response = authService.register(registerRequest);
        return Result.success("注册成功", response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            authService.logout(username);
            log.info("用户 {} 登出", username);
            return Result.success("登出成功");
        }
        return Result.error("未登录");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<Object> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Result.success(authentication.getPrincipal());
        }
        return Result.error("未登录");
    }

}
