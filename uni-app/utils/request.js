import { API_BASE } from './config.js'

function getBase() {
  const override = uni.getStorageSync('apiBase')
  return (override || API_BASE || '').replace(/\/$/, '')
}

export function request(options) {
  const token = uni.getStorageSync('token')
  const tenantId = uni.getStorageSync('tenantId')
  const userId = uni.getStorageSync('userId')
  const header = Object.assign({ 'Content-Type': 'application/json' }, options.header || {})
  if (token) header['Authorization'] = 'Bearer ' + token
  if (tenantId) header['X-Tenant-Id'] = String(tenantId)
  if (userId) header['X-User-Id'] = String(userId)

  const url = (options.url || '').startsWith('http') ? options.url : getBase() + options.url

  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: options.method || 'GET',
      data: options.data,
      header,
      timeout: options.timeout || 20000,
      success(res) {
        const data = res.data
        if (res.statusCode === 401 || (data && data.code === 401)) {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error((data && data.msg) || '未登录'))
          return
        }
        if (data && typeof data.code !== 'undefined' && data.code !== 200) {
          uni.showToast({ title: data.msg || '请求失败', icon: 'none' })
          reject(new Error(data.msg || 'Error'))
          return
        }
        resolve(data)
      },
      fail(err) {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const http = {
  get: (url, data) => request({ url, method: 'GET', data }),
  post: (url, data) => request({ url, method: 'POST', data }),
  put: (url, data) => request({ url, method: 'PUT', data }),
  delete: (url, data) => request({ url, method: 'DELETE', data })
}
