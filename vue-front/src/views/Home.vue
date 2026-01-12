
<template>
  <div class="home-container">
    <!-- 轮播图 -->
    <div class="banner-section">
      <el-carousel :interval="4000" type="card" height="300px">
        <el-carousel-item v-for="banner in banners" :key="banner.bannerId">
          <img :src="banner.image" :alt="banner.title" class="banner-img" />
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 分类导航 -->
    <div class="category-section">
      <div class="section-title">水果分类</div>
      <div class="category-grid">
        <div 
          v-for="category in categories" 
          :key="category.categoryId" 
          class="category-item"
          @click="goToCategory(category.categoryId)"
        >
          <div class="category-icon">
            <img :src="category.icon" :alt="category.categoryName" />
          </div>
          <div class="category-name">{{ category.categoryName }}</div>
        </div>
      </div>
    </div>

    <!-- 热销商品 -->
    <div class="product-section">
      <div class="section-header">
        <div class="section-title">热销商品</div>
        <div class="section-more" @click="goToProductList('hot')">查看更多</div>
      </div>
      <div class="product-grid">
        <ProductCard 
          v-for="product in hotProducts" 
          :key="product.productId" 
          :product="product"
        />
      </div>
    </div>

    <!-- 新品上市 -->
    <div class="product-section">
      <div class="section-header">
        <div class="section-title">新品上市</div>
        <div class="section-more" @click="goToProductList('new')">查看更多</div>
      </div>
      <div class="product-grid">
        <ProductCard 
          v-for="product in newProducts" 
          :key="product.productId" 
          :product="product"
        />
      </div>
    </div>

    <!-- 推荐商品 -->
    <div class="product-section">
      <div class="section-header">
        <div class="section-title">为您推荐</div>
        <div class="section-more" @click="goToProductList('recommend')">查看更多</div>
      </div>
      <div class="product-grid">
        <ProductCard 
          v-for="product in recommendProducts" 
          :key="product.productId" 
          :product="product"
        />
      </div>
    </div>

    <!-- 公告列表 -->
    <div class="notice-section">
      <div class="section-header">
        <div class="section-title">商城公告</div>
        <div class="section-more" @click="goToNotices">查看更多</div>
      </div>
      <div class="notice-list">
        <div 
          v-for="notice in notices" 
          :key="notice.noticeId" 
          class="notice-item"
          @click="viewNotice(notice)"
        >
          <div class="notice-title">{{ notice.title }}</div>
          <div class="notice-time">{{ formatDate(notice.createTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { home } from '@/api'
import ProductCard from '@/components/product/ProductCard.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 轮播图数据
const banners = ref([])

// 分类数据
const categories = ref([])

// 热销商品
const hotProducts = ref([])

// 新品上市
const newProducts = ref([])

// 推荐商品
const recommendProducts = ref([])

// 公告列表
const notices = ref([])

// 获取首页数据
const fetchHomeData = async () => {
  try {
    const response = await home.getIndexData()
    const data = response.data

    banners.value = data.banners || []
    categories.value = data.categories || []
    hotProducts.value = data.hotProducts || []
    newProducts.value = data.newProducts || []
    recommendProducts.value = data.recommendProducts || []
  } catch (error) {
    console.error('获取首页数据失败:', error)
    ElMessage.error('获取首页数据失败')
  }
}

// 获取公告列表
const fetchNotices = async () => {
  try {
    const response = await home.getNotices({ page: 1, pageSize: 5 })
    notices.value = response.data.list || []
  } catch (error) {
    console.error('获取公告列表失败:', error)
  }
}

// 跳转到分类页
const goToCategory = (categoryId) => {
  router.push({
    path: '/category',
    query: { categoryId }
  })
}

// 跳转到商品列表页
const goToProductList = (type) => {
  router.push({
    path: '/category',
    query: { type }
  })
}

// 跳转到公告页
const goToNotices = () => {
  router.push('/notices')
}

// 查看公告详情
const viewNotice = (notice) => {
  // 这里可以打开公告详情弹窗或跳转到公告详情页
  ElMessage.info(`查看公告: ${notice.title}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 初始化
onMounted(async () => {
  await fetchHomeData()
  await fetchNotices()
})
</script>

<style scoped>
.home-container {
  padding-bottom: 40px;
}

.banner-section {
  margin-bottom: 30px;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.category-section, .product-section, .notice-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  position: relative;
  padding-left: 10px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background-color: #4CAF50;
  border-radius: 2px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-more {
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.section-more:hover {
  color: #4CAF50;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 20px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s;
}

.category-item:hover {
  transform: translateY(-5px);
}

.category-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  overflow: hidden;
}

.category-icon img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.category-name {
  font-size: 14px;
  color: #333;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover {
  background-color: #f9f9f9;
}

.notice-title {
  flex: 1;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}
</style>
