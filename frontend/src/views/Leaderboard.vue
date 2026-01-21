<template>
  <div class="leaderboard-container">
    <el-card class="page-header">
      <div class="header-content">
        <div>
          <h2>学生排行榜</h2>
          <p>基于Redis ZSet实现的实时排行榜（前10名）</p>
        </div>
        <el-button type="primary" :icon="Refresh" @click="fetchData">
          刷新数据
        </el-button>
      </div>
    </el-card>

    <el-card class="content-card" v-loading="loading">
      <div class="leaderboard-list">
        <div
          v-for="(item, index) in leaderboardData"
          :key="item.studentId"
          class="leaderboard-item"
          :class="getRankClass(index)"
        >
          <div class="rank-badge">
            <div class="rank-number" v-if="index >= 3">{{ index + 1 }}</div>
            <el-icon v-else class="medal-icon">
              <Trophy v-if="index === 0" />
              <Medal v-else-if="index === 1" />
              <Medal v-else />
            </el-icon>
          </div>

          <div class="student-info">
            <el-avatar :size="50" :src="getAvatarUrl(item.studentId)" />
            <div class="info-text">
              <div class="student-name">学生 #{{ item.studentId }}</div>
              <div class="student-id">ID: {{ item.studentId }}</div>
            </div>
          </div>

          <div class="score-info">
            <div class="score-label">分数</div>
            <div class="score-value">{{ item.score.toFixed(1) }}</div>
          </div>
        </div>

        <el-empty v-if="!loading && leaderboardData.length === 0" description="暂无排行数据" />
      </div>

      <!-- Add Score Form -->
      <el-divider />
      <div class="add-score-section">
        <h3>添加/更新分数</h3>
        <el-form :inline="true" :model="scoreForm" class="score-form">
          <el-form-item label="学生ID">
            <el-input-number
              v-model="scoreForm.studentId"
              :min="1"
              placeholder="学生ID"
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="分数">
            <el-input-number
              v-model="scoreForm.score"
              :min="0"
              :max="100"
              :precision="1"
              placeholder="分数"
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleAddScore"
              :loading="addLoading"
              :icon="Plus"
            >
              提交分数
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Trophy, Medal, Plus } from '@element-plus/icons-vue'
import { leaderboardAPI } from '@/api'

const loading = ref(false)
const addLoading = ref(false)
const leaderboardData = ref([])

const scoreForm = reactive({
  studentId: null,
  score: null
})

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const data = await leaderboardAPI.getTop10()
    leaderboardData.value = data || []
  } catch (error) {
    ElMessage.error('获取排行榜数据失败')
  } finally {
    loading.value = false
  }
}

const handleAddScore = async () => {
  if (!scoreForm.studentId) {
    ElMessage.warning('请输入学生ID')
    return
  }
  if (scoreForm.score === null || scoreForm.score === undefined) {
    ElMessage.warning('请输入分数')
    return
  }

  addLoading.value = true
  try {
    await leaderboardAPI.addScore(scoreForm.studentId, scoreForm.score)
    ElMessage.success('分数提交成功')
    scoreForm.studentId = null
    scoreForm.score = null
    await fetchData()
  } catch (error) {
    ElMessage.error('分数提交失败')
  } finally {
    addLoading.value = false
  }
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return ''
}

const getAvatarUrl = (studentId) => {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${studentId}`
}
</script>

<style scoped>
.leaderboard-container {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
}

.header-content p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.content-card {
  min-height: calc(100vh - 250px);
}

.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.leaderboard-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #ccc;
}

.leaderboard-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.leaderboard-item.rank-gold {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
}

.leaderboard-item.rank-gold::before {
  background: #FFD700;
}

.leaderboard-item.rank-silver {
  background: linear-gradient(135deg, #E8E8E8 0%, #C0C0C0 100%);
}

.leaderboard-item.rank-silver::before {
  background: #C0C0C0;
}

.leaderboard-item.rank-bronze {
  background: linear-gradient(135deg, #FFDAB9 0%, #CD853F 100%);
}

.leaderboard-item.rank-bronze::before {
  background: #CD853F;
}

.rank-badge {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.rank-number {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.medal-icon {
  font-size: 48px;
  color: #fff;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.student-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.student-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.student-id {
  font-size: 14px;
  color: #666;
}

.score-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.score-label {
  font-size: 12px;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  font-family: 'Arial', sans-serif;
}

.add-score-section {
  margin-top: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.add-score-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #333;
}

.score-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
