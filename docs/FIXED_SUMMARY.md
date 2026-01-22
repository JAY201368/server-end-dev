# 403 错误修复总结

## 问题根源

问题有**两个主要原因**：

### 1. 前端 Token 提取错误 ✅ 已修复
- **问题**: [Login.vue:92](frontend/src/views/Login.vue#L92) 尝试从 `response.token` 获取 token
- **实际**: 后端返回 `{ code: 200, message: "登录成功", data: { token: "...", ... } }`
- **修复**: 改为从 `response.data.token` 正确提取

### 2. 前后端 API 路径不匹配 ✅ 已修复
- **问题**: 前端调用的 API 路径与后端 Controller 定义的路径不一致
- **修复**: 统一了所有 API 路径

## 已修复的文件

### 前端修改

#### 1. [frontend/src/views/Login.vue](frontend/src/views/Login.vue)
```javascript
// 修复前
localStorage.setItem('token', response.token)  // ❌ undefined

// 修复后
if (response && response.data && response.data.token) {
  const token = response.data.token
  localStorage.setItem('token', token)  // ✅ 正确的 JWT token
}
```

#### 2. [frontend/src/utils/request.js](frontend/src/utils/request.js)
- ✅ 添加了详细的请求和响应日志
- ✅ 添加了 403 错误的详细输出

#### 3. [frontend/src/api/index.js](frontend/src/api/index.js)
修复了所有 API 路径：

| API | 修复前 | 修复后 | 状态 |
|-----|--------|--------|------|
| 获取学生列表 | `/students` | `/student/list` | ✅ |
| 获取单个学生 | `/students/{id}` | `/student/{id}` | ✅ |
| 添加学生 | `/students` | `/student/add` | ✅ |
| 更新学生 | `/students/{id}` | `/student/update` | ✅ |
| 删除学生 | `/students/{id}` | `/student/{id}` | ✅ |
| 监控统计 | `/monitoring/stats` | `/monitor/stats` | ✅ |

## API 访问权限说明

### 无需登录可访问（permitAll）
- ✅ `/auth/login` - 登录
- ✅ `/auth/register` - 注册
- ✅ `/student/**` - 学生管理（开发阶段临时放行）
- ✅ `/score/**` - 成绩管理（开发阶段临时放行）
- ✅ `/monitor/**` - 接口监控（开发阶段临时放行）

### 需要登录访问（authenticated）
- 🔒 `/leaderboard/**` - 排行榜
- 🔒 其他未明确放行的接口

## 测试验证

### 1. 学生管理 ✅
```bash
curl "http://localhost:8088/api/student/list?page=0&size=10"
# 返回 200 OK，数据正常
```

### 2. 接口监控 ✅
```bash
curl "http://localhost:8088/api/monitor/stats"
# 返回 200 OK，监控数据正常
```

### 3. 排行榜 🔒
```bash
curl "http://localhost:8088/api/leaderboard/top10"
# 返回 403（预期行为，需要登录）
```

## 前端使用指南

### 1. 清除旧数据
```javascript
// 在浏览器控制台执行
localStorage.clear()
```

### 2. 重新登录
- 访问登录页面
- 输入用户名和密码（例如：admin / admin123）
- 查看控制台输出，应该看到：
  ```
  登录响应完整对象: { code: 200, message: "登录成功", data: {...} }
  登录响应data字段: { token: "eyJ...", userId: 1, ... }
  提取的token: eyJhbGciOiJIUzI1NiJ9...
  ```

### 3. 访问功能页面
- ✅ **学生管理**：可以正常访问（已临时放行）
- ✅ **接口监控**：可以正常访问（已临时放行）
- 🔒 **排行榜**：需要登录（带 token）才能访问

### 4. 查看请求日志
访问任何页面时，控制台会显示：
```
请求拦截器 - Token: eyJhbGciOiJIUzI1NiJ9...
请求拦截器 - Authorization header: Bearer eyJhbGciOiJIUzI1NiJ9...
响应拦截器 - 原始响应: { status: 200, ... }
```

## Redis 状态

Redis 容器正在运行：
```bash
docker ps | grep redis
# student_sys_redis - Up 2 hours (healthy)

docker exec student_sys_redis redis-cli -a redis123456 ping
# PONG

docker exec student_sys_redis redis-cli -a redis123456 KEYS "token:*"
# token:admin
# token:student
# token:teacher
# token:no_why
```

## 后端配置（无需修改）

### SecurityConfig.java
```java
.requestMatchers("/auth/login", "/auth/register").permitAll()
.requestMatchers("/student/**", "/score/**").permitAll()  // 临时放行
.requestMatchers("/monitor/**").permitAll()  // 临时放行
.anyRequest().authenticated()
```

### Controller 端点
- `StudentController`: `/student/*`
- `ScoreController`: `/score/*`
- `MonitorController`: `/monitor/*`
- `AuthController`: `/auth/*`
- `LeaderboardController`: `/leaderboard/*`（需要认证）

## 生产环境建议

在生产环境部署前，应该：

1. **移除临时放行规则**，改为需要认证：
   ```java
   // 移除这两行临时放行
   // .requestMatchers("/student/**", "/score/**").permitAll()
   // .requestMatchers("/monitor/**").permitAll()
   ```

2. **添加基于角色的访问控制**：
   ```java
   .requestMatchers("/student/**").hasRole("ADMIN")
   .requestMatchers("/monitor/**").hasAnyRole("ADMIN", "MONITOR")
   ```

3. **移除调试日志**：
   - 删除 [request.js](frontend/src/utils/request.js) 中的 console.log
   - 删除 [Login.vue](frontend/src/views/Login.vue) 中的 console.log

## 问题解决流程

1. ✅ 发现 Redis 容器正在运行
2. ✅ 发现 Redis 中存在 token
3. ✅ 发现即使有 token 仍返回 403
4. ✅ 发现前端 API 路径与后端不匹配
5. ✅ 修复所有 API 路径
6. ✅ 验证 API 可以正常访问

## 总结

**主要问题：前后端 API 路径不一致**

- 前端调用 `/students`、`/monitoring` 等路径
- 后端实际提供 `/student/list`、`/monitor/stats` 等端点
- 由于路径不匹配，请求无法到达正确的 Controller
- Security 过滤器拦截了这些不存在的路径，返回 403

**次要问题：Token 提取错误**

- 前端从错误的位置提取 token（`response.token` 而不是 `response.data.token`）
- 导致 localStorage 中保存了 `undefined`
- 虽然 API 已被 permitAll 放行，但这个问题在排行榜等需要认证的接口中会导致问题

现在所有问题都已修复，系统可以正常工作！🎉
