<template>
  <view class="page">
    <view v-if="isLoggedIn" class="card user-card">
      <view class="user-top">
        <view class="avatar-big">{{ (nickname || '?').charAt(0) }}</view>
        <view>
          <text class="user-name">{{ nickname || '用户' }}</text>
          <text class="user-phone">{{ phone || '' }}</text>
        </view>
      </view>
    </view>

    <view v-if="isLoggedIn" class="card">
      <view class="menu-item" @tap="goTo('/pages/profile/index')">
        <text>✏️ 编辑资料</text><text class="arrow">›</text>
      </view>
      <view class="menu-item" @tap="goTo('/pages/publish/index')">
        <text>📝 发布活动</text><text class="arrow">›</text>
      </view>
      <view class="menu-item" @tap="goTab('/pages/messages/index')">
        <text>💬 我的消息</text><text class="arrow">›</text>
      </view>
    </view>

    <view v-if="isLoggedIn" class="card">
      <view class="menu-item" @tap="goTo('/pages/my-data/index')">
        <text>📦 我的数据（导出/注销）</text><text class="arrow">›</text>
      </view>
      <view class="menu-item" @tap="goTo('/pages/privacy/index')">
        <text>🔒 隐私政策</text><text class="arrow">›</text>
      </view>
    </view>

    <view v-if="isLoggedIn" style="padding:20rpx;">
      <button class="btn-logout" @tap="doLogout">退出登录</button>
    </view>

    <view v-if="!isLoggedIn" class="card" style="text-align:center;padding:60rpx;">
      <text style="font-size:40rpx;display:block;margin-bottom:20rpx;">🎯</text>
      <text style="font-size:30rpx;font-weight:700;color:#4a2040;display:block;margin-bottom:12rpx;">同好出发</text>
      <text style="font-size:26rpx;color:#b8929e;display:block;margin-bottom:32rpx;">登录后发布活动、参与活动</text>
      <button class="btn-primary" @tap="goTo('/pages/login/index')">登录 / 注册</button>
      <view style="margin-top:20rpx;">
        <text style="font-size:24rpx;color:#b8929e;" @tap="goTo('/pages/privacy/index')">🔒 隐私政策</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const isLoggedIn = ref(false)
const nickname = ref('')
const phone = ref('')

function goTo(url) { uni.navigateTo({ url }) }
function goTab(url) { uni.switchTab({ url }) }

function checkLogin() {
  const token = uni.getStorageSync('token')
  isLoggedIn.value = !!token
  nickname.value = uni.getStorageSync('nickname') || ''
  phone.value = uni.getStorageSync('phone') || ''
}

function doLogout() {
  uni.showModal({
    title: '提示', content: '确定退出登录？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userId')
        uni.removeStorageSync('nickname')
        uni.removeStorageSync('phone')
        isLoggedIn.value = false
        uni.showToast({ title: '已退出', icon: 'success' })
      }
    }
  })
}

onMounted(checkLogin)
// 每次显示时刷新状态
uni.$on('pageShow', checkLogin)
</script>

<style scoped>
.page { padding: 20rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 0; margin-bottom: 20rpx; border: 1px solid #fce4ec; overflow: hidden; }
.user-card { padding: 32rpx; }
.user-top { display: flex; gap: 20rpx; align-items: center; }
.avatar-big { width: 100rpx; height: 100rpx; border-radius: 50%; background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 42rpx; font-weight: 700; }
.user-name { font-size: 34rpx; font-weight: 800; color: #4a2040; display: block; }
.user-phone { font-size: 26rpx; color: #b8929e; display: block; margin-top: 4rpx; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 28rpx 32rpx; border-bottom: 1px solid #fce4ec; font-size: 28rpx; color: #4a2040; }
.menu-item:last-child { border-bottom: none; }
.arrow { color: #b8929e; font-size: 32rpx; }
.btn-primary { background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 30rpx; font-weight: 600; }
.btn-logout { background: #fff; color: #e11d48; border: 2px solid #fce4ec; border-radius: 40rpx; font-size: 28rpx; }
</style>
