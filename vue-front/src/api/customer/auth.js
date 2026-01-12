import request from '@/utils/request'

export const auth = {
  register(data) {
    return request({
      url: '/customer/auth/register',
      method: 'post',
      data
    })
  },
  login(data) {
    return request({
      url: '/customer/auth/login',
      method: 'post',
      data
    })
  },
  loginByPhone(data) {
    return request({
      url: '/customer/auth/loginByPhone',
      method: 'post',
      data
    })
  },
  sendCode(data) {
    return request({
      url: '/customer/auth/sendCode',
      method: 'post',
      data
    })
  },
  logout() {
    return request({
      url: '/customer/auth/logout',
      method: 'post'
    })
  },
  refreshToken() {
    return request({
      url: '/customer/auth/refresh',
      method: 'post'
    })
  },
  resetPassword(data) {
    return request({
      url: '/customer/auth/resetPassword',
      method: 'post',
      data
    })
  }
}

export default auth
