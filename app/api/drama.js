import { get } from './request'

export const getDramaList = (params) => get('/drama/list', params)
export const getDramaDetail = (id) => get(`/drama/${id}`)
export const getTodayUpdated = (region) => get('/drama/today', { region })
export const searchDramas = (keyword, page, size) => get('/drama/search', { keyword, page, size })
export const getDramasByRegion = (region, page, size) => get(`/drama/region/${region}`, { page, size })
