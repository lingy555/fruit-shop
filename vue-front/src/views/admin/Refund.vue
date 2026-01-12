<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="page-title">退款处理</div>

      <div class="toolbar">
        <el-input v-model="refundNo" placeholder="退款单号" clearable style="width: 260px" @keyup.enter="fetch" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px" @change="fetch">
          <el-option label="待处理" :value="1" />
          <el-option label="已同意" :value="2" />
          <el-option label="已拒绝" :value="3" />
          <el-option label="退款中" :value="4" />
          <el-option label="已完成" :value="5" />
          <el-option label="已关闭" :value="6" />
        </el-select>
        <el-button type="primary" @click="fetch">查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="refundId" label="ID" width="90" />
        <el-table-column prop="refundNo" label="退款单号" min-width="220" />
        <el-table-column prop="orderNo" label="订单号" min-width="220" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="shopName" label="店铺" width="160" />
        <el-table-column prop="typeText" label="类型" width="120" />
        <el-table-column prop="refundAmount" label="金额" width="120" />
        <el-table-column prop="statusText" label="状态" width="120" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openArbitrate(row)">仲裁</el-button>
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

    <el-dialog v-model="dialogVisible" title="仲裁退款" width="520px">
      <el-form :model="arb" label-width="90px">
        <el-form-item label="结果">
          <el-radio-group v-model="arb.result">
            <el-radio :label="1">支持退款</el-radio>
            <el-radio :label="0">驳回退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="arb.reason" type="textarea" :rows="3" />
        </el-form-item>
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
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const refundNo = ref('')
const status = ref(null)

const dialogVisible = ref(false)
const submitting = ref(false)
const arb = reactive({ refundId: null, result: 1, reason: '' })

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.refundList({ page: page.value, pageSize: pageSize.value, refundNo: refundNo.value || undefined, status: status.value ?? undefined })
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

const openArbitrate = (row) => {
  arb.refundId = row.refundId
  arb.result = 1
  arb.reason = ''
  dialogVisible.value = true
}

const submit = async () => {
  if (!arb.refundId) return
  submitting.value = true
  try {
    await admin.refundArbitrate({ refundId: arb.refundId, result: arb.result, reason: arb.reason })
    ElMessage.success('已提交')
    dialogVisible.value = false
    fetch()
  } finally {
    submitting.value = false
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
