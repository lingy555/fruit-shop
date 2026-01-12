<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="page-title">用户列表</div>

      <div class="toolbar">
        <el-input v-model="keyword" placeholder="用户名/手机号" clearable style="width: 260px" @keyup.enter="fetch" />
        <el-button type="primary" @click="fetch">查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="levelName" label="等级" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
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
const keyword = ref('')

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.userList({ page: page.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
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
