
import axios from 'axios'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'
import { auth } from '@/api'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL || '/api', // API基础URL
  timeout: 10000 // 请求超时时间
})

// 是否正在刷新token
let isRefreshing = false
// 存储待重试的请求
let failedQueue = []

// 处理待重试的请求
const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

const safeJson = (val) => {
  try {
    return JSON.stringify(val)
  } catch (e) {
    return String(val)
  }
}

const logRequest = (config) => {
  const method = (config.method || 'get').toUpperCase()
  const url = `${config.baseURL || ''}${config.url || ''}`
  const params = config.params
  const data = config.data
  console.groupCollapsed(`[API Request] ${method} ${url}`)
  console.log('params:', params)
  console.log('data:', data)
  console.log('headers:', config.headers)
  console.groupEnd()
}

const logResponse = (response) => {
  const cfg = response.config || {}
  const method = (cfg.method || 'get').toUpperCase()
  const url = `${cfg.baseURL || ''}${cfg.url || ''}`
  console.groupCollapsed(`[API Response] ${method} ${url} (HTTP ${response.status})`)
  console.log('data:', response.data)
  console.groupEnd()
}

const logError = (error) => {
  const cfg = error.config || {}
  const method = (cfg.method || 'get').toUpperCase()
  const url = `${cfg.baseURL || ''}${cfg.url || ''}`
  const status = error.response?.status
  const respData = error.response?.data
  console.groupCollapsed(`[API Error] ${method} ${url}${status ? ` (HTTP ${status})` : ''}`)
  console.log('message:', error.message)
  console.log('params:', cfg.params)
  console.log('data:', cfg.data)
  console.log('response:', respData)
  console.groupEnd()
}

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = Cookies.get('token')
    const adminToken = Cookies.get('adminToken')
    const merchantToken = Cookies.get('merchantToken')

    const url = config.url || ''
    const useAdmin = url.startsWith('/admin/') || url.startsWith('admin/')
    const useMerchant = url.startsWith('/merchant/') || url.startsWith('merchant/')
    config.__useAdmin = useAdmin
    config.__useMerchant = useMerchant
    const finalToken = useAdmin ? adminToken : (useMerchant ? merchantToken : token)
    if (finalToken) {
      config.headers['Authorization'] = 'Bearer ' + finalToken
    }

    if (import.meta?.env?.DEV) {
      logRequest(config)
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    logError(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    if (import.meta?.env?.DEV) {
      logResponse(response)
    }

    // 如果状态码不是200，则判断为错误
    if (res.code !== 200) {
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000
      })

      // 401: 未登录或token过期
      if (res.code === 401) {
        // 尝试刷新token
        const originalRequest = response.config
        if (!originalRequest._retry && !originalRequest.url?.includes('/auth/refresh') && !originalRequest.url?.includes('/merchant/')) {
          originalRequest._retry = true
          
          if (isRefreshing) {
            // 如果正在刷新，将请求加入队列
            return new Promise((resolve, reject) => {
              failedQueue.push({ resolve, reject })
            }).then(token => {
              originalRequest.headers['Authorization'] = 'Bearer ' + token
              return service(originalRequest)
            }).catch(err => {
              return Promise.reject(err)
            })
          }

          isRefreshing = true
          
          return new Promise((resolve, reject) => {
            auth.refreshToken().then(response => {
              const { token } = response.data
              Cookies.set('token', token)
              processQueue(null, token)
              originalRequest.headers['Authorization'] = 'Bearer ' + token
              resolve(service(originalRequest))
            }).catch(err => {
              processQueue(err, null)
              // 刷新失败，只清除对应的token并跳转登录
              const useAdmin = originalRequest?.__useAdmin || window.location.pathname.startsWith('/admin')
              const useMerchant = originalRequest?.__useMerchant || window.location.pathname.startsWith('/merchant')
              
              if (useAdmin) {
                Cookies.remove('adminToken')
                window.location.assign('/admin/login')
              } else if (useMerchant) {
                Cookies.remove('merchantToken')
                window.location.assign('/merchant/login')
              } else {
                Cookies.remove('token')
                window.location.assign('/login')
              }
              reject(err)
            }).finally(() => {
              isRefreshing = false
            })
          })
        } else {
          // 已经重试过或者是刷新接口本身，直接跳转登录
          const useAdmin = response.config?.__useAdmin || window.location.pathname.startsWith('/admin')
          const useMerchant = response.config?.__useMerchant || window.location.pathname.startsWith('/merchant')
          
          if (useAdmin) {
            Cookies.remove('adminToken')
            window.location.assign('/admin/login')
          } else if (useMerchant) {
            Cookies.remove('merchantToken')
            window.location.assign('/merchant/login')
          } else {
            Cookies.remove('token')
            window.location.assign('/login')
          }
        }
      }
      const err = new Error(res.message || 'Error')
      err.name = 'ApiBusinessError'
      err.code = res.code
      err.response = res
      return Promise.reject(err)
    } else {
      return res
    }
  },
  error => {
    logError(error)
    const status = error.response?.status
    const msg = error.response?.data?.message || error.response?.data?.msg || error.message
    ElMessage({
      message: msg,
      type: 'error',
      duration: 5 * 1000
    })

    if (status === 401) {
      // 尝试刷新token
      const originalRequest = error.config
      if (!originalRequest._retry && !originalRequest.url?.includes('/auth/refresh')) {
        originalRequest._retry = true
        
        if (isRefreshing) {
          // 如果正在刷新，将请求加入队列
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then(token => {
            originalRequest.headers['Authorization'] = 'Bearer ' + token
            return service(originalRequest)
          }).catch(err => {
            return Promise.reject(err)
          })
        }

        isRefreshing = true
        
        return new Promise((resolve, reject) => {
          auth.refreshToken().then(response => {
            const { token } = response.data
            Cookies.set('token', token)
            processQueue(null, token)
            originalRequest.headers['Authorization'] = 'Bearer ' + token
            resolve(service(originalRequest))
          }).catch(err => {
            processQueue(err, null)
            // 刷新失败，清除token并跳转登录
            Cookies.remove('token')
            Cookies.remove('adminToken')
            Cookies.remove('merchantToken')
            const useAdmin = originalRequest?.__useAdmin || window.location.pathname.startsWith('/admin')
            const useMerchant = originalRequest?.__useMerchant || window.location.pathname.startsWith('/merchant')
            window.location.assign(useAdmin ? '/admin/login' : (useMerchant ? '/merchant/login' : '/login'))
            reject(err)
          }).finally(() => {
            isRefreshing = false
          })
        })
      } else {
        // 已经重试过或者是刷新接口本身，直接跳转登录
        Cookies.remove('token')
        Cookies.remove('adminToken')
        Cookies.remove('merchantToken')
        const useAdmin = error.config?.__useAdmin || window.location.pathname.startsWith('/admin')
        const useMerchant = error.config?.__useMerchant || window.location.pathname.startsWith('/merchant')
        window.location.assign(useAdmin ? '/admin/login' : (useMerchant ? '/merchant/login' : '/login'))
      }
    }
    return Promise.reject(error)
  }
)

export default service
