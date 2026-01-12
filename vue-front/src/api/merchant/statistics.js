import request from '@/utils/request'

export const merchantStatistics = {
  overview(params) {
    return request({
      url: '/merchant/statistics/overview',
      method: 'get',
      params
    })
  },
  sales(params) {
    return request({
      url: '/merchant/statistics/sales',
      method: 'get',
      params
    })
  },
  product(params) {
    return request({
      url: '/merchant/statistics/product',
      method: 'get',
      params
    })
  },
  customer(params) {
    return request({
      url: '/merchant/statistics/customer',
      method: 'get',
      params
    })
  }
}

export default merchantStatistics
