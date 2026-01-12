<template>
  <div class="filter-page">
    <div class="filter-header">
      <div class="filter-title">
        <span class="filter-icon">🍎</span>
        <span>水果筛选</span>
      </div>
      <p class="filter-desc">精选优质水果，找到您的心仪之选</p>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <!-- 第一行：分类筛选 -->
      <div class="filter-row category-row">
        <div class="filter-label">分类：</div>
        <div class="filter-options">
          <div 
            class="filter-option"
            :class="{ active: selectedCategoryId === 0 }"
            @click="selectCategory(0)"
          >
            全部
          </div>
          <div 
            v-for="category in categories" 
            :key="category.categoryId"
            class="filter-option"
            :class="{ active: selectedCategoryId === category.categoryId }"
            @click="selectCategory(category.categoryId)"
          >
            <div class="option-icon">
              <img :src="category.icon" :alt="category.categoryName" />
            </div>
            <span>{{ category.categoryName }}</span>
            <div class="option-count">({{ category.productCount || 0 }})</div>
          </div>
        </div>
      </div>

      <!-- 第二行：排序筛选 -->
      <div class="filter-row sort-row">
        <div class="filter-label">排序：</div>
        <div class="filter-options">
          <div 
            class="filter-option"
            :class="{ active: currentSort.field === 'sales' && currentSort.order === 'desc' }"
            @click="selectSort('sales', 'desc')"
          >
            <span class="sort-icon">🔥</span>
            <span>销量从高到低</span>
          </div>
          <div 
            class="filter-option"
            :class="{ active: currentSort.field === 'createTime' && currentSort.order === 'desc' }"
            @click="selectSort('createTime', 'desc')"
          >
            <span class="sort-icon">🆕</span>
            <span>最新上架</span>
          </div>
          <div 
            class="filter-option"
            :class="{ active: currentSort.field === 'price' && currentSort.order === 'asc' }"
            @click="selectSort('price', 'asc')"
          >
            <span class="sort-icon">💰</span>
            <span>价格从低到高</span>
          </div>
          <div 
            class="filter-option"
            :class="{ active: currentSort.field === 'price' && currentSort.order === 'desc' }"
            @click="selectSort('price', 'desc')"
          >
            <span class="sort-icon">💎</span>
            <span>价格从高到低</span>
          </div>
          <div 
            class="filter-option"
            :class="{ active: currentSort.field === 'score' && currentSort.order === 'desc' }"
            @click="selectSort('score', 'desc')"
          >
            <span class="sort-icon">⭐</span>
            <span>评分从高到低</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-section" v-loading="loading">
      <div class="current-filter-info" v-if="hasActiveFilter">
        <span class="filter-info-text">
          {{ currentFilterText }}
        </span>
        <el-button 
          type="text" 
          size="small" 
          @click="clearAllFilters"
          class="clear-filter-btn"
        >
          清除筛选
        </el-button>
      </div>

      <div class="product-grid" v-if="products.length > 0">
        <ProductCard 
          v-for="product in products" 
          :key="product.productId" 
          :product="product"
          class="filter-product-card"
        />
      </div>
      <el-empty v-else description="暂无符合条件的商品" :image-size="200" />

      <div class="pagination-container" v-if="pagination.total > 0">
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { product } from '@/api'
import ProductCard from '@/components/product/ProductCard.vue'
import { usePagedList } from '@/composables/usePagedList'

// 分类列表
const categories = ref([])

// 选中的分类ID，0表示全部
const selectedCategoryId = ref(0)

// 当前排序方式
const currentSort = ref({ field: 'sales', order: 'desc' })

// 使用分页列表组合式函数
const {
  list: products,
  loading,
  pagination,
  fetch,
  onSizeChange,
  onCurrentChange,
  resetPage
} = usePagedList(
  (params) => product.getList(params),
  {
    pageSize: 20
  }
)

// 计算是否有激活的筛选条件
const hasActiveFilter = computed(() => {
  return selectedCategoryId.value !== 0 || currentSort.value.field !== 'sales' || currentSort.value.order !== 'desc'
})

