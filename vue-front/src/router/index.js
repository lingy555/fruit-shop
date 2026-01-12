
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import Cookies from 'js-cookie'

// 路由组件懒加载
const Home = () => import('@/views/Home.vue')
const Category = () => import('@/views/Category.vue')
//const Product = () => import('@/views/Product.vue')
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
const Promotion = () => import('@/views/Promotion.vue')
const New = () => import('@/views/New.vue')
const Hot = () => import('@/views/Hot.vue')
const Filter = () => import('@/views/Filter.vue')
const Notices = () => import('@/views/Notices.vue')
const Payment = () => import('@/views/Payment.vue')
const AlipayPayment = () => import('@/views/AlipayPayment.vue')
const PaymentResult = () => import('@/views/PaymentResult.vue')
const CommentAdd = () => import('@/views/CommentAdd.vue')
const Chat = () => import('@/views/Chat.vue')
const NotFound = () => import('@/views/NotFound.vue')

// 管理员端
const AdminLayout = () => import('@/components/admin/AdminLayout.vue')
const AdminLogin = () => import('@/views/admin/Login.vue')
const AdminDashboard = () => import('@/views/admin/Dashboard.vue')
const AdminMerchant = () => import('@/views/admin/Merchant.vue')
const AdminSystemAdmin = () => import('@/views/admin/User.vue')
const AdminUserList = () => import('@/views/admin/UserList.vue')
const AdminProductList = () => import('@/views/admin/ProductList.vue')
const AdminCategory = () => import('@/views/admin/Category.vue')
const AdminOrderList = () => import('@/views/admin/OrderList.vue')
const AdminRefund = () => import('@/views/admin/Refund.vue')
const AdminContentBanner = () => import('@/views/admin/ContentBanner.vue')
const AdminContentNotice = () => import('@/views/admin/ContentNotice.vue')
const AdminContentCoupon = () => import('@/views/admin/ContentCoupon.vue')
const AdminSystemRole = () => import('@/views/admin/SystemRole.vue')
const AdminSystemMenu = () => import('@/views/admin/SystemMenu.vue')

// 商家端
const MerchantLayout = () => import('@/components/merchant/MerchantLayout.vue')
const MerchantLogin = () => import('@/views/merchant/Login.vue')
const MerchantProduct = () => import('@/views/merchant/Product.vue')
const MerchantOrder = () => import('@/views/merchant/Order.vue')
const MerchantChat = () => import('@/views/merchant/Chat.vue')
const MerchantShop = () => import('@/views/merchant/Shop.vue')
const MerchantStatistics = () => import('@/views/merchant/Statistics.vue')

// 路由配置
const Entry = () => import('@/views/Entry.vue')

const routes = [
  {
    path: '/',
    name: 'Entry',
    component: Entry,
    meta: { title: '选择身份登录' }
  },
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    component: MerchantLogin,
    meta: { title: '商家登录' }
  },
  {
    path: '/merchant',
    component: MerchantLayout,
    meta: { title: '商家后台' },
    children: [
      { path: '', redirect: '/merchant/product' },
      { path: 'product', component: MerchantProduct, meta: { title: '商品管理' } },
      { path: 'order', component: MerchantOrder, meta: { title: '订单管理' } },
      { path: 'chat', component: MerchantChat, meta: { title: '客服消息' } },
      { path: 'shop', component: MerchantShop, meta: { title: '店铺管理' } },
      { path: 'statistics', component: MerchantStatistics, meta: { title: '数据统计' } }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin,
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { title: '管理后台' },
    children: [
      { path: '', redirect: '/admin/dashboard/overview' },
      { path: 'dashboard/overview', component: AdminDashboard, meta: { title: '数据概览' } },
      { path: 'dashboard/user', component: AdminDashboard, meta: { title: '用户统计' } },
      { path: 'dashboard/order', component: AdminDashboard, meta: { title: '订单统计' } },
      { path: 'dashboard/product', component: AdminDashboard, meta: { title: '商品统计' } },

      { path: 'merchant/list', component: AdminMerchant, meta: { title: '商家列表' } },
      { path: 'merchant/audit', component: AdminMerchant, meta: { title: '商家审核' } },

      { path: 'product/list', component: AdminProductList, meta: { title: '商品列表' } },
      { path: 'product/category', component: AdminCategory, meta: { title: '分类管理' } },

      { path: 'order/list', component: AdminOrderList, meta: { title: '订单列表' } },
      { path: 'order/refund', component: AdminRefund, meta: { title: '退款处理' } },

      { path: 'user/list', component: AdminUserList, meta: { title: '用户列表' } },

      { path: 'content/banner', component: AdminContentBanner, meta: { title: '轮播图管理' } },
      { path: 'content/notice', component: AdminContentNotice, meta: { title: '公告管理' } },
      { path: 'content/coupon', component: AdminContentCoupon, meta: { title: '优惠券管理' } },

      { path: 'system/admin', component: AdminSystemAdmin, meta: { title: '管理员管理' } },
      { path: 'system/role', component: AdminSystemRole, meta: { title: '角色权限' } },
      { path: 'system/menu', component: AdminSystemMenu, meta: { title: '菜单管理' } }
    ]
  },
  {
    path: '/home',
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
    path: '/promotion',
    name: 'Promotion',
    component: Promotion,
    meta: { title: '促销活动' }
  },
  {
    path: '/new',
    name: 'New',
    component: New,
    meta: { title: '新品上市' }
  },
  {
    path: '/hot',
    name: 'Hot',
    component: Hot,
    meta: { title: '热销榜单' }
  },
  {
    path: '/filter',
    name: 'Filter',
    component: Filter,
    meta: { title: '水果筛选' }
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
    path: '/notices',
    name: 'Notices',
    component: Notices,
    meta: { title: '商城公告' }
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
    path: '/payment',
    name: 'Payment',
    component: Payment,
    meta: { title: '支付', requireAuth: true }
  },
  {
    path: '/payment/alipay',
    name: 'AlipayPayment',
    component: AlipayPayment,
    meta: { title: '支付宝支付', requireAuth: true }
  },
  {
    path: '/payment/success',
    name: 'PaymentSuccess',
    component: PaymentResult,
    meta: { title: '支付成功', requireAuth: true }
  },
  {
    path: '/payment/return',
    name: 'PaymentReturn',
    component: PaymentResult,
    meta: { title: '支付回调', requireAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: Chat,
    meta: { title: '消息', requireAuth: true }
  },
  {
    path: '/payment/fail',
    name: 'PaymentFail',
    component: PaymentResult,
    meta: { title: '支付失败', requireAuth: true }
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
    path: '/comment/add/:orderId',
    name: 'CommentAdd',
    component: CommentAdd,
    meta: { title: '评价订单', requireAuth: true }
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
  document.title = to.meta.title ? `${to.meta.title} - 水果商城` : '水果商城'

  // 商家端鉴权
  if (to.path.startsWith('/merchant')) {
    if (to.path === '/merchant/login') {
      return next()
    }
    const merchantToken = Cookies.get('merchantToken')
    if (!merchantToken) {
      return next({
        path: '/merchant/login',
        query: { redirect: to.fullPath }
      })
    }
    return next()
  }

  // 管理员端鉴权
  if (to.path.startsWith('/admin')) {
    if (to.path === '/admin/login') {
      return next()
    }
    const adminToken = Cookies.get('adminToken')
    if (!adminToken) {
      return next({
        path: '/admin/login',
        query: { redirect: to.fullPath }
      })
    }
    return next()
  }

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
    next({ path: '/home' })
  } else {
    next()
  }
})

export default router
