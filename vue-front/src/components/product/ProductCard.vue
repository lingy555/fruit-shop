
<template>
  <div class="product-card" @click="goToDetail">
    <div class="product-image">
      <picture>
        <source v-if="product.imageWebp" :srcset="product.imageWebp" type="image/webp" />
        <img v-lazy="product.image || product.imageWebp" :alt="product.productName" />
      </picture>
      <div class="product-tags" v-if="product.tags && product.tags.length">
        <span 
          v-for="(tag, index) in product.tags.slice(0, 2)" 
          :key="index" 
          class="product-tag"
        >
          {{ tag }}
        </span>
      </div>
    </div>

    <div class="product-info">
      <div class="product-name" :title="product.productName">{{ product.productName }}</div>
      <div class="product-shop">{{ product.shopName }}</div>
      <div class="product-price">
        <span class="current-price">¥{{ product.price }}</span>
        <span 
          v-if="product.originalPrice && product.originalPrice > product.price" 
          class="original-price"
        >
          ¥{{ product.originalPrice }}
        </span>
      </div>
      <div class="product-stats">
        <span class="product-sales">已售{{ formatSales(product.sales) }}</span>
        <div class="product-rating">
          <el-rate 
            v-model="product.score" 
            disabled 
            show-score 
            text-color="#ff9900" 
            score-template="{value}"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 跳转到商品详情页
const goToDetail = () => {
  router.push(`/product/${props.product.productId}`)
}

// 格式化销量
const formatSales = (sales) => {
  if (sales >= 10000) {
    return `${(sales / 10000).toFixed(1)}万`
  }
  return sales
}
</script>

<style scoped>
.product-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.15);
}

.product-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.product-image picture,
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
  display: block;
}

.product-image img {
  opacity: 0;
  transition: opacity 0.3s ease, transform 0.3s;
}

.product-image img.is-loaded {
  opacity: 1;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.product-tag {
  background-color: #ff5722;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.product-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  line-height: 1.4;
  height: 2.8em;
}

.product-shop {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.product-price {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff5722;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.product-sales {
  font-size: 12px;
  color: #999;
}

.product-rating {
  display: flex;
  align-items: center;
}

:deep(.el-rate) {
  height: auto;
}

:deep(.el-rate__text) {
  font-size: 12px;
  margin-left: 4px;
}
</style>
