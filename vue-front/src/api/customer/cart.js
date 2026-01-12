import request from '@/utils/request'

export const cart = {
  getList() {
    return request({
      url: '/customer/cart/list',
      method: 'get'
    })
  },
  add(data) {
    return request({
      url: '/customer/cart/add',
      method: 'post',
      data
    })
  },
  updateQuantity(data) {
    return request({
      url: '/customer/cart/updateQuantity',
      method: 'put',
      data
    })
  },
  delete(data) {
    return request({
      url: '/customer/cart/delete',
      method: 'delete',
      data
    })
  },
  selectAll(data) {
    return request({
      url: '/customer/cart/selectAll',
      method: 'put',
      data
    })
  },
  select(data) {
    return request({
      url: '/customer/cart/select',
      method: 'put',
      data
    })
  },
  clearInvalid() {
    return request({
      url: '/customer/cart/clearInvalid',
      method: 'delete'
    })
  }
}

export default cart
