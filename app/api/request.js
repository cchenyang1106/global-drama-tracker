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
          uni.showToast({ title: msg, icon: 'none' })
          reject(new Error(msg))
        }
      },
      fail(err) {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      },
    })
  })
}
