<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="page-title">角色权限</div>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="roleId" label="ID" width="90" />
        <el-table-column prop="roleName" label="角色名称" min-width="220" />
        <el-table-column prop="remark" label="备注" min-width="220" />
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

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.roleList({ page: page.value, pageSize: pageSize.value })
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
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
