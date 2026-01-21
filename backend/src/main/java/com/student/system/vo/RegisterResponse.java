package com.student.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 注册响应VO
 *
 * @author Student System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 注册时间
     */
    private String createTime;

}
