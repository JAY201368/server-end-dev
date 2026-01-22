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
  // Backend expects page starting from 1, and supports name, studentNo, status filters
  getList(page = 1, size = 10, filters = {}) {
    const params = { page, size }
    if (filters.name) params.name = filters.name
    if (filters.studentNo) params.studentNo = filters.studentNo
    if (filters.status !== undefined && filters.status !== null) params.status = filters.status
    return request.get('/student/list', { params })
  },

  getById(id) {
    return request.get(`/student/${id}`)
  },

  getByStudentNo(studentNo) {
    return request.get(`/student/no/${studentNo}`)
  },

  // Backend expects StudentDTO with fields: studentNo, name, gender, birthDate, phone, email, avatar, classId, enrollmentDate, status
  create(student) {
    return request.post('/student/add', student)
  },

  // Backend expects StudentDTO with id field included
  update(student) {
    return request.put('/student/update', student)
  },

  delete(id) {
    return request.delete(`/student/${id}`)
  },

  // Batch delete - backend expects array of IDs
  batchDelete(ids) {
    return request.delete('/student/batch', { data: ids })
  }
}

// Score API (renamed from leaderboardAPI to match backend)
export const scoreAPI = {
  // Save/create a new score record
  // Backend expects: studentId, courseId, score, semester, examDate (optional), remark (optional)
  save(scoreData) {
    return request.post('/score/save', scoreData)
  },

  // Update an existing score record
  update(scoreData) {
    return request.put('/score/update', scoreData)
  },

  // Delete a score record
  delete(id) {
    return request.delete(`/score/${id}`)
  },

  // Get score by ID
  getById(id) {
    return request.get(`/score/${id}`)
  },

  // Get score ranking for a semester
  // Backend returns: List<ScoreRankVO> with fields: rank, studentId, studentNo, studentName, score, semester, avatar
  getRanking(semester, topN = 10) {
    return request.get('/score/ranking', {
      params: { semester, topN }
    })
  },

  // Get student rank in a semester
  getStudentRank(studentId, semester) {
    return request.get('/score/rank', {
      params: { studentId, semester }
    })
  },

  // Sync scores from MySQL to Redis
  syncToRedis(semester) {
    return request.post('/score/sync', null, {
      params: { semester }
    })
  }
}

// Keep leaderboardAPI as an alias for backward compatibility
export const leaderboardAPI = scoreAPI

// Monitoring API
export const monitoringAPI = {
  getStats() {
    return request.get('/monitor/stats')
  }
}
