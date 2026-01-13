
<template>
  <div class="address-container">
    <div class="address-header">
      <h2>收货地址</h2>
      <el-button type="primary" @click="showAddressDialog">新增地址</el-button>
    </div>

    <div class="address-list" v-if="addresses.length > 0">
      <div 
        v-for="address in addresses" 
        :key="address.addressId"
        class="address-item"
        :class="{ default: address.isDefault }"
      >
        <div class="address-info">
          <div class="address-name-phone">
            <span class="name">{{ address.receiverName }}</span>
            <span class="phone">{{ address.receiverPhone }}</span>
            <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
          </div>
          <div class="address-detail">
            {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
          </div>
        </div>

        <div class="address-actions">
          <el-button 
            text 
            type="primary"
            @click="editAddress(address)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="!address.isDefault" 
            text 
            type="success"
            @click="setDefaultAddress(address.addressId)"
          >
            设为默认
          </el-button>
          <el-button 
            text 
            type="danger"
            @click="deleteAddress(address.addressId)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <el-empty 
      v-else
      description="暂无收货地址，请添加收货地址"
      :image-size="200"
    >
      <el-button type="primary" @click="showAddressDialog">添加收货地址</el-button>
    </el-empty>

    <!-- 地址对话框 -->
    <el-dialog
      v-model="addressDialogVisible"
      :title="isEdit ? '编辑地址' : '新增地址'"
      width="500px"
      @close="resetAddressForm"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择省市区"
            clearable
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="addressForm.detail"
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址信息，如道路、门牌号、小区、楼栋号、单元等信息"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="addressSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { address } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 地址列表
const addresses = ref([])
const addressLoading = ref(false)

// 地址对话框可见性
const addressDialogVisible = ref(false)

// 是否编辑地址
const isEdit = ref(false)

// 当前编辑的地址ID
const editAddressId = ref(0)

// 地址表单引用
const addressFormRef = ref(null)

// 地址表单
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detail: '',
  isDefault: false
})

// 地址表单验证规则
const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择地区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 地区选项（这里应该从API获取）
const regionOptions = ref([
  {
    value: '110000',
    label: '北京市',
    children: [
      {
        value: '110100',
        label: '北京市',
        children: [
          { value: '110101', label: '东城区' },
          { value: '110102', label: '西城区' },
          { value: '110105', label: '朝阳区' },
          // 其他区...
        ]
      }
    ]
  },
  // 其他省市...
])

// 地址保存状态
const addressSaving = ref(false)

// 获取地址列表
const fetchAddresses = async () => {
  addressLoading.value = true
  try {
    const response = await address.getList()
    addresses.value = response.data || []
  } catch (error) {
    console.error('获取收货地址失败:', error)
    ElMessage.error('获取收货地址失败')
  } finally {
    addressLoading.value = false
  }
}

// 显示地址对话框
const showAddressDialog = () => {
  isEdit.value = false
  editAddressId.value = 0
  resetAddressForm()
  addressDialogVisible.value = true
}

// 编辑地址
const editAddress = (address) => {
  isEdit.value = true
  editAddressId.value = address.addressId

  // 填充表单
  Object.assign(addressForm, {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    region: [address.province, address.city, address.district],
    detail: address.detail,
    isDefault: address.isDefault
  })

  addressDialogVisible.value = true
}

// 重置地址表单
const resetAddressForm = () => {
  isEdit.value = false
  editAddressId.value = 0
  Object.assign(addressForm, {
    receiverName: '',
    receiverPhone: '',
    region: [],
    detail: '',
    isDefault: false
  })

  if (addressFormRef.value) {
    addressFormRef.value.resetFields()
  }
}

// 保存地址
const saveAddress = async () => {
  const valid = await addressFormRef.value.validate().catch(() => false)
  if (!valid) return

  addressSaving.value = true
  try {
    const [province, city, district] = addressForm.region
    const payload = {
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province,
      city,
      district,
      detail: addressForm.detail,
      isDefault: addressForm.isDefault
    }

    if (isEdit.value) {
      await address.update({ ...payload, addressId: editAddressId.value })
      ElMessage.success('地址更新成功')
    } else {
      await address.add(payload)
      ElMessage.success('地址添加成功')
    }

    addressDialogVisible.value = false
    fetchAddresses()
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存地址失败')
  } finally {
    addressSaving.value = false
  }
}

// 设为默认地址
const setDefaultAddress = async (addressId) => {
  try {
    await address.setDefault(addressId)
    fetchAddresses()
    ElMessage.success('已设为默认地址')
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}

// 删除地址
const deleteAddress = async (addressId) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await address.delete(addressId)

    // 刷新列表
    fetchAddresses()

    ElMessage.success('地址删除成功')
  } catch (error) {
    console.error('删除地址失败:', error)
    ElMessage.error('删除地址失败')
  }
}

// 初始化
onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped>
.address-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.address-header h2 {
  font-size: 20px;
  color: #333;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  position: relative;
}

.address-item.default {
  border-color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.05);
}

.address-info {
  flex: 1;
}

.address-name-phone {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.phone {
  font-size: 14px;
  color: #666;
}

.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}
</style>
