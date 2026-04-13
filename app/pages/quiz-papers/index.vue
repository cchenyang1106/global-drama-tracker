<template>
  <view class="page">
    <text class="title">📊 答卷批改</text>

    <view v-if="papers.length === 0" class="empty">暂无人提交答卷</view>

    <view v-for="p in papers" :key="p.userId" class="card">
      <view class="paper-head">
        <view class="avatar-sm">{{ (p.nickname || '?').charAt(0) }}</view>
        <view style="flex:1;">
          <text class="p-name">{{ p.nickname }}</text>
          <text class="p-sub">{{ [p.gender===1?'男':p.gender===2?'女':'', p.age?p.age+'岁':'', p.city].filter(Boolean).join(' · ') }}</text>
        </view>
        <text v-if="p.status === 1" class="status-pass">✅ 已通过</text>
        <text v-else-if="p.status === 2" class="status-fail">❌ 未通过</text>
        <text v-else class="status-pending">⏳ 待批改</text>
      </view>

      <view v-for="(a, i) in p.answers" :key="a.id" class="answer-item">
        <text class="q-label">题目 {{ i + 1 }}：</text>
        <text class="a-text">{{ a.answerText }}</text>
        <view v-if="a.answerImages" class="img-row">
          <image v-for="(img, j) in parseImages(a.answerImages)" :key="j" :src="img" class="thumb"
            @tap="uni.previewImage({urls: parseImages(a.answerImages), current: j})" />
        </view>
      </view>

      <view v-if="p.status === 0" class="grade-actions">
        <button class="btn-pass" @tap="grade(p.userId, 1)">✅ 通过</button>
        <button class="btn-fail" @tap="grade(p.userId, 2)">❌ 不通过</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPapers, gradePaper } from '@/api/quiz'

const papers = ref([])
let activityId = null

function parseImages(str) {
  if (!str) return []
  try { return JSON.parse(str) } catch { return [] }
}

async function grade(userId, status) {
  const action = status === 1 ? '通过' : '不通过'
  uni.showModal({
    title: `确认${action}？`,
    content: status === 1 ? '通过后将自动拉入群聊' : '确认标记为不通过？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await gradePaper(activityId, userId, { status })
          uni.showToast({ title: `已${action}`, icon: 'success' })
          loadPapers()
        } catch (e) { uni.showToast({ title: e?.message || '操作失败', icon: 'none' }) }
      }
    }
  })
}

async function loadPapers() {
  try { papers.value = await getPapers(activityId) || [] } catch {}
}

onLoad((options) => {
  activityId = options?.activityId
  if (activityId) loadPapers()
})
</script>

<style scoped>
.page { padding: 20rpx; }
.title { display: block; font-size: 36rpx; font-weight: 800; color: #4a2040; text-align: center; margin-bottom: 24rpx; }
.empty { text-align: center; padding: 60rpx; color: #b8929e; }
.card { background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx; border: 1px solid #fce4ec; }
.paper-head { display: flex; gap: 12rpx; align-items: center; margin-bottom: 16rpx; padding-bottom: 16rpx; border-bottom: 1px solid #fce4ec; }
.avatar-sm { width: 60rpx; height: 60rpx; border-radius: 50%; background: #f9a8d4; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 700; flex-shrink: 0; }
.p-name { font-size: 28rpx; font-weight: 600; color: #4a2040; display: block; }
.p-sub { font-size: 22rpx; color: #b8929e; display: block; }
.status-pass { color: #059669; font-size: 24rpx; font-weight: 600; }
.status-fail { color: #e11d48; font-size: 24rpx; }
.status-pending { color: #d97706; font-size: 24rpx; }
.answer-item { padding: 12rpx 0; border-top: 1px solid #fef0f5; }
.q-label { font-size: 24rpx; color: #b8929e; display: block; }
.a-text { font-size: 28rpx; color: #4a2040; line-height: 1.6; display: block; margin-top: 4rpx; }
.img-row { display: flex; gap: 12rpx; margin-top: 8rpx; }
.thumb { width: 150rpx; height: 150rpx; border-radius: 12rpx; }
.grade-actions { display: flex; gap: 16rpx; margin-top: 20rpx; }
.btn-pass { flex: 1; background: #059669; color: #fff; border: none; border-radius: 40rpx; font-size: 28rpx; }
.btn-fail { flex: 1; background: #fff; color: #e11d48; border: 2px solid #e11d48; border-radius: 40rpx; font-size: 28rpx; }
</style>
