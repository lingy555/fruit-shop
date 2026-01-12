<template>
  <div class="comment-add-page">
    <div class="page-header">
      <el-button @click="goBack">返回</el-button>
      <h2>评价订单</h2>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && commentableItems.length === 0" description="该订单暂无可评价的商品">
        <el-button type="primary" @click="goOrderDetail">查看订单详情</el-button>
      </el-empty>

      <el-card v-for="item in commentableItems" :key="item.orderItemId" class="item-card">
        <div class="item-info">
          <img class="item-image" :src="item.image" :alt="item.productName" />
          <div class="item-meta">
            <div class="item-name" :title="item.productName">{{ item.productName }}</div>
            <div class="item-spec" v-if="item.specs">规格：{{ item.specs }}</div>
            <div class="item-qty">数量：× {{ item.quantity }}</div>
          </div>
        </div>

        <ReviewForm
          :order-item-id="item.orderItemId"
          :product-id="item.productId"
          @success="handleSuccess"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { order } from '@/api'
import ReviewForm from '@/components/product/ReviewForm.vue'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => Number(route.params.orderId))

const loading = ref(false)
const orderDetail = ref(null)

const commentableItems = computed(() => {
  const items = orderDetail.value?.items
  if (!Array.isArray(items)) return []
  return items.filter(i => i && i.canComment)
})

const fetchDetail = async () => {
  if (!orderId.value) return
  loading.value = true
  try {
    const resp = await order.getDetail(orderId.value)
    orderDetail.value = resp.data || null
  } catch (e) {
    console.error('获取订单详情失败:', e)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const handleSuccess = async () => {
  ElMessage.success('评价提交成功')
  await fetchDetail()
  if (commentableItems.value.length === 0) {
    router.replace(`/order/${orderId.value}`)
  }
}

const goBack = () => {
  router.back()
}

const goOrderDetail = () => {
  router.push(`/order/${orderId.value}`)
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.comment-add-page {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 18px;
  margin: 0;
}

.item-card {
  margin-bottom: 16px;
}

.item-info {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}

.item-meta {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-spec,
.item-qty {
  margin-top: 4px;
  color: #666;
  font-size: 13px;
}
</style>
