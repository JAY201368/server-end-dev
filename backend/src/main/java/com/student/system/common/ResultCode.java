package com.student.system.common;

/**
 * 响应状态码常量
 *
 * @author Student System
 */
public interface ResultCode {

    /**
     * 成功
     */
    int SUCCESS = 200;

    /**
     * 失败
     */
    int ERROR = 500;

    /**
     * 参数错误
     */
    int BAD_REQUEST = 400;

    /**
     * 未认证
     */
    int UNAUTHORIZED = 401;

    /**
     * 无权限
     */
    int FORBIDDEN = 403;

    /**
     * 资源不存在
     */
    int NOT_FOUND = 404;

    /**
     * 业务异常
     */
    int BUSINESS_ERROR = 600;

}
