<template>
  <div class="alipay-payment-container" v-loading="loading">
    <div class="payment-card">
      <div class="payment-header">
        <div class="payment-title">
          <span class="alipay-icon">💳</span>
          <span>支付宝支付</span>
        </div>
        <div class="payment-desc">安全、快捷的在线支付</div>
      </div>

      <!-- 订单信息 -->
      <div class="order-info" v-if="orderInfo">
        <div class="info-row">
          <span class="label">订单编号：</span>
          <span class="value">{{ orderInfo.orderId }}</span>
        </div>
        <div class="info-row">
          <span class="label">商品名称：</span>
          <span class="value">{{ orderInfo.subject }}</span>
        </div>
        <div class="info-row">
          <span class="label">订单金额：</span>
          <span class="value amount">¥{{ orderInfo.amount }}</span>
        </div>
        <div class="info-row" v-if="orderInfo.description">
          <span class="label">订单描述：</span>
          <span class="value">{{ orderInfo.description }}</span>
        </div>
      </div>

      <!-- 支付方式选择 -->
      <div class="payment-methods">
        <div class="method-title">选择支付方式</div>
        <div class="method-list">
          <div 
            class="method-item active"
            @click="selectMethod('alipay')"
          >
            <div class="method-icon">
              <img src="../assets/payment/alipay.jpg" alt="支付宝" />
            </div>
            <div class="method-info">
              <div class="method-name">支付宝</div>
              <div class="method-desc">推荐使用支付宝支付</div>
            </div>
            <div class="method-check">
              <el-icon><Check /></el-icon>
            </div>
          </div>
          
          <div 
            class="method-item"
            @click="selectMethod('wechat')"
          >
            <div class="method-icon">
              <img src="../assets/payment/wechat.jpg" alt="微信支付" />
            </div>
            <div class="method-info">
              <div class="method-name">微信支付</div>
              <div class="method-desc">微信安全支付</div>
            </div>
            <div class="method-check">
              <el-icon></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 支付表单 -->
      <div class="payment-form" v-if="payForm">
        <div class="form-title">支付确认</div>
        <div v-html="payForm" class="alipay-form"></div>
      </div>

      <!-- 支付按钮 -->
      <div class="payment-actions" v-if="!payForm">
        <el-button 
          type="primary" 
          size="large" 
          :loading="paying"
          @click="startPayment"
          class="pay-button"
        >
          <span v-if="!paying">立即支付 ¥{{ orderInfo?.amount }}</span>
          <span v-else>正在生成支付...</span>
        </el-button>
        
        <el-button 
          size="large" 
          @click="goBack"
          class="back-button"
        >
          返回订单
        </el-button>
      </div>

      <!-- 支付状态 -->
      <div class="payment-status" v-if="paymentStatus">
        <el-alert 
          :title="paymentStatus.title" 
          :type="paymentStatus.type" 
          :description="paymentStatus.description"
          show-icon
          :closable="false"
        />
      </div>

      <!-- 安全提示 -->
      <div class="security-tips">
        <div class="tips-title">
          <el-icon><Lock /></el-icon>
          安全保障
        </div>
        <ul class="tips-list">
          <li>支付宝加密技术保护您的支付信息</li>
          <li>资金安全由支付宝担保交易</li>
          <li>7×24小时客服支持</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Lock } from '@element-plus/icons-vue'
import { order } from '@/api'
import Cookies from 'js-cookie'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const paying = ref(false)
const payForm = ref('')
const orderInfo = ref(null)
const paymentStatus = ref(null)
const selectedMethod = ref('alipay')

// 获取订单信息
const fetchOrderInfo = async () => {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.error('订单ID不能为空')
    goBack()
    return
  }

  try {
    loading.value = true
    const response = await order.getDetail(orderId)
    if (response.code === 200 && response.data) {
      const order = response.data
      orderInfo.value = {
        orderId: order.orderId,
        subject: `欢喜果铺订单-${order.orderId}`,
        amount: order.totalAmount,
        description: `订单号：${order.orderId}，商品数量：${order.items?.length || 0}`
      }
    } else {
      ElMessage.error('获取订单信息失败')
      goBack()
    }
  } catch (error) {
    console.error('获取订单信息失败:', error)
    ElMessage.error('获取订单信息失败')
    goBack()
  } finally {
    loading.value = false
  }
}

// 选择支付方式
const selectMethod = (method) => {
  selectedMethod.value = method
  // 这里可以添加不同支付方式的处理逻辑
}

