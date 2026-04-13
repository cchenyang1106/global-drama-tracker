<template>
  <view class="page" v-if="activity">
    <!-- 活动信息 -->
    <view class="card">
      <view class="row-between">
        <text class="cat-tag">{{ activity.category }}</text>
        <text v-if="!isOwner && isLoggedIn" class="report-btn" @click="showReport = true">🚩 举报</text>
      </view>
      <text class="title">{{ activity.title }}</text>

      <view class="author" @click="goUser(activity.userId)">
        <view class="avatar-circle">{{ (activity.authorName || '?').charAt(0) }}</view>
        <view>
          <text class="author-name">{{ activity.authorName }}</text>
          <text class="author-sub">{{ activity.authorCity || '' }} {{ activity.authorGender === 1 ? '♂' : activity.authorGender === 2 ? '♀' : '' }}</text>
        </view>
      </view>

      <text class="desc">{{ activity.description }}</text>

      <view class="info-grid">
        <view class="info-item" v-if="activity.location"><text class="info-label">📍 地点</text><text>{{ activity.location }}</text></view>
        <view class="info-item" v-if="activity.activityTime"><text class="info-label">🕐 时间</text><text>{{ activity.activityTime }}</text></view>
        <view class="info-item"><text class="info-label">👥 人数</text><text>{{ activity.joinedCount || 0 }}/{{ activity.maxPeople || 1 }}</text></view>
        <view class="info-item"><text class="info-label">👁 浏览</text><text>{{ activity.viewCount || 0 }}</text></view>
      </view>
    </view>

    <!-- 申请区 -->
    <view class="card" v-if="canApply">
      <text class="section-title">想参加？发送申请吧！</text>
      <textarea v-model="applyMsg" placeholder="写一句话介绍自己..." maxlength="200"
        style="width:100%;height:120rpx;background:#fff5f7;border:1px solid #fce4ec;border-radius:12rpx;padding:16rpx;font-size:28rpx;color:#4a2040;box-sizing:border-box;" />
      <button class="btn-primary" @click="doApply" :disabled="applying">{{ applying ? '提交中...' : '申请加入' }}</button>
    </view>
    <view class="card status-card" v-else-if="!isLoggedIn">
      <text @click="uni.navigateTo({url:'/pages/login/index'})" style="color:#f472b6;font-weight:600;">登录后申请加入 →</text>
    </view>
    <view class="card status-card" v-else-if="isOwner">
      <text style="color:#b8929e;">这是你发布的活动</text>
    </view>
    <view class="card status-card" v-else-if="applyStatus === 0">
      <text style="color:#d97706;">⏳ 已申请，等待对方确认</text>
    </view>
    <view class="card status-card" v-else-if="applyStatus === 1">
      <text style="color:#059669;font-weight:700;font-size:32rpx;">🎉 组队成功！</text>
      <button class="btn-primary" style="margin-top:16rpx;" @click="uni.navigateTo({url:`/pages/chat/index?matchId=${matchId}`})">去聊天 →</button>
    </view>
    <view class="card status-card" v-else-if="applyStatus === 2">
      <text style="color:#e11d48;">❌ 申请已被拒绝</text>
    </view>

    <!-- 评论区 -->
    <view class="card">
      <text class="section-title">💬 评论区 ({{ commentTotal }})</text>

      <view v-if="isLoggedIn" style="margin-bottom:20rpx;">
        <textarea v-model="commentText" :placeholder="replyTo ? `回复 ${replyTo.authorName}...` : '说点什么...'" maxlength="300"
          style="width:100%;height:120rpx;background:#fff5f7;border:1px solid #fce4ec;border-radius:12rpx;padding:16rpx;font-size:28rpx;color:#4a2040;box-sizing:border-box;" />
        <view style="display:flex;justify-content:space-between;align-items:center;margin-top:8rpx;">
          <text v-if="replyTo" style="font-size:24rpx;color:#f472b6;" @click="replyTo = null">回复 {{ replyTo.authorName }} ✕取消</text>
          <text v-else></text>
          <button class="btn-small" @click="submitComment" :disabled="!commentText.trim()">发表</button>
        </view>
      </view>

      <view v-if="comments.length === 0" style="text-align:center;padding:40rpx;color:#b8929e;font-size:26rpx;">暂无评论，来说两句吧~</view>

      <view v-for="c in comments" :key="c.id" :style="c.pinned ? 'background:#fff0f5;margin:0 -20rpx;padding:16rpx 20rpx;border-radius:12rpx;' : ''"
        style="padding:16rpx 0;border-top:1px solid #fce4ec;">
        <view style="display:flex;align-items:center;gap:12rpx;margin-bottom:8rpx;">
          <view class="avatar-sm">{{ (c.authorName || '?').charAt(0) }}</view>
          <text style="font-weight:600;font-size:26rpx;color:#7c3a5e;" @click="goUser(c.userId)">{{ c.authorName }}</text>
          <text v-if="c.pinned" style="font-size:22rpx;color:#d97706;background:#fef3c7;padding:2rpx 10rpx;border-radius:20rpx;">置顶</text>
          <text v-if="c.replyToName" style="font-size:22rpx;color:#b8929e;">回复 {{ c.replyToName }}</text>
          <text style="font-size:22rpx;color:#b8929e;margin-left:auto;">{{ formatTime(c.createTime) }}</text>
        </view>
        <text style="font-size:28rpx;color:#4a2040;line-height:1.6;">{{ c.content }}</text>
        <view style="display:flex;gap:16rpx;margin-top:8rpx;">
          <text class="action-text" @click="setReply(c)">回复</text>
          <text v-if="isOwner" class="action-text" @click="togglePin(c)">{{ c.pinned ? '取消置顶' : '置顶' }}</text>
          <text v-if="isOwner" class="action-text" style="color:#e11d48;" @click="doHideComment(c.id)">隐藏</text>
          <text v-if="c.userId == currentUserId" class="action-text" style="color:#e11d48;" @click="doDeleteComment(c.id)">删除</text>
        </view>
      </view>

      <view v-if="hasMoreComments" style="text-align:center;padding:16rpx;">
        <text style="color:#f472b6;font-size:26rpx;" @click="loadMoreComments">加载更多...</text>
      </view>
    </view>

    <!-- 举报弹窗 -->
    <uni-popup ref="reportPopup" type="center" v-if="showReport">
      <view class="popup-content">
        <text class="popup-title">举报</text>
        <view v-for="r in reasons" :key="r" class="reason-item" @click="reportReason = r"
          :style="reportReason === r ? 'background:#fce4ec;border-color:#f472b6;' : ''">
          <text>{{ r }}</text>
        </view>
        <textarea v-model="reportDetail" placeholder="详细说明（选填）" maxlength="200"
          style="width:100%;height:120rpx;background:#fff5f7;border:1px solid #fce4ec;border-radius:12rpx;padding:16rpx;font-size:26rpx;color:#4a2040;margin-top:16rpx;box-sizing:border-box;" />
        <view style="display:flex;gap:16rpx;margin-top:20rpx;">
          <button class="btn-cancel" @click="showReport = false">取消</button>
          <button class="btn-danger" @click="submitReport" :disabled="!reportReason">提交举报</button>
        </view>
      </view>
    </uni-popup>
  </view>
  <view v-else style="text-align:center;padding:100rpx;color:#b8929e;">加载中...</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getActivityDetail } from '@/api/activity'
