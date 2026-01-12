<template>
  <div class="chat-page">
    <div class="page-header">
      <h2>消息中心</h2>
      <p>与商家进行实时沟通，获取优质服务</p>
    </div>
    
    <div class="chat-container">
      <UserChat 
        ref="userChatRef" 
        :userId="currentUserId"
        :showSessionList="true"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import UserChat from '@/components/UserChat.vue'

const router = useRouter()
const userStore = useUserStore()
const userChatRef = ref(null)

// 立即获取用户ID，避免组件渲染时为0
const currentUserId = computed(() => {
  return userStore.userInfo?.userId || userStore.userInfo?.id || 0
})

onMounted(async () => {
  console.log('Chat页面加载开始')
  
  // 如果用户token存在但没有用户信息，尝试获取
  if (userStore.token && !userStore.userInfo) {
    console.log('尝试获取用户信息...')
    try {
      await userStore.getUserInfo()
      console.log('获取用户信息成功')
    } catch (error) {
      console.error('获取用户信息失败:', error)
      router.push('/login')
      return
    }
  }
  
  console.log('用户ID:', currentUserId.value)
  
  // 如果没有用户信息，重定向到登录页
  if (!currentUserId.value) {
    console.log('用户未登录，跳转到登录页')
    router.push('/login')
    return
  }
  
  // 检查是否有待处理的聊天请求
  const pendingChat = localStorage.getItem('pendingChat')
  console.log('待处理聊天数据:', pendingChat)
  
  if (pendingChat) {
    try {
      const chatData = JSON.parse(pendingChat)
      console.log('解析聊天数据:', chatData)
      
      // 等待组件加载完成后创建聊天会话
      await nextTick()
      
      // 如果缺少merchantId，使用shopId作为替代
      const merchantId = chatData.merchantId || chatData.shopId
      const shopId = chatData.shopId
      
      console.log('使用的ID:', { merchantId, shopId })
      console.log('原始聊天数据:', chatData)
      
      if (userChatRef.value && merchantId && shopId) {
        console.log('开始创建聊天会话')
        // 直接创建会话并打开聊天，不依赖现有会话列表
        const session = await userChatRef.value.createOrGetSession(merchantId, shopId)
        
        console.log('创建的会话信息:', session)
        
        // 清除待处理的聊天请求
        localStorage.removeItem('pendingChat')
        console.log('聊天会话创建完成')
      } else {
        console.log('缺少必要信息:', { 
          hasRef: !!userChatRef.value, 
          merchantId, 
          shopId 
        })
      }
    } catch (error) {
      console.error('处理聊天请求失败:', error)
      localStorage.removeItem('pendingChat')
    }
  } else {
    // 没有待处理的聊天请求，正常显示会话列表
    console.log('没有待处理聊天，显示会话列表')
    // UserChat组件会自动加载会话列表
  }
})
</script>

<style scoped>
.chat-page {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
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
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

@media (max-width: 768px) {
  .chat-page {
    padding: 10px;
  }
  
  .chat-container {
    height: calc(100vh - 160px);
  }
}
</style>
