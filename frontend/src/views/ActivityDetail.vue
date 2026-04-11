<template>
  <div class="detail-page" v-if="activity">
    <div class="back-bar">
      <el-button text @click="$router.back()">← 返回</el-button>
      <el-button v-if="userStore.isLoggedIn && !isOwner" text type="danger" size="small"
        @click="reportTarget = { type: 'activity', id: activity.id }; showReport = true">🚩 举报</el-button>
    </div>

    <div class="detail-card">
      <div class="detail-header">
        <el-tag :type="catColor(activity.category)" round>{{ activity.category }}</el-tag>
        <el-tag v-if="activity.status === 0" type="info" round>已关闭</el-tag>
        <el-tag v-if="activity.status === 2" type="warning" round>已满员</el-tag>
      </div>
      <h1 class="detail-title">{{ activity.title }}</h1>

      <div class="author-block" @click="$router.push(`/user/${activity.userId}`)">
        <el-avatar :size="44" :src="activity.authorAvatar" style="background:#f472b6">
          {{ (activity.authorName || '?').charAt(0) }}
        </el-avatar>
        <div>
          <div class="ab-name">{{ activity.authorName }}</div>
          <div class="ab-sub">
            <span v-if="activity.authorCity">📍{{ activity.authorCity }}</span>
            <span v-if="activity.authorAge">{{ activity.authorAge }}岁</span>
            <span v-if="activity.authorGender === 1">♂</span>
            <span v-if="activity.authorGender === 2">♀</span>
          </div>
        </div>
      </div>

      <div class="detail-body">
        <p>{{ activity.description }}</p>
      </div>

      <div class="info-grid">
        <div v-if="activity.location" class="info-item"><span class="info-label">📍 地点</span><span>{{ activity.location }}</span></div>
        <div v-if="activity.activityTime" class="info-item"><span class="info-label">🕐 时间</span><span>{{ activity.activityTime }}</span></div>
        <div class="info-item"><span class="info-label">👥 人数</span><span>{{ activity.joinedCount || 0 }}/{{ activity.maxPeople || 1 }}</span></div>
        <div class="info-item"><span class="info-label">👁 浏览</span><span>{{ activity.viewCount || 0 }}</span></div>
      </div>

      <div v-if="activity.tags" class="detail-tags">
        <el-tag v-for="t in activity.tags.split(',')" :key="t" size="small" round>{{ t }}</el-tag>
      </div>
    </div>

    <!-- 申请加入 -->
    <div v-if="canApply" class="apply-section">
      <h3>想参加？发送申请吧！</h3>
      <el-input v-model="applyMsg" type="textarea" :rows="2" placeholder="写一句话介绍自己，增加通过率..." maxlength="200" show-word-limit />
      <el-button type="primary" :loading="applying" @click="doApply" style="margin-top:12px">申请加入</el-button>
    </div>
    <div v-else-if="!userStore.isLoggedIn" class="apply-section">
      <router-link :to="`/login?redirect=/activity/${activity.id}`" class="login-tip">登录后申请加入</router-link>
    </div>
    <div v-else-if="isOwner" class="apply-section owner-tip">
      这是你发布的活动
    </div>
    <div v-else-if="applied" class="apply-section owner-tip">
      ✅ 已申请，等待对方确认
    </div>

    <!-- 评论区 -->
    <div class="comment-section">
      <h3 class="section-title">💬 评论区 <span class="comment-count">({{ commentTotal }})</span></h3>

      <!-- 发评论 -->
      <div v-if="userStore.isLoggedIn" class="comment-form">
        <el-input v-model="commentText" type="textarea" :rows="2"
          :placeholder="replyTo ? `回复 ${replyTo.authorName}...` : '说点什么...'" maxlength="300" show-word-limit />
        <div class="form-bar">
          <span v-if="replyTo" class="reply-hint">
            回复 {{ replyTo.authorName }} <span class="cancel-reply" @click="replyTo = null">✕取消</span>
          </span>
          <span v-else></span>
          <el-button type="primary" size="small" :loading="submitting" @click="submitComment"
            :disabled="!commentText.trim()" round>发表</el-button>
        </div>
      </div>
      <div v-else class="login-comment-tip">
        <router-link :to="`/login?redirect=/activity/${activity.id}`">登录后参与评论</router-link>
      </div>

      <!-- 评论列表 -->
      <div v-if="comments.length === 0" class="no-comments">暂无评论，来说两句吧~</div>
      <div v-for="c in comments" :key="c.id" :class="['comment-item', { pinned: c.pinned }]">
        <div class="comment-header">
          <el-avatar :size="32" :src="c.authorAvatar" style="background:#f472b6">
            {{ (c.authorName || '?').charAt(0) }}
          </el-avatar>
          <span class="comment-author" @click="$router.push(`/user/${c.userId}`)">{{ c.authorName }}</span>
          <el-tag v-if="c.pinned" size="small" type="warning" round>置顶</el-tag>
          <span v-if="c.replyToName" class="reply-tag">回复 {{ c.replyToName }}</span>
          <span class="comment-time">{{ formatTime(c.createTime) }}</span>
        </div>
        <p class="comment-content">{{ c.content }}</p>
        <div class="comment-actions">
          <el-button text size="small" @click="setReply(c)">回复</el-button>
          <template v-if="isOwner">
            <el-button text size="small" @click="togglePin(c)">{{ c.pinned ? '取消置顶' : '置顶' }}</el-button>
            <el-button text size="small" type="danger" @click="doHide(c.id)">隐藏</el-button>
          </template>
          <el-button v-if="c.userId === userStore.userId" text size="small" type="danger" @click="doDelete(c.id)">删除</el-button>
        </div>
      </div>

      <div v-if="hasMoreComments" class="load-more">
        <el-button text @click="loadMoreComments">加载更多...</el-button>
      </div>
    </div>
  </div>
  <div v-else class="loading-page">加载中...</div>

  <ReportDialog v-model="showReport" :target-type="reportTarget.type" :target-id="reportTarget.id" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActivityDetail } from '@/api/activity'
