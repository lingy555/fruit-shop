
<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>欢迎来到欢喜果铺</p>
      </div>

      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form"
        @submit.prevent
      >
        <el-tabs v-model="activeTab" class="login-tabs">
          <!-- 账号密码登录 -->
          <el-tab-pane label="账号登录" name="account">
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名或手机号"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item prop="captcha">
              <div class="captcha-row">
                <el-input 
                  v-model="loginForm.captcha" 
                  placeholder="请输入验证码"
                  prefix-icon="Key"
                  clearable
                  @keyup.enter="handleLogin"
                />
                <img 
                  class="captcha-img"
                  :src="captchaImage"
                  alt="验证码"
                  @click="loadCaptcha"
                />
              </div>
            </el-form-item>

            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
            </div>
          </el-tab-pane>

          <!-- 手机验证码登录 -->
          <el-tab-pane label="手机登录" name="phone">
            <el-form-item prop="phone">
              <el-input 
                v-model="loginForm.phone" 
                placeholder="请输入手机号"
                prefix-icon="Phone"
                clearable
              />
            </el-form-item>

            <el-form-item prop="verifyCode">
              <div class="code-input">
                <el-input 
                  v-model="loginForm.verifyCode" 
                  placeholder="请输入验证码"
                  prefix-icon="Key"
                  clearable
                  @keyup.enter="handleLogin"
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
          </el-tab-pane>
        </el-tabs>

        <el-button 
          type="primary" 
          class="login-btn" 
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>

        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { auth } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref(null)

// 当前激活的标签页
const activeTab = ref('account')

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
  phone: '',
  verifyCode: '',
  loginType: 'password'
})

const captchaImage = ref('')

// 表单验证规则
const loginRules = computed(() => {
  if (activeTab.value === 'account') {
    return {
      username: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      captcha: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { len: 4, message: '验证码为4位字符', trigger: 'blur' }
      ]
    }
  }

  return {
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    verifyCode: [
      { required: true, message: '请输入验证码', trigger: 'blur' },
      { len: 6, message: '验证码长度为6位', trigger: 'blur' }
    ]
  }

  loadCaptcha()
})

watch(activeTab, () => {
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
})

// 是否记住我
const rememberMe = ref(false)

// 加载状态
const loading = ref(false)

// 验证码倒计时
const codeCountdown = ref(0)

// 处理登录
const handleLogin = async () => {
  // 设置登录类型（要在 validate 前设置，避免校验规则/提交逻辑不一致）
  loginForm.loginType = activeTab.value === 'account' ? 'password' : 'sms'

  if (activeTab.value === 'account' && !loginForm.captchaKey) {
    await loadCaptcha()
    ElMessage.warning('请先获取验证码')
    return
  }

  // 表单验证
  const valid = await loginFormRef.value.validate().catch((e) => {
    console.groupCollapsed('[Login Validate Failed]')
    console.log('form:', { ...loginForm })
    console.log('details:', e)
    console.groupEnd()
    return false
  })
  if (!valid) {
    return
  }

  loading.value = true
  try {
    let response
    if (activeTab.value === 'account') {
      // 账号密码登录
      response = await userStore.login({
        username: loginForm.username,
        password: loginForm.password,
        captcha: loginForm.captcha,
        captchaKey: loginForm.captchaKey,
        loginType: 'password'
      })
    } else {
      // 手机验证码登录
      response = await userStore.loginByPhone({
        phone: loginForm.phone,
        verifyCode: loginForm.verifyCode,
        loginType: 'sms'
      })
    }

    ElMessage.success('登录成功')

    // 记住我功能
    if (rememberMe.value) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('username', activeTab.value === 'account' ? loginForm.username : loginForm.phone)
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('username')
    }

    // 跳转到之前的页面或首页
    const redirect = route.query.redirect || '/home'
    router.push(redirect).catch((e) => {
      console.groupCollapsed('[Login Redirect Failed]')
      console.log('redirect:', redirect)
      console.log('details:', e)
      console.groupEnd()
    })
  } catch (error) {
    console.groupCollapsed('[Login Failed]')
    console.log('form:', { ...loginForm, password: '******' })
    console.log('error:', error)
    console.log('error.code:', error?.code)
    console.log('error.response:', error?.response)
    console.groupEnd()
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

// 发送验证码
const sendCode = async () => {
  // 验证手机号
  if (!/^1[3-9]\d{9}$/.test(loginForm.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }

  try {
    await auth.sendCode({
      phone: loginForm.phone,
      type: 'login'
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
  }
}

const loadCaptcha = async () => {
  try {
    const res = await auth.captcha()
    loginForm.captchaKey = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请稍后再试')
  }
}

// 初始化
onMounted(() => {
  // 检查是否记住登录
  if (localStorage.getItem('rememberMe') === 'true') {
    rememberMe.value = true
    const username = localStorage.getItem('username')
    if (username) {
      if (/^1[3-9]\d{9}$/.test(username)) {
        activeTab.value = 'phone'
        loginForm.phone = username
      } else {
        activeTab.value = 'account'
        loginForm.username = username
      }
    }
  }
})
</script>

<style scoped>
.login-container {
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  color: #909399;
}

.login-form {
  margin-top: 20px;
}

.login-tabs {
  margin-bottom: 20px;
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-link {
  font-size: 14px;
  color: #409EFF;
}

.forgot-link:hover {
  text-decoration: underline;
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

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  margin-bottom: 20px;
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.register-link a {
  color: #409EFF;
  margin-left: 5px;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
