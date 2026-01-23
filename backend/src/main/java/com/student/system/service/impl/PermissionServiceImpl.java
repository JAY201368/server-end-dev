package com.student.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.dto.PermissionDTO;
import com.student.system.entity.SysPermission;
import com.student.system.mapper.SysPermissionMapper;
import com.student.system.mapper.SysRolePermissionMapper;
import com.student.system.service.PermissionService;
import com.student.system.vo.PermissionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理Service实现
 *
 * @author Student System
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPermission(PermissionDTO permissionDTO) {
        // 检查权限代码是否已存在
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getPermissionCode, permissionDTO.getPermissionCode());
        Long count = permissionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("权限代码已存在");
        }

        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permissionMapper.insert(permission);
        log.info("添加权限成功: {}", permission.getPermissionCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(PermissionDTO permissionDTO) {
        if (permissionDTO.getId() == null) {
            throw new RuntimeException("权限ID不能为空");
        }

        SysPermission permission = permissionMapper.selectById(permissionDTO.getId());
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }

        // 检查权限代码是否被其他权限使用
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getPermissionCode, permissionDTO.getPermissionCode())
                .ne(SysPermission::getId, permissionDTO.getId());
        Long count = permissionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("权限代码已被使用");
        }

        BeanUtils.copyProperties(permissionDTO, permission);
        permissionMapper.updateById(permission);
        log.info("更新权限成功: {}", permission.getPermissionCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }

        // 删除权限（注意：这会影响已分配该权限的角色）
        permissionMapper.deleteById(id);
        log.info("删除权限成功: {}", permission.getPermissionCode());
    }

    @Override
    public PermissionVO getPermissionById(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }

        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }

    @Override
    public IPage<PermissionVO> getPermissionList(Integer page, Integer size, String permissionName, String resourceType) {
        Page<SysPermission> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(permissionName)) {
            wrapper.like(SysPermission::getPermissionName, permissionName);
        }
        if (StringUtils.hasText(resourceType)) {
            wrapper.eq(SysPermission::getResourceType, resourceType);
        }

        wrapper.orderByDesc(SysPermission::getCreateTime);
        IPage<SysPermission> permissionPage = permissionMapper.selectPage(pageParam, wrapper);

        // 转换为VO
        IPage<PermissionVO> voPage = new Page<>(permissionPage.getCurrent(), permissionPage.getSize(), permissionPage.getTotal());
        List<PermissionVO> voList = permissionPage.getRecords().stream().map(permission -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(permission, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public List<PermissionVO> getAllPermissions() {
        List<SysPermission> permissions = permissionMapper.selectList(null);
        return permissions.stream().map(permission -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(permission, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> getPermissionsByRoleId(Long roleId) {
        // 通过角色ID查询权限列表
        List<SysPermission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .inSql(SysPermission::getId,
                                "SELECT permission_id FROM sys_role_permission WHERE role_id = " + roleId)
        );

        return permissions.stream().map(permission -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(permission, vo);
            return vo;
        }).collect(Collectors.toList());
    }

}
