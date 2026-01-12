<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const userStore = useUserStore()
const route = useRoute()

const isBackOffice = () => {
  return route.path.startsWith('/admin') || route.path.startsWith('/merchant')
}

// 页面加载时，如果有token则获取用户信息
onMounted(async () => {
  if (userStore.token) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      userStore.resetState()
    }
  }
})
</script>

<template>
  <div class="app-container">
    <AppHeader v-if="!isBackOffice()" />

    <main :class="isBackOffice() ? 'admin-main-content' : 'main-content'">
      <router-view />
    </main>

    <AppFooter v-if="!isBackOffice()" />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  background-color: #f5f5f5;
}

a {
  text-decoration: none;
  color: inherit;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.admin-main-content {
  flex: 1;
  width: 100%;
  margin: 0;
  padding: 0;
}
</style>
