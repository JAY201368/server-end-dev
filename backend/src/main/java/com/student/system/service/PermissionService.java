package com.student.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.system.dto.PermissionDTO;
import com.student.system.entity.SysPermission;
import com.student.system.vo.PermissionVO;

import java.util.List;

/**
 * 权限管理Service接口
 *
 * @author Student System
 */
public interface PermissionService {

    /**
     * 添加权限
     */
    void addPermission(PermissionDTO permissionDTO);

    /**
     * 更新权限
     */
    void updatePermission(PermissionDTO permissionDTO);

    /**
     * 删除权限
     */
    void deletePermission(Long id);

    /**
     * 根据ID查询权限
     */
    PermissionVO getPermissionById(Long id);

    /**
     * 分页查询权限列表
     */
    IPage<PermissionVO> getPermissionList(Integer page, Integer size, String permissionName, String resourceType);

    /**
     * 查询所有权限
     */
    List<PermissionVO> getAllPermissions();

    /**
     * 根据角色ID查询权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(Long roleId);

}
