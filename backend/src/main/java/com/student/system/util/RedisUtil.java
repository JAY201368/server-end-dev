package com.student.system.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * @author Student System
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.redis.key-prefix.token}")
    private String tokenPrefix;

    @Value("${app.redis.token-expire}")
    private Long tokenExpire;

    /**
     * 存储Token到Redis
     *
     * @param username 用户名
     * @param token    Token
     */
    public void setToken(String username, String token) {
        String key = tokenPrefix + username;
        redisTemplate.opsForValue().set(key, token, tokenExpire, TimeUnit.SECONDS);
    }

    /**
     * 从Redis获取Token
     *
     * @param username 用户名
     * @return Token
     */
    public String getToken(String username) {
        String key = tokenPrefix + username;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    /**
     * 删除Token
     *
     * @param username 用户名
     */
    public void deleteToken(String username) {
        String key = tokenPrefix + username;
        redisTemplate.delete(key);
    }

    /**
     * 验证Token是否存在
     *
     * @param username 用户名
     * @param token    Token
     * @return 是否存在
     */
    public Boolean validateToken(String username, String token) {
        String redisToken = getToken(username);
        return redisToken != null && redisToken.equals(token);
    }

    /**
     * 刷新Token过期时间
     *
     * @param username 用户名
     */
    public void refreshTokenExpire(String username) {
        String key = tokenPrefix + username;
        redisTemplate.expire(key, tokenExpire, TimeUnit.SECONDS);
    }

    /**
     * 通用set方法
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 通用get方法
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 通用delete方法
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

}
