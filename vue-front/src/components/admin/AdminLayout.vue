
<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="admin-sidebar">
      <div class="logo-container">
        <img src="@/assets/logo.png" alt="欢喜果铺管理系统" class="logo" />
        <h1 class="system-name">欢喜果铺管理系统</h1>
      </div>

      <el-menu 
        :default-active="activeMenu" 
        class="admin-menu"
        @select="handleMenuSelect"
      >
        <el-sub-menu index="dashboard">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>数据概览</span>
          </template>

          <el-menu-item index="dashboard-overview">
            <el-icon><Odometer /></el-icon>
            <span>数据概览</span>
          </el-menu-item>

          <el-menu-item index="dashboard-user">
            <el-icon><User /></el-icon>
            <span>用户统计</span>
          </el-menu-item>

          <el-menu-item index="dashboard-order">
            <el-icon><Document /></el-icon>
            <span>订单统计</span>
          </el-menu-item>

          <el-menu-item index="dashboard-product">
            <el-icon><Goods /></el-icon>
            <span>商品统计</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="merchant">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>商家管理</span>
          </template>

          <el-menu-item index="merchant-list">
            <el-icon><List /></el-icon>
            <span>商家列表</span>
          </el-menu-item>

          <el-menu-item index="merchant-audit">
            <el-icon><Select /></el-icon>
            <span>商家审核</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="product">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>商品管理</span>
          </template>

          <el-menu-item index="product-list">
            <el-icon><Goods /></el-icon>
            <span>商品列表</span>
          </el-menu-item>

          <el-menu-item index="product-category">
            <el-icon><Menu /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="order">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </template>

          <el-menu-item index="order-list">
            <el-icon><List /></el-icon>
            <span>订单列表</span>
          </el-menu-item>

          <el-menu-item index="order-refund">
            <el-icon><RefreshLeft /></el-icon>
            <span>退款处理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>

          <el-menu-item index="user-list">
            <el-icon><UserFilled /></el-icon>
            <span>用户列表</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="content">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>

          <el-menu-item index="content-banner">
            <el-icon><Picture /></el-icon>
            <span>轮播图管理</span>
          </el-menu-item>

          <el-menu-item index="content-notice">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>

          <el-menu-item index="content-coupon">
            <el-icon><Ticket /></el-icon>
            <span>优惠券管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>

          <el-menu-item index="system-admin">
            <el-icon><UserFilled /></el-icon>
            <span>管理员管理</span>
          </el-menu-item>

          <el-menu-item index="system-role">
            <el-icon><Key /></el-icon>
            <span>角色权限</span>
          </el-menu-item>

          <el-menu-item index="system-menu">
            <el-icon><Menu /></el-icon>
            <span>菜单管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部导航 -->
      <div class="admin-header">
        <div class="breadcrumb-container">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentBreadcrumb }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <div class="user-avatar">
              <el-avatar :size="30" :src="userInfo.avatar" fit="cover">
                {{ userInfo.nickname ? userInfo.nickname.charAt(0) : 'A' }}
              </el-avatar>
              <span class="user-name">{{ userInfo.nickname || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
          </el-dropdown>

          <el-button type="primary" @click="logout">退出登录</el-button>
        </div>
      </div>

      <!-- 路由内容 -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { admin } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import Cookies from 'js-cookie'

const route = useRoute()
const router = useRouter()
const adminInfo = ref({})

// 当前激活的菜单
const activeMenu = computed(() => {
  // 获取当前路径，判断激活的菜单
  const path = route.path

  // 数据概览页面
  if (path.startsWith('/admin/dashboard')) {
    if (path.includes('overview')) return 'dashboard-overview'
    if (path.includes('user')) return 'dashboard-user'
    if (path.includes('order')) return 'dashboard-order'
    if (path.includes('product')) return 'dashboard-product'
    return 'dashboard-overview'
  }

  // 商家管理页面
  if (path.startsWith('/admin/merchant')) {
    if (path.includes('list')) return 'merchant-list'
    if (path.includes('audit')) return 'merchant-audit'
    return 'merchant-list'
  }

  // 商品管理页面
  if (path.startsWith('/admin/product')) {
    if (path.includes('list')) return 'product-list'
    if (path.includes('category')) return 'product-category'
    return 'product-list'
  }

  // 订单管理页面
  if (path.startsWith('/admin/order')) {
    if (path.includes('list')) return 'order-list'
    if (path.includes('refund')) return 'order-refund'
    return 'order-list'
  }

  // 用户管理页面
  if (path.startsWith('/admin/user')) {
    return 'user-list'
  }

  // 内容管理页面
  if (path.startsWith('/admin/content')) {
    if (path.includes('banner')) return 'content-banner'
    if (path.includes('notice')) return 'content-notice'
    if (path.includes('coupon')) return 'content-coupon'
    return 'content-banner'
  }

  // 系统设置页面
  if (path.startsWith('/admin/system')) {
    if (path.includes('admin')) return 'system-admin'
    if (path.includes('role')) return 'system-role'
    if (path.includes('menu')) return 'system-menu'
    return 'system-admin'
  }

  return ''
})

// 当前面包屑
const currentBreadcrumb = computed(() => {
  // 根据当前路径生成面包屑
  const path = route.path

  if (path === '/admin') return '数据概览'
  if (path === '/admin/dashboard/overview') return '数据概览'
  if (path === '/admin/dashboard/user') return '用户统计'
  if (path === '/admin/dashboard/order') return '订单统计'
  if (path === '/admin/dashboard/product') return '商品统计'

  if (path.startsWith('/admin/merchant')) {
    if (path === '/admin/merchant/list') return '商家列表'
    if (path === '/admin/merchant/audit') return '商家审核'
    return '商家管理'
  }

  if (path.startsWith('/admin/product')) {
    if (path === '/admin/product/list') return '商品列表'
    if (path === '/admin/product/category') return '分类管理'
    return '商品管理'
  }

  if (path.startsWith('/admin/order')) {
    if (path === '/admin/order/list') return '订单列表'
    if (path === '/admin/order/refund') return '退款处理'
    return '订单管理'
  }

  if (path.startsWith('/admin/user')) {
    return '用户管理'
  }

  if (path.startsWith('/admin/content')) {
    if (path === '/admin/content/banner') return '轮播图管理'
    if (path === '/admin/content/notice') return '公告管理'
    if (path === '/admin/content/coupon') return '优惠券管理'
    return '内容管理'
  }

  if (path.startsWith('/admin/system')) {
    if (path === '/admin/system/admin') return '管理员管理'
    if (path === '/admin/system/role') return '角色权限'
    if (path === '/admin/system/menu') return '菜单管理'
    return '系统设置'
  }

  return '未知页面'
})

// 用户信息
const userInfo = computed(() => adminInfo.value || {})

const handleMenuSelect = (index) => {
  const map = {
    'dashboard-overview': '/admin/dashboard/overview',
    'dashboard-user': '/admin/dashboard/user',
    'dashboard-order': '/admin/dashboard/order',
    'dashboard-product': '/admin/dashboard/product',

    'merchant-list': '/admin/merchant/list',
    'merchant-audit': '/admin/merchant/audit',

    'product-list': '/admin/product/list',
    'product-category': '/admin/product/category',

    'order-list': '/admin/order/list',
    'order-refund': '/admin/order/refund',

    'user-list': '/admin/user/list',

    'content-banner': '/admin/content/banner',
    'content-notice': '/admin/content/notice',
    'content-coupon': '/admin/content/coupon',

    'system-admin': '/admin/system/admin',
    'system-role': '/admin/system/role',
    'system-menu': '/admin/system/menu'
  }

  const path = map[index]
  if (path) {
    router.push(path)
  }
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/admin/profile')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await admin.logout()
        } catch (e) {
        }
        Cookies.remove('adminToken')
        ElMessage.success('退出登录成功')
        router.push('/admin/login')
      }).catch(() => {
        // 取消退出
      })
      break
  }
}

