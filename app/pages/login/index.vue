<template>
  <view style="padding: 150rpx 48rpx 0;">
    <view style="background: #fff; border-radius: 24rpx; padding: 60rpx 40rpx; text-align: center;">
      <!-- 登录/注册 -->
      <view v-if="mode !== 'reset'">
        <text style="font-size: 40rpx; font-weight: 800; color: #4a2040; display: block; margin-bottom: 8rpx;">{{ isRegister ? '📝 注册' : '🎯 登录' }}</text>
        <text style="font-size: 24rpx; color: #b8929e; display: block; margin-bottom: 48rpx;">同好出发 · 发现有趣的活动，找到一起玩的人</text>

        <input v-model="form.phone" placeholder="手机号" maxlength="11"
          style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 16rpx; padding: 24rpx; color: #4a2040; font-size: 28rpx; margin-bottom: 24rpx;" />

        <input v-if="isRegister" v-model="form.nickname" placeholder="昵称（选填）"
          style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 16rpx; padding: 24rpx; color: #4a2040; font-size: 28rpx; margin-bottom: 24rpx;" />

        <view style="position:relative;">
          <input v-model="form.password" placeholder="密码（至少6位）" :password="!showPwd"
            style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 16rpx; padding: 24rpx; padding-right: 80rpx; color: #4a2040; font-size: 28rpx; margin-bottom: 12rpx;" />
          <text @tap="showPwd = !showPwd"
            style="position:absolute; right: 20rpx; top: 22rpx; font-size: 28rpx; color: #b8929e;">{{ showPwd ? '🙈' : '👁' }}</text>
        </view>

        <view v-if="!isRegister" style="text-align:right;margin-bottom:24rpx;">
          <text style="font-size:24rpx;color:#f472b6;" @tap="mode='reset'">忘记密码？</text>
        </view>

        <button @tap="handleSubmit" :disabled="loading"
          style="background: linear-gradient(135deg, #f472b6, #c084fc); color: white; border: none; border-radius: 40rpx; padding: 24rpx; font-size: 30rpx; font-weight: 700;">
          {{ loading ? '处理中...' : (isRegister ? '注 册' : '登 录') }}
        </button>

        <view v-if="isRegister" style="display: flex; align-items: center; justify-content: center; gap: 8rpx; margin-top: 20rpx;">
          <checkbox :checked="agreePrivacy" @tap="agreePrivacy = !agreePrivacy" style="transform: scale(0.7);" />
          <text style="font-size: 22rpx; color: #b8929e;">我已阅读并同意</text>
          <text style="font-size: 22rpx; color: #f472b6;" @tap="uni.navigateTo({url:'/pages/privacy/index'})">《隐私政策》</text>
        </view>

        <view style="display: flex; justify-content: center; gap: 8rpx; margin-top: 28rpx;">
          <text style="font-size: 24rpx; color: #b8929e;">{{ isRegister ? '已有账号？' : '还没有账号？' }}</text>
          <text style="font-size: 24rpx; color: #f472b6;" @tap="isRegister = !isRegister">{{ isRegister ? '去登录' : '立即注册' }}</text>
        </view>
      </view>

      <!-- 重置密码 -->
      <view v-else>
        <text style="font-size: 40rpx; font-weight: 800; color: #4a2040; display: block; margin-bottom: 8rpx;">🔑 重置密码</text>
        <text style="font-size: 24rpx; color: #b8929e; display: block; margin-bottom: 48rpx;">输入注册手机号和新密码</text>

        <input v-model="resetForm.phone" placeholder="手机号" maxlength="11"
          style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 16rpx; padding: 24rpx; color: #4a2040; font-size: 28rpx; margin-bottom: 24rpx;" />

        <view style="position:relative;">
          <input v-model="resetForm.newPassword" placeholder="新密码（至少6位）" :password="!showResetPwd"
            style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 16rpx; padding: 24rpx; padding-right: 80rpx; color: #4a2040; font-size: 28rpx; margin-bottom: 24rpx;" />
          <text @tap="showResetPwd = !showResetPwd"
            style="position:absolute; right: 20rpx; top: 22rpx; font-size: 28rpx; color: #b8929e;">{{ showResetPwd ? '🙈' : '👁' }}</text>
        </view>

        <button @tap="handleReset" :disabled="loading"
          style="background: linear-gradient(135deg, #f472b6, #c084fc); color: white; border: none; border-radius: 40rpx; padding: 24rpx; font-size: 30rpx; font-weight: 700;">
          {{ loading ? '处理中...' : '重置密码' }}
        </button>

        <view style="margin-top: 28rpx;">
          <text style="font-size: 24rpx; color: #f472b6;" @tap="mode='login'">← 返回登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { register, login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { request } from '@/api/request'

const userStore = useUserStore()
const loading = ref(false)
const isRegister = ref(false)
const agreePrivacy = ref(false)
const showPwd = ref(false)
const showResetPwd = ref(false)
const mode = ref('login') // login | reset
const form = reactive({ phone: '', password: '', nickname: '' })
const resetForm = reactive({ phone: '', newPassword: '' })

async function handleSubmit() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  if (!form.password || form.password.length < 6) return uni.showToast({ title: '密码至少6位', icon: 'none' })
  if (isRegister.value && !agreePrivacy.value) return uni.showToast({ title: '请先同意隐私政策', icon: 'none' })

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

async function handleReset() {
  if (!resetForm.phone || !/^1[3-9]\d{9}$/.test(resetForm.phone)) return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  if (!resetForm.newPassword || resetForm.newPassword.length < 6) return uni.showToast({ title: '新密码至少6位', icon: 'none' })

  loading.value = true
  try {
    await request({ url: '/auth/reset-password', method: 'POST', data: resetForm })
    uni.showToast({ title: '密码重置成功', icon: 'success' })
    mode.value = 'login'
  } catch {}
  loading.value = false
}
</script>
