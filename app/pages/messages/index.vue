<template>
  <view class="page">
    <view class="tabs">
      <text :class="['tab', tab === 'chats' && 'active']" @tap="tab = 'chats'">聊天</text>
      <text :class="['tab', tab === 'received' && 'active']" @tap="tab = 'received'">收到的申请</text>
      <text :class="['tab', tab === 'sent' && 'active']" @tap="tab = 'sent'">我的申请</text>
    </view>

    <!-- 聊天列表 -->
    <view v-if="tab === 'chats'">
      <view v-if="chats.length === 0" class="empty">暂无聊天，去活动广场看看吧！</view>
      <view v-for="c in chats" :key="c.matchId" class="chat-item" @tap="goChat(c.matchId)">
        <view class="avatar-circle">{{ (c.partnerName || '?').charAt(0) }}</view>
        <view style="flex:1;min-width:0;">
          <view class="chat-top">
            <text class="chat-name">{{ c.partnerName }}</text>
            <text class="chat-time">{{ formatTime(c.lastTime) }}</text>
          </view>
          <view class="chat-bottom">
            <text class="chat-msg">{{ c.lastMessage || '暂无消息' }}</text>
            <view v-if="c.unreadCount > 0" class="badge">{{ c.unreadCount > 99 ? '99+' : c.unreadCount }}</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 收到的申请 -->
    <view v-if="tab === 'received'">
      <view v-if="received.length === 0" class="empty">暂无收到的申请</view>
      <view v-for="r in received" :key="r.id" class="req-item">
        <view class="req-top">
          <view class="avatar-sm">{{ (r.applicantName || '?').charAt(0) }}</view>
          <view>
            <text class="req-name">{{ r.applicantName }} <text v-if="r.applicantCity" style="color:#b8929e;">· {{ r.applicantCity }}</text></text>
            <text class="req-sub">想和你聊聊：{{ r.activityTitle }}</text>
          </view>
        </view>
        <text v-if="r.message" class="req-msg">"{{ r.message }}"</text>
        <view v-if="r.status === 0" class="req-actions">
          <button class="btn-accept" @tap="handleReq(r.id, 1)">同意</button>
          <button class="btn-reject" @tap="handleReq(r.id, 2)">拒绝</button>
        </view>
        <text v-else-if="r.status === 1" class="status-green">已同意 ✅</text>
        <text v-else class="status-gray">已拒绝</text>
      </view>
    </view>

    <!-- 我的申请 -->
    <view v-if="tab === 'sent'">
      <view v-if="sent.length === 0" class="empty">暂无发出的申请</view>
      <view v-for="s in sent" :key="s.id" class="req-item">
        <text class="req-title">{{ s.activityTitle }}</text>
        <text class="req-sub">发起者：{{ s.authorName }}</text>
        <text v-if="s.message" class="req-msg">"{{ s.message }}"</text>
        <text v-if="s.status === 0" class="status-yellow">⏳ 等待确认</text>
        <text v-else-if="s.status === 1" class="status-green">已组队 ✅</text>
        <text v-else class="status-gray">已拒绝</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { getChatList, getReceivedRequests, getSentRequests, handleRequest } from '@/api/match'

const tab = ref('chats')
const chats = ref([])
const received = ref([])
const sent = ref([])
let pollTimer = null

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t.replace('T', ' ').replace(/-/g, '/'))
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  if (d.toDateString() === now.toDateString()) return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function goChat(matchId) { uni.navigateTo({ url: `/pages/chat/index?matchId=${matchId}` }) }

async function loadAll() {
  try { chats.value = await getChatList() || [] } catch {}
  try { received.value = await getReceivedRequests() || [] } catch {}
  try { sent.value = await getSentRequests() || [] } catch {}
  updateBadge()
}

function updateBadge() {
  const unread = (chats.value || []).reduce((sum, c) => sum + (c.unreadCount || 0), 0)
  const pending = (received.value || []).filter(r => r.status === 0).length
  const total = unread + pending
  if (total > 0) {
    uni.setTabBarBadge({ index: 1, text: String(total > 99 ? '99+' : total) })
  } else {
    uni.removeTabBarBadge({ index: 1 })
  }
}

async function handleReq(id, action) {
  try {
    await handleRequest(id, action)
    uni.showToast({ title: action === 1 ? '已同意' : '已拒绝', icon: 'success' })
    loadAll()
  } catch {}
}

onShow(() => {
  loadAll()
  pollTimer = setInterval(loadAll, 10000) // 每10秒刷新
})

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
.avatar-circle { width: 80rpx; height: 80rpx; border-radius: 50%; background: #f472b6; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 32rpx; font-weight: 700; flex-shrink: 0; }
.avatar-sm { width: 60rpx; height: 60rpx; border-radius: 50%; background: #f9a8d4; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 700; flex-shrink: 0; }
.chat-top { display: flex; justify-content: space-between; }
.chat-name { font-size: 28rpx; font-weight: 600; color: #4a2040; }
.chat-time { font-size: 22rpx; color: #b8929e; }
.chat-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 6rpx; }
.chat-msg { font-size: 24rpx; color: #b8929e; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.badge { min-width: 32rpx; height: 32rpx; padding: 0 8rpx; border-radius: 16rpx; background: #f43f5e; color: #fff; font-size: 20rpx; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.req-item { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; border: 1px solid #fce4ec; }
.req-top { display: flex; gap: 12rpx; align-items: center; margin-bottom: 8rpx; }
.req-name { font-size: 28rpx; font-weight: 600; color: #4a2040; }
.req-sub { font-size: 24rpx; color: #b8929e; display: block; }
.req-title { font-size: 28rpx; font-weight: 600; color: #4a2040; display: block; }
.req-msg { font-size: 24rpx; color: #7c5270; font-style: italic; display: block; margin: 8rpx 0; }
.req-actions { display: flex; gap: 16rpx; margin-top: 12rpx; }
.btn-accept { flex: 1; background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 12rpx; font-size: 26rpx; }
.btn-reject { flex: 1; background: #f5f5f5; color: #666; border: none; border-radius: 12rpx; font-size: 26rpx; }
.status-green { color: #059669; font-size: 24rpx; font-weight: 600; }
.status-yellow { color: #d97706; font-size: 24rpx; }
.status-gray { color: #b8929e; font-size: 24rpx; }
</style>