// 退出登录
const logout = async () => {
  try {
    try {
      await admin.logout()
    } catch (e) {
    }
    Cookies.remove('adminToken')
    ElMessage.success('退出登录成功')
    router.push('/admin/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

onMounted(async () => {
  try {
    const res = await admin.info()
    adminInfo.value = res.data || {}
  } catch (e) {
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #f0f2f5;
}

.admin-sidebar {
  width: 220px;
  background-color: #304156;
  color: #fff;
  box-shadow: 2px 0 8px 0 rgba(0, 0, 0, 0.15);
  overflow-y: auto;
  z-index: 1;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  width: 40px;
  height: 40px;
  margin-bottom: 10px;
}

.system-name {
  font-size: 18px;
  font-weight: 500;
}

.admin-menu {
  border-right: none;
}

:deep(.el-menu) {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  color: #ecf0f1;
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
}

:deep(.el-sub-menu__title) {
  color: #ecf0f1;
  height: 50px;
  line-height: 50px;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.breadcrumb-container {
  flex: 1;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  cursor: pointer;
}

.user-name {
  margin: 0 10px;
  font-size: 14px;
  color: #333;
}

.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f5f5;
}

@media (max-width: 768px) {
  .admin-layout {
    flex-direction: column;
  }

  .admin-sidebar {
    width: 100%;
    height: auto;
    position: relative;
    z-index: auto;
  }

  .admin-main {
    padding-top: 0;
  }
}
</style>
