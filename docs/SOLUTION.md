# 403 错误解决方案

## 问题根源

**Redis 服务未启动！**

系统使用 Redis 存储 JWT token，当用户登录时：
1. 后端生成 JWT token
2. 将 token 存储到 Redis（key: `token:username`，value: token字符串）
3. 返回 token 给前端

当用户访问受保护的资源时：
1. 前端在请求头中携带 `Authorization: Bearer <token>`
2. 后端 [JwtAuthenticationFilter.java:64](backend/src/main/java/com/student/system/security/JwtAuthenticationFilter.java#L64) 会验证 token 是否在 Redis 中存在
3. **如果 Redis 未运行，验证失败，返回 403**

## 解决步骤

### 方案 1: 启动 Redis 服务（推荐）

#### 检查 Redis 是否安装
```bash
brew list | grep redis
```

#### 如果未安装，先安装 Redis
```bash
brew install redis
```

#### 启动 Redis 服务
```bash
# 方式1: 使用 brew services 后台运行（推荐）
brew services start redis

# 方式2: 直接启动（前台运行，终端关闭则停止）
redis-server
```

#### 验证 Redis 运行状态
```bash
# 检查服务状态
brew services list | grep redis

# 或者测试连接
redis-cli ping
# 应该返回: PONG

# 使用密码连接（根据application.yml配置）
redis-cli -a redis123456 ping
```

#### 重启后端应用
Redis 启动后，重启后端服务以建立新的 Redis 连接。

### 方案 2: 修改代码移除 Redis 依赖（不推荐）

如果您不想使用 Redis，需要修改以下文件：

#### 1. 修改 [JwtAuthenticationFilter.java:64](backend/src/main/java/com/student/system/security/JwtAuthenticationFilter.java#L64)

```java
// 注释掉 Redis 验证
// if (redisUtil.validateToken(username, token)) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    if (jwtUtil.validateToken(token, username)) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("用户 {} 认证成功", username);
    }
// }
```

#### 2. 修改 [AuthServiceImpl.java:80](backend/src/main/java/com/student/system/service/impl/AuthServiceImpl.java#L80)

```java
// 注释掉 Redis 存储
// redisUtil.setToken(username, token);
```

#### 3. 修改 [AuthServiceImpl.java:172](backend/src/main/java/com/student/system/service/impl/AuthServiceImpl.java#L172)

```java
// 注释掉 Redis 删除
// redisUtil.deleteToken(username);
```

**注意：** 方案2会导致：
- 无法主动让 token 失效（登出不生效）
- 无法实现单点登录（踢出其他设备）
- Token 只能通过过期时间自然失效

## 验证修复

### 1. 启动 Redis 后验证
```bash
# 测试登录
curl -X POST "http://localhost:8088/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 提取token，然后测试访问受保护资源
TOKEN="<从上面响应中复制token>"
curl -X GET "http://localhost:8088/api/students?page=0&size=10" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2. 前端测试
1. 清除浏览器 localStorage: `localStorage.clear()`
2. 刷新页面，重新登录
3. 查看浏览器控制台日志，应该看到正确的 token
4. 导航到学生管理页面，应该能正常访问

### 3. 检查 Redis 中的数据
```bash
redis-cli -a redis123456

# 查看所有 token keys
KEYS token:*

# 查看特定用户的 token
GET token:admin

# 查看 key 的过期时间（秒）
TTL token:admin
```

## 前端已修复的问题

我已经修复了前端的 token 提取逻辑：

### 修改的文件

1. **[frontend/src/views/Login.vue:97](frontend/src/views/Login.vue#L97)**
   - 正确提取 token: `response.data.token`
   - 添加了详细的调试日志

2. **[frontend/src/utils/request.js:18-21](frontend/src/utils/request.js#L18-L21)**
   - 添加了请求拦截器日志
   - 添加了响应拦截器日志
   - 添加了 403 错误详情输出

## 配置信息

### Redis 配置 ([application.yml:31-44](backend/src/main/resources/application.yml#L31-L44))
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: redis123456
      database: 0
```

### Token 配置 ([application.yml:152-180](backend/src/main/resources/application.yml#L152-L180))
```yaml
app:
  jwt:
    secret: StudentManagementSystemSecretKey2024ForJWTToken
    expiration: 86400000  # 24小时
    header: Authorization
    token-prefix: "Bearer "
  redis:
    key-prefix:
      token: "token:"
    token-expire: 86400  # 24小时（秒）
```

## 常见问题

### Q: Redis 密码错误
**A:** 检查 [application.yml:35](backend/src/main/resources/application.yml#L35) 中的密码配置是否与 Redis 实际密码一致

### Q: Redis 端口冲突
**A:** 检查 6379 端口是否被占用：
```bash
lsof -i :6379
```

### Q: 前端仍然提示 403
**A:**
1. 确认 Redis 正在运行
2. 清除浏览器 localStorage
3. 重新登录
4. 检查浏览器控制台日志

### Q: Token 保存成功但仍然 403
**A:**
1. 检查后端日志，查找 "Token在Redis中不存在或已失效"
2. 验证 Redis 中是否有对应的 key: `redis-cli -a redis123456 KEYS token:*`
3. 检查 token 是否过期: `redis-cli -a redis123456 TTL token:username`

## 总结

**主要问题：Redis 服务未启动**

**快速解决：**
```bash
# 1. 启动 Redis
brew services start redis

# 2. 验证 Redis
redis-cli -a redis123456 ping

# 3. 清除浏览器缓存并重新登录
```

前端的 token 提取逻辑已经修复，一旦 Redis 启动，整个认证流程应该可以正常工作。
