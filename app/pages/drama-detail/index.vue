<template>
  <view class="page-bg">
    <!-- 自定义导航 -->
    <view class="detail-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <text class="back-btn" @tap="goBack">← 返回</text>
    </view>

    <view v-if="drama">
      <!-- 海报 + 基本信息 -->
      <view class="detail-header">
        <image class="detail-poster" :src="drama.posterUrl || defaultPoster" mode="aspectFill" />
        <view class="detail-info">
          <text class="detail-title">{{ drama.title }}</text>
          <text class="detail-original" v-if="drama.originalTitle">{{ drama.originalTitle }}</text>
          <view class="detail-tags">
            <text class="tag">{{ getRegionLabel(drama.region) }}</text>
            <text class="tag" :style="{ color: getStatusColor(drama.status) }">{{ getStatusLabel(drama.status) }}</text>
          </view>
          <text class="detail-rating" v-if="drama.userRating">⭐ {{ Number(drama.userRating).toFixed(1) }} <text class="rating-count">{{ drama.ratingCount || 0 }}人评</text></text>
        </view>
      </view>

      <!-- 详情信息 -->
      <view class="detail-section" v-if="drama.description">
        <text class="sec-title">📖 剧情简介</text>
        <text class="sec-text">{{ drama.description }}</text>
      </view>

      <view class="detail-section">
        <view class="meta-row" v-if="drama.directors"><text class="meta-key">导演</text><text class="meta-val">{{ drama.directors }}</text></view>
        <view class="meta-row" v-if="drama.actors"><text class="meta-key">主演</text><text class="meta-val">{{ drama.actors }}</text></view>
        <view class="meta-row" v-if="drama.genres"><text class="meta-key">类型</text><text class="meta-val">{{ drama.genres }}</text></view>
        <view class="meta-row" v-if="drama.platforms"><text class="meta-key">平台</text><text class="meta-val">{{ drama.platforms }}</text></view>
        <view class="meta-row" v-if="drama.releaseDate"><text class="meta-key">首播</text><text class="meta-val">{{ drama.releaseDate }}</text></view>
      </view>

      <!-- 评论区 -->
      <view class="detail-section">
        <text class="sec-title">💬 评论讨论</text>

        <!-- 写评论 -->
        <view class="comment-form" v-if="userStore.isLoggedIn">
          <text class="form-label">以 {{ userStore.nickname }} 身份评论</text>
          <textarea class="form-textarea" v-model="commentContent" placeholder="写下你的评论..." maxlength="500" />
          <view class="form-bottom">
            <view class="rating-row">
              <text class="form-label">评分：</text>
              <view class="star-row">
                <text v-for="s in 10" :key="s" class="star" :class="{ active: s <= commentRating }" @tap="commentRating = s">★</text>
              </view>
              <text class="rating-val" v-if="commentRating > 0">{{ commentRating }}分</text>
            </view>
            <view class="submit-btn" @tap="submitComment">发表</view>
          </view>
        </view>
        <view v-else class="login-prompt" @tap="goLogin">登录后参与评论讨论 ></view>

        <!-- 评论列表 -->
        <view class="comment-item" v-for="c in comments" :key="c.id">
          <view class="c-header">
            <text class="c-author">{{ c.nickname }}</text>
            <text class="c-rating" v-if="c.rating">{{ Number(c.rating).toFixed(1) }}分</text>
            <text class="c-featured" v-if="c.featured">精选</text>
            <text class="c-time">{{ formatTime(c.createTime) }}</text>
          </view>
          <text class="c-content">{{ c.content }}</text>
          <text class="c-like" @tap="handleLike(c)">👍 {{ c.likeCount || 0 }}</text>
        </view>

        <view v-if="comments.length === 0" class="empty-hint">暂无评论</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDramaDetail } from '@/api/drama'
