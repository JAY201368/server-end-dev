package com.student.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.system.annotation.RequiresPermission;
import com.student.system.annotation.RequiresRole;
import com.student.system.common.Result;
import com.student.system.dto.RoleDTO;
import com.student.system.dto.UserRoleDTO;
import com.student.system.service.RoleService;
import com.student.system.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author Student System
 */
@Slf4j
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 添加角色
     */
    @PostMapping("/add")
    @RequiresPermission("system:role:add")
    public Result<String> addRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("添加角色: {}", roleDTO.getRoleCode());
        roleService.addRole(roleDTO);
        return Result.success("添加角色成功");
    }

    /**
     * 更新角色
     */
    @PutMapping("/update")
    @RequiresPermission("system:role:edit")
    public Result<String> updateRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("更新角色: {}", roleDTO.getId());
        roleService.updateRole(roleDTO);
        return Result.success("更新角色成功");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("system:role:delete")
    public Result<String> deleteRole(@PathVariable Long id) {
        log.info("删除角色: {}", id);
        roleService.deleteRole(id);
        return Result.success("删除角色成功");
    }

    /**
     * 查询角色详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("system:role:view")
    public Result<RoleVO> getRoleById(@PathVariable Long id) {
        RoleVO roleVO = roleService.getRoleById(id);
        return Result.success(roleVO);
    }

    /**
     * 分页查询角色列表
     */
    @GetMapping("/list")
    @RequiresPermission("system:role:view")
    public Result<IPage<RoleVO>> getRoleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        IPage<RoleVO> rolePage = roleService.getRoleList(page, size, roleName, status);
        return Result.success(rolePage);
    }

    /**
     * 查询所有角色（不分页）
     */
    @GetMapping("/all")
    public Result<List<RoleVO>> getAllRoles() {
        List<RoleVO> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 为角色分配权限
     */
    @PostMapping("/assign-permissions")
    @RequiresPermission("system:role:assign")
    public Result<String> assignPermissions(
            @RequestParam Long roleId,
            @RequestBody List<Long> permissionIds) {
        log.info("为角色 {} 分配权限", roleId);
        roleService.assignPermissions(roleId, permissionIds);
        return Result.success("分配权限成功");
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/assign-to-user")
    @RequiresPermission("system:user:assign-role")
    public Result<String> assignRolesToUser(@Valid @RequestBody UserRoleDTO userRoleDTO) {
        log.info("为用户 {} 分配角色", userRoleDTO.getUserId());
        roleService.assignRolesToUser(userRoleDTO);
        return Result.success("分配角色成功");
    }

    /**
     * 查询用户的角色列表
     */
    @GetMapping("/user/{userId}")
    @RequiresRole(value = {"ROLE_ADMIN", "ROLE_TEACHER"}, logical = RequiresRole.Logical.OR)
    public Result<List<RoleVO>> getRolesByUserId(@PathVariable Long userId) {
        List<RoleVO> roles = roleService.getRolesByUserId(userId);
        return Result.success(roles);
    }

}
