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
        <image v-if="activity.authorAvatar" :src="activity.authorAvatar" class="avatar-img" mode="aspectFill" />
        <view v-else class="avatar-circle">{{ (activity.authorName || '?').charAt(0) }}</view>
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
    <!-- 已满员截止提示 -->
    <view class="card status-card" v-if="activity.teamComplete === 1">
      <text style="color:#059669;font-weight:700;">✅ 该活动已满员截止</text>
    </view>

    <!-- 申请区：可以申请 -->
    <view class="card" v-else-if="canApply">
      <text class="section-title">感兴趣？先答题申请</text>
      <!-- 流程提示 -->
      <view class="flow-hint">
        <view class="flow-step"><text class="flow-icon">📝</text><text class="flow-text">答题</text></view>
        <text class="flow-arrow">→</text>
        <view class="flow-step"><text class="flow-icon">⏳</text><text class="flow-text">等待批改</text></view>
        <text class="flow-arrow">→</text>
        <view class="flow-step"><text class="flow-icon">✅</text><text class="flow-text">通过</text></view>
        <text class="flow-arrow">→</text>
        <view class="flow-step"><text class="flow-icon">🔓</text><text class="flow-text">查看详情</text></view>
      </view>
      <text style="font-size:24rpx;color:#b8929e;display:block;margin-bottom:16rpx;">
        发布人设置了 {{ activity.quizCount || 0 }} 道题目，回答后等待批改，通过即可查看活动联系方式
      </text>
      <button v-if="activity.quizCount > 0" class="btn-primary" @tap="goTo(`/pages/quiz-answer/index?activityId=${activity.id}`)">开始答题</button>
      <text v-else style="color:#b8929e;font-size:24rpx;display:block;text-align:center;">发布人还没出题，请稍后再来</text>
    </view>
    <view class="card status-card" v-else-if="!isLoggedIn">
      <text @tap="goTo('/pages/login/index')" style="color:#f472b6;font-weight:600;">登录后参与 →</text>
    </view>
    <view class="card" v-else-if="isOwner">
      <text style="color:#7c5270;font-size:28rpx;display:block;margin-bottom:16rpx;">📋 你是发布人</text>
      <view style="display:flex;flex-direction:column;gap:12rpx;">
        <button class="btn-outline" @tap="goTo(`/pages/publish/index?activityId=${activity.id}`)">📝 编辑活动信息</button>
        <button class="btn-outline" @tap="goTo(`/pages/quiz-edit/index?activityId=${activity.id}`)">✏️ 出题 / 编辑题目</button>
        <button class="btn-outline" @tap="goTo(`/pages/quiz-papers/index?activityId=${activity.id}`)">📊 查看答卷 / 批改</button>
        <button class="btn-outline" @tap="editAnnouncement">📢 发布/更新公告</button>
        <button v-if="activity.teamComplete !== 1 && activity.status === 1" class="btn-primary" @tap="doTeamComplete">🔒 标记为满员（停止接受申请）</button>
        <button class="btn-danger-outline" @tap="doDeleteActivity">🗑 删除此活动</button>
      </view>
      <!-- 当前公告 -->
      <view v-if="activity.announcement" style="margin-top:16rpx;padding:16rpx;background:#fef9f0;border-radius:12rpx;border:1px dashed #fbbf24;">
        <text style="font-size:24rpx;color:#92400e;font-weight:600;display:block;margin-bottom:4rpx;">📢 当前公告</text>
        <text style="font-size:26rpx;color:#78350f;line-height:1.6;">{{ activity.announcement }}</text>
      </view>
    </view>
    <!-- 等待批改 -->
    <view class="card status-card" v-else-if="applyStatus === 0">
      <text style="color:#d97706;font-weight:700;font-size:30rpx;">⏳ 已提交答卷，等待发布人批改</text>
      <view class="status-flow-hint">
        <text class="status-step done">📝 答题 ✓</text>
        <text class="status-step current">⏳ 等待批改</text>
        <text class="status-step">✅ 查看详情</text>
      </view>
      <text style="font-size:24rpx;color:#b8929e;display:block;margin-top:12rpx;">发布人批改后你会收到结果通知，通过后可查看活动联系方式</text>
    </view>
    <!-- 通过 -->
    <view class="card" v-else-if="applyStatus === 1">
      <text style="color:#059669;font-weight:700;font-size:32rpx;display:block;text-align:center;">🎉 恭喜通过！</text>
      <view class="status-flow-hint">
        <text class="status-step done">📝 答题 ✓</text>
        <text class="status-step done">✅ 已通过 ✓</text>
        <text class="status-step done">🔓 已解锁 ✓</text>
      </view>

      <!-- 活动倒计时 -->
      <view v-if="activity.activityTime && countdownText" style="margin-top:16rpx;padding:16rpx 20rpx;background:linear-gradient(135deg,#fef3f8,#f0e6ff);border-radius:16rpx;text-align:center;">
        <text style="font-size:24rpx;color:#7c5270;display:block;">⏰ 距活动开始</text>
        <text style="font-size:32rpx;font-weight:700;color:#c084fc;display:block;margin-top:4rpx;">{{ countdownText }}</text>
      </view>

      <!-- 联系方式 -->
      <view v-if="contactInfo" style="margin-top:16rpx;padding:20rpx;background:#ecfdf5;border-radius:16rpx;border:1px solid #a7f3d0;">
        <text style="font-size:26rpx;color:#065f46;font-weight:700;display:block;margin-bottom:8rpx;">🔗 活动联系方式</text>
        <text style="font-size:28rpx;color:#047857;line-height:1.6;" selectable>{{ contactInfo }}</text>
        <text style="font-size:22rpx;color:#6ee7b7;display:block;margin-top:8rpx;">长按可复制</text>
      </view>
      <view v-else style="margin-top:16rpx;text-align:center;">
        <text style="font-size:24rpx;color:#b8929e;">发布人暂未设置联系方式</text>
      </view>

      <!-- 公告 -->
      <view v-if="activity.announcement" style="margin-top:16rpx;padding:16rpx;background:#fef9f0;border-radius:12rpx;border:1px dashed #fbbf24;">
        <text style="font-size:24rpx;color:#92400e;font-weight:600;display:block;margin-bottom:4rpx;">📢 活动公告</text>
        <text style="font-size:26rpx;color:#78350f;line-height:1.6;">{{ activity.announcement }}</text>
      </view>
    </view>

    <!-- 通过后：活动成员 -->
    <view class="card" v-if="applyStatus === 1 || isOwner">
      <text class="section-title">👥 活动成员（{{ members.length }}人）</text>
      <view style="display:flex;flex-wrap:wrap;gap:16rpx;">
        <view v-for="m in members" :key="m.userId" class="member-item" @tap="goUser(m.userId)">
          <view class="avatar-sm">{{ (m.nickname || '?').charAt(0) }}</view>
          <view>
            <text style="font-size:24rpx;color:#4a2040;font-weight:600;display:block;">{{ m.nickname }}</text>
            <text style="font-size:20rpx;color:#b8929e;">{{ m.role }}{{ m.city ? ' · ' + m.city : '' }}</text>
          </view>
        </view>
      </view>
      <view v-if="members.length === 0" style="text-align:center;padding:20rpx;">
        <text style="font-size:24rpx;color:#b8929e;">暂无通过的成员</text>
      </view>
    </view>

    <!-- 通过后：留言板 -->
    <view class="card" v-if="applyStatus === 1 || isOwner">
      <text class="section-title">💬 活动留言板</text>
      <!-- 发留言 -->
      <view style="display:flex;gap:12rpx;margin-bottom:16rpx;">
        <input v-model="newMessage" placeholder="说点什么..." maxlength="200"
          style="flex:1;background:#fff5f7;border:1px solid #fce4ec;border-radius:24rpx;padding:12rpx 20rpx;font-size:26rpx;color:#4a2040;" />
        <button class="btn-send" @tap="sendMessage" :disabled="!newMessage.trim()">发送</button>
      </view>
      <!-- 留言列表 -->
      <view v-if="messages.length === 0" style="text-align:center;padding:20rpx;">
        <text style="font-size:24rpx;color:#b8929e;">暂无留言，来第一个发言吧~</text>
      </view>
      <view v-for="msg in messages" :key="msg.id" class="msg-item">
        <view style="display:flex;justify-content:space-between;align-items:center;margin-bottom:4rpx;">
          <view style="display:flex;align-items:center;gap:8rpx;">
            <text style="font-size:24rpx;font-weight:600;color:#4a2040;" @tap="goUser(msg.userId)">{{ msg.nickname }}</text>
            <text v-if="msg.isOwner" style="font-size:18rpx;color:#fff;background:#f472b6;padding:2rpx 8rpx;border-radius:8rpx;">发起人</text>
          </view>
          <text style="font-size:20rpx;color:#ccc;">{{ formatMsgTime(msg.createTime) }}</text>
        </view>
        <text style="font-size:26rpx;color:#4a2040;line-height:1.6;">{{ msg.content }}</text>
      </view>
    </view>
    <!-- 未通过 -->
    <view class="card status-card" v-if="applyStatus === 2 && !isOwner">
      <text style="color:#e11d48;font-weight:700;">❌ 答卷未通过</text>
      <text style="font-size:24rpx;color:#b8929e;display:block;margin-top:8rpx;">没关系，去看看其他活动吧，总有适合你的活动~</text>
    </view>

    <!-- 举报弹窗（居中遮罩层） -->
    <view v-if="showReport" class="overlay" @tap.self="showReport = false">
      <view class="popup-content">
        <text class="popup-title">🚩 举报</text>
        <text style="font-size:24rpx;color:#b8929e;display:block;margin-bottom:20rpx;">请选择举报原因</text>
        <view v-for="r in reasons" :key="r" class="reason-item" @tap="reportReason = r"
          :style="reportReason === r ? 'background:#fce4ec;border-color:#f472b6;font-weight:600;' : ''">
          <text>{{ reportReason === r ? '✓ ' : '' }}{{ r }}</text>
        </view>
        <textarea v-model="reportDetail" placeholder="详细说明（选填）" maxlength="200"
          style="width:100%;height:120rpx;background:#fff5f7;border:1px solid #fce4ec;border-radius:12rpx;padding:16rpx;font-size:26rpx;color:#4a2040;margin-top:16rpx;box-sizing:border-box;" />
        <view style="display:flex;gap:16rpx;margin-top:20rpx;">
          <button class="btn-cancel" @tap="showReport = false">取消</button>
          <button class="btn-danger" @tap="submitReport" :disabled="!reportReason">提交举报</button>
        </view>
      </view>
    </view>
  </view>
  <view v-else style="text-align:center;padding:100rpx;color:#b8929e;">加载中...</view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { onLoad, onShow, onHide } from '@dcloudio/uni-app'
