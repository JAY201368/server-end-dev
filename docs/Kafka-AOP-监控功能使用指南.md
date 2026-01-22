# Kafka 和 AOP 监控功能实现文档

## 功能概述

本文档介绍如何使用已实现的 Kafka 消息队列和 AOP 接口监控功能。

## 一、Kafka 系统日志功能

### 1.1 功能说明
- 使用 Kafka 处理系统操作日志
- 在学生删除操作时，发送消息到 `system-log-topic`
- Kafka 消费者监听该 Topic，将日志写入 MySQL 的 `sys_log` 表

### 1.2 数据库表结构

需要先执行 SQL 创建 `sys_log` 表：

```sql
-- 位置: init-mysql/sys-log-table.sql
CREATE TABLE IF NOT EXISTS `sys_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `operator` VARCHAR(100) NOT NULL COMMENT '操作人',
    `target_id` BIGINT COMMENT '操作目标ID',
    `target_info` VARCHAR(500) COMMENT '操作目标信息（JSON格式）',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `result` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '操作结果',
    `remark` TEXT COMMENT '备注信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 1.3 使用方式

#### 启动 Kafka
确保 Kafka 服务正在运行（默认端口 9092）：
```bash
# 启动 Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# 启动 Kafka
bin/kafka-server-start.sh config/server.properties
```

#### 测试删除学生接口
```bash
# 删除学生（会自动发送 Kafka 消息）
curl -X DELETE http://localhost:8088/api/student/1
```

#### 查看日志表
```sql
SELECT * FROM sys_log ORDER BY operation_time DESC;
```

### 1.4 核心组件

- **Producer**: `StudentController.sendDeleteLogToKafka()` - 发送删除日志
- **Consumer**: `SystemLogConsumer` - 监听并处理消息
- **Topic**: `system-log-topic` (3个分区，1个副本)
- **Message DTO**: `SystemLogMessage`
- **Entity**: `SysLog`

### 1.5 消息格式示例

```json
{
  "operationType": "DELETE_STUDENT",
  "operator": "admin",
  "targetId": 1,
  "targetInfo": "{\"id\":1,\"studentNo\":\"2021001\",\"name\":\"张三\"}",
  "operationTime": "2024-01-20T10:30:00",
  "ipAddress": "192.168.1.100",
  "remark": "删除学生: 张三 (学号: 2021001)"
}
```

---

## 二、AOP 接口监控功能

### 2.1 功能说明
- 使用 AOP 切面拦截带有 `@RequestMonitor` 注解的方法
- 统计接口调用次数、耗时、成功/失败次数
- 将统计数据存储到 Redis Hash 结构中
- 提供 `/monitor/stats` 接口查询统计数据

### 2.2 使用 @RequestMonitor 注解

在需要监控的接口方法上添加注解：

```java
@GetMapping("/list")
@RequestMonitor(value = "查询学生列表", category = "student")
public Result<IPage<Student>> listStudents(...) {
    // 方法实现
}
```

参数说明：
- `value`: 接口名称/描述（可选，默认使用方法签名）
- `category`: 接口分类（可选，默认为 "default"）

### 2.3 已添加监控的接口

#### 学生管理接口
- `添加学生` - POST /api/student/add
- `删除学生` - DELETE /api/student/{id}
- `查询学生列表` - GET /api/student/list

#### 成绩管理接口
- `录入成绩` - POST /api/score/save
- `查询成绩排行榜` - GET /api/score/ranking

### 2.4 查询监控统计

#### 获取所有接口统计
```bash
curl http://localhost:8088/api/monitor/stats
```

响应示例：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "apiName": "查询学生列表",
      "count": 150,
      "successCount": 148,
      "failureCount": 2,
      "totalDuration": 3500,
      "avgDuration": 23,
      "minDuration": 15,
      "maxDuration": 89,
      "lastCallTime": "2024-01-20T15:30:45"
    },
    {
      "apiName": "添加学生",
      "count": 50,
      "successCount": 48,
      "failureCount": 2,
      "totalDuration": 2500,
      "avgDuration": 50,
      "minDuration": 35,
      "maxDuration": 120,
      "lastCallTime": "2024-01-20T15:28:30"
    }
  ]
}
```

#### 清空统计数据
```bash
curl http://localhost:8088/api/monitor/stats/clear
```

### 2.5 Redis 数据结构

统计数据使用 Redis Hash 存储：

```
Key: api:stats:查询学生列表
Hash Fields:
  - count: 150              (总调用次数)
  - totalDuration: 3500     (总耗时，毫秒)
  - successCount: 148       (成功次数)
  - failureCount: 2         (失败次数)
  - minDuration: 15         (最小耗时)
  - maxDuration: 89         (最大耗时)
  - lastCallTime: 1705738245000  (最后调用时间戳)
```

查看 Redis 数据：
```bash
# 查看所有监控 key
redis-cli KEYS "api:stats:*"

