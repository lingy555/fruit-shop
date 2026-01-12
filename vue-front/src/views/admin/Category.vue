<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-card">
      <div class="header">
        <div class="page-title">分类管理</div>
        <el-button type="primary" @click="openAddRoot">新增一级分类</el-button>
      </div>
      <el-tree
        v-loading="loading"
        :data="tree"
        node-key="categoryId"
        :props="{ label: 'categoryName', children: 'children' }"
        default-expand-all

        :expand-on-click-node="false"
      >
        <template #default="{ data }">
          <div class="node-row">
            <div class="node-left">
              <span class="node-title">{{ data.categoryName }}</span>
              <el-tag size="small" :type="data.status === 1 ? 'success' : 'info'" style="margin-left: 8px;">
                {{ data.status === 1 ? '启用' : '禁用' }}
              </el-tag>
              <span class="node-meta">ID: {{ data.categoryId }} | 排序: {{ data.sort }} | 商品: {{ data.productCount || 0 }}</span>
            </div>
            <div class="node-actions">
              <el-button size="small" type="primary" @click.stop="openEdit(data)">编辑</el-button>
              <el-button size="small" type="danger" @click.stop="remove(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="560px" @close="resetForm">
      <el-form :model="form" label-width="110px">
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="可选" />
        </el-form-item>
        <el-form-item label="父级分类ID">
          <el-input v-model="form.parentId" disabled />
        </el-form-item>
        <el-form-item label="层级">
          <el-input v-model="form.level" disabled />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
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
const tree = ref([])

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)

const form = reactive({
  categoryName: '',
  icon: '',
  parentId: 0,
  level: 1,
  sort: 0,
  status: 1
})

const fetch = async () => {
  loading.value = true
  try {
    const res = await admin.categoryTree()
    const raw = res.data || []
    tree.value = raw
      .filter(n => Number(n.parentId ?? 0) === 0)
      .map(n => ({ ...n, children: [] }))
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, { categoryName: '', icon: '', parentId: 0, level: 1, sort: 0, status: 1 })
}

const openAddRoot = () => {
  resetForm()
  form.parentId = 0
  form.level = 1
  dialogVisible.value = true
}

const openEdit = (node) => {
  editingId.value = node.categoryId
  Object.assign(form, {
    categoryName: node.categoryName,
    icon: node.icon || '',
    parentId: node.parentId ?? 0,
    level: node.level ?? 1,
    sort: node.sort ?? 0,
    status: node.status ?? 1
  })
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.categoryName || !String(form.categoryName).trim()) {
    ElMessage.error('请输入分类名称')
    return
  }

  submitting.value = true
  try {
    const payload = {
      categoryName: String(form.categoryName).trim(),
      icon: form.icon || null,
      parentId: 0,
      level: 1,
      sort: Number(form.sort) || 0,
      status: Number(form.status)
    }

    if (editingId.value) {
      await admin.categoryUpdate(editingId.value, payload)
      ElMessage.success('已更新')
    } else {
      await admin.categoryAdd(payload)
      ElMessage.success('已新增')
    }

    dialogVisible.value = false
    fetch()
  } finally {
    submitting.value = false
  }
}

const remove = async (node) => {
  try {
    await ElMessageBox.confirm(`确定删除分类「${node.categoryName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await admin.categoryDelete(node.categoryId)
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

.node-row {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.node-left {
  display: flex;
  align-items: center;
  overflow: hidden;
}

.node-title {
  font-weight: 500;
}

.node-meta {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.node-actions {
  flex: 0 0 auto;
}
</style>
