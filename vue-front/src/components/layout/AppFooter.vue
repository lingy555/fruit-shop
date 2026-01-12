<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-section">
          <h3 class="footer-title">关于我们</h3>
          <ul class="footer-links">
            <li><a href="#">公司简介</a></li>
            <li><a href="#">招贤纳士</a></li>
            <li><a href="#">联系我们</a></li>
            <li><a href="#">投诉建议</a></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">帮助中心</h3>
          <ul class="footer-links">
            <li><a href="#">购物指南</a></li>
            <li><a href="#">支付方式</a></li>
            <li><a href="#">配送方式</a></li>
            <li><a href="#">售后服务</a></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">商家服务</h3>
          <ul class="footer-links">
            <li><a href="#">商家入驻</a></li>
            <li><a href="#">商家中心</a></li>
            <li><a href="#">运营服务</a></li>
            <li><a href="#">营销推广</a></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">关注我们</h3>
          <div class="footer-qrcode">
            <div class="qrcode-item">
              <div class="qrcode-img"></div>
              <span>微信公众号</span>
            </div>
            <div class="qrcode-item">
              <div class="qrcode-img"></div>
              <span>小程序</span>
            </div>
          </div>
          <div class="footer-contact">
            <p><el-icon><Phone /></el-icon> 客服热线：400-888-8888</p>
            <p><el-icon><Clock /></el-icon> 工作时间：09:00-21:00</p>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <div class="copyright">
          <p>Copyright © 2024 欢喜果铺 版权所有</p>
          <p>
            <a href="#">隐私政策</a> |
            <a href="#">用户协议</a> |
            <a href="#">营业执照</a> |
            <a href="#">ICP备案号：粤ICP备88888888号</a>
          </p>
        </div>

        <!-- <div class="footer-links-bottom">
          <a href="#"><img src="@/assets/footer/friendlink1.png" alt="友情链接" /></a>
          <a href="#"><img src="@/assets/footer/friendlink2.png" alt="友情链接" /></a>
          <a href="#"><img src="@/assets/footer/friendlink3.png" alt="友情链接" /></a>
        </div> -->
      </div>
    </div>
  </footer>
</template>

<script setup>
// 这里可以添加一些逻辑，如获取友情链接等
</script>

<style scoped>
.app-footer {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: 40px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
}

.footer-section {
  flex: 1;
  padding: 0 15px;
}

.footer-title {
  font-size: 18px;
  margin-bottom: 20px;
  color: #fff;
  position: relative;
  padding-bottom: 10px;
}

.footer-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 40px;
  height: 2px;
  background-color: #4CAF50;
}

.footer-links {
  list-style: none;
}

.footer-links li {
  margin-bottom: 10px;
}

.footer-links a {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #4CAF50;
}

.footer-qrcode {
  display: flex;
  margin-bottom: 20px;
}

.qrcode-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
}

.qrcode-img {
  width: 80px;
  height: 80px;
  background-color: #fff;
  margin-bottom: 5px;
  border-radius: 4px;
}

.qrcode-item span {
  font-size: 12px;
  color: #bdc3c7;
}

.footer-contact {
  font-size: 14px;
  color: #bdc3c7;
}

.footer-contact p {
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.footer-contact .el-icon {
  margin-right: 5px;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.copyright {
  font-size: 12px;
  color: #95a5a6;
}

.copyright p {
  margin-bottom: 5px;
}

.copyright a {
  color: #95a5a6;
  text-decoration: none;
  margin: 0 5px;
}

.copyright a:hover {
  color: #4CAF50;
}

.footer-links-bottom {
  display: flex;
  align-items: center;
}

.footer-links-bottom a {
  margin-left: 10px;
}

.footer-links-bottom img {
  height: 30px;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.footer-links-bottom img:hover {
  opacity: 1;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
  }

  .footer-section {
    margin-bottom: 30px;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
  }

  .footer-links-bottom {
    margin-top: 15px;
  }
}
</style>

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 路由组件懒加载
const Home = () => import('@/views/Home.vue')
const Category = () => import('@/views/Category.vue')
const Cart = () => import('@/views/Cart.vue')
const Order = () => import('@/views/Order.vue')
const User = () => import('@/views/User.vue')
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')
const ProductDetail = () => import('@/views/ProductDetail.vue')
const Checkout = () => import('@/views/Checkout.vue')
const OrderDetail = () => import('@/views/OrderDetail.vue')
const Address = () => import('@/views/Address.vue')
const Search = () => import('@/views/Search.vue')
const NotFound = () => import('@/views/NotFound.vue')

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '首页' }
  },
  {
    path: '/category',
    name: 'Category',
    component: Category,
    meta: { title: '分类' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetail,
    meta: { title: '商品详情' }
  },
  {
    path: '/search',
    name: 'Search',
    component: Search,
    meta: { title: '搜索' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: { title: '购物车', requireAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
    meta: { title: '结算', requireAuth: true }
  },
  {
    path: '/order',
    name: 'Order',
    component: Order,
    meta: { title: '我的订单', requireAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { title: '订单详情', requireAuth: true }
  },
  {
    path: '/user',
    name: 'User',
    component: User,
    meta: { title: '个人中心', requireAuth: true }
  },
  {
    path: '/address',
    name: 'Address',
    component: Address,
    meta: { title: '收货地址', requireAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面不存在' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 欢喜果铺` : '欢喜果铺'

  // 检查是否需要登录权限
  const userStore = useUserStore()
  if (to.meta.requireAuth && !userStore.isLogin) {
    // 需要登录但未登录，跳转到登录页
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLogin) {
    // 已登录状态下访问登录/注册页，跳转到首页
    next({ path: '/' })
  } else {
    next()
  }
})

export default router