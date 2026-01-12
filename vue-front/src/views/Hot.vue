<template>
  <div class="hot-page">
    <div class="hot-header">
      <div class="hot-title">
        <span class="hot-icon">🔥</span>
        <span>热销榜单</span>
      </div>
      <p class="hot-desc">人气爆款，销量领先</p>
    </div>

    <div class="product-section" v-loading="loading">
      <div class="product-grid" v-if="products.length > 0">
        <div 
          v-for="(p, idx) in products" 
          :key="p.productId" 
          class="hot-item"
        >
          <div class="hot-rank">{{ idx + 1 }}</div>
          <ProductCard :product="p" class="hot-card" />
        </div>
      </div>
      <el-empty v-else description="暂无商品" :image-size="200" />

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
import { onMounted } from 'vue'
import { product } from '@/api'
import ProductCard from '@/components/product/ProductCard.vue'
import { usePagedList } from '@/composables/usePagedList'

const {
  list: products,
  loading,
  pagination,
  fetch,
  onSizeChange,
  onCurrentChange
} = usePagedList((params) => product.getList(params), { pageSize: 20 })

const baseQuery = { sortBy: 'sales', sortOrder: 'desc' }

const load = () => {
  return fetch(baseQuery)
}

const handleSizeChange = (size) => {
  return onSizeChange(size, baseQuery)
}

const handleCurrentChange = (page) => {
  return onCurrentChange(page, baseQuery)
}

onMounted(load)
</script>

<style scoped>
.hot-page {
  background: linear-gradient(135deg, #fef7ed 0%, #fed7aa 100%);
  border-radius: 12px;
  box-shadow: 0 4px 20px 0 rgba(251, 146, 60, 0.12);
  overflow: hidden;
}

.hot-header {
  background: linear-gradient(90deg, #fb923c 0%, #f97316 100%);
  color: #fff;
  padding: 28px 24px;
  text-align: center;
}

.hot-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.hot-icon {
  font-size: 28px;
}

.hot-desc {
  font-size: 15px;
  margin: 0;
  opacity: 0.9;
}

.product-section {
  padding: 24px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
}

.hot-item {
  position: relative;
}

.hot-rank {
  position: absolute;
  top: -8px;
  left: -8px;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #f97316, #dc2626);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.3);
}

.hot-card {
  border: 2px solid #fb923c;
  border-radius: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.hot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(251, 146, 60, 0.2);
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
