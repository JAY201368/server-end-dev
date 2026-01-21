package com.student.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.system.dto.LoginRequest;
import com.student.system.entity.SysUser;
import com.student.system.mapper.SysUserMapper;
import com.student.system.service.AuthService;
import com.student.system.util.JwtUtil;
import com.student.system.util.RedisUtil;
import com.student.system.vo.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证服务实现
 *
 * @author Student System
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 1. 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.error("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("密码错误: {}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() != 1) {
            log.error("用户已被禁用: {}", username);
            throw new BadCredentialsException("用户已被禁用");
        }

        // 4. 查询用户角色和权限
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = userMapper.selectPermissionCodesByUserId(user.getId());

        // 5. 生成JWT Token
        String token = jwtUtil.generateToken(username);

        // 6. 将Token存入Redis（关键：Redis存储Token）
        redisUtil.setToken(username, token);

        log.info("用户 {} 登录成功，Token已存入Redis", username);

        // 7. 返回登录响应
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                roles,
                permissions
        );
    }

    @Override
    public void logout(String username) {
        // 从Redis删除Token
        redisUtil.deleteToken(username);
        log.info("用户 {} 已登出，Token已从Redis删除", username);
    }

}
