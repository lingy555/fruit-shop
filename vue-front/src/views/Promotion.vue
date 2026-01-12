<template>
  <div class="promotion-page">
    <div class="promotion-banner">
      <div class="banner-content">
        <h2 class="banner-title">限时促销</h2>
        <p class="banner-subtitle">超值优惠，不容错过！</p>
      </div>
    </div>

    <div class="product-section" v-loading="loading">
      <div class="product-grid" v-if="products.length > 0">
        <ProductCard v-for="p in products" :key="p.productId" :product="p" />
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

const baseQuery = { sortBy: 'price', sortOrder: 'asc' }

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
.promotion-page {
  background: linear-gradient(135deg, #fff5f5 0%, #ffebe8 100%);
  border-radius: 12px;
  box-shadow: 0 4px 20px 0 rgba(255, 107, 53, 0.1);
  overflow: hidden;
}

.promotion-banner {
  background: linear-gradient(90deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
  padding: 32px 24px;
  text-align: center;
}

.banner-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.15);
}

.banner-subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.product-section {
  padding: 24px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
