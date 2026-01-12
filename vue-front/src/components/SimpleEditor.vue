<template>
  <div class="simple-editor">
    <div class="editor-toolbar">
      <button 
        v-for="tool in toolbar" 
        :key="tool.command"
        @click="execCommand(tool.command, tool.value)"
        :class="{ active: tool.isActive() }"
        :title="tool.title"
        type="button"
      >
        <i :class="tool.icon"></i>
      </button>
    </div>
    
    <div 
      class="editor-content"
      contenteditable="true"
      @input="updateContent"
      @keydown="handleKeydown"
      ref="editorRef"
      placeholder="请输入商品详情..."
    ></div>
    
    <div class="editor-footer">
      <div class="word-count">{{ wordCount }} 字</div>
      <div class="editor-actions">
        <button @click="insertImage" type="button" class="btn-insert-image">
          <i class="el-icon-picture"></i> 插入图片
        </button>
        <button @click="insertImageUrl" type="button" class="btn-insert-url">
          <i class="el-icon-link"></i> 图片URL
        </button>
        <button @click="clearContent" type="button" class="btn-clear">
          <i class="el-icon-delete"></i> 清空
        </button>
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
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'
import request from '@/utils/request'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const imageInputRef = ref(null)

// 工具栏配置
const toolbar = [
  { command: 'bold', icon: 'el-icon-bold', title: '粗体', isActive: () => document.queryCommandState('bold') },
  { command: 'italic', icon: 'el-icon-italic', title: '斜体', isActive: () => document.queryCommandState('italic') },
  { command: 'underline', icon: 'el-icon-underline', title: '下划线', isActive: () => document.queryCommandState('underline') },
  { command: 'strikeThrough', icon: 'el-icon-delete', title: '删除线', isActive: () => document.queryCommandState('strikeThrough') },
  { command: 'justifyLeft', icon: 'el-icon-align-left', title: '左对齐', isActive: () => document.queryCommandState('justifyLeft') },
  { command: 'justifyCenter', icon: 'el-icon-align-center', title: '居中', isActive: () => document.queryCommandState('justifyCenter') },
  { command: 'justifyRight', icon: 'el-icon-align-right', title: '右对齐', isActive: () => document.queryCommandState('justifyRight') },
  { command: 'insertOrderedList', icon: 'el-icon-list', title: '有序列表', isActive: () => document.queryCommandState('insertOrderedList') },
  { command: 'insertUnorderedList', icon: 'el-icon-menu', title: '无序列表', isActive: () => document.queryCommandState('insertUnorderedList') },
  { command: 'createLink', icon: 'el-icon-link', title: '插入链接', isActive: () => false, value: null }
]

// 字数统计
const wordCount = computed(() => {
  if (!editorRef.value) return 0
  const text = editorRef.value.innerText || ''
  return text.length
})

// 执行编辑命令
const execCommand = (command, value = null) => {
  if (command === 'createLink') {
    const url = prompt('请输入链接地址:')
    if (url) {
      document.execCommand(command, false, url)
    }
  } else {
    document.execCommand(command, false, value)
  }
  updateContent()
}

// 更新内容
const updateContent = () => {
  if (editorRef.value) {
    const content = editorRef.value.innerHTML
    emit('update:modelValue', content)
  }
}

// 处理键盘事件
const handleKeydown = (e) => {
  // 处理Tab键缩进
  if (e.key === 'Tab') {
    e.preventDefault()
    document.execCommand('insertText', false, '  ')
  }
}

// 插入图片
const insertImage = () => {
  imageInputRef.value?.click()
}

// 插入图片URL
const insertImageUrl = () => {
  const url = prompt('请输入图片URL:')
  if (url && url.trim()) {
    const imgHtml = `<img src="${url.trim()}" alt="商品图片" style="max-width: 100%; height: auto; border-radius: 4px; margin: 10px 0;" onerror="this.style.display='none'; this.alt='图片加载失败'">`
    document.execCommand('insertHTML', false, imgHtml)
    updateContent()
    ElMessage.success('图片插入成功')
  }
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
    
    console.log('开始上传图片:', file.name, file.size)
    
    // 使用配置好的request实例
    const response = await request.post('/upload', formData)
    
    console.log('上传响应:', response)
    
    if (response.code === 200) {
      // 在光标位置插入图片
      const imgHtml = `<img src="${response.data.url}" alt="商品图片" style="max-width: 100%; height: auto; border-radius: 4px; margin: 10px 0;">`
      document.execCommand('insertHTML', false, imgHtml)
      updateContent()
      ElMessage.success('图片插入成功')
    } else {
      ElMessage.error('图片上传失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败: ' + (error.message || '网络错误'))
  }
  
  // 清空文件输入
  e.target.value = ''
}

// 清空内容
const clearContent = () => {
  if (editorRef.value) {
    editorRef.value.innerHTML = ''
    updateContent()
  }
}

// 监听外部内容变化
watch(() => props.modelValue, (newValue) => {
  if (editorRef.value && editorRef.value.innerHTML !== newValue) {
    editorRef.value.innerHTML = newValue
  }
}, { immediate: true })

onMounted(() => {
  // 初始化内容
  if (editorRef.value && props.modelValue) {
    editorRef.value.innerHTML = props.modelValue
  }
})
</script>

<style scoped>
.simple-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  padding: 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.editor-toolbar button {
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 3px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
}

.editor-toolbar button:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.editor-toolbar button.active {
  background: #409eff;
  border-color: #409eff;
  color: white;
}

.editor-content {
  min-height: 300px;
  max-height: 500px;
  padding: 15px;
  overflow-y: auto;
  line-height: 1.6;
  font-size: 14px;
  color: #303133;
}

.editor-content:focus {
  outline: none;
  background: #fafafa;
}

.editor-content:empty:before {
  content: attr(placeholder);
  color: #c0c4cc;
  font-style: italic;
}

.editor-content img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 10px 0;
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 15px;
  background: #f5f7fa;
  border-top: 1px solid #dcdfe6;
  font-size: 12px;
  color: #909399;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.btn-insert-image, .btn-insert-url, .btn-clear {
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  color: #606266;
  transition: all 0.2s;
}

.btn-insert-image:hover, .btn-insert-url:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.btn-clear:hover {
  background: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .editor-toolbar {
    gap: 1px;
  }
  
  .editor-toolbar button {
    padding: 4px 6px;
    font-size: 12px;
  }
  
  .editor-content {
    min-height: 200px;
    padding: 10px;
  }
  
  .editor-footer {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>
