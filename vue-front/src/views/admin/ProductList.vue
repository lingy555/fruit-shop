<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="page-title">商品列表</div>

      <div class="toolbar">
        <el-input v-model="keyword" placeholder="商品名称" clearable style="width: 260px" @keyup.enter="fetch" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px" @change="fetch">
          <el-option label="已下架" :value="0" />
          <el-option label="已上架" :value="1" />
          <el-option label="待审核" :value="2" />
          <el-option label="审核失败" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetch">查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="productId" label="ID" width="90" />
        <el-table-column prop="productName" label="商品" min-width="220" />
        <el-table-column prop="categoryName" label="分类" width="140" />
        <el-table-column prop="shopName" label="店铺" width="160" />
        <el-table-column prop="price" label="价格" width="120" />
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="statusText" label="状态" width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 2"
              size="small"
              type="primary"
              @click="openAudit(row)"
            >
              审核
            </el-button>
            <el-button
              v-if="row.status === 1"
              size="small"
              @click="forceOffline(row)"
            >
              强制下架
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="remove(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @current-change="fetch"
          @size-change="onSize"
        />
      </div>
    </el-card>

    <el-dialog v-model="auditDialogVisible" title="商品审核" width="560px" @close="resetAuditForm">
      <el-form :model="auditForm" label-width="110px">
        <el-form-item label="商品ID">
          <el-input v-model="auditForm.productId" disabled />
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :value="1">通过并上架</el-radio>
            <el-radio :value="3">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="4" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="submitAudit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { admin } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const status = ref(null)

const auditDialogVisible = ref(false)
const auditing = ref(false)

const auditForm = reactive({
  productId: null,
  status: 1,
  auditRemark: ''
})

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.productList({ page: page.value, pageSize: pageSize.value, keyword: keyword.value || undefined, status: status.value ?? undefined })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const onSize = (s) => {
  pageSize.value = s
  page.value = 1
  fetch()
}

const resetAuditForm = () => {
  Object.assign(auditForm, { productId: null, status: 1, auditRemark: '' })
}

const openAudit = (row) => {
  resetAuditForm()
  auditForm.productId = row.productId
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (!auditForm.productId) {
    ElMessage.error('缺少商品ID')
    return
  }
  auditing.value = true
  try {
    await admin.productAudit({
      productId: auditForm.productId,
      status: auditForm.status,
      auditRemark: auditForm.auditRemark || null
    })
    ElMessage.success('已审核')
    auditDialogVisible.value = false
    fetch()
  } finally {
    auditing.value = false
  }
}

const forceOffline = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入下架原因（可选）', '强制下架', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '可选'
    })
    await admin.productForceOffline({ productId: row.productId, reason: value || null })
    ElMessage.success('已下架')
    fetch()
  } catch (e) {
    // ignore
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.productName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await admin.productDelete(row.productId)
    ElMessage.success('已删除')
    fetch()
  } catch (e) {
    // ignore
  }
}

onMounted(fetch)
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.page-card {
  border-radius: 12px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
