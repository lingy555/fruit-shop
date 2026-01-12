<template>
  <div class="merchant-layout">
    <div class="merchant-sidebar">
      <div class="logo-container">
        <img src="@/assets/logo.png" alt="欢喜果铺商家端" class="logo" />
        <h1 class="system-name">欢喜果铺商家端</h1>
      </div>

      <el-menu :default-active="activeMenu" class="merchant-menu" @select="handleMenuSelect">
        <el-menu-item index="product">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="order">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>客服消息</span>
        </el-menu-item>
        <el-menu-item index="shop">
          <el-icon><Shop /></el-icon>
          <span>店铺管理</span>
        </el-menu-item>
        <el-menu-item index="statistics">
          <el-icon><Odometer /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="merchant-main">
      <div class="merchant-header">
        <div class="breadcrumb-container">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/merchant' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentBreadcrumb }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="user-info">
          <el-button type="primary" @click="logout">退出登录</el-button>
        </div>
      </div>

      <div class="merchant-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'
import { merchantAuth } from '@/api'

import { Goods, Document, Shop, Odometer, ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/merchant/product')) return 'product'
  if (p.startsWith('/merchant/order')) return 'order'
  if (p.startsWith('/merchant/chat')) return 'chat'
  if (p.startsWith('/merchant/shop')) return 'shop'
  if (p.startsWith('/merchant/statistics')) return 'statistics'
  return 'product'
})

const currentBreadcrumb = computed(() => {
  const map = {
    product: '商品管理',
    order: '订单管理',
    chat: '客服消息',
    shop: '店铺管理',
    statistics: '数据统计'
  }
  return map[activeMenu.value] || '商家端'
})

const handleMenuSelect = (key) => {
  const map = {
    product: '/merchant/product',
    order: '/merchant/order',
    chat: '/merchant/chat',
    shop: '/merchant/shop',
    statistics: '/merchant/statistics'
  }
  router.push(map[key]).catch(() => {})
}

const logout = async () => {
  try {
    await merchantAuth.logout()
  } catch (e) {
    // ignore
  }
  Cookies.remove('merchantToken')
  ElMessage.success('已退出登录')
  router.replace('/merchant/login')
}
</script>

<style scoped>
.merchant-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.merchant-sidebar {
  width: 220px;
  background: #1f2d3d;
  color: #fff;
  flex-shrink: 0;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo {
  width: 32px;
  height: 32px;
}

.system-name {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
}

.merchant-menu {
  border-right: none;
  background: transparent;
}

/* Ensure menu text is visible */
.merchant-menu .el-menu-item {
  color: #bfcbd9 !important;
  background: transparent !important;
}

.merchant-menu .el-menu-item:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

.merchant-menu .el-menu-item.is-active {
  color: #409eff !important;
  background: rgba(64, 158, 255, 0.1) !important;
}

.merchant-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.merchant-header {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #eee;
}

.merchant-content {
  padding: 16px;
}
</style>
