<template>
  <div class="chat-container">
    <!-- 聊天会话列表 -->
    <div class="chat-sidebar" v-if="showSessionList">
      <div class="chat-header">
        <h3>消息列表</h3>
        <div class="unread-badge" v-if="totalUnreadCount > 0">
          {{ totalUnreadCount }}
        </div>
      </div>
      
      <div class="session-list">
        <div 
          v-for="session in sessions" 
          :key="session.sessionId"
          class="session-item"
          :class="{ active: currentSession.sessionId === session.sessionId }"
          @click="openChat(session)"
        >
          <div class="session-avatar">
            <img v-if="session.shopLogo" :src="session.shopLogo" :alt="session.shopName" />
            <div v-else class="default-avatar">{{ session.shopName?.charAt(0) || '店' }}</div>
          </div>
          
          <div class="session-content">
            <div class="session-header">
              <span class="shop-name">{{ session.shopName }}</span>
              <span class="last-time">{{ formatTime(session.lastMessageTime) }}</span>
            </div>
            <div class="session-message">
              <span class="last-message">{{ session.lastMessageContent || '暂无消息' }}</span>
              <span class="unread-count" v-if="session.userUnreadCount > 0">
                {{ session.userUnreadCount }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="sessions.length === 0" class="empty-sessions">
        <p>暂无聊天记录</p>
        <p class="tip">去订单页面联系商家开始聊天吧</p>
      </div>
    </div>

    <!-- 聊天详情界面 -->
    <div class="chat-detail" v-if="showChatDetail || !showSessionList">
      <div class="chat-header">
        <button class="back-btn" v-if="showSessionList && showChatDetail" @click="backToList">
          <i class="el-icon-arrow-left"></i>
        </button>
        <div class="shop-info">
          <div class="shop-avatar">
            <img v-if="currentSession.shopLogo" :src="currentSession.shopLogo" :alt="currentSession.shopName" />
            <div v-else class="default-avatar">{{ currentSession.shopName?.charAt(0) || '店' }}</div>
          </div>
          <span class="shop-name">{{ currentSession.shopName }}</span>
        </div>
      </div>

      <div class="message-list" ref="messageListRef">
        <div 
          v-for="message in messages" 
          :key="message.messageId"
          class="message-item"
          :class="{ 'message-sent': message.senderType === 1, 'message-received': message.senderType === 2 }"
        >
          <div class="message-content">
            <div class="message-bubble">
              <div v-if="message.messageType === 1" class="text-message">
                {{ message.content }}
              </div>
              <div v-else-if="message.messageType === 2" class="image-message">
                <img :src="message.content" :alt="message.content" @click="previewImage(message.content)" />
              </div>
              <div v-else-if="message.messageType === 3" class="product-message">
                商品链接: {{ message.content }}
              </div>
            </div>
            <div class="message-time">
              {{ formatMessageTime(message.sendTime) }}
            </div>
          </div>
        </div>
      </div>

      <div class="message-input">
        <div class="input-toolbar">
          <button class="toolbar-btn" @click="selectImage">
            <i class="el-icon-picture"></i>
          </button>
          <button class="toolbar-btn" @click="selectProduct">
            <i class="el-icon-goods"></i>
          </button>
        </div>
        
        <div class="input-area">
          <textarea 
            v-model="messageText"
            placeholder="输入消息..."
            @keydown.enter.prevent="sendMessage"
            rows="3"
          ></textarea>
          <button 
            class="send-btn"
            @click="sendMessage"
            :disabled="!messageText.trim()"
          >
            发送
          </button>
        </div>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input 
      ref="imageInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleImageUpload"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import Cookies from 'js-cookie'

const props = defineProps({
  userId: {
    type: Number,
    required: true
  },
  showSessionList: {
    type: Boolean,
    default: false
  }
})

// 响应式数据
const sessions = ref([])
const messages = ref([])
const currentSession = ref({})
const messageText = ref('')
const showChatDetail = ref(false)
const totalUnreadCount = ref(0)

// DOM引用
const messageListRef = ref(null)
const imageInputRef = ref(null)

// 获取用户聊天会话列表
const fetchSessions = async () => {
  try {
    console.log('获取用户会话列表，userId:', props.userId)
    const response = await request.get('/chat/sessions/user', {
      params: { userId: props.userId }
    })
    console.log('用户会话列表响应:', response.data)
    sessions.value = response.data.list || []
    calculateTotalUnread()
    console.log('当前会话数量:', sessions.value.length)
  } catch (error) {
    console.error('获取会话列表失败:', error)
    ElMessage.error('获取消息列表失败')
  }
}

// 获取会话消息
const fetchMessages = async (sessionId) => {
  try {
    const response = await request.get(`/chat/messages/${sessionId}`, {
      params: { userId: props.userId }
    })
    messages.value = response.data.list || []
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('获取消息失败:', error)
    ElMessage.error('获取消息失败')
  }
}

// 打开聊天
const openChat = async (session) => {
  console.log('打开聊天详情:', session)
  
  currentSession.value = session
  showChatDetail.value = true
  
  await fetchMessages(session.sessionId)
  
  // 标记消息为已读
  markAsRead(session.sessionId)
  
  console.log('聊天详情已打开，showChatDetail:', showChatDetail.value)
}

// 返回列表
const backToList = () => {
  if (!props.showSessionList) {
    // 只有在不显示会话列表时才隐藏聊天详情
    showChatDetail.value = false
    currentSession.value = {}
    messages.value = []
    messageText.value = ''
  }
  // 如果显示会话列表，不清除聊天状态，只是隐藏返回按钮
}

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim()) return
  
  try {
    const response = await request.post('/chat/message/send', {
      sessionId: currentSession.value.sessionId,
      senderType: 1, // 用户
      senderId: props.userId,
      messageType: 1, // 文本消息
      content: messageText.value.trim()
    })
    
    messages.value.push(response.data)
    messageText.value = ''
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
    
    // 更新会话列表
    await fetchSessions()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

// 标记消息为已读
const markAsRead = async (sessionId) => {
  try {
    await request.post('/chat/messages/read', null, {
      params: {
        sessionId,
        readerType: 1,
        readerId: props.userId
      }
    })
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 选择图片
const selectImage = () => {
  imageInputRef.value?.click()
}

// 处理图片上传
const handleImageUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 验证文件大小 (2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const uploadResponse = await request.post('/upload', formData)
    
    // 发送图片消息
    const messageResponse = await request.post('/chat/message/send', {
      sessionId: currentSession.value.sessionId,
      senderType: 1,
      senderId: props.userId,
      messageType: 2, // 图片消息
      content: uploadResponse.data.url
    })
    
    messages.value.push(messageResponse.data)
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
    
    // 更新会话列表
    await fetchSessions()
  } catch (error) {
    console.error('发送图片失败:', error)
    ElMessage.error('发送图片失败')
  }
  
  // 清空文件输入
  e.target.value = ''
}

// 选择商品
const selectProduct = () => {
  ElMessage.info('商品选择功能开发中')
}

// 预览图片
const previewImage = (url) => {
  window.open(url, '_blank')
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 计算总未读数
const calculateTotalUnread = () => {
  totalUnreadCount.value = sessions.value.reduce((total, session) => {
    return total + (session.userUnreadCount || 0)
  }, 0)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

// 格式化消息时间
const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 创建或获取会话
const createOrGetSession = async (merchantId, shopId) => {
  try {
    console.log('创建聊天会话:', { merchantId, shopId, userId: props.userId })
    
    const response = await request.post('/chat/session/create', null, {
      params: {
        userId: props.userId,
        merchantId,
        shopId
      }
    })
    
    console.log('会话创建成功:', response.data)
    
    // 确保显示聊天详情，不管是否有会话列表
    const session = response.data
    currentSession.value = session
    showChatDetail.value = true
    
    // 获取消息（即使是新会话也要尝试获取）
    await fetchMessages(session.sessionId)
    
    // 更新会话列表（可选）
    await fetchSessions()
    
    ElMessage.success('已进入与商家的聊天')
    
    // 返回会话信息
    return session
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败')
    throw error
  }
}

// 暴露方法给父组件
defineExpose({
  createOrGetSession
})

onMounted(() => {
  fetchSessions()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 600px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.unread-badge {
  background: #ff4757;
  color: white;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
  min-width: 18px;
  text-align: center;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: background-color 0.2s;
}

.session-item:hover {
  background: #f8f9fa;
}

.session-item.active {
  background: #e6f7ff;
  border-left: 3px solid #409eff;
}

.session-item.active .shop-name {
  color: #409eff;
}

.session-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
  flex-shrink: 0;
}

.session-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  width: 100%;
  height: 100%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.shop-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.last-time {
  font-size: 12px;
  color: #999;
}

.session-message {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  color: #666;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.unread-count {
  background: #ff4757;
  color: white;
  border-radius: 8px;
  padding: 1px 6px;
  font-size: 11px;
  margin-left: 8px;
}

.chat-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  background: #f8f9fa;
}

.back-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  margin-right: 12px;
  color: #666;
}

.shop-info {
  display: flex;
  align-items: center;
}

.shop-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 8px;
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-list {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f5f5f5;
}

.message-item {
  margin-bottom: 16px;
  display: flex;
}

.message-sent {
  justify-content: flex-end;
}

.message-received {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  background: white;
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  margin-bottom: 4px;
}

.message-sent .message-bubble {
  background: #409eff;
  color: white;
}

.text-message {
  word-wrap: break-word;
  line-height: 1.4;
}

.image-message img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
  cursor: pointer;
}

.product-message {
  color: #666;
  font-style: italic;
}

.message-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}

.message-sent .message-time {
  text-align: right;
}

.message-received .message-time {
  text-align: left;
}

.message-input {
  border-top: 1px solid #e0e0e0;
  padding: 12px;
  background: white;
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.toolbar-btn {
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: #e9ecef;
  border-color: #409eff;
  color: #409eff;
}

.input-area {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.input-area textarea {
  flex: 1;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 8px 12px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.4;
}

.input-area textarea:focus {
  outline: none;
  border-color: #409eff;
}

.send-btn {
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #337ecc;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.empty-sessions {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  text-align: center;
  padding: 40px 20px;
}

.empty-sessions p {
  margin: 8px 0;
  font-size: 16px;
}

.empty-sessions .tip {
  font-size: 14px;
  color: #bbb;
}

@media (max-width: 768px) {
  .chat-container {
    height: 100vh;
    border-radius: 0;
  }
  
  .chat-sidebar {
    width: 100%;
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>