import { applyActivity, getSentRequests } from '@/api/match'
import { request } from '@/api/request'

const activity = ref(null)
const applyMsg = ref('')
const applying = ref(false)
const applyStatus = ref(-1)
const matchId = ref(null)
let activityId = null

// 评论
const comments = ref([])
const commentText = ref('')
const commentTotal = ref(0)
const commentPage = ref(1)
const hasMoreComments = ref(false)
const replyTo = ref(null)

// 举报
const showReport = ref(false)
const reportReason = ref('')
const reportDetail = ref('')
const reasons = ['色情低俗', '辱骂骚扰', '虚假信息', '广告推销', '诈骗欺诈', '其他']

const currentUserId = uni.getStorageSync('userId')
const isLoggedIn = computed(() => !!uni.getStorageSync('token'))
const isOwner = computed(() => activity.value && currentUserId && activity.value.userId == currentUserId)
const canApply = computed(() => isLoggedIn.value && !isOwner.value && applyStatus.value === -1 && activity.value?.status === 1)

function goUser(id) { uni.navigateTo({ url: `/pages/profile/index?userId=${id}` }) }

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t.replace('T', ' ').replace(/-/g, '/'))
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  if (d.toDateString() === now.toDateString()) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return `${d.getMonth()+1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2,'0')}`
}

async function doApply() {
  applying.value = true
  try {
    await applyActivity({ activityId: Number(activityId), message: applyMsg.value })
    uni.showToast({ title: '申请已发送', icon: 'success' })
    applyStatus.value = 0
  } catch (e) { uni.showToast({ title: e?.message || '申请失败', icon: 'none' }) }
  applying.value = false
}

// 评论
async function loadComments(append = false) {
  if (!append) commentPage.value = 1
  try {
    const data = await request({ url: `/activity-comment/list/${activityId}?page=${commentPage.value}&size=20` })
    const records = data?.records || []
    if (append) comments.value.push(...records)
    else comments.value = records
    commentTotal.value = data?.total || 0
    hasMoreComments.value = comments.value.length < commentTotal.value
  } catch {}
}
function loadMoreComments() { commentPage.value++; loadComments(true) }
function setReply(c) { replyTo.value = c; commentText.value = '' }

