# 403 错误调试指南

## 问题现状
前端登录后访问学生管理/排行榜/接口监控等页面时提示 403 拒绝访问。

## 已做的修改

### 1. 前端修改

#### `/frontend/src/utils/request.js`
- ✅ 添加了详细的请求和响应日志
- ✅ 请求拦截器会打印 token 和 Authorization header
- ✅ 响应拦截器会打印完整的响应数据和错误信息

#### `/frontend/src/views/Login.vue`
- ✅ 修正了 token 提取逻辑：`response.data.token` (而不是 `response.token`)
- ✅ 添加了详细的调试日志

## 调试步骤

### 步骤 1: 清理旧数据
1. 打开浏览器开发者工具 (F12)
2. 进入 Application/Storage 标签
3. 清除 localStorage 中的 token
4. 刷新页面

### 步骤 2: 登录测试
1. 在登录页面输入用户名和密码
2. 打开浏览器控制台 (Console 标签)
3. 点击登录按钮
4. 查看控制台输出，应该看到：
   ```
   响应拦截器 - 原始响应: {...}
   响应拦截器 - response.data: { code: 200, message: "登录成功", data: {...} }
   登录响应完整对象: { code: 200, message: "登录成功", data: { token: "...", ... } }
   登录响应data字段: { token: "...", userId: ..., ... }
   提取的token: eyJhbGciOiJIUzI1NiJ9...
   ```

### 步骤 3: 验证 Token 存储
1. 登录成功后，在 Console 中输入：
   ```javascript
   localStorage.getItem('token')
   ```
2. 应该看到一个 JWT token 字符串（以 `eyJ` 开头）

### 步骤 4: 测试访问受保护的页面
1. 导航到学生管理页面 `/students`
2. 查看控制台输出，应该看到：
   ```
   请求拦截器 - Token: eyJhbGciOiJIUzI1NiJ9...
   请求拦截器 - Authorization header: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```

### 步骤 5: 检查后端日志
如果仍然出现 403 错误，检查后端控制台日志：

1. 查找 JWT 认证相关日志：
   - 成功：`用户 xxx 认证成功`
   - 失败：`Token在Redis中不存在或已失效: xxx` 或 `JWT认证失败: xxx`

2. 查找登录相关日志：
   - `用户 xxx 登录成功，Token已存入Redis`

## 可能的问题和解决方案

### 问题 1: Token 未正确保存
**症状**: localStorage 中没有 token 或 token 值为 `undefined`

**原因**: 响应数据结构不匹配

**解决**: 查看登录响应的完整结构，确认 token 的实际位置

### 问题 2: Token 格式错误
**症状**: 请求头中的 Authorization 不是 `Bearer eyJxxx...` 格式

**原因**:
- Token 值为 null/undefined
- 前端没有添加 "Bearer " 前缀（已在 request.js 中添加）

**解决**:
- 确认 localStorage 中的 token 值
- 确认请求拦截器正确添加了 Bearer 前缀

### 问题 3: 后端 Redis 中没有 Token
**症状**: 后端日志显示 "Token在Redis中不存在或已失效"

**原因**:
- 登录时 token 没有保存到 Redis
- Redis 服务未启动或连接失败
- Token 已过期

**解决**:
1. 确认 Redis 服务运行中：`redis-cli ping` 应返回 `PONG`
2. 检查后端登录日志是否有 "Token已存入Redis"
3. 手动检查 Redis：
   ```bash
   redis-cli
   KEYS token:*
   GET token:你的用户名
   ```

### 问题 4: JWT Token 无效
**症状**: 后端日志显示 "JWT认证失败"

**原因**:
- Token 签名错误
- Token 格式错误
- Token 已过期

**解决**:
- 检查前后端的 JWT secret 是否一致
- 检查 Token 过期时间配置

## 后端相关配置

### JWT 配置 (application.yml)
```yaml
app:
  jwt:
    secret: StudentManagementSystemSecretKey2024ForJWTToken
    expiration: 86400000  # 24小时
    header: Authorization
    token-prefix: "Bearer "
```

### Security 放行路径 (SecurityConfig.java)
```java
.requestMatchers("/auth/login", "/auth/register").permitAll()
.requestMatchers("/student/**", "/score/**").permitAll()  // 临时放行
.requestMatchers("/monitor/**").permitAll()  // 临时放行
```

## 下一步操作

1. ✅ 已添加详细日志到前端
2. ✅ 已修正 token 提取逻辑
3. ⏳ 按照调试步骤测试登录流程
4. ⏳ 根据控制台输出确定问题点

## 联系信息
如果以上步骤都无法解决问题，请提供：
1. 浏览器控制台的完整输出
2. 后端日志的相关部分
3. localStorage 中 token 的实际值
