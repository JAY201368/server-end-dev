# Thymeleaf 集成说明文档

## 1. 概述

本项目采用 **混合模式** 架构，同时使用 **Thymeleaf 模板引擎** 和 **Vue3 前后端分离**，以满足作业评分要求：

- ✅ **Thymeleaf 模板引擎（8分）** - 用于渲染系统门户首页
- ✅ **Vue3 前后端分离（10分）** - 作为主要的管理系统界面

---

## 2. 架构设计

### 2.1 混合模式架构

```
用户访问流程：
1. 访问 http://localhost:8088/api/ 
   → Thymeleaf 渲染门户首页（index.html）
   
2. 点击"进入管理系统"按钮
   → 跳转到 Vue3 应用 (http://localhost:5173)
   
3. Vue3 应用通过 RESTful API 与后端交互
   → Spring Boot 提供 JSON 数据接口
```

### 2.2 技术栈

| 层级 | 技术 | 用途 |
|------|------|------|
| 门户首页 | Thymeleaf | 服务端渲染系统入口页面 |
| 管理系统 | Vue 3 | 前后端分离的 SPA 应用 |
| 后端 API | Spring Boot | RESTful API 服务 |
| 数据库 | MySQL + Redis | 数据持久化和缓存 |

---

## 3. Thymeleaf 页面

### 3.1 页面列表

| 路径 | 模板文件 | 说明 |
|------|----------|------|
| `/` | `index.html` | 系统门户首页 |
| `/notice` | `notice.html` | 系统公告页面 |
| `/about` | `about.html` | 关于系统页面 |

### 3.2 首页功能

**index.html** 包含以下内容：

1. **系统信息展示**
   - 系统名称
   - 版本号
   - 当前时间（服务端渲染）
   - 系统描述

2. **核心功能展示**
   - JWT + Redis 认证
   - RBAC 权限控制
   - Redis ZSet 排名
   - Kafka 异步日志
   - AOP 接口监控
   - Docker 容器化

3. **操作按钮**
   - 🚀 **进入管理系统** - 跳转到 Vue3 应用
   - 📖 **了解更多** - 查看系统详情

4. **技术栈展示**
   - Spring Boot 3.x
   - Vue 3
   - Thymeleaf
   - MySQL 8.0
   - Redis
   - Kafka

5. **页面链接**
   - 系统公告
   - 关于系统
   - Kafka 监控

---

## 4. Controller 实现

### 4.1 IndexController

```java
@Controller
public class IndexController {
    
    @GetMapping("/")
    public String index(Model model) {
        // 传递数据到 Thymeleaf 模板
        model.addAttribute("systemName", "学生信息管理系统");
        model.addAttribute("systemVersion", "v1.0.0");
        model.addAttribute("currentTime", LocalDateTime.now()...);
        model.addAttribute("description", "...");
        
        return "index";  // 返回模板名称
    }
    
    @GetMapping("/notice")
    public String notice(Model model) {
        model.addAttribute("notices", ...);
        return "notice";
    }
    
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("features", ...);
        return "about";
    }
}
```

### 4.2 关键点

- 使用 `@Controller` 注解（不是 `@RestController`）
- 返回值是模板名称（String）
- 通过 `Model` 对象传递数据到模板

---

## 5. Thymeleaf 配置

### 5.1 Maven 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 5.2 application.yml 配置

```yaml
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    mode: HTML
    encoding: UTF-8
    cache: false  # 开发环境关闭缓存
```

### 5.3 Security 配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            // 放行 Thymeleaf 页面
            .requestMatchers("/", "/index", "/notice", "/about").permitAll()
            // 放行静态资源
            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
            // ...
        );
        return http.build();
    }
}
```

---

## 6. 模板语法示例

### 6.1 变量输出

```html
<!-- 输出文本 -->
<h1 th:text="${systemName}">默认文本</h1>

<!-- 输出HTML（不转义） -->
<div th:utext="${htmlContent}"></div>

<!-- 属性绑定 -->
<a th:href="${url}">链接</a>
<img th:src="${imageUrl}" />
```

### 6.2 条件判断

```html
<!-- if 条件 -->
<div th:if="${user != null}">
    欢迎, <span th:text="${user.name}"></span>
</div>

<!-- unless 条件（相当于 if not） -->
<div th:unless="${user != null}">
    请先登录
</div>

<!-- switch 条件 -->
<div th:switch="${role}">
    <p th:case="'admin'">管理员</p>
    <p th:case="'teacher'">教师</p>
    <p th:case="*">其他</p>
</div>
```

### 6.3 循环遍历

```html
<!-- 遍历列表 -->
<ul>
    <li th:each="item : ${items}" th:text="${item}">项目</li>
</ul>

<!-- 带索引的遍历 -->
<ul>
    <li th:each="item, stat : ${items}">
        <span th:text="${stat.index}">0</span>: 
        <span th:text="${item}">项目</span>
    </li>
</ul>
```

### 6.4 URL 链接

```html
<!-- 相对路径 -->
<a th:href="@{/about}">关于</a>

