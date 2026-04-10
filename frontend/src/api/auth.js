import request from './request'

export function register(phone, password, nickname) {
  return request.post('/auth/register', { phone, password, nickname })
}

export function login(phone, password) {
  return request.post('/auth/login', { phone, password })
}

export function getMe() {
  const token = localStorage.getItem('user_token')
  if (!token) return Promise.reject('no token')
  return request.get('/auth/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
}
