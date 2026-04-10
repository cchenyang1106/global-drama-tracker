import request from '@/api/request'

/** 获取或生成访客唯一 ID (UV 标识) */
function getVisitorId() {
  let id = localStorage.getItem('_vid')
  if (!id) {
    id = 'v_' + Math.random().toString(36).slice(2) + Date.now().toString(36)
    localStorage.setItem('_vid', id)
  }
  return id
}

/** 上报页面访问 */
export function reportPageView(path, pageTitle) {
  try {
    const data = {
      path,
      pageTitle,
      referrer: document.referrer || '',
      visitorId: getVisitorId(),
      screenWidth: window.screen.width,
      screenHeight: window.screen.height,
    }
    // 使用 sendBeacon 优先（页面关闭时也能发送），降级用 fetch
    const url = '/api/analytics/report'
    const body = JSON.stringify(data)
    if (navigator.sendBeacon) {
      navigator.sendBeacon(url, new Blob([body], { type: 'application/json' }))
    } else {
      request.post(url, data).catch(() => {})
    }
  } catch {
    // 埋点不影响用户体验
  }
}

/** Vue Router 插件：自动上报每次路由切换 */
export function setupTracker(router) {
  router.afterEach((to) => {
    const title = to.meta.title || document.title
    reportPageView(to.fullPath, title)
  })
}
