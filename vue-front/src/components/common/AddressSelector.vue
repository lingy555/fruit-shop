
<template>
  <div class="address-selector">
    <div class="address-list" v-if="addresses.length > 0">
      <div 
        v-for="address in addresses" 
        :key="address.addressId"
        class="address-item"
        :class="{ active: selectedAddressId === address.addressId, default: address.isDefault }"
        @click="selectAddress(address.addressId)"
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
            @click.stop="editAddress(address)"
          >
            编辑
          </el-button>
          <el-button 
            text 
            type="danger"
            @click.stop="deleteAddress(address.addressId)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <div class="no-address" v-else>
      <el-empty 
        description="暂无收货地址"
        :image-size="150"
      >
        <el-button type="primary" @click="addAddress">添加收货地址</el-button>
      </el-empty>
    </div>

    <div class="address-actions-bottom">
      <el-button type="primary" @click="addAddress">新增地址</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  addresses: {
    type: Array,
    default: () => []
  },
  selectedAddressId: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['select', 'edit', 'delete', 'add'])

// 选择地址
const selectAddress = (addressId) => {
  emit('select', addressId)
}

// 编辑地址
const editAddress = (address) => {
  emit('edit', address)
}

// 删除地址
const deleteAddress = (addressId) => {
  emit('delete', addressId)
}

// 添加地址
const addAddress = () => {
  emit('add')
}
</script>

<style scoped>
.address-selector {
  margin-bottom: 20px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.address-item {
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.05);
}

.address-item.active {
  border-color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.05);
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
  margin-bottom: 8px;
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
  position: absolute;
  top: 15px;
  right: 15px;
  display: flex;
  gap: 10px;
}

.no-address {
  padding: 20px 0;
}

.address-actions-bottom {
  text-align: center;
  margin-top: 20px;
}
</style>
