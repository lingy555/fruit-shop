import request from '@/utils/request'

export const user = {
  getProfile() {
    return request({
      url: '/customer/user/profile',
      method: 'get'
    })
  },
  updateProfile(data) {
    return request({
      url: '/customer/user/update',
      method: 'put',
      data
    })
  },
  changePassword(data) {
    return request({
      url: '/customer/user/changePassword',
      method: 'put',
      data
    })
  },
  changePhone(data) {
    return request({
      url: '/customer/user/changePhone',
      method: 'put',
      data
    })
  },
  getBalance() {
    return request({
      url: '/customer/user/balance',
      method: 'get'
    })
  },
  getPointsRecords(params) {
    return request({
      url: '/customer/user/points/records',
      method: 'get',
      params
    })
  }
}

export default user
