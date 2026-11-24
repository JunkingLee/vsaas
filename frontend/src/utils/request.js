import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  const tenantId = localStorage.getItem('tenantId')
  const userId = localStorage.getItem('userId')
  if (token) config.headers['Authorization'] = 'Bearer ' + token
  if (tenantId) config.headers['X-Tenant-Id'] = tenantId
  if (userId) config.headers['X-User-Id'] = userId
  return config
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code && data.code !== 200) {
      ElMessage.error(data.msg || '请求失败')
      if (data.code === 401) {
        localStorage.clear()
        router.push('/login')
      }
      return Promise.reject(new Error(data.msg || 'Error'))
    }
    return data
  },
  err => {
    ElMessage.error(err.message || '网络错误')
    return Promise.reject(err)
  }
)

export default request
