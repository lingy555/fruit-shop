
<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-message">抱歉，您访问的页面不存在</div>
      <div class="error-description">
        可能是输入了错误的网址，或者该页面已被删除
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 返回首页
const goHome = () => {
  router.push('/home')
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 20px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #4CAF50;
  line-height: 1.2;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-message {
  font-size: 24px;
  color: #333;
  margin-bottom: 15px;
}

.error-description {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>
