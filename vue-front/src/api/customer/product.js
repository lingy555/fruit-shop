import request from '@/utils/request'

export const product = {
  getCategories() {
    return request({
      url: '/customer/product/categories',
      method: 'get'
    })
  },
  getList(params) {
    return request({
      url: '/customer/product/list',
      method: 'get',
      params
    })
  },
  getDetail(productId) {
    return request({
      url: `/customer/product/detail/${productId}`,
      method: 'get'
    })
  },
  getComments(productId, params) {
    return request({
      url: `/customer/product/comments/${productId}`,
      method: 'get',
      params
    })
  },
  getHotKeywords() {
    return request({
      url: '/customer/product/hotKeywords',
      method: 'get'
    })
  },
  getSearchHistory() {
    return request({
      url: '/customer/product/searchHistory',
      method: 'get'
    })
  },
  clearSearchHistory() {
    return request({
      url: '/customer/product/searchHistory',
      method: 'delete'
    })
  }
}

export default product
