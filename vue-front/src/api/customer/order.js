import request from '@/utils/request'

export const order = {
  confirm(data) {
    return request({
      url: '/customer/order/confirm',
      method: 'post',
      data
    })
  },
  create(data) {
    return request({
      url: '/customer/order/create',
      method: 'post',
      data
    })
  },
  getList(params) {
    return request({
      url: '/customer/order/list',
      method: 'get',
      params
    })
  },
  getDetail(orderId) {
    return request({
      url: `/customer/order/detail/${orderId}`,
      method: 'get'
    })
  },
  cancel(orderId, data) {
    return request({
      url: `/customer/order/cancel/${orderId}`,
      method: 'put',
      data
    })
  },
  confirmReceive(orderId) {
    return request({
      url: `/customer/order/confirm/${orderId}`,
      method: 'put'
    })
  },
  delete(orderId) {
    return request({
      url: `/customer/order/delete/${orderId}`,
      method: 'delete'
    })
  },
  pay(data) {
    return request({
      url: '/customer/order/pay',
      method: 'post',
      data
    })
  },
  getPayStatus(orderId) {
    return request({
      url: `/customer/order/payStatus/${orderId}`,
      method: 'get'
    })
  },
  getLogistics(orderId) {
    return request({
      url: `/customer/order/logistics/${orderId}`,
      method: 'get'
    })
  },
  remindDeliver(orderId) {
    return request({
      url: `/customer/order/remindDeliver/${orderId}`,
      method: 'post'
    })
  }
}

export default order
