
<template>
  <div class="order-container">
    <div class="order-header">
      <h2>我的订单</h2>

      <!-- 订单状态筛选 -->
      <el-radio-group v-model="orderStatus" @change="fetchOrders" class="status-tabs">
        <el-radio-button value="0">全部</el-radio-button>
        <el-radio-button value="1">待付款</el-radio-button>
        <el-radio-button value="2">待发货</el-radio-button>
        <el-radio-button value="3">待收货</el-radio-button>
        <el-radio-button value="4">待评价</el-radio-button>
        <el-radio-button value="5">已完成</el-radio-button>
        <el-radio-button value="6">已取消</el-radio-button>
        <el-radio-button value="7">退款中</el-radio-button>
      </el-radio-group>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索订单号或商品名称"
          @keyup.enter="fetchOrders"
          clearable
          class="search-input"
        >
          <template #append>
            <el-button icon="Search" @click="fetchOrders">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 订单统计 -->
    <div class="order-stats" v-if="statusCount">
      <div class="stat-item">
        <span class="stat-label">待付款：</span>
        <span class="stat-value">{{ statusCount.waitPay || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">待发货：</span>
        <span class="stat-value">{{ statusCount.waitDeliver || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">待收货：</span>
        <span class="stat-value">{{ statusCount.waitReceive || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">待评价：</span>
        <span class="stat-value">{{ statusCount.waitComment || 0 }}</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading">
      <div 
        v-for="order in orders" 
        :key="order.orderId"
        class="order-item"
      >
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatDate(order.createTime) }}</span>
          </div>
          <div class="order-status">{{ order.statusText }}</div>
        </div>

        <div class="order-shop">
          <span class="shop-name">{{ order.shopName }}</span>
          <el-button 
            text 
            type="primary"
            @click="contactShop(order)"
          >
            联系商家
          </el-button>
        </div>

        <div class="order-items">
          <div 
            v-for="item in order.items" 
            :key="item.orderItemId"
            class="order-product"
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

        <div class="order-footer">
          <div class="order-total">
            <span>共{{ getTotalQuantity(order.items) }}件商品</span>
            <span>合计：¥{{ order.actualAmount.toFixed(2) }}</span>
          </div>

          <div class="order-actions">
            <el-button 
              v-for="action in getAvailableActions(order)" 
              :key="action.text"
              :type="action.type"
              :icon="action.icon"
              @click="handleAction(action, order)"
            >
              {{ action.text }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="orders.length > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-if="orders.length === 0 && !loading"
        description="暂无订单"
        :image-size="200"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { order as orderApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usePagedList } from '@/composables/usePagedList'
import { useUserStore } from '@/store/user'

const router = useRouter()

// 订单状态
const orderStatus = ref('0')

// 搜索关键词
const keyword = ref('')

// 订单状态统计
const statusCount = ref(null)

const {
  list: orders,
  loading,
  pagination,
  fetch,
  onSizeChange,
  onCurrentChange,
  resetPage
} = usePagedList(
  (params) => orderApi.getList(params),
  {
    pageSize: 10,
    onSuccess: (res) => {
      statusCount.value = res?.data?.statusCount || null
    }
  }
)

// 获取订单列表
const fetchOrders = async () => {
  try {
    const params = {
      status: orderStatus.value === '0' ? undefined : parseInt(orderStatus.value),
      keyword: keyword.value.trim() || undefined
    }

    resetPage()
    await fetch(params)
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  }
}

const handleSizeChange = (size) => {
  return onSizeChange(size)
}

const handleCurrentChange = (page) => {
  return onCurrentChange(page)
}

// 计算商品总数量
const getTotalQuantity = (items) => {
  return items.reduce((sum, item) => sum + item.quantity, 0)
}

// 获取可用操作
const getAvailableActions = (orderItem) => {
  const actions = []

  switch (orderItem.status) {
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
const handleAction = (action, orderItem) => {
  switch (action.action) {
    case 'pay':
      router.push({
        path: '/payment',
        query: {
          orderId: orderItem.orderId,
          paymentMethod: orderItem.paymentMethod || 'alipay'
        }
      })
      break
    case 'cancel':
      cancelOrder(orderItem)
      break
    case 'remind':
      remindDeliver(orderItem)
      break
    case 'logistics':
      router.push(`/order/${orderItem.orderId}`)
      break
    case 'confirm':
      confirmOrder(orderItem)
      break
    case 'comment':
      router.push(`/comment/add/${orderItem.orderId}`)
      break
    case 'buy':
      // 再次购买，跳转到商品详情页
      if (orderItem.items && orderItem.items.length > 0) {
        router.push(`/product/${orderItem.items[0].productId}`)
      }
      break
    case 'delete':
      deleteOrder(orderItem)
      break
  }
}

// 取消订单
const cancelOrder = async (orderItem) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '取消原因不能为空'
    })

    await orderApi.cancel(orderItem.orderId, { reason: value })
    ElMessage.success('订单已取消')

    // 刷新订单列表
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

// 提醒发货
const remindDeliver = async (orderItem) => {
  try {
    await orderApi.remindDeliver(orderItem.orderId)
    ElMessage.success('已提醒商家发货')
  } catch (error) {
    console.error('提醒发货失败:', error)
    ElMessage.error('提醒发货失败')
  }
}

// 确认收货
const confirmOrder = async (orderItem) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await orderApi.confirmReceive(orderItem.orderId)
    ElMessage.success('已确认收货')

    // 刷新订单列表
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }
}

// 删除订单
const deleteOrder = async (orderItem) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '删除订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await orderApi.delete(orderItem.orderId)
    ElMessage.success('订单已删除')

    // 刷新订单列表
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败')
    }
  }
}