import { applyActivity, getSentRequests } from '@/api/match'
import { getComments, addComment, pinComment, hideComment, deleteComment } from '@/api/activityComment'
import { useUserStore } from '@/stores/user'
import ReportDialog from '@/components/ReportDialog.vue'

const route = useRoute()
const userStore = useUserStore()
const activity = ref(null)
const applyMsg = ref('')
const applying = ref(false)
const applied = ref(false)
const showReport = ref(false)
const reportTarget = ref({ type: 'activity', id: 0 })

// 评论
const comments = ref([])
const commentText = ref('')
const commentTotal = ref(0)
const commentPage = ref(1)
const hasMoreComments = ref(false)
const submitting = ref(false)
const replyTo = ref(null)

const isOwner = computed(() => activity.value && userStore.userId && activity.value.userId === userStore.userId)
const canApply = computed(() => {
  return userStore.isLoggedIn && !isOwner.value && !applied.value && activity.value?.status === 1
})

function catColor(cat) {
  const map = { '旅游': 'success', '运动': 'warning', '美食': 'danger', '电影': '', '学习': 'info' }
  return map[cat] || 'info'
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' }) + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function doApply() {
  applying.value = true
  try {
    await applyActivity({ activityId: activity.value.id, message: applyMsg.value })
    ElMessage.success('申请已发送！')
    applied.value = true
  } catch (e) { ElMessage.error(e?.message || '申请失败') }
  applying.value = false
}

async function loadComments(append = false) {
  if (!append) commentPage.value = 1
  try {
    const data = await getComments(route.params.id, commentPage.value, 20)
    const records = data?.records || []
    if (append) comments.value.push(...records)
    else comments.value = records
    commentTotal.value = data?.total || 0
    hasMoreComments.value = comments.value.length < commentTotal.value
  } catch { /* ignore */ }
}

function loadMoreComments() { commentPage.value++; loadComments(true) }

function setReply(c) { replyTo.value = c; commentText.value = '' }

async function submitComment() {
  if (!commentText.value.trim()) return
  submitting.value = true
  try {
    await addComment({
      activityId: Number(route.params.id),
      content: commentText.value,
      parentId: replyTo.value?.id || null,
    })
    ElMessage.success('评论成功')
    commentText.value = ''
    replyTo.value = null
    loadComments()
  } catch (e) { ElMessage.error(e?.message || '评论失败') }
  submitting.value = false
}

