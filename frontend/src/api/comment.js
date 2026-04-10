import request from './request'

export function getComments(dramaId, page = 1, size = 20) {
  return request.get(`/comment/list/${dramaId}`, { params: { page, size } })
}

export function getFeaturedComments(dramaId, limit = 10) {
  return request.get('/comment/featured', { params: { dramaId, limit } })
}

export function addComment(data) {
  return request.post('/comment/add', data)
}

export function likeComment(id) {
  return request.post(`/comment/like/${id}`)
}

export function getRatingStats(dramaId) {
  return request.get(`/comment/rating-stats/${dramaId}`)
}

// 管理端
export function getAllComments(page = 1, size = 20) {
  const token = localStorage.getItem('admin_token')
  return request.get('/comment/admin/list', {
    params: { page, size },
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function setFeatured(id, featured) {
  const token = localStorage.getItem('admin_token')
  return request.post(`/comment/admin/featured/${id}?featured=${featured}`, null, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function deleteCommentAdmin(id) {
  const token = localStorage.getItem('admin_token')
  return request.delete(`/comment/admin/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

// 管理员登录
export function adminLogin(username, password) {
  return request.post('/admin/login', { username, password })
}

export function verifyAdmin() {
  const token = localStorage.getItem('admin_token')
  if (!token) return Promise.reject('no token')
  return request.get('/admin/verify', {
    headers: { Authorization: `Bearer ${token}` },
  })
}