// 开始支付
const startPayment = async () => {
  if (!orderInfo.value) {
    ElMessage.error('订单信息不完整')
    return
  }

  try {
    paying.value = true
    
    // 使用axios发送请求，确保携带token和自动刷新
    const response = await request({
      url: '/payment/alipay/create',
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      data: new URLSearchParams({
        orderId: orderInfo.value.orderId,
        amount: orderInfo.value.amount,
        subject: orderInfo.value.subject,
        body: orderInfo.value.description
      })
    })
    
    if (response.code === 200) {
      // 显示支付表单
      payForm.value = response.data.payForm
      
      // 自动提交表单
      setTimeout(() => {
        const form = document.querySelector('.alipay-form form')
        if (form) {
          form.submit()
        }
      }, 1000)
      
    } else {
      ElMessage.error(response.message || '创建支付订单失败')
    }
    
  } catch (error) {
    console.error('创建支付订单失败:', error)
    ElMessage.error('创建支付订单失败，请重试')
  } finally {
    paying.value = false
  }
}

// 返回订单页面
const goBack = () => {
  router.push(`/order/${route.query.orderId}`)
}

// 检查支付状态
const checkPaymentStatus = async () => {
  if (!orderInfo.value) return

  try {
    const response = await request({
      url: `/payment/alipay/query/${orderInfo.value.orderId}`,
      method: 'GET'
    })
    
    if (response.code === 200) {
      const { success, tradeStatus } = response.data
      
      if (success && (tradeStatus === 'TRADE_SUCCESS' || tradeStatus === 'TRADE_FINISHED')) {
        paymentStatus.value = {
          title: '支付成功',
          type: 'success',
          description: '订单支付成功，即将跳转到订单详情页面'
        }
        
        // 3秒后跳转到订单详情页
        setTimeout(() => {
          router.push(`/order/${orderInfo.value.orderId}`)
        }, 3000)
      } else if (tradeStatus === 'WAIT_BUYER_PAY') {
        paymentStatus.value = {
          title: '等待支付',
          type: 'info',
          description: '请在支付宝页面完成支付'
        }
      } else if (tradeStatus === 'TRADE_CLOSED') {
        paymentStatus.value = {
          title: '支付关闭',
          type: 'warning',
          description: '支付已关闭，请重新发起支付'
        }
      }
    }
  } catch (error) {
    console.error('查询支付状态失败:', error)
  }
}

  // 定期检查支付状态
let statusTimer = null
const startStatusCheck = () => {
  statusTimer = setInterval(() => {
    checkPaymentStatus()
  }, 3000) // 每3秒检查一次
}

const stopStatusCheck = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

// 组件挂载
onMounted(() => {
  fetchOrderInfo()
  startStatusCheck()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopStatusCheck()
})
</script>

<style scoped>
.alipay-payment-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.payment-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
  overflow: hidden;
}

.payment-header {
  background: linear-gradient(135deg, #1677ff 0%, #1890ff 100%);
  color: white;
  padding: 30px;
  text-align: center;
}

.payment-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.alipay-icon {
  font-size: 28px;
}

.payment-desc {
  font-size: 14px;
  opacity: 0.9;
}

.order-info {
  padding: 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.info-row:last-child {
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
  font-size: 18px;
  font-weight: 600;
}

.payment-methods {
  padding: 24px;
  border-bottom: 1px solid #e9ecef;
}

.method-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.method-item:hover {
  border-color: #1677ff;
  background: #f0f5ff;
}

.method-item.active {
  border-color: #1677ff;
  background: #f0f5ff;
}

.method-icon {
  width: 40px;
  height: 40px;
  margin-right: 12px;
}

.method-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.method-info {
  flex: 1;
}

.method-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 12px;
  color: #666;
}

.method-check {
  color: #1677ff;
  font-size: 18px;
}

.payment-form {
  padding: 24px;
  text-align: center;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.alipay-form {
  min-height: 200px;
}

.payment-actions {
  padding: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.pay-button {
  background: linear-gradient(135deg, #1677ff 0%, #1890ff 100%);
  border: none;
  padding: 12px 40px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  min-width: 200px;
}

.back-button {
  border: 1px solid #d9d9d9;
  background: white;
  color: #666;
}

.payment-status {
  padding: 0 24px 24px;
}

.security-tips {
  padding: 24px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  padding-left: 16px;
  position: relative;
}

.tips-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #1677ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .alipay-payment-container {
    padding: 10px;
  }
  
  .payment-card {
    margin: 0;
  }
  
  .payment-header {
    padding: 20px;
  }
  
  .payment-title {
    font-size: 20px;
  }
  
  .payment-actions {
    flex-direction: column;
  }
  
  .pay-button {
    width: 100%;
  }
}
</style>
