package com.student.system.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.system.entity.SysUser;
import com.student.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security UserDetailsService 实现
 * 基于 RBAC 从 MySQL 加载用户权限
 *
 * @author Student System
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.error("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 2. 查询用户的角色列表（从MySQL）
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        log.info("用户 {} 的角色: {}", username, roles);

        // 3. 查询用户的权限列表（从MySQL）
        List<String> permissions = userMapper.selectPermissionCodesByUserId(user.getId());
        log.info("用户 {} 的权限: {}", username, permissions);

        // 4. 构建UserDetails对象
        return new UserDetailsImpl(user, roles, permissions);
    }

}