async function submitComment() {
  if (!commentText.value.trim()) return
  try {
    await request({ url: '/activity-comment/add', method: 'POST', needAuth: true,
      data: { activityId: Number(activityId), content: commentText.value, parentId: replyTo.value?.id || null } })
    uni.showToast({ title: '评论成功', icon: 'success' })
    commentText.value = ''; replyTo.value = null; loadComments()
  } catch (e) { uni.showToast({ title: e?.message || '评论失败', icon: 'none' }) }
}

async function togglePin(c) {
  try {
    await request({ url: `/activity-comment/pin/${c.id}?pinned=${c.pinned ? 0 : 1}`, method: 'POST', needAuth: true })
    loadComments()
  } catch {}
}
async function doHideComment(id) {
  try { await request({ url: `/activity-comment/hide/${id}`, method: 'POST', needAuth: true }); loadComments() } catch {}
}
async function doDeleteComment(id) {
  try { await request({ url: `/activity-comment/${id}`, method: 'DELETE', needAuth: true }); loadComments() } catch {}
}

// 举报
async function submitReport() {
  try {
    await request({ url: '/report/submit', method: 'POST', needAuth: true,
      data: { targetType: 'activity', targetId: Number(activityId), reason: reportReason.value, detail: reportDetail.value } })
    uni.showToast({ title: '举报已提交', icon: 'success' })
    showReport.value = false; reportReason.value = ''; reportDetail.value = ''
  } catch (e) { uni.showToast({ title: e?.message || '提交失败', icon: 'none' }) }
}

onLoad(async (options) => {
  activityId = options?.id
  if (activityId) activity.value = await getActivityDetail(activityId)
  loadComments()
  if (isLoggedIn.value) {
    try {
      const sent = await getSentRequests()
      const myReq = (sent || []).find(r => r.activityId == activityId)
      if (myReq) { applyStatus.value = myReq.status; matchId.value = myReq.id }
    } catch {}
  }
})
</script>

<style scoped>
.page { padding: 20rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 28rpx; margin-bottom: 20rpx; border: 1px solid #fce4ec; }
.row-between { display: flex; justify-content: space-between; align-items: center; }
.cat-tag { background: #fce4ec; color: #ec4899; font-size: 24rpx; padding: 4rpx 16rpx; border-radius: 20rpx; }
.report-btn { color: #e11d48; font-size: 24rpx; }
.title { font-size: 36rpx; font-weight: 800; color: #4a2040; display: block; margin: 16rpx 0; }
.author { display: flex; gap: 16rpx; align-items: center; margin: 16rpx 0; }
.avatar-circle { width: 72rpx; height: 72rpx; border-radius: 50%; background: #f472b6; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 700; }
.avatar-sm { width: 48rpx; height: 48rpx; border-radius: 50%; background: #f9a8d4; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 22rpx; font-weight: 700; flex-shrink: 0; }
.author-name { font-weight: 600; font-size: 28rpx; color: #4a2040; display: block; }
.author-sub { font-size: 24rpx; color: #b8929e; display: block; }
.desc { font-size: 28rpx; color: #4a2040; line-height: 1.7; display: block; margin: 12rpx 0; }
.info-grid { display: flex; flex-wrap: wrap; gap: 12rpx; }
.info-item { flex: 1 1 45%; display: flex; gap: 8rpx; font-size: 26rpx; color: #7c5270; min-width: 280rpx; }
.info-label { color: #b8929e; }
.section-title { font-size: 30rpx; font-weight: 700; color: #4a2040; display: block; margin-bottom: 16rpx; }
.status-card { text-align: center; padding: 32rpx; }
.action-text { font-size: 24rpx; color: #f472b6; }
.btn-primary { margin-top: 16rpx; background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 28rpx; font-weight: 600; padding: 16rpx 0; }
.btn-small { background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 20rpx; font-size: 24rpx; padding: 8rpx 24rpx; line-height: 1.4; }
.btn-cancel { flex: 1; background: #f5f5f5; color: #666; border: none; border-radius: 40rpx; font-size: 28rpx; }
.btn-danger { flex: 1; background: #e11d48; color: #fff; border: none; border-radius: 40rpx; font-size: 28rpx; }

/* 举报弹窗 */
.popup-content { background: #fff; border-radius: 24rpx; padding: 32rpx; width: 600rpx; }
.popup-title { font-size: 32rpx; font-weight: 700; color: #4a2040; display: block; margin-bottom: 20rpx; text-align: center; }
.reason-item { padding: 16rpx 20rpx; border: 1px solid #fce4ec; border-radius: 12rpx; margin-bottom: 12rpx; font-size: 28rpx; color: #4a2040; }
</style>
