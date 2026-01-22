# 后端项目结构说明

## 项目架构

```
backend/
├── src/main/java/com/student/system/
│   ├── StudentSystemApplication.java    # 应用启动类
│   ├── aspect/                          # AOP 切面（日志、权限等）
│   ├── common/                          # 公共类（统一响应、常量等）
│   ├── config/                          # 配置类（Redis、Security、Kafka等）
│   ├── controller/                      # 控制器层
│   ├── dto/                            # 数据传输对象
│   ├── entity/                         # 实体类
│   ├── exception/                      # 异常处理
│   ├── kafka/                          # Kafka 生产者/消费者
│   ├── mapper/                         # MyBatis Mapper 接口
│   ├── security/                       # Spring Security 配置
│   ├── service/                        # 业务逻辑层
│   ├── util/                           # 工具类
│   └── vo/                             # 视图对象
├── src/main/resources/
│   ├── application.yml                 # 主配置文件
│   ├── mapper/                         # MyBatis XML 映射文件
│   ├── static/                         # 静态资源
│   └── templates/                      # Thymeleaf 模板
└── pom.xml                             # Maven 依赖配置
```

## 技术栈

- **Spring Boot 3.2.1** - 基础框架
- **Java 17** - 开发语言
- **MyBatis-Plus 3.5.5** - ORM 框架
- **Spring Security** - 安全认证
- **JWT (JJWT 0.12.3)** - Token 认证
- **Redis (Spring Data Redis)** - 缓存
- **Kafka (Spring Kafka)** - 消息队列
- **MySQL 8.0** - 数据库
- **Lombok** - 简化代码
- **Hutool** - 工具类库

## 数据库设计

### 权限管理表（RBAC模型）
- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_permission` - 权限表
- `sys_user_role` - 用户角色关联表
- `sys_role_permission` - 角色权限关联表

### 业务表
- `student` - 学生信息表（含头像字段）
- `course` - 课程表
- `score` - 成绩表

### 日志统计表
- `operation_log` - 操作日志表（Kafka消费写入）
- `api_stats` - 接口访问统计表

## 配置说明

### 数据库连接
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/student_sys
    username: student_user
    password: student_pass
```

### Redis 配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: redis123456
```

### Kafka 配置
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

## 启动步骤

1. **确保 Docker 服务已启动**
   ```bash
   docker-compose up -d
   ```

2. **编译项目**
   ```bash
   cd backend
   mvn clean install
   ```

3. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

4. **访问健康检查**
   ```
   http://localhost:8088/api/health
   ```

## 核心功能映射

### 得分点实现计划

1. **JWT + Redis 登录认证（10分）**
   - `security/` - Spring Security 配置
   - `util/JwtUtil.java` - JWT 工具类
   - Redis 存储 Token

2. **RBAC 权限控制（10分）**
   - 基于 MySQL 的权限持久化
   - Spring Security 动态权限鉴权

3. **Redis ZSet 成绩排名（10分）**
   - `service/ScoreRankService.java` - 成绩排名服务

4. **Kafka 异步日志（10分）**
   - `kafka/producer/` - 日志生产者
   - `kafka/consumer/` - 日志消费者

5. **AOP 接口统计（10分）**
   - `aspect/ApiStatsAspect.java` - 接口统计切面
   - 结合 Redis + MySQL 存储

6. **图片上传展示（8分）**
   - `controller/FileController.java` - 文件上传
   - 学生头像字段

7. **Docker 部署（15分）**
   - `Dockerfile` - 后端镜像
   - `docker-compose.yml` - 一键启动

## 下一步开发

1. 实现 JWT + Security 认证
2. 开发用户、学生、课程 CRUD
3. 实现 Redis 成绩排名
4. 集成 Kafka 异步日志
5. AOP 接口统计
6. 文件上传功能
7. 前端集成
