<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="header">
        <div class="page-title">轮播图管理</div>
        <el-button type="primary" @click="openAdd">新增</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="bannerId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width: 80px; height: 40px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="position" label="位置" width="120" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="startTime" label="开始时间" min-width="160" />
        <el-table-column prop="endTime" label="结束时间" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑轮播图' : '新增轮播图'" width="560px" @close="resetForm">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="图片">
          <div style="display: flex; align-items: center; gap: 12px; width: 100%;">
            <el-upload
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :limit="1"
              accept="image/*"
            >
              <el-button>上传</el-button>
            </el-upload>
            <el-input v-model="form.image" placeholder="也可以手动粘贴图片URL" />
          </div>
        </el-form-item>
        <el-form-item label="位置"><el-input v-model="form.position" placeholder="home" /></el-form-item>
        <el-form-item label="链接类型"><el-input v-model="form.linkType" placeholder="product/category/url" /></el-form-item>
        <el-form-item label="链接值"><el-input v-model="form.linkValue" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-input v-model="form.startTime" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-input v-model="form.endTime" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
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
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)

const uploadUrl = '/api/upload'

const form = reactive({
  title: '',
  image: '',
  position: 'home',
  linkType: 'url',
  linkValue: '',
  sort: 0,
  status: 1,
  startTime: '',
  endTime: ''
})

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.bannerList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    title: '',
    image: '',
    position: 'home',
    linkType: 'url',
    linkValue: '',
    sort: 0,
    status: 1,
    startTime: '',
    endTime: ''
  })
}

const openAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.bannerId
  Object.assign(form, {
    title: row.title || '',
    image: row.image || '',
    position: row.position || 'home',
    linkType: row.linkType || 'url',
    linkValue: row.linkValue || '',
    sort: row.sort ?? 0,
    status: row.status ?? 1,
    startTime: row.startTime || '',
    endTime: row.endTime || ''
  })
  dialogVisible.value = true
}

const handleUploadSuccess = (response) => {
  const url = response?.data?.url || response?.url
  if (!url) {
    ElMessage.error('上传失败：未返回图片地址')
    return
  }
  form.image = url
  ElMessage.success('上传成功')
}

const submit = async () => {
  submitting.value = true
  try {
    if (!form.title || !String(form.title).trim()) {
      ElMessage.error('请输入标题')
      return
    }
    if (!form.image || !String(form.image).trim()) {
      ElMessage.error('请填写/上传图片')
      return
    }
    if (!form.position || !String(form.position).trim()) {
      ElMessage.error('请输入位置')
      return
    }
    if (!form.linkType || !String(form.linkType).trim()) {
      ElMessage.error('请输入链接类型')
      return
    }

    if (editingId.value) {
      await admin.bannerUpdate(editingId.value, { ...form })
      ElMessage.success('已更新')
    } else {
      const addPayload = {
        title: form.title,
        image: form.image,
        position: form.position,
        linkType: form.linkType,
        linkValue: form.linkValue,
        sort: form.sort,
        startTime: form.startTime,
        endTime: form.endTime
      }
      await admin.bannerAdd(addPayload)
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
    await ElMessageBox.confirm(`确定删除轮播图「${row.title || row.bannerId}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await admin.bannerDelete(row.bannerId)
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
</style>
