const BASE_URL = 'https://global-drama-tracker-production.up.railway.app/api'

export function request({ url, method = 'GET', data = {}, needAuth = false }) {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' }
    if (needAuth) {
      const token = uni.getStorageSync('token')
      if (token) header['Authorization'] = `Bearer ${token}`
    }
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      success(res) {
        if (res.data && res.data.code === 200) {
          resolve(res.data.data)
        } else {
          const msg = res.data?.message || '请求失败'
          // 限流/频繁请求错误静默处理
          const isSilent = res.statusCode === 429 || msg.includes('频繁') || msg.includes('rate')
          if (!isSilent) {
            uni.showToast({ title: msg, icon: 'none' })
          }
          reject(new Error(msg))
        }
      },
      fail(err) {
        // 网络错误也静默处理，避免切后台时弹提示
        reject(err)
      },
    })
  })
}
