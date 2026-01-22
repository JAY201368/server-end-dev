import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// Create axios instance
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - add token automatically
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('请求拦截器 - Token:', token)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('请求拦截器 - Authorization header:', config.headers.Authorization)
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor - handle 401 status
request.interceptors.response.use(
  response => {
    console.log('响应拦截器 - 原始响应:', response)
    console.log('响应拦截器 - response.data:', response.data)
    return response.data
  },
  error => {
    console.error('响应拦截器 - 错误:', error)
    console.error('响应拦截器 - 错误响应:', error.response)
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('token')
          router.push('/login')
          break
        case 403:
          ElMessage.error('拒绝访问')
          console.error('403错误详情:', error.response.data)
          break
        case 404:
          ElMessage.error('请求资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
