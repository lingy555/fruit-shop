import request from '@/utils/request'

export const coupon = {
  available(params) {
    return request({
      url: '/customer/coupon/available',
      method: 'get',
      params
    })
  },
  receive(couponId) {
    return request({
      url: `/customer/coupon/receive/${couponId}`,
      method: 'post'
    })
  },
  my(params) {
    return request({
      url: '/customer/coupon/my',
      method: 'get',
      params
    })
  }
}

export default coupon
