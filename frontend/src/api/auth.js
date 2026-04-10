import request from './request'

export function register(phone, password, nickname) {
  return request.post('/auth/register', { phone, password, nickname })
}

export function login(phone, password) {
  return request.post('/auth/login', { phone, password })
}

export function getMe() {
  const token = localStorage.getItem('token')
  if (!token) return Promise.reject('no token')
  return request.get('/auth/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
}

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
