<template>
  <div class="merchant-chat-container">
    <!-- 聊天会话列表 -->
    <div class="chat-sidebar">
      <div class="chat-header">
        <h3>客服消息</h3>
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
            <img v-if="session.userAvatar" :src="session.userAvatar" :alt="session.userName" />
            <div v-else class="default-avatar">{{ session.userName?.charAt(0) || '用' }}</div>
          </div>
          
          <div class="session-content">
            <div class="session-header">
              <span class="user-name">{{ session.userName }}</span>
              <span class="last-time">{{ formatTime(session.lastMessageTime) }}</span>
            </div>
            <div class="session-message">
              <span class="last-message">{{ session.lastMessageContent || '暂无消息' }}</span>
              <span class="unread-count" v-if="session.merchantUnreadCount > 0">
                {{ session.merchantUnreadCount }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="sessions.length === 0" class="empty-sessions">
        <p>暂无客户消息</p>
        <p class="tip">等待客户联系...</p>
      </div> 
    </div>

    <!-- 聊天详情界面 -->
    <div class="chat-detail">
      <div class="chat-header">
        <div class="user-info">
          <div class="user-avatar">
            <img v-if="currentSession.userAvatar" :src="currentSession.userAvatar" :alt="currentSession.userName" />
            <div v-else class="default-avatar">{{ currentSession.userName?.charAt(0) || '用' }}</div>
          </div>
          <span class="user-name">{{ currentSession.userName || '请选择会话' }}</span>
        </div>
        <div class="session-actions">
          <button class="action-btn" @click="markAllAsRead" v-if="hasSelectedSession && currentSession.merchantUnreadCount > 0">
            标记已读
          </button>
        </div>
      </div>

      <div class="message-list" ref="messageListRef" v-if="hasSelectedSession">
        <div 
          v-for="message in messages" 
          :key="message.messageId"
          class="message-item"
          :class="{ 
            'message-sent': message.senderType === 2, 
            'message-received': message.senderType === 1 
          }"
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
                <div class="product-card">
                  <div class="product-info">
                    <i class="el-icon-goods"></i>
                    <span>商品咨询</span>
                  </div>
                  <div class="product-content">{{ message.content }}</div>
                </div>
              </div>
            </div>
            <div class="message-time">
              {{ formatMessageTime(message.sendTime) }}
            </div>
          </div>
        </div>
      </div>

      <div class="message-list" v-else>
        <div class="empty-sessions">
          <p>请选择左侧会话开始聊天</p>
        </div>
      </div>

      <div class="message-input">
        <div class="input-toolbar">
          <button class="toolbar-btn" @click="selectImage" :disabled="!hasSelectedSession">
            <i class="el-icon-picture"></i>
          </button>
          <button class="toolbar-btn" @click="selectProduct" :disabled="!hasSelectedSession">
            <i class="el-icon-goods"></i>
          </button>
          <button class="toolbar-btn" @click="insertQuickReply" v-if="quickReplies.length > 0" :disabled="!hasSelectedSession">
            <i class="el-icon-chat-dot-round"></i>
            快捷回复
          </button>
        </div>
        
        <div class="input-area">
          <textarea 
            v-model="messageText"
            placeholder="输入回复消息..."
            @keydown.enter.prevent="sendMessage"
            rows="3"
            :disabled="!hasSelectedSession"
          ></textarea>
          <button 
            class="send-btn"
            @click="sendMessage"
            :disabled="!hasSelectedSession || !messageText.trim()"
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

    <!-- 快捷回复弹窗 -->
    <div class="quick-reply-modal" v-if="showQuickReply" @click="showQuickReply = false">
      <div class="quick-reply-content" @click.stop>
        <div class="quick-reply-header">
          <h4>快捷回复</h4>
          <button class="close-btn" @click="showQuickReply = false">
            <i class="el-icon-close"></i>
          </button>
        </div>
        <div class="quick-reply-list">
          <div 
            v-for="reply in quickReplies" 
            :key="reply"
            class="quick-reply-item"
            @click="selectQuickReply(reply)"
          >
            {{ reply }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import merchantRequest from '@/utils/merchantRequest'
import Cookies from 'js-cookie'

const props = defineProps({
  merchantId: {
    type: Number,
    required: true
  }
})

// 响应式数据
const sessions = ref([])
const messages = ref([])
const currentSession = ref({})
const messageText = ref('')
const showChatDetail = ref(false)
const totalUnreadCount = ref(0)
const showQuickReply = ref(false)

const hasSelectedSession = computed(() => !!currentSession.value?.sessionId)

// DOM引用
const messageListRef = ref(null)
const imageInputRef = ref(null)

// 快捷回复列表
const quickReplies = ref([
  '您好，欢迎咨询！请问有什么可以帮助您的？',
  '感谢您的咨询，我会尽快为您解答。',
  '抱歉让您久等了，我正在为您查询相关信息。',
  '感谢您的理解与支持！',
  '如果您还有其他问题，请随时联系我们。',
  '祝您生活愉快，购物愉快！'
])

// 获取商家聊天会话列表
const fetchSessions = async () => {
  try {
    console.log('获取商家会话列表，merchantId:', props.merchantId)
    const response = await merchantRequest.get('/chat/sessions/merchant', {
      params: { merchantId: props.merchantId }
    })
    console.log('商家会话列表响应:', response)
    sessions.value = response.list || []
    calculateTotalUnread()
    console.log('当前会话数量:', sessions.value.length)

    if (!hasSelectedSession.value && sessions.value.length > 0) {
      await openChat(sessions.value[0])
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
    ElMessage.error('获取消息列表失败')
  }
}

// 获取会话消息
const fetchMessages = async (sessionId) => {
  try {
    const response = await merchantRequest.get(`/chat/messages/${sessionId}`, {
      params: { merchantId: props.merchantId }
    })
    messages.value = response.list || []
    
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
  currentSession.value = session
  showChatDetail.value = true
  
  await fetchMessages(session.sessionId)
  
  // 自动标记为已读
  if (session.merchantUnreadCount > 0) {
    markAsRead(session.sessionId)
  }
}

// 返回列表
const backToList = () => {
  // 商家端始终显示分栏布局，不需要隐藏聊天详情
  // 只是清除当前选中的会话状态
  currentSession.value = {}
  messages.value = []
  messageText.value = ''
  showChatDetail.value = false
}

// 发送消息
const sendMessage = async () => {
  if (!hasSelectedSession.value) {
    ElMessage.warning('请选择会话')
    return
  }
  if (!messageText.value.trim()) return
  
  console.log('商家发送消息:', {
    sessionId: currentSession.value.sessionId,
    senderType: 2,
    senderId: props.merchantId,
    messageType: 1,
    content: messageText.value.trim()
  })
  
  try {
    const response = await merchantRequest.post('/chat/message/send', {
      sessionId: currentSession.value.sessionId,
      senderType: 2, // 商家
      senderId: props.merchantId,
      messageType: 1, // 文本消息
      content: messageText.value.trim()
    })
    
    console.log('消息发送成功:', response)
    
    messages.value.push(response)
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
    await merchantRequest.post('/chat/messages/read', null, {
      params: {
        sessionId,
        readerType: 2,
        readerId: props.merchantId
      }
    })
    
    // 更新会话状态
    const session = sessions.value.find(s => s.sessionId === sessionId)
    if (session) {
      session.merchantUnreadCount = 0
    }
    calculateTotalUnread()
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 标记所有消息为已读
const markAllAsRead = async () => {
  await markAsRead(currentSession.value.sessionId)
  ElMessage.success('已标记为已读')
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
    
    const uploadResponse = await merchantRequest.post('/upload', formData)
    
    // 发送图片消息
    const messageResponse = await merchantRequest.post('/chat/message/send', {
      sessionId: currentSession.value.sessionId,
      senderType: 2,
      senderId: props.merchantId,
      messageType: 2, // 图片消息
      content: uploadResponse.url
    })
    
    messages.value.push(messageResponse)
    
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

// 插入快捷回复
const insertQuickReply = () => {
  if (!hasSelectedSession.value) {
    ElMessage.warning('请选择会话')
    return
  }
  showQuickReply.value = true
}

// 选择快捷回复
const selectQuickReply = (reply) => {
  messageText.value = reply
  showQuickReply.value = false
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
    return total + (session.merchantUnreadCount || 0)
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

// 暴露方法给父组件
defineExpose({
  fetchSessions
})

onMounted(() => {
  console.log('商家聊天组件初始化')
  console.log('merchantId:', props.merchantId)
  
  if (!props.merchantId) {
    console.error('商家ID为空，无法获取会话列表')
    return
  }
  
  fetchSessions()
  
  // 定时刷新消息列表
  setInterval(() => {
    if (!showChatDetail.value) {
      console.log('定时刷新会话列表')
      fetchSessions()
    }
  }, 30000) // 30秒刷新一次
})
</script>

<style scoped>
.merchant-chat-container {
  display: flex;
  height: 100%;
  min-height: 520px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.chat-sidebar {
  width: 320px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  min-height: 0;
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
  min-height: 0;
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

.session-item.active .user-name {
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
  background: #28a745;
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

.user-name {
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
  min-height: 0;
}

.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 8px;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.session-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn:hover {
  background: #218838;
}

.message-list {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f5f5f5;
  min-height: 0;
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
  background: #28a745;
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
  padding: 0;
}

.product-card {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 8px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 12px;
  color: #666;
}

.product-content {
  font-size: 13px;
  color: #333;
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
  font-size: 12px;
  color: #666;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: #e9ecef;
  border-color: #28a745;
  color: #28a745;
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
  border-color: #28a745;
}

.send-btn {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #218838;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.quick-reply-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.quick-reply-content {
  background: white;
  border-radius: 8px;
  width: 400px;
  max-height: 500px;
  overflow: hidden;
}

.quick-reply-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-reply-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #666;
}

.quick-reply-list {
  max-height: 400px;
  overflow-y: auto;
}

.quick-reply-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.quick-reply-item:hover {
  background: #f8f9fa;
}

.quick-reply-item:last-child {
  border-bottom: none;
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
  .merchant-chat-container {
    height: 100vh;
    border-radius: 0;
  }
  
  .chat-sidebar {
    width: 100%;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .quick-reply-content {
    width: 90%;
    margin: 0 20px;
  }
}
</style>
