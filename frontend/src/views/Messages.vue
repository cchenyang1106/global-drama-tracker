<template>
  <div class="messages-page">
    <h2>消息中心</h2>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="聊天" name="chats">
        <div v-if="chats.length === 0" class="empty">暂无聊天，去活动广场找搭子吧！</div>
        <div v-for="c in chats" :key="c.matchId" class="chat-item" @click="$router.push(`/chat/${c.matchId}`)">
          <el-avatar :size="44" :src="c.partnerAvatar" style="background:#6366f1">
            {{ (c.partnerName || '?').charAt(0) }}
          </el-avatar>
          <div class="chat-info">
            <div class="chat-top">
              <span class="chat-name">{{ c.partnerName }}</span>
              <span class="chat-time">{{ formatTime(c.lastTime) }}</span>
            </div>
            <div class="chat-bottom">
              <span class="chat-preview">{{ c.lastMessage || '暂无消息' }}</span>
              <el-badge v-if="c.unreadCount > 0" :value="c.unreadCount" :max="99" />
            </div>
            <div class="chat-activity">{{ c.activityTitle }}</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="收到的申请" name="received">
        <div v-if="received.length === 0" class="empty">暂无收到的申请</div>
        <div v-for="r in received" :key="r.id" class="request-item">
          <div class="req-header">
            <el-avatar :size="36" :src="r.applicantAvatar" style="background:#ec4899">
              {{ (r.applicantName || '?').charAt(0) }}
            </el-avatar>
            <div>
              <div class="req-name">{{ r.applicantName }}
                <span class="req-sub" v-if="r.applicantCity">· {{ r.applicantCity }}</span>
              </div>
              <div class="req-activity">申请加入：{{ r.activityTitle }}</div>
            </div>
          </div>
          <p v-if="r.message" class="req-msg">"{{ r.message }}"</p>
          <div class="req-actions" v-if="r.status === 0">
            <el-button type="primary" size="small" @click="handle(r.id, 1)">同意</el-button>
            <el-button size="small" @click="handle(r.id, 2)">拒绝</el-button>
          </div>
          <el-tag v-else-if="r.status === 1" type="success" size="small">已同意</el-tag>
          <el-tag v-else type="info" size="small">已拒绝</el-tag>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的申请" name="sent">
        <div v-if="sent.length === 0" class="empty">暂无发出的申请</div>
        <div v-for="s in sent" :key="s.id" class="request-item">
          <div class="req-header">
            <div>
              <div class="req-activity">{{ s.activityTitle }}</div>
              <div class="req-sub">发起者：{{ s.authorName }}</div>
            </div>
          </div>
          <p v-if="s.message" class="req-msg">"{{ s.message }}"</p>
          <el-tag v-if="s.status === 0" type="warning" size="small">等待确认</el-tag>
          <el-tag v-else-if="s.status === 1" type="success" size="small">已匹配 ✅</el-tag>
          <el-tag v-else type="info" size="small">已拒绝</el-tag>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getChatList, getReceivedRequests, getSentRequests, handleRequest } from '@/api/match'

const activeTab = ref('chats')
const chats = ref([])
const received = ref([])
const sent = ref([])

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

async function loadChats() { try { chats.value = await getChatList() || [] } catch {} }
async function loadReceived() { try { received.value = await getReceivedRequests() || [] } catch {} }
async function loadSent() { try { sent.value = await getSentRequests() || [] } catch {} }

async function handle(id, action) {
  try {
    await handleRequest(id, action)
    ElMessage.success(action === 1 ? '已同意！可以开始聊天了' : '已拒绝')
    loadReceived()
    loadChats()
  } catch (e) { ElMessage.error(e?.message || '操作失败') }
}

function onTabChange(tab) {
  if (tab === 'chats') loadChats()
  else if (tab === 'received') loadReceived()
  else loadSent()
}

onMounted(() => { loadChats(); loadReceived(); loadSent() })
</script>

<style scoped>
.messages-page { max-width: 700px; margin: 0 auto; padding: 20px; }
.messages-page h2 { font-size: 22px; font-weight: 800; margin-bottom: 16px; }
.empty { text-align: center; padding: 40px; color: var(--text-muted); }

.chat-item { display: flex; gap: 12px; padding: 14px; border-radius: 10px; cursor: pointer; transition: background 0.2s; }
.chat-item:hover { background: rgba(99,102,241,0.08); }
.chat-info { flex: 1; min-width: 0; }
.chat-top { display: flex; justify-content: space-between; }
.chat-name { font-weight: 600; font-size: 15px; }
.chat-time { font-size: 12px; color: var(--text-muted); }
.chat-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 4px; }
.chat-preview { font-size: 13px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.chat-activity { font-size: 11px; color: var(--text-muted); margin-top: 2px; opacity: 0.7; }

.request-item { background: var(--bg-card); border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.req-header { display: flex; gap: 10px; align-items: center; }
.req-name { font-weight: 600; font-size: 14px; }
.req-sub { font-size: 12px; color: var(--text-muted); }
.req-activity { font-size: 13px; color: var(--text-secondary); }
.req-msg { font-size: 13px; color: var(--text-secondary); margin: 8px 0; font-style: italic; }
.req-actions { display: flex; gap: 8px; margin-top: 8px; }
</style>
