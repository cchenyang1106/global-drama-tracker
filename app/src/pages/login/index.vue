<template>
  <view class="login-wrap">
    <view class="login-card">
      <text class="login-title">{{ isRegister ? '📝 注册账号' : '🎯 登录' }}</text>
      <text class="login-sub">找搭子 · 找到志同道合的伙伴</text>

      <view class="form-item">
        <input class="form-input" v-model="form.phone" placeholder="手机号" type="text" maxlength="11" />
      </view>
      <view class="form-item" v-if="isRegister">
        <input class="form-input" v-model="form.nickname" placeholder="昵称（选填）" type="text" />
      </view>
      <view class="form-item">
        <input class="form-input" v-model="form.password" placeholder="密码（至少6位）" :password="true" type="text" />
      </view>

      <button class="submit-btn" @tap="handleSubmit" :disabled="loading">
        {{ loading ? '处理中...' : (isRegister ? '注 册' : '登 录') }}
      </button>

      <view class="switch-row">
        <text class="switch-text">{{ isRegister ? '已有账号？' : '还没有账号？' }}</text>
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

<style scoped>
.login-wrap {
  padding: 200rpx 48rpx 0;
}
.login-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  box-shadow: 0 4rpx 20rpx rgba(244,114,182,0.1);
}
.login-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #4a2040;
  display: block;
  margin-bottom: 8rpx;
}
.login-sub {
  font-size: 24rpx;
  color: #b8929e;
  display: block;
  margin-bottom: 48rpx;
}
.form-item {
  margin-bottom: 24rpx;
}
.form-input {
  background: #fff5f7;
  border: 2rpx solid #fce4ec;
  border-radius: 16rpx;
  padding: 24rpx;
  color: #4a2040;
  font-size: 28rpx;
  width: 100%;
  box-sizing: border-box;
}
.submit-btn {
  background: linear-gradient(135deg, #f472b6, #c084fc);
  border-radius: 40rpx;
  padding: 24rpx;
  text-align: center;
  color: white;
  font-size: 30rpx;
  font-weight: 700;
  margin-top: 16rpx;
  border: none;
}
.switch-row {
  display: flex;
  justify-content: center;
  gap: 8rpx;
  margin-top: 28rpx;
}
.switch-text { font-size: 24rpx; color: #b8929e; }
.switch-link { font-size: 24rpx; color: #f472b6; }
</style>
