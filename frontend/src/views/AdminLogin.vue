<template>
  <div class="login-page">
    <div class="login-card">
      <h1>🔐 管理端登录</h1>
      <p class="subtitle">趣活圈 管理中心</p>
      <el-form @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="管理员账号" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%">
          登 录
        </el-button>
      </el-form>
      <router-link to="/" class="back-link">← 返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await adminLogin(form.username, form.password)
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_user', data.username)
    ElMessage.success('登录成功')
    router.push('/admin')
  } catch {
    ElMessage.error('用户名或密码错误')
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
.login-card h1 {
  font-size: 24px;
  margin-bottom: 8px;
}
.subtitle {
  color: var(--text-muted);
  margin-bottom: 32px;
  font-size: 14px;
}
.login-form {
  margin-bottom: 20px;
}
.back-link {
  display: inline-block;
  margin-top: 16px;
  color: var(--text-muted);
  font-size: 13px;
}
.back-link:hover { color: var(--primary-light); }
</style>
