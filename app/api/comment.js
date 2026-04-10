import { get, post } from './request'

export const getComments = (dramaId, page, size) => get(`/comment/list/${dramaId}`, { page, size })
export const getFeaturedComments = (dramaId, limit) => get('/comment/featured', { dramaId, limit })
export const addComment = (data) => post('/comment/add', data)
export const likeComment = (id) => post(`/comment/like/${id}`)
