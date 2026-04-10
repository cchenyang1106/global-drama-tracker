import request from './request'

export function getActivities(params) {
  return request.get('/activity/list', { params })
}

export function getActivityDetail(id) {
  return request.get(`/activity/${id}`)
}

export function publishActivity(data) {
  const token = localStorage.getItem('token')
  return request.post('/activity/publish', data, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function getMyActivities() {
  const token = localStorage.getItem('token')
  return request.get('/activity/my', {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function closeActivity(id) {
  const token = localStorage.getItem('token')
  return request.post(`/activity/close/${id}`, null, {
    headers: { Authorization: `Bearer ${token}` },
  })
}
