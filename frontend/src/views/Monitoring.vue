<template>
  <div class="monitoring-container">
    <el-card class="page-header">
      <div class="header-content">
        <div>
          <h2>接口监控</h2>
          <p>基于AOP统计的接口性能数据可视化</p>
        </div>
        <el-button type="primary" :icon="Refresh" @click="fetchData">
          刷新数据
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- Statistics Cards -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Connection /></el-icon>
            <div class="stat-info">
              <div class="stat-label">总请求数</div>
              <div class="stat-value">{{ totalRequests }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><Timer /></el-icon>
            <div class="stat-info">
              <div class="stat-label">平均响应时间</div>
              <div class="stat-value">{{ avgResponseTime }}ms</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-label">慢请求数</div>
              <div class="stat-value">{{ slowRequests }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C"><CircleClose /></el-icon>
            <div class="stat-info">
              <div class="stat-label">错误请求数</div>
              <div class="stat-value">{{ errorRequests }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- Chart: Response Time by Endpoint -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>各接口平均响应时间</span>
            </div>
          </template>
          <v-chart class="chart" :option="responseTimeOption" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- Chart: Request Count by Endpoint -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>各接口请求次数</span>
            </div>
          </template>
          <v-chart class="chart" :option="requestCountOption" v-loading="loading" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- Data Table -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>接口性能详情</span>
            </div>
          </template>
          <el-table
            :data="tableData"
            v-loading="loading"
            style="width: 100%"
            :header-cell-style="{ background: '#fafafa', color: '#333' }"
          >
            <el-table-column prop="endpoint" label="接口名称" min-width="180" />
            <!-- <el-table-column prop="method" label="请求方法" width="100" /> -->
            <el-table-column prop="count" label="调用次数" width="120" sortable />
            <el-table-column prop="avgTime" label="平均响应时间(ms)" width="150" sortable>
              <template #default="{ row }">
                <el-tag :type="getTimeTagType(row.avgTime)">
                  {{ row.avgTime }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="minTime" label="最小时间(ms)" width="130" sortable />
            <el-table-column prop="maxTime" label="最大时间(ms)" width="130" sortable />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.avgTime)">
                  {{ getStatus(row.avgTime) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Connection, Timer, Warning, CircleClose } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { monitoringAPI } from '@/api'

use([
  CanvasRenderer,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const loading = ref(false)
const statsData = ref([])
const tableData = ref([])

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const response = await monitoringAPI.getStats()
    // 后端返回格式: { code: 200, message: "查询成功", data: [...] }
    const data = response.data || []
    statsData.value = data

    // Transform data for table - 后端返回的字段: apiName, avgDuration, minDuration, maxDuration
    // apiName是中文描述,如"查询学生列表"而非路径
    tableData.value = data.map(item => ({
      endpoint: item.apiName || 'N/A',  // 使用中文描述作为接口名称
      method: '-',  // 后端未提供HTTP方法信息
      count: item.count || 0,
      avgTime: item.avgDuration || 0,
      minTime: item.minDuration || 0,
      maxTime: item.maxDuration || 0
    }))
  } catch (error) {
    console.error('获取监控数据失败:', error)
    ElMessage.error('获取监控数据失败: ' + (error.message || '请检查后端服务'))
  } finally {
    loading.value = false
  }
}

const totalRequests = computed(() => {
  return tableData.value.reduce((sum, item) => sum + item.count, 0)
})

const avgResponseTime = computed(() => {
  if (tableData.value.length === 0) return 0
  const total = tableData.value.reduce((sum, item) => sum + item.avgTime, 0)
  return (total / tableData.value.length).toFixed(2)
})

const slowRequests = computed(() => {
  return tableData.value.filter(item => item.avgTime > 100).length
})

const errorRequests = computed(() => {
  return tableData.value.filter(item => item.avgTime > 1000).length
})

const responseTimeOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: tableData.value.map(item => item.endpoint.substring(0, 10)),  // 截取接口名称前10个字符
    axisLabel: {
      interval: 0,
      rotate: 45,
      fontSize: 10
    }
  },
  yAxis: {
    type: 'value',
    name: '响应时间(ms)'
  },
  series: [
    {
      name: '平均响应时间',
      type: 'bar',
      data: tableData.value.map(item => item.avgTime),
      itemStyle: {
        color: (params) => {
          const value = params.value
          if (value < 50) return '#67C23A'
          if (value < 100) return '#409EFF'
          if (value < 200) return '#E6A23C'
          return '#F56C6C'
        }
      }
    }
  ]
}))

const requestCountOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    bottom: '5%',
    left: 'center'
  },
  series: [
    {
      name: '请求次数',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: tableData.value.map(item => ({
        value: item.count,
        name: item.endpoint  // 使用完整的接口名称
      }))
    }
  ]
}))

const getTimeTagType = (time) => {
  if (time < 50) return 'success'
  if (time < 100) return ''
  if (time < 200) return 'warning'
  return 'danger'
}

const getStatusTagType = (time) => {
  if (time < 100) return 'success'
  if (time < 200) return 'warning'
  return 'danger'
}

const getStatus = (time) => {
  if (time < 100) return '正常'
  return '偏慢'
}
</script>

<style scoped>
.monitoring-container {
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

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
}

.stat-icon {
  font-size: 48px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.chart {
  height: 400px;
  width: 100%;
}
</style>
