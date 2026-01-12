<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="header">
        <div class="page-title">优惠券管理</div>
        <el-button type="primary" @click="openAdd">新增优惠券</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="couponId" label="ID" width="80" />
        <el-table-column prop="couponName" label="名称" min-width="220" />
        <el-table-column prop="couponType" label="类型" width="100" />
        <el-table-column prop="discountType" label="折扣类型" width="100" />
        <el-table-column prop="discountValue" label="折扣值" width="120" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑优惠券' : '新增优惠券'" width="720px" @close="resetForm">
      <el-form :model="form" label-width="110px">
        <el-form-item label="名称"><el-input v-model="form.couponName" /></el-form-item>
        <el-form-item label="类型"><el-input-number v-model="form.couponType" :min="0" /></el-form-item>
        <el-form-item label="折扣类型"><el-input-number v-model="form.discountType" :min="0" /></el-form-item>
        <el-form-item label="折扣值"><el-input-number v-model="form.discountValue" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="最低金额"><el-input-number v-model="form.minAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="总数量"><el-input-number v-model="form.totalCount" :min="0" /></el-form-item>
        <el-form-item label="每人限领"><el-input-number v-model="form.limitPerUser" :min="1" /></el-form-item>
        <el-form-item label="有效天数"><el-input-number v-model="form.validDays" :min="0" /></el-form-item>
        <el-form-item label="开始时间"><el-input v-model="form.startTime" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-input v-model="form.endTime" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        <el-form-item label="状态" v-if="editingId">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
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

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)

const form = reactive({
  couponName: '',
  couponType: 1,
  discountType: 1,
  discountValue: 0,
  minAmount: 0,
  totalCount: 0,
  limitPerUser: 1,
  validDays: 0,
  startTime: '',
  endTime: '',
  status: 1,
  description: ''
})

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.couponList({ page: page.value, pageSize: pageSize.value })
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

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    couponName: '',
    couponType: 1,
    discountType: 1,
    discountValue: 0,
    minAmount: 0,
    totalCount: 0,
    limitPerUser: 1,
    validDays: 0,
    startTime: '',
    endTime: '',
    status: 1,
    description: ''
  })
}

const openAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (row) => {
  editingId.value = row.couponId
  const res = await admin.couponDetail(row.couponId)
  Object.assign(form, {
    couponName: res.data.couponName,
    couponType: res.data.couponType,
    discountType: res.data.discountType,
    discountValue: res.data.discountValue,
    minAmount: res.data.minAmount,
    totalCount: res.data.totalCount,
    limitPerUser: res.data.limitPerUser,
    validDays: res.data.validDays,
    startTime: res.data.startTime || '',
    endTime: res.data.endTime || '',
    status: res.data.status,
    description: res.data.description || ''
  })
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.couponName || !String(form.couponName).trim()) {
    ElMessage.error('请输入优惠券名称')
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      const updatePayload = {
        couponName: form.couponName,
        couponType: form.couponType,
        discountType: form.discountType,
        discountValue: form.discountValue,
        minAmount: form.minAmount,
        totalCount: form.totalCount,
        limitPerUser: form.limitPerUser,
        validDays: form.validDays,
        startTime: form.startTime,
        endTime: form.endTime,
        status: form.status,
        description: form.description
      }
      await admin.couponUpdate(editingId.value, updatePayload)
      ElMessage.success('已更新')
    } else {
      const addPayload = {
        couponName: form.couponName,
        couponType: form.couponType,
        discountType: form.discountType,
        discountValue: form.discountValue,
        minAmount: form.minAmount,
        totalCount: form.totalCount,
        limitPerUser: form.limitPerUser,
        validDays: form.validDays,
        startTime: form.startTime,
        endTime: form.endTime,
        description: form.description
      }
      await admin.couponAdd(addPayload)
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    fetch()
  } finally {
    submitting.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除优惠券「${row.couponName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await admin.couponDelete(row.couponId)
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

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
