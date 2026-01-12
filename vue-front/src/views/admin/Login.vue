<template>
  <div class="admin-login-page">
    <el-card class="login-card" shadow="hover">
      <div class="title">管理员登录</div>

      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" />
            <img class="captcha-img" :src="captchaImage" @click="loadCaptcha" />
          </div>
        </el-form-item>

        <el-button type="primary" class="login-btn" :loading="loading" @click="submit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { admin } from '@/api'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const captchaImage = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: ''
})

const loadCaptcha = async () => {
  try {
    const res = await admin.captcha()
    form.captchaKey = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
  } catch (e) {
    ElMessage.error('获取验证码失败')
  }
}

const submit = async () => {
  if (!form.username || !form.password || !form.captcha || !form.captchaKey) {
    ElMessage.warning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const res = await admin.login({
      username: form.username,
      password: form.password,
      captcha: form.captcha,
      captchaKey: form.captchaKey
    })

    const token = res.data?.token
    if (!token) {
      ElMessage.error('登录失败：未返回 token')
      return
    }

    Cookies.set('adminToken', token)

    const redirect = route.query.redirect || '/admin/dashboard/overview'
    router.replace(String(redirect))
  } catch (e) {
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.admin-login-page {
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

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #eee;
}

.login-btn {
  width: 100%;
}
</style>
