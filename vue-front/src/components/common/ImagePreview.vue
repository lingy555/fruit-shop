
<template>
  <div class="image-preview">
    <!-- 图片预览 -->
    <el-image
      :src="images[currentIndex]"
      :preview-src-list="images"
      :initial-index="currentIndex"
      fit="contain"
      class="preview-image"
      @switch="handleSwitch"
      @close="handleClose"
    />

    <!-- 缩略图列表 -->
    <div class="thumbnail-list" v-if="showThumbnails">
      <div 
        v-for="(image, index) in images" 
        :key="index"
        class="thumbnail-item"
        :class="{ active: currentIndex === index }"
        @click="selectImage(index)"
      >
        <img :src="image" :alt="`图片${index + 1}`" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  initialIndex: {
    type: Number,
    default: 0
  },
  showThumbnails: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['close'])

// 当前图片索引
const currentIndex = ref(props.initialIndex)

// 选择图片
const selectImage = (index) => {
  currentIndex.value = index
}

// 切换图片
const handleSwitch = (index) => {
  currentIndex.value = index
}

// 关闭预览
const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.image-preview {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.preview-image {
  max-width: 80%;
  max-height: 80%;
}

.thumbnail-list {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  gap: 10px;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.6;
  transition: all 0.3s;
}

.thumbnail-item.active {
  border-color: #4CAF50;
  opacity: 1;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
