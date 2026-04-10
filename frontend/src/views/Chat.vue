<template>
  <div class="chat-page">
    <div class="chat-header">
      <el-button text @click="$router.push('/messages')">← 返回</el-button>
      <div class="chat-header-info">
        <span class="chat-partner-name">{{ partnerName }}</span>
        <span class="chat-activity-title">{{ activityTitle }}</span>
      </div>
      <span v-if="partnerWechat" class="wechat-tag">微信: {{ partnerWechat }}</span>
    </div>

    <div class="chat-body" ref="chatBody">
      <div v-for="msg in messages" :key="msg.id" :class="['msg-row', { mine: msg.isMine }]">
        <div class="msg-bubble">
          <div class="msg-content">{{ msg.content }}</div>
          <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
      <div v-if="messages.length === 0" class="empty-chat">开始聊天吧！</div>
    </div>

    <div class="chat-input">
      <el-input v-model="inputMsg" placeholder="输入消息..." @keyup.enter="send" :disabled="sending" />
      <el-button type="primary" :loading="sending" @click="send" :disabled="!inputMsg.trim()">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getChatMessages, sendMessage } from '@/api/match'

const route = useRoute()
const matchId = route.params.matchId
const messages = ref([])
const inputMsg = ref('')
const sending = ref(false)
const partnerName = ref('')
const partnerWechat = ref('')
const activityTitle = ref('')
const chatBody = ref(null)
let pollTimer = null

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  })
}

async function loadMessages() {
  try {
    const data = await getChatMessages(matchId)
    messages.value = data?.messages || []
    partnerName.value = data?.partnerName || ''
    partnerWechat.value = data?.partnerWechat || ''
    activityTitle.value = data?.activityTitle || ''
    scrollToBottom()
  } catch { /* ignore */ }
}

async function send() {
  if (!inputMsg.value.trim()) return
  sending.value = true
  try {
    await sendMessage({ matchId: Number(matchId), content: inputMsg.value })
    inputMsg.value = ''
    await loadMessages()
  } catch { /* ignore */ }
  sending.value = false
}

onMounted(() => {
  loadMessages()
  pollTimer = setInterval(loadMessages, 5000)
})

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })
</script>

<style scoped>
.chat-page { display: flex; flex-direction: column; height: calc(100vh - 64px); max-width: 700px; margin: 0 auto; }
.chat-header { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: var(--bg-card); border-bottom: 1px solid rgba(255,255,255,0.06); }
.chat-header-info { flex: 1; text-align: center; }
.chat-partner-name { font-weight: 700; font-size: 16px; }
.chat-activity-title { display: block; font-size: 12px; color: var(--text-muted); }
.wechat-tag { font-size: 12px; background: rgba(7,193,96,0.15); color: #07c160; padding: 4px 10px; border-radius: 12px; white-space: nowrap; }

.chat-body { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 10px; }
.msg-row { display: flex; }
.msg-row.mine { justify-content: flex-end; }
.msg-bubble { max-width: 70%; padding: 10px 14px; border-radius: 12px; background: var(--bg-card); }
.msg-row.mine .msg-bubble { background: linear-gradient(135deg, #6366f1, #8b5cf6); color: white; }
.msg-content { font-size: 14px; line-height: 1.6; word-break: break-word; }
.msg-time { font-size: 11px; opacity: 0.6; text-align: right; margin-top: 4px; }
.empty-chat { text-align: center; color: var(--text-muted); padding: 40px; }

.chat-input { display: flex; gap: 10px; padding: 12px 16px; background: var(--bg-card); border-top: 1px solid rgba(255,255,255,0.06); }
.chat-input .el-input { flex: 1; }
</style>
