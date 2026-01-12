<template>
  <div class="new-page">
    <div class="new-header">
      <div class="new-title">
        <span class="new-icon">✨</span>
        <span>新品上市</span>
      </div>
      <p class="new-desc">精选新品，抢先体验</p>
    </div>

    <div class="product-section" v-loading="loading">
      <div class="product-grid" v-if="products.length > 0">
        <ProductCard v-for="(p, idx) in products" :key="p.productId" :product="p" class="new-card" />
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

const baseQuery = { sortBy: 'createTime', sortOrder: 'desc' }

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
.new-page {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border-radius: 12px;
  box-shadow: 0 4px 20px 0 rgba(34, 197, 94, 0.08);
  overflow: hidden;
}

.new-header {
  background: linear-gradient(90deg, #22c55e 0%, #16a34a 100%);
  color: #fff;
  padding: 28px 24px;
  text-align: center;
}

.new-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.new-icon {
  font-size: 28px;
}

.new-desc {
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

.new-card {
  border: 2px solid #22c55e;
  border-radius: 12px;
  position: relative;
}

.new-card::before {
  content: 'NEW';
  position: absolute;
  top: 8px;
  right: 8px;
  background: #22c55e;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  z-index: 1;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
