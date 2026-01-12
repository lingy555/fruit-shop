<template>
  <div class="merchant-login-page">
    <el-card class="login-card" shadow="hover">
      <div class="title">商家登录</div>

      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="账号">
          <el-input v-model="form.account" placeholder="请输入手机号或用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-button type="primary" class="login-btn" :loading="loading" @click="submit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMerchantStore } from '@/store/merchant'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'
import { merchantAuth } from '@/api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)

// 添加merchant store
const merchantStore = useMerchantStore()

const form = reactive({
  account: '',
  password: ''
})

const submit = async () => {
  if (!form.account || !form.password) {
    ElMessage.warning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const res = await merchantAuth.login({
      account: form.account,
      password: form.password
    })

    const token = res.data?.token
    const merchantInfo = res.data?.merchantInfo
    
    if (!token) {
      ElMessage.error('登录失败：未返回 token')
      return
    }

    // 保存token到cookie
    Cookies.set('merchantToken', token)
    
    // 保存商家信息到store
    if (merchantInfo) {
      merchantStore.merchantToken = token
      merchantStore.merchantInfo = merchantInfo
      merchantStore.isLogin = true
      console.log('商家信息已保存到store:', merchantInfo)
    }

    const redirect = route.query.redirect || '/merchant/product'
    router.replace(String(redirect))
  } catch (e) {
    // request.js 已统一弹错误信息
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.merchant-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8f5e9 100%);
  padding: 24px;
}

.login-card {
  width: 420px;
  border-radius: 12px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  text-align: center;
}

.login-btn {
  width: 100%;
}
</style>
