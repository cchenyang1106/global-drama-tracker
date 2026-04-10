import request from './request'

function authHeader() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function applyActivity(data) {
  return request.post('/match/apply', data, { headers: authHeader() })
}

export function getReceivedRequests() {
  return request.get('/match/received', { headers: authHeader() })
}

export function getSentRequests() {
  return request.get('/match/sent', { headers: authHeader() })
}

export function handleRequest(id, action) {
  return request.post(`/match/handle/${id}?action=${action}`, null, { headers: authHeader() })
}

export function getChatList() {
  return request.get('/match/chats', { headers: authHeader() })
}

export function sendMessage(data) {
  return request.post('/match/chat/send', data, { headers: authHeader() })
}

export function getChatMessages(matchId, page = 1, size = 50) {
  return request.get(`/match/chat/messages/${matchId}`, {
    params: { page, size },
    headers: authHeader(),
  })
}
