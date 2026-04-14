<template>
  <view class="page">
    <view class="hero">
      <text class="hero-title">趣活圈，发现精彩活动 🎯</text>
      <view class="search-bar">
        <input class="search-input" v-model="keyword" placeholder="搜索活动..." @confirm="loadData()" />
      </view>
    </view>

    <scroll-view class="cats" scroll-x>
      <text v-for="c in cats" :key="c.value" :class="['cat-item', { active: currentCat === c.value }]"
        @tap="currentCat = c.value; loadData()">{{ c.icon }} {{ c.label }}</text>
    </scroll-view>

    <view class="list">
      <view v-for="item in list" :key="item.id" class="card" @tap="goDetail(item.id)">
        <view class="card-top">
          <view class="author">
            <view class="avatar">{{ (item.authorName || '?').charAt(0) }}</view>
            <view>
              <text class="name">{{ item.authorName || '匿名' }}</text>
              <text class="sub">{{ item.authorCity || '' }}</text>
            </view>
          </view>
          <text class="cat-badge">{{ item.category }}</text>
        </view>
        <text class="card-title">{{ item.title }}</text>
        <text class="card-desc">{{ item.description }}</text>
        <view class="card-meta">
          <text v-if="item.location">📍{{ item.location }}</text>
          <text v-if="item.activityTime">🕐{{ item.activityTime }}</text>
          <text>👥{{ item.joinedCount || 0 }}/{{ item.maxPeople || 1 }}</text>
        </view>
      </view>
      <view v-if="list.length === 0 && !loading" class="empty">暂无活动，快来发布第一个吧！</view>
    </view>

    <view class="fab" @tap="goPublish">✏️</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { getActivities } from '@/api/activity'
import { getReceivedRequests } from '@/api/match'

const keyword = ref('')
const currentCat = ref('all')
const list = ref([])
const loading = ref(false)

const cats = [
  { value: 'all', label: '全部', icon: '🔥' },
  { value: '旅游', label: '旅游', icon: '✈️' },
  { value: '运动', label: '运动', icon: '⚽' },
  { value: '美食', label: '美食', icon: '🍔' },
  { value: '学习', label: '学习', icon: '📚' },
  { value: '游戏', label: '游戏', icon: '🎮' },
  { value: '追星', label: '追星', icon: '⭐' },
  { value: '其他', label: '其他', icon: '💡' },
]

function goDetail(id) { uni.navigateTo({ url: `/pages/activity-detail/index?id=${id}` }) }
function goPublish() {
  const token = uni.getStorageSync('token')
  if (!token) return uni.navigateTo({ url: '/pages/login/index' })
  uni.navigateTo({ url: '/pages/publish/index' })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getActivities({
      page: 1, size: 20,
      category: currentCat.value === 'all' ? '' : currentCat.value,
      keyword: keyword.value,
    })
    list.value = data?.records || []
  } catch {}
  loading.value = false
}

let unreadTimer = null

onShow(() => {
  loadData()
  checkUnread()
  unreadTimer = setInterval(checkUnread, 15000)
})
onHide(() => {
  if (unreadTimer) { clearInterval(unreadTimer); unreadTimer = null }
})

async function checkUnread() {
  if (!uni.getStorageSync('token')) return
  try {
    const received = await getReceivedRequests()
    const pending = (received || []).filter(r => r.status === 0).length
    if (pending > 0) {
      uni.setTabBarBadge({ index: 1, text: String(pending > 99 ? '99+' : pending), fail() {} })
    } else {
      uni.removeTabBarBadge({ index: 1, fail() {} })
    }
  } catch {}
}
</script>

<style>
.page { padding: 16rpx; background: #fff5f7; min-height: 100vh; }
.hero { text-align: center; padding: 40rpx 0 20rpx; }
.hero-title { font-size: 44rpx; font-weight: 800; color: #f472b6; }
.search-bar { margin-top: 20rpx; padding: 0 40rpx; }
.search-input { background: #ffffff; border-radius: 16rpx; padding: 16rpx 24rpx; color: #4a2040; font-size: 28rpx; }

.cats { white-space: nowrap; padding: 16rpx 0; }
.cat-item { display: inline-block; padding: 12rpx 24rpx; margin-right: 12rpx; border-radius: 32rpx; background: #ffffff; color: #7c5270; font-size: 26rpx; }
.cat-item.active { background: rgba(99,102,241,0.2); color: #f472b6; }

.card { background: #ffffff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.author { display: flex; align-items: center; gap: 16rpx; }
.avatar { width: 64rpx; height: 64rpx; border-radius: 50%; background: #f472b6; color: white; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 700; }
.name { font-size: 28rpx; font-weight: 600; color: #4a2040; display: block; }
.sub { font-size: 22rpx; color: #b8929e; }
.cat-badge { background: rgba(99,102,241,0.15); color: #f472b6; padding: 6rpx 16rpx; border-radius: 16rpx; font-size: 24rpx; }
.card-title { font-size: 32rpx; font-weight: 700; color: #4a2040; margin-bottom: 8rpx; display: block; }
.card-desc { font-size: 26rpx; color: #7c5270; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-meta { display: flex; gap: 16rpx; margin-top: 12rpx; font-size: 24rpx; color: #b8929e; flex-wrap: wrap; }
.empty { text-align: center; padding: 80rpx; color: #b8929e; font-size: 28rpx; }
.fab { position: fixed; right: 32rpx; bottom: 180rpx; width: 96rpx; height: 96rpx; border-radius: 50%; background: linear-gradient(135deg,#f472b6,#c084fc); display: flex; align-items: center; justify-content: center; font-size: 40rpx; box-shadow: 0 8rpx 24rpx rgba(99,102,241,0.4); }
</style>