<!-- 带参数 -->
<a th:href="@{/user/{id}(id=${user.id})}">用户详情</a>

<!-- 带查询参数 -->
<a th:href="@{/search(keyword=${keyword},page=${page})}">搜索</a>
```

---

## 7. 访问测试

### 7.1 启动应用

```bash
# 1. 启动 Docker 服务
docker-compose up -d

# 2. 启动 Spring Boot 后端
cd backend
mvn spring-boot:run

# 3. 启动 Vue3 前端
cd frontend
npm run dev
```

### 7.2 访问页面

| 页面 | URL | 说明 |
|------|-----|------|
| 门户首页 | http://localhost:8088/api/ | Thymeleaf 渲染 |
| 系统公告 | http://localhost:8088/api/notice | Thymeleaf 渲染 |
| 关于系统 | http://localhost:8088/api/about | Thymeleaf 渲染 |
| Vue3 管理系统 | http://localhost:5173 | Vue3 SPA |

### 7.3 测试流程

1. ✅ 访问 `http://localhost:8088/api/`
2. ✅ 查看 Thymeleaf 渲染的门户首页
3. ✅ 点击"进入管理系统"按钮
4. ✅ 跳转到 Vue3 应用
5. ✅ 在 Vue3 应用中登录并使用系统功能

---

## 8. 页面截图说明

### 8.1 门户首页特点

- 🎨 **现代化设计** - 渐变背景、圆角卡片、阴影效果
- 📱 **响应式布局** - 支持移动端和桌面端
- ✨ **动画效果** - 淡入动画、悬停效果
- 🎯 **清晰导航** - 显著的"进入管理系统"按钮

### 8.2 页面元素

1. **Logo** - 🎓 图标
2. **标题** - 系统名称 + 版本号
3. **描述** - 系统简介
4. **功能列表** - 6个核心功能卡片
5. **操作按钮** - 进入管理系统、了解更多
6. **技术栈** - 展示使用的技术
7. **页脚** - 当前时间、快速链接、版权信息

---

## 9. 与 Vue3 的集成

### 9.1 数据流

```
Thymeleaf 页面 (门户)
    ↓
点击"进入管理系统"
    ↓
跳转到 Vue3 应用
    ↓
Vue3 调用 RESTful API
    ↓
Spring Boot 返回 JSON 数据
```

### 9.2 API 接口

Vue3 应用通过以下 API 与后端交互：

```javascript
// 登录
POST /api/auth/login

// 获取学生列表
GET /api/student/list

// 添加学生
POST /api/student/add

// 获取成绩排行榜
GET /api/score/ranking
```

---

## 10. 优势说明

### 10.1 满足作业要求

✅ **Thymeleaf 模板引擎（8分）**
- 使用 Thymeleaf 渲染门户首页
- 服务端渲染，SEO 友好
- 展示系统信息和公告

✅ **Vue3 前后端分离（10分）**
- Vue3 作为主要管理系统
- 前后端完全分离
- RESTful API 交互

### 10.2 技术优势

1. **最佳实践** - 门户用 SSR，管理系统用 SPA
2. **性能优化** - 首页快速加载，管理系统交互流畅
3. **SEO 友好** - Thymeleaf 渲染的页面利于搜索引擎收录
4. **用户体验** - 清晰的入口，流畅的跳转

---

## 11. 常见问题

### Q1: 为什么要同时使用 Thymeleaf 和 Vue3？

**A:** 作业要求必须同时使用模板引擎和前后端分离。我们采用混合模式：
- Thymeleaf 渲染门户首页（满足模板引擎要求）
- Vue3 作为主要管理系统（满足前后端分离要求）

### Q2: Thymeleaf 页面如何跳转到 Vue3 应用？

**A:** 在 Thymeleaf 页面中添加链接：
```html
<a href="http://localhost:5173">进入管理系统</a>
```

### Q3: 如何修改 Thymeleaf 页面内容？

**A:** 
1. 修改模板文件：`backend/src/main/resources/templates/index.html`
2. 修改 Controller：`IndexController.java`
3. 重启应用或等待热部署生效

### Q4: 生产环境如何部署？

**A:** 
1. 将 Vue3 应用打包：`npm run build`
2. 将打包文件放到 Spring Boot 的 `static` 目录
3. 修改 Thymeleaf 页面的跳转链接为相对路径：`/app`

---

## 12. 总结

本项目成功实现了 Thymeleaf 和 Vue3 的混合模式：

✅ **Thymeleaf 部分**
- 门户首页（index.html）
- 系统公告（notice.html）
- 关于系统（about.html）
- 服务端渲染，数据动态绑定

✅ **Vue3 部分**
- 完整的管理系统界面
- 前后端分离架构
- RESTful API 交互

✅ **集成方式**
- 清晰的入口导航
- 流畅的页面跳转
- 统一的视觉风格

**满足作业评分要求：Thymeleaf（8分）+ Vue3（10分）= 18分** 🎉
