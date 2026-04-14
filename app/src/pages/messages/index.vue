<template>
  <view class="page">
    <view class="tabs">
      <text :class="['tab', tab === 'joined' && 'active']" @tap="tab = 'joined'">我的活动</text>
      <text :class="['tab', tab === 'received' && 'active']" @tap="tab = 'received'">收到的申请{{ pendingCount > 0 ? ` (${pendingCount})` : '' }}</text>
      <text :class="['tab', tab === 'sent' && 'active']" @tap="tab = 'sent'">我的申请</text>
    </view>

    <!-- 我的活动（已通过的） -->
    <view v-if="tab === 'joined'">
      <view v-if="joinedActivities.length === 0" class="empty">暂无已参与的活动</view>
      <view v-for="s in joinedActivities" :key="s.id" class="act-item" @tap="goActivity(s.activityId)">
        <view class="act-top">
          <text class="act-title">{{ s.activityTitle }}</text>
          <text class="act-status">已通过 ✅</text>
        </view>
        <text class="act-sub">发起者：{{ s.authorName }}</text>
      </view>
    </view>

    <!-- 收到的申请（答卷） -->
    <view v-if="tab === 'received'">
      <view v-if="received.length === 0" class="empty">暂无收到的申请</view>
      <view v-for="r in received" :key="r.id" class="req-item">
        <view class="req-top">
          <view class="avatar-sm">{{ (r.applicantName || '?').charAt(0) }}</view>
          <view>
            <text class="req-name">{{ r.applicantName }}</text>
            <text class="req-sub">申请参与：{{ r.activityTitle }}</text>
          </view>
        </view>
        <text v-if="r.status === 0" class="status-yellow">⏳ 待批改答卷</text>
        <text v-else-if="r.status === 1" class="status-green">已通过 ✅</text>
        <text v-else-if="r.status === 2" class="status-red">❌ 未通过</text>
        <view v-if="r.status === 0" style="margin-top:12rpx;">
          <button class="btn-review" @tap="goReview(r.activityId)">去批改答卷 →</button>
        </view>
      </view>
    </view>

    <!-- 我的申请 -->
    <view v-if="tab === 'sent'">
      <view v-if="sent.length === 0" class="empty">暂无发出的申请</view>
      <view v-for="s in sent" :key="s.id" class="req-item" @tap="goActivity(s.activityId)">
        <text class="req-title">{{ s.activityTitle }}</text>
        <text class="req-sub">发起者：{{ s.authorName }}</text>
        <text v-if="s.status === 0" class="status-yellow">⏳ 已提交答卷，等待批改</text>
        <text v-else-if="s.status === 1" class="status-green">已通过 ✅ 点击查看活动详情</text>
        <text v-else class="status-gray">未通过</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { getReceivedRequests, getSentRequests } from '@/api/match'

const tab = ref('joined')
const received = ref([])
const sent = ref([])
let pollTimer = null

const pendingCount = computed(() => (received.value || []).filter(r => r.status === 0).length)
const joinedActivities = computed(() => (sent.value || []).filter(s => s.status === 1))

function goActivity(activityId) { uni.navigateTo({ url: `/pages/activity-detail/index?id=${activityId}` }) }
function goReview(activityId) { uni.navigateTo({ url: `/pages/quiz-papers/index?activityId=${activityId}` }) }

async function loadAll() {
  const [r, s] = await Promise.allSettled([getReceivedRequests(), getSentRequests()])
  received.value = r.status === 'fulfilled' ? (r.value || []) : received.value
  sent.value = s.status === 'fulfilled' ? (s.value || []) : sent.value
  updateBadge()
}

function updateBadge() {
  const total = pendingCount.value
  if (total > 0) uni.setTabBarBadge({ index: 1, text: String(total > 99 ? '99+' : total), fail() {} })
  else uni.removeTabBarBadge({ index: 1, fail() {} })
}

onShow(() => { loadAll(); pollTimer = setInterval(loadAll, 8000) })
onHide(() => { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } })
onUnmounted(() => { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } })
</script>

<style scoped>
.page { padding: 20rpx; }
.tabs { display: flex; gap: 0; margin-bottom: 20rpx; background: #fff; border-radius: 16rpx; overflow: hidden; border: 1px solid #fce4ec; }
.tab { flex: 1; text-align: center; padding: 20rpx 0; font-size: 26rpx; color: #b8929e; }
.tab.active { color: #ec4899; font-weight: 700; background: #fff0f5; }
.empty { text-align: center; padding: 60rpx; color: #b8929e; font-size: 26rpx; }
.act-item { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; border: 1px solid #fce4ec; }
.act-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.act-title { font-size: 28rpx; font-weight: 600; color: #4a2040; }
.act-status { font-size: 24rpx; color: #059669; font-weight: 600; }
.act-sub { font-size: 24rpx; color: #b8929e; }
.avatar-sm { width: 60rpx; height: 60rpx; border-radius: 50%; background: #f9a8d4; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 700; flex-shrink: 0; }
.req-item { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; border: 1px solid #fce4ec; }
.req-top { display: flex; gap: 12rpx; align-items: center; margin-bottom: 8rpx; }
.req-name { font-size: 28rpx; font-weight: 600; color: #4a2040; display: block; }
.req-sub { font-size: 24rpx; color: #b8929e; display: block; }
.req-title { font-size: 28rpx; font-weight: 600; color: #4a2040; display: block; }
.status-green { color: #059669; font-size: 24rpx; font-weight: 600; }
.status-yellow { color: #d97706; font-size: 24rpx; }
.status-red { color: #e11d48; font-size: 24rpx; font-weight: 600; }
.status-gray { color: #b8929e; font-size: 24rpx; }
.btn-review { background: linear-gradient(135deg,#f472b6,#c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 26rpx; }
</style>
