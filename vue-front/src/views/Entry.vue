<template>
  <div class="entry-page">
    <div class="entry-card">
      <div class="entry-header">
        <p class="eyebrow">欢喜果铺 · 登录中心</p>
        <h1>选择您的登录身份</h1>
        <p class="subtitle">统一入口 · 角色分明 · 安全认证</p>
      </div>

      <div class="identity-cards">
        <div
          v-for="role in roles"
          :key="role.value"
          class="identity-card"
          :style="{ background: role.gradient, boxShadow: role.shadow }"
          @click="gotoRole(role.path)"
        >
          <div class="icon-wrapper">
            <component :is="role.icon" class="role-icon" />
          </div>
          <div class="card-title">{{ role.label }}</div>
          <div class="card-desc">{{ role.desc }}</div>
          <div class="card-tagline">{{ role.short }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCartFull, Shop, Tools } from '@element-plus/icons-vue'

const router = useRouter()

const roles = [
  {
    value: 'user',
    label: '用户端入口',
    desc: '轻松选购、实时沟通',
    short: '适用于消费者',
    path: '/login',
    icon: ShoppingCartFull,
    gradient: 'linear-gradient(145deg, rgba(255, 243, 213, 0.95), rgba(255, 196, 140, 0.95))',
    shadow: '0 25px 45px rgba(255, 179, 108, 0.35)'
  },
  {
    value: 'merchant',
    label: '商家端入口',
    desc: '商品运营、订单处理',
    short: '适用于商户伙伴',
    path: '/merchant/login',
    icon: Shop,
    gradient: 'linear-gradient(145deg, rgba(255, 223, 186, 0.95), rgba(255, 161, 130, 0.95))',
    shadow: '0 25px 45px rgba(255, 153, 121, 0.32)'
  },
  {
    value: 'admin',
    label: '管理员入口',
    desc: '数据看板、权限维护',
    short: '适用于平台管理员',
    path: '/admin/login',
    icon: Tools,
    gradient: 'linear-gradient(145deg, rgba(210, 243, 255, 0.95), rgba(133, 202, 255, 0.95))',
    shadow: '0 25px 45px rgba(118, 184, 255, 0.32)'
  }
]

const gotoRole = (path) => {
  if (!path) {
    ElMessage.warning('暂不可用')
    return
  }
  router.push(path)
}
</script>

<style scoped>
.entry-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xxl) var(--space-lg);
  overflow: hidden;
}

.entry-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url('https://images.unsplash.com/photo-1500336624523-d727130c3328?auto=format&fit=crop&w=1600&q=80');
  background-size: cover;
  background-position: center;
  filter: blur(6px) brightness(0.9);
  transform: scale(1.05);
}

.entry-page::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(18, 167, 138, 0.65), rgba(255, 243, 196, 0.55));
  backdrop-filter: blur(2px);
}

.entry-card {
  position: relative;
  width: 100%;
  max-width: 640px;
  background: var(--color-glass);
  border-radius: var(--radius-xxl);
  padding: var(--space-xxl) var(--space-xl);
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  backdrop-filter: blur(var(--glass-blur));
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
  z-index: 1;
}

.entry-header .eyebrow {
  font-size: var(--font-size-sm);
  letter-spacing: 0.2em;
  color: var(--color-primary);
  text-transform: uppercase;
  margin-bottom: var(--space-sm);
}

.entry-header h1 {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--color-neutral-900);
  margin-bottom: var(--space-sm);
}

.entry-header .subtitle {
  color: var(--color-neutral-700);
  font-size: var(--font-size-base);
}

.identity-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-md);
}

.identity-card {
  border-radius: var(--radius-lg);
  padding: var(--space-lg) var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  cursor: pointer;
  transition: var(--transition-fast);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.identity-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-card);
}

.icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
}

.role-icon {
  width: 36px;
  height: 36px;
  color: var(--color-primary);
}

.identity-card:nth-child(2) .role-icon {
  color: #c35a2e;
}

.identity-card:nth-child(3) .role-icon {
  color: #0b6bc6;
}

.card-title {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-neutral-900);
}

.card-desc {
  font-size: var(--font-size-base);
  color: var(--color-neutral-700);
}

.card-tagline {
  font-size: var(--font-size-sm);
  color: var(--color-neutral-500);
}

@media (max-width: 768px) {
  .identity-cards {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

@media (max-width: 520px) {
  .entry-card {
    padding: 32px 26px;
    border-radius: 32px;
  }
}
</style>
