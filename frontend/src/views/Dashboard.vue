<template>
  <div class="dashboard-container">
    <el-card class="welcome-card">
      <h1>欢迎使用学生管理系统</h1>
      <p>基于 Spring Boot + Vue 3 的全栈应用</p>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card class="feature-card" shadow="hover" @click="navigate('/students')">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#409EFF"><User /></el-icon>
            <div class="feature-info">
              <h3>学生管理</h3>
              <p>管理学生信息</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="feature-card" shadow="hover" @click="navigate('/leaderboard')">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#67C23A"><TrophyBase /></el-icon>
            <div class="feature-info">
              <h3>排行榜</h3>
              <p>查看学生排名</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="feature-card" shadow="hover" @click="navigate('/monitoring')">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#E6A23C"><Monitor /></el-icon>
            <div class="feature-info">
              <h3>接口监控</h3>
              <p>查看性能数据</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="feature-card" shadow="hover">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#F56C6C"><DataAnalysis /></el-icon>
            <div class="feature-info">
              <h3>数据统计</h3>
              <p>敬请期待</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>技术栈</span>
            </div>
          </template>
          <div class="tech-stack">
            <el-tag size="large" effect="plain" type="success">Spring Boot</el-tag>
            <el-tag size="large" effect="plain" type="primary">Vue 3</el-tag>
            <el-tag size="large" effect="plain" type="warning">MySQL</el-tag>
            <el-tag size="large" effect="plain" type="danger">Redis</el-tag>
            <el-tag size="large" effect="plain" type="info">Kafka</el-tag>
            <el-tag size="large" effect="plain">Element Plus</el-tag>
            <el-tag size="large" effect="plain">Spring Security</el-tag>
            <el-tag size="large" effect="plain">AOP</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>功能特性</span>
            </div>
          </template>
          <ul class="feature-list">
            <li><el-icon color="#67C23A"><Check /></el-icon> JWT 身份认证</li>
            <li><el-icon color="#67C23A"><Check /></el-icon> CRUD 完整功能</li>
            <li><el-icon color="#67C23A"><Check /></el-icon> Redis 排行榜</li>
            <li><el-icon color="#67C23A"><Check /></el-icon> Kafka 消息队列</li>
            <li><el-icon color="#67C23A"><Check /></el-icon> AOP 性能监控</li>
            <li><el-icon color="#67C23A"><Check /></el-icon> 响应式前端设计</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { User, TrophyBase, Monitor, DataAnalysis, Check } from '@element-plus/icons-vue'

const router = useRouter()

const navigate = (path) => {
  router.push(path)
}
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.welcome-card {
  text-align: center;
  padding: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-card h1 {
  margin: 0 0 16px 0;
  font-size: 36px;
  font-weight: 700;
}

.welcome-card p {
  margin: 0;
  font-size: 18px;
  opacity: 0.9;
}

.feature-card {
  cursor: pointer;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  text-align: center;
}

.feature-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.feature-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #333;
}

.feature-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.tech-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 0;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  color: #333;
}

.feature-list li:last-child {
  border-bottom: none;
}
</style>
