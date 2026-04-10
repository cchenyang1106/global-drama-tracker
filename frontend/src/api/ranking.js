import request from './request'

/** 获取日榜 */
export function getDailyRanking(params = {}) {
  return request.get('/ranking/daily', { params })
}

/** 获取周榜 */
export function getWeeklyRanking(params = {}) {
  return request.get('/ranking/weekly', { params })
}

/** 获取月榜 */
export function getMonthlyRanking(params = {}) {
  return request.get('/ranking/monthly', { params })
}

/** 获取地区排行榜 */
export function getRegionRanking(region, type = 1, limit = 50) {
  return request.get(`/ranking/region/${region}`, { params: { type, limit } })
}

/** 获取新剧热度榜 */
export function getNewDramaRanking(days = 30, limit = 20) {
  return request.get('/ranking/new', { params: { days, limit } })
}
