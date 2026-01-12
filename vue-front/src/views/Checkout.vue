
<template>
  <div class="checkout-container" v-loading="loading">
    <div class="checkout-layout">
      <!-- 左侧订单信息 -->
      <div class="checkout-main">
        <!-- 收货地址 -->
        <div class="address-section">
          <div class="section-header">
            <h3>收货地址</h3>
            <el-button text @click="showAddressDialog">新增地址</el-button>
          </div>

          <div class="address-list" v-if="addresses.length > 0">
            <div 
              v-for="address in addresses" 
              :key="address.addressId"
              class="address-item"
              :class="{ active: selectedAddressId === address.addressId }"
              @click="selectAddress(address.addressId)"
            >
              <div class="address-info">
                <div class="address-name-phone">
                  <span class="name">{{ address.receiverName }}</span>
                  <span class="phone">{{ address.receiverPhone }}</span>
                </div>
                <div class="address-detail">
                  {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
                </div>
              </div>
              <div class="address-actions">
                <el-button 
                  v-if="!address.isDefault" 
                  text 
                  type="primary"
                  @click.stop="setDefaultAddress(address.addressId)"
                >
                  设为默认
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

          <el-empty 
            v-else
            description="暂无收货地址，请添加收货地址"
            :image-size="150"
          >
            <el-button type="primary" @click="showAddressDialog">添加收货地址</el-button>
          </el-empty>
        </div>

        <!-- 商品清单 -->
        <div class="order-section">
          <div class="section-header">
            <h3>商品清单</h3>
          </div>

          <div class="order-items">
            <!-- 按店铺分组 -->
            <div 
              v-for="shop in orderItems" 
              :key="shop.shopId"
              class="shop-group"
            >
              <div class="shop-name">{{ shop.shopName }}</div>

              <div 
                v-for="item in shop.products" 
                :key="item.orderItemId"
                class="order-item"
              >
                <div class="item-image">
                  <img :src="item.image" :alt="item.productName" />
                </div>

                <div class="item-info">
                  <div class="item-name" :title="item.productName">{{ item.productName }}</div>
                  <div class="item-specs" v-if="item.specs">{{ item.specs }}</div>
                  <div class="item-price">¥{{ item.price }}</div>
                </div>

                <div class="item-quantity">× {{ item.quantity }}</div>

                <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 优惠券选择 -->
        <div class="coupon-section">
          <div class="section-header">
            <h3>优惠券</h3>
          </div>

          <div v-if="availableCoupons.length > 0" class="coupon-list">
            <div 
              v-for="coupon in availableCoupons" 
              :key="coupon.couponId"
              class="coupon-item"
              :class="{ active: selectedCouponId === coupon.couponId }"
              @click="selectCoupon(coupon.couponId)"
            >
              <div class="coupon-info">
                <div class="coupon-name">{{ coupon.couponName }}</div>
                <div class="coupon-desc">满¥{{ coupon.minAmount }}减¥{{ coupon.discountAmount }}</div>
              </div>
              <div class="coupon-discount">-¥{{ coupon.discountAmount }}</div>
            </div>
            <div 
              class="coupon-item"
              :class="{ active: selectedCouponId === 0 }"
              @click="selectCoupon(0)"
            >
              <div class="coupon-info">
                <div class="coupon-name">不使用优惠券</div>
              </div>
            </div>
          </div>

          <div v-else class="no-coupon">
            暂无可用优惠券
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="payment-section">
          <div class="section-header">
            <h3>支付方式</h3>
          </div>

          <el-radio-group v-model="paymentMethod" class="payment-methods">
            <el-radio value="alipay">
              <img src="@/assets/payment/alipay.jpg" alt="支付宝" class="payment-icon" />
              <span>支付宝</span>
            </el-radio>
            <el-radio value="wechat">
              <img src="@/assets/payment/wechat.jpg" alt="微信支付" class="payment-icon" />
              <span>微信支付</span>
            </el-radio>
            <el-radio value="balance">
              <el-icon><Wallet /></el-icon>
              <span>余额支付（¥{{ userBalance }}）</span>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 订单备注 -->
        <div class="remark-section">
          <div class="section-header">
            <h3>订单备注</h3>
          </div>

          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            placeholder="请输入订单备注（选填）"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>

      <!-- 右侧价格信息 -->
      <div class="checkout-sidebar">
        <div class="price-card">
          <div class="price-item">
            <span class="price-label">商品总额：</span>
            <span class="price-value">¥{{ orderInfo.totalAmount.toFixed(2) }}</span>
          </div>

          <div class="price-item">
            <span class="price-label">运费：</span>
            <span class="price-value">¥{{ orderInfo.shippingFee.toFixed(2) }}</span>
          </div>

          <div class="price-item" v-if="orderInfo.couponDiscount > 0">
            <span class="price-label">优惠券：</span>
            <span class="price-value discount">-¥{{ orderInfo.couponDiscount.toFixed(2) }}</span>
          </div>

          <div class="price-item" v-if="orderInfo.pointsDiscount > 0">
            <span class="price-label">积分抵扣：</span>
            <span class="price-value discount">-¥{{ orderInfo.pointsDiscount.toFixed(2) }}</span>
          </div>

          <div class="price-divider"></div>

          <div class="price-item total">
            <span class="price-label">应付金额：</span>
            <span class="price-value">¥{{ orderInfo.actualAmount.toFixed(2) }}</span>
          </div>

          <el-button 
            type="primary" 
            size="large"
            class="submit-btn"
            :disabled="!canSubmit"
            @click="submitOrder"
          >
            提交订单
          </el-button>
        </div>
      </div>
    </div>

    <!-- 地址对话框 -->
    <el-dialog
      v-model="addressDialogVisible"
      title="添加收货地址"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { order, address, user } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 购物车ID列表
const cartIds = computed(() => {
  return route.query.cartIds ? route.query.cartIds.split(',').map(id => parseInt(id)) : []
})

// 收货地址列表
const addresses = ref([])

// 选中的地址ID
const selectedAddressId = ref(0)

// 地址对话框可见性
const addressDialogVisible = ref(false)

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

// 订单信息
const orderInfo = ref({
  totalAmount: 0,
  shippingFee: 0,
  couponDiscount: 0,
  pointsDiscount: 0,
  actualAmount: 0
})

// 订单商品
const orderItems = ref([])

// 支付方式
const paymentMethod = ref('alipay')

// 订单备注
const remark = ref('')

// 用户余额
const userBalance = ref(0)

// 可用优惠券
const availableCoupons = ref([])

// 选中的优惠券ID
const selectedCouponId = ref(0)

// 加载状态
const loading = ref(false)

// 是否可以提交订单
const canSubmit = computed(() => {
  return selectedAddressId.value > 0 && paymentMethod.value
})

// 获取收货地址列表
const fetchAddresses = async () => {
  try {
    // 这里应该调用地址API获取地址列表
    // const response = await address.getList()
    // addresses.value = response.data || []

    // 模拟数据
    addresses.value = [
      {
        addressId: 1,
        receiverName: '张三',
        receiverPhone: '13800138000',
        province: '广东省',
        city: '深圳市',
        district: '南山区',
        detail: '科技园南区A座',
        isDefault: true
      }
    ]

    // 默认选中第一个地址或默认地址
    const defaultAddress = addresses.value.find(addr => addr.isDefault)
    selectedAddressId.value = defaultAddress ? defaultAddress.addressId : (addresses.value[0] ? addresses.value[0].addressId : 0)
  } catch (error) {
    console.error('获取收货地址失败:', error)
  }
}

// 获取用户余额
const fetchUserBalance = async () => {
  try {
    const response = await user.getBalance()
    userBalance.value = response.data.balance || 0
  } catch (error) {
    console.error('获取用户余额失败:', error)
  }
}

// 确认订单信息
const fetchOrderConfirm = async () => {
  if (!cartIds.value.length) {
    ElMessage.warning('请选择要结算的商品')
    router.replace('/cart')
    return
  }

  loading.value = true
  try {
    const response = await order.confirm({
      cartIds: cartIds.value,
      buyType: 'cart'
    })

    orderInfo.value = response.data
    orderItems.value = response.data.orderItems || []
    availableCoupons.value = response.data.availableCoupons || []
  } catch (error) {
    console.error('确认订单信息失败:', error)
    ElMessage.error('确认订单信息失败')
  } finally {
    loading.value = false
  }
}

// 选择地址
const selectAddress = (addressId) => {
  selectedAddressId.value = addressId
}

// 显示地址对话框
const showAddressDialog = () => {
  addressDialogVisible.value = true
}

// 重置地址表单
const resetAddressForm = () => {
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
    // 这里应该调用地址API保存地址
    // await address.add({
    //   receiverName: addressForm.receiverName,
    //   receiverPhone: addressForm.receiverPhone,
    //   province: addressForm.region[0],
    //   city: addressForm.region[1],
    //   district: addressForm.region[2],
    //   detail: addressForm.detail,
    //   isDefault: addressForm.isDefault
    // })

    // 模拟保存成功
    const newAddress = {
      addressId: Date.now(),
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.region[0],
      city: addressForm.region[1],
      district: addressForm.region[2],
      detail: addressForm.detail,
      isDefault: addressForm.isDefault
    }

    addresses.value.push(newAddress)

    // 如果是默认地址，则选中
    if (newAddress.isDefault) {
      selectedAddressId.value = newAddress.addressId
    }

    addressDialogVisible.value = false
    ElMessage.success('添加地址成功')
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
    // 这里应该调用地址API设置默认地址
    // await address.setDefault(addressId)

    // 更新本地数据
    addresses.value.forEach(addr => {
      addr.isDefault = addr.addressId === addressId
    })

    ElMessage.success('设置默认地址成功')
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}

// 删除地址
const deleteAddress = async (addressId) => {
  try {
    // 这里应该调用地址API删除地址
    // await address.delete(addressId)

    // 从列表中移除
    const index = addresses.value.findIndex(addr => addr.addressId === addressId)
    if (index !== -1) {
      addresses.value.splice(index, 1)
    }

    // 如果删除的是选中的地址，则重新选择
    if (selectedAddressId.value === addressId && addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].addressId
    }

    ElMessage.success('删除地址成功')
  } catch (error) {
    console.error('删除地址失败:', error)
    ElMessage.error('删除地址失败')
  }
}

