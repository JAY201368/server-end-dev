<template>
  <div class="layout-container">
    <el-container>
      <!-- Sidebar -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
        <div class="logo">
          <img v-if="!isCollapse" src="@/assets/logo.svg" alt="Logo" />
          <span v-if="!isCollapse" class="logo-text">管理系统</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="isCollapse"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-menu-item index="/students">
            <el-icon><User /></el-icon>
            <template #title>学生管理</template>
          </el-menu-item>

          <el-menu-item index="/leaderboard">
            <el-icon><TrophyBase /></el-icon>
            <template #title>排行榜</template>
          </el-menu-item>

          <el-menu-item index="/monitoring">
            <el-icon><Monitor /></el-icon>
            <template #title>接口监控</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- Main Content Area -->
      <el-container>
        <!-- Top Header -->
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-icon" @click="toggleCollapse">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
                {{ item.name }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                <span class="username">{{ username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- Content Area -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  User,
  TrophyBase,
  Monitor,
  Fold,
  Expand,
  ArrowDown,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)
const username = ref(localStorage.getItem('username') || 'Admin')

const activeMenu = computed(() => route.path)

const menuMap = {
  '/dashboard': { name: '首页' },
  '/students': { name: '学生管理' },
  '/leaderboard': { name: '排行榜' },
  '/monitoring': { name: '接口监控' }
}

const breadcrumbs = computed(() => {
  const path = route.path
  const items = [{ name: '首页', path: '/dashboard' }]

  if (path !== '/dashboard' && menuMap[path]) {
    items.push({ name: menuMap[path].name, path })
  }

  return items
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中')
      break
    case 'settings':
      ElMessage.info('设置功能开发中')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch {
        // User cancelled
      }
      break
  }
}
</script>

<style scoped>
.layout-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.el-container {
  height: 100%;
}

/* Sidebar Styles */
.sidebar {
  background-color: #001529;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  background-color: #002140;
}

.logo img {
  width: 32px;
  height: 32px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  margin-left: 12px;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 64px);
}

.sidebar-menu :deep(.el-menu-item) {
  height: 56px;
  line-height: 56px;
  transition: all 0.3s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(24, 144, 255, 0.1) !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(24, 144, 255, 0.2) !important;
}

/* Header Styles */
.header {
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
}

.collapse-icon:hover {
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* Main Content Styles */
.main-content {
  background-color: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}

/* Page Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
