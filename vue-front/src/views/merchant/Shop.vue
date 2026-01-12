
<template>
  <div class="shop-container">
    <div class="shop-header">
      <h2>店铺管理</h2>
      <div class="header-actions">
        <el-button 
          type="primary" 
          @click="showEditDialog"
        >
          编辑店铺信息
        </el-button>
        <el-button 
          :type="shopInfo.status === 1 ? 'warning' : 'success'"
          @click="toggleShopStatus"
        >
          {{ shopInfo.status === 1 ? '暂停营业' : '开始营业' }}
        </el-button>
      </div>
    </div>

    <div class="shop-content">
      <!-- 店铺基本信息 -->
      <div class="info-section">
        <div class="section-title">基本信息</div>

        <div class="info-list">
          <div class="info-item">
            <span class="info-label">店铺名称：</span>
            <span class="info-value">{{ shopInfo.shopName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">店铺Logo：</span>
            <div class="logo-preview">
              <img :src="shopInfo.logo" :alt="shopInfo.shopName" />
            </div>
          </div>
          <div class="info-item">
            <span class="info-label">店铺Banner：</span>
            <div class="banner-preview">
              <img :src="shopInfo.banner" :alt="shopInfo.shopName" />
            </div>
          </div>
          <div class="info-item">
            <span class="info-label">店铺简介：</span>
            <span class="info-value">{{ shopInfo.description }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">营业时间：</span>
            <span class="info-value">{{ shopInfo.businessHours || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">店铺状态：</span>
            <el-tag :type="shopInfo.status === 1 ? 'success' : 'danger'">
              {{ shopInfo.status === 1 ? '营业中' : '已暂停' }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 店铺统计数据 -->
      <div class="stats-section">
        <div class="section-title">数据统计</div>

        <div class="date-filter">
          <el-radio-group v-model="dateType" @change="fetchStatistics">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="year">本年</el-radio-button>
          </el-radio-group>
        </div>

        <div class="stats-cards" v-loading="statsLoading">
          <div class="stats-card">
            <div class="card-title">订单数据</div>
            <div class="card-content">
              <div class="data-item">
                <span class="data-label">订单数：</span>
                <span class="data-value">{{ statisticsData.orderCount || 0 }}</span>
              </div>
              <div class="data-item">
                <span class="data-label">成交额：</span>
                <span class="data-value">¥{{ statisticsData.orderAmount || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="stats-card">
            <div class="card-title">访客数据</div>
            <div class="card-content">
              <div class="data-item">
                <span class="data-label">访问量：</span>
                <span class="data-value">{{ statisticsData.visitCount || 0 }}</span>
              </div>
              <div class="data-item">
                <span class="data-label">新增粉丝：</span>
                <span class="data-value">{{ statisticsData.newFanCount || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="stats-card">
            <div class="card-title">商品数据</div>
            <div class="card-content">
              <div class="data-item">
                <span class="data-label">商品总数：</span>
                <span class="data-value">{{ statisticsData.productCount || 0 }}</span>
              </div>
              <div class="data-item">
                <span class="data-label">低库存商品：</span>
                <span class="data-value">{{ statisticsData.lowStockCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 店铺编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑店铺信息"
      width="600px"
      @close="resetEditForm"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="editForm.shopName" placeholder="请输入店铺名称" />
        </el-form-item>

        <el-form-item label="店铺Logo" prop="logo">
          <el-upload
            ref="logoUploadRef"
            v-model:file-list="editForm.logoList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeLogoUpload"
            :on-success="handleLogoSuccess"
            :limit="1"
            list-type="picture-card"
            accept="image/*"
          >
            <template #trigger>
              <el-button type="primary">选择图片</el-button>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="店铺Banner" prop="banner">
          <el-upload
            ref="bannerUploadRef"
            v-model:file-list="editForm.bannerList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeBannerUpload"
            :on-success="handleBannerSuccess"
            :limit="1"
            list-type="picture-card"
            accept="image/*"
          >
            <template #trigger>
              <el-button type="primary">选择图片</el-button>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="店铺简介" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入店铺简介"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="editForm.businessHours" placeholder="请输入营业时间，如：08:00-22:00" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveShopInfo" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { merchantShop as shop } from '@/api'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'

// 店铺信息
const shopInfo = ref({})

// 统计数据
const statisticsData = ref({})

// 统计日期类型
const dateType = ref('today')

// 编辑对话框可见性
const editDialogVisible = ref(false)

// 编辑表单引用
const editFormRef = ref(null)

// Logo上传引用
const logoUploadRef = ref(null)

// Banner上传引用
const bannerUploadRef = ref(null)

// 上传地址
const uploadUrl = '/api/upload'

const uploadHeaders = computed(() => {
  const merchantToken = Cookies.get('merchantToken')
  return merchantToken ? { Authorization: `Bearer ${merchantToken}` } : {}
})

// 编辑表单
const editForm = reactive({
  shopName: '',
  logoList: [],
  bannerList: [],
  description: '',
  businessHours: '',
  contactPhone: ''
})

// 编辑表单验证规则
const editRules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' }
  ],
  logo: [
    { required: true, message: '请上传店铺Logo', trigger: 'change' }
  ],
  banner: [
    { required: true, message: '请上传店铺Banner', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入店铺简介', trigger: 'blur' }
  ],
  businessHours: [
    { required: true, message: '请输入营业时间', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 保存状态
const saving = ref(false)

// 统计加载状态
const statsLoading = ref(false)

// 获取店铺信息
const fetchShopInfo = async () => {
  try {
    const response = await shop.info()
    shopInfo.value = response.data || {}
  } catch (error) {
    console.error('获取店铺信息失败:', error)
    ElMessage.error('获取店铺信息失败')
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  statsLoading.value = true
  try {
    const response = await shop.statistics({ dateType: dateType.value })
    const data = response.data || {}

    // 统一摊平，适配页面直接使用 statisticsData.orderCount 等字段
    statisticsData.value = {
      ...(data.todayData || {}),
      ...(data.totalData || {}),
      ...(data.pendingCount || {})
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// 显示编辑对话框
const showEditDialog = () => {
  // 填充表单
  editForm.shopName = shopInfo.value.shopName || ''
  editForm.description = shopInfo.value.description || ''
  editForm.businessHours = shopInfo.value.businessHours || ''
  editForm.contactPhone = shopInfo.value.contactPhone || ''

  // 设置图片
  if (shopInfo.value.logo) {
    editForm.logoList = [{ name: 'logo', url: shopInfo.value.logo }]
  }

  if (shopInfo.value.banner) {
    editForm.bannerList = [{ name: 'banner', url: shopInfo.value.banner }]
  }

  editDialogVisible.value = true
}

// 重置编辑表单
const resetEditForm = () => {
  Object.assign(editForm, {
    shopName: '',
    logoList: [],
    bannerList: [],
    description: '',
    businessHours: '',
    contactPhone: ''
  })

  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

// Logo上传前校验
const beforeLogoUpload = (file) => {
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

// Banner上传前校验
const beforeBannerUpload = (file) => {
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

// Logo上传成功
const handleLogoSuccess = (response) => {
  const url = response?.data?.url || response?.url
  if (!url) {
    ElMessage.error('Logo上传失败：未返回图片地址')
    return
  }
  editForm.logoList = [{ name: 'logo', url }]
  ElMessage.success('Logo上传成功')
}

// Banner上传成功
const handleBannerSuccess = (response) => {
  const url = response?.data?.url || response?.url
  if (!url) {
    ElMessage.error('Banner上传失败：未返回图片地址')
    return
  }
  editForm.bannerList = [{ name: 'banner', url }]
  ElMessage.success('Banner上传成功')
}

// 保存店铺信息
const saveShopInfo = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const logo = editForm.logoList.length > 0 ? editForm.logoList[0].url : shopInfo.value.logo
    const banner = editForm.bannerList.length > 0 ? editForm.bannerList[0].url : shopInfo.value.banner

    await shop.update({
      shopName: editForm.shopName,
      logo,
      banner,
      description: editForm.description,
      businessHours: editForm.businessHours,
      contactPhone: editForm.contactPhone
    })

    // 更新本地数据
    shopInfo.value = {
      ...shopInfo.value,
      shopName: editForm.shopName,
      logo,
      banner,
      description: editForm.description,
      businessHours: editForm.businessHours,
      contactPhone: editForm.contactPhone
    }

    ElMessage.success('店铺信息更新成功')
    editDialogVisible.value = false
  } catch (error) {
    console.error('保存店铺信息失败:', error)
    ElMessage.error('保存店铺信息失败')
  } finally {
    saving.value = false
  }
}

// 切换店铺状态
const toggleShopStatus = async () => {
  try {
    const newStatus = shopInfo.value.status === 1 ? 2 : 1
    await shop.status({ status: newStatus })

    // 更新本地数据
    shopInfo.value.status = newStatus

    ElMessage.success(`店铺已${newStatus === 1 ? '开始营业' : '暂停营业'}`)
  } catch (error) {
    console.error('切换店铺状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 初始化
onMounted(() => {
  fetchShopInfo()
  fetchStatistics()
})
</script>

<style scoped>
.shop-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.shop-header h2 {
  font-size: 20px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.shop-content {
  display: flex;
  gap: 30px;
}

.info-section, .stats-section {
  flex: 1;
}

.section-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
  width: 120px;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.logo-preview, .banner-preview {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.logo-preview img, .banner-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-preview {
  width: 160px;
  height: 80px;
}

.date-filter {
  margin-bottom: 20px;
}

.stats-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.stats-card {
  flex: 1;
  min-width: 280px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 15px;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.data-item {
  display: flex;
  justify-content: space-between;
}

.data-label {
  font-size: 14px;
  color: #666;
}

.data-value {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
}

@media (max-width: 768px) {
  .shop-content {
    flex-direction: column;
  }

  .stats-cards {
    flex-direction: column;
  }
}
</style>
