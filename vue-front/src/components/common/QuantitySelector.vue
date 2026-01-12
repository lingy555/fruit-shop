
<template>
  <div class="quantity-selector">
    <el-input-number 
      v-model="modelValue" 
      :min="min" 
      :max="max" 
      :size="size"
      :disabled="disabled"
      @change="handleChange"
    />

    <div class="quantity-info" v-if="showInfo">
      <span v-if="stock !== undefined">库存：{{ stock }}件</span>
      <span class="unit">{{ unit || '件' }}</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Number,
    default: 1
  },
  min: {
    type: Number,
    default: 1
  },
  max: {
    type: Number,
    default: 9999
  },
  size: {
    type: String,
    default: 'default'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  stock: {
    type: Number,
    default: undefined
  },
  unit: {
    type: String,
    default: '件'
  },
  showInfo: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

// 处理数量变化
const handleChange = (value) => {
  emit('update:modelValue', value)
}
</script>

<style scoped>
.quantity-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-info {
  font-size: 12px;
  color: #666;
}

.unit {
  margin-left: 5px;
}
</style>
