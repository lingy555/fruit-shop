<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="header">
        <div class="page-title">公告管理</div>
        <el-button type="primary" @click="openAdd">发布公告</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="noticeId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="260" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="sort" label="排序" width="100" />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '更新公告' : '发布公告'" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="类型"><el-input-number v-model="form.type" :min="1" /></el-form-item>
        <el-form-item label="状态"><el-input-number v-model="form.status" :min="0" :max="1" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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

const form = reactive({ title: '', content: '', type: 1, status: 1, sort: 0 })

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.noticeList({ page: page.value, pageSize: pageSize.value })
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

const openAdd = () => {
  editingId.value = null
  Object.assign(form, { title: '', content: '', type: 1, status: 1, sort: 0 })
  dialogVisible.value = true
}

const openEdit = async (row) => {
  editingId.value = row.noticeId
  const res = await admin.noticeDetail(row.noticeId)
  Object.assign(form, {
    title: res.data.title,
    content: res.data.content,
    type: res.data.type,
    status: res.data.status,
    sort: res.data.sort
  })
  dialogVisible.value = true
}

const submit = async () => {
  submitting.value = true
  try {
    if (editingId.value) {
      await admin.noticeUpdate(editingId.value, { ...form })
    } else {
      await admin.noticeAdd({ ...form })
    }
    ElMessage.success('已提交')
    dialogVisible.value = false
    fetch()
  } finally {
    submitting.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await admin.noticeDelete(row.noticeId)
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
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
