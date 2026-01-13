
<template>
  <div class="category-container">
    <div class="category-layout">
      <!-- 左侧分类导航 -->
      <div class="category-sidebar">
        <div class="category-list">
          <div 
            v-for="category in categories" 
            :key="category.categoryId"
            class="category-item"
            :class="{ active: selectedCategoryId === category.categoryId }"
            @click="selectCategory(category.categoryId)"
          >
            <div class="category-icon">
              <img :src="category.icon" :alt="category.categoryName" />
            </div>
            <div class="category-name">{{ category.categoryName }}</div>
            <div class="category-count">{{ category.productCount || 0 }}件</div>
          </div>
        </div>
      </div>

      <!-- 右侧商品列表 -->
      <div class="category-content">
        <div class="category-header" v-if="selectedCategory">
          <h2>{{ selectedCategory.categoryName }}</h2>
          <p>{{ selectedCategory.description || '精选优质水果，新鲜直达' }}</p>
        </div>

        <!-- 子分类 -->
        <!-- <div class="subcategories" v-if="selectedCategory && selectedCategory.children && selectedCategory.children.length">
          <div 
            v-for="subCategory in selectedCategory.children" 
            :key="subCategory.categoryId"
            class="subcategory-item"
            @click="selectSubCategory(subCategory.categoryId)"
          >
            {{ subCategory.categoryName }}
          </div>
        </div> -->

        <!-- 商品列表 -->
        <div class="product-section">
          <div class="product-grid skeleton-grid" v-if="loading">
            <ProductCardSkeleton 
              v-for="placeholder in skeletonPlaceholders"
              :key="placeholder"
            />
          </div>

          <template v-else-if="products.length > 0">
            <div class="product-grid">
              <ProductCard 
                v-for="product in products" 
                :key="product.productId" 
                :product="product"
              />
            </div>

            <!-- 分页 -->
            <div class="pagination-container">
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
          </template>

          <!-- 空状态 -->
          <el-empty 
            v-else
            description="暂无商品"
            :image-size="200"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { product } from '@/api'
import ProductCard from '@/components/product/ProductCard.vue'
import ProductCardSkeleton from '@/components/product/ProductCardSkeleton.vue'
import { usePagedList } from '@/composables/usePagedList'

const route = useRoute()
const router = useRouter()

// 分类列表
const categories = ref([])

// 选中的分类ID
const selectedCategoryId = ref(0)

// 选中的分类对象
const selectedCategory = ref(null)

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

const skeletonPlaceholders = Array.from({ length: 8 }, (_, index) => index)

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await product.getCategories()
    categories.value = response.data || []

    // 如果URL中有分类ID，则选中该分类
    const categoryId = route.query.categoryId
    if (categoryId) {
      await applyCategoryFromRoute(parseInt(categoryId))
    } else if (categories.value.length > 0) {
      // 否则默认选中第一个分类
      const defaultCategoryId = categories.value[0].categoryId
      await router.replace({
        path: '/category',
        query: { categoryId: defaultCategoryId }
      })
      await applyCategoryFromRoute(defaultCategoryId)
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const fetchProducts = async () => {
  if (!selectedCategoryId.value) return
  await fetch({ categoryId: selectedCategoryId.value })
}

// 选择分类
const selectCategory = async (categoryId) => {
  if (!categoryId) return
  if (selectedCategoryId.value === categoryId) return
  resetPage()
  await router.push({
    path: '/category',
    query: { categoryId }
  })
}

const applyCategoryFromRoute = async (categoryId) => {
  selectedCategoryId.value = categoryId
  selectedCategory.value = categories.value.find(c => c.categoryId === categoryId) || null
  resetPage()
  await fetchProducts()
}

// 选择子分类
const selectSubCategory = (subCategoryId) => {
  router.push({
    path: '/category',
    query: { categoryId: subCategoryId }
  })
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  return onSizeChange(size, { categoryId: selectedCategoryId.value })
}

// 处理页码变化
const handleCurrentChange = (page) => {
  return onCurrentChange(page, { categoryId: selectedCategoryId.value })
}

// 监听路由变化
watch(
  () => route.query.categoryId,
  (newCategoryId) => {
    if (newCategoryId) {
      const id = parseInt(newCategoryId)
      if (id !== selectedCategoryId.value) {
        applyCategoryFromRoute(id)
      }
    }
  }
)

// 初始化
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-container {
  min-height: calc(100vh - 80px);
  background: radial-gradient(circle at top, #fdf8f3, var(--color-page) 45%, #e4f3ff);
  padding: var(--space-xxl) var(--space-lg);
  display: flex;
  justify-content: center;
}

.category-layout {
  width: 100%;
  max-width: 1280px;
  display: flex;
  gap: var(--space-xl);
  background: var(--color-glass);
  border-radius: var(--radius-xl);
  padding: var(--space-xl);
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  backdrop-filter: blur(var(--glass-blur));
}

.category-sidebar {
  width: 280px;
  flex-shrink: 0;
  padding-right: var(--space-md);
  border-right: 1px solid rgba(15, 118, 110, 0.08);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 14px 16px;
  border-radius: var(--radius-md);
  background: rgba(248, 252, 250, 0.9);
  border: 1px solid rgba(15, 118, 110, 0.08);
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.category-item.active {
  border-color: rgba(18, 167, 138, 0.4);
  background: var(--gradient-mint);
  box-shadow: var(--shadow-soft);
}

.category-item:hover {
  transform: translateX(4px);
  box-shadow: 0 16px 30px rgba(15, 118, 110, 0.12);
}

.category-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 118, 110, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-icon img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.category-name {
  flex: 1;
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-neutral-900);
}

.category-count {
  font-size: var(--font-size-sm);
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.08);
  color: var(--color-primary);
}

.category-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.category-header {
  text-align: left;
  margin-bottom: var(--space-md);
  padding-bottom: var(--space-sm);
  border-bottom: 1px solid rgba(15, 118, 110, 0.08);
}

.category-header h2 {
  font-size: var(--font-size-2xl);
  color: var(--color-neutral-900);
  margin-bottom: 8px;
  letter-spacing: 0.4px;
}

.category-header p {
  color: var(--color-neutral-500);
  font-size: var(--font-size-base);
}

.product-section {
  min-height: 400px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.skeleton-grid .product-card {
  cursor: default;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: var(--space-sm);
}

@media (max-width: 1024px) {
  .category-layout {
    flex-direction: column;
  }
  .category-sidebar {
    width: 100%;
    border-right: none;
    padding-right: 0;
    border-bottom: 1px solid rgba(15, 118, 110, 0.08);
    padding-bottom: 20px;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .category-container {
    padding: var(--space-xl) var(--space-md);
  }
  .category-layout {
    padding: var(--space-lg);
    border-radius: var(--radius-lg);
  }
  .category-list {
    flex-direction: row;
    overflow-x: auto;
    padding-bottom: 12px;
  }
  .category-item {
    min-width: 160px;
    flex-direction: column;
    align-items: flex-start;
  }
  .category-icon {
    width: 56px;
    height: 56px;
  }
}
</style>
