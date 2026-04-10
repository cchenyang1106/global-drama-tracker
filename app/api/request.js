const BASE_URL = 'https://global-drama-tracker-production.up.railway.app'

function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('user_token')
    uni.request({
      url: BASE_URL + '/api' + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
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
        console.error('请求失败:', options.url, err)
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      },
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const del = (url) => request({ url, method: 'DELETE' })

export default { get, post, del }
