
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
        <div class="product-section" v-if="products.length > 0">
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
        </div>

        <!-- 空状态 -->
        <el-empty 
          v-else
          description="暂无商品"
          :image-size="200"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { product } from '@/api'
import ProductCard from '@/components/product/ProductCard.vue'
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
  background: radial-gradient(circle at top, #fdf8f3, #f2faf4 45%, #e9f5ff);
  padding: 50px 24px;
  display: flex;
  justify-content: center;
}

.category-layout {
  width: 100%;
  max-width: 1280px;
  display: flex;
  gap: 32px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 30px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 30px 60px rgba(13, 70, 40, 0.09);
  backdrop-filter: blur(12px);
}

.category-sidebar {
  width: 280px;
  flex-shrink: 0;
  padding-right: 20px;
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
  gap: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(248, 252, 250, 0.9);
  border: 1px solid rgba(15, 118, 110, 0.08);
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.category-item.active {
  border-color: rgba(18, 167, 138, 0.4);
  background: linear-gradient(135deg, rgba(32, 180, 126, 0.15), rgba(18, 167, 138, 0.05));
  box-shadow: 0 18px 35px rgba(15, 118, 110, 0.15);
}

.category-item:hover {
  transform: translateX(4px);
  box-shadow: 0 16px 30px rgba(15, 118, 110, 0.12);
}

.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
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
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.category-count {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.08);
  color: #0f766e;
}

.category-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.category-header {
  text-align: left;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(15, 118, 110, 0.08);
}

.category-header h2 {
  font-size: 28px;
  color: #0f172a;
  margin-bottom: 8px;
  letter-spacing: 0.4px;
}

.category-header p {
  color: #475569;
  font-size: 15px;
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

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 12px;
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
    padding: 24px 12px;
  }
  .category-layout {
    padding: 24px;
    border-radius: 24px;
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
