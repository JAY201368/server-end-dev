package com.student.system.aspect;

import com.student.system.annotation.RequestMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 请求监控切面
 * 使用 AOP + Redis 统计接口调用次数和耗时
 *
 * @author Student System
 * @since 2024
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestMonitorAspect {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.redis.key-prefix.api-stats:api:stats:}")
    private String apiStatsPrefix;

    /**
     * 定义切入点：所有带有 @RequestMonitor 注解的方法
     */
    @Pointcut("@annotation(com.student.system.annotation.RequestMonitor)")
    public void requestMonitorPointcut() {
    }

    /**
     * 环绕通知：统计接口调用次数和耗时
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("requestMonitorPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        RequestMonitor requestMonitor = method.getAnnotation(RequestMonitor.class);

        // 获取接口名称
        String apiName = getApiName(requestMonitor, method);

        // 执行目标方法
        Object result = null;
        Throwable exception = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            // 计算耗时
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 统计到 Redis
            recordApiStats(apiName, duration, exception == null);

            log.debug("接口监控 - 接口: {}, 耗时: {}ms, 状态: {}",
                    apiName, duration, exception == null ? "SUCCESS" : "FAILURE");
        }

        return result;
    }

    /**
     * 获取接口名称
     *
     * @param requestMonitor 注解
     * @param method 方法
     * @return 接口名称
     */
    private String getApiName(RequestMonitor requestMonitor, Method method) {
        String value = requestMonitor.value();
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // 使用方法签名作为接口名
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        return className + "." + methodName;
    }

    /**
     * 记录接口统计信息到 Redis
     * 使用 Redis Hash 结构：api:stats:{apiName} -> {count, totalDuration, successCount, failureCount}
     *
     * @param apiName 接口名称
     * @param duration 耗时（毫秒）
     * @param success 是否成功
     */
    private void recordApiStats(String apiName, long duration, boolean success) {
        try {
            String hashKey = apiStatsPrefix + apiName;

            // 增加调用次数
            stringRedisTemplate.opsForHash().increment(hashKey, "count", 1);

            // 累加总耗时
            stringRedisTemplate.opsForHash().increment(hashKey, "totalDuration", duration);

            // 增加成功或失败次数
            if (success) {
                stringRedisTemplate.opsForHash().increment(hashKey, "successCount", 1);
            } else {
                stringRedisTemplate.opsForHash().increment(hashKey, "failureCount", 1);
            }

            // 记录最后一次调用时间
            stringRedisTemplate.opsForHash().put(hashKey, "lastCallTime", String.valueOf(System.currentTimeMillis()));

            // 更新最小耗时
            String minDurationStr = (String) stringRedisTemplate.opsForHash().get(hashKey, "minDuration");
            long minDuration = minDurationStr != null ? Long.parseLong(minDurationStr) : Long.MAX_VALUE;
            if (duration < minDuration) {
                stringRedisTemplate.opsForHash().put(hashKey, "minDuration", String.valueOf(duration));
            }

            // 更新最大耗时
            String maxDurationStr = (String) stringRedisTemplate.opsForHash().get(hashKey, "maxDuration");
            long maxDuration = maxDurationStr != null ? Long.parseLong(maxDurationStr) : 0;
            if (duration > maxDuration) {
                stringRedisTemplate.opsForHash().put(hashKey, "maxDuration", String.valueOf(duration));
            }

        } catch (Exception e) {
            log.error("记录接口统计信息到Redis失败 - 接口: {}", apiName, e);
        }
    }
}
