package com.student.system.aspect;

import com.student.system.annotation.RequiresPermission;
import com.student.system.annotation.RequiresRole;
import com.student.system.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 权限校验切面
 * 基于自定义注解 @RequiresPermission 和 @RequiresRole 进行权限控制
 *
 * @author Student System
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    /**
     * 拦截 @RequiresPermission 注解
     */
    @Around("@annotation(com.student.system.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("用户未认证，拒绝访问: {}", method.getName());
            throw new AccessDeniedException("未登录或登录已过期");
        }

        // 获取用户详情
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            log.warn("无法获取用户详情，拒绝访问: {}", method.getName());
            throw new AccessDeniedException("无权限访问");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        List<String> userPermissions = userDetails.getPermissions();

        // 获取需要的权限
        String[] requiredPermissions = annotation.value();
        RequiresPermission.Logical logical = annotation.logical();

        // 校验权限
        boolean hasPermission = checkPermissions(userPermissions, requiredPermissions, logical);

        if (!hasPermission) {
            log.warn("用户 {} 权限不足，需要权限: {}, 拥有权限: {}", 
                    userDetails.getUsername(), 
                    Arrays.toString(requiredPermissions), 
                    userPermissions);
            throw new AccessDeniedException("权限不足，需要权限: " + Arrays.toString(requiredPermissions));
        }

        log.debug("用户 {} 权限校验通过: {}", userDetails.getUsername(), Arrays.toString(requiredPermissions));
        return joinPoint.proceed();
    }

    /**
     * 拦截 @RequiresRole 注解
     */
    @Around("@annotation(com.student.system.annotation.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        RequiresRole annotation = method.getAnnotation(RequiresRole.class);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("用户未认证，拒绝访问: {}", method.getName());
            throw new AccessDeniedException("未登录或登录已过期");
        }

        // 获取用户详情
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            log.warn("无法获取用户详情，拒绝访问: {}", method.getName());
            throw new AccessDeniedException("无权限访问");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        List<String> userRoles = userDetails.getRoles();

        // 获取需要的角色
        String[] requiredRoles = annotation.value();
        RequiresRole.Logical logical = annotation.logical();

        // 校验角色
        boolean hasRole = checkPermissions(userRoles, requiredRoles, logical);

        if (!hasRole) {
            log.warn("用户 {} 角色不足，需要角色: {}, 拥有角色: {}", 
                    userDetails.getUsername(), 
                    Arrays.toString(requiredRoles), 
                    userRoles);
            throw new AccessDeniedException("角色不足，需要角色: " + Arrays.toString(requiredRoles));
        }

        log.debug("用户 {} 角色校验通过: {}", userDetails.getUsername(), Arrays.toString(requiredRoles));
        return joinPoint.proceed();
    }

    /**
     * 校验权限/角色
     * 
     * @param userItems 用户拥有的权限/角色列表
     * @param requiredItems 需要的权限/角色数组
     * @param logical 逻辑关系（AND/OR）
     * @return 是否通过校验
     */
    private boolean checkPermissions(List<String> userItems, String[] requiredItems, Object logical) {
        if (requiredItems == null || requiredItems.length == 0) {
            return true;
        }

        if (userItems == null || userItems.isEmpty()) {
            return false;
        }

        // 判断逻辑关系
        boolean isAnd = true;
        if (logical instanceof RequiresPermission.Logical) {
            isAnd = ((RequiresPermission.Logical) logical) == RequiresPermission.Logical.AND;
        } else if (logical instanceof RequiresRole.Logical) {
            isAnd = ((RequiresRole.Logical) logical) == RequiresRole.Logical.AND;
        }

        if (isAnd) {
            // AND 逻辑：必须拥有所有权限/角色
            return Arrays.stream(requiredItems).allMatch(userItems::contains);
        } else {
            // OR 逻辑：拥有任意一个权限/角色即可
            return Arrays.stream(requiredItems).anyMatch(userItems::contains);
        }
    }

}
