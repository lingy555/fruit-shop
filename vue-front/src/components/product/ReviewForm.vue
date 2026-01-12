
<template>
  <div class="review-form">
    <el-form
      ref="reviewFormRef"
      :model="reviewForm"
      :rules="reviewRules"
      label-width="80px"
    >
      <el-form-item label="评分" prop="score">
        <el-rate 
          v-model="reviewForm.score" 
          :max="5"
          show-text
          text-color="#ff9900"
        />
      </el-form-item>

      <el-form-item label="评价" prop="content">
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          placeholder="请输入您的评价内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="图片">
        <el-upload
          ref="uploadRef"
          v-model:file-list="reviewForm.images"
          :action="uploadUrl"
          :before-upload="beforeUpload"
          :on-success="handleSuccess"
          :on-error="handleError"
          :limit="5"
          list-type="picture-card"
          accept="image/*"
        >
          <template #trigger>
            <el-button type="primary">上传图片</el-button>
          </template>
          <template #tip>
            <div class="upload-tip">
              <p>最多上传5张图片</p>
              <p>支持jpg、png格式，单张不超过2MB</p>
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="reviewForm.isAnonymous">匿名评价</el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button 
          type="primary" 
          :loading="submitting"
          @click="submitReview"
        >
          提交评价
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { comment } from '@/api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  orderItemId: {
    type: Number,
    required: true
  },
  productId: {
    type: Number,
    required: true
  }
})

// 评价表单引用
const reviewFormRef = ref(null)

// 上传组件引用
const uploadRef = ref(null)

// 上传地址
const uploadUrl = '/api/upload'

// 评价表单
const reviewForm = reactive({
  score: 5,
  content: '',
  images: [],
  isAnonymous: false
})

// 评价表单验证规则
const reviewRules = {
  score: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容不能少于10个字符', trigger: 'blur' }
  ]
}

// 提交状态
const submitting = ref(false)

// 上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }

  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }

  return true
}

// 上传成功
const handleSuccess = (response, file) => {
  const url = response?.data?.url || response?.url
  if (!url) {
    ElMessage.error('图片上传失败：未返回图片地址')
    return
  }
  if (file) {
    file.url = url
  }
  ElMessage.success('图片上传成功')
}

// 上传失败
const handleError = () => {
  ElMessage.error('图片上传失败')
}

// 提交评价
const submitReview = async () => {
  const valid = await reviewFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await comment.add({
      orderItemId: props.orderItemId,
      productId: props.productId,
      score: reviewForm.score,
      content: reviewForm.content,
      images: (reviewForm.images || [])
        .map(f => f?.url || f?.response?.data?.url || f?.response?.url)
        .filter(Boolean),
      isAnonymous: reviewForm.isAnonymous
    })

    ElMessage.success('评价提交成功')

    // 触发成功事件
    emit('success')
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('提交评价失败')
  } finally {
    submitting.value = false
  }
}

// 定义事件
const emit = defineEmits(['success'])
</script>

<style scoped>
.review-form {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.upload-tip p {
  margin: 0;
  line-height: 1.5;
}
</style>
