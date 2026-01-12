
<template>
  <div class="product-detail-container" v-loading="loading">
    <div class="product-layout" v-if="productDetail">
      <!-- 左侧图片区 -->
      <div class="product-images">
        <div class="main-image">
          <img :src="currentImage" :alt="productDetail.productName" />
        </div>
        <div class="thumbnail-list">
          <div 
            v-for="(image, index) in (productDetail.images || [])" 
            :key="index"
            class="thumbnail-item"
            :class="{ active: currentImageIndex === index }"
            @click="selectImage(index)"
          >
            <img :src="image" :alt="`${productDetail.productName} - 图片${index + 1}`" />
          </div>
        </div>
      </div>

      <!-- 中间信息区 -->
      <div class="product-info">
        <h1 class="product-name">{{ productDetail.productName }}</h1>

        <div class="product-price">
          <span class="current-price">¥{{ currentSku.price || productDetail.price }}</span>
          <span 
            v-if="productDetail.originalPrice && (currentSku.price || productDetail.price) < productDetail.originalPrice" 
            class="original-price"
          >
            ¥{{ productDetail.originalPrice }}
          </span>
        </div>

        <div class="product-tags" v-if="productDetail.tags && productDetail.tags.length">
          <el-tag 
            v-for="(tag, index) in productDetail.tags" 
            :key="index" 
            type="success"
            effect="light"
          >
            {{ tag }}
          </el-tag>
        </div>

        <div class="product-meta">
          <div class="meta-item" v-if="productDetail.weight">
            <span class="meta-label">重量：</span>
            <span class="meta-value">{{ productDetail.weight }}</span>
          </div>
          <div class="meta-item" v-if="productDetail.unit">
            <span class="meta-label">单位：</span>
            <span class="meta-value">{{ productDetail.unit }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">销量：</span>
            <span class="meta-value">{{ productDetail.sales }}件</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">库存：</span>
            <span class="meta-value">{{ currentSku.stock || productDetail.stock }}件</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">评分：</span>
            <el-rate 
              v-model="productDetail.score" 
              disabled 
              show-score 
              text-color="#ff9900" 
              score-template="{value}"
            />
          </div>
        </div>

        <!-- 规格选择 -->
        <div class="spec-section" v-if="productDetail.specifications && productDetail.specifications.length">
          <div 
            v-for="spec in productDetail.specifications" 
            :key="spec.specId"
            class="spec-group"
            v-show="spec.specName !== '重量'"
          >
            <div class="spec-name">{{ spec.specName }}</div>
            <div class="spec-options">
              <el-radio-group 
                v-model="selectedSpecs[spec.specName]" 
                @change="selectSku"
              >
                <el-radio-button 
                  v-for="option in spec.options" 
                  :key="option"
                  :label="option"
                >
                  {{ option }}
                </el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </div>

        <!-- 数量选择 -->
        <div class="quantity-section">
          <div class="quantity-label">数量：</div>
          <el-input-number 
            v-model="quantity" 
            :min="1" 
            :max="Math.max(1, currentSku.stock || productDetail.stock || 1)"
            :disabled="(currentSku.stock || productDetail.stock || 0) <= 0"
            class="quantity-input"
          />
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button 
            type="primary" 
            size="large"
            :disabled="(currentSku.stock || productDetail.stock) <= 0"
            @click="buyNow"
          >
            立即购买
          </el-button>
          <el-button 
            size="large"
            :disabled="(currentSku.stock || productDetail.stock) <= 0"
            @click="addToCart"
          >
            加入购物车
          </el-button>
          <el-button 
            type="success"
            size="large"
            @click="contactMerchant"
          >
            联系商家
          </el-button>
          <el-button 
            :icon="productDetail.isCollect ? 'StarFilled' : 'Star'"
            size="large"
            @click="toggleCollect"
          >
            {{ productDetail.isCollect ? '已收藏' : '收藏' }}
          </el-button>
        </div>

        <!-- 配送信息 -->
        <div class="delivery-info">
          <div class="info-item">
            <el-icon><Van /></el-icon>
            <span>{{ deliveryInfo.deliveryTime || '预计明日送达' }}</span>
          </div>
          <div class="info-item">
            <el-icon><Service /></el-icon>
            <span>{{ deliveryInfo.freeShipping ? '免运费' : `运费¥${deliveryInfo.shippingFee}` }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧店铺信息 -->
      <div class="shop-info">
        <div class="shop-header">
          <img :src="productDetail.shopLogo" :alt="productDetail.shopName" class="shop-logo" />
          <div class="shop-name">{{ productDetail.shopName }}</div>
          <el-button 
            size="small" 
            type="primary" 
            plain
            @click="goToShop"
          >
            进店逛逛                  
          </el-button>
        </div>

        <div class="shop-stats">
          <div class="stat-item">
            <span class="stat-label">店铺评分：</span>
            <el-rate 
              v-model="productDetail.shopScore" 
              disabled 
              show-score 
              text-color="#ff9900" 
              score-template="{value}"
            />
          </div>
          <div class="stat-item">
            <span class="stat-label">商品数量：</span>
            <span class="stat-value">{{ productDetail.productCount }}件</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">粉丝数量：</span>
            <span class="stat-value">{{ productDetail.fanCount }}人</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品详情 -->
    <div class="product-detail-tabs" v-if="productDetail">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="商品详情" name="detail">
          <div class="detail-content" v-html="productDetail.detail || ''"></div>
        </el-tab-pane>

        <el-tab-pane label="商品评价" name="comments">
          <CommentList :product-id="productId" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { product, cart, shop } from '@/api'
import { useUserStore } from '@/store/user'
import CommentList from '@/components/product/CommentList.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 商品ID
const productId = computed(() => parseInt(route.params.id))

// 商品详情
const productDetail = ref(null)

const deliveryInfo = computed(() => {
  return productDetail.value?.deliveryInfo || { freeShipping: true, shippingFee: 0, deliveryTime: '' }
})

// 当前显示的图片索引
const currentImageIndex = ref(0)

// 当前显示的图片
const currentImage = computed(() => {
  if (!productDetail.value || !productDetail.value.images || productDetail.value.images.length === 0) {
    return ''
  }
  return productDetail.value.images[currentImageIndex.value]
})

// 选中的规格
const selectedSpecs = reactive({})

// 当前SKU
const currentSku = computed(() => {
  if (!productDetail.value || !productDetail.value.skuList || productDetail.value.skuList.length === 0) {
    return {}
  }

  // 根据选中的规格查找对应的SKU
  const specs = Object.values(selectedSpecs)
  if (specs.length === 0) {
    return productDetail.value.skuList[0] || {}
  }

  return productDetail.value.skuList.find(sku => {
    return Object.entries(sku.specs).every(([key, value]) => selectedSpecs[key] === value)
  }) || {}
})

// 购买数量
const quantity = ref(1)

// 当前激活的标签页
const activeTab = ref('detail')

// 加载状态
const loading = ref(false)

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    const response = await product.getDetail(productId.value)
    const d = response.data || {}
    d.images = Array.isArray(d.images) && d.images.length > 0 ? d.images : (d.image ? [d.image] : [])
    d.tags = Array.isArray(d.tags) ? d.tags : []
    d.specifications = Array.isArray(d.specifications) ? d.specifications : []
    d.skuList = Array.isArray(d.skuList) ? d.skuList : []
    d.deliveryInfo = d.deliveryInfo || { freeShipping: true, shippingFee: 0, deliveryTime: '' }
    d.detail = d.detail || ''
    productDetail.value = d

    // 默认选中第一个图片
    if (productDetail.value.images && productDetail.value.images.length > 0) {
      currentImageIndex.value = 0
    }

    // 默认选中第一个规格选项
    productDetail.value.specifications.forEach(spec => {
      selectedSpecs[spec.specName] = spec.options?.[0]
    })
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

const getSelectedSkuId = () => {
  const skuId = currentSku.value?.skuId || productDetail.value?.skuList?.[0]?.skuId
  return skuId ? Number(skuId) : null
}

// 选择图片
const selectImage = (index) => {
  currentImageIndex.value = index
}

// 选择SKU
const selectSku = () => {
  // 选择SKU后可以做一些处理，比如更新价格、库存等
}

// 立即购买
const buyNow = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  try {
    const skuId = getSelectedSkuId()
    if (!skuId) {
      ElMessage.error('该商品暂无可购买的SKU')
      return
    }
    // 添加到购物车并跳转到结算页
    await cart.add({
      productId: productId.value,
      skuId,
      quantity: quantity.value
    })

    router.push('/checkout')
  } catch (error) {
    console.error('购买失败:', error)
  }
}

// 加入购物车
const addToCart = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  try {
    const skuId = getSelectedSkuId()
    if (!skuId) {
      ElMessage.error('该商品暂无可购买的SKU')
      return
    }
    await cart.add({
      productId: productId.value,
      skuId,
      quantity: quantity.value
    })

    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('加入购物车失败:', error)
  }
}

