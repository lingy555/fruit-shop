
<template>
  <div class="cart-container">
    <div class="cart-header">
      <h2>我的购物车</h2>
      <div class="header-actions">
        <el-button 
          v-if="cartItems.length > 0" 
          @click="clearInvalid"
          text
          type="danger"
        >
          清空失效商品
        </el-button>
      </div>
    </div>

    <!-- 购物车为空 -->
    <el-empty 
      v-if="cartItems.length === 0 && !loading"
      description="购物车空空如也，快去选购吧"
      :image-size="200"
    >
      <el-button type="primary" @click="goToHome">去逛逛</el-button>
    </el-empty>

    <!-- 购物车列表 -->
    <div v-else-if="cartItems.length > 0" v-loading="loading">
      <!-- 全选操作栏 -->
      <div class="cart-actions">
        <el-checkbox 
          v-model="selectAll" 
          @change="handleSelectAll"
          class="select-all"
        >
          全选
        </el-checkbox>

        <div class="selected-info">
          已选择 <span class="selected-count">{{ selectedCount }}</span> 件商品
        </div>

        <el-button 
          :disabled="selectedCount === 0" 
          type="danger"
          @click="deleteSelected"
          text
        >
          删除选中
        </el-button>
      </div>

      <!-- 商品列表 -->
      <div class="cart-list">
        <!-- 按店铺分组 -->
        <div 
          v-for="shop in groupedCartItems" 
          :key="shop.shopId"
          class="shop-group"
        >
          <div class="shop-header">
            <div class="shop-info">
              <el-checkbox 
                v-model="shop.selected" 
                @change="handleShopSelect(shop)"
              />
              <span class="shop-name">{{ shop.shopName }}</span>
            </div>
            <el-button 
              text
              type="primary"
              @click="goToShop(shop.shopId)"
            >
              进店逛逛
            </el-button>
          </div>

          <div class="product-list">
            <div 
              v-for="item in shop.items" 
              :key="item.cartId"
              class="cart-item"
              :class="{ invalid: !item.isValid }"
            >
              <div class="item-select">
                <el-checkbox 
                  v-model="item.selected" 
                  :disabled="!item.isValid"
                  @change="handleItemSelect"
                />
              </div>

              <div class="item-image">
                <img :src="item.image" :alt="item.productName" />
              </div>

              <div class="item-info">
                <div class="item-name" :title="item.productName">{{ item.productName }}</div>
                <div class="item-specs" v-if="item.specs">{{ item.specs }}</div>
                <div class="item-price">
                  <span class="current-price">¥{{ item.price }}</span>
                  <span 
                    v-if="item.originalPrice && item.originalPrice > item.price" 
                    class="original-price"
                  >
                    ¥{{ item.originalPrice }}
                  </span>
                </div>
              </div>

              <div class="item-actions">
                <el-input-number 
                  v-model="item.quantity" 
                  :min="1" 
                  :max="item.stock"
                  :disabled="!item.isValid"
                  @change="updateQuantity(item)"
                  size="small"
                />
                <el-button 
                  :icon="Delete" 
                  circle
                  :disabled="!item.isValid"
                  @click="deleteItem(item)"
                  size="small"
                  type="danger"
                  plain
                />
              </div>

              <div class="invalid-tip" v-if="!item.isValid">
                商品已失效
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="cart-footer">
        <div class="total-info">
          <div class="total-item">
            <span class="total-label">商品总额：</span>
            <span class="total-value">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div class="total-item">
            <span class="total-label">运费：</span>
            <span class="total-value">¥{{ shippingFee.toFixed(2) }}</span>
          </div>
          <div class="total-item discount">
            <span class="total-label">优惠：</span>
            <span class="total-value">-¥{{ discountAmount.toFixed(2) }}</span>
          </div>
          <div class="total-item final">
            <span class="total-label">应付总额：</span>
            <span class="total-value">¥{{ actualAmount.toFixed(2) }}</span>
          </div>
        </div>

        <el-button 
          type="primary" 
          size="large"
          :disabled="selectedCount === 0"
          @click="goToCheckout"
        >
          结算({{ selectedCount }})
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cart } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 购物车商品列表
const cartItems = ref([])

// 加载状态
const loading = ref(false)

// 全选状态
const selectAll = computed({
  get() {
    if (cartItems.value.length === 0) return false
    return cartItems.value.every(item => item.selected || !item.isValid)
  },
  set(value) {
    cartItems.value.forEach(item => {
      if (item.isValid) {
        item.selected = value
      }
    })
    updateSelectedStatus()
  }
})

// 选中商品数量
const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected && item.isValid).length
})

// 按店铺分组的商品
const groupedCartItems = computed(() => {
  const shops = {}

  cartItems.value.forEach(item => {
    if (!shops[item.shopId]) {
      shops[item.shopId] = {
        shopId: item.shopId,
        shopName: item.shopName,
        items: [],
        selected: true
      }
    }

    shops[item.shopId].items.push(item)

    // 检查店铺是否全选
    shops[item.shopId].selected = shops[item.shopId].items.every(i => i.selected || !i.isValid)
  })

  return Object.values(shops)
})

