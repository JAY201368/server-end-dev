package com.student.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.dto.RoleDTO;
import com.student.system.dto.UserRoleDTO;
import com.student.system.entity.SysRole;
import com.student.system.entity.SysRolePermission;
import com.student.system.entity.SysUserRole;
import com.student.system.mapper.SysRoleMapper;
import com.student.system.mapper.SysRolePermissionMapper;
import com.student.system.mapper.SysUserRoleMapper;
import com.student.system.service.PermissionService;
import com.student.system.service.RoleService;
import com.student.system.vo.PermissionVO;
import com.student.system.vo.RoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理Service实现
 *
 * @author Student System
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private PermissionService permissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleDTO roleDTO) {
        // 检查角色代码是否已存在
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleDTO.getRoleCode());
        Long count = roleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("角色代码已存在");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        if (role.getStatus() == null) {
            role.setStatus(1); // 默认启用
        }
        roleMapper.insert(role);

        // 分配权限
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            assignPermissions(role.getId(), roleDTO.getPermissionIds());
        }

        log.info("添加角色成功: {}", role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleDTO roleDTO) {
        if (roleDTO.getId() == null) {
            throw new RuntimeException("角色ID不能为空");
        }

        SysRole role = roleMapper.selectById(roleDTO.getId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 检查角色代码是否被其他角色使用
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleDTO.getRoleCode())
                .ne(SysRole::getId, roleDTO.getId());
        Long count = roleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("角色代码已被使用");
        }

        BeanUtils.copyProperties(roleDTO, role);
        roleMapper.updateById(role);

        // 更新权限
        if (roleDTO.getPermissionIds() != null) {
            assignPermissions(role.getId(), roleDTO.getPermissionIds());
        }

        log.info("更新角色成功: {}", role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 检查是否有用户使用该角色
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, id);
        Long count = userRoleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该角色已分配给用户，无法删除");
        }

        // 删除角色
        roleMapper.deleteById(id);
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);

        log.info("删除角色成功: {}", role.getRoleCode());
    }

    @Override
    public RoleVO getRoleById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);

        // 查询角色的权限列表
        List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(id);
        vo.setPermissions(permissions);

        return vo;
    }

    @Override
    public IPage<RoleVO> getRoleList(Integer page, Integer size, String roleName, Integer status) {
        Page<SysRole> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }

        wrapper.orderByDesc(SysRole::getCreateTime);
        IPage<SysRole> rolePage = roleMapper.selectPage(pageParam, wrapper);

        // 转换为VO
        IPage<RoleVO> voPage = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        List<RoleVO> voList = rolePage.getRecords().stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            // 查询角色的权限列表
            List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(role.getId());
            vo.setPermissions(permissions);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public List<RoleVO> getAllRoles() {
        List<SysRole> roles = roleMapper.selectList(null);
        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除该角色的所有权限
        rolePermissionMapper.deleteByRoleId(roleId);

        // 重新分配权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }

        log.info("为角色 {} 分配权限成功", roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(UserRoleDTO userRoleDTO) {
        // 先删除该用户的所有角色
        userRoleMapper.deleteByUserId(userRoleDTO.getUserId());

        // 重新分配角色
        if (userRoleDTO.getRoleIds() != null && !userRoleDTO.getRoleIds().isEmpty()) {
            for (Long roleId : userRoleDTO.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userRoleDTO.getUserId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        log.info("为用户 {} 分配角色成功", userRoleDTO.getUserId());
    }

    @Override
    public List<RoleVO> getRolesByUserId(Long userId) {
        // 通过用户ID查询角色列表
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .inSql(SysRole::getId,
                                "SELECT role_id FROM sys_user_role WHERE user_id = " + userId)
        );

        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
    }

}
