
<template>
  <div class="merchant-container">
    <div class="merchant-header">
      <h2>商家管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAuditDialog">审核商家</el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <div class="filter-item">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商家名称/联系人/手机号"
          @keyup.enter="fetchMerchants"
          clearable
          class="search-input"
        >
          <template #append>
            <el-button icon="Search" @click="fetchMerchants">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-item">
        <span class="filter-label">状态：</span>
        <el-select 
          v-model="status" 
          @change="fetchMerchants" 
          placeholder="全部状态"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="待审核" value="0" />
          <el-option label="正常" value="1" />
          <el-option label="禁用" value="2" />
          <el-option label="审核失败" value="3" />
        </el-select>
      </div>

      <div class="filter-item">
        <span class="filter-label">地区：</span>
        <el-select 
          v-model="province" 
          @change="fetchMerchants" 
          placeholder="全部省份"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option 
            v-for="p in provinces" 
            :key="p.value"
            :label="p.label"
            :value="p.value"
          />
        </el-select>
      </div>

      <div class="filter-actions">
        <el-button type="primary" @click="showBatchDialog">批量操作</el-button>
      </div>
    </div>

    <!-- 商家统计 -->
    <div class="merchant-stats" v-if="statusCount">
      <div class="stat-item">
        <span class="stat-label">待审核：</span>
        <span class="stat-value">{{ statusCount.pending || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">正常：</span>
        <span class="stat-value">{{ statusCount.normal || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">禁用：</span>
        <span class="stat-value">{{ statusCount.disabled || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">审核失败：</span>
        <span class="stat-value">{{ statusCount.rejected || 0 }}</span>
      </div>
    </div>

    <!-- 商家列表 -->
    <div class="merchant-list" v-loading="loading">
      <el-table
        :data="merchants"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column label="商家信息" min-width="300">
          <template #default="{ row }">
            <div class="merchant-info">
              <div class="merchant-logo">
                <img :src="row.logo" :alt="row.shopName" />
              </div>
              <div class="merchant-details">
                <div class="shop-name">{{ row.shopName }}</div>
                <div class="contact-info">
                  <div class="contact-item">
                    <span class="label">联系人：</span>
                    <span class="value">{{ row.contactName }}</span>
                  </div>
                  <div class="contact-item">
                    <span class="label">手机号：</span>
                    <span class="value">{{ row.contactPhone }}</span>
                  </div>
                  <div class="contact-item">
                    <span class="label">邮箱：</span>
                    <span class="value">{{ row.email || '未设置' }}</span>
                  </div>
                </div>
                <div class="shop-stats">
                  <div class="stat-item">
                    <span class="label">商品数：</span>
                    <span class="value">{{ row.productCount }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="label">总销量：</span>
                    <span class="value">{{ row.totalSales }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="label">粉丝数：</span>
                    <span class="value">{{ row.fanCount }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="label">店铺评分：</span>
                    <el-rate 
                      v-model="row.score" 
                      disabled 
                      show-score 
                      text-color="#ff9900" 
                      score-template="{value}"
                    />
                  </div>
                </div>
              </div>
            </div>
            </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="审核时间" width="150">
          <template #default="{ row }">
            {{ row.auditTime || '-' }}
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
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="商家审核"
      width="600px"
      @close="resetAuditForm"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        label-width="80px"
      >
        <el-form-item label="商家ID" prop="merchantId">
          <el-input v-model="auditForm.merchantId" placeholder="请输入商家ID" />
        </el-form-item>

        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审核备注" prop="auditRemark">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="auditSubmitting">提交审核</el-button>
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
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="processBatch" :loading="batchProcessing">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { admin } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索关键词
const searchKeyword = ref('')

// 状态
const status = ref('')

// 省份
const province = ref('')

// 商家列表
const merchants = ref([])

// 状态统计
const statusCount = ref(null)

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 选中的商家
const selectedMerchants = ref([])

// 省份选项
const provinces = ref([
  { value: '110000', label: '北京市' },
  { value: '120000', label: '天津市' },
  { value: '130000', label: '河北省' },
  { value: '140000', label: '山西省' },
  { value: '150000', label: '内蒙古自治区' },
  { value: '210000', label: '辽宁省' },
  { value: '220000', label: '吉林省' },
  { value: '230000', label: '黑龙江省' },
  { value: '310000', label: '上海市' },
  { value: '320000', label: '江苏省' },
  { value: '330000', label: '浙江省' },
  { value: '340000', label: '安徽省' },
  { value: '350000', label: '福建省' },
  { value: '360000', label: '江西省' },
  { value: '370000', label: '山东省' },
  { value: '410000', label: '河南省' },
  { value: '420000', label: '湖北省' },
  { value: '430000', label: '湖南省' },
  { value: '440000', label: '广东省' },
  { value: '450000', label: '广西壮族自治区' },
  { value: '460000', label: '海南省' },
  { value: '500000', label: '重庆市' },
  { value: '510000', label: '四川省' },
  { value: '520000', label: '贵州省' },
  { value: '530000', label: '云南省' },
  { value: '540000', label: '西藏自治区' },
  { value: '610000', label: '陕西省' },
  { value: '620000', label: '甘肃省' },
  { value: '630000', label: '青海省' },
  { value: '640000', label: '宁夏回族自治区' },
  { value: '650000', label: '新疆维吾尔自治区' }
])

// 审核对话框可见性
const auditDialogVisible = ref(false)

// 批量操作对话框可见性
const batchDialogVisible = ref(false)

// 批量操作类型
const batchType = ref('status')

// 批量状态
const batchStatus = ref('1')

// 审核表单引用
const auditFormRef = ref(null)

// 审核表单
const auditForm = reactive({
  merchantId: '',
  status: 1,
  auditRemark: ''
})

// 审核表单验证规则
const auditRules = {
  merchantId: [
    { required: true, message: '请输入商家ID', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  auditRemark: [
    { required: true, message: '请输入审核备注', trigger: 'blur' }
  ]
}

// 审核提交状态
const auditSubmitting = ref(false)

// 批量操作状态
const batchProcessing = ref(false)

// 加载状态
const loading = ref(false)

// 获取商家列表
const fetchMerchants = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value.trim() || undefined,
      status: status.value || undefined,
      province: province.value || undefined
    }

    const response = await admin.merchants(params)
    merchants.value = response.data.list || []
    statusCount.value = response.data.statusCount || null
    pagination.total = response.data.total || 0
  } catch (error) {
    console.error('获取商家列表失败:', error)
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    case 3: return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '正常'
    case 2: return '禁用'
    case 3: return '审核失败'
    default: return '未知状态'
  }
}

// 获取可用操作
const getAvailableActions = (merchant) => {
  const actions = []

  switch (merchant.status) {
    case 0: // 待审核
      actions.push(
        { text: '审核', type: 'primary', icon: 'Select', action: 'audit' }
      )
      break
    case 1: // 正常
      actions.push(
        { text: '禁用', type: 'danger', icon: 'CircleClose', action: 'disable' }
      )
      break
    case 2: // 禁用
      actions.push(
        { text: '启用', type: 'success', icon: 'CircleCheck', action: 'enable' }
      )
      break
    case 3: // 审核失败
      actions.push(
        { text: '重新审核', type: 'primary', icon: 'RefreshRight', action: 'audit' }
      )
      break
  }

  return actions
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedMerchants.value = selection
}

// 显示审核对话框
const showAuditDialog = (merchant) => {
  auditForm.merchantId = merchant && merchant.merchantId ? merchant.merchantId : ''
  auditDialogVisible.value = true
}

// 处理行内操作
const handleAction = async (action, merchant) => {
  if (!action || !merchant) return
  try {
    if (action.action === 'audit') {
      showAuditDialog(merchant)
      return
    }
    if (action.action === 'disable') {
      await admin.updateMerchantStatus({ merchantIds: [merchant.merchantId], status: 2 })
      merchant.status = 2
      ElMessage.success('已禁用')
      return
    }
    if (action.action === 'enable') {
      await admin.updateMerchantStatus({ merchantIds: [merchant.merchantId], status: 1 })
      merchant.status = 1
      ElMessage.success('已启用')
      return
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 重置审核表单
const resetAuditForm = () => {
  Object.assign(auditForm, {
    merchantId: '',
    status: 1,
    auditRemark: ''
  })

  if (auditFormRef.value) {
    auditFormRef.value.resetFields()
  }
}

// 提交审核
const submitAudit = async () => {
  const valid = await auditFormRef.value.validate().catch(() => false)
  if (!valid) return

  auditSubmitting.value = true
  try {
    await admin.auditMerchant({
      merchantId: auditForm.merchantId,
      status: auditForm.status,
      auditRemark: auditForm.auditRemark
    })

    // 更新列表中的商家状态
    const index = merchants.value.findIndex(m => m.merchantId === auditForm.merchantId)
    if (index !== -1) {
      const newStatus = auditForm.status === 2 ? 3 : 1
      merchants.value[index] = {
        ...merchants.value[index],
        status: newStatus,
        auditTime: new Date().toISOString()
      }
    }

    ElMessage.success('审核提交成功')
    auditDialogVisible.value = false
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  } finally {
    auditSubmitting.value = false
  }
}

// 显示批量操作对话框
const showBatchDialog = () => {
  batchDialogVisible.value = true
}

// 处理批量操作
const processBatch = async () => {
  if (selectedMerchants.value.length === 0) {
    ElMessage.warning('请先选择商家')
    return
  }

  batchProcessing.value = true
  try {
    if (batchType.value === 'status') {
      // 批量上下架
      const merchantIds = selectedMerchants.value.map(m => m.merchantId)
      const status = batchStatus.value === '1' ? 1 : 0

      await admin.updateMerchantStatus({
        merchantIds,
        status
      })

      // 更新列表中的商家状态
      merchants.value.forEach(merchant => {
        if (merchantIds.includes(merchant.merchantId)) {
          merchant.status = status
        }
      })

      ElMessage.success(`批量${batchStatus.value === '1' ? '上架' : '下架'}成功`)
    } else if (batchType.value === 'delete') {
      // 批量删除
      const merchantIds = selectedMerchants.value.map(m => m.merchantId)

      await ElMessageBox.confirm(`确定要删除选中的${merchantIds.length}个商家吗？`, '批量删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await admin.deleteMerchants({ merchantIds })

      // 从列表中移除
      merchants.value = merchants.value.filter(m => !merchantIds.includes(m.merchantId))

      ElMessage.success('批量删除成功')
    }
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
  fetchMerchants()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  fetchMerchants()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchMerchants()
})
</script>

<style scoped>
.merchant-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.merchant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.merchant-header h2 {
  font-size: 20px;
  color: #333;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.1);
}

.filter-item {
  flex: 1;
  min-width: 200px;
}

.search-input {
  width: 100%;
}

.filter-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
  white-space: nowrap;
}

.merchant-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.05);
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
}

.merchant-list {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.merchant-info {
  display: flex;
  align-items: flex-start;
  padding: 15px;
}

.merchant-logo {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 15px;
  flex-shrink: 0;
}

.merchant-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.merchant-details {
  flex: 1;
}

.shop-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
}

.contact-info {
  margin-bottom: 10px;
}

.contact-item {
  display: flex;
  margin-bottom: 5px;
}

.label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
  min-width: 60px;
}

.value {
  font-size: 14px;
  color: #333;
}

.shop-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    gap: 10px;
  }

  .filter-item {
    min-width: 100%;
    margin-bottom: 10px;
  }

  .merchant-stats {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
