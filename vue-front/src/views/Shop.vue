
<template>
  <div class="shop-container" v-loading="loading">
    <div class="shop-header" v-if="shopInfo">
      <div class="shop-banner">
        <img :src="shopInfo.banner" :alt="shopInfo.shopName" />
      </div>

      <div class="shop-info">
        <div class="shop-logo">
          <img :src="shopInfo.logo" :alt="shopInfo.shopName" />
        </div>

        <div class="shop-details">
          <h1>{{ shopInfo.shopName }}</h1>
          <p>{{ shopInfo.description }}</p>

          <div class="shop-stats">
            <div class="stat-item">
              <span class="stat-label">店铺评分：</span>
              <el-rate 
                v-model="shopInfo.score" 
                disabled 
                show-score 
                text-color="#ff9900" 
                score-template="{value}"
              />
            </div>
            <div class="stat-item">
              <span class="stat-label">商品数量：</span>
              <span class="stat-value">{{ shopInfo.productCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">总销量：</span>
              <span class="stat-value">{{ shopInfo.totalSales }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">粉丝数量：</span>
              <span class="stat-value">{{ shopInfo.fanCount }}</span>
            </div>
          </div>

          <div class="shop-actions">
            <el-button 
              v-if="!shopInfo.isFavorite"
              type="primary"
              @click="toggleFavorite"
            >
              <el-icon><Star /></el-icon>
              关注店铺
            </el-button>
            <el-button 
              v-else
              @click="toggleFavorite"
            >
              <el-icon><StarFilled /></el-icon>
              已关注
            </el-button>

            <el-button 
              type="success"
              @click="contactShop"
            >
              <el-icon><Service /></el-icon>
              联系商家
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="shop-content">
      <div class="content-header">
        <h2>全部商品</h2>

        <div class="sort-options">
          <span class="option-label">排序：</span>
          <el-select v-model="sortBy" @change="fetchProducts" placeholder="默认排序">
            <el-option label="默认排序" value="" />
            <el-option label="价格从低到高" value="price" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="销量从高到低" value="sales" />
            <el-option label="好评优先" value="score" />
            <el-option label="最新上架" value="createTime" />
          </el-select>
        </div>
      </div>

      <div class="product-grid" v-if="products.length > 0">
        <ProductCard 
          v-for="product in products" 
          :key="product.productId" 
          :product="product"
        />
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="products.length > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[20, 40, 80, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-if="products.length === 0"
        description="暂无商品"
        :image-size="200"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { shop, product } from '@/api'
import { useUserStore } from '@/store/user'
import ProductCard from '@/components/product/ProductCard.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 店铺ID
const shopId = computed(() => parseInt(route.params.id))

// 店铺信息
const shopInfo = ref(null)

// 商品列表
const products = ref([])

// 排序方式
const sortBy = ref('')

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 加载状态
const loading = ref(false)

// 获取店铺详情
const fetchShopDetail = async () => {
  loading.value = true
  try {
    const response = await shop.getDetail(shopId.value)
    shopInfo.value = response.data
  } catch (error) {
    console.error('获取店铺详情失败:', error)
    ElMessage.error('获取店铺详情失败')
  } finally {
    loading.value = false
  }
}

// 获取店铺商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      shopId: shopId.value,
      sortBy: sortBy.value
    }

    const response = await product.getList(params)
    products.value = response.data.list || []
    pagination.total = response.data.total || 0
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  try {
    if (shopInfo.value.isFavorite) {
      // 取消收藏
      // await shop.cancelFavorite(shopId.value)
      shopInfo.value.isFavorite = false
      ElMessage.success('已取消关注')
    } else {
      // 添加收藏
      // await shop.favorite({ shopId: shopId.value })
      shopInfo.value.isFavorite = true
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('操作收藏失败:', error)
    ElMessage.error('操作收藏失败')
  }
}

// 联系商家
const contactShop = () => {
  // 这里可以打开聊天窗口或跳转到联系页面
  ElMessage.info('联系商家功能开发中')
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchProducts()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  fetchProducts()
}

// 初始化
onMounted(async () => {
  await fetchShopDetail()
  await fetchProducts()
})
</script>

<style scoped>
.shop-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.shop-header {
  margin-bottom: 30px;
}

.shop-banner {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.shop-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-info {
  display: flex;
  gap: 20px;
}

.shop-logo {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.shop-logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.shop-details {
  flex: 1;
}

.shop-details h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.shop-details p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
}

.shop-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-right: 5px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
}

.shop-actions {
  display: flex;
  gap: 15px;
}

.shop-content {
  margin-top: 30px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content-header h2 {
  font-size: 20px;
  color: #333;
}

.sort-options {
  display: flex;
  align-items: center;
}

.option-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .shop-info {
    flex-direction: column;
  }

  .shop-logo {
    width: 80px;
    height: 80px;
    margin: 0 auto 15px;
  }

  .shop-stats {
    justify-content: center;
  }

  .shop-actions {
    justify-content: center;
  }
}
</style>
