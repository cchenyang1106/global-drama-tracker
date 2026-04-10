import request from './request'

function authHeader() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function getComments(activityId, page = 1, size = 20) {
  return request.get(`/activity-comment/list/${activityId}`, { params: { page, size } })
}

export function addComment(data) {
  return request.post('/activity-comment/add', data, { headers: authHeader() })
}

export function pinComment(id, pin) {
  return request.post(`/activity-comment/pin/${id}?pin=${pin}`, null, { headers: authHeader() })
}

export function hideComment(id) {
  return request.post(`/activity-comment/hide/${id}`, null, { headers: authHeader() })
}

export function deleteComment(id) {
  return request.delete(`/activity-comment/${id}`, { headers: authHeader() })
}
