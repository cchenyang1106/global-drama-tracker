import request from './request'

const authHeader = () => ({ headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } })

export function submitReport(data) {
  return request.post('/report/submit', data, authHeader())
}
