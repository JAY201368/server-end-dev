package com.student.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.system.dto.RoleDTO;
import com.student.system.dto.UserRoleDTO;
import com.student.system.vo.RoleVO;

import java.util.List;

/**
 * 角色管理Service接口
 *
 * @author Student System
 */
public interface RoleService {

    /**
     * 添加角色
     */
    void addRole(RoleDTO roleDTO);

    /**
     * 更新角色
     */
    void updateRole(RoleDTO roleDTO);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 根据ID查询角色
     */
    RoleVO getRoleById(Long id);

    /**
     * 分页查询角色列表
     */
    IPage<RoleVO> getRoleList(Integer page, Integer size, String roleName, Integer status);

    /**
     * 查询所有角色
     */
    List<RoleVO> getAllRoles();

    /**
     * 为角色分配权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 为用户分配角色
     */
    void assignRolesToUser(UserRoleDTO userRoleDTO);

    /**
     * 根据用户ID查询角色列表
     */
    List<RoleVO> getRolesByUserId(Long userId);

}