async function togglePin(c) {
  try {
    await pinComment(c.id, c.pinned ? 0 : 1)
    ElMessage.success(c.pinned ? '已取消置顶' : '已置顶')
    loadComments()
  } catch (e) { ElMessage.error(e?.message || '操作失败') }
}

async function doHide(id) {
  try { await hideComment(id); ElMessage.success('已隐藏'); loadComments() }
  catch (e) { ElMessage.error(e?.message || '操作失败') }
}

async function doDelete(id) {
  try { await deleteComment(id); ElMessage.success('已删除'); loadComments() }
  catch (e) { ElMessage.error(e?.message || '操作失败') }
}

onMounted(async () => {
  activity.value = await getActivityDetail(route.params.id)
  loadComments()
  if (userStore.isLoggedIn) {
    try {
      const sent = await getSentRequests()
      applied.value = (sent || []).some(r => r.activityId === activity.value?.id)
    } catch { /* ignore */ }
  }
})
</script>

<style scoped>
.detail-page { max-width: 700px; margin: 0 auto; padding: 20px; }
.back-bar { margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; }
.detail-card { background: var(--bg-card); border-radius: 12px; padding: 24px; }
.detail-header { display: flex; gap: 8px; margin-bottom: 12px; }
.detail-title { font-size: 22px; font-weight: 800; margin-bottom: 16px; }
.author-block { display: flex; gap: 12px; align-items: center; padding: 12px; background: rgba(255,255,255,0.03); border-radius: 10px; margin-bottom: 16px; cursor: pointer; }
.author-block:hover { background: rgba(244,114,182,0.08); }
.ab-name { font-weight: 600; font-size: 15px; }
.ab-sub { font-size: 12px; color: var(--text-muted); display: flex; gap: 8px; }
.detail-body { font-size: 15px; line-height: 1.8; color: var(--text-secondary); margin-bottom: 16px; white-space: pre-wrap; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px; }
.info-item { display: flex; justify-content: space-between; padding: 8px 12px; background: rgba(255,255,255,0.03); border-radius: 8px; font-size: 14px; }
.info-label { color: var(--text-muted); }
.detail-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.apply-section { background: var(--bg-card); border-radius: 12px; padding: 20px; margin-top: 16px; }
.apply-section h3 { font-size: 16px; margin-bottom: 12px; }
.login-tip { display: block; text-align: center; padding: 12px; background: linear-gradient(135deg,#f472b6,#c084fc); color: white; border-radius: 8px; font-weight: 600; }
.owner-tip { text-align: center; color: var(--text-muted); }
.loading-page { text-align: center; padding: 60px; color: var(--text-muted); }

/* 评论区 */
.comment-section { background: var(--bg-card); border-radius: 16px; padding: 20px; margin-top: 16px; border: 1px solid var(--border-color); }
.section-title { font-size: 16px; font-weight: 700; margin-bottom: 16px; }
.comment-count { font-weight: 400; color: var(--text-muted); font-size: 14px; }

.comment-form { margin-bottom: 16px; }
.form-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.reply-hint { font-size: 13px; color: var(--primary); }
.cancel-reply { cursor: pointer; margin-left: 6px; color: var(--text-muted); }
.cancel-reply:hover { color: var(--accent-red); }
.login-comment-tip { text-align: center; padding: 12px; margin-bottom: 16px; }
.login-comment-tip a { color: var(--primary); font-weight: 600; }

.no-comments { text-align: center; padding: 30px; color: var(--text-muted); font-size: 14px; }

.comment-item { padding: 14px 0; border-top: 1px solid var(--border-color); }
.comment-item.pinned { background: rgba(244,114,182,0.04); margin: 0 -20px; padding: 14px 20px; border-radius: 8px; }
.comment-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; flex-wrap: wrap; }
.comment-author { font-weight: 600; font-size: 14px; cursor: pointer; color: var(--primary-dark); }
.comment-author:hover { text-decoration: underline; }
.reply-tag { font-size: 12px; color: var(--text-muted); }
.comment-time { font-size: 12px; color: var(--text-muted); margin-left: auto; }
.comment-content { font-size: 14px; line-height: 1.6; color: var(--text-secondary); margin-bottom: 4px; }
.comment-actions { display: flex; gap: 4px; }
.load-more { text-align: center; padding: 8px; }
</style>