// 联系商家
const contactShop = (order) => {
  // 添加调试信息
  console.log('联系商家 - 订单数据:', order)
  console.log('merchantId:', order.merchantId)
  console.log('shopId:', order.shopId)
  console.log('shopName:', order.shopName)
  
  // 检查用户是否登录
  const userStore = useUserStore()
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 检查必要的商家信息
  const merchantId = order.merchantId || order.shopId
  if (!merchantId || !order.shopId) {
    ElMessage.error('商家信息不完整，无法联系商家')
    return
  }
  
  console.log('使用的merchantId:', merchantId)
  
  // 跳转到聊天页面
  router.push('/chat')
  
  // 延迟创建聊天会话，等待组件加载完成
  setTimeout(() => {
    // 使用localStorage传递商家信息
    localStorage.setItem('pendingChat', JSON.stringify({
      merchantId: merchantId,
      shopId: order.shopId,
      shopName: order.shopName
    }))
    
    console.log('已设置待处理聊天:', {
      merchantId: merchantId,
      shopId: order.shopId,
      shopName: order.shopName
    })
  }, 100)
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.order-header h2 {
  font-size: 20px;
  color: #333;
}

.status-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-box {
  width: 300px;
}

.search-input {
  width: 100%;
}

.order-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-right: 5px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-no {
  font-size: 14px;
  color: #333;
}

.order-time {
  font-size: 12px;
  color: #999;
}

.order-status {
  font-size: 14px;
  font-weight: 500;
  color: #4CAF50;
}

.order-shop {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #f0f0f0;
}

.shop-name {
  font-size: 14px;
  color: #333;
}

.order-items {
  padding: 15px;
}

.order-product {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.product-image {
  width: 60px;
  height: 60px;
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
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.product-specs {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.product-price {
  font-size: 14px;
  color: #ff5722;
}

.product-quantity {
  font-size: 14px;
  color: #666;
  margin: 0 15px;
}

.product-subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-right: 15px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
}

.order-total {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-total span:first-child {
  font-size: 14px;
  color: #666;
}

.order-total span:last-child {
  font-size: 16px;
  font-weight: 500;
  color: #ff5722;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .status-tabs {
    overflow-x: auto;
    padding-bottom: 10px;
  }

  .search-box {
    width: 100%;
  }

  .order-stats {
    flex-wrap: wrap;
    gap: 15px;
  }

  .order-footer {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .order-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