import { getActivityDetail } from '@/api/activity'
import { applyActivity, getSentRequests } from '@/api/match'
import { request } from '@/api/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const activity = ref(null)
const applyMsg = ref('')
const applying = ref(false)
const applyStatus = ref(-1)
const matchId = ref(null)
const contactInfo = ref('')
const members = ref([])
const messages = ref([])
const newMessage = ref('')
const countdownText = ref('')
let activityId = null
let countdownTimer = null

// 举报
const showReport = ref(false)
const reportReason = ref('')
const reportDetail = ref('')
const reasons = ['色情低俗', '辱骂骚扰', '虚假信息', '广告推销', '诈骗欺诈', '其他']

const currentUserId = ref(uni.getStorageSync('userId'))
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isOwner = computed(() => activity.value && currentUserId.value && activity.value.userId == currentUserId.value)
const canApply = computed(() => isLoggedIn.value && !isOwner.value && applyStatus.value === -1 && activity.value?.status === 1)

function goUser(id) { uni.navigateTo({ url: `/pages/profile/index?userId=${id}` }) }
function goTo(url) { uni.navigateTo({ url }) }

// 发布人更新公告
function editAnnouncement() {
  uni.showModal({
    title: '更新活动公告',
    editable: true,
    placeholderText: '输入公告内容（如集合地点、注意事项等）',
    content: activity.value?.announcement || '',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({ url: `/activity/announcement/${activityId}`, method: 'POST', data: { announcement: res.content }, needAuth: true })
          uni.showToast({ title: '公告已更新', icon: 'success' })
          activity.value.announcement = res.content
        } catch (e) { uni.showToast({ title: e?.message || '更新失败', icon: 'none' }) }
      }
    }
  })
}