// 计算当前筛选条件的描述文本
const currentFilterText = computed(() => {
  const parts = []
  
  // 分类筛选
  if (selectedCategoryId.value !== 0) {
    const category = categories.value.find(c => c.categoryId === selectedCategoryId.value)
    if (category) {
      parts.push(`分类：${category.categoryName}`)
    }
  }
  
  // 排序筛选
  const sortTexts = {
    'sales_desc': '销量从高到低',
    'createTime_desc': '最新上架',
    'price_asc': '价格从低到高',
    'price_desc': '价格从高到低',
    'score_desc': '评分从高到低'
  }
  const sortKey = `${currentSort.value.field}_${currentSort.value.order}`
  parts.push(`排序：${sortTexts[sortKey]}`)
  
  return parts.join(' | ')
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await product.getCategories()
    categories.value = response.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 选择分类
const selectCategory = async (categoryId) => {
  if (selectedCategoryId.value === categoryId) return
  selectedCategoryId.value = categoryId
  resetPage()
  await loadProducts()
}

// 选择排序
const selectSort = async (field, order) => {
  if (currentSort.value.field === field && currentSort.value.order === order) return
  currentSort.value = { field, order }
  resetPage()
  await loadProducts()
}

// 清除所有筛选条件
const clearAllFilters = async () => {
  selectedCategoryId.value = 0
  currentSort.value = { field: 'sales', order: 'desc' }
  resetPage()
  await loadProducts()
}

// 加载商品列表
const loadProducts = async () => {
  const params = {}
  
  // 添加分类筛选
  if (selectedCategoryId.value !== 0) {
    params.categoryId = selectedCategoryId.value
  }
  
  // 添加排序
  params.sortBy = currentSort.value.field
  params.sortOrder = currentSort.value.order
  
  await fetch(params)
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  const params = getQueryParams()
  return onSizeChange(size, params)
}

// 处理页码变化
const handleCurrentChange = (page) => {
  const params = getQueryParams()
  return onCurrentChange(page, params)
}

// 获取查询参数
const getQueryParams = () => {
  const params = {}
  
  if (selectedCategoryId.value !== 0) {
    params.categoryId = selectedCategoryId.value
  }
  
  params.sortBy = currentSort.value.field
  params.sortOrder = currentSort.value.order
  
  return params
}

// 初始化
onMounted(async () => {
  await fetchCategories()
  await loadProducts()
})
</script>

<style scoped>
.filter-page {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 12px;
  box-shadow: 0 4px 20px 0 rgba(71, 85, 105, 0.08);
  overflow: hidden;
}

.filter-header {
  background: linear-gradient(90deg, #10b981 0%, #059669 100%);
  color: #fff;
  padding: 28px 24px;
  text-align: center;
}

.filter-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.filter-icon {
  font-size: 28px;
}

.filter-desc {
  font-size: 15px;
  margin: 0;
  opacity: 0.9;
}

.filter-section {
  background: #fff;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  min-width: 60px;
  margin-right: 16px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  flex: 1;
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  font-size: 14px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.filter-option:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #475569;
  transform: translateY(-1px);
}

.filter-option.active {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-color: #10b981;
  color: #fff;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.option-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.option-icon img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

.option-count {
  font-size: 12px;
  opacity: 0.8;
}

.sort-icon {
  font-size: 16px;
}

.product-section {
  padding: 24px;
}

.current-filter-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
}

.filter-info-text {
  font-size: 14px;
  color: #166534;
  font-weight: 500;
}

.clear-filter-btn {
  color: #dc2626;
  font-size: 12px;
  padding: 4px 8px;
}

.clear-filter-btn:hover {
  background: #fef2f2;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
}

.filter-product-card {
  border: 2px solid transparent;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.filter-product-card:hover {
  border-color: #10b981;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(16, 185, 129, 0.15);
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .filter-label {
    min-width: auto;
    margin-right: 0;
  }
  
  .filter-options {
    width: 100%;
  }
  
  .filter-option {
    font-size: 13px;
    padding: 6px 12px;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .filter-section {
    padding: 16px;
  }
  
  .product-section {
    padding: 16px;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }
}
</style>
