package com.student.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.system.annotation.RequiresPermission;
import com.student.system.common.Result;
import com.student.system.dto.PermissionDTO;
import com.student.system.service.PermissionService;
import com.student.system.vo.PermissionVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 *
 * @author Student System
 */
@Slf4j
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 添加权限
     */
    @PostMapping("/add")
    @RequiresPermission("system:permission:add")
    public Result<String> addPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("添加权限: {}", permissionDTO.getPermissionCode());
        permissionService.addPermission(permissionDTO);
        return Result.success("添加权限成功");
    }

    /**
     * 更新权限
     */
    @PutMapping("/update")
    @RequiresPermission("system:permission:edit")
    public Result<String> updatePermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("更新权限: {}", permissionDTO.getId());
        permissionService.updatePermission(permissionDTO);
        return Result.success("更新权限成功");
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("system:permission:delete")
    public Result<String> deletePermission(@PathVariable Long id) {
        log.info("删除权限: {}", id);
        permissionService.deletePermission(id);
        return Result.success("删除权限成功");
    }

    /**
     * 查询权限详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("system:permission:view")
    public Result<PermissionVO> getPermissionById(@PathVariable Long id) {
        PermissionVO permissionVO = permissionService.getPermissionById(id);
        return Result.success(permissionVO);
    }

    /**
     * 分页查询权限列表
     */
    @GetMapping("/list")
    @RequiresPermission("system:permission:view")
    public Result<IPage<PermissionVO>> getPermissionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String permissionName,
            @RequestParam(required = false) String resourceType) {
        IPage<PermissionVO> permissionPage = permissionService.getPermissionList(page, size, permissionName, resourceType);
        return Result.success(permissionPage);
    }

    /**
     * 查询所有权限（不分页）
     */
    @GetMapping("/all")
    public Result<List<PermissionVO>> getAllPermissions() {
        List<PermissionVO> permissions = permissionService.getAllPermissions();
        return Result.success(permissions);
    }

    /**
     * 查询角色的权限列表
     */
    @GetMapping("/role/{roleId}")
    @RequiresPermission("system:permission:view")
    public Result<List<PermissionVO>> getPermissionsByRoleId(@PathVariable Long roleId) {
        List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(roleId);
        return Result.success(permissions);
    }

}
