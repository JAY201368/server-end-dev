package com.student.system.controller;

import com.student.system.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 监控控制器
 * 提供接口调用统计信息
 *
 * @author Student System
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.redis.key-prefix.api-stats:api:stats:}")
    private String apiStatsPrefix;

    /**
     * 获取所有接口的统计信息
     * 从 Redis 中读取统计数据
     *
     * @return 接口统计数据列表
     */
    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getApiStats() {
        log.info("查询接口统计信息");

        try {
            List<Map<String, Object>> statsList = new ArrayList<>();

            // 获取所有匹配的 key
            Set<String> keys = stringRedisTemplate.keys(apiStatsPrefix + "*");

            if (keys == null || keys.isEmpty()) {
                return Result.success("暂无统计数据", statsList);
            }

            // 遍历每个 key，获取统计信息
            for (String key : keys) {
                Map<Object, Object> hashData = stringRedisTemplate.opsForHash().entries(key);

                if (hashData.isEmpty()) {
                    continue;
                }

                // 解析接口名称（去除前缀）
                String apiName = key.substring(apiStatsPrefix.length());

                // 构建统计数据
                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("apiName", apiName);
                stats.put("count", parseLong(hashData.get("count"), 0L));
                stats.put("successCount", parseLong(hashData.get("successCount"), 0L));
                stats.put("failureCount", parseLong(hashData.get("failureCount"), 0L));

                long totalDuration = parseLong(hashData.get("totalDuration"), 0L);
                long count = parseLong(hashData.get("count"), 1L);
                stats.put("totalDuration", totalDuration);
                stats.put("avgDuration", count > 0 ? totalDuration / count : 0);
                stats.put("minDuration", parseLong(hashData.get("minDuration"), 0L));
                stats.put("maxDuration", parseLong(hashData.get("maxDuration"), 0L));

                // 最后调用时间
                long lastCallTime = parseLong(hashData.get("lastCallTime"), 0L);
                if (lastCallTime > 0) {
                    LocalDateTime dateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(lastCallTime), ZoneId.systemDefault());
                    stats.put("lastCallTime", dateTime);
                } else {
                    stats.put("lastCallTime", null);
                }

                statsList.add(stats);
            }

            // 按调用次数降序排序
            statsList.sort((a, b) -> {
                Long countA = (Long) a.get("count");
                Long countB = (Long) b.get("count");
                return countB.compareTo(countA);
            });

            return Result.success("查询成功", statsList);

        } catch (Exception e) {
            log.error("查询接口统计信息失败", e);
            return Result.error("查询接口统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有统计数据
     *
     * @return 响应结果
     */
    @GetMapping("/stats/clear")
    public Result<String> clearStats() {
        log.info("清空接口统计信息");

        try {
            Set<String> keys = stringRedisTemplate.keys(apiStatsPrefix + "*");

            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = stringRedisTemplate.delete(keys);
                log.info("已清空 {} 个接口的统计数据", deletedCount);
                return Result.success("已清空 " + deletedCount + " 个接口的统计数据");
            }

            return Result.success("暂无统计数据");

        } catch (Exception e) {
            log.error("清空接口统计信息失败", e);
            return Result.error("清空接口统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 解析 Long 类型，处理异常
     */
    private Long parseLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
