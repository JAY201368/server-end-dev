<template>
  <div class="register-container">
    <div class="register-background">
      <div class="shape shape1"></div>
      <div class="shape shape2"></div>
      <div class="shape shape3"></div>
    </div>

    <div class="register-card">
      <div class="register-header">
        <h1>用户注册</h1>
        <p>Create Your Account</p>
      </div>

      <el-form
        :model="registerForm"
        :rules="rules"
        ref="registerFormRef"
        class="register-form"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="3-20个字符，字母、数字或下划线"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            size="large"
            prefix-icon="Avatar"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="6-20个字符"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="选填，用于找回密码"
            size="large"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="选填，11位手机号"
            size="large"
            prefix-icon="Phone"
            clearable
            maxlength="11"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            class="register-button"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <p>已有账号？<a @click="goToLogin">立即登录</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 验证用户名格式
const validateUsername = (rule, value, callback) => {
  const usernameRegex = /^[a-zA-Z0-9_]+$/
  if (value === '') {
    callback(new Error('请输入用户名'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度必须在3-20个字符之间'))
  } else if (!usernameRegex.test(value)) {
    callback(new Error('用户名只能包含字母、数字和下划线'))
  } else {
    callback()
  }
}

// 验证手机号格式
const validatePhone = (rule, value, callback) => {
  if (value === '' || value === null) {
    callback() // 手机号可选
  } else {
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(value)) {
      callback(new Error('手机号格式不正确'))
    } else {
      callback()
    }
  }
}

const rules = {
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await authAPI.register(
          registerForm.username,
          registerForm.password,
          registerForm.confirmPassword,
          registerForm.nickname,
          registerForm.email,
          registerForm.phone
        )
        ElMessage.success('注册成功！即将跳转到登录页')

        // 2秒后跳转到登录页
        setTimeout(() => {
          router.push('/login')
        }, 2000)
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '注册失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.register-background {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s infinite;
}

.shape1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.shape2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  bottom: -100px;
  right: -100px;
  animation-delay: 5s;
}

.shape3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 30px) scale(0.9);
  }
}

.register-card {
  position: relative;
  width: 500px;
  max-height: 90vh;
  padding: 40px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 10;
  overflow-y: auto;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
  letter-spacing: 1px;
}

.register-form {
  margin-top: 20px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 8px 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.register-button {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
  margin-top: 10px;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.register-button:active {
  transform: translateY(0);
}

.register-footer {
  text-align: center;
  margin-top: 15px;
}

.register-footer p {
  color: #666;
  font-size: 14px;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.3s;
}

.register-footer a:hover {
  color: #764ba2;
}

/* 滚动条样式 */
.register-card::-webkit-scrollbar {
  width: 6px;
}

.register-card::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.register-card::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 10px;
}

.register-card::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
