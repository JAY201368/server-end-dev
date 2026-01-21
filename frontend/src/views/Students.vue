<template>
  <div class="student-container">
    <el-card class="page-header">
      <div class="header-content">
        <div>
          <h2>学生管理</h2>
          <p>管理学生信息，支持增删改查操作</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          添加学生
        </el-button>
      </div>
    </el-card>

    <el-card class="content-card">
      <!-- Search Bar -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学生姓名或学号"
          clearable
          style="width: 300px"
          :prefix-icon="Search"
          @input="handleSearch"
        />
      </div>

      <!-- Student Table -->
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', color: '#333' }"
      >
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="头像" width="100">
          <template #default="{ row }">
            <el-avatar :size="50" :src="row.avatarUrl || defaultAvatar" />
          </template>
        </el-table-column>

        <el-table-column prop="name" label="姓名" width="120" />

        <el-table-column prop="studentNumber" label="学号" width="150" />

        <el-table-column prop="major" label="专业" min-width="150" />

        <el-table-column prop="grade" label="年级" width="100" />

        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="warning" link :icon="Edit" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="100" :src="form.avatarUrl || defaultAvatar" />
            <el-upload
              class="upload-button"
              :show-file-list="false"
              :before-upload="handleAvatarUpload"
              accept="image/*"
            >
              <el-button type="primary" :icon="Upload" size="small">
                上传头像
              </el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入学生姓名" />
        </el-form-item>

        <el-form-item label="学号" prop="studentNumber">
          <el-input v-model="form.studentNumber" placeholder="请输入学号" />
        </el-form-item>

        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业" />
        </el-form-item>

        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="请输入年级" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="学生详情" width="500px">
      <div class="student-detail">
        <div class="detail-avatar">
          <el-avatar :size="120" :src="viewData.avatarUrl || defaultAvatar" />
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">{{ viewData.id }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ viewData.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ viewData.studentNumber }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ viewData.major }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ viewData.grade }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, Edit, Delete, Upload } from '@element-plus/icons-vue'
import { studentAPI } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('添加学生')
const isEdit = ref(false)
const formRef = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const form = reactive({
  id: null,
  name: '',
  studentNumber: '',
  major: '',
  grade: '',
  avatarUrl: ''
})

const viewData = reactive({
  id: null,
  name: '',
  studentNumber: '',
  major: '',
  grade: '',
  avatarUrl: ''
})

const rules = {
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  studentNumber: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }]
}

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const response = await studentAPI.getList(currentPage.value - 1, pageSize.value)
    tableData.value = response.content || []
    total.value = response.totalElements || 0
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchData()
}

const handlePageChange = () => {
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '添加学生'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学生'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  Object.assign(viewData, row)
  viewDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除学生 ${row.name} 吗？此操作会触发Kafka消息。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await studentAPI.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleAvatarUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }

  // Convert to base64 for preview
  const reader = new FileReader()
  reader.onload = (e) => {
    form.avatarUrl = e.target.result
  }
  reader.readAsDataURL(file)

  return false // Prevent auto upload
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await studentAPI.update(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await studentAPI.create(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.studentNumber = ''
  form.major = ''
  form.grade = ''
  form.avatarUrl = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style scoped>
.student-container {
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

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.student-detail {
  padding: 20px 0;
}

.detail-avatar {
  text-align: center;
  margin-bottom: 30px;
}
</style>
