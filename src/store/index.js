
/**
 * Vuex store配置文件
 * 用于全局状态管理，替代Pinia
 */

import { createStore } from 'vuex'
import user from './modules/user'
import cart from './modules/cart'
import order from './modules/order'

// 创建store实例
const store = createStore({
  modules: {
    user,
    cart,
    order
  },

  // 开发环境下的严格模式
  strict: process.env.NODE_ENV !== 'production'
})

export default store