// 选择优惠券
const selectCoupon = (couponId) => {
  selectedCouponId.value = couponId
  const coupon = availableCoupons.value.find(c => c.couponId === couponId)
  if (coupon) {
    orderInfo.value.couponDiscount = coupon.discountAmount
  } else {
    orderInfo.value.couponDiscount = 0
  }
  // 重新计算实际金额
  orderInfo.value.actualAmount = orderInfo.value.totalAmount + orderInfo.value.shippingFee - orderInfo.value.couponDiscount - orderInfo.value.pointsDiscount
}

// 提交订单
const submitOrder = async () => {
  if (!canSubmit.value) return

  if (!cartIds.value.length) {
    ElMessage.warning('请选择要结算的商品')
    router.replace('/cart')
    return
  }

  loading.value = true
  try {
    const response = await order.create({
      addressId: selectedAddressId.value,
      cartIds: cartIds.value,
      couponId: selectedCouponId.value === 0 ? undefined : selectedCouponId.value,
      paymentMethod: paymentMethod.value,
      remark: remark.value
    })

    ElMessage.success('订单创建成功')

    // 跳转到支付页面
    router.push({
      path: '/payment',
      query: {
        orderId: response.data.orderId,
        paymentUrl: response.data.paymentUrl
      }
    })
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error('提交订单失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(async () => {
  await fetchAddresses()
  await fetchUserBalance()
  await fetchOrderConfirm()
})
</script>

<style scoped>
.checkout-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.checkout-layout {
  display: flex;
  gap: 30px;
}

.checkout-main {
  flex: 1;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  font-size: 18px;
  color: #333;
}

.address-section, .order-section, .coupon-section, .payment-section, .remark-section {
  margin-bottom: 30px;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coupon-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.coupon-item.active {
  border-color: #ff6b35;
  background-color: rgba(255, 107, 53, 0.05);
}

.coupon-item:hover {
  border-color: #ff6b35;
}

.coupon-info {
  flex: 1;
}

.coupon-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.coupon-desc {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.coupon-discount {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b35;
}

.no-coupon {
  padding: 20px;
  text-align: center;
  color: #999;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item.active {
  border-color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.05);
}

.address-item:hover {
  border-color: #4CAF50;
}

.address-info {
  flex: 1;
}

.address-name-phone {
  margin-bottom: 5px;
}

.name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-right: 10px;
}

.phone {
  font-size: 14px;
  color: #666;
}

.address-detail {
  font-size: 14px;
  color: #666;
}

.address-actions {
  display: flex;
  gap: 10px;
}

.order-items {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 15px;
}

.shop-group {
  margin-bottom: 20px;
}

.shop-group:last-child {
  margin-bottom: 0;
}

.shop-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #f0f0f0;
}

.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.order-item:last-child {
  margin-bottom: 0;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 15px;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
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
  font-size: 14px;
  color: #ff5722;
}

.item-quantity {
  font-size: 14px;
  color: #666;
  margin: 0 15px;
}

.item-subtotal {
  font-size: 16px;
  font-weight: 500;
  color: #ff5722;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.payment-icon {
  width: 24px;
  height: 24px;
  margin-right: 8px;
}

.checkout-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.price-card {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.price-label {
  color: #666;
}

.price-value {
  font-weight: 500;
  color: #333;
}

.price-value.discount {
  color: #ff5722;
}

.price-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 15px 0;
}

.price-item.total {
  margin-bottom: 20px;
}

.price-item.total .price-label,
.price-item.total .price-value {
  font-size: 16px;
  font-weight: 500;
}

.price-item.total .price-value {
  color: #ff5722;
}

.submit-btn {
  width: 100%;
}

@media (max-width: 768px) {
  .checkout-layout {
    flex-direction: column;
  }

  .checkout-sidebar {
    width: 100%;
  }
}
</style>
