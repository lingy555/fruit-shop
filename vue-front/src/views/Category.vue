
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
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.category-layout {
  display: flex;
  gap: 30px;
}

.category-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.category-list {
  border-right: 1px solid #f0f0f0;
  padding-right: 20px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 1px solid #f5f5f5;
}

.category-item:last-child {
  border-bottom: none;
}

.category-item.active {
  color: #4CAF50;
}

.category-item:hover {
  background-color: #f9f9f9;
}

.category-icon {
  width: 40px;
  height: 40px;
  margin-right: 12px;
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
  font-size: 16px;
}

.category-count {
  font-size: 12px;
  color: #999;
}

.category-content {
  flex: 1;
}

.category-header {
  margin-bottom: 20px;
  text-align: center;
}

.category-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.category-header p {
  color: #666;
  font-size: 14px;
}

.subcategories {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.subcategory-item {
  padding: 8px 15px;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.subcategory-item:hover {
  border-color: #4CAF50;
  color: #4CAF50;
}

.product-section {
  min-height: 400px;
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
  .category-layout {
    flex-direction: column;
  }

  .category-sidebar {
    width: 100%;
    margin-bottom: 20px;
  }

  .category-list {
    display: flex;
    overflow-x: auto;
    padding-bottom: 10px;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }

  .category-item {
    flex-shrink: 0;
    width: 120px;
    flex-direction: column;
    text-align: center;
    padding: 10px;
    margin-right: 10px;
    border-bottom: none;
    border-right: 1px solid #f5f5f5;
  }

  .category-icon {
    margin-right: 0;
    margin-bottom: 5px;
  }
}
</style>
