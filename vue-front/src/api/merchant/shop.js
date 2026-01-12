import request from '@/utils/request'

export const merchantShop = {
  info() {
    return request({
      url: '/merchant/shop/info',
      method: 'get'
    })
  },
  update(data) {
    return request({
      url: '/merchant/shop/update',
      method: 'put',
      data
    })
  },
  statistics(params) {
    // 兼容页面：Shop.vue 用的是 /shop/statistics；Statistics.vue 用的是 overview 语义
    if (params?.dateType != null || params?.startDate != null || params?.endDate != null) {
      return request({
        url: '/merchant/statistics/overview',
        method: 'get',
        params
      })
    }
    return request({
      url: '/merchant/shop/statistics',
      method: 'get',
      params
    })
  },
  status(data) {
    return request({
      url: '/merchant/shop/status',
      method: 'put',
      data
    })
  },
  salesChart(params) {
    return request({
      url: '/merchant/statistics/sales',
      method: 'get',
      params
    })
  },
  productRank(params) {
    return request({
      url: '/merchant/statistics/product',
      method: 'get',
      params
    })
  }
}

export default merchantShop
