<template>
  <view class="page-bg login-page">
    <view class="login-card">
      <text class="login-title">{{ isRegister ? '📝 注册账号' : '🎬 登录' }}</text>
      <text class="login-sub">Global Drama Tracker</text>

      <view class="form-group">
        <input class="form-input" v-model="form.phone" placeholder="手机号" type="number" maxlength="11" />
      </view>
      <view class="form-group" v-if="isRegister">
        <input class="form-input" v-model="form.nickname" placeholder="昵称（选填）" />
      </view>
      <view class="form-group">
        <input class="form-input" v-model="form.password" placeholder="密码（至少6位）" password />
      </view>

      <view class="submit-btn" @tap="handleSubmit">
        <text>{{ loading ? '处理中...' : (isRegister ? '注 册' : '登 录') }}</text>
      </view>

      <view class="switch-row">
        <text class="switch-text" v-if="!isRegister">还没有账号？</text>
        <text class="switch-text" v-else>已有账号？</text>
        <text class="switch-link" @tap="isRegister = !isRegister">{{ isRegister ? '去登录' : '立即注册' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { register, login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const isRegister = ref(false)
const form = reactive({ phone: '', password: '', nickname: '' })

async function handleSubmit() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' }); return
  }
  if (!form.password || form.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' }); return
  }
  loading.value = true
  try {
    let data
    if (isRegister.value) {
      data = await register(form.phone, form.password, form.nickname)
      uni.showToast({ title: '注册成功', icon: 'success' })
    } else {
      data = await login(form.phone, form.password)
      uni.showToast({ title: '登录成功', icon: 'success' })
    }
    userStore.setUser(data)
    setTimeout(() => uni.navigateBack(), 500)
  } catch {}
  loading.value = false
}
</script>

<style lang="scss">
.login-page { display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 40rpx; }
.login-card { width: 100%; max-width: 600rpx; background: #1e293b; border-radius: 24rpx; padding: 60rpx 40rpx; text-align: center; }
.login-title { font-size: 40rpx; font-weight: 800; color: #f1f5f9; display: block; margin-bottom: 8rpx; }
.login-sub { font-size: 24rpx; color: #64748b; display: block; margin-bottom: 48rpx; }

.form-group { margin-bottom: 24rpx; }
.form-input { background: #0f172a; border-radius: 16rpx; padding: 24rpx; color: #f1f5f9; font-size: 28rpx; width: 100%; box-sizing: border-box; }

.submit-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 40rpx; padding: 24rpx; text-align: center;
  color: white; font-size: 30rpx; font-weight: 700; margin-top: 16rpx;
}

.switch-row { display: flex; justify-content: center; gap: 8rpx; margin-top: 28rpx; }
.switch-text { font-size: 24rpx; color: #64748b; }
.switch-link { font-size: 24rpx; color: #6366f1; }
</style>
