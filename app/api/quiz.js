import { request } from './request'

export function getQuizList(activityId) {
  return request({ url: `/quiz/list/${activityId}` })
}
export function saveQuiz(data) {
  return request({ url: '/quiz/save', method: 'POST', data, needAuth: true })
}
export function deleteQuiz(id) {
  return request({ url: `/quiz/${id}`, method: 'DELETE', needAuth: true })
}
export function submitAnswer(data) {
  return request({ url: '/quiz/answer', method: 'POST', data, needAuth: true })
}
export function getPapers(activityId) {
  return request({ url: `/quiz/papers/${activityId}`, needAuth: true })
}
export function gradePaper(activityId, userId, data) {
  return request({ url: `/quiz/grade/${activityId}/${userId}`, method: 'POST', data, needAuth: true })
}
