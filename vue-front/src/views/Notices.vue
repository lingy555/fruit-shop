<template>
  <div class="page">
    <div class="page-header">
      <div class="page-title">商城公告</div>
    </div>

    <el-card shadow="never" class="page-card">
      <div v-loading="loading">
        <div v-if="notices.length > 0" class="notice-list">
          <div v-for="n in notices" :key="n.noticeId" class="notice-item">
            <div class="notice-title">{{ n.title }}</div>
            <div class="notice-meta">{{ formatDate(n.createTime) }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无公告" :image-size="200" />

        <div class="pagination-container" v-if="pagination.total > 0">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { home } from '@/api'
import { usePagedList } from '@/composables/usePagedList'

const {
  list: notices,
  loading,
  pagination,
  fetch,
  onSizeChange,
  onCurrentChange
} = usePagedList((params) => home.getNotices(params), { pageSize: 10 })

const load = () => {
  return fetch({})
}

const handleSizeChange = (size) => {
  return onSizeChange(size, {})
}

const handleCurrentChange = (page) => {
  return onCurrentChange(page, {})
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return String(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.page-card {
  border-radius: 12px;
}

.notice-list {
  display: flex;
  flex-direction: column;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  font-size: 16px;
  color: #333;
}

.notice-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
