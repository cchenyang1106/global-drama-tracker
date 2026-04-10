import express from 'express'
import { createProxyMiddleware } from 'http-proxy-middleware'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const app = express()
const PORT = process.env.PORT || 3000
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080'

// API 反向代理 - 转发 /api/** 到后端，保留完整路径
app.use(createProxyMiddleware({
  pathFilter: '/api',
  target: BACKEND_URL,
  changeOrigin: true,
}))

// 静态文件
app.use(express.static(join(__dirname, 'dist')))

// SPA 路由回退
app.get('*', (req, res) => {
  res.sendFile(join(__dirname, 'dist', 'index.html'))
})

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Frontend server running on port ${PORT}`)
  console.log(`API proxy -> ${BACKEND_URL}`)
})
