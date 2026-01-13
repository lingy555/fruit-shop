
<template>
  <div class="search-container">
    <div class="search-header">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索水果"
          @keyup.enter="handleSearch"
          clearable
          class="search-input"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div class="search-options">
        <div class="sort-options">
          <span class="option-label">排序：</span>
          <el-select v-model="sortBy" @change="handleSortChange" placeholder="默认排序">
            <el-option label="默认排序" value="" />
            <el-option label="价格从低到高" value="price" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="销量从高到低" value="sales" />
            <el-option label="好评优先" value="score" />
            <el-option label="最新上架" value="createTime" />
          </el-select>
        </div>

        <div class="filter-options">
          <span class="option-label">价格区间：</span>
          <el-input-number
            v-model="priceRange[0]"
            :min="0"
            :max="priceRange[1]"
            placeholder="最低价"
            class="price-input"
            @change="handleSearch"
          />
          <span class="price-separator">-</span>
          <el-input-number
            v-model="priceRange[1]"
            :min="priceRange[0]"
            placeholder="最高价"
            class="price-input"
            @change="handleSearch"
          />
        </div>

        <div class="filter-options">
          <el-checkbox v-model="onlyStock" @change="handleSearch">
            仅显示有货
          </el-checkbox>
        </div>
      </div>
    </div>

    <!-- 搜索历史和热词 -->
    <div class="search-suggestions" v-if="!searched">
      <div class="search-history" v-if="searchHistory.length > 0">
        <div class="suggestion-header">
          <span>搜索历史</span>
          <el-button text @click="clearHistory">清空</el-button>
        </div>
        <div class="history-tags">
          <el-tag
            v-for="(keyword, index) in searchHistory"
            :key="index"
            @click="searchByKeyword(keyword)"
            class="history-tag"
          >
            {{ keyword }}
          </el-tag>
        </div>
      </div>

      <div class="hot-keywords" v-if="hotKeywords.length > 0">
        <div class="suggestion-header">热门搜索</div>
        <div class="keyword-tags">
          <el-tag
            v-for="(keyword, index) in hotKeywords"
            :key="index"
            @click="searchByKeyword(keyword)"
            class="keyword-tag"
          >
            {{ keyword }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results" v-else>
      <div class="result-header">
        <span v-if="products.length > 0">
          共找到 <span class="result-count">{{ total }}</span> 件相关商品
        </span>
        <span v-else class="no-result">
          没有找到与"{{ currentKeyword }}"相关的商品
        </span>
      </div>

      <div class="product-grid" v-if="products.length > 0 && !loading">
        <ProductCard 
          v-for="product in products" 
          :key="product.productId" 
          :product="product"
        />
      </div>

      <div class="product-grid skeleton-grid" v-else-if="loading">
        <ProductCardSkeleton 
          v-for="placeholder in skeletonPlaceholders"
          :key="placeholder"
        />
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="products.length > 0 && !loading">
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
        v-if="products.length === 0 && searched"
        description="没有找到相关商品，请尝试其他关键词"
        :image-size="200"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { product } from '@/api'
import ProductCard from '@/components/product/ProductCard.vue'
import ProductCardSkeleton from '@/components/product/ProductCardSkeleton.vue'
import { ElMessage } from 'element-plus'
import { usePagedList } from '@/composables/usePagedList'

const route = useRoute()
const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 当前搜索的关键词
const currentKeyword = ref('')

// 是否已搜索
const searched = ref(false)

// 搜索历史
const searchHistory = ref([])

// 热门搜索词
const hotKeywords = ref([])

// 排序方式
const sortBy = ref('')

// 价格区间
const priceRange = ref([0, 9999])

// 仅显示有货
const onlyStock = ref(false)

// 商品列表
const {
  list: products,
  total,
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

// 获取热门搜索词
const fetchHotKeywords = async () => {
  try {
    const response = await product.getHotKeywords()
    hotKeywords.value = response.data || []
  } catch (error) {
    console.error('获取热门搜索词失败:', error)
  }
}

// 获取搜索历史
const fetchSearchHistory = async () => {
  try {
    const response = await product.getSearchHistory()
    searchHistory.value = response.data || []
  } catch (error) {
    console.error('获取搜索历史失败:', error)
  }
}

// 清空搜索历史
const clearHistory = async () => {
  try {
    await product.clearSearchHistory()
    searchHistory.value = []
    ElMessage.success('搜索历史已清空')
  } catch (error) {
    console.error('清空搜索历史失败:', error)
  }
}

// 通过关键词搜索
const searchByKeyword = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 处理搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  // 重置分页
  resetPage()

  // 更新URL
  router.push({
    path: '/search',
    query: {
      keyword: searchKeyword.value,
      sortBy: sortBy.value,
      minPrice: priceRange.value[0],
      maxPrice: priceRange.value[1],
      onlyStock: onlyStock.value
    }
  }).catch((e) => {
    console.groupCollapsed('[Search Route Push Failed]')
    console.log('details:', e)
    console.groupEnd()
  })

  // 标记已搜索
  searched.value = true
  currentKeyword.value = searchKeyword.value

  // 获取商品列表
  fetchProducts()
}

// 处理排序变化
const handleSortChange = () => {
  if (searched.value) {
    handleSearch()
  }
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  return onSizeChange(size, buildQuery())
}

// 处理页码变化
const handleCurrentChange = (page) => {
  return onCurrentChange(page, buildQuery())
}

const buildQuery = () => {
  return {
    keyword: searchKeyword.value,
    sortBy: sortBy.value,
    minPrice: priceRange.value[0],
    maxPrice: priceRange.value[1],
    onlyStock: onlyStock.value
  }
}

// 获取商品列表
const fetchProducts = async () => {
  try {
    await fetch(buildQuery())
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

// 初始化
onMounted(async () => {
  // 从URL中获取搜索参数
  const { keyword, sortBy: sort, minPrice, maxPrice, onlyStock: onlyStockQuery, stock } = route.query

  if (keyword) {
    searchKeyword.value = keyword
    currentKeyword.value = keyword
    searched.value = true

    if (sort) sortBy.value = sort
    if (minPrice) priceRange.value[0] = Number(minPrice)
    if (maxPrice) priceRange.value[1] = Number(maxPrice)
    if (onlyStockQuery != null) {
      onlyStock.value = onlyStockQuery === 'true'
    } else if (stock != null) {
      // 兼容旧参数名
      onlyStock.value = stock === 'true'
    }

    resetPage()
    await fetchProducts()
  } else {
    await fetchHotKeywords()
    await fetchSearchHistory()
  }
})
</script>

<style scoped>
.search-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.search-header {
  margin-bottom: 20px;
}

.search-box {
  margin-bottom: 15px;
}

.search-input {
  max-width: 600px;
  margin: 0 auto;
  display: block;
}

.search-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.option-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
  white-space: nowrap;
}

.sort-options, .filter-options {
  display: flex;
  align-items: center;
}

.price-input {
  width: 120px;
}

.price-separator {
  margin: 0 10px;
  color: #999;
}

.search-suggestions {
  padding: 20px 0;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.history-tags, .keyword-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.history-tag {
  cursor: pointer;
}

.keyword-tag {
  background-color: #ff5722;
  color: #fff;
  border-color: #ff5722;
  cursor: pointer;
}

.search-results {
  padding-top: 10px;
}

.result-header {
  margin-bottom: 20px;
  font-size: 16px;
  color: #333;
}

.result-count {
  color: #ff5722;
  font-weight: 500;
}

.no-result {
  color: #999;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.skeleton-grid .product-card {
  cursor: default;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .search-options {
    flex-direction: column;
    gap: 15px;
  }

  .sort-options, .filter-options {
    width: 100%;
  }
}
</style>
