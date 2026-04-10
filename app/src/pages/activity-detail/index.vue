<template>
  <view class="page" v-if="activity">
    <view class="card">
      <text class="cat-badge">{{ activity.category }}</text>
      <text class="title">{{ activity.title }}</text>

      <view class="author-block" @tap="goUser(activity.userId)">
        <view class="avatar">{{ (activity.authorName || '?').charAt(0) }}</view>
        <view>
          <text class="aname">{{ activity.authorName }}</text>
          <text class="asub">{{ [activity.authorCity, activity.authorAge ? activity.authorAge+'岁' : '', activity.authorGender===1?'男':activity.authorGender===2?'女':''].filter(Boolean).join(' · ') }}</text>
        </view>
      </view>

      <text class="desc">{{ activity.description }}</text>

      <view class="info-grid">
        <view v-if="activity.location" class="info-row"><text class="label">📍 地点</text><text>{{ activity.location }}</text></view>
        <view v-if="activity.activityTime" class="info-row"><text class="label">🕐 时间</text><text>{{ activity.activityTime }}</text></view>
        <view class="info-row"><text class="label">👥 人数</text><text>{{ activity.joinedCount||0 }}/{{ activity.maxPeople||1 }}</text></view>
      </view>
    </view>

    <view v-if="canApply" class="apply-box">
      <textarea class="apply-input" v-model="applyMsg" placeholder="写一句话介绍自己..." maxlength="200" />
      <button class="apply-btn" @tap="doApply" :loading="applying">申请加入</button>
    </view>
    <view v-else-if="!isLoggedIn" class="apply-box">
      <button class="login-btn" @tap="goLogin">登录后申请加入</button>
    </view>
    <view v-else-if="applied" class="status-tip">✅ 已申请，等待对方确认</view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getActivityDetail } from '@/api/activity'
import { applyActivity, getSentRequests } from '@/api/match'

const activity = ref(null)
const applyMsg = ref('')
const applying = ref(false)
const applied = ref(false)

const isLoggedIn = computed(() => !!uni.getStorageSync('token'))
const myUserId = computed(() => uni.getStorageSync('userId'))
const isOwner = computed(() => activity.value && myUserId.value && activity.value.userId == myUserId.value)
const canApply = computed(() => isLoggedIn.value && !isOwner.value && !applied.value && activity.value?.status === 1)

function goUser(id) { uni.navigateTo({ url: `/pages/profile/index?userId=${id}` }) }
function goLogin() { uni.navigateTo({ url: '/pages/login/index' }) }

async function doApply() {
  applying.value = true
  try {
    await applyActivity({ activityId: activity.value.id, message: applyMsg.value })
    uni.showToast({ title: '申请已发送！', icon: 'success' })
    applied.value = true
  } catch {}
  applying.value = false
}

import { onLoad } from '@dcloudio/uni-app'

onLoad(async (options) => {
  const id = options?.id
  if (id) activity.value = await getActivityDetail(id)
  if (isLoggedIn.value) {
    try { const sent = await getSentRequests(); applied.value = (sent||[]).some(r => r.activityId == id) } catch {}
  }
})
</script>

<style>
.page { padding: 24rpx; background: #fff5f7; min-height: 100vh; }
.card { background: #ffffff; border-radius: 20rpx; padding: 32rpx; }
.cat-badge { display: inline-block; background: rgba(99,102,241,0.15); color: #f472b6; padding: 8rpx 20rpx; border-radius: 16rpx; font-size: 24rpx; margin-bottom: 12rpx; }
.title { display: block; font-size: 38rpx; font-weight: 800; color: #4a2040; margin-bottom: 20rpx; }
.author-block { display: flex; gap: 16rpx; align-items: center; padding: 16rpx; background: rgba(255,255,255,0.03); border-radius: 12rpx; margin-bottom: 20rpx; }
.avatar { width: 72rpx; height: 72rpx; border-radius: 50%; background: #f472b6; color: white; display: flex; align-items: center; justify-content: center; font-size: 32rpx; font-weight: 700; }
.aname { font-size: 30rpx; font-weight: 600; color: #4a2040; display: block; }
.asub { font-size: 24rpx; color: #b8929e; }
.desc { display: block; font-size: 28rpx; line-height: 1.7; color: #7c5270; margin-bottom: 20rpx; }
.info-grid { display: flex; flex-direction: column; gap: 12rpx; }
.info-row { display: flex; justify-content: space-between; padding: 12rpx 16rpx; background: rgba(255,255,255,0.03); border-radius: 10rpx; font-size: 28rpx; color: #4a2040; }
.label { color: #b8929e; }
.apply-box { background: #ffffff; border-radius: 20rpx; padding: 24rpx; margin-top: 20rpx; }
.apply-input { background: #fff5f7; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx; width: 100%; min-height: 120rpx; }
.apply-btn { background: linear-gradient(135deg,#f472b6,#c084fc); color: white; border-radius: 12rpx; font-size: 30rpx; font-weight: 600; margin-top: 16rpx; }
.login-btn { background: linear-gradient(135deg,#f472b6,#c084fc); color: white; border-radius: 12rpx; font-size: 30rpx; font-weight: 600; }
.status-tip { text-align: center; padding: 24rpx; color: #b8929e; font-size: 28rpx; margin-top: 20rpx; }
</style>
