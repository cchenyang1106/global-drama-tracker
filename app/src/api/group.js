import { request } from './request'

export function getGroupList() {
  return request({ url: '/group/list', needAuth: true })
}
export function getGroupMessages(groupId, page, size) {
  return request({ url: `/group/messages/${groupId}?page=${page}&size=${size}`, needAuth: true })
}
export function sendGroupMessage(data) {
  return request({ url: '/group/send', method: 'POST', data, needAuth: true })
}
export function getGroupMembers(groupId) {
  return request({ url: `/group/members/${groupId}`, needAuth: true })
}