// 获取联系方式（通过审核后，静默请求不弹错误）
async function loadContactInfo() {
  if (!activityId || !isLoggedIn.value) return
  try {
    const token = uni.getStorageSync('token')
    const res = await new Promise((resolve) => {
      uni.request({
        url: `https://global-drama-tracker-production.up.railway.app/api/activity/contact/${activityId}`,
        header: { 'Authorization': `Bearer ${token}` },
        success: (r) => resolve(r),
        fail: () => resolve(null)
      })
    })
    if (res && res.data && res.data.code === 200) {
      contactInfo.value = res.data.data || ''
    }
  } catch {}
}

// 加载活动成员
async function loadMembers() {
  if (!activityId || !isLoggedIn.value) return
  try {
    const res = await request({ url: `/activity/members/${activityId}`, needAuth: true })
    members.value = res || []
  } catch { members.value = [] }
}

// 加载留言板
async function loadMessages() {
  if (!activityId || !isLoggedIn.value) return
  try {
    const res = await request({ url: `/activity/messages/${activityId}`, needAuth: true })
    messages.value = (res && res.records) ? res.records : []
  } catch { messages.value = [] }
}

// 发送留言
async function sendMessage() {
  if (!newMessage.value.trim()) return
  try {
    await request({ url: `/activity/messages/${activityId}`, method: 'POST', needAuth: true,
      data: { content: newMessage.value.trim() } })
    newMessage.value = ''
    loadMessages()
  } catch (e) { uni.showToast({ title: e?.message || '发送失败', icon: 'none' }) }
}

