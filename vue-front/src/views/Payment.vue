<template>
  <div class="payment-container" v-loading="loading">
    <div class="payment-card">
      <div class="payment-title">支付</div>

      <div class="payment-info">
        <div class="info-row">
          <span class="label">订单ID：</span>
          <span class="value">{{ orderId || '-' }}</span>
        </div>
        <div class="info-row" v-if="paymentUrl">
          <span class="label">支付链接：</span>
          <span class="value monospace">{{ paymentUrl }}</span>
        </div>
      </div>

      <div class="payment-method">
        <div class="section-title">选择支付方式</div>
        <el-radio-group v-model="method">
          <el-radio value="alipay">
            <div class="payment-option">
              <img src="../assets/payment/alipay.jpg" alt="支付宝" class="payment-icon" />
              <span>支付宝</span>
            </div>
          </el-radio>
          <el-radio value="wechat">
            <div class="payment-option">
              <img src="../assets/payment/wechat.jpg" alt="微信支付" class="payment-icon" />
              <span>微信支付</span>
            </div>
          </el-radio>
          <el-radio value="balance">
            <div class="payment-option">
              <el-icon><Wallet /></el-icon>
              <span>余额支付</span>
            </div>
          </el-radio>
        </el-radio-group>
      </div>

      <div class="payment-actions">
        <el-button 
          type="primary" 
          :loading="paying" 
          :disabled="!orderId" 
          @click="doPay"
          class="pay-button"
        >
          {{ getPayButtonText() }}
        </el-button>
        <el-button :disabled="!orderId" @click="goOrderDetail">查看订单</el-button>
      </div>

      <div class="payment-status" v-if="statusText">
        当前状态：{{ statusText }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { order } from '@/api'
import { ElMessage } from 'element-plus'
import { Wallet } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => {
  const v = route.query.orderId
  if (!v) return null
  const n = Number(v)
  return Number.isFinite(n) ? n : null
})

const paymentUrl = computed(() => {
  return route.query.paymentUrl ? String(route.query.paymentUrl) : ''
})

const method = ref(route.query.paymentMethod ? String(route.query.paymentMethod) : 'alipay')

const loading = ref(false)
const paying = ref(false)
const statusText = ref('')

// 获取支付按钮文本
const getPayButtonText = () => {
  if (paying.value) return '正在处理...'
  
  switch (method.value) {
    case 'alipay':
      return '支付宝支付'
    case 'wechat':
      return '微信支付'
    case 'balance':
      return '余额支付'
    default:
      return '立即支付'
  }
}

const refreshStatus = async () => {
  if (!orderId.value) return
  try {
    const res = await order.getPayStatus(orderId.value)
    statusText.value = res?.data?.statusText || ''
  } catch (e) {
    // ignore
  }
}

const doPay = async () => {
  if (!orderId.value) return
  
  // 根据支付方式跳转到不同的支付页面
  if (method.value === 'alipay') {
    // 跳转到支付宝支付页面
    router.push({
      path: '/payment/alipay',
      query: { orderId: orderId.value }
    })
  } else if (method.value === 'wechat') {
    // 微信支付（暂未实现）
    ElMessage.info('微信支付功能开发中，请选择其他支付方式')
  } else if (method.value === 'balance') {
    // 余额支付
    paying.value = true
    try {
      await order.pay({ orderId: orderId.value, paymentMethod: method.value })
      ElMessage.success('支付成功')
      await refreshStatus()
      router.replace(`/order/${orderId.value}`)
    } catch (e) {
      console.error('支付失败:', e)
      ElMessage.error('支付失败，请重试')
    } finally {
      paying.value = false
    }
  }
}

const goOrderDetail = () => {
  if (!orderId.value) return
  router.push(`/order/${orderId.value}`)
}

onMounted(async () => {
  loading.value = true
  try {
    await refreshStatus()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.payment-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.payment-card {
  max-width: 720px;
  margin: 0 auto;
}

.payment-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
}

.payment-info {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  width: 90px;
  color: #666;
}

.value {
  flex: 1;
  color: #333;
  word-break: break-all;
}

.monospace {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}

.payment-method {
  margin-bottom: 16px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.payment-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.payment-actions {
  display: flex;
  gap: 10px;
}

.pay-button {
  background: linear-gradient(135deg, #1677ff 0%, #1890ff 100%);
  border: none;
  min-width: 120px;
}

.payment-status {
  margin-top: 12px;
  color: #666;
}
</style>
