import { request } from './request'

export function getMyProfile() {
  return request({ url: '/profile/me', method: 'GET', needAuth: true })
}

export function saveProfile(data) {
  return request({ url: '/profile/save', method: 'POST', data, needAuth: true })
}

export function getUserProfile(userId) {
  return request({ url: `/profile/user/${userId}`, method: 'GET' })
}
