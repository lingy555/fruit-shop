import request from '@/utils/request'

export const comment = {
  getWaitList(params) {
    return request({
      url: '/customer/comment/waitList',
      method: 'get',
      params
    })
  },
  add(data) {
    return request({
      url: '/customer/comment/add',
      method: 'post',
      data
    })
  },
  append(data) {
    return request({
      url: '/customer/comment/append',
      method: 'post',
      data
    })
  },
  getMyList(params) {
    return request({
      url: '/customer/comment/myList',
      method: 'get',
      params
    })
  },
  delete(commentId) {
    return request({
      url: `/customer/comment/delete/${commentId}`,
      method: 'delete'
    })
  }
}

export default comment
