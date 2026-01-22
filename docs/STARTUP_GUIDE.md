# 启动和测试说明

## 问题修复

已将 MyBatis-Plus 版本从 3.5.5 升级到 3.5.7，解决了与 Spring Boot 3.2.1 的兼容性问题。

## 启动步骤

### 1. 插入测试数据
```bash
docker exec -i student_sys_mysql mysql -uroot -proot123456 student_sys < init-mysql/test-data.sql
```

### 2. 启动应用
```bash
cd backend
mvn spring-boot:run
```

### 3. 验证健康检查
```bash
curl http://localhost:8088/api/health
```

### 4. 测试登录
```bash
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 5. 测试管理员权限
```bash
# 将 YOUR_TOKEN 替换为上一步返回的 token
curl -X GET http://localhost:8088/api/test/admin \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6. 验证 Redis 中的 Token
```bash
docker exec -it student_sys_redis redis-cli -a redis123456
keys token:*
get token:admin
ttl token:admin
```

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| teacher | teacher123 | 教师 |
| student | student123 | 学生 |

## 预期结果

1. 应用正常启动，无错误
2. 登录成功返回 Token
3. Token 存储在 Redis 中
4. 管理员可以访问 /test/admin
5. 教师和学生访问 /test/admin 返回 403

详细测试步骤请查看: docs/AUTH_TEST_GUIDE.md
