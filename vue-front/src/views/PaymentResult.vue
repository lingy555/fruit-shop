<template>
  <div class="payment-result-container">
    <div class="result-card" :class="{ success: isSuccess, fail: !isSuccess }">
      <div class="result-icon">
        <div v-if="isSuccess" class="success-icon">
          <el-icon><SuccessFilled /></el-icon>
        </div>
        <div v-else class="fail-icon">
          <el-icon><CircleCloseFilled /></el-icon>
        </div>
      </div>
      
      <div class="result-title">
        {{ isSuccess ? '支付成功' : '支付失败' }}
      </div>
      
      <div class="result-message">
        {{ isSuccess ? '您的订单已成功支付，感谢您的购买！' : errorMessage }}
      </div>
      
      <div class="order-info" v-if="orderInfo">
        <div class="info-item">
          <span class="label">订单编号：</span>
          <span class="value">{{ orderInfo.orderId }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付金额：</span>
          <span class="value amount">¥{{ orderInfo.amount }}</span>
        </div>
        <div class="info-item" v-if="orderInfo.tradeNo">
          <span class="label">交易号：</span>
          <span class="value">{{ orderInfo.tradeNo }}</span>
        </div>
        <div class="info-item" v-if="orderInfo.payTime">
          <span class="label">支付时间：</span>
          <span class="value">{{ formatTime(orderInfo.payTime) }}</span>
        </div>
      </div>
      
      <div class="result-actions">
        <el-button 
          type="primary" 
          size="large" 
          @click="viewOrder"
          class="action-button primary"
        >
          查看订单详情
        </el-button>
        
        <el-button 
          size="large" 
          @click="goHome"
          class="action-button secondary"
        >
          返回首页
        </el-button>
        
        <el-button 
          v-if="isSuccess"
          size="large" 
          @click="continueShopping"
          class="action-button tertiary"
        >
          继续购物
        </el-button>
      </div>
      
      <div class="recommendations" v-if="isSuccess">
        <div class="recommend-title">为您推荐</div>
        <div class="recommend-list">
          <div 
            v-for="item in recommendations" 
            :key="item.id"
            class="recommend-item"
            @click="goToProduct(item.id)"
          >
            <img :src="item.image" :alt="item.name" class="recommend-image" />
            <div class="recommend-info">
              <div class="recommend-name">{{ item.name }}</div>
              <div class="recommend-price">¥{{ item.price }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { order } from '@/api'

const route = useRoute()
const router = useRouter()

// 响应式数据
const orderInfo = ref(null)
const errorMessage = ref('')
const recommendations = ref([])

// 计算属性
const isSuccess = computed(() => {
  return route.query.status !== 'fail' && !route.query.error
})

// 获取订单信息
const fetchOrderInfo = async () => {
  const orderId = route.query.orderId
  if (!orderId) return

  try {
    const response = await order.getDetail(orderId)
    if (response.code === 200 && response.data) {
      orderInfo.value = {
        orderId: response.data.orderId,
        amount: response.data.totalAmount,
        tradeNo: response.data.tradeNo,
        payTime: response.data.payTime,
        status: response.data.status
      }
    }
  } catch (error) {
    console.error('获取订单信息失败:', error)
  }
}

// 获取推荐商品
const fetchRecommendations = async () => {
  try {
    // 这里可以调用获取推荐商品的API
    // 暂时使用模拟数据
    recommendations.value = [
      {
        id: 1,
        name: '新鲜苹果',
        price: '12.8',
        image: '/api/uploads/apple.jpg'
      },
      {
        id: 2,
        name: '香甜橙子',
        price: '15.6',
        image: '/api/uploads/orange.jpg'
      },
      {
        id: 3,
        name: '甜美香蕉',
        price: '8.9',
        image: '/api/uploads/banana.jpg'
      },
      {
        id: 4,
        name: '多汁葡萄',
        price: '22.5',
        image: '/api/uploads/grape.jpg'
      }
    ]
  } catch (error) {
    console.error('获取推荐商品失败:', error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

// 查看订单详情
const viewOrder = () => {
  if (orderInfo.value) {
    router.push(`/order/${orderInfo.value.orderId}`)
  }
}

// 返回首页
const goHome = () => {
  router.push('/home')
}

// 继续购物
const continueShopping = () => {
  router.push('/category')
}

// 跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

// 组件挂载
onMounted(() => {
  // 设置错误信息
  if (route.query.error) {
    errorMessage.value = decodeURIComponent(route.query.error)
  }
  
  // 获取订单信息
  fetchOrderInfo()
  
  // 获取推荐商品（仅支付成功时）
  if (isSuccess.value) {
    fetchRecommendations()
  }
})
</script>

<style scoped>
.payment-result-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.result-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
  padding: 40px;
  text-align: center;
}

.result-card.success {
  border-top: 4px solid #52c41a;
}

.result-card.fail {
  border-top: 4px solid #ff4d4f;
}

.result-icon {
  margin-bottom: 24px;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: #f6ffed;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: #52c41a;
  font-size: 40px;
}

.fail-icon {
  width: 80px;
  height: 80px;
  background: #fff2f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: #ff4d4f;
  font-size: 40px;
}

.result-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.result-card.success .result-title {
  color: #52c41a;
}

.result-card.fail .result-title {
  color: #ff4d4f;
}

.result-message {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
  line-height: 1.5;
}

.order-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-size: 14px;
}

.value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.amount {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 600;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 40px;
}

.action-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
}

.action-button.primary {
  background: linear-gradient(135deg, #1677ff 0%, #1890ff 100%);
  border: none;
  color: white;
}

.action-button.secondary {
  border: 1px solid #d9d9d9;
  background: white;
  color: #666;
}

.action-button.tertiary {
  border: 1px solid #1677ff;
  background: white;
  color: #1677ff;
}

.recommendations {
  text-align: left;
}

.recommend-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.recommend-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.recommend-item {
  cursor: pointer;
  transition: transform 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e9ecef;
}

.recommend-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.recommend-image {
  width: 100%;
  height: 80px;
  object-fit: cover;
}

.recommend-info {
  padding: 8px;
}

.recommend-name {
  font-size: 12px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-price {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .payment-result-container {
    padding: 20px 10px;
  }
  
  .result-card {
    padding: 24px;
  }
  
  .result-title {
    font-size: 24px;
  }
  
  .recommend-list {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
