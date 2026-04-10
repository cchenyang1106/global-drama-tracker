<template>
  <view class="page">
    <view v-if="partnerWechat" class="wechat-bar">微信号: {{ partnerWechat }}</view>
    <scroll-view class="chat-body" scroll-y :scroll-top="scrollTop" :scroll-with-animation="true">
      <view v-for="msg in messages" :key="msg.id" :class="['msg-row', { mine: msg.isMine }]">
        <view class="msg-bubble">
          <text class="msg-text">{{ msg.content }}</text>
          <text class="msg-time">{{ formatTime(msg.createTime) }}</text>
        </view>
      </view>
      <view v-if="messages.length === 0" class="empty">开始聊天吧！</view>
    </scroll-view>
    <view class="input-bar">
      <input class="msg-input" v-model="inputMsg" placeholder="输入消息..." @confirm="send" />
      <button class="send-btn" @tap="send" :disabled="!inputMsg.trim()">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getChatMessages, sendMessage } from '@/api/match'

const messages = ref([])
const inputMsg = ref('')
const scrollTop = ref(0)
const partnerWechat = ref('')
let matchId = ''
let timer = null

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function load() {
  try {
    const data = await getChatMessages(matchId)
    messages.value = data?.messages || []
    partnerWechat.value = data?.partnerWechat || ''
    scrollTop.value = 99999
  } catch {}
}

async function send() {
  if (!inputMsg.value.trim()) return
  try {
    await sendMessage({ matchId: Number(matchId), content: inputMsg.value })
    inputMsg.value = ''
    await load()
  } catch {}
}

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  matchId = page?.options?.matchId
  load()
  timer = setInterval(load, 5000)
})

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style>
.page { display: flex; flex-direction: column; height: 100vh; background: #0f172a; }
.wechat-bar { text-align: center; padding: 12rpx; background: rgba(7,193,96,0.1); color: #07c160; font-size: 24rpx; }
.chat-body { flex: 1; padding: 16rpx; }
.msg-row { display: flex; margin-bottom: 16rpx; }
.msg-row.mine { justify-content: flex-end; }
.msg-bubble { max-width: 70%; padding: 16rpx 24rpx; border-radius: 20rpx; background: #1e293b; }
.msg-row.mine .msg-bubble { background: linear-gradient(135deg,#6366f1,#8b5cf6); }
.msg-text { font-size: 28rpx; color: #e2e8f0; line-height: 1.6; word-break: break-all; }
.msg-time { display: block; font-size: 20rpx; color: rgba(255,255,255,0.5); text-align: right; margin-top: 4rpx; }
.empty { text-align: center; padding: 80rpx; color: #64748b; font-size: 28rpx; }
.input-bar { display: flex; gap: 12rpx; padding: 16rpx 24rpx; background: #1e293b; border-top: 1rpx solid rgba(255,255,255,0.06); }
.msg-input { flex: 1; background: #0f172a; border-radius: 32rpx; padding: 12rpx 24rpx; color: #e2e8f0; font-size: 28rpx; }
.send-btn { background: #6366f1; color: white; border-radius: 32rpx; padding: 0 32rpx; font-size: 28rpx; line-height: 72rpx; }
</style>
