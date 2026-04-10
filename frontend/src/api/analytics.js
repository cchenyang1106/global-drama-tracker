import request from './request'

export function getOverview() {
  return request.get('/analytics/overview')
}

export function getDailyStats(days = 30) {
  return request.get('/analytics/daily', { params: { days } })
}

export function getPageStats(days = 30) {
  return request.get('/analytics/pages', { params: { days } })
}
