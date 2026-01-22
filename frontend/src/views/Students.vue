<template>
  <div class="student-container">
    <el-card class="page-header">
      <div class="header-content">
        <div>
          <h2>学生管理</h2>
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
            <el-avatar :size="50" :src="row.avatar || defaultAvatar" />
          </template>
        </el-table-column>

        <el-table-column prop="name" label="姓名" width="120" />

        <el-table-column prop="studentNo" label="学号" width="150" />

        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 0 ? '女' : '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="联系电话" width="120" />

        <el-table-column prop="email" label="邮箱" min-width="150" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">在读</el-tag>
            <el-tag v-else-if="row.status === 0" type="warning">休学</el-tag>
            <el-tag v-else-if="row.status === 2" type="info">毕业</el-tag>
            <el-tag v-else-if="row.status === 3" type="danger">退学</el-tag>
            <el-tag v-else type="info">-</el-tag>
          </template>
        </el-table-column>

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
            <el-avatar :size="100" :src="form.avatar || defaultAvatar" />
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

        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="form.birthDate"
            type="date"
            placeholder="请选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="班级ID" prop="classId">
          <el-input-number v-model="form.classId" placeholder="请输入班级ID" style="width: 100%" />
        </el-form-item>

        <el-form-item label="入学日期" prop="enrollmentDate">
          <el-date-picker
            v-model="form.enrollmentDate"
            type="date"
            placeholder="请选择入学日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="休学" :value="0" />
            <el-option label="在读" :value="1" />
            <el-option label="毕业" :value="2" />
            <el-option label="退学" :value="3" />
          </el-select>
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
    <el-dialog v-model="viewDialogVisible" title="学生详情" width="600px">
      <div class="student-detail">
        <div class="detail-avatar">
          <el-avatar :size="120" :src="viewData.avatar || defaultAvatar" />
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ viewData.id }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ viewData.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ viewData.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ viewData.gender === 1 ? '男' : viewData.gender === 0 ? '女' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ viewData.birthDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ viewData.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱" :span="2">{{ viewData.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="班级ID">{{ viewData.classId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入学日期">{{ viewData.enrollmentDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="viewData.status === 1" type="success">在读</el-tag>
            <el-tag v-else-if="viewData.status === 0" type="warning">休学</el-tag>
            <el-tag v-else-if="viewData.status === 2" type="info">毕业</el-tag>
            <el-tag v-else-if="viewData.status === 3" type="danger">退学</el-tag>
            <el-tag v-else type="info">-</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ viewData.createTime || '-' }}</el-descriptions-item>
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
  studentNo: '',
  name: '',
  gender: 1,
  birthDate: '',
  phone: '',
  email: '',
  avatar: '',
  classId: null,
  enrollmentDate: '',
  status: 1
})

const viewData = reactive({
  id: null,
  studentNo: '',
  name: '',
  gender: null,
  birthDate: '',
  phone: '',
  email: '',
  avatar: '',
  classId: null,
  enrollmentDate: '',
  status: null,
  createTime: '',
  updateTime: ''
})

const rules = {
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }]
}

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    // Backend response format: { code, message, data: { records, total, size, current, pages }, timestamp }
    // studentAPI.getList returns response.data from the interceptor (the full Result object)
    const response = await studentAPI.getList(currentPage.value, pageSize.value)
    // response is the full Result object, so we need to access response.data to get the IPage object
    const pageData = response.data || {}
    tableData.value = pageData.records || []
    total.value = pageData.total || 0
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

  // Convert to base64 for preview (backend uses 'avatar' field)
  const reader = new FileReader()
  reader.onload = (e) => {
    form.avatar = e.target.result
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
          // Backend expects the entire student object with id included
          await studentAPI.update(form)
          ElMessage.success('更新成功')
        } else {
          await studentAPI.create(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        // Error message is already shown by the interceptor
        console.error('提交失败:', error)
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
  form.studentNo = ''
  form.name = ''
  form.gender = 1
  form.birthDate = ''
  form.phone = ''
  form.email = ''
  form.avatar = ''
  form.classId = null
  form.enrollmentDate = ''
  form.status = 1
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
