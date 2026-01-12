
<template>
  <div class="product-container">
    <div class="product-header">
      <h2>商品管理</h2>
      <div class="header-actions">
        <el-button 
          type="primary" 
          icon="Plus"
          @click="showAddDialog"
        >
          添加商品
        </el-button>

        <el-button 
          icon="Upload"
          @click="showBatchImportDialog"
        >
          批量导入
        </el-button>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <div class="filter-item">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称"
            @keyup.enter="fetchProducts"
            clearable
            class="search-input"
          >
            <template #append>
              <el-button icon="Search" @click="fetchProducts">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="filter-item">
          <span class="filter-label">分类：</span>
          <el-select 
            v-model="categoryId" 
            @change="fetchProducts" 
            placeholder="全部分类"
            clearable
          >
            <el-option 
              v-for="category in categories" 
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
            />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="filter-label">状态：</span>
          <el-select 
            v-model="status" 
            @change="fetchProducts" 
            placeholder="全部状态"
          >
            <el-option label="全部" value="" />
            <el-option label="下架" value="0" />
            <el-option label="上架" value="1" />
            <el-option label="待审核" value="2" />
            <el-option label="审核失败" value="3" />
          </el-select>
        </div>

        <div class="filter-item">
          <el-button 
            type="primary" 
            @click="showBatchDialog"
          >
            批量操作
          </el-button>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-list" v-loading="loading">
      <div class="list-header">
        <div class="total-info">
          共 <span class="total-count">{{ pagination.total }}</span> 件商品
        </div>

        <div class="sort-options">
          <span class="sort-label">排序：</span>
          <el-select 
            v-model="sortBy" 
            @change="fetchProducts" 
            placeholder="默认排序"
          >
            <el-option label="默认排序" value="" />
            <el-option label="创建时间" value="createTime" />
            <el-option label="销量" value="sales" />
            <el-option label="库存" value="stock" />
            <el-option label="价格" value="price" />
          </el-select>
        </div>
      </div>

      <el-table
        :data="products"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column label="商品信息" min-width="300">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-image">
                <img :src="row.image" :alt="row.productName" />
              </div>
              <div class="product-details">
                <div class="product-name" :title="row.productName">{{ row.productName }}</div>
                <div class="product-category">{{ row.categoryName }}</div>
                <div class="product-price">¥{{ row.price }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="库存" prop="stock" width="80" />

        <el-table-column label="销量" prop="sales" width="80" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              size="small"
              @click="showEditDialog(row)"
            >
              编辑
            </el-button>

            <el-button 
              v-if="row.status === 1"
              link 
              type="danger" 
              size="small"
              @click="updateStatus(row.productId, 0)"
            >
              下架
            </el-button>

            <el-button 
              v-if="row.status === 0"
              link 
              type="success" 
              size="small"
              @click="updateStatus(row.productId, 1)"
            >
              上架
            </el-button>

            <el-button 
              link 
              type="danger" 
              size="small"
              @click="deleteProduct(row.productId)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑商品' : '添加商品'"
      width="80%"
      top="5vh"
      @close="resetEditForm"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="editForm.productName" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select 
            v-model="editForm.categoryId" 
            placeholder="请选择商品分类"
          >
            <el-option 
              v-for="category in categories" 
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品图片" prop="imageList">
          <div style="display: flex; align-items: center; gap: 12px; width: 100%; margin-bottom: 8px;">
            <el-input
              v-model="imageUrlInput"
              placeholder="也可以手动粘贴图片URL，回车或点击添加"
              @keyup.enter="addImageByUrl"
            />
            <el-button type="primary" @click="addImageByUrl">添加</el-button>
          </div>
          <el-upload
            ref="imageUploadRef"
            v-model:file-list="editForm.imageList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeImageUpload"
            :on-success="handleImageSuccess"
            :on-error="handleImageError"
            :limit="5"
            list-type="picture-card"
            accept="image/*"
          >
            <template #trigger>
              <el-button type="primary">选择图片</el-button>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品视频" prop="videoUrl">
          <el-upload
            ref="videoUploadRef"
            v-model:file-list="editForm.videoList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeVideoUpload"
            :on-success="handleVideoSuccess"
            :on-error="handleVideoError"
            :limit="1"
            accept="video/*"
          >
            <template #trigger>
              <el-button type="primary">选择视频</el-button>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number 
            v-model="editForm.price" 
            :precision="2"
            :min="0"
            placeholder="请输入商品价格"
          />
        </el-form-item>

        <el-form-item label="原价" prop="originalPrice">
          <el-input-number 
            v-model="editForm.originalPrice" 
            :precision="2"
            :min="0"
            placeholder="请输入商品原价"
          />
        </el-form-item>

        <el-form-item label="成本价" prop="costPrice">
          <el-input-number 
            v-model="editForm.costPrice" 
            :precision="2"
            :min="0"
            placeholder="请输入成本价"
          />
        </el-form-item>

        <el-form-item label="库存" prop="stock">
          <el-input-number 
            v-model="editForm.stock" 
            :min="0"
            placeholder="请输入库存数量"
          />
        </el-form-item>

        <el-form-item label="单位" prop="unit">
          <el-input v-model="editForm.unit" placeholder="请输入单位，如：个、箱、斤" />
        </el-form-item>

        <el-form-item label="重量" prop="weight">
          <el-input v-model="editForm.weight" placeholder="请输入重量，如：500g、1kg" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input 
            v-model="editForm.description" 
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>

        <el-form-item label="商品详情" prop="detail">
          <SimpleEditor v-model="editForm.detail" />
        </el-form-item>

        <el-form-item label="商品标签" prop="tags">
          <el-input 
            v-model="editForm.tags" 
            placeholder="请输入商品标签，多个标签用逗号分隔"
          />
        </el-form-item>

        <el-form-item label="运费设置">
          <el-checkbox v-model="editForm.freeShipping">包邮</el-checkbox>
          <el-input-number 
            v-model="editForm.shippingFee" 
            :precision="2"
            :min="0"
            :disabled="editForm.freeShipping"
            placeholder="请输入运费"
            style="margin-left: 10px; width: 200px;"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="saving"
          @click="saveProduct"
        >
          {{ isEdit ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量操作对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量操作"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="batchType">
            <el-radio value="status">批量上下架</el-radio>
            <el-radio value="delete">批量删除</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="操作状态" v-if="batchType === 'status'">
          <el-radio-group v-model="batchStatus">
            <el-radio value="1">上架</el-radio>
            <el-radio value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="batchProcessing"
          @click="processBatch"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { merchantProduct as product } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import Cookies from 'js-cookie'
import SimpleEditor from '@/components/SimpleEditor.vue'

// 搜索关键词
const searchKeyword = ref('')

// 分类ID
const categoryId = ref('')

// 商品状态
const status = ref('')

// 排序方式
const sortBy = ref('')

// 分类列表
const categories = ref([])

// 商品列表
const products = ref([])

// 选中的商品
const selectedProducts = ref([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 加载状态
const loading = ref(false)

// 编辑对话框可见性
const editDialogVisible = ref(false)

// 是否编辑模式
const isEdit = ref(false)

// 编辑表单引用
const editFormRef = ref(null)

// 图片上传引用
const imageUploadRef = ref(null)

// 视频上传引用
const videoUploadRef = ref(null)

// 上传地址
const uploadUrl = '/api/upload'

const uploadHeaders = computed(() => {
  const merchantToken = Cookies.get('merchantToken')
  return merchantToken ? { Authorization: `Bearer ${merchantToken}` } : {}
})

// 编辑表单
const editForm = reactive({
  productName: '',
  categoryId: '',
  imageList: [],
  videoList: [],
  price: 0,
  originalPrice: 0,
  costPrice: 0,
  stock: 0,
  unit: '',
  weight: '',
  description: '',
  detail: '',
  tags: '',
  freeShipping: true,
  shippingFee: 0
})

const imageUrlInput = ref('')

// 编辑表单验证规则
const editRules = {
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  imageList: [
    { required: true, message: '请上传商品图片', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请输入商品单位', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' }
  ]
}

// 保存状态
const saving = ref(false)

// 批量操作对话框可见性
const batchDialogVisible = ref(false)

// 批量操作类型
const batchType = ref('status')

// 批量操作状态
const batchStatus = ref('1')

// 批量操作处理状态
const batchProcessing = ref(false)

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value.trim() || undefined,
      categoryId: categoryId.value || undefined,
      status: status.value || undefined,
      sortBy: sortBy.value || undefined
    }

    const response = await product.getList(params)
    products.value = response.data.list || []
    pagination.total = response.data.total || 0
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await product.getCategories()
    categories.value = response.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedProducts.value = selection
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  resetEditForm()
  editDialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = async (row) => {
  isEdit.value = true

  try {
    // 获取完整商品详情，确保所有字段都有值
    const response = await product.getDetail(row.productId)
    const full = response.data || {}

    // Debug: log incoming product data
    console.log('Editing product (full detail):', full)

    // 处理图片列表：优先使用 images 数组，否则用 image 单字段
    let images = []
    if (Array.isArray(full.images) && full.images.length > 0) {
      images = full.images
    } else if (full.image) {
      images = [full.image]
    }

    // 填充表单
    Object.assign(editForm, {
      productId: full.productId,
      productName: full.productName || '',
      categoryId: full.categoryId || '',
      imageList: images.map((img, index) => ({ name: `image${index}`, url: img })),
      videoList: full.videoUrl ? [{ name: 'video', url: full.videoUrl }] : [],
      price: full.price ?? 0,
      originalPrice: full.originalPrice ?? 0,
      costPrice: full.costPrice ?? 0,
      stock: full.stock ?? 0,
      unit: full.unit || '',
      weight: full.weight || '',
      description: full.description || '',
      detail: full.detail || '',
      tags: Array.isArray(full.tags) ? full.tags.join(',') : (full.tags || ''),
      freeShipping: full.deliveryInfo?.freeShipping ?? true,
      shippingFee: full.deliveryInfo?.shippingFee ?? 0
    })

    // Debug: log final form data
    console.log('Form data after assign:', editForm)

    editDialogVisible.value = true
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
}

// 显示批量导入对话框
const showBatchImportDialog = () => {
  ElMessage.info('批量导入功能开发中')
}

// 重置编辑表单
const resetEditForm = () => {
  Object.assign(editForm, {
    productId: null,
    productName: '',
    categoryId: '',
    imageList: [],
    videoList: [],
    price: 0,
    originalPrice: 0,
    costPrice: 0,
    stock: 0,
    unit: '',
    weight: '',
    description: '',
    detail: '',
    tags: '',
    freeShipping: true,
    shippingFee: 0
  })

  imageUrlInput.value = ''

  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

const addImageByUrl = async () => {
  const url = (imageUrlInput.value || '').trim()
  if (!url) {
    return
  }
  if (!/^https?:\/\//i.test(url) && !url.startsWith('/uploads/')) {
    ElMessage.error('请输入正确的图片URL（http/https 或 /uploads/...）')
    return
  }
  const list = editForm.imageList || []
  if (list.length >= 5) {
    ElMessage.warning('最多添加5张图片')
    return
  }
  if (list.some(i => i?.url === url)) {
    ElMessage.warning('该图片已添加')
    return
  }
  editForm.imageList = [...list, { name: `image-url-${Date.now()}`, url }]
  imageUrlInput.value = ''

  if (editFormRef.value) {
    await editFormRef.value.validateField('imageList').catch(() => {})
  }
}

// 图片上传前校验
const beforeImageUpload = (file) => {
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

// 视频上传前校验
const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }

  if (!isLt10M) {
    ElMessage.error('视频大小不能超过10MB!')
    return false
  }

  return true
}

// 图片上传成功
const handleImageSuccess = (response, uploadFile) => {
  const url = response?.data?.url || response?.url
  if (!url) {
    ElMessage.error('图片上传失败：未返回图片地址')
    return
  }
  if (uploadFile) {
    uploadFile.url = url
  }
  ElMessage.success('图片上传成功')
}

// 视频上传成功
const handleVideoSuccess = (response, uploadFile) => {
  const url = response?.data?.url || response?.url
  if (!url) {
    ElMessage.error('视频上传失败：未返回视频地址')
    return
  }
  if (uploadFile) {
    uploadFile.url = url
  }
  editForm.videoList = [{ name: 'video', url }]
  ElMessage.success('视频上传成功')
}

// 图片上传失败
const handleImageError = () => {
  ElMessage.error('图片上传失败')
}

// 视频上传失败
const handleVideoError = () => {
  ElMessage.error('视频上传失败')
}

// 保存商品
const saveProduct = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      productName: editForm.productName,
      categoryId: editForm.categoryId,
      image: editForm.imageList?.[0]?.url || '',
      images: (editForm.imageList || []).map(i => i?.url).filter(Boolean),
      videoUrl: editForm.videoList?.[0]?.url || null,
      price: editForm.price,
      originalPrice: editForm.originalPrice,
      costPrice: editForm.costPrice,
      stock: editForm.stock,
      unit: editForm.unit,
      weight: editForm.weight,
      description: editForm.description,
      detail: editForm.detail,
      tags: (editForm.tags || '')
        .split(',')
        .map(t => t.trim())
        .filter(Boolean),
      specifications: [],
      skuList: [],
      freeShipping: !!editForm.freeShipping,
      shippingFee: editForm.shippingFee
    }

    if (isEdit.value) {
      // 编辑商品
      await product.update(editForm.productId, payload)
      ElMessage.success('商品更新成功')
    } else {
      // 添加商品
      await product.add(payload)
      ElMessage.success('商品添加成功')
    }

    editDialogVisible.value = false

    // 刷新列表
    fetchProducts()
  } catch (error) {
    console.error('保存商品失败:', error)
    ElMessage.error('保存商品失败')
  } finally {
    saving.value = false
  }
}

// 更新商品状态
const updateStatus = async (productId, status) => {
  try {
    await product.updateStatus(productId, status)
    ElMessage.success('状态更新成功')

    // 刷新列表
    fetchProducts()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 删除商品
const deleteProduct = async (productId) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await product.delete(productId)
    ElMessage.success('商品删除成功')

    // 刷新列表
    fetchProducts()
  } catch (error) {
    console.error('删除商品失败:', error)
    ElMessage.error('删除商品失败')
  }
}

// 处理批量操作
const processBatch = async () => {
  if (selectedProducts.value.length === 0) {
    ElMessage.warning('请选择要操作的商品')
    return
  }

  try {
    batchProcessing.value = true

    if (batchType.value === 'status') {
      // 批量上下架
      const productIds = selectedProducts.value.map(p => p.productId)
      await product.batchStatus(productIds, parseInt(batchStatus.value))
      ElMessage.success(`批量${batchStatus.value === 1 ? '上架' : '下架'}成功`)
    } else if (batchType.value === 'delete') {
      // 批量删除
      const productIds = selectedProducts.value.map(p => p.productId)
      await product.batchDelete(productIds)
      ElMessage.success('批量删除成功')
    }

    batchDialogVisible.value = false

    // 刷新列表
    fetchProducts()
  } catch (error) {
    console.error('批量操作失败:', error)
    ElMessage.error('批量操作失败')
  } finally {
    batchProcessing.value = false
  }
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchProducts()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  fetchProducts()
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.product-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.product-header h2 {
  font-size: 20px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.search-input {
  width: 300px;
}

.product-list {
  margin-bottom: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.total-info {
  font-size: 14px;
  color: #666;
}

.total-count {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
}

.sort-options {
  display: flex;
  align-items: center;
}

.sort-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 10px;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-details {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-category {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.product-price {
  font-size: 16px;
  font-weight: 500;
  color: #ff5722;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
  }

  .filter-item {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }
}
</style>
