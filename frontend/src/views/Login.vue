<template>
  <div class="login-page">
    <div class="login-card">
      <h1>{{ isRegister ? '📝 注册账号' : '🎯 登录' }}</h1>
      <p class="subtitle">找搭子 · 找到志同道合的伙伴</p>

      <el-form class="login-form" @submit.prevent="handleSubmit">
        <el-form-item>
          <el-input v-model="form.phone" placeholder="手机号" size="large" maxlength="11">
            <template #prefix><el-icon><Iphone /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item v-if="isRegister">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" size="large" show-password @keyup.enter="handleSubmit">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" @click="handleSubmit" style="width:100%">
          {{ isRegister ? '注 册' : '登 录' }}
        </el-button>
      </el-form>

      <div class="switch-mode">
        <span v-if="!isRegister">
          还没有账号？<a @click="isRegister = true">立即注册</a>
        </span>
        <span v-else>
          已有账号？<a @click="isRegister = false">去登录</a>
        </span>
      </div>

      <router-link to="/" class="back-link">← 返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const isRegister = ref(false)
const form = reactive({ phone: '', password: '', nickname: '' })

async function handleSubmit() {
  if (!form.phone || !form.phone.match(/^1[3-9]\d{9}$/)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!form.password || form.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }

  loading.value = true
  try {
    let data
    if (isRegister.value) {
      data = await register(form.phone, form.password, form.nickname)
      ElMessage.success('注册成功')
    } else {
      data = await login(form.phone, form.password)
      ElMessage.success('登录成功')
    }
    userStore.setUser(data)
    // 跳回原页面或首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    // error already shown by interceptor
  }
  loading.value = false
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-dark, #0f172a);
}
.login-card {
  background: var(--bg-card, #1e293b);
  border-radius: 16px;
  padding: 48px 40px;
  width: 400px;
  text-align: center;
}
.login-card h1 { font-size: 24px; margin-bottom: 8px; }
.subtitle { color: var(--text-muted); margin-bottom: 32px; font-size: 14px; }
.login-form { margin-bottom: 16px; }
.switch-mode { font-size: 13px; color: var(--text-muted); margin-bottom: 12px; }
.switch-mode a { color: var(--primary-light, #60a5fa); cursor: pointer; }
.switch-mode a:hover { text-decoration: underline; }
.back-link { display: inline-block; margin-top: 8px; color: var(--text-muted); font-size: 13px; }
.back-link:hover { color: var(--primary-light); }
</style>
