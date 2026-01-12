<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="page-title">订单列表</div>

      <div class="toolbar">
        <el-input v-model="orderNo" placeholder="订单号" clearable style="width: 260px" @keyup.enter="fetch" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px" @change="fetch">
          <el-option label="待付款" :value="1" />
          <el-option label="待发货" :value="2" />
          <el-option label="待收货" :value="3" />
          <el-option label="待评价" :value="4" />
          <el-option label="已完成" :value="5" />
          <el-option label="已取消" :value="6" />
        </el-select>
        <el-button type="primary" @click="fetch">查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderId" label="ID" width="90" />
        <el-table-column prop="orderNo" label="订单号" min-width="200" />
        <el-table-column prop="username" label="用户" width="140" />
        <el-table-column prop="shopName" label="店铺" width="160" />
        <el-table-column prop="statusText" label="状态" width="120" />
        <el-table-column prop="actualAmount" label="实付" width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { admin } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const orderNo = ref('')
const status = ref(null)

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.orderList({
      page: page.value,
      pageSize: pageSize.value,
      orderNo: orderNo.value || undefined,
      status: status.value ?? undefined
    })
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