// 商品总额
const totalAmount = computed(() => {
  return cartItems.value
    .filter(item => item.selected && item.isValid)
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 运费
const shippingFee = computed(() => {
  // 这里可以根据实际业务规则计算运费
  return selectedCount.value > 0 ? 0 : 0
})

// 优惠金额
const discountAmount = computed(() => {
  // 这里可以根据实际业务规则计算优惠
  return 0
})

// 应付总额
const actualAmount = computed(() => {
  return totalAmount.value + shippingFee.value - discountAmount.value
})

// 获取购物车列表
const fetchCartList = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const response = await cart.getList()
    cartItems.value = response.data.items || []
    updateSelectedStatus()
  } catch (error) {
    console.error('获取购物车列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新选中状态
const updateSelectedStatus = () => {
  // 这里可以更新选中状态
}

// 处理店铺选择
const handleShopSelect = (shop) => {
  shop.items.forEach(item => {
    if (item.isValid) {
      item.selected = shop.selected
    }
  })
  updateSelectedStatus()
}

// 处理商品选择
const handleItemSelect = () => {
  updateSelectedStatus()
}

// 处理全选
const handleSelectAll = () => {
  updateSelectedStatus()
}

// 更新商品数量
const updateQuantity = async (item) => {
  try {
    await cart.updateQuantity({
      cartId: item.cartId,
      quantity: item.quantity
    })
  } catch (error) {
    console.error('更新购物车数量失败:', error)
    // 恢复原数量
    // 这里可以恢复原数量
  }
}

// 删除商品
const deleteItem = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cart.delete({ cartIds: [item.cartId] })

    // 从列表中移除
    const index = cartItems.value.findIndex(i => i.cartId === item.cartId)
    if (index !== -1) {
      cartItems.value.splice(index, 1)
    }

    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除购物车商品失败:', error)
  }
}

// 删除选中商品
const deleteSelected = async () => {
  const selectedItems = cartItems.value.filter(item => item.selected && item.isValid)
  if (selectedItems.length === 0) return

  try {
    await ElMessageBox.confirm(`确定要删除选中的${selectedItems.length}件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const cartIds = selectedItems.map(item => item.cartId)
    await cart.delete({ cartIds })

    // 从列表中移除
    cartItems.value = cartItems.value.filter(item => !cartIds.includes(item.cartId))

    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除选中商品失败:', error)
  }
}

// 清空失效商品
const clearInvalid = async () => {
  const invalidItems = cartItems.value.filter(item => !item.isValid)
  if (invalidItems.length === 0) return

  try {
    await ElMessageBox.confirm(`确定要清空${invalidItems.length}件失效商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cart.clearInvalid()

    // 从列表中移除
    cartItems.value = cartItems.value.filter(item => item.isValid)

    ElMessage.success('清空成功')
  } catch (error) {
    console.error('清空失效商品失败:', error)
  }
}

// 进入店铺
const goToShop = (shopId) => {
  router.push(`/shop/${shopId}`)
}

// 去首页
const goToHome = () => {
  router.push('/home')
}

// 去结算
const goToCheckout = () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  const selectedCartIds = cartItems.value
    .filter(item => item.selected && item.isValid)
    .map(item => item.cartId)

  router.push({
    path: '/checkout',
    query: { cartIds: selectedCartIds.join(',') }
  })
}

// 初始化
onMounted(() => {
  fetchCartList()
})
</script>

<style scoped>
.cart-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-header h2 {
  font-size: 20px;
  color: #333;
}

.cart-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.select-all {
  margin-right: 20px;
}

.selected-info {
  font-size: 14px;
  color: #666;
}

.selected-count {
  color: #ff5722;
  font-weight: 500;
}

.cart-list {
  margin-bottom: 30px;
}

.shop-group {
  margin-bottom: 30px;
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 15px;
}

.shop-info {
  display: flex;
  align-items: center;
}

.shop-name {
  margin-left: 10px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  position: relative;
}

.cart-item.invalid {
  opacity: 0.6;
  background-color: #f9f9f9;
}

.item-select {
  margin-right: 15px;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 15px;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.item-specs {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.item-price {
  margin-bottom: 5px;
}

.current-price {
  font-size: 16px;
  font-weight: 500;
  color: #ff5722;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 5px;
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-left: 15px;
}

.invalid-tip {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 8px;
  background-color: #ff5722;
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.total-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.total-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.total-item.discount .total-value {
  color: #ff5722;
}

.total-item.final {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e0e0e0;
  font-weight: 500;
}

.total-item.final .total-value {
  font-size: 18px;
  color: #ff5722;
  font-weight: bold;
}

@media (max-width: 768px) {
  .cart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .cart-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .cart-item {
    flex-wrap: wrap;
  }

  .item-image {
    width: 60px;
    height: 60px;
    margin-right: 10px;
  }

  .item-info {
    width: 100%;
    margin-bottom: 10px;
  }

  .item-actions {
    flex-direction: row;
    margin-left: 0;
    margin-top: 5px;
  }

  .cart-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style>
