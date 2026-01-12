
import request from '@/utils/request'

import { auth } from './customer/auth'
import { user } from './customer/user'
import { home } from './customer/home'
import { product } from './customer/product'
import { shop } from './customer/shop'
import { cart } from './customer/cart'
import { order } from './customer/order'
import { comment } from './customer/comment'
import { address } from './customer/address'
import { coupon } from './customer/coupon'

import { merchantAuth } from './merchant/auth'
import { merchantProduct } from './merchant/product'
import { merchantOrder } from './merchant/order'
import { merchantShop } from './merchant/shop'
import { merchantStatistics } from './merchant/statistics'

export {
  auth,
  user,
  home,
  product,
  shop,
  cart,
  order,
  comment,
  address,
  coupon,
  merchantAuth,
  merchantProduct,
  merchantOrder,
  merchantShop,
  merchantStatistics
}

// 管理员端接口
export const admin = {
  captcha() {
    return request({
      url: '/admin/auth/captcha',
      method: 'get'
    })
  },
  login(data) {
    return request({
      url: '/admin/auth/login',
      method: 'post',
      data
    })
  },
  logout() {
    return request({
      url: '/admin/auth/logout',
      method: 'post'
    })
  },
  info() {
    return request({
      url: '/admin/auth/info',
      method: 'get'
    })
  },

  todayData(params) {
    return request({
      url: '/admin/dashboard/todayData',
      method: 'get',
      params
    })
  },
  platformData() {
    return request({
      url: '/admin/dashboard/platformData',
      method: 'get'
    })
  },
  chartData(params) {
    return request({
      url: '/admin/dashboard/chartData',
      method: 'get',
      params
    })
  },

  merchants(params) {
    return request({
      url: '/admin/merchant/list',
      method: 'get',
      params
    })
  },
  auditMerchant(data) {
    return request({
      url: '/admin/merchant/audit',
      method: 'post',
      data
    })
  },
  updateMerchantStatus(data) {
    return request({
      url: '/admin/merchant/updateStatus',
      method: 'put',
      data
    })
  },

  userList(params) {
    return request({
      url: '/admin/user/list',
      method: 'get',
      params
    })
  },
  userDetail(userId) {
    return request({
      url: `/admin/user/detail/${userId}`,
      method: 'get'
    })
  },
  userUpdateStatus(data) {
    return request({
      url: '/admin/user/updateStatus',
      method: 'put',
      data
    })
  },
  userAdjustBalance(data) {
    return request({
      url: '/admin/user/adjustBalance',
      method: 'post',
      data
    })
  },
  userAdjustPoints(data) {
    return request({
      url: '/admin/user/adjustPoints',
      method: 'post',
      data
    })
  },
  userOrders(userId, params) {
    return request({
      url: `/admin/user/orders/${userId}`,
      method: 'get',
      params
    })
  },

  productList(params) {
    return request({
      url: '/admin/product/list',
      method: 'get',
      params
    })
  },
  productDetail(productId) {
    return request({
      url: `/admin/product/detail/${productId}`,
      method: 'get'
    })
  },
  productAudit(data) {
    return request({
      url: '/admin/product/audit',
      method: 'post',
      data
    })
  },
  productForceOffline(data) {
    return request({
      url: '/admin/product/forceOffline',
      method: 'put',
      data
    })
  },
  productDelete(productId) {
    return request({
      url: `/admin/product/delete/${productId}`,
      method: 'delete'
    })
  },

  categoryTree() {
    return request({
      url: '/admin/category/tree',
      method: 'get'
    })
  },
  categoryAdd(data) {
    return request({
      url: '/admin/category/add',
      method: 'post',
      data
    })
  },
  categoryUpdate(categoryId, data) {
    return request({
      url: `/admin/category/update/${categoryId}`,
      method: 'put',
      data
    })
  },
  categoryDelete(categoryId) {
    return request({
      url: `/admin/category/delete/${categoryId}`,
      method: 'delete'
    })
  },

  orderList(params) {
    return request({
      url: '/admin/order/list',
      method: 'get',
      params
    })
  },
  orderDetail(orderId) {
    return request({
      url: `/admin/order/detail/${orderId}`,
      method: 'get'
    })
  },
  orderStatistics(params) {
    return request({
      url: '/admin/order/statistics',
      method: 'get',
      params
    })
  },

  refundList(params) {
    return request({
      url: '/admin/refund/list',
      method: 'get',
      params
    })
  },
  refundDetail(refundId) {
    return request({
      url: `/admin/refund/detail/${refundId}`,
      method: 'get'
    })
  },
  refundArbitrate(data) {
    return request({
      url: '/admin/refund/arbitrate',
      method: 'post',
      data
    })
  },

  commentList(params) {
    return request({
      url: '/admin/comment/list',
      method: 'get',
      params
    })
  },
  commentDelete(commentId) {
    return request({
      url: `/admin/comment/delete/${commentId}`,
      method: 'delete'
    })
  },
  commentUpdateStatus(data) {
    return request({
      url: '/admin/comment/updateStatus',
      method: 'put',
      data
    })
  },

  couponList(params) {
    return request({
      url: '/admin/coupon/list',
      method: 'get',
      params
    })
  },
  couponDetail(couponId) {
    return request({
      url: `/admin/coupon/detail/${couponId}`,
      method: 'get'
    })
  },
  couponAdd(data) {
    return request({
      url: '/admin/coupon/add',
      method: 'post',
      data
    })
  },
  couponUpdate(couponId, data) {
    return request({
      url: `/admin/coupon/update/${couponId}`,
      method: 'put',
      data
    })
  },
  couponDelete(couponId) {
    return request({
      url: `/admin/coupon/delete/${couponId}`,
      method: 'delete'
    })
  },

  bannerList() {
    return request({
      url: '/admin/banner/list',
      method: 'get'
    })
  },
  bannerAdd(data) {
    return request({
      url: '/admin/banner/add',
      method: 'post',
      data
    })
  },
  bannerUpdate(bannerId, data) {
    return request({
      url: `/admin/banner/update/${bannerId}`,
      method: 'put',
      data
    })
  },
  bannerDelete(bannerId) {
    return request({
      url: `/admin/banner/delete/${bannerId}`,
      method: 'delete'
    })
  },

  noticeList(params) {
    return request({
      url: '/admin/notice/list',
      method: 'get',
      params
    })
  },
  noticeDetail(noticeId) {
    return request({
      url: `/admin/notice/detail/${noticeId}`,
      method: 'get'
    })
  },
  noticeAdd(data) {
    return request({
      url: '/admin/notice/add',
      method: 'post',
      data
    })
  },
  noticeUpdate(noticeId, data) {
    return request({
      url: `/admin/notice/update/${noticeId}`,
      method: 'put',
      data
    })
  },
  noticeDelete(noticeId) {
    return request({
      url: `/admin/notice/delete/${noticeId}`,
      method: 'delete'
    })
  },

  adminList(params) {
    return request({
      url: '/admin/admin/list',
      method: 'get',
      params
    })
  },
  adminAdd(data) {
    return request({
      url: '/admin/admin/add',
      method: 'post',
      data
    })
  },
  adminUpdate(adminId, data) {
    return request({
      url: `/admin/admin/update/${adminId}`,
      method: 'put',
      data
    })
  },
  adminDelete(adminId) {
    return request({
      url: `/admin/admin/delete/${adminId}`,
      method: 'delete'
    })
  },
  adminResetPassword(adminId, data) {
    return request({
      url: `/admin/admin/resetPassword/${adminId}`,
      method: 'put',
      data
    })
  },

  roleList(params) {
    return request({
      url: '/admin/role/list',
      method: 'get',
      params
    })
  },
  roleDetail(roleId) {
    return request({
      url: `/admin/role/detail/${roleId}`,
      method: 'get'
    })
  },
  roleAdd(data) {
    return request({
      url: '/admin/role/add',
      method: 'post',
      data
    })
  },
  roleUpdate(roleId, data) {
    return request({
      url: `/admin/role/update/${roleId}`,
      method: 'put',
      data
    })
  },
  roleDelete(roleId) {
    return request({
      url: `/admin/role/delete/${roleId}`,
      method: 'delete'
    })
  },
  rolePermissions(roleId) {
    return request({
      url: `/admin/role/permissions/${roleId}`,
      method: 'get'
    })
  },
  permissionTree() {
    return request({
      url: '/admin/permission/tree',
      method: 'get'
    })
  },

  // 兼容 views/admin/User.vue 里使用的旧方法名
  list(params) {
    return request({
      url: '/admin/admin/list',
      method: 'get',
      params
    })
  },
  roles() {
    return request({
      url: '/admin/role/list',
      method: 'get',
      params: { page: 1, pageSize: 9999 }
    }).then(res => ({ ...res, data: res.data?.list || [] }))
  },
  add(data) {
    return request({
      url: '/admin/admin/add',
      method: 'post',
      data
    })
  },
  update(adminId, data) {
    return request({
      url: `/admin/admin/update/${adminId}`,
      method: 'put',
      data
    })
  },
  updateStatus(adminId, data) {
    // 后端不支持单独 updateStatus 时，前端页面会自行传 roleId 等字段
    return request({
      url: `/admin/admin/update/${adminId}`,
      method: 'put',
      data
    })
  },
  resetPassword(adminId) {
    return request({
      url: `/admin/admin/resetPassword/${adminId}`,
      method: 'put',
      data: { newPassword: '123456' }
    })
  },

  // 兼容 views/admin/Merchant.vue 批量操作（后端是单条接口，这里做循环）
  async updateMerchantStatus(data) {
    const ids = data?.merchantIds || []
    const targetStatus = data?.status
    for (const id of ids) {
      await request({
        url: '/admin/merchant/updateStatus',
        method: 'put',
        data: { merchantId: id, status: targetStatus }
      })
    }
    return { code: 200, message: 'success', data: null }
  },
  async deleteMerchants(data) {
    const ids = data?.merchantIds || []
    for (const id of ids) {
      await request({
        url: '/admin/merchant/updateStatus',
        method: 'put',
        data: { merchantId: id, status: 2, reason: '批量禁用' }
      })
    }
    return { code: 200, message: 'success', data: null }
  }
}
