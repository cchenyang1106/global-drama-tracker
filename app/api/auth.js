import { request } from './request'

export function register(phone, password, nickname) {
  return request({ url: '/auth/register', method: 'POST', data: { phone, password, nickname } })
}

export function login(phone, password) {
  return request({ url: '/auth/login', method: 'POST', data: { phone, password } })
}

export function getMe() {
  return request({ url: '/auth/me', method: 'GET', needAuth: true })
}
