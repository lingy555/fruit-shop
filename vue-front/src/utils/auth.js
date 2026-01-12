
/**
 * 认证工具
 * 用于处理用户登录状态、token管理等
 */

import Cookies from 'js-cookie'

// Token相关操作
export const getToken = () => {
  return Cookies.get('token') || ''
}

export const setToken = (token) => {
  if (token) {
    Cookies.set('token', token, { expires: 7 }) // 7天过期
  } else {
    Cookies.remove('token')
  }
}

export const removeToken = () => {
  Cookies.remove('token')
}

// 用户信息相关操作
export const getUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo) : null
}

export const setUserInfo = (userInfo) => {
  if (userInfo) {
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  } else {
    localStorage.removeItem('userInfo')
  }
}

// 登录状态检查
export const isLoggedIn = () => {
  return !!getToken()
}

// 权限检查
export const hasPermission = (permission) => {
  // 这里可以根据实际需求实现权限检查逻辑
  return true // 暂时返回true
}

// 清除所有认证信息
export const clearAuth = () => {
  removeToken()
  setUserInfo(null)
}

// 密码加密（简单示例）
export const encryptPassword = (password) => {
  // 这里应该使用更安全的加密方式
  return btoa(password) // 简单示例，实际项目中应该使用bcrypt等
}

// 密码验证
export const validatePassword = (password) => {
  // 密码长度至少6位
  return password.length >= 6
}

// 手机号验证
export const validatePhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

// 邮箱验证
export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 格式化手机号
export const formatPhone = (phone) => {
  const cleanedPhone = phone.replace(/\s/g, '')
  return cleanedPhone
}

// 格式化金额
export const formatMoney = (amount) => {
  return `¥${parseFloat(amount).toFixed(2)}`
}

// 格式化日期
export const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 生成随机字符串
export const generateRandomString = (length = 8) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}
