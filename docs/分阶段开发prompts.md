这是一份非常详尽的作业要求。为了确保你能顺利拿到高分，我将这一项目拆解为**一份面向高分的需求规格说明书**和**一套分阶段的Claude交互指令（Prompts）**。

这种开发方式的核心在于：**不要试图一次性让AI生成所有代码**，而是像项目经理一样，分模块、分技术点地指挥它。

---

### 第一部分：需求规格说明书 (Project Requirements Document)

**项目名称：** 基于Spring Boot与Vue3的高性能学生信息管理系统
**技术架构：**
*   **后端：** Java 17+, Spring Boot 3.x, MyBatis-Plus
*   **前端：** Vue 3 (Composition API) + Element Plus (作为SPA主应用), Thymeleaf (作为入口或特定展示页)
*   **数据库：** MySQL 8.0
*   **中间件：** Redis (缓存与会话), Kafka (异步消息处理)
*   **部署：** Docker Compose

**核心功能与得分点映射：**

1.  **系统模块 (Security & Deployment)**
    *   **登录认证：** 使用JWT + Redis存储Token，实现有状态管理（对应：*基于Redis的用户登录 10分*）。
    *   **权限控制：** 基于RBAC模型（用户-角色-权限），权限数据存入MySQL，通过Spring Security动态鉴权（对应：*权限持久化存储 10分*）。
    *   **部署：** 编写Dockerfile和docker-compose.yml一键启动（对应：*docker-compose 15分*）。

2.  **业务模块 (Basic Functions)**
    *   **学生/课程管理：** 标准CRUD，支持图片上传（头像）（对应：*前端图片展示 8分*）。
    *   **成绩排名：** 使用Redis的 `ZSet` 数据结构存储学生成绩并进行实时排行（对应：*Redis解决特定场景 10分*）。
    *   **异步日志/通知：** 关键操作（如删除学生）发送Kafka消息，消费者异步写入操作日志数据库（对应：*Kafka生产消费 10分*）。

3.  **监控模块 (Monitoring)**
    *   **接口统计：** 使用AOP拦截Controller请求，统计访问次数和耗时，存入Redis，并在前端图表展示（对应：*AOP+Redis+前端展示 10分*）。

---

### 第二部分：分阶段开发 Prompts (复制给 Claude Code 使用)

请按照以下顺序，一步步发送给Claude。每一步完成后，请在本地运行测试，确保无误再进行下一步。

#### 阶段一：环境搭建与基础设施 (Docker & Scaffold)
**目标：** 建立项目骨架，启动MySQL, Redis, Kafka。

> **Prompt 1:**
> 我需要开发一个学生信息管理系统，基于Spring Boot 3, Vue 3, MySQL, Redis, Kafka。
> 请首先帮我编写 `docker-compose.yml` 文件。
> **要求：**
> 1. 包含 MySQL 8.0 (设置root密码，创建数据库 `student_sys`)。
> 2. 包含 Redis (设置密码)。
> 3. 包含 Zookeeper 和 Kafka (配置好监听端口，确保容器间通信正常)。
> 4. 包含 Kafka-UI (可选，方便查看消息)。
> 5. 请提供启动这些容器的命令，并告诉我如何验证它们已启动成功。

*(执行并验证容器启动后，进入下一步)*

#### 阶段二：后端项目初始化与数据库设计
**目标：** 搭建Spring Boot，设计满足RBAC和业务的表结构。

> **Prompt 2:**
> 容器环境已就绪。现在请帮我初始化后端 Spring Boot 项目。
> **要求：**
> 1. 给出 `pom.xml` 配置，包含：Spring Web, MySQL Driver, MyBatis-Plus, Redis (Spring Data Redis), Kafka (Spring Kafka), Spring Security, JWT (JJC), Lombok, AOP。
> 2. 设计数据库表结构（SQL脚本），包含以下表：
>    - `sys_user` (用户), `sys_role` (角色), `sys_permission` (权限), `sys_user_role`, `sys_role_permission`。
>    - `student` (学生信息，包含头像url字段), `course` (课程), `score` (成绩)。
>    - `sys_log` (操作日志，用于后续Kafka消费者写入)。
> 3. 请确保遵循数据库设计规范，提供建表SQL。
> 4. 配置 `application.yml` 连接到本地Docker环境。

#### 阶段三：安全认证模块 (Security & Redis)
**目标：** 拿满“安全管理”的20分。

> **Prompt 3:**
> 现在实现登录和权限认证模块。
> **评分要求：** 必须使用 **Redis** 存储登录Token，权限必须 **持久化在MySQL** 中。
> **任务：**
> 1. 实现基于 RBAC 的 `UserDetailService`。
> 2. 编写 JWT 工具类。
> 3. 编写登录接口 `/auth/login`：验证密码后生成Token，将Token存入Redis（设置过期时间），返回Token给前端。
> 4. 配置 Spring Security 过滤器链，拦截请求验证Header中的Token，并从Redis校验有效性。
> 5. 编写一个测试接口 `/test/admin`，仅允许拥有 'ADMIN' 权限的用户访问，用于验证。

#### 阶段四：业务功能与 Redis 高级应用
**目标：** 实现CRUD，并使用Redis ZSet解决排名问题。

