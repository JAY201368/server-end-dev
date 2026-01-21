# 学生管理系统前端

基于 Vue 3 + Vite + Element Plus 构建的现代化学生管理系统前端。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - Vue 3 UI 组件库
- **Vue Router** - Vue 官方路由管理器
- **Axios** - HTTP 客户端
- **ECharts** - 数据可视化图表库

## 功能特性

### 1. 用户认证
- 精美的登录页面设计
- JWT Token 自动管理
- 401 状态自动跳转登录

### 2. 学生管理
- 学生列表展示（支持分页）
- 添加/编辑/删除学生
- 头像上传和显示
- 搜索功能

### 3. 排行榜
- 基于 Redis ZSet 的实时排行榜
- 显示前 10 名学生
- 支持添加/更新分数

### 4. 接口监控
- 使用 ECharts 可视化
- 显示 AOP 统计的性能数据
- 响应时间分析
- 请求次数统计

## 快速开始

### 安装依赖

\`\`\`bash
npm install
\`\`\`

### 开发模式

\`\`\`bash
npm run dev
\`\`\`

应用将在 [http://localhost:3000](http://localhost:3000) 启动

### 构建生产版本

\`\`\`bash
npm run build
\`\`\`

### 预览生产构建

\`\`\`bash
npm run preview
\`\`\`

## 项目结构

\`\`\`
frontend/
├── src/
│   ├── api/              # API 接口定义
│   │   └── index.js
│   ├── assets/           # 静态资源
│   │   └── logo.svg
│   ├── components/       # 公共组件
│   │   └── Layout.vue    # 布局组件
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios 封装
│   ├── views/            # 页面组件
│   │   ├── Dashboard.vue
│   │   ├── Login.vue
│   │   ├── Students.vue
│   │   ├── Leaderboard.vue
│   │   └── Monitoring.vue
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── vite.config.js        # Vite 配置
└── package.json
\`\`\`

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 学生接口
- `GET /api/students` - 获取学生列表
- `GET /api/students/{id}` - 获取单个学生
- `POST /api/students` - 创建学生
- `PUT /api/students/{id}` - 更新学生
- `DELETE /api/students/{id}` - 删除学生

### 排行榜接口
- `GET /api/leaderboard/top10` - 获取前10名
- `POST /api/leaderboard/add` - 添加分数

### 监控接口
- `GET /api/monitoring/stats` - 获取统计数据

## Axios 拦截器

### 请求拦截器
- 自动添加 JWT Token 到请求头
- 格式：`Authorization: Bearer {token}`

### 响应拦截器
- 处理 401 状态码，自动跳转登录页
- 统一错误提示
- 自动提取响应数据

## 路由守卫

- 未登录用户自动重定向到登录页
- 已登录用户访问登录页自动重定向到首页

## 代理配置

开发环境下，所有 `/api` 请求会被代理到 `http://localhost:8080`

## 页面截图

### 登录页
- 渐变背景动画
- 响应式设计
- 表单验证

### 学生管理页
- 数据表格展示
- 分页功能
- CRUD 操作
- 头像展示

### 排行榜页
- 前三名特殊展示（金银铜牌）
- 实时分数更新
- 添加分数功能

### 接口监控页
- 统计卡片
- ECharts 图表
- 详细数据表格

## 注意事项

1. 确保后端服务在 `http://localhost:8080` 运行
2. Token 存储在 localStorage 中
3. 支持现代浏览器（Chrome, Firefox, Safari, Edge）

## License

MIT
