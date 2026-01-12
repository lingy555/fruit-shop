
<template>
  <div class="admin-container">
    <div class="admin-header">
      <h2>管理员管理</h2>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <div class="filter-item">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名/手机号"
          @keyup.enter="fetchAdmins"
          clearable
          class="search-input"
        >
          <template #append>
            <el-button icon="Search" @click="fetchAdmins">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-item">
        <span class="filter-label">角色：</span>
        <el-select 
          v-model="roleId" 
          @change="fetchAdmins" 
          placeholder="全部分色"
          clearable
        >
          <el-option 
            v-for="role in roles" 
            :key="role.roleId"
            :label="role.roleName"
            :value="role.roleId"
          />
        </el-select>
      </div>

      <div class="filter-item">
        <span class="filter-label">状态：</span>
        <el-select 
          v-model="status" 
          @change="fetchAdmins" 
          placeholder="全部状态"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="启用" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
      </div>

      <div class="filter-actions">
        <el-button type="primary" icon="Plus" @click="showAddDialog">添加管理员</el-button>
      </div>
    </div>

    <!-- 管理员列表 -->
    <div class="admin-list" v-loading="loading">
      <el-table
        :data="admins"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar" fit="cover">
              {{ row.nickname.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>

        <el-table-column label="用户名" prop="username" width="120" />

        <el-table-column label="昵称" prop="nickname" width="120" />

        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.roleId === 1 ? 'danger' : 'primary'">
              {{ row.roleName }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="手机号" prop="phone" width="120" />

        <el-table-column label="邮箱" prop="email" width="180" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="最后登录" width="150">
          <template #default="{ row }">
            {{ formatDate(row.lastLoginTime) }}
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              size="small"
              @click="showEditDialog(row)"
            >
              编辑
            </el-button>

            <el-button 
              link 
              type="success" 
              size="small"
              @click="resetPassword(row)"
            >
              重置密码
            </el-button>

            <el-button 
              v-if="row.status === 1"
              link 
              type="danger" 
              size="small"
              @click="updateStatus(row, 0)"
            >
              禁用
            </el-button>

            <el-button 
              v-if="row.status === 0"
              link 
              type="success" 
              size="small"
              @click="updateStatus(row, 1)"
            >
              启用
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑管理员对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑管理员' : '添加管理员'"
      width="600px"
      @close="resetEditForm"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="角色" prop="roleId">
          <el-select 
            v-model="editForm.roleId" 
            placeholder="请选择角色"
          >
            <el-option 
              v-for="role in roles" 
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAdmin" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { admin } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索关键词
const searchKeyword = ref('')

// 角色ID
const roleId = ref('')

// 状态
const status = ref('')

// 管理员列表
const admins = ref([])

// 角色列表
const roles = ref([])

// 选中的管理员
const selectedAdmins = ref([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 编辑对话框可见性
const editDialogVisible = ref(false)

// 是否编辑
const isEdit = ref(false)

// 当前编辑的管理员ID
const editAdminId = ref(0)

// 编辑表单引用
const editFormRef = ref(null)

// 编辑表单
const editForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  roleId: null,
  status: 1
})

// 编辑表单验证规则
const editRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 保存状态
const saving = ref(false)

// 加载状态
const loading = ref(false)

// 获取管理员列表
const fetchAdmins = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value.trim() || undefined,
      roleId: roleId.value || undefined,
      status: status.value || undefined
    }

    const response = await admin.list(params)
    admins.value = response.data.list || []
    pagination.total = response.data.total || 0
  } catch (error) {
    console.error('获取管理员列表失败:', error)
    ElMessage.error('获取管理员列表失败')
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const fetchRoles = async () => {
  try {
    const response = await admin.roles()
    roles.value = response.data || []
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  }
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedAdmins.value = selection
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  editAdminId.value = 0

  // 重置表单
  Object.assign(editForm, {
    username: '',
    nickname: '',
    phone: '',
    email: '',
    roleId: null,
    status: 1
  })

  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }

  editDialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (admin) => {
  isEdit.value = true
  editAdminId.value = admin.adminId

  // 填充表单
  Object.assign(editForm, {
    username: admin.username,
    nickname: admin.nickname,
    phone: admin.phone,
    email: admin.email,
    roleId: admin.roleId,
    status: admin.status
  })

  editDialogVisible.value = true
}

// 重置编辑表单
const resetEditForm = () => {
  Object.assign(editForm, {
    username: '',
    nickname: '',
    phone: '',
    email: '',
    roleId: null,
    status: 1
  })

  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

// 保存管理员
const saveAdmin = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      // 编辑管理员
      await admin.update(editAdminId.value, {
        nickname: editForm.nickname,
        phone: editForm.phone,
        email: editForm.email,
        roleId: editForm.roleId,
        status: editForm.status
      })

      ElMessage.success('管理员信息更新成功')
      await fetchAdmins()
    } else {
      // 添加管理员
      await admin.add({
        username: editForm.username,
        password: '123456', // 默认密码，需要管理员首次登录后修改
        nickname: editForm.nickname,
        phone: editForm.phone,
        email: editForm.email,
        roleId: editForm.roleId,
        status: editForm.status
      })

      ElMessage.success('管理员添加成功')
      await fetchAdmins()
    }

    editDialogVisible.value = false
  } catch (error) {
    console.error('保存管理员失败:', error)
    ElMessage.error('保存管理员失败')
  } finally {
    saving.value = false
  }
}

// 重置密码
const resetPassword = async (admin) => {
  try {
    await ElMessageBox.confirm('确定要重置该管理员的密码吗？重置后密码为123456', '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await admin.resetPassword(admin.adminId)
    ElMessage.success('密码重置成功')
    await fetchAdmins()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

// 更新状态
const updateStatus = async (row, status) => {
  try {
    await admin.update(row.adminId, {
      nickname: row.nickname,
      phone: row.phone,
      email: row.email,
      roleId: row.roleId,
      status
    })

    await fetchAdmins()

    ElMessage.success(`管理员已${status === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchAdmins()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  fetchAdmins()
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchAdmins()
  fetchRoles()
})
</script>

<style scoped>
.admin-container {
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
}

.admin-header {
  margin-bottom: 20px;
}

.admin-header h2 {
  font-size: 20px;
  color: #333;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
}

.filter-item {
  display: flex;
  align-items: center;
}

.search-input {
  width: 300px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
  white-space: nowrap;
}

.filter-actions {
  margin-left: auto;
}

.admin-list {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
