import request from '@/utils/request'

export const merchantAuth = {
  login(data) {
    return request({
      url: '/merchant/auth/login',
      method: 'post',
      data
    })
  },
  logout() {
    return request({
      url: '/merchant/auth/logout',
      method: 'post'
    })
  },
  getMerchantInfo() {
    return request({
      url: '/merchant/auth/info',
      method: 'get'
    })
  },
  register(data) {
    return request({
      url: '/merchant/auth/register',
      method: 'post',
      data
    })
  },
  changePassword(data) {
    return request({
      url: '/merchant/auth/changePassword',
      method: 'put',
      data
    })
  },
  resetPassword(data) {
    return request({
      url: '/merchant/auth/resetPassword',
      method: 'post',
      data
    })
  }
}

export default merchantAuth
