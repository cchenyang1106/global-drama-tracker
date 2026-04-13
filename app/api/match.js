import { request } from './request'

export function applyActivity(data) {
  return request({ url: '/match/apply', method: 'POST', data, needAuth: true })
}

export function getReceivedRequests() {
  return request({ url: '/match/received', method: 'GET', needAuth: true })
}

export function getSentRequests() {
  return request({ url: '/match/sent', method: 'GET', needAuth: true })
}

export function handleRequest(id, action) {
  return request({ url: `/match/handle/${id}?action=${action}`, method: 'POST', needAuth: true })
}

export function getChatList() {
  return request({ url: '/match/chats', method: 'GET', needAuth: true })
}

export function sendMessage(data) {
  return request({ url: '/match/chat/send', method: 'POST', data, needAuth: true })
}

export function getChatMessages(matchId, page = 1, size = 50) {
  return request({ url: `/match/chat/messages/${matchId}`, method: 'GET', data: { page, size }, needAuth: true })
}
