<template>
  <view class="page">
    <view v-if="partnerWechat" class="wechat-tip">对方微信：{{ partnerWechat }}</view>

    <scroll-view scroll-y :scroll-into-view="scrollToId" style="flex:1;height:calc(100vh - 200rpx);">
      <view style="padding: 20rpx;">
        <view v-for="(m, i) in messages" :key="m.id || i" :id="'msg-' + i"
          :style="m.isMine ? 'display:flex;justify-content:flex-end;margin-bottom:16rpx;' : 'display:flex;justify-content:flex-start;margin-bottom:16rpx;'">
          <view :style="m.isMine ? 'max-width:70%;' : 'max-width:70%;'">
            <text style="font-size:20rpx;color:#b8929e;display:block;margin-bottom:4rpx;"
              :style="m.isMine ? 'text-align:right;' : ''">{{ formatTime(m.createTime) }}</text>
            <view :style="m.isMine
              ? 'background:linear-gradient(135deg,#f472b6,#c084fc);color:#fff;padding:16rpx 20rpx;border-radius:20rpx 4rpx 20rpx 20rpx;'
              : 'background:#fff;color:#4a2040;padding:16rpx 20rpx;border-radius:4rpx 20rpx 20rpx 20rpx;border:1px solid #fce4ec;'">
              <text style="font-size:28rpx;line-height:1.6;">{{ m.content }}</text>
            </view>
          </view>
        </view>
        <view v-if="messages.length === 0" style="text-align:center;padding:60rpx;color:#b8929e;font-size:26rpx;">开始聊天吧~</view>
      </view>
    </scroll-view>

    <view class="input-bar">
      <input v-model="inputText" placeholder="输入消息..." confirm-type="send" @confirm="send"
        style="flex:1;background:#fff5f7;border:1px solid #fce4ec;border-radius:40rpx;padding:16rpx 24rpx;font-size:28rpx;color:#4a2040;" />
      <button @tap="send" :disabled="!inputText.trim()"
        style="background:linear-gradient(135deg,#f472b6,#c084fc);color:#fff;border:none;border-radius:40rpx;font-size:26rpx;padding:14rpx 32rpx;margin-left:12rpx;">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onUnmounted, nextTick } from 'vue'
import { onLoad, onHide } from '@dcloudio/uni-app'
import { sendMessage, getChatMessages } from '@/api/match'

const messages = ref([])
const inputText = ref('')
const partnerWechat = ref('')
const scrollToId = ref('')
let matchId = null
let timer = null

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t.replace('T', ' ').replace(/-/g, '/'))
  if (isNaN(d.getTime())) return ''
  return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function load() {
  if (!matchId) return
  try {
    const data = await getChatMessages(matchId, 1, 100)
    messages.value = data?.messages || []
    if (data?.partnerWechat) partnerWechat.value = data.partnerWechat
    if (data?.partnerName) uni.setNavigationBarTitle({ title: data.partnerName })
    scrollToBottom()
  } catch {}
}

function scrollToBottom() {
  nextTick(() => {
    if (messages.value.length > 0) {
      scrollToId.value = ''
      nextTick(() => { scrollToId.value = 'msg-' + (messages.value.length - 1) })
    }
  })
}

async function send() {
  if (!inputText.value.trim() || !matchId) return
  try {
    await sendMessage({ matchId: Number(matchId), content: inputText.value })
    inputText.value = ''
    load()
  } catch (e) { uni.showToast({ title: e?.message || '发送失败', icon: 'none' }) }
}

onLoad((options) => {
  matchId = options?.matchId
  if (!matchId) {
    uni.showToast({ title: '参数错误', icon: 'none' })
    return
  }
  load()
  timer = setInterval(load, 3000) // 3秒轮询
})

onHide(() => { if (timer) { clearInterval(timer); timer = null } })
onUnmounted(() => { if (timer) { clearInterval(timer); timer = null } })
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #fff5f7; }
.wechat-tip { text-align: center; padding: 12rpx; background: #fef3c7; color: #92400e; font-size: 24rpx; }
.input-bar { display: flex; align-items: center; padding: 16rpx 20rpx; background: #fff; border-top: 1px solid #fce4ec; padding-bottom: calc(16rpx + env(safe-area-inset-bottom)); }
</style>
