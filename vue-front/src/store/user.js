
import { defineStore } from 'pinia'
import { auth, user } from '@/api'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: Cookies.get('token') || '',
    userInfo: null,
    isLogin: false
  }),

  getters: {
    // 获取用户头像
    avatar: (state) => {
      return state.userInfo?.avatar || ''
    },
    // 获取用户昵称
    nickname: (state) => {
      return state.userInfo?.nickname || ''
    }
  },

  actions: {
    // 用户登录
    async login(loginForm) {
      try {
        const response = await auth.login(loginForm)
        const { token, userInfo } = response.data

        // 保存token到cookie
        Cookies.set('token', token)
        this.token = token
        this.userInfo = userInfo
        this.isLogin = true

        return response
      } catch (error) {
        throw error
      }
    },

    // 手机验证码登录
    async loginByPhone(loginForm) {
      try {
        const response = await auth.loginByPhone(loginForm)
        const { token, userInfo } = response.data

        // 保存token到cookie
        Cookies.set('token', token)
        this.token = token
        this.userInfo = userInfo
        this.isLogin = true

        return response
      } catch (error) {
        throw error
      }
    },

    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await user.getProfile()
        console.log('getUserInfo API响应:', response)
        this.userInfo = response.data.data
        this.isLogin = true
        console.log('userInfo已设置:', this.userInfo)
        return response
      } catch (error) {
        throw error
      }
    },

    // 用户退出登录
    async logout() {
      try {
        await auth.logout()
      } catch (error) {
        console.error('退出登录请求失败:', error)
      } finally {
        // 无论请求成功失败，都清除本地状态
        this.token = ''
        this.userInfo = null
        this.isLogin = false
        Cookies.remove('token')
      }
    },

    // 重置状态
    resetState() {
      this.token = ''
      this.userInfo = null
      this.isLogin = false
      Cookies.remove('token')
    }
  }
})
