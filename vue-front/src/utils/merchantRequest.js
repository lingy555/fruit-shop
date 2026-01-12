// 商家专用请求工具，避免token刷新干扰
import axios from 'axios'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'

const merchantRequest = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 请求拦截器
merchantRequest.interceptors.request.use(
  config => {
    const merchantToken = Cookies.get('merchantToken')
    if (merchantToken) {
      config.headers['Authorization'] = 'Bearer ' + merchantToken
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 简化版，不处理401
merchantRequest.interceptors.response.use(
  response => {
    // 后端返回的是ApiResponse.success()包装的数据，需要返回data字段
    return response.data.data || response.data
  },
  error => {
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      if (status === 401) {
        console.warn('商家端401错误，忽略处理')
        return Promise.reject(error)
      }
      
      ElMessage({
        message: data?.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })
    }
    
    return Promise.reject(error)
  }
)

export default merchantRequest
