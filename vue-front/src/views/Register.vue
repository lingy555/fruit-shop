
<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>加入我们，享受新鲜水果</p>
      </div>

      <el-form 
        ref="registerFormRef" 
        :model="registerForm" 
        :rules="registerRules" 
        class="register-form"
        @submit.prevent
      >
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="请输入手机号"
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>

        <el-form-item prop="verifyCode">
          <div class="code-input">
            <el-input 
              v-model="registerForm.verifyCode" 
              placeholder="请输入验证码"
              prefix-icon="Key"
              clearable
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

        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请设置密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="请输入邮箱（选填）"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <a href="#" @click.prevent="showAgreement">《用户协议》</a>
            和
            <a href="#" @click.prevent="showPrivacy">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>

        <el-button 
          type="primary" 
          class="register-btn" 
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </el-button>

        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 表单引用
const registerFormRef = ref(null)

// 注册表单
const registerForm = reactive({
  username: '',
  phone: '',
  verifyCode: '',
  password: '',
  confirmPassword: '',
  email: '',
  agreement: false
})

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度在4到20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  agreement: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意用户协议和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 加载状态
const loading = ref(false)

// 验证码倒计时
const codeCountdown = ref(0)

// 处理注册
const handleRegister = async () => {
  // 表单验证
  const valid = await registerFormRef.value.validate().catch((e) => {
    console.groupCollapsed('[Register Validate Failed]')
    console.log('form:', { ...registerForm, password: '******', confirmPassword: '******' })
    console.log('fields:', e?.fields)
    console.log('details:', e)
    console.groupEnd()
    return false
  })
  if (!valid) {
    return
  }

  loading.value = true
  try {
    const response = await auth.register({
      username: registerForm.username,
      phone: registerForm.phone,
      verifyCode: registerForm.verifyCode,
      password: registerForm.password,
      email: registerForm.email
    })

    ElMessage.success('注册成功，请登录')

    // 跳转到登录页
    router.push('/login').catch((e) => {
      console.groupCollapsed('[Register Redirect Failed]')
      console.log('details:', e)
      console.groupEnd()
    })
  } catch (error) {
    console.groupCollapsed('[Register Failed]')
    console.log('form:', { ...registerForm, password: '******', confirmPassword: '******' })
    console.log('error:', error)
    console.log('error.code:', error?.code)
    console.log('error.response:', error?.response)
    console.groupEnd()
  } finally {
    loading.value = false
  }
}

// 发送验证码
const sendCode = async () => {
  // 验证手机号
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }

  try {
    await auth.sendCode({
      phone: registerForm.phone,
      type: 'register'
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

// 显示用户协议
const showAgreement = () => {
  ElMessage.info('显示用户协议内容')
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessage.info('显示隐私政策内容')
}
</script>

<style scoped>
.register-container {
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 450px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.register-header p {
  font-size: 14px;
  color: #909399;
}

.register-form {
  margin-top: 20px;
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

.register-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  margin: 20px 0;
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.login-link a {
  color: #409EFF;
  margin-left: 5px;
}

.login-link a:hover {
  text-decoration: underline;
}

:deep(.el-form-item__content a) {
  color: #409EFF;
}
</style>