import { getComments, addComment, likeComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { REGIONS, STATUS_MAP } from '@/utils/config'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 44
const defaultPoster = 'https://via.placeholder.com/300x420/1e293b/475569?text=No+Poster'
const userStore = useUserStore()

const drama = ref(null)
const comments = ref([])
const commentContent = ref('')
const commentRating = ref(0)

function getRegionLabel(code) { return REGIONS[code]?.label || '其他' }
function getStatusLabel(s) { return STATUS_MAP[s]?.label || '未知' }
function getStatusColor(s) { return STATUS_MAP[s]?.color || '#94a3b8' }
function goBack() { uni.navigateBack() }
function goLogin() { uni.navigateTo({ url: '/pages/login/index' }) }

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}-${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function loadComments(id) {
  try {
    const data = await getComments(id, 1, 50)
    comments.value = data?.records || data?.list || []
  } catch {}
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  try {
    const id = drama.value.id
    await addComment({
      dramaId: id,
      nickname: userStore.nickname,
      content: commentContent.value,
      rating: commentRating.value > 0 ? commentRating.value : null,
    })
    uni.showToast({ title: '评论成功', icon: 'success' })
    commentContent.value = ''
    commentRating.value = 0
    loadComments(id)
  } catch {}
}

async function handleLike(c) {
  try { await likeComment(c.id); c.likeCount = (c.likeCount || 0) + 1 } catch {}
}

onMounted(async () => {
  const id = uni.getLaunchOptionsSync?.()?.query?.id || ''
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const dramaId = currentPage?.options?.id || id
  if (!dramaId) return

  userStore.init()
  try { drama.value = await getDramaDetail(dramaId) } catch {}
  loadComments(dramaId)
})
</script>

<style lang="scss">
.detail-nav { background: #0f172a; padding-bottom: 12rpx; padding-left: 24rpx; }
.back-btn { font-size: 28rpx; color: #60a5fa; }

.detail-header { display: flex; gap: 24rpx; padding: 24rpx; }
.detail-poster { width: 220rpx; height: 308rpx; border-radius: 16rpx; flex-shrink: 0; background: #1e293b; }
.detail-info { flex: 1; min-width: 0; }
.detail-title { font-size: 36rpx; font-weight: 800; color: #f1f5f9; display: block; }
.detail-original { font-size: 24rpx; color: #64748b; margin-top: 6rpx; display: block; }
.detail-tags { display: flex; gap: 12rpx; margin-top: 16rpx; }
.tag { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; background: rgba(99,102,241,0.12); color: #818cf8; }
.detail-rating { font-size: 36rpx; font-weight: 700; color: #fbbf24; margin-top: 20rpx; display: block; }
.rating-count { font-size: 22rpx; color: #94a3b8; font-weight: 400; }

.detail-section { padding: 0 24rpx 32rpx; }
.sec-title { font-size: 30rpx; font-weight: 700; color: #f1f5f9; margin-bottom: 16rpx; display: block; }
.sec-text { font-size: 26rpx; color: #cbd5e1; line-height: 1.8; }

.meta-row { display: flex; gap: 16rpx; margin-bottom: 10rpx; font-size: 26rpx; }
.meta-key { color: #64748b; width: 80rpx; flex-shrink: 0; }
.meta-val { color: #cbd5e1; flex: 1; }

.comment-form { background: #1e293b; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.form-label { font-size: 24rpx; color: #94a3b8; display: block; margin-bottom: 10rpx; }
.form-textarea { width: 100%; height: 160rpx; background: #0f172a; border-radius: 12rpx; padding: 16rpx; color: #f1f5f9; font-size: 26rpx; box-sizing: border-box; }
.form-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 12rpx; }
.rating-row { display: flex; align-items: center; gap: 8rpx; }
.star-row { display: flex; }
.star { font-size: 32rpx; color: #334155; }
.star.active { color: #fbbf24; }
.rating-val { font-size: 24rpx; color: #fbbf24; }
.submit-btn { padding: 12rpx 32rpx; background: #6366f1; border-radius: 24rpx; color: white; font-size: 26rpx; font-weight: 600; }

.login-prompt { text-align: center; padding: 24rpx; background: #1e293b; border-radius: 12rpx; color: #6366f1; font-size: 26rpx; margin-bottom: 20rpx; }

.comment-item { background: #1e293b; border-radius: 12rpx; padding: 16rpx; margin-bottom: 12rpx; }
.c-header { display: flex; align-items: center; gap: 10rpx; margin-bottom: 8rpx; }
.c-author { font-size: 26rpx; font-weight: 600; color: #60a5fa; }
.c-rating { font-size: 22rpx; color: #fbbf24; background: rgba(251,191,36,0.1); padding: 2rpx 10rpx; border-radius: 10rpx; }
.c-featured { font-size: 20rpx; color: #f59e0b; background: rgba(245,158,11,0.1); padding: 2rpx 10rpx; border-radius: 10rpx; }
.c-time { font-size: 22rpx; color: #64748b; margin-left: auto; }
.c-content { font-size: 26rpx; color: #cbd5e1; line-height: 1.7; display: block; }
.c-like { font-size: 24rpx; color: #64748b; margin-top: 8rpx; display: block; }
.empty-hint { text-align: center; padding: 40rpx; color: #64748b; font-size: 26rpx; }
</style>
