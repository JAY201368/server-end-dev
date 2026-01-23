package com.student.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 权限DTO
 *
 * @author Student System
 */
@Data
public class PermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID（更新时需要）
     */
    private Long id;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    /**
     * 权限代码
     */
    @NotBlank(message = "权限代码不能为空")
    private String permissionCode;

    /**
     * 资源类型: menu/button/api
     */
    private String resourceType;

    /**
     * URL
     */
    private String url;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 描述
     */
    private String description;

}
