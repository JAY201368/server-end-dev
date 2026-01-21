package com.student.system.service;

import com.student.system.dto.LoginRequest;
import com.student.system.vo.LoginResponse;

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
     * 用户登出
     *
     * @param username 用户名
     */
    void logout(String username);

}
