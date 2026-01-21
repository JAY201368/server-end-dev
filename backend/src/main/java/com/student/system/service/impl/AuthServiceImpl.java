package com.student.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.system.dto.LoginRequest;
import com.student.system.dto.RegisterRequest;
import com.student.system.entity.SysUser;
import com.student.system.mapper.SysUserMapper;
import com.student.system.service.AuthService;
import com.student.system.util.JwtUtil;
import com.student.system.util.RedisUtil;
import com.student.system.vo.LoginResponse;
import com.student.system.vo.RegisterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
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
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();
        String nickname = registerRequest.getNickname();

        // 1. 验证两次密码是否一致
        if (!password.equals(confirmPassword)) {
            log.error("两次密码不一致: {}", username);
            throw new BadCredentialsException("两次密码不一致");
        }

        // 2. 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            log.error("用户名已存在: {}", username);
            throw new BadCredentialsException("用户名已存在");
        }

        // 3. 检查邮箱是否已存在（如果提供）
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(SysUser::getEmail, registerRequest.getEmail());
            Long emailCount = userMapper.selectCount(emailWrapper);
            if (emailCount > 0) {
                log.error("邮箱已被注册: {}", registerRequest.getEmail());
                throw new BadCredentialsException("邮箱已被注册");
            }
        }

        // 4. 检查手机号是否已存在（如果提供）
        if (registerRequest.getPhone() != null && !registerRequest.getPhone().isEmpty()) {
            LambdaQueryWrapper<SysUser> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(SysUser::getPhone, registerRequest.getPhone());
            Long phoneCount = userMapper.selectCount(phoneWrapper);
            if (phoneCount > 0) {
                log.error("手机号已被注册: {}", registerRequest.getPhone());
                throw new BadCredentialsException("手机号已被注册");
            }
        }

        // 5. 创建新用户
        SysUser newUser = new SysUser();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // 加密密码
        newUser.setNickname(nickname);
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPhone(registerRequest.getPhone());
        newUser.setStatus(1); // 默认启用
        newUser.setDeleted(0); // 未删除

        // 6. 插入数据库
        int rows = userMapper.insert(newUser);
        if (rows == 0) {
            log.error("用户注册失败: {}", username);
            throw new RuntimeException("用户注册失败");
        }

        log.info("用户注册成功: {} (ID: {})", username, newUser.getId());

        // 7. 返回注册响应
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new RegisterResponse(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getNickname(),
                newUser.getCreateTime().format(formatter)
        );
    }

    @Override
    public void logout(String username) {
        // 从Redis删除Token
        redisUtil.deleteToken(username);
        log.info("用户 {} 已登出，Token已从Redis删除", username);
    }

}
