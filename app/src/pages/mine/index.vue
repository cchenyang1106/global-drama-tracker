<template>
  <view class="page">
    <view v-if="isLoggedIn" class="user-card">
      <view class="user-top">
        <view class="big-avatar">{{ (nickname || '?').charAt(0) }}</view>
        <view>
          <text class="uname">{{ nickname || '用户' }}</text>
          <text class="usub">{{ phone }}</text>
        </view>
      </view>

      <view class="menu-list">
        <view class="menu-item" @tap="goPage('/pages/profile/index')">
          <text>📝 编辑资料</text><text class="arrow">›</text>
        </view>
        <view class="menu-item" @tap="goPage('/pages/publish/index')">
          <text>✏️ 发布活动</text><text class="arrow">›</text>
        </view>
        <view class="menu-item" @tap="switchTab('/pages/messages/index')">
          <text>💬 我的消息</text><text class="arrow">›</text>
        </view>
      </view>

      <button class="logout-btn" @tap="doLogout">退出登录</button>
    </view>

    <view v-else class="login-card">
      <text class="login-title">🎯 找搭子</text>
      <text class="login-sub">登录后发布活动，找到志同道合的伙伴</text>
      <button class="login-btn" @tap="goLogin">登录 / 注册</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const isLoggedIn = computed(() => !!uni.getStorageSync('token'))
const nickname = ref('')
const phone = ref('')

function goPage(url) { uni.navigateTo({ url }) }
function switchTab(url) { uni.switchTab({ url }) }
function goLogin() { uni.navigateTo({ url: '/pages/login/index' }) }

function doLogout() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userId')
  uni.removeStorageSync('nickname')
  uni.showToast({ title: '已退出', icon: 'success' })
  nickname.value = ''
  phone.value = ''
}

onMounted(() => {
  nickname.value = uni.getStorageSync('nickname') || ''
  phone.value = uni.getStorageSync('phone') || ''
})
</script>

<style>
.page { padding: 24rpx; background: #0f172a; min-height: 100vh; }
.user-card { background: #1e293b; border-radius: 20rpx; padding: 32rpx; }
.user-top { display: flex; gap: 20rpx; align-items: center; margin-bottom: 32rpx; }
.big-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; background: #6366f1; color: white; display: flex; align-items: center; justify-content: center; font-size: 40rpx; font-weight: 700; }
.uname { display: block; font-size: 36rpx; font-weight: 800; color: #f1f5f9; }
.usub { display: block; font-size: 26rpx; color: #64748b; }
.menu-list { margin-bottom: 24rpx; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid rgba(255,255,255,0.05); font-size: 30rpx; color: #e2e8f0; }
.arrow { color: #64748b; font-size: 36rpx; }
.logout-btn { background: #334155; color: #f87171; border-radius: 12rpx; font-size: 30rpx; }

.login-card { background: #1e293b; border-radius: 20rpx; padding: 80rpx 40rpx; text-align: center; }
.login-title { display: block; font-size: 48rpx; font-weight: 800; color: #818cf8; margin-bottom: 12rpx; }
.login-sub { display: block; font-size: 28rpx; color: #64748b; margin-bottom: 40rpx; }
.login-btn { background: linear-gradient(135deg,#6366f1,#8b5cf6); color: white; border-radius: 12rpx; font-size: 32rpx; font-weight: 700; }
</style>
