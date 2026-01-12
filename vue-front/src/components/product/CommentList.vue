
<template>
  <div class="comment-list-container">
    <!-- 评论统计 -->
    <div class="comment-summary" v-if="summary">
      <div class="summary-item">
        <span class="summary-label">好评率：</span>
        <span class="summary-value good">{{ summary.goodRate }}%</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">好评：</span>
        <span class="summary-value">{{ summary.goodCount }}条</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">中评：</span>
        <span class="summary-value">{{ summary.mediumCount }}条</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">差评：</span>
        <span class="summary-value">{{ summary.badCount }}条</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">有图：</span>
        <span class="summary-value">{{ summary.imageCount }}条</span>
      </div>
    </div>

    <!-- 评论筛选 -->
    <div class="comment-filters">
      <el-radio-group v-model="commentType" @change="fetchComments">
        <el-radio-button label="0">全部</el-radio-button>
        <el-radio-button label="1">好评</el-radio-button>
        <el-radio-button label="2">中评</el-radio-button>
        <el-radio-button label="3">差评</el-radio-button>
        <el-radio-button label="4">有图</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list" v-if="comments.length > 0">
      <div 
        v-for="comment in comments" 
        :key="comment.commentId"
        class="comment-item"
      >
        <div class="comment-header">
          <el-avatar :size="40" :src="comment.avatar" fit="cover">
            {{ comment.username.charAt(0) }}
          </el-avatar>
          <div class="user-info">
            <div class="username">{{ comment.username }}</div>
            <div class="comment-time">{{ formatDate(comment.createTime) }}</div>
          </div>
          <el-rate 
            v-model="comment.score" 
            disabled 
            show-score 
            text-color="#ff9900" 
            score-template="{value}"
          />
        </div>

        <div class="comment-content">{{ comment.content }}</div>

        <!-- 评论图片 -->
        <div class="comment-images" v-if="comment.images && comment.images.length > 0">
          <el-image 
            v-for="(image, index) in comment.images" 
            :key="index"
            :src="image" 
            :preview-src-list="comment.images"
            fit="cover"
            class="comment-image"
          />
        </div>

        <!-- 规格信息 -->
        <div class="comment-specs" v-if="comment.specs">
          <span class="specs-label">规格：</span>
          <span class="specs-value">{{ comment.specs }}</span>
        </div>

        <!-- 商家回复 -->
        <div class="comment-reply" v-if="comment.reply">
          <div class="reply-header">
            <span class="reply-label">商家回复：</span>
            <span class="reply-time">{{ formatDate(comment.reply.replyTime) }}</span>
          </div>
          <div class="reply-content">{{ comment.reply.content }}</div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="comments.length > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 空状态 -->
    <el-empty 
      v-if="comments.length === 0 && !loading"
      description="暂无评价"
      :image-size="200"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, defineProps } from 'vue'
import { product } from '@/api'

const props = defineProps({
  productId: {
    type: Number,
    required: true
  }
})

// 评论类型
const commentType = ref('0')

// 评论统计
const summary = ref(null)

// 评论列表
const comments = ref([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 获取商品评价
const fetchComments = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      type: commentType.value
    }

    const response = await product.getComments(props.productId, params)
    comments.value = response.data.list || []
    summary.value = response.data.summary || null
    pagination.total = response.data.total || 0
  } catch (error) {
    console.error('获取商品评价失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchComments()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  fetchComments()
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list-container {
  padding: 20px 0;
}

.comment-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.summary-item {
  display: flex;
  align-items: center;
}

.summary-label {
  font-size: 14px;
  color: #666;
  margin-right: 5px;
}

.summary-value {
  font-size: 14px;
  font-weight: 500;
}

.summary-value.good {
  color: #4CAF50;
}

.comment-filters {
  margin-bottom: 20px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  flex: 1;
  margin-left: 10px;
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-top: 3px;
}

.comment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 10px;
}

.comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}

.comment-specs {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.specs-label {
  margin-right: 5px;
}

.comment-reply {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 10px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.reply-label {
  font-size: 14px;
  color: #4CAF50;
  font-weight: 500;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.reply-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
