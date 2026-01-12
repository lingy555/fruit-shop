
<template>
  <transition name="notification" appear>
    <div 
      v-if="visible" 
      class="notification-container"
      :class="[`notification-${type}`]"
    >
      <div class="notification-content">
        <div class="notification-icon">
          <el-icon v-if="type === 'success'"><SuccessFilled /></el-icon>
          <el-icon v-else-if="type === 'warning'"><WarningFilled /></el-icon>
          <el-icon v-else-if="type === 'error'"><CircleCloseFilled /></el-icon>
          <el-icon v-else><InfoFilled /></el-icon>
        </div>

        <div class="notification-message">{{ message }}</div>

        <div class="notification-close" @click="close">
          <el-icon><Close /></el-icon>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  message: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['success', 'warning', 'error', 'info'].includes(value)
  },
  duration: {
    type: Number,
    default: 3000
  },
  showClose: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['close'])

// 是否可见
const visible = ref(false)

// 定时器
let timer = null

// 显示通知
const show = () => {
  visible.value = true

  // 自动关闭
  if (props.duration > 0) {
    timer = setTimeout(() => {
      close()
    }, props.duration)
  }
}

// 关闭通知
const close = () => {
  visible.value = false
  emit('close')

  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

// 监听message变化
onMounted(() => {
  if (props.message) {
    show()
  }
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
})
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 350px;
  word-break: break-word;
}

.notification-success {
  background-color: #f0f9eb;
  color: #67c23a;
  border-left: 4px solid #67c23a;
}

.notification-warning {
  background-color: #fdf6ec;
  color: #e6a23c;
  border-left: 4px solid #e6a23c;
}

.notification-error {
  background-color: #fef0f0;
  color: #f56c6c;
  border-left: 4px solid #f56c6c;
}

.notification-info {
  background-color: #f4f4f5;
  color: #909399;
  border-left: 4px solid #909399;
}

.notification-content {
  display: flex;
  align-items: center;
}

.notification-icon {
  margin-right: 10px;
  font-size: 20px;
}

.notification-message {
  flex: 1;
  font-size: 14px;
  line-height: 1.5;
}

.notification-close {
  margin-left: 15px;
  cursor: pointer;
  font-size: 16px;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.notification-close:hover {
  opacity: 1;
}

/* 过渡动画 */
.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.notification-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