> **Prompt 4:**
> 接下来实现核心业务功能。
> **任务：**
> 1. 使用 MyBatis-Plus 生成 `Student` 和 `Score` 表的 Controller, Service, Mapper 代码。
> 2. 实现“录入成绩”接口：保存到MySQL的同时，使用 **Redis ZSet** 数据结构存储 `(studentId, score)`，用于快速获取排名。
> 3. 实现“获取成绩排行榜”接口：直接从 Redis ZSet 中获取前10名学生ID和分数。
> 4. 确保代码中包含必要的注释。

#### 阶段五：AOP监控与 Kafka 消息队列
**目标：** 拿满“基本功能”中的统计和MQ分数。

> **Prompt 5:**
> 现在需要集成 Kafka 和 AOP 监控功能。
> **评分要求：** 使用AOP+Redis统计接口调用，使用Kafka处理消息。
> **任务：**
> 1. **Kafka部分：**
>    - 定义一个 Kafka Topic `system-log-topic`。
>    - 在 `StudentController` 的“删除学生”接口中，使用 `KafkaTemplate` 发送一条消息（包含操作人、时间、被删除ID）。
>    - 编写一个 Kafka 消费者，监听该 Topic，收到消息后将日志写入 MySQL 的 `sys_log` 表。
> 2. **AOP监控部分：**
>    - 定义一个注解 `@RequestMonitor`。
>    - 编写 AOP 切面，拦截所有加了该注解的方法。
>    - 统计接口耗时，并将 `(接口名, 调用次数)` 累加存入 Redis Hash 结构中。
>    - 提供一个接口 `/monitor/stats` 返回Redis中的统计数据。

#### 阶段六：前端 Vue3 实现 (前后端分离)
**目标：** 实现SPA应用，拿满前端分数。

> **Prompt 6:**
> 后端接口已完成，现在开始写前端。请使用 Vue 3 + Vite + Element Plus + Axios。
> **任务：**
> 1. 给出项目初始化命令和依赖安装命令。
> 2. 封装 Axios：实现请求拦截（自动带上Token）和响应拦截（处理401状态跳转登录页）。
> 3. **页面开发：**
>    - **登录页：** 漂亮的CSS样式，调用后端登录接口。
>    - **后台布局：** 侧边栏导航，顶部栏。
>    - **学生管理页：** 表格展示学生，支持分页，有“添加”和“删除”按钮（删除后触发后端的Kafka逻辑）。需要展示学生头像图片（满足图片展示要求）。
>    - **排行榜页：** 调用后端的Redis ZSet接口展示前10名。
>    - **接口监控页：** 使用 ECharts 或简单的表格，展示从后端获取的 AOP 统计数据。

#### 阶段七：Thymeleaf 集成 (混合模式)
**目标：** 满足作业中“同时使用Vue3和Thymeleaf”的特殊要求。

> **Prompt 7:**
> 作业评分标准要求必须使用 Thymeleaf 模板引擎 (8分) 和 前后端分离 Vue (10分)。
> 为了同时满足这两个要求，请帮我调整后端配置：
> **任务：**
> 1. 在 Spring Boot 中引入 Thymeleaf 依赖。
> 2. 创建一个 Controller，映射根路径 `/`。
> 3. 编写一个 `index.html` (Thymeleaf模板)，作为系统的“门户首页”或“公告页”。
> 4. 在该页面中放置一个显著的按钮“进入管理系统”，点击跳转到 Vue 项目的地址（或者如果是打包在一起，跳转到 /app）。
> *注：这样既使用了模板引擎渲染首页，又保留了Vue作为主系统的分离架构。*

#### 阶段八：最终部署
**目标：** 交付 Docker 部署方案。

> **Prompt 8:**
> 项目开发完成。现在请帮我编写最终的部署文件。
> **任务：**
> 1. 编写后端的 `Dockerfile` (基于 openjdk:17)。
> 2. 编写前端 Vue 的 `Dockerfile` (基于 nginx，包含 build 阶段)。
> 3. 更新第一步的 `docker-compose.yml`，加入 Backend 和 Frontend 服务，并编排启动顺序（MySQL/Redis/Kafka -> Backend -> Frontend）。
> 4. 提供 Nginx 的配置文件 `nginx.conf`，实现反向代理：前端请求 `/api` 转发到后端容器。

---

### 关键验证步骤 (Self-Check List)

在Claude生成代码后，你需要重点人工检查以下几点，以确保拿到对应分数：

1.  **Redis Token:** 检查登录代码里，是不是真的把Token存Redis了，而不是只生成了一个JWT就完了。（这是10分和7分的区别）。
2.  **Kafka日志:** 可以在Kafka-UI里看到消息，且删除了学生后，数据库 `sys_log` 表里真的多了一条记录。
3.  **AOP统计:** 狂点几个接口，然后看Redis里（用Redis Desktop Manager）有没有对应的Key计数增加。
4.  **Thymeleaf:** 确保你的项目里真的有 `src/main/resources/templates/index.html`，并且通过浏览器能访问到它。
5.  **Vue:** 确保前端是独立运行的（npm run dev），并且有路由跳转。

按照这个流程走，Claude生成的代码质量会非常高，且逻辑清晰，完全符合你的高分作业标准。