// 切换收藏状态
const toggleCollect = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  try {
    if (productDetail.value.isCollect) {
      // 取消收藏
      // await shop.cancelFavorite(productDetail.value.shopId)
      productDetail.value.isCollect = false
      ElMessage.success('已取消收藏')
    } else {
      // 添加收藏
      // await shop.favorite({ shopId: productDetail.value.shopId })
      productDetail.value.isCollect = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('操作收藏失败:', error)
  }
}

// 进入店铺
const goToShop = () => {
  router.push(`/shop/${productDetail.value.shopId}`)
}

// 联系商家
const contactMerchant = () => {
  // 检查用户是否登录
  const userStore = useUserStore()
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 跳转到聊天页面
  router.push('/chat')
  
  // 延迟创建聊天会话，等待组件加载完成
  setTimeout(() => {
    // 这里可以通过路由参数或者全局状态传递商家信息
    // 暂时使用localStorage传递
    localStorage.setItem('pendingChat', JSON.stringify({
      merchantId: productDetail.value.merchantId,
      shopId: productDetail.value.shopId,
      shopName: productDetail.value.shopName
    }))
  }, 100)
}

// 初始化
onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.product-layout {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.product-images {
  width: 400px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  height: 400px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.6;
  transition: all 0.3s;
}

.thumbnail-item.active {
  border-color: #4CAF50;
  opacity: 1;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin-bottom: 15px;
}

.product-price {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.current-price {
  font-size: 24px;
  font-weight: bold;
  color: #ff5722;
}

.original-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
}

.product-tags {
  margin-bottom: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  color: #666;
  margin-right: 5px;
}

.spec-section {
  margin-bottom: 20px;
}

.spec-group {
  margin-bottom: 15px;
}

.spec-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quantity-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.quantity-label {
  margin-right: 10px;
  font-size: 14px;
  color: #333;
}

.quantity-input {
  width: 120px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.delivery-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.info-item .el-icon {
  margin-right: 5px;
}

.shop-info {
  width: 240px;
  flex-shrink: 0;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 15px;
}

.shop-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  text-align: center;
}

.shop-logo {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-bottom: 10px;
}

.shop-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
}

.shop-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.stat-label {
  color: #666;
  margin-right: 5px;
}

.product-detail-tabs {
  margin-top: 20px;
}

.detail-content {
  padding: 20px 0;
  line-height: 1.6;
}

:deep(.detail-content img) {
  max-width: 100%;
  height: auto;
}

@media (max-width: 1200px) {
  .product-layout {
    flex-wrap: wrap;
  }

  .product-images {
    width: 100%;
  }

  .shop-info {
    width: 100%;
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .product-layout {
    gap: 20px;
  }

  .main-image {
    height: 300px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
