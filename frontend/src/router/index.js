import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Dashboard from '@/views/Dashboard.vue'
import Students from '@/views/Students.vue'
import Leaderboard from '@/views/Leaderboard.vue'
import Monitoring from '@/views/Monitoring.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: 'students',
        name: 'Students',
        component: Students
      },
      {
        path: 'leaderboard',
        name: 'Leaderboard',
        component: Leaderboard
      },
      {
        path: 'monitoring',
        name: 'Monitoring',
        component: Monitoring
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard to check authentication
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPages = ['/login', '/register']
  const authRequired = !publicPages.includes(to.path)

  if (authRequired && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