// 留言时间格式化
function formatMsgTime(time) {
  if (!time) return ''
  let d
  if (Array.isArray(time)) {
    d = new Date(time[0], time[1] - 1, time[2], time[3] || 0, time[4] || 0, time[5] || 0)
  } else if (typeof time === 'string') {
    d = new Date(time.replace('T', ' ').replace(/-/g, '/'))
  } else {
    d = new Date(time)
  }
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  const diffMs = now - d
  if (diffMs < 60000) return '刚刚'
  if (diffMs < 3600000) return Math.floor(diffMs / 60000) + '分钟前'
  if (diffMs < 86400000) return Math.floor(diffMs / 3600000) + '小时前'
  return (d.getMonth() + 1) + '/' + d.getDate() + ' ' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
}

// 活动倒计时
function startCountdown() {
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(updateCountdown, 1000)
  updateCountdown()
}
function updateCountdown() {
  const timeStr = activity.value?.activityTime
  if (!timeStr) { countdownText.value = ''; return }
  const target = new Date(timeStr.replace(/-/g, '/'))
  if (isNaN(target.getTime())) { countdownText.value = ''; return }
  const diff = target - new Date()
  if (diff <= 0) { countdownText.value = '活动已开始'; return }
  const days = Math.floor(diff / 86400000)
  const hours = Math.floor((diff % 86400000) / 3600000)
  const mins = Math.floor((diff % 3600000) / 60000)
  const secs = Math.floor((diff % 60000) / 1000)
  countdownText.value = (days > 0 ? days + '天 ' : '') + hours + '小时 ' + mins + '分 ' + secs + '秒'
}
async function doTeamComplete() {
  uni.showModal({
    title: '确认活动满员？', content: '标记后活动将不再接受新的申请',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({ url: `/match/team-complete/${activityId}`, method: 'POST', needAuth: true })
          uni.showToast({ title: '已标记完成', icon: 'success' })
          activity.value.teamComplete = 1
          activity.value.status = 2
        } catch (e) { uni.showToast({ title: e?.message || '操作失败', icon: 'none' }) }
      }
    }
  })
}

async function doDeleteActivity() {
  uni.showModal({
    title: '确认删除？', content: '删除后不可恢复',
    confirmColor: '#e11d48',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({ url: `/activity/delete/${activityId}`, method: 'POST', needAuth: true })
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 500)
        } catch (e) { uni.showToast({ title: e?.message || '删除失败', icon: 'none' }) }
      }
    }
  })
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

// 举报
async function submitReport() {
  try {
    await request({ url: '/report/submit', method: 'POST', needAuth: true,
      data: { targetType: 'activity', targetId: Number(activityId), reason: reportReason.value, detail: reportDetail.value } })
    uni.showToast({ title: '举报已提交', icon: 'success' })
    showReport.value = false; reportReason.value = ''; reportDetail.value = ''
  } catch (e) { uni.showToast({ title: e?.message || '提交失败', icon: 'none' }) }
}

// 加载/刷新活动详情和申请状态
async function refreshData() {
  if (!activityId) return
  try { activity.value = await getActivityDetail(activityId) } catch {}
  if (isLoggedIn.value) {
    try {
      const sent = await getSentRequests()
      const myReq = (sent || []).find(r => r.activityId == activityId)
      if (myReq) { applyStatus.value = myReq.status; matchId.value = myReq.id }
    } catch {}
    // 通过后获取联系方式（仅申请人，发布人不需要）
    if (applyStatus.value === 1 && !isOwner.value) {
      loadContactInfo()
    }
    // 通过后或发布人，加载成员+留言
    if (applyStatus.value === 1 || isOwner.value) {
      loadMembers()
      loadMessages()
      startCountdown()
    }
  }
}

