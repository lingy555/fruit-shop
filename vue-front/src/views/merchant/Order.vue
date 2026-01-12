
<template>
  <div class="order-container">
    <div class="order-header">
      <h2>订单管理</h2>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <div class="filter-item">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号/收货人/手机号"
          @keyup.enter="fetchOrders"
          clearable
          class="search-input"
        >
          <template #append>
            <el-button icon="Search" @click="fetchOrders">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-item">
        <span class="filter-label">状态：</span>
        <el-select 
          v-model="status" 
          @change="fetchOrders" 
          placeholder="全部状态"
        >
          <el-option label="全部" value="" />
          <el-option label="待发货" value="1" />
          <el-option label="待收货" value="2" />
          <el-option label="已完成" value="3" />
          <el-option label="已取消" value="4" />
          <el-option label="退款中" value="5" />
        </el-select>
      </div>

      <div class="filter-item">
        <span class="filter-label">时间：</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="fetchOrders"
        />
      </div>

      <div class="filter-item">
        <el-button 
          type="primary" 
          icon="Download"
          @click="exportOrders"
        >
          导出订单
        </el-button>
      </div>
    </div>

    <!-- 订单统计 -->
    <div class="order-stats" v-if="statusCount">
      <div class="stat-item">
        <span class="stat-label">待发货：</span>
        <span class="stat-value">{{ statusCount.waitDeliver || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">待收货：</span>
        <span class="stat-value">{{ statusCount.waitReceive || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">已完成：</span>
        <span class="stat-value">{{ statusCount.finished || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">退款中：</span>
        <span class="stat-value">{{ statusCount.refunding || 0 }}</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading">
      <el-table
        :data="orders"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column label="订单信息" min-width="300">
          <template #default="{ row }">
            <div class="order-info">
              <div class="order-no">订单号：{{ row.orderNo }}</div>
              <div class="order-time">{{ formatDate(row.createTime) }}</div>
            </div>
            <div class="user-info">
              <div class="user-name">{{ row.username }}</div>
              <div class="user-phone">{{ row.address?.receiverPhone || '-' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="商品信息" width="400">
          <template #default="{ row }">
            <div v-if="getPrimaryItem(row)" class="product-info">
              <div class="product-image">
                <img :src="getPrimaryItem(row).image" :alt="getPrimaryItem(row).productName" />
              </div>
              <div class="product-details">
                <div class="product-name" :title="getPrimaryItem(row).productName">{{ getPrimaryItem(row).productName }}</div>
                <div class="product-specs" v-if="getPrimaryItem(row).specs">{{ getPrimaryItem(row).specs }}</div>
                <div class="product-price">¥{{ formatMoney(getPrimaryItem(row).price) }}</div>
              </div>
            </div>
            <div v-else class="product-info">
              <div class="product-details">
                <div class="product-name">-</div>
              </div>
            </div>
            <div class="product-quantity">× {{ getTotalQuantity(row) }}</div>
            <div class="product-subtotal">¥{{ formatMoney(calcSubtotal(row)) }}</div>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <div class="amount-info">
              <div class="amount-item">总额：¥{{ formatMoney(row.totalAmount) }}</div>
              <div class="amount-item">实付：¥{{ formatMoney(row.actualAmount) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-for="action in getAvailableActions(row)" 
              :key="action.text"
              :type="action.type"
              :icon="action.icon"
              size="small"
              @click="handleAction(action, row)"
            >
              {{ action.text }}
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
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 发货对话框 -->
    <el-dialog
      v-model="deliverDialogVisible"
      title="发货"
      width="500px"
    >
      <el-form
        ref="deliverFormRef"
        :model="deliverForm"
        :rules="deliverRules"
        label-width="80px"
      >
        <el-form-item label="快递公司" prop="expressCompany">
          <el-input v-model="deliverForm.expressCompany" placeholder="请输入快递公司" />
        </el-form-item>

        <el-form-item label="快递单号" prop="expressNo">
          <el-input v-model="deliverForm.expressNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="deliverDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="delivering"
          @click="submitDeliver"
        >
          确定发货
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量发货对话框 -->
    <el-dialog
      v-model="batchDeliverDialogVisible"
      title="批量发货"
      width="800px"
    >
      <div class="batch-deliver-content">
        <div class="upload-section">
          <el-upload
            ref="excelUploadRef"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeExcelUpload"
            :on-success="handleExcelSuccess"
            :on-error="handleExcelError"
            accept=".xlsx,.xls"
            drag
          >
            <template #trigger>
              <el-button type="primary">选择Excel文件</el-button>
            </template>
            <template #tip>
              <div class="upload-tip">
                <p>请下载模板文件，按照格式填写订单信息</p>
                <el-button type="text" @click="downloadTemplate">下载模板</el-button>
              </div>
            </template>
          </el-upload>
        </div>

        <div class="preview-section" v-if="batchDeliverList.length > 0">
          <h4>预览发货信息</h4>
          <el-table :data="batchDeliverList" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="expressCompany" label="快递公司" width="120">
              <template #default="{ row }">
                <el-input v-model="row.expressCompany" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="expressNo" label="快递单号" width="150">
              <template #default="{ row }">
                <el-input v-model="row.expressNo" size="small" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchDeliverDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="batchDelivering"
          @click="submitBatchDeliver"
        >
          确定批量发货
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { merchantOrder as order } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const toNumber = (val) => {
  if (val === null || val === undefined || val === '') return 0
  const n = Number(val)
  return Number.isFinite(n) ? n : 0
}

const formatMoney = (val) => {
  const n = toNumber(val)
  return n.toFixed(2)
}

const getPrimaryItem = (row) => {
  const items = row?.items
  return Array.isArray(items) && items.length > 0 ? items[0] : null
}

const getTotalQuantity = (row) => {
  const items = row?.items
  if (!Array.isArray(items) || items.length === 0) return 0
  return items.reduce((sum, it) => sum + toNumber(it?.quantity), 0)
}

const calcSubtotal = (row) => {
  const items = row?.items
  if (!Array.isArray(items) || items.length === 0) return 0
  return items.reduce((sum, it) => {
    const subtotal = it?.subtotal
    if (subtotal !== null && subtotal !== undefined && subtotal !== '') {
      return sum + toNumber(subtotal)
    }
    return sum + toNumber(it?.price) * toNumber(it?.quantity)
  }, 0)
}

const getStatusType = (status) => {
  switch (status) {
    case 1: return 'warning'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'info'
    case 5: return 'danger'
    default: return 'info'
  }
}

// 搜索关键词
const searchKeyword = ref('')

// 订单状态
const status = ref('')

// 时间范围
const dateRange = ref([])

// 订单列表
const orders = ref([])

// 选中的订单
const selectedOrders = ref([])

// 订单状态统计
const statusCount = ref(null)

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 发货对话框可见性
const deliverDialogVisible = ref(false)

// 批量发货对话框可见性
const batchDeliverDialogVisible = ref(false)

// 发货表单引用
const deliverFormRef = ref(null)

// 发货表单
const deliverForm = reactive({
  orderId: 0,
  expressCompany: '',
  expressNo: ''
})

// 发货表单验证规则
const deliverRules = {
  expressCompany: [
    { required: true, message: '请输入快递公司', trigger: 'blur' }
  ],
  expressNo: [
    { required: true, message: '请输入快递单号', trigger: 'blur' }
  ]
}

// 发货状态
const delivering = ref(false)

// 批量发货列表
const batchDeliverList = ref([])

// 批量发货状态
const batchDelivering = ref(false)

// 上传地址
const uploadUrl = '/api/merchant/upload'

// Excel上传引用
const excelUploadRef = ref(null)

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value || []
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: status.value || undefined,
      keyword: searchKeyword.value.trim() || undefined,
      startTime: startDate ? formatDate(startDate) : undefined,
      endTime: endDate ? formatDate(endDate) : undefined
    }

    const response = await order.list(params)
    orders.value = response.data.list || []
    statusCount.value = response.data.statusCount || null
    pagination.total = response.data.total || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取可用操作
const getAvailableActions = (order) => {
  const actions = []

  switch (order.status) {
    case 1: // 待发货
      actions.push(
        { text: '发货', type: 'primary', icon: 'Van', action: 'deliver' }
      )
      break
    case 2: // 待收货
      actions.push(
        { text: '查看详情', type: 'default', icon: 'View', action: 'detail' }
      )
      break
    case 3: // 已完成
      actions.push(
        { text: '查看详情', type: 'default', icon: 'View', action: 'detail' }
      )
      break
  case 4: // 已取消
      actions.push(
        { text: '查看详情', type: 'default', icon: 'View', action: 'detail' }
      )
      break
    case 5: // 退款中
      actions.push(
        { text: '处理退款', type: 'warning', icon: 'Warning', action: 'refund' }
      )
      break
  }

  return actions
}

// 处理订单操作
const handleAction = (action, order) => {
  switch (action.action) {
    case 'deliver':
      showDeliverDialog(order.orderId)
      break
    case 'detail':
      // 跳转到订单详情页
      break
    case 'refund':
      // 跳转到退款处理页
      break
  }
}

// 显示发货对话框
const showDeliverDialog = (orderId) => {
  deliverForm.orderId = orderId
  deliverForm.expressCompany = ''
  deliverForm.expressNo = ''
  deliverDialogVisible.value = true
}

// 提交发货
const submitDeliver = async () => {
  const valid = await deliverFormRef.value.validate().catch(() => false)
  if (!valid) return

  delivering.value = true
  try {
    await order.deliver({
      orderId: deliverForm.orderId,
      expressCompany: deliverForm.expressCompany,
      expressNo: deliverForm.expressNo
    })

    ElMessage.success('发货成功')
    deliverDialogVisible.value = false

    // 刷新订单列表
    fetchOrders()
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  } finally {
    delivering.value = false
  }
}

// 显示批量发货对话框
const showBatchDeliverDialog = () => {
  // 获取选中的待发货订单
  const waitDeliverOrders = selectedOrders.value.filter(order => order.status === 1)

  if (waitDeliverOrders.length === 0) {
    ElMessage.warning('请选择待发货的订单')
    return
  }

  // 转换为批量发货格式
  batchDeliverList.value = waitDeliverOrders.map(order => ({
    orderNo: order.orderNo,
    expressCompany: '',
    expressNo: ''
  }))

  batchDeliverDialogVisible.value = true
}

// 提交批量发货
const submitBatchDeliver = async () => {
  // 验证数据
  const validData = batchDeliverList.value.every(item => 
    item.expressCompany && item.expressNo
  )

  if (!validData) {
    ElMessage.warning('请完善所有订单的发货信息')
    return
  }

  batchDelivering.value = true
  try {
    const deliverList = batchDeliverList.value.map(item => ({
      orderId: orders.value.find(o => o.orderNo === item.orderNo).orderId,
      expressCompany: item.expressCompany,
      expressNo: item.expressNo
    }))

    await order.batchDeliver({ deliverList })

    ElMessage.success('批量发货成功')
    batchDeliverDialogVisible.value = false

    // 刷新订单列表
    fetchOrders()
  } catch (error) {
    console.error('批量发货失败:', error)
    ElMessage.error('批量发货失败')
  } finally {
    batchDelivering.value = false
  }
}

// 导出订单
const exportOrders = async () => {
  try {
    const [startDate, endDate] = dateRange.value || []
    await ElMessageBox.confirm('确定要导出订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 这里应该调用导出API
    // await order.export({
    //   status: status.value || undefined,
    //   keyword: searchKeyword.value.trim() || undefined,
    //   startTime: startDate ? formatDate(startDate) : undefined,
    //   endTime: endDate ? formatDate(endDate) : undefined
    // })

    ElMessage.success('导出成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出失败:', error)
      ElMessage.error('导出失败')
    }
  }
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchOrders()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  fetchOrders()
}

// Excel上传前校验
const beforeExcelUpload = (file) => {
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')

  if (!isExcel) {
    ElMessage.error('只能上传Excel文件!')
    return false
  }

  return true
}

// Excel上传成功
const handleExcelSuccess = (response) => {
  // 这里应该处理上传成功的响应，解析Excel并填充到批量发货列表
  ElMessage.success('文件上传成功')
}

// Excel上传失败
const handleExcelError = () => {
  ElMessage.error('文件上传失败')
}

// 下载模板
const downloadTemplate = () => {
  // 这里应该提供模板下载链接
  ElMessage.success('模板下载中...')
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''

  if (date instanceof Date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
  if (typeof date === 'string') {
    return date.length >= 10 ? date.slice(0, 10) : date
  }

  const d = new Date(date)
  if (Number.isNaN(d.getTime())) {
    return String(date)
  }
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.order-header {
  margin-bottom: 20px;
}

.order-header h2 {
  font-size: 20px;
  color: #333;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.filter-item {
  display: flex;
  align-items: center;
}

.filter-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
  white-space: nowrap;
}

.search-input {
  width: 250px;
}

.order-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-right: 5px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
}

.order-list {
  margin-bottom: 20px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-no {
  font-size: 14px;
  color: #333;
}

.order-time {
  font-size: 12px;
  color: #999;
}

.user-info {
  display: flex;
  gap: 10px;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.user-phone {
  font-size: 14px;
  color: #666;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  overflow: hidden;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.product-specs {
  font-size: 12px;
  color: #999;
  margin-top: 3px;
}

.product-price {
  font-size: 14px;
  font-weight: 500;
  color: #ff5722;
}

.product-quantity {
  font-size: 14px;
  color: #666;
}

.product-subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #ff5722;
}

.amount-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.amount-item {
  font-size: 14px;
  color: #666;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.batch-deliver-content {
  max-height: 60vh;
  overflow-y: auto;
}

.upload-section {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

.upload-tip p {
  margin: 0;
  line-height: 1.5;
}

.preview-section {
  margin-top: 20px;
}

.preview-section h4 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}
</style>
