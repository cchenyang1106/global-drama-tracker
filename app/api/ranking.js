import { get } from './request'

export const getDailyRanking = (params) => get('/ranking/daily', params)
export const getWeeklyRanking = (params) => get('/ranking/weekly', params)
export const getMonthlyRanking = (params) => get('/ranking/monthly', params)
export const getNewRanking = (params) => get('/ranking/new', params)
