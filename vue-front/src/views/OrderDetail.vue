
<template>
  <div class="order-detail-container" v-loading="loading">
    <div class="order-section" v-if="orderDetail">
      <!-- 订单状态 -->
      <div class="order-header">
        <div class="order-info">
          <h2>订单详情</h2>
          <div class="order-status">{{ orderDetail.statusText }}</div>
        </div>

        <div class="order-actions">
          <el-button 
            v-for="action in getAvailableActions()" 
            :key="action.text"
            :type="action.type"
            :icon="action.icon"
            @click="handleAction(action)"
          >
            {{ action.text }}
          </el-button>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="section-card">
        <div class="card-header">
          <h3>收货信息</h3>
        </div>

        <div class="address-info">
          <div class="info-item">
            <span class="info-label">收货人：</span>
            <span class="info-value">{{ orderDetail.address.receiverName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">联系电话：</span>
            <span class="info-value">{{ orderDetail.address.receiverPhone }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">收货地址：</span>
            <span class="info-value">
              {{ orderDetail.address.province }} {{ orderDetail.address.city }} 
              {{ orderDetail.address.district }} {{ orderDetail.address.detail }}
            </span>
          </div>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="section-card">
        <div class="card-header">
          <h3>商品信息</h3>
        </div>

        <div class="product-list">
          <div 
            v-for="item in orderDetail.items" 
            :key="item.orderItemId"
            class="product-item"
          >
            <div class="product-image">
              <img :src="item.image" :alt="item.productName" />
            </div>

            <div class="product-info">
              <div class="product-name" :title="item.productName">{{ item.productName }}</div>
              <div class="product-specs" v-if="item.specs">{{ item.specs }}</div>
              <div class="product-price">¥{{ item.price }}</div>
            </div>

            <div class="product-quantity">× {{ item.quantity }}</div>

            <div class="product-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="section-card">
        <div class="card-header">
          <h3>订单信息</h3>
        </div>

        <div class="order-info-list">
          <div class="info-item">
            <span class="info-label">订单号：</span>
            <span class="info-value">{{ orderDetail.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">下单时间：</span>
            <span class="info-value">{{ formatDate(orderDetail.createTime) }}</span>
          </div>
          <div class="info-item" v-if="orderDetail.payTime">
            <span class="info-label">付款时间：</span>
            <span class="info-value">{{ formatDate(orderDetail.payTime) }}</span>
          </div>
          <div class="info-item" v-if="orderDetail.deliverTime">
            <span class="info-label">发货时间：</span>
            <span class="info-value">{{ formatDate(orderDetail.deliverTime) }}</span>
          </div>
          <div class="info-item" v-if="orderDetail.receiveTime">
            <span class="info-label">收货时间：</span>
            <span class="info-value">{{ formatDate(orderDetail.receiveTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付方式：</span>
            <span class="info-value">{{ orderDetail.paymentMethodText }}</span>
          </div>
          <div class="info-item" v-if="orderDetail.remark">
            <span class="info-label">订单备注：</span>
            <span class="info-value">{{ orderDetail.remark }}</span>
          </div>
        </div>
      </div>

      <!-- 费用信息 -->
      <div class="section-card">
        <div class="card-header">
          <h3>费用信息</h3>
        </div>

        <div class="price-list">
          <div class="price-item">
            <span class="price-label">商品总额：</span>
            <span class="price-value">¥{{ orderDetail.totalAmount.toFixed(2) }}</span>
          </div>
          <div class="price-item">
            <span class="price-label">运费：</span>
            <span class="price-value">¥{{ orderDetail.shippingFee.toFixed(2) }}</span>
          </div>
          <div class="price-item" v-if="orderDetail.couponDiscount > 0">
            <span class="price-label">优惠券：</span>
            <span class="price-value discount">-¥{{ orderDetail.couponDiscount.toFixed(2) }}</span>
          </div>
          <div class="price-item" v-if="orderDetail.pointsDiscount > 0">
            <span class="price-label">积分抵扣：</span>
            <span class="price-value discount">-¥{{ orderDetail.pointsDiscount.toFixed(2) }}</span>
          </div>
          <div class="price-divider"></div>
          <div class="price-item total">
            <span class="price-label">实付金额：</span>
            <span class="price-value">¥{{ orderDetail.actualAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 物流信息 -->
      <div class="section-card" v-if="orderDetail.logistics">
        <div class="card-header">
          <h3>物流信息</h3>
        </div>

        <div class="logistics-info">
          <div class="info-item">
            <span class="info-label">快递公司：</span>
            <span class="info-value">{{ orderDetail.logistics.expressCompany }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">快递单号：</span>
            <span class="info-value">{{ orderDetail.logistics.expressNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">当前状态：</span>
            <span class="info-value">{{ orderDetail.logistics.currentStatus }}</span>
          </div>
        </div>

        <div class="logistics-timeline">
          <el-timeline>
            <el-timeline-item
              v-for="(trace, index) in orderDetail.logistics.traces" 
              :key="index"
              :timestamp="trace.time"
            >
              {{ trace.status }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { order } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 订单ID
const orderId = computed(() => parseInt(route.params.id))

// 订单详情
const orderDetail = ref(null)

// 加载状态
const loading = ref(false)

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const response = await order.getDetail(orderId.value)
    orderDetail.value = response.data
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// 获取可用操作
const getAvailableActions = () => {
  if (!orderDetail.value) return []

  const actions = []

  switch (orderDetail.value.status) {
    case 1: // 待付款
      actions.push(
        { text: '立即付款', type: 'primary', icon: 'CreditCard', action: 'pay' },
        { text: '取消订单', type: 'danger', icon: 'Close', action: 'cancel' }
      )
      break
    case 2: // 待发货
      actions.push(
        { text: '提醒发货', type: 'primary', icon: 'Bell', action: 'remind' }
      )
      break
    case 3: // 待收货
      actions.push(
        { text: '查看物流', type: 'primary', icon: 'Van', action: 'logistics' },
        { text: '确认收货', type: 'success', icon: 'Check', action: 'confirm' }
      )
      break
    case 4: // 待评价
      actions.push(
        { text: '评价订单', type: 'primary', icon: 'ChatDotRound', action: 'comment' }
      )
      break
    case 5: // 已完成
      actions.push(
        { text: '再次购买', type: 'primary', icon: 'ShoppingCart', action: 'buy' },
        { text: '查看物流', type: 'default', icon: 'Van', action: 'logistics' }
      )
      break
    case 6: // 已取消
      actions.push(
        { text: '再次购买', type: 'primary', icon: 'ShoppingCart', action: 'buy' },
        { text: '删除订单', type: 'danger', icon: 'Delete', action: 'delete' }
      )
      break
  }

  return actions
}

// 处理订单操作
const handleAction = async (action) => {
  switch (action.action) {
    case 'pay':
      router.push({
        path: '/payment',
        query: {
          orderId: orderId.value,
          paymentMethod: orderDetail.value?.paymentMethod || 'alipay'
        }
      })
      break
    case 'cancel':
      cancelOrder()
      break
    case 'remind':
      remindDeliver()
      break
    case 'logistics':
      // 已经有物流信息，直接显示
      break
    case 'confirm':
      confirmOrder()
      break
    case 'comment':
      router.push(`/comment/add/${orderId.value}`)
      break
    case 'buy':
      // 再次购买，跳转到商品详情页
      if (orderDetail.value.items && orderDetail.value.items.length > 0) {
        router.push(`/product/${orderDetail.value.items[0].productId}`)
      }
      break
    case 'delete':
      deleteOrder()
      break
  }
}

// 取消订单
const cancelOrder = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '取消原因不能为空'
    })

    await order.cancel(orderId.value, { reason: value })
    ElMessage.success('订单已取消')

    // 刷新订单详情
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

// 提醒发货
const remindDeliver = async () => {
  try {
    await order.remindDeliver(orderId.value)
    ElMessage.success('已提醒商家发货')
  } catch (error) {
    console.error('提醒发货失败:', error)
    ElMessage.error('提醒发货失败')
  }
}

// 确认收货
const confirmOrder = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await order.confirmReceive(orderId.value)
    ElMessage.success('已确认收货')

    // 刷新订单详情
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
    }
  }
}

// 删除订单
const deleteOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '删除订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await order.delete(orderId.value)
    ElMessage.success('订单已删除')

    // 返回订单列表
    router.push('/order')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
    }
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.order-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-info h2 {
  font-size: 20px;
  color: #333;
}

.order-status {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
  padding: 5px 10px;
  background-color: rgba(76, 175, 80, 0.1);
  border-radius: 4px;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.section-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  padding: 15px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  font-size: 16px;
  color: #333;
  margin: 0;
}

.address-info, .order-info-list, .price-list, .logistics-info {
  padding: 15px;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 100px;
  color: #666;
}

.info-value {
  color: #333;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 15px;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.product-specs {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.product-price {
  font-size: 16px;
  font-weight: 500;
  color: #ff5722;
}

.product-quantity {
  margin: 0 15px;
  color: #666;
}

.product-subtotal {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-left: auto;
}

.price-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.price-item:last-child {
  margin-bottom: 0;
}

.price-label {
  color: #666;
}

.price-value {
  color: #333;
}

.price-value.discount {
  color: #4CAF50;
}

.price-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 10px 0;
}

.price-item.total {
  font-size: 16px;
  font-weight: 500;
}

.price-item.total .price-label {
  color: #333;
}

.price-item.total .price-value {
  color: #ff5722;
}

.logistics-timeline {
  padding: 0 15px 15px;
}
</style>
