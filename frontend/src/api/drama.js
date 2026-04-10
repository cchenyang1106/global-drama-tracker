import request from './request'

/** 分页查询剧集列表 */
export function getDramaList(params) {
  return request.get('/drama/list', { params })
}

/** 获取剧集详情 */
export function getDramaDetail(id) {
  return request.get(`/drama/${id}`)
}

/** 获取今日更新 */
export function getTodayUpdated(region) {
  return request.get('/drama/today', { params: { region } })
}

/** 搜索剧集 */
export function searchDramas(keyword, page = 1, pageSize = 20) {
  return request.get('/drama/search', { params: { keyword, page, pageSize } })
}

/** 按地区获取剧集 */
export function getDramasByRegion(region, page = 1, pageSize = 20) {
  return request.get(`/drama/region/${region}`, { params: { page, pageSize } })
}
