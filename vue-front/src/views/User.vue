
<template>
  <div class="user-container">
    <div class="user-layout">
      <!-- 左侧菜单 -->
      <div class="user-sidebar">
        <div class="user-info">
          <el-avatar :size="80" :src="userStore.avatar" fit="cover">
            {{ userStore.nickname.charAt(0) }}
          </el-avatar>
          <div class="user-name">{{ userStore.nickname }}</div>
          <div class="user-level">
            <el-tag type="success" effect="light">{{ userInfo.levelName || '普通会员' }}</el-tag>
          </div>
        </div>

        <el-menu 
          :default-active="activeMenu" 
          class="user-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="security">
            <el-icon><Lock /></el-icon>
            <span>账户安全</span>
          </el-menu-item>
          <el-menu-item index="address">
            <el-icon><Location /></el-icon>
            <span>收货地址</span>
          </el-menu-item>
          <el-menu-item index="order">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="coupon">
            <el-icon><Ticket /></el-icon>
            <span>优惠券</span>
          </el-menu-item>
          <el-menu-item index="points">
            <el-icon><Star /></el-icon>
            <span>我的积分</span>
          </el-menu-item>
          <el-menu-item index="balance">
            <el-icon><Wallet /></el-icon>
            <span>账户余额</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容 -->
      <div class="user-content">
        <!-- 个人信息 -->
        <div v-show="activeMenu === 'profile'" class="content-section">
          <div class="section-header">
            <h3>个人信息</h3>
            <el-button type="primary" @click="showProfileDialog">编辑</el-button>
          </div>

          <div class="info-list">
            <div class="info-item">
              <span class="info-label">用户名：</span>
              <span class="info-value">{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">昵称：</span>
              <span class="info-value">{{ userInfo.nickname }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机号：</span>
              <span class="info-value">{{ userInfo.phone }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱：</span>
              <span class="info-value">{{ userInfo.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">性别：</span>
              <span class="info-value">{{ getGenderText(userInfo.gender) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">生日：</span>
              <span class="info-value">{{ userInfo.birthday || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间：</span>
              <span class="info-value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 账户安全 -->
        <div v-show="activeMenu === 'security'" class="content-section">
          <div class="section-header">
            <h3>账户安全</h3>
          </div>

          <div class="security-list">
            <div class="security-item" @click="showPasswordDialog">
              <div class="item-icon">
                <el-icon><Lock /></el-icon>
              </div>
              <div class="item-info">
                <div class="item-title">登录密码</div>
                <div class="item-desc">定期更换密码，保护账户安全</div>
              </div>
              <div class="item-action">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>

            <div class="security-item" @click="showPhoneDialog">
              <div class="item-icon">
                <el-icon><Phone /></el-icon>
              </div>
              <div class="item-info">
                <div class="item-title">手机号</div>
                <div class="item-desc">更换手机号，用于登录和找回密码</div>
              </div>
              <div class="item-action">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div v-show="activeMenu === 'address'" class="content-section">
          <div class="section-header">
            <h3>收货地址</h3>
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
                <el-button text type="primary" @click="editAddress(address)">编辑</el-button>
                <el-button text type="danger" @click="deleteAddress(address.addressId)">删除</el-button>
              </div>
            </div>
          </div>

          <el-empty 
            v-else
            description="暂无收货地址"
            :image-size="200"
          >
            <el-button type="primary" @click="showAddressDialog">添加收货地址</el-button>
          </el-empty>
        </div>

        <!-- 我的订单 -->
        <div v-show="activeMenu === 'order'" class="content-section">
          <div class="section-header">
            <h3>我的订单</h3>
            <el-button type="primary" @click="goToOrder">查看全部</el-button>
          </div>

          <div class="order-stats">
            <div class="stat-item">
              <span class="stat-label">待付款：</span>
              <span class="stat-value">{{ orderStats.waitPay || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">待发货：</span>
              <span class="stat-value">{{ orderStats.waitDeliver || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">待收货：</span>
              <span class="stat-value">{{ orderStats.waitReceive || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">待评价：</span>
              <span class="stat-value">{{ orderStats.waitComment || 0 }}</span>
            </div>
          </div>

          <div class="recent-orders">
            <div 
              v-for="order in recentOrders" 
              :key="order.orderId"
              class="order-item"
              @click="goToOrderDetail(order.orderId)"
            >
              <div class="order-image">
                <img :src="order.image" :alt="order.productName" />
              </div>
              <div class="order-info">
                <div class="order-name">{{ order.productName }}</div>
                <div class="order-status">{{ order.statusText }}</div>
                <div class="order-price">¥{{ order.actualAmount }}</div>
              </div>
            </div>
          </div>
        </div>

        <div v-show="activeMenu === 'coupon'" class="content-section">
          <div class="section-header">
            <h3>优惠券</h3>
          </div>

          <el-tabs v-model="couponTab">
            <el-tab-pane label="领券中心" name="available">
              <el-table :data="availableCoupons" style="width: 100%" v-loading="availableCouponLoading">
                <el-table-column prop="couponName" label="优惠券" min-width="220" />
                <el-table-column label="优惠" width="120">
                  <template #default="{ row }">
                    <span>¥{{ row.discountValue }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="门槛" width="140">
                  <template #default="{ row }">
                    <span>满 ¥{{ row.minAmount }} 可用</span>
                  </template>
                </el-table-column>
                <el-table-column label="剩余" width="100">
                  <template #default="{ row }">
                    <span>{{ row.remainingCount }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="140">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" :disabled="!row.canReceive" @click="receiveCoupon(row)">
                      {{ row.canReceive ? '领取' : '不可领取' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="history-pagination">
                <el-pagination
                  v-model:current-page="availableCouponPage.page"
                  v-model:page-size="availableCouponPage.pageSize"
                  :total="availableCouponPage.total"
                  layout="prev, pager, next"
                  @current-change="fetchAvailableCoupons"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的优惠券" name="my">
              <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                <span>状态：</span>
                <el-select v-model="myCouponStatus" style="width: 160px;" @change="handleMyCouponFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="未使用" value="0" />
                  <el-option label="已使用" value="1" />
                  <el-option label="已过期" value="2" />
                </el-select>
              </div>

              <el-table :data="myCoupons" style="width: 100%" v-loading="myCouponLoading">
                <el-table-column prop="couponName" label="优惠券" min-width="220" />
                <el-table-column label="优惠" width="120">
                  <template #default="{ row }">
                    <span>¥{{ row.discountValue }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="门槛" width="140">
                  <template #default="{ row }">
                    <span>满 ¥{{ row.minAmount }} 可用</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="110">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 0 ? 'success' : (row.status === 1 ? 'info' : 'warning')">
                      {{ row.statusText }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="expireTime" label="到期时间" width="170" />
              </el-table>

              <div class="history-pagination">
                <el-pagination
                  v-model:current-page="myCouponPage.page"
                  v-model:page-size="myCouponPage.pageSize"
                  :total="myCouponPage.total"
                  layout="prev, pager, next"
                  @current-change="fetchMyCoupons"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 我的积分 -->
        <div v-show="activeMenu === 'points'" class="content-section">
          <div class="section-header">
            <h3>我的积分</h3>
          </div>

          <div class="points-summary">
            <div class="summary-item">
              <span class="summary-label">当前积分：</span>
              <span class="summary-value">{{ userInfo.points || 0 }}</span>
            </div>
          </div>

          <div class="points-history">
            <div class="history-header">积分明细</div>

            <div class="history-list" v-if="pointsRecords.length > 0">
              <div 
                v-for="record in pointsRecords" 
                :key="record.recordId"
                class="history-item"
              >
                <div class="record-info">
                  <div class="record-type">{{ record.typeName }}</div>
                  <div class="record-desc">{{ record.description }}</div>
                </div>
                <div class="record-points">
                  <span :class="{ 'positive': record.type === 1, 'negative': record.type === 2 }">
                    {{ record.type === 1 ? '+' : '-' }}{{ record.points }}
                  </span>
                </div>
                <div class="record-time">{{ formatDate(record.createTime) }}</div>
              </div>
            </div>

            <div class="history-pagination">
              <el-pagination
                v-model:current-page="pointsPage.page"
                v-model:page-size="pointsPage.pageSize"
                :total="pointsPage.total"
                layout="prev, pager, next"
                @current-change="fetchPointsRecords"
              />
            </div>
          </div>
        </div>

        <!-- 账户余额 -->
        <div v-show="activeMenu === 'balance'" class="content-section">
          <div class="section-header">
            <h3>账户余额</h3>
            <el-button type="primary" @click="showRechargeDialog">充值</el-button>
          </div>

          <div class="balance-summary">
            <div class="summary-item">
              <span class="summary-label">可用余额：</span>
              <span class="summary-value">¥{{ balanceInfo.balance || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">冻结余额：</span>
              <span class="summary-value">¥{{ balanceInfo.frozenBalance || 0 }}</span>
            </div>
          </div>

          <div class="recharge-tips">
            <h4>充值说明</h4>
            <ul>
              <li>充值金额实时到账</li>
              <li>充值后可在余额支付时使用</li>
              <li>如有问题请联系客服</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 个人信息对话框 -->
    <el-dialog
      v-model="profileDialogVisible"
      title="编辑个人信息"
      width="500px"
      @close="resetProfileForm"
    >
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="80px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="profileForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">保密</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="profileForm.birthday"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="profileSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      @close="resetPasswordForm"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="80px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="passwordSaving">修改</el-button>
      </template>
    </el-dialog>

    <!-- 修改手机号对话框 -->
    <el-dialog
      v-model="phoneDialogVisible"
      title="修改手机号"
      width="400px"
      @close="resetPhoneForm"
    >
      <el-form
        ref="phoneFormRef"
        :model="phoneForm"
        :rules="phoneRules"
        label-width="80px"
      >
        <el-form-item label="新手机号" prop="newPhone">
          <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" />
        </el-form-item>

        <el-form-item label="验证码" prop="verifyCode">
          <div class="code-input">
            <el-input
              v-model="phoneForm.verifyCode"
              placeholder="请输入验证码"
            />
            <el-button 
              :disabled="codeCountdown > 0" 
              @click="sendCode"
              class="code-btn"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="phoneDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="changePhone" :loading="phoneSaving">修改</el-button>
      </template>
    </el-dialog>

    <!-- 地址对话框 -->
    <el-dialog
      v-model="addressDialogVisible"
      :title="isEditAddress ? '编辑地址' : '新增地址'"
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

    <!-- 充值对话框 -->
    <el-dialog
      v-model="rechargeDialogVisible"
      title="账户充值"
      width="400px"
      @close="resetRechargeForm"
    >
      <el-form
        ref="rechargeFormRef"
        :model="rechargeForm"
        :rules="rechargeRules"
        label-width="80px"
      >
        <el-form-item label="充值金额" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :step="10"
            placeholder="请输入充值金额"
          />
        </el-form-item>

        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="rechargeForm.paymentMethod">
            <el-radio value="alipay">
              <img src="@/assets/payment/alipay.jpg" alt="支付宝" class="payment-icon" />
              <span>支付宝</span>
            </el-radio>
            <el-radio value="wechat">
              <img src="@/assets/payment/wechat.jpg" alt="微信支付" class="payment-icon" />
              <span>微信支付</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="recharge" :loading="rechargeSaving">充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { user, order, coupon, address } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = ref('profile')

// 用户信息
const userInfo = ref({})

// 订单统计
const orderStats = ref({})

// 最近订单
const recentOrders = ref([])

// 收货地址
const addresses = ref([])

// 积分记录
const pointsRecords = ref([])

// 积分分页
const pointsPage = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const couponTab = ref('available')

const availableCoupons = ref([])
const availableCouponLoading = ref(false)
const availableCouponPage = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const myCoupons = ref([])
const myCouponLoading = ref(false)
const myCouponPage = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const myCouponStatus = ref('')

// 账户余额
const balanceInfo = ref({})

// 对话框状态
const profileDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const phoneDialogVisible = ref(false)
const addressDialogVisible = ref(false)
const rechargeDialogVisible = ref(false)

// 是否编辑地址
const isEditAddress = ref(false)
const editAddressId = ref(0)

// 表单引用
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const phoneFormRef = ref(null)
const addressFormRef = ref(null)
const rechargeFormRef = ref(null)

// 个人信息表单
const profileForm = reactive({
  nickname: '',
  gender: 0,
  birthday: '',
  email: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 手机号表单
const phoneForm = reactive({
  newPhone: '',
  verifyCode: ''
})

// 地址表单
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detail: '',
  isDefault: false
})

// 充值表单
const rechargeForm = reactive({
  amount: 50,
  paymentMethod: 'alipay'
})

// 表单验证规则
const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const phoneRules = {
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

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

const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 1, message: '充值金额不能小于1元', trigger: 'blur' }
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

// 保存状态
const profileSaving = ref(false)
const passwordSaving = ref(false)
const phoneSaving = ref(false)
const addressSaving = ref(false)
const rechargeSaving = ref(false)

// 验证码倒计时
const codeCountdown = ref(0)

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await user.getProfile()
    userInfo.value = response.data || {}
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 获取订单统计
const fetchOrderStats = async () => {
  try {
    // 这里应该调用API获取订单统计
    // const response = await order.getStats()
    // orderStats.value = response.data || {}

    // 模拟数据
    orderStats.value = {
      waitPay: 2,
      waitDeliver: 3,
      waitReceive: 5,
      waitComment: 8
    }
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

// 获取最近订单
const fetchRecentOrders = async () => {
  try {
    const response = await order.getList({ page: 1, pageSize: 5 })
    recentOrders.value = response.data.list || []
  } catch (error) {
    console.error('获取最近订单失败:', error)
  }
}

// 获取收货地址
const fetchAddresses = async () => {
  try {
    const response = await address.getList()
    addresses.value = response.data || []
  } catch (error) {
    console.error('获取收货地址失败:', error)
    ElMessage.error('获取收货地址失败')
  }
}

// 获取积分记录
const fetchPointsRecords = async () => {
  try {
    const response = await user.getPointsRecords(pointsPage)
    pointsRecords.value = response.data.list || []
    pointsPage.total = response.data.total || 0
  } catch (error) {
    console.error('获取积分记录失败:', error)
  }
}

// 获取账户余额
const fetchBalanceInfo = async () => {
  try {
    const response = await user.getBalance()
    balanceInfo.value = response.data || {}
  } catch (error) {
    console.error('获取账户余额失败:', error)
  }
}

const fetchAvailableCoupons = async () => {
  availableCouponLoading.value = true
  try {
    const response = await coupon.available({
      page: availableCouponPage.page,
      pageSize: availableCouponPage.pageSize
    })
    availableCoupons.value = response.data.list || []
    availableCouponPage.total = response.data.total || 0
  } catch (error) {
    console.error('获取可领取优惠券失败:', error)
  } finally {
    availableCouponLoading.value = false
  }
}

const fetchMyCoupons = async () => {
  myCouponLoading.value = true
  try {
    const status = myCouponStatus.value === '' ? undefined : Number(myCouponStatus.value)
    const response = await coupon.my({
      page: myCouponPage.page,
      pageSize: myCouponPage.pageSize,
      status
    })
    myCoupons.value = response.data.list || []
    myCouponPage.total = response.data.total || 0
  } catch (error) {
    console.error('获取我的优惠券失败:', error)
  } finally {
    myCouponLoading.value = false
  }
}

const handleMyCouponFilterChange = () => {
  myCouponPage.page = 1
  fetchMyCoupons()
}

const receiveCoupon = async (row) => {
  if (!row?.couponId) return
  try {
    await coupon.receive(row.couponId)
    ElMessage.success('领取成功')
    fetchAvailableCoupons()
    fetchMyCoupons()
    fetchUserInfo()
  } catch (error) {
    console.error('领取优惠券失败:', error)
    ElMessage.error('领取失败')
  }
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index

  // 根据菜单项加载对应数据
  switch (index) {
    case 'profile':
      fetchUserInfo()
      break
    case 'order':
      fetchOrderStats()
      fetchRecentOrders()
      break
    case 'coupon':
      fetchAvailableCoupons()
      fetchMyCoupons()
      break
    case 'address':
      fetchAddresses()
      break
    case 'points':
      fetchPointsRecords()
      break
    case 'balance':
      fetchBalanceInfo()
      break
  }
}

// 显示个人信息对话框
const showProfileDialog = () => {
  // 填充表单
  profileForm.nickname = userInfo.value.nickname || ''
  profileForm.gender = userInfo.value.gender || 0
  profileForm.birthday = userInfo.value.birthday || ''
  profileForm.email = userInfo.value.email || ''

  profileDialogVisible.value = true
}

// 重置个人信息表单
const resetProfileForm = () => {
  Object.assign(profileForm, {
    nickname: '',
    gender: 0,
    birthday: '',
    email: ''
  })
  if (profileFormRef.value) {
    profileFormRef.value.resetFields()
  }
}

// 保存个人信息
const saveProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  profileSaving.value = true
  try {
    await user.updateProfile({
      nickname: profileForm.nickname,
      gender: profileForm.gender,
      birthday: profileForm.birthday,
      email: profileForm.email
    })

    // 更新本地用户信息
    userInfo.value.nickname = profileForm.nickname
    userInfo.value.gender = profileForm.gender
    userInfo.value.birthday = profileForm.birthday
    userInfo.value.email = profileForm.email

    profileDialogVisible.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error('保存失败')
  } finally {
    profileSaving.value = false
  }
}

// 显示修改密码对话框
const showPasswordDialog = () => {
  passwordDialogVisible.value = true
}

// 重置密码表单
const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

// 修改密码
const changePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordSaving.value = true
  try {
    await user.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    passwordDialogVisible.value = false
    ElMessage.success('密码修改成功')
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改失败')
  } finally {
    passwordSaving.value = false
  }
}

// 显示修改手机号对话框
const showPhoneDialog = () => {
  phoneDialogVisible.value = true
}

// 重置手机号表单
const resetPhoneForm = () => {
  Object.assign(phoneForm, {
    newPhone: '',
    verifyCode: ''
  })
  codeCountdown.value = 0
  if (phoneFormRef.value) {
    phoneFormRef.value.resetFields()
  }
}

// 发送验证码
const sendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }

  try {
    await user.sendCode({
      phone: phoneForm.newPhone,
      type: 'changePhone'
    })

    ElMessage.success('验证码已发送，请注意查收')

    // 开始倒计时
    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送验证码失败')
  }
}

// 修改手机号
const changePhone = async () => {
  const valid = await phoneFormRef.value.validate().catch(() => false)
  if (!valid) return

  phoneSaving.value = true
  try {
    await user.changePhone({
      newPhone: phoneForm.newPhone,
      verifyCode: phoneForm.verifyCode
    })

    // 更新本地用户信息
    userInfo.value.phone = phoneForm.newPhone

    phoneDialogVisible.value = false
    ElMessage.success('手机号修改成功')
  } catch (error) {
    console.error('修改手机号失败:', error)
    ElMessage.error('修改失败')
  } finally {
    phoneSaving.value = false
  }
}

// 显示地址对话框
const showAddressDialog = () => {
  isEditAddress.value = false
  addressDialogVisible.value = true
}

// 编辑地址
const editAddress = (address) => {
  isEditAddress.value = true
  editAddressId.value = address.addressId

  // 填充表单
  addressForm.receiverName = address.receiverName
  addressForm.receiverPhone = address.receiverPhone
  addressForm.region = [address.province, address.city, address.district]
  addressForm.detail = address.detail
  addressForm.isDefault = address.isDefault

  addressDialogVisible.value = true
}

// 重置地址表单
const resetAddressForm = () => {
  isEditAddress.value = false
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
    if (isEditAddress.value) {
      await address.update({ ...payload, addressId: editAddressId.value })
      ElMessage.success('地址更新成功')
    } else {
      await address.add(payload)
      ElMessage.success('地址添加成功')
    }

    fetchAddresses()

    addressDialogVisible.value = false
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存失败')
  } finally {
    addressSaving.value = false
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

    fetchAddresses()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 显示充值对话框
const showRechargeDialog = () => {
  rechargeDialogVisible.value = true
}

// 重置充值表单
const resetRechargeForm = () => {
  Object.assign(rechargeForm, {
    amount: 50,
    paymentMethod: 'alipay'
  })
  if (rechargeFormRef.value) {
    rechargeFormRef.value.resetFields()
  }
}

// 充值
const recharge = async () => {
  const valid = await rechargeFormRef.value.validate().catch(() => false)
  if (!valid) return

  rechargeSaving.value = true
  try {
    const response = await user.recharge({
      amount: rechargeForm.amount,
      paymentMethod: rechargeForm.paymentMethod
    })
    rechargeDialogVisible.value = false
    const data = response?.data || {}
    const latestBalance = data.balance
    if (latestBalance !== undefined) {
      balanceInfo.value.balance = latestBalance
    }
    ElMessage.success('充值成功')

    fetchBalanceInfo()
  } catch (error) {
    console.error('充值失败:', error)
    ElMessage.error(error?.response?.data?.message || '充值失败')
  } finally {
    rechargeSaving.value = false
  }
}

// 跳转到订单页
const goToOrder = () => {
  router.push('/order')
}

// 跳转到订单详情
const goToOrderDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

// 获取性别文本
const getGenderText = (gender) => {
  switch (gender) {
    case 1:
      return '男'
    case 2:
      return '女'
    default:
      return '保密'
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchUserInfo()
  handleMenuSelect(activeMenu.value)
})
</script>

<style scoped>
.user-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.user-layout {
  display: flex;
  gap: 30px;
}

.user-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  margin: 10px 0;
}

.user-level {
  margin-top: 10px;
}

.user-menu {
  border-right: none;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
}

:deep(.el-menu-item.is-active) {
  color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.1);
}

.user-content {
  flex: 1;
  min-width: 0;
}

.content-section {
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  color: #333;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  width: 100px;
  color: #666;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 14px;
}

.security-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.security-item:hover {
  border-color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.05);
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: #4CAF50;
}

.item-info {
  flex: 1;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.item-desc {
  font-size: 12px;
  color: #999;
}

.item-action {
  color: #999;
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
  position: relative;
}

.address-item.default {
  border-color: #4CAF50;
}

.address-info {
  flex: 1;
}

.address-name-phone {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
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
  color: #333;
}

.address-actions {
  display: flex;
  gap: 10px;
}

.order-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-right: 5px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
}

.recent-orders {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.05);
}

.order-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 15px;
}

.order-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-status {
  font-size: 12px;
  color: #4CAF50;
}

.order-price {
  font-size: 16px;
  font-weight: 500;
  color: #ff5722;
}

.points-summary {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-label {
  font-size: 16px;
  color: #666;
}

.summary-value {
  font-size: 20px;
  font-weight: bold;
  color: #ff5722;
}

.points-history {
  margin-top: 20px;
}

.history-header {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 15px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.record-info {
  flex: 1;
}

.record-type {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.record-desc {
  font-size: 12px;
  color: #666;
}

.record-points {
  font-size: 16px;
  font-weight: bold;
}

.record-points.positive {
  color: #4CAF50;
}

.record-points.negative {
  color: #ff5722;
}

.record-time {
  font-size: 12px;
  color: #999;
}

.history-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.balance-summary {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.recharge-tips {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.recharge-tips h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
}

.recharge-tips ul {
  padding-left: 20px;
}

.recharge-tips li {
  margin-bottom: 5px;
  font-size: 14px;
  color: #666;
}

.code-input {
  display: flex;
  gap: 10px;
}

.code-input .el-input {
  flex: 1;
}

.code-btn {
  width: 120px;
}

.payment-icon {
  width: 20px;
  height: 20px;
  margin-right: 5px;
  vertical-align: middle;
}

@media (max-width: 768px) {
  .user-layout {
    flex-direction: column;
  }

  .user-sidebar {
    width: 100%;
    margin-bottom: 20px;
  }
}
</style>
