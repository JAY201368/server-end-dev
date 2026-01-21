package com.student.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 登录响应VO
 *
 * @author Student System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Token
     */
    private String token;

    /**
     * Token类型
     */
    private String tokenType = "Bearer";

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;

    public LoginResponse(String token, Long userId, String username, String nickname,
                        List<String> roles, List<String> permissions) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.roles = roles;
        this.permissions = permissions;
    }

}
