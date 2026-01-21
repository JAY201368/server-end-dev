import request from '@/utils/request'

// Auth API
export const authAPI = {
  login(username, password) {
    return request.post('/auth/login', { username, password })
  },

  register(username, password, confirmPassword, nickname, email, phone) {
    return request.post('/auth/register', {
      username,
      password,
      confirmPassword,
      nickname,
      email,
      phone
    })
  }
}

// Student API
export const studentAPI = {
  getList(page = 0, size = 10) {
    return request.get('/students', { params: { page, size } })
  },

  getById(id) {
    return request.get(`/students/${id}`)
  },

  create(student) {
    return request.post('/students', student)
  },

  update(id, student) {
    return request.put(`/students/${id}`, student)
  },

  delete(id) {
    return request.delete(`/students/${id}`)
  },

  uploadAvatar(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`/students/${id}/avatar`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// Leaderboard API
export const leaderboardAPI = {
  getTop10() {
    return request.get('/leaderboard/top10')
  },

  addScore(studentId, score) {
    return request.post('/leaderboard/add', null, {
      params: { studentId, score }
    })
  }
}

// Monitoring API
export const monitoringAPI = {
  getStats() {
    return request.get('/monitoring/stats')
  }
}
