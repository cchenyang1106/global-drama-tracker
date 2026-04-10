<template>
  <view class="page-bg">
    <!-- 已登录 -->
    <view v-if="userStore.isLoggedIn" class="mine-page">
      <view class="user-card">
        <view class="avatar">{{ userStore.nickname?.charAt(0) || '?' }}</view>
        <view class="user-info">
          <text class="user-name">{{ userStore.nickname }}</text>
          <text class="user-phone">{{ userStore.user?.phone }}</text>
        </view>
      </view>

      <view class="menu-list">
        <view class="menu-item" @tap="goPage('/pages/dramas/index')">
          <text>📺 我的收藏</text>
          <text class="arrow">></text>
        </view>
        <view class="menu-item" @tap="goPage('/pages/ranking/index')">
          <text>🏆 排行榜</text>
          <text class="arrow">></text>
        </view>
      </view>

      <view class="logout-btn" @tap="handleLogout">
        <text>退出登录</text>
      </view>
    </view>

    <!-- 未登录 -->
    <view v-else class="mine-page">
      <view class="not-login">
        <text class="nl-icon">🎬</text>
        <text class="nl-title">登录 Drama Tracker</text>
        <text class="nl-desc">登录后可以评论、收藏、打分</text>
        <view class="nl-btn" @tap="goLogin">
          <text>立即登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

function goLogin() { uni.navigateTo({ url: '/pages/login/index' }) }
function goPage(url) { uni.navigateTo({ url }) }

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录？',
    success(res) {
      if (res.confirm) {
        userStore.logout()
        uni.showToast({ title: '已退出', icon: 'success' })
      }
    },
  })
}

onShow(() => { userStore.init() })
</script>

<style lang="scss">
.mine-page { padding: 24rpx; }

.user-card {
  display: flex; align-items: center; gap: 24rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20rpx; padding: 40rpx 30rpx;
}
.avatar {
  width: 100rpx; height: 100rpx; border-radius: 50%;
  background: rgba(255,255,255,0.2); color: white;
  font-size: 44rpx; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  text-align: center; line-height: 100rpx;
}
.user-info { flex: 1; }
.user-name { font-size: 34rpx; font-weight: 700; color: white; display: block; }
.user-phone { font-size: 24rpx; color: rgba(255,255,255,0.7); margin-top: 6rpx; display: block; }

.menu-list { margin-top: 32rpx; }
.menu-item {
  display: flex; justify-content: space-between; align-items: center;
  background: #1e293b; border-radius: 16rpx; padding: 28rpx 24rpx;
  margin-bottom: 12rpx; font-size: 28rpx; color: #f1f5f9;
}
.arrow { color: #64748b; }

.logout-btn {
  margin-top: 60rpx; text-align: center;
  padding: 24rpx; background: rgba(239,68,68,0.1);
  border-radius: 16rpx; color: #f87171; font-size: 28rpx;
}

.not-login { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
.nl-icon { font-size: 100rpx; margin-bottom: 20rpx; }
.nl-title { font-size: 36rpx; font-weight: 700; color: #f1f5f9; margin-bottom: 10rpx; }
.nl-desc { font-size: 26rpx; color: #94a3b8; margin-bottom: 40rpx; }
.nl-btn {
  padding: 20rpx 60rpx; background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 40rpx; color: white; font-size: 30rpx; font-weight: 600;
}
</style>
