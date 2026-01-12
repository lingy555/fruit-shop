import request from '@/utils/request'

export const merchantOrder = {
  list(params) {
    return request({
      url: '/merchant/order/list',
      method: 'get',
      params
    })
  },
  detail(orderId) {
    return request({
      url: `/merchant/order/detail/${orderId}`,
      method: 'get'
    })
  },
  deliver(data) {
    return request({
      url: '/merchant/order/deliver',
      method: 'post',
      data
    })
  },
  batchDeliver(data) {
    return request({
      url: '/merchant/order/batchDeliver',
      method: 'post',
      data
    })
  },
  updateShippingFee(data) {
    return request({
      url: '/merchant/order/updateShippingFee',
      method: 'put',
      data
    })
  },
  updateRemark(data) {
    return request({
      url: '/merchant/order/updateRemark',
      method: 'put',
      data
    })
  },
  export(data) {
    return request({
      url: '/merchant/order/export',
      method: 'post',
      data
    })
  }
}

export default merchantOrder
