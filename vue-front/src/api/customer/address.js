import request from '@/utils/request'

export const address = {
  getList() {
    return request({
      url: '/customer/address/list',
      method: 'get'
    })
  },
  add(data) {
    return request({
      url: '/customer/address/add',
      method: 'post',
      data
    })
  },
  update(data) {
    return request({
      url: '/customer/address/update',
      method: 'put',
      data
    })
  },
  delete(addressId) {
    return request({
      url: `/customer/address/delete/${addressId}`,
      method: 'delete'
    })
  },
  setDefault(addressId) {
    return request({
      url: `/customer/address/setDefault/${addressId}`,
      method: 'put'
    })
  }
}

export default address
