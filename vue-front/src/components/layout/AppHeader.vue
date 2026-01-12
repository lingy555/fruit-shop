
<template>
  <header class="app-header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/home" class="logo">
          <img src="@/assets/logo.png" alt="欢喜果铺" />
          <span>欢喜果铺</span>
        </router-link>
      </div>

      <div class="header-center">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索水果"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #append>
              <el-button icon="Search" @click="handleSearch" />
            </template>
          </el-input>
          <div class="history-search" v-if="historyKeywords.length">
            <div class="history-title">
              <span>历史搜索</span>
              <button type="button" class="history-clear" @click="clearHistory">
                <el-icon><Delete /></el-icon>
                清除
              </button>
            </div>
            <div class="history-tags">
              <span
                v-for="keyword in historyKeywords"
                :key="keyword"
                class="history-tag"
                @click="handleHistoryClick(keyword)"
              >
                {{ keyword }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="header-right">
        <router-link to="/cart" class="header-item cart-item">
          <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge">
            <el-icon size="20"><ShoppingCart /></el-icon>
          </el-badge>
          <span>购物车</span>
        </router-link>

        <template v-if="userStore.isLogin">
          <el-dropdown class="user-dropdown" @command="handleCommand">
            <div class="header-item user-item">
              <el-avatar :size="30" :src="userStore.avatar" fit="cover">
                {{ userStore.nickname.charAt(0) }}
              </el-avatar>
              <span>{{ userStore.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="user">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="order">
                  <el-icon><Document /></el-icon>
                  我的订单
                </el-dropdown-item>
                <el-dropdown-item command="chat">
                  <el-icon><ChatDotRound /></el-icon>
                  消息中心
                </el-dropdown-item>
                <el-dropdown-item command="address">
                  <el-icon><Location /></el-icon>
                  收货地址
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <router-link to="/login" class="header-item">登录</router-link>
          <router-link to="/register" class="header-item">注册</router-link>
        </template>
      </div>
    </div>

    <div class="nav-container">
      <div class="nav-left">
        <el-menu mode="horizontal" :default-active="activeIndex" router>
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/category">分类</el-menu-item>
          <el-menu-item index="/filter">水果筛选</el-menu-item>
          <el-menu-item index="/promotion">促销活动</el-menu-item>
          <el-menu-item index="/new">新品上市</el-menu-item>
          <el-menu-item index="/hot">热销榜单</el-menu-item>
        </el-menu>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 搜索关键词
const searchKeyword = ref('')
const historyKeywords = ref([])
const HISTORY_KEY = 'fruit-shop_search_history'

// 购物车数量
const cartCount = ref(0)

// 当前激活的菜单项
const activeIndex = computed(() => route.path)

// 处理搜索
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  addKeywordToHistory(searchKeyword.value.trim())

  router.push({
    path: '/search',
    query: {
      keyword: searchKeyword.value
    }
  })
}

const addKeywordToHistory = (keyword) => {
  const existsIndex = historyKeywords.value.indexOf(keyword)
  if (existsIndex !== -1) {
    historyKeywords.value.splice(existsIndex, 1)
  }
  historyKeywords.value.unshift(keyword)
  if (historyKeywords.value.length > 6) {
    historyKeywords.value.pop()
  }
  localStorage.setItem(HISTORY_KEY, JSON.stringify(historyKeywords.value))
}

const handleHistoryClick = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

const clearHistory = () => {
  historyKeywords.value = []
  localStorage.removeItem(HISTORY_KEY)
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'user':
      router.push('/user')
      break
    case 'order':
      router.push('/order')
      break
    case 'chat':
      router.push('/chat')
      break
    case 'address':
      router.push('/address')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await userStore.logout()
        ElMessage.success('退出登录成功')
        router.push('/home')
      }).catch(() => {
        // 取消退出
      })
      break
  }
}

// 初始化
onMounted(() => {
  // 这里可以获取购物车数量
  // cartCount.value = await getCartCount()
  const saved = localStorage.getItem(HISTORY_KEY)
  if (saved) {
    try {
      historyKeywords.value = JSON.parse(saved)
    } catch (error) {
      historyKeywords.value = []
    }
  }
})
</script>

<style scoped>
.app-header {
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-left {
  flex: 1;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: bold;
  color: #4CAF50;
}

.logo img {
  height: 40px;
  margin-right: 10px;
}

.header-center {
  flex: 2;
  display: flex;
  justify-content: center;
}

.search-box {
  width: 100%;
  max-width: 500px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-search {
  background: #fafafa;
  border: 1px solid #f3f3f3;
  border-radius: 16px;
  padding: 12px 14px;
}

.history-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.history-clear {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: #999;
  cursor: pointer;
  font-size: 12px;
}

.history-clear:hover {
  color: #ff7043;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid #e2e8f0;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-tag:hover {
  border-color: #22c55e;
  color: #16a34a;
}

.header-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
}

.header-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.header-item:hover {
  color: #4CAF50;
}

.cart-item {
  position: relative;
}

.cart-badge {
  margin-bottom: 5px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.user-item span {
  font-size: 14px;
}

.nav-container {
  border-top: 1px solid #f0f0f0;
}

.nav-left {
  max-width: 1200px;
  margin: 0 auto;
}

.el-menu {
  border-bottom: none;
}

.el-menu-item {
  font-size: 16px;
}

.el-menu-item.is-active {
  color: #4CAF50;
  border-bottom-color: #4CAF50;
}
</style>
