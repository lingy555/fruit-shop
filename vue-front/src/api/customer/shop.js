import request from '@/utils/request'

export const shop = {
  getDetail(shopId) {
    return request({
      url: `/customer/shop/${shopId}`,
      method: 'get'
    })
  },
  getProducts(shopId, params) {
    return request({
      url: `/customer/shop/${shopId}/products`,
      method: 'get',
      params
    })
  },
  favorite(data) {
    return request({
      url: '/customer/shop/favorite',
      method: 'post',
      data
    })
  },
  cancelFavorite(shopId) {
    return request({
      url: `/customer/shop/favorite/${shopId}`,
      method: 'delete'
    })
  },
  getMyFavorites(params) {
    return request({
      url: '/customer/shop/myFavorites',
      method: 'get',
      params
    })
  }
}

export default shop
