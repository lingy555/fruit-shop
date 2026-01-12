<template>
  <div class="merchant-chat-page">
    <div class="page-header">
      <h2>客服消息</h2>
      <p>与客户进行实时沟通，提供优质服务</p>
    </div>
    
    <div class="chat-container">
      <MerchantChat v-if="currentMerchantId > 0" :merchantId="currentMerchantId" />
      <div v-else class="loading-container">
        <p>正在加载商家信息...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMerchantStore } from '@/store/merchant'
import MerchantChat from '@/components/MerchantChat.vue'

const router = useRouter()
const merchantStore = useMerchantStore()
const currentMerchantId = ref(0)

onMounted(async () => {
  console.log('商家聊天页面加载开始')
  console.log('merchantToken:', merchantStore.merchantToken)
  console.log('merchantInfo:', merchantStore.merchantInfo)
  console.log('isLoggedIn:', merchantStore.isLoggedIn)
  
  // 如果商家token存在但没有商家信息，尝试获取
  if (merchantStore.merchantToken && !merchantStore.merchantInfo) {
    console.log('尝试获取商家信息...')
    try {
      const response = await merchantStore.getMerchantInfo()
      console.log('获取商家信息成功:', response)
    } catch (error) {
      console.error('获取商家信息失败:', error)
      console.log('重定向到登录页')
      router.push('/merchant/login')
      return
    }
  }
  
  // 获取当前商家ID
  if (merchantStore.merchantInfo) {
    currentMerchantId.value = merchantStore.merchantInfo.merchantId || merchantStore.merchantInfo.id
    console.log('商家ID:', currentMerchantId.value)
  }
  
  // 如果没有商家信息，重定向到登录页
  if (!currentMerchantId.value) {
    console.log('没有商家ID，重定向到登录页')
    router.push('/merchant/login')
  } else {
    console.log('商家信息验证通过，显示聊天界面')
  }
})
</script>

<style scoped>
.merchant-chat-page {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
  text-align: left;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.chat-container {
  max-width: 1200px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  height: calc(100vh - 200px);
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 200px);
  color: #666;
  font-size: 16px;
}

@media (max-width: 768px) {
  .merchant-chat-page {
    padding: 10px;
  }
  
  .chat-container {
    height: calc(100vh - 160px);
  }
}
</style>
