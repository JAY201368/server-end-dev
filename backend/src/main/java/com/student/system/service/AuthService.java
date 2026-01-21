package com.student.system.service;

import com.student.system.dto.LoginRequest;
import com.student.system.dto.RegisterRequest;
import com.student.system.vo.LoginResponse;
import com.student.system.vo.RegisterResponse;

/**
 * 认证服务接口
 *
 * @author Student System
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含Token）
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    RegisterResponse register(RegisterRequest registerRequest);

    /**
     * 用户登出
     *
     * @param username 用户名
     */
    void logout(String username);

}
