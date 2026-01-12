
/**
 * 用户状态管理模块
 */

import { defineStore } from 'pinia'

// 定义用户状态
const state = () => ({
  // 用户信息
  userInfo: null,

  // 是否已登录
  isLogin: false,

  // 用户token
  token: localStorage.getItem('token') || '',

  // 购物车数量
  cartCount: 0,

  // 收货地址列表
  addresses: [],

  // 订单数量统计
  orderStats: {
    waitPay: 0,
    waitDeliver: 0,
    waitReceive: 0,
    waitComment: 0
  }
})

// 定义mutations
const mutations = {
  // 设置用户信息
  setUserInfo(state, userInfo) {
    state.userInfo = userInfo
  },

  // 设置登录状态
  setLoginStatus(state, isLogin) {
    state.isLogin = isLogin
  },

  // 设置token
  setToken(state, token) {
    state.token = token
    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }
  },

  // 设置购物车数量
  setCartCount(state, count) {
    state.cartCount = count
  },

  // 设置收货地址
  setAddresses(state, addresses) {
    state.addresses = addresses
  },

  // 设置订单统计
  setOrderStats(state, stats) {
    state.orderStats = { ...state.orderStats, ...stats }
  },

  // 更新订单统计
  updateOrderStats(state, type, value) {
    if (state.orderStats.hasOwnProperty(type)) {
      state.orderStats[type] = value
    }
  }
}

// 定义actions
const actions = {
  // 用户登录
  login({ commit }, loginData) {
    commit('setToken', loginData.token)
    commit('setUserInfo', loginData.userInfo)
    commit('setLoginStatus', true)
  },

  // 用户退出
  logout({ commit }) {
    commit('setToken', '')
    commit('setUserInfo', null)
    commit('setLoginStatus', false)
  },

  // 更新用户信息
  updateUserInfo({ commit }, userInfo) {
    commit('setUserInfo', userInfo)
  },

  // 更新购物车数量
  updateCartCount({ commit }, count) {
    commit('setCartCount', count)
  },

  // 设置收货地址
  setAddresses({ commit }, addresses) {
    commit('setAddresses', addresses)
  },

  // 添加收货地址
  addAddress({ commit }, address) {
    const newAddresses = [...(state().addresses || []), address]
    commit('setAddresses', newAddresses)
  },

  // 更新收货地址
  updateAddress({ commit }, index, address) {
    const newAddresses = [...(state().addresses || [])]
    newAddresses[index] = address
    commit('setAddresses', newAddresses)
  },

  // 删除收货地址
  deleteAddress({ commit }, index) {
    const newAddresses = [...(state().addresses || [])]
    newAddresses.splice(index, 1)
    commit('setAddresses', newAddresses)
  },

  // 设置订单统计
  setOrderStats({ commit }, stats) {
    commit('setOrderStats', stats)
  }
}

// 定义getters
const getters = {
  // 获取用户信息
  userInfo: state => state.userInfo,

  // 获取登录状态
  isLogin: state => state.isLogin,

  // 获取token
  token: state => state.token,

  // 获取购物车数量
  cartCount: state => state.cartCount,

  // 获取收货地址
  addresses: state => state.addresses,

  // 获取订单统计
  orderStats: state => state.orderStats
}

// 创建并导出store
export const useUserStore = defineStore({
  id: 'user',
  state,
  getters,
  actions,
  mutations
})
