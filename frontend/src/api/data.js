import request from './request'

/**
 * 初始化种子数据（内置精选数据，不需要 API Key）
 */
export const initSeedData = () => {
  return request.post('/data/init/seed')
}

/**
 * 从 TMDB 抓取全量数据
 */
export const fetchFromTmdb = () => {
  return request.post('/data/fetch/tmdb')
}

/**
 * 按地区抓取 TMDB 数据
 */
export const fetchByRegion = (region) => {
  return request.post(`/data/fetch/tmdb/${region}`)
}

/**
 * 刷新排行榜
 */
export const refreshRankings = () => {
  return request.post('/data/refresh/ranking')
}
