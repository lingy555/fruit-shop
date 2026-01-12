
<template>
  <div class="order-status">
    <el-tag 
      :type="tagType" 
      :effect="effect"
      class="status-tag"
    >
      <el-icon v-if="icon">
        <component :is="icon" />
      </el-icon>
      {{ text }}
    </el-tag>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: Number,
    required: true
  }
})

// 根据状态计算标签类型和图标
const statusConfig = computed(() => {
  switch (props.status) {
    case 1: // 待付款
      return { type: 'warning', icon: 'Clock', text: '待付款' }
    case 2: // 待发货
      return { type: 'primary', icon: 'Box', text: '待发货' }
    case 3: // 待收货
      return { type: 'info', icon: 'Van', text: '待收货' }
    case 4: // 待评价
      return { type: 'success', icon: 'ChatDotRound', text: '待评价' }
    case 5: // 已完成
      return { type: 'success', icon: 'CircleCheck', text: '已完成' }
    case 6: // 已取消
      return { type: 'danger', icon: 'CircleClose', text: '已取消' }
    case 7: // 退款中
      return { type: 'warning', icon: 'Refresh', text: '退款中' }
    case 8: // 已退款
      return { type: 'info', icon: 'CircleCheck', text: '已退款' }
    default:
      return { type: 'info', icon: 'QuestionFilled', text: '未知状态' }
  }
})

const tagType = computed(() => statusConfig.value.type)
const icon = computed(() => statusConfig.value.icon)
const text = computed(() => statusConfig.value.text)
const effect = computed(() => props.status === 5 ? 'dark' : 'light')
</script>

<style scoped>
.order-status {
  display: inline-flex;
  align-items: center;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.status-tag .el-icon {
  margin-right: 0;
}
</style>
