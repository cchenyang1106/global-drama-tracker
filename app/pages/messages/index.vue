<template>
  <view class="page">
    <view class="tabs">
      <text :class="['tab', tab === 'groups' && 'active']" @tap="tab = 'groups'">群聊{{ groupUnreadTotal > 0 ? ` (${groupUnreadTotal})` : '' }}</text>
      <text :class="['tab', tab === 'received' && 'active']" @tap="tab = 'received'">收到的申请{{ pendingCount > 0 ? ` (${pendingCount})` : '' }}</text>
      <text :class="['tab', tab === 'sent' && 'active']" @tap="tab = 'sent'">我的申请</text>
    </view>

    <!-- 群聊列表 -->
    <view v-if="tab === 'groups'">
      <view v-if="groups.length === 0" class="empty">暂无群聊，参与活动组队后自动创建</view>
      <view v-for="g in groups" :key="g.groupId" class="chat-item" @tap="goGroup(g.groupId)">
        <view class="avatar-wrap">
          <view class="avatar-circle">{{ (g.name || '?').charAt(0) }}</view>
          <text v-if="g.unreadCount > 0" class="badge">{{ g.unreadCount > 99 ? '99+' : g.unreadCount }}</text>
        </view>
        <view style="flex:1;min-width:0;">
          <view class="chat-top">
            <text class="chat-name">{{ g.name }}</text>
            <text class="chat-sub">{{ g.memberCount }}人</text>
          </view>
          <text class="chat-msg">{{ g.lastMessage || '暂无消息' }}</text>
        </view>
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
        <text v-else-if="r.status === 1" class="status-green">已通过 ✅ 已加入活动群聊</text>
        <text v-else-if="r.status === 2" class="status-red">❌ 未通过</text>
        <view v-if="r.status === 0" style="margin-top:12rpx;">
          <button class="btn-review" @tap="goReview(r.activityId)">去批改答卷 →</button>
        </view>
      </view>
    </view>

    <!-- 我的申请 -->
    <view v-if="tab === 'sent'">
      <view v-if="sent.length === 0" class="empty">暂无发出的申请</view>
      <view v-for="s in sent" :key="s.id" class="req-item">
        <text class="req-title">{{ s.activityTitle }}</text>
        <text class="req-sub">发起者：{{ s.authorName }}</text>
        <text v-if="s.status === 0" class="status-yellow">⏳ 已提交答卷，等待批改</text>
        <text v-else-if="s.status === 1" class="status-green">已通过 ✅ 已自动加入群聊</text>
        <text v-else class="status-gray">未通过</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { getReceivedRequests, getSentRequests } from '@/api/match'
import { getGroupList } from '@/api/group'

const tab = ref('groups')
const groups = ref([])
const received = ref([])
const sent = ref([])
const refreshing = ref(false)
let pollTimer = null

const groupUnreadTotal = computed(() => (groups.value || []).reduce((sum, g) => sum + (g.unreadCount || 0), 0))
const pendingCount = computed(() => (received.value || []).filter(r => r.status === 0).length)

function goGroup(groupId) { uni.navigateTo({ url: `/pages/group-chat/index?groupId=${groupId}` }) }
function goReview(activityId) { uni.navigateTo({ url: `/pages/quiz-papers/index?activityId=${activityId}` }) }

async function loadAll() {
  refreshing.value = true
  const [g, r, s] = await Promise.allSettled([getGroupList(), getReceivedRequests(), getSentRequests()])
  groups.value = g.status === 'fulfilled' ? (g.value || []) : groups.value
  received.value = r.status === 'fulfilled' ? (r.value || []) : received.value
  sent.value = s.status === 'fulfilled' ? (s.value || []) : sent.value
  updateBadge()
  refreshing.value = false
}

function updateBadge() {
  const total = groupUnreadTotal.value + pendingCount.value
  if (total > 0) uni.setTabBarBadge({ index: 1, text: String(total > 99 ? '99+' : total) })
  else uni.removeTabBarBadge({ index: 1 })
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
.chat-item { display: flex; gap: 16rpx; align-items: center; background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; border: 1px solid #fce4ec; }
.avatar-wrap { position: relative; flex-shrink: 0; }
.avatar-circle { width: 80rpx; height: 80rpx; border-radius: 50%; background: linear-gradient(135deg,#f472b6,#c084fc); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 32rpx; font-weight: 700; }
.badge { position: absolute; top: -8rpx; right: -8rpx; min-width: 32rpx; height: 32rpx; line-height: 32rpx; text-align: center; background: #e11d48; color: #fff; font-size: 20rpx; font-weight: 700; border-radius: 32rpx; padding: 0 8rpx; }
.avatar-sm { width: 60rpx; height: 60rpx; border-radius: 50%; background: #f9a8d4; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 700; flex-shrink: 0; }
.chat-top { display: flex; justify-content: space-between; align-items: center; }
.chat-name { font-size: 28rpx; font-weight: 600; color: #4a2040; }
.chat-sub { font-size: 22rpx; color: #b8929e; }
.chat-msg { font-size: 24rpx; color: #b8929e; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; margin-top: 6rpx; }
.req-item { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; border: 1px solid #fce4ec; }
.req-top { display: flex; gap: 12rpx; align-items: center; margin-bottom: 8rpx; }
.req-name { font-size: 28rpx; font-weight: 600; color: #4a2040; display: block; }
.req-sub { font-size: 24rpx; color: #b8929e; display: block; }
.req-title { font-size: 28rpx; font-weight: 600; color: #4a2040; display: block; }
.req-msg { font-size: 24rpx; color: #7c5270; font-style: italic; display: block; margin: 8rpx 0; }
.status-green { color: #059669; font-size: 24rpx; font-weight: 600; }
.status-yellow { color: #d97706; font-size: 24rpx; }
.status-red { color: #e11d48; font-size: 24rpx; font-weight: 600; }
.btn-review { background: linear-gradient(135deg,#f472b6,#c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 26rpx; }
</style>
