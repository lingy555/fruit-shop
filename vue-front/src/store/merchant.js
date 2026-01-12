import { defineStore } from 'pinia'
import { merchantAuth } from '@/api'
import Cookies from 'js-cookie'

export const useMerchantStore = defineStore('merchant', {
  state: () => ({
    merchantToken: Cookies.get('merchantToken') || '',
    merchantInfo: null,
    isLogin: false
  }),

  getters: {
    // 获取商家头像
    avatar: (state) => {
      return state.merchantInfo?.avatar || ''
    },
    // 获取商家昵称
    nickname: (state) => {
      return state.merchantInfo?.shopName || state.merchantInfo?.nickname || ''
    },
    // 检查是否已登录
    isLoggedIn: (state) => {
      return !!(state.merchantToken && state.merchantInfo)
    }
  },

  actions: {
    // 商家登录
    async login(loginForm) {
      try {
        const response = await merchantAuth.login(loginForm)
        const { token, merchantInfo } = response.data

        // 保存token到cookie
        Cookies.set('merchantToken', token)
        this.merchantToken = token
        this.merchantInfo = merchantInfo
        this.isLogin = true

        return response
      } catch (error) {
        throw error
      }
    },

    // 获取商家信息
    async getMerchantInfo() {
      try {
        const response = await merchantAuth.getMerchantInfo()
        console.log('getMerchantInfo API响应:', response)
        this.merchantInfo = response.data.data
        this.isLogin = true
        console.log('merchantInfo已设置:', this.merchantInfo)
        return response
      } catch (error) {
        this.resetState()
        throw error
      }
    },

    // 商家登出
    async logout() {
      try {
        await merchantAuth.logout()
      } catch (error) {
        // 忽略错误，继续清理本地状态
      } finally {
        this.resetState()
      }
    },

    // 重置状态
    resetState() {
      Cookies.remove('merchantToken')
      this.merchantToken = ''
      this.merchantInfo = null
      this.isLogin = false
    }
  }
})