# 查看特定接口统计
redis-cli HGETALL "api:stats:查询学生列表"
```

---

## 三、配置说明

### 3.1 application.yml 配置

Kafka 和 Redis 配置已在 `application.yml` 中定义：

```yaml
spring:
  # Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      password: redis123456

  # Kafka 配置
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: student-system-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

app:
  redis:
    key-prefix:
      api-stats: "api:stats:"
  kafka:
    topics:
      operation-log: operation-log-topic
```

### 3.2 依赖项

已包含在 `pom.xml` 中：
- `spring-boot-starter-data-redis`
- `spring-kafka`
- `spring-boot-starter-aop`
- `commons-pool2` (Redis 连接池)

---

## 四、测试步骤

### 4.1 测试 Kafka 日志功能

1. 启动 MySQL、Redis、Kafka
2. 执行 `sys-log-table.sql` 创建日志表
3. 启动 Spring Boot 应用
4. 调用删除学生接口：
   ```bash
   curl -X DELETE http://localhost:8088/api/student/1
   ```
5. 查看 MySQL `sys_log` 表，确认日志已写入
6. 查看应用日志，确认 Kafka 消息发送和消费成功

### 4.2 测试 AOP 监控功能

1. 多次调用被监控的接口：
   ```bash
   # 调用学生列表接口 10 次
   for i in {1..10}; do
     curl "http://localhost:8088/api/student/list?page=1&size=10"
     sleep 1
   done
   ```

2. 查询监控统计：
   ```bash
   curl http://localhost:8088/api/monitor/stats
   ```

3. 验证统计数据：
   - `count` 应为 10
   - `avgDuration` 应为平均耗时
   - `lastCallTime` 应为最近一次调用时间

4. 查看 Redis 数据：
   ```bash
   redis-cli HGETALL "api:stats:查询学生列表"
   ```

---

## 五、扩展使用

### 5.1 添加更多监控接口

在任何 Controller 方法上添加 `@RequestMonitor` 注解：

```java
@PostMapping("/custom-api")
@RequestMonitor(value = "自定义接口", category = "custom")
public Result<?> customApi() {
    // 方法实现
}
```

### 5.2 扩展 Kafka 日志功能

可以在其他 Controller 中发送 Kafka 消息：

```java
@Autowired
private KafkaTemplate<String, String> kafkaTemplate;

private void sendCustomLog(...) {
    SystemLogMessage logMessage = SystemLogMessage.builder()
        .operationType("CUSTOM_OPERATION")
        .operator(getCurrentUsername())
        .operationTime(LocalDateTime.now())
        .build();

    String messageJson = JSON.toJSONString(logMessage);
    kafkaTemplate.send("system-log-topic", messageJson);
}
```

---

## 六、注意事项

1. **Kafka 连接**：确保 Kafka 服务在应用启动前已运行，否则会启动失败
2. **Redis 连接**：确保 Redis 密码配置正确（默认：redis123456）
3. **性能影响**：AOP 监控会增加少量开销（通常 < 5ms），建议仅在关键接口使用
4. **数据清理**：监控数据会持续累积，建议定期调用 `/monitor/stats/clear` 清理
5. **安全性**：生产环境建议对 `/monitor/**` 接口添加权限控制

---

## 七、故障排查

### Kafka 消息未消费
- 检查 Kafka 服务是否运行
- 查看应用日志中的 Kafka 错误信息
- 验证 Topic 是否创建成功：`kafka-topics.sh --list --bootstrap-server localhost:9092`

### AOP 监控未生效
- 确认方法上已添加 `@RequestMonitor` 注解
- 检查 Redis 连接是否正常
- 查看应用日志中的 AOP 错误信息

### Redis 统计数据异常
- 使用 `redis-cli` 检查数据是否存在
- 确认 Redis key 前缀配置正确
- 检查 Redis 内存是否充足

---

## 八、核心文件清单

### Kafka 相关
- `SysLog.java` - 日志实体类
- `SysLogMapper.java` - 日志 Mapper
- `SysLogService.java` / `SysLogServiceImpl.java` - 日志服务
- `SystemLogMessage.java` - Kafka 消息 DTO
- `SystemLogConsumer.java` - Kafka 消费者
- `KafkaConfig.java` - Kafka 配置
- `sys-log-table.sql` - 日志表 SQL

### AOP 监控相关
- `RequestMonitor.java` - 监控注解
- `RequestMonitorAspect.java` - AOP 切面
- `MonitorController.java` - 监控查询接口

---

## 总结

本项目成功实现了：
1. ✅ Kafka 消息队列处理系统日志
2. ✅ 学生删除操作自动发送 Kafka 消息
3. ✅ Kafka 消费者将日志写入 MySQL
4. ✅ AOP 切面拦截 `@RequestMonitor` 注解
5. ✅ Redis 统计接口调用次数和耗时
6. ✅ `/monitor/stats` 接口返回统计数据

所有功能已完成并可正常使用。
