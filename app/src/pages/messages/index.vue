<template>
  <view class="page">
    <view class="tabs">
      <text :class="['tab', { active: tab === 'chats' }]" @tap="tab = 'chats'">聊天</text>
      <text :class="['tab', { active: tab === 'received' }]" @tap="tab = 'received'">收到的申请</text>
      <text :class="['tab', { active: tab === 'sent' }]" @tap="tab = 'sent'">我的申请</text>
    </view>

    <!-- 聊天列表 -->
    <view v-if="tab === 'chats'">
      <view v-if="chats.length === 0" class="empty">暂无聊天</view>
      <view v-for="c in chats" :key="c.matchId" class="chat-item" @tap="goChat(c.matchId)">
        <view class="avatar">{{ (c.partnerName || '?').charAt(0) }}</view>
        <view class="chat-info">
          <view class="chat-top">
            <text class="chat-name">{{ c.partnerName }}</text>
            <text class="chat-time">{{ formatTime(c.lastTime) }}</text>
          </view>
          <text class="chat-preview">{{ c.lastMessage || '暂无消息' }}</text>
          <text class="chat-activity">{{ c.activityTitle }}</text>
        </view>
        <view v-if="c.unreadCount > 0" class="badge">{{ c.unreadCount > 99 ? '99+' : c.unreadCount }}</view>
      </view>
    </view>

    <!-- 收到的申请 -->
    <view v-if="tab === 'received'">
      <view v-if="received.length === 0" class="empty">暂无收到的申请</view>
      <view v-for="r in received" :key="r.id" class="req-item">
        <view class="req-top">
          <view class="avatar small">{{ (r.applicantName || '?').charAt(0) }}</view>
          <view>
            <text class="req-name">{{ r.applicantName }}</text>
            <text class="req-sub">{{ r.activityTitle }}</text>
          </view>
        </view>
        <text v-if="r.message" class="req-msg">"{{ r.message }}"</text>
        <view v-if="r.status === 0" class="req-actions">
          <button class="btn-accept" @tap="handle(r.id, 1)">同意</button>
          <button class="btn-reject" @tap="handle(r.id, 2)">拒绝</button>
        </view>
        <text v-else-if="r.status === 1" class="status-tag green">已同意</text>
        <text v-else class="status-tag gray">已拒绝</text>
      </view>
    </view>

    <!-- 我的申请 -->
    <view v-if="tab === 'sent'">
      <view v-if="sent.length === 0" class="empty">暂无发出的申请</view>
      <view v-for="s in sent" :key="s.id" class="req-item">
        <text class="req-name">{{ s.activityTitle }}</text>
        <text v-if="s.message" class="req-msg">"{{ s.message }}"</text>
        <text v-if="s.status === 0" class="status-tag yellow">等待确认</text>
        <text v-else-if="s.status === 1" class="status-tag green">已匹配 ✅</text>
        <text v-else class="status-tag gray">已拒绝</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getChatList, getReceivedRequests, getSentRequests, handleRequest } from '@/api/match'

const tab = ref('chats')
const chats = ref([])
const received = ref([])
const sent = ref([])

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

function goChat(matchId) { uni.navigateTo({ url: `/pages/chat/index?matchId=${matchId}` }) }

async function handle(id, action) {
  try {
    await handleRequest(id, action)
    uni.showToast({ title: action === 1 ? '已同意' : '已拒绝', icon: 'success' })
    received.value = await getReceivedRequests() || []
    chats.value = await getChatList() || []
  } catch {}
}

onMounted(async () => {
  try { chats.value = await getChatList() || [] } catch {}
  try { received.value = await getReceivedRequests() || [] } catch {}
  try { sent.value = await getSentRequests() || [] } catch {}
})
</script>

<style>
.page { padding: 16rpx; background: #fff5f7; min-height: 100vh; }
.tabs { display: flex; gap: 8rpx; margin-bottom: 20rpx; }
.tab { flex: 1; text-align: center; padding: 16rpx; border-radius: 12rpx; background: #ffffff; color: #7c5270; font-size: 28rpx; }
.tab.active { background: rgba(99,102,241,0.2); color: #f472b6; }
.empty { text-align: center; padding: 80rpx; color: #b8929e; font-size: 28rpx; }

.chat-item { display: flex; align-items: center; gap: 16rpx; padding: 20rpx; border-radius: 16rpx; margin-bottom: 8rpx; }
.avatar { width: 72rpx; height: 72rpx; border-radius: 50%; background: #f472b6; color: white; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 700; flex-shrink: 0; }
.avatar.small { width: 56rpx; height: 56rpx; font-size: 24rpx; }
.chat-info { flex: 1; min-width: 0; }
.chat-top { display: flex; justify-content: space-between; }
.chat-name { font-size: 30rpx; font-weight: 600; color: #4a2040; }
.chat-time { font-size: 22rpx; color: #b8929e; }
.chat-preview { display: block; font-size: 26rpx; color: #b8929e; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4rpx; }
.chat-activity { display: block; font-size: 22rpx; color: #475569; margin-top: 2rpx; }
.badge { background: #ef4444; color: white; border-radius: 50%; min-width: 36rpx; height: 36rpx; display: flex; align-items: center; justify-content: center; font-size: 22rpx; padding: 0 8rpx; }

.req-item { background: #ffffff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.req-top { display: flex; gap: 12rpx; align-items: center; margin-bottom: 8rpx; }
.req-name { font-size: 28rpx; font-weight: 600; color: #4a2040; display: block; }
.req-sub { font-size: 24rpx; color: #b8929e; display: block; }
.req-msg { display: block; font-size: 26rpx; color: #7c5270; font-style: italic; margin: 8rpx 0; }
.req-actions { display: flex; gap: 12rpx; margin-top: 12rpx; }
.btn-accept { flex: 1; background: #f472b6; color: white; border-radius: 10rpx; font-size: 28rpx; }
.btn-reject { flex: 1; background: #334155; color: #7c5270; border-radius: 10rpx; font-size: 28rpx; }
.status-tag { display: inline-block; padding: 6rpx 16rpx; border-radius: 8rpx; font-size: 24rpx; margin-top: 8rpx; }
.green { background: rgba(34,197,94,0.15); color: #22c55e; }
.yellow { background: rgba(234,179,8,0.15); color: #eab308; }
.gray { background: rgba(148,163,184,0.1); color: #b8929e; }
</style>
