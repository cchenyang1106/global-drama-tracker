import { request } from './request'

export function getActivities(params) {
  return request({ url: '/activity/list', method: 'GET', data: params })
}

export function getActivityDetail(id) {
  return request({ url: `/activity/${id}`, method: 'GET' })
}

export function publishActivity(data) {
  return request({ url: '/activity/publish', method: 'POST', data, needAuth: true })
}

export function getMyActivities() {
  return request({ url: '/activity/my', method: 'GET', needAuth: true })
}
