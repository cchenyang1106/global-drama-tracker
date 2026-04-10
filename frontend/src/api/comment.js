import request from './request'

/** 获取剧集评论列表 */
export function getComments(dramaId, page = 1, size = 20) {
  return request.get(`/comment/list/${dramaId}`, { params: { page, size } })
}

/** 发表评论 */
export function addComment(data) {
  return request.post('/comment/add', data)
}

/** 点赞评论 */
export function likeComment(id) {
  return request.post(`/comment/like/${id}`)
}

/** 获取评分统计 */
export function getRatingStats(dramaId) {
  return request.get(`/comment/rating-stats/${dramaId}`)
}
