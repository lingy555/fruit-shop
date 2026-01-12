
<template>
  <div class="spec-selector">
    <div 
      v-for="spec in specifications" 
      :key="spec.specId"
      class="spec-group"
    >
      <div class="spec-name">{{ spec.specName }}</div>
      <div class="spec-options">
        <el-radio-group 
          v-model="selectedSpecs[spec.specName]" 
          @change="selectSku"
          class="spec-radio-group"
        >
          <el-radio-button 
            v-for="option in spec.options" 
            :key="option"
            :label="option"
            :disabled="!isOptionAvailable(spec.specName, option)"
            class="spec-option"
          >
            {{ option }}
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="stock-info" v-if="currentSku">
      <span v-if="currentSku.stock > 0">库存：{{ currentSku.stock }}件</span>
      <span v-else class="out-of-stock">暂时缺货</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'

const props = defineProps({
  specifications: {
    type: Array,
    default: () => []
  },
  skuList: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])

// 选中的规格
const selectedSpecs = reactive({ ...props.modelValue })

// 当前SKU
const currentSku = computed(() => {
  if (!props.skuList || props.skuList.length === 0) {
    return null
  }

  // 根据选中的规格查找对应的SKU
  const specs = Object.values(selectedSpecs)
  if (specs.length === 0) {
    return props.skuList[0] || null
  }

  return props.skuList.find(sku => {
    return Object.entries(sku.specs).every(([key, value]) => selectedSpecs[key] === value)
  }) || null
})

// 检查选项是否可用
const isOptionAvailable = (specName, option) => {
  if (!props.skuList || props.skuList.length === 0) {
    return true
  }

  return props.skuList.some(sku => {
    return sku.specs[specName] === option && sku.stock > 0
  })
}

// 选择SKU
const selectSku = () => {
  // 通知父组件更新选中值
  emit('update:modelValue', { ...selectedSpecs })
}

// 监听选中值变化
watch(
  () => props.modelValue,
  (newVal) => {
    Object.assign(selectedSpecs, newVal)
  }
)
</script>

<style scoped>
.spec-selector {
  margin-bottom: 20px;
}

.spec-group {
  margin-bottom: 15px;
}

.spec-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-radio-group {
  width: 100%;
}

.spec-option {
  margin-right: 10px;
  margin-bottom: 10px;
}

.stock-info {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.out-of-stock {
  color: #f56c6c;
  font-weight: 500;
}
</style>
