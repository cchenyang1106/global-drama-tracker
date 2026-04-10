import { get, post } from './request'

export const register = (phone, password, nickname) => post('/auth/register', { phone, password, nickname })
export const login = (phone, password) => post('/auth/login', { phone, password })
export const getMe = () => get('/auth/me')