onLoad((options) => { activityId = options?.id })
onShow(() => {
  // 登录返回后，重新初始化 store 和 userId，确保响应式状态立即更新
  userStore.init()
  currentUserId.value = uni.getStorageSync('userId')
  refreshData()
})
onHide(() => { if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null } })
onUnmounted(() => { if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null } })
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
.avatar-img { width: 72rpx; height: 72rpx; border-radius: 50%; flex-shrink: 0; }
.avatar-sm { width: 48rpx; height: 48rpx; border-radius: 50%; background: #f9a8d4; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 22rpx; font-weight: 700; flex-shrink: 0; }
.author-name { font-weight: 600; font-size: 28rpx; color: #4a2040; display: block; }
.author-sub { font-size: 24rpx; color: #b8929e; display: block; }
.desc { font-size: 28rpx; color: #4a2040; line-height: 1.7; display: block; margin: 12rpx 0; }
.info-grid { display: flex; flex-wrap: wrap; gap: 12rpx; }
.info-item { flex: 1 1 45%; display: flex; gap: 8rpx; font-size: 26rpx; color: #7c5270; min-width: 280rpx; }
.info-label { color: #b8929e; }
.section-title { font-size: 30rpx; font-weight: 700; color: #4a2040; display: block; margin-bottom: 16rpx; }
.status-card { text-align: center; padding: 32rpx; }
.flow-hint { display: flex; align-items: center; justify-content: center; gap: 8rpx; margin-bottom: 16rpx; padding: 16rpx; background: linear-gradient(135deg, #fef3f8, #f0e6ff); border-radius: 16rpx; }
.flow-step { display: flex; flex-direction: column; align-items: center; gap: 4rpx; }
.flow-icon { font-size: 32rpx; }
.flow-text { font-size: 20rpx; color: #7c5270; font-weight: 600; }
.flow-arrow { font-size: 24rpx; color: #c084fc; font-weight: 700; }
.status-flow-hint { display: flex; align-items: center; justify-content: center; gap: 12rpx; margin-top: 16rpx; padding: 12rpx 16rpx; background: #fef9f0; border-radius: 12rpx; }
.status-step { font-size: 22rpx; color: #b8929e; padding: 6rpx 16rpx; border-radius: 20rpx; background: #f5f0f2; }
.status-step.done { color: #059669; background: #ecfdf5; }
.status-step.current { color: #d97706; background: #fef3c7; font-weight: 700; }
.btn-primary { margin-top: 0; background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 28rpx; font-weight: 600; padding: 16rpx 0; }
.btn-outline { background: #fff; color: #f472b6; border: 2px solid #fce4ec; border-radius: 40rpx; font-size: 28rpx; }
.btn-danger-outline { background: #fff; color: #e11d48; border: 2px solid #fecdd3; border-radius: 40rpx; font-size: 28rpx; }
.btn-cancel { flex: 1; background: #f5f5f5; color: #666; border: none; border-radius: 40rpx; font-size: 28rpx; }
.btn-danger { flex: 1; background: #e11d48; color: #fff; border: none; border-radius: 40rpx; font-size: 28rpx; }

/* 举报弹窗 */
.overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.popup-content { background: #fff; border-radius: 24rpx; padding: 32rpx; width: 600rpx; max-height: 80vh; overflow-y: auto; }
.popup-title { font-size: 32rpx; font-weight: 700; color: #4a2040; display: block; margin-bottom: 8rpx; text-align: center; }
.reason-item { padding: 16rpx 20rpx; border: 1px solid #fce4ec; border-radius: 12rpx; margin-bottom: 12rpx; font-size: 28rpx; color: #4a2040; }

/* 活动成员 */
.member-item { display: flex; gap: 10rpx; align-items: center; background: #fef7fa; border-radius: 12rpx; padding: 10rpx 16rpx; min-width: 200rpx; }

/* 留言板 */
.msg-item { padding: 16rpx; border-bottom: 1px solid #fce4ec; }
.msg-item:last-child { border-bottom: none; }
.btn-send { background: linear-gradient(135deg,#f472b6,#c084fc); color: #fff; border: none; border-radius: 24rpx; font-size: 26rpx; padding: 0 28rpx; height: 64rpx; line-height: 64rpx; flex-shrink: 0; }
</style>
