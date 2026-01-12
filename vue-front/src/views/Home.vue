
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
  min-height: calc(100vh - 80px);
  padding: 60px 24px 40px;
  background: radial-gradient(circle at top, #fdf8f3, #f2faf4 45%, #e9f5ff);
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.banner-section {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(15, 118, 110, 0.12);
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(240,252,247,0.9));
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 24px;
}

.category-section,
.product-section,
.notice-section {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 24px 45px rgba(13, 70, 40, 0.08);
  backdrop-filter: blur(12px);
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #102a43;
  margin-bottom: 18px;
  position: relative;
  padding-left: 16px;
  letter-spacing: 0.3px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 24px;
  background: linear-gradient(180deg, #21c47b, #12a78a);
  border-radius: 999px;
  box-shadow: 0 6px 18px rgba(33, 196, 123, 0.4);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  gap: 12px;
}

.section-more {
  color: #0f766e;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.08);
  transition: all 0.25s ease;
}

.section-more:hover {
  background: rgba(15, 118, 110, 0.18);
  color: #075e54;
  box-shadow: 0 10px 25px rgba(15, 118, 110, 0.15);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 24px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border-radius: 20px;
  background: linear-gradient(145deg, #fdfdfd, #f5fbff);
  border: 1px solid rgba(15, 118, 110, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.category-item::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(18, 167, 138, 0.08), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.category-item:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 18px 35px rgba(15, 118, 110, 0.15);
}

.category-item:hover::after {
  opacity: 1;
}

.category-icon {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: radial-gradient(circle, #fff, #f1fbf6);
  border: 1px solid rgba(15, 118, 110, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.category-icon img {
  width: 44px;
  height: 44px;
  object-fit: contain;
}

.category-name {
  font-size: 15px;
  font-weight: 600;
  color: #134e4a;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  border-radius: 16px;
  background: rgba(248, 252, 250, 0.85);
  border: 1px solid rgba(15, 118, 110, 0.08);
  cursor: pointer;
  transition: all 0.25s ease;
  gap: 12px;
}

.notice-item:hover {
  background: #ffffff;
  box-shadow: 0 15px 35px rgba(15, 118, 110, 0.08);
  transform: translateY(-2px);
}

.notice-title {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: #102a43;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 13px;
  color: #64748b;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.08);
}

@media (max-width: 768px) {
  .home-container {
    padding: 24px 16px 32px;
    gap: 20px;
  }

  .banner-section {
    border-radius: 20px;
  }

  .category-section,
  .product-section,
  .notice-section {
    padding: 20px;
    border-radius: 22px;
  }

  .section-title {
    font-size: 18px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .category-icon {
    width: 64px;
    height: 64px;
  }
}
</style>
