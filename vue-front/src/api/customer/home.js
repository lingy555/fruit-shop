import request from '@/utils/request'

export const home = {
  getIndexData() {
    return request({
      url: '/customer/home/index',
      method: 'get'
    })
  },
  getBanners(params) {
    return request({
      url: '/customer/home/banners',
      method: 'get',
      params
    })
  },
  getNotices(params) {
    return request({
      url: '/customer/home/notices',
      method: 'get',
      params
    })
  }
}

export default home
