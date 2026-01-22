# Student Management System Backend

## 项目说明
基于 Spring Boot 3 + Vue 3 的高性能学生信息管理系统后端服务

## 技术栈
- Spring Boot 3.2.1
- Java 17
- MyBatis-Plus 3.5.5
- Spring Security + JWT
- Redis + Kafka
- MySQL 8.0

## 快速开始

### 1. 启动 Docker 环境
```bash
cd ..
docker-compose up -d
./verify-services.sh
```

### 2. 编译项目
```bash
mvn clean install -DskipTests
```

### 3. 运行应用
```bash
mvn spring-boot:run
```

### 4. 访问测试
```bash
curl http://localhost:8088/api/health
```

## 详细文档
请查看 [README.md](README.md) 获取完整的项目说明。
