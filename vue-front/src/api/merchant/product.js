import request from '@/utils/request'

export const merchantProduct = {
  getList(params) {
    return request({
      url: '/merchant/product/list',
      method: 'get',
      params
    })
  },
  getDetail(productId) {
    return request({
      url: `/merchant/product/detail/${productId}`,
      method: 'get'
    })
  },
  add(data) {
    return request({
      url: '/merchant/product/add',
      method: 'post',
      data
    })
  },
  update(productId, data) {
    return request({
      url: `/merchant/product/update/${productId}`,
      method: 'put',
      data
    })
  },
  delete(productId) {
    return request({
      url: `/merchant/product/delete/${productId}`,
      method: 'delete'
    })
  },
  batchStatus(productIds, status) {
    return request({
      url: '/merchant/product/batchStatus',
      method: 'put',
      data: { productIds, status }
    })
  },
  async updateStatus(productId, status) {
    return this.batchStatus([productId], status)
  },
  async batchDelete(productIds) {
    for (const id of productIds || []) {
      await request({
        url: `/merchant/product/delete/${id}`,
        method: 'delete'
      })
    }
    return { code: 200, message: 'success', data: null }
  },
  getCategories() {
    return request({
      url: '/merchant/product/categories',
      method: 'get'
    })
  }
}

export default merchantProduct
