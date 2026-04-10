import request from './request'

function authHeader() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function getMyProfile() {
  return request.get('/profile/me', { headers: authHeader() })
}

export function saveProfile(data) {
  return request.post('/profile/save', data, { headers: authHeader() })
}

export function getUserProfile(userId) {
  return request.get(`/profile/user/${userId}`)
}
