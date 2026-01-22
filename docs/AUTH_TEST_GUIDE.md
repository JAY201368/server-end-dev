# JWT + Redis + RBAC 认证测试指南

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | ROLE_ADMIN | 管理员，拥有所有权限 |
| teacher | teacher123 | ROLE_TEACHER | 教师，可管理学生和成绩 |
| student | student123 | ROLE_STUDENT | 学生，只能查看信息 |

## 测试步骤

### 1. 启动服务

```bash
# 确保 Docker 服务运行
docker-compose up -d

# 插入测试数据
docker exec -i student_sys_mysql mysql -uroot -proot123456 student_sys < init-mysql/test-data.sql

# 启动 Spring Boot
cd backend
mvn spring-boot:run
```

### 2. 测试登录接口

#### 管理员登录
```bash
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "admin",
    "nickname": "系统管理员",
    "roles": ["ROLE_ADMIN"],
    "permissions": ["user:view", "user:add", ...]
  },
  "timestamp": 1737449000000
}
```

#### 教师登录
```bash
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teacher",
    "password": "teacher123"
  }'
```

#### 学生登录
```bash
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student",
    "password": "student123"
  }'
```

### 3. 测试权限接口

#### 测试管理员权限（应该成功）
```bash
# 替换 YOUR_TOKEN 为登录返回的 token
curl -X GET http://localhost:8088/api/test/admin \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应：**
```json
{
  "code": 200,
  "message": "管理员权限验证成功",
  "data": {
    "message": "恭喜！您拥有管理员权限",
    "username": "admin",
    "authorities": [
      {"authority": "ROLE_ADMIN"},
      {"authority": "user:view"},
      ...
    ]
  },
  "timestamp": 1737449100000
}
```

#### 测试教师访问管理员接口（应该失败）
```bash
# 使用 teacher 的 token
curl -X GET http://localhost:8088/api/test/admin \
  -H "Authorization: Bearer TEACHER_TOKEN"
```

**预期响应：**
```
403 Forbidden
```

#### 测试教师权限接口（应该成功）
```bash
curl -X GET http://localhost:8088/api/test/teacher \
  -H "Authorization: Bearer TEACHER_TOKEN"
```

#### 测试学生权限接口（应该成功）
```bash
curl -X GET http://localhost:8088/api/test/student \
  -H "Authorization: Bearer STUDENT_TOKEN"
```

### 4. 验证 Redis 中的 Token

```bash
# 连接 Redis
docker exec -it student_sys_redis redis-cli -a redis123456

# 查看所有 token key
keys token:*

# 查看具体用户的 token
get token:admin

# 验证 token 过期时间（秒）
ttl token:admin
```

**预期输出：**
```
1) "token:admin"
2) "token:teacher"
3) "token:student"

# token 值应该与登录返回的一致
"eyJhbGciOiJIUzI1NiJ9..."

# TTL 应该接近 86400（24小时）
(integer) 86395
```

### 5. 测试登出

```bash
curl -X POST http://localhost:8088/api/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应：**
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1737449200000
}
```

**验证 Redis 中 Token 已删除：**
```bash
docker exec -it student_sys_redis redis-cli -a redis123456
get token:admin
```
应该返回 `(nil)`

### 6. 测试 Token 失效后访问

```bash
# 登出后，再次使用旧 token 访问
curl -X GET http://localhost:8088/api/test/admin \
  -H "Authorization: Bearer OLD_TOKEN"
```

**预期响应：**
```
401 Unauthorized
```

## 得分点验证清单

### ✅ JWT + Redis 登录认证（10分）
- [x] Token 存储在 Redis 中
- [x] 登录时生成 JWT Token
- [x] Token 设置过期时间（24小时）
- [x] 登出时从 Redis 删除 Token

### ✅ RBAC 权限持久化（10分）
- [x] 用户、角色、权限存储在 MySQL
- [x] 通过 UserDetailsService 从 MySQL 加载权限
- [x] Spring Security 动态鉴权
- [x] @PreAuthorize 注解控制接口权限

## 架构说明

```
登录流程：
用户登录 → 验证密码 → 生成JWT Token → 存入Redis → 返回Token

请求认证流程：
请求到达 → JwtAuthenticationFilter
  → 从Header获取Token
  → 从Redis验证Token是否存在
  → 从JWT解析用户名
  → 加载用户权限（从MySQL）
  → 设置到SecurityContext
  → 继续请求处理
```

## 故障排查

### Token 验证失败
1. 检查 Redis 中是否存在 token
2. 检查 JWT secret 配置
3. 检查 Token 是否过期
4. 查看后端日志

### 权限验证失败
1. 检查数据库中的角色权限配置
2. 查看 UserDetailsService 日志
3. 确认 @PreAuthorize 注解的角色名称

### 数据库连接失败
1. 确认 MySQL 容器运行
2. 检查端口 3307
3. 验证数据库用户名密码
