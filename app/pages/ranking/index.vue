<template>
  <view class="page-bg">
    <!-- Tab 切换 -->
    <view class="rank-tabs">
      <text class="tab-item" :class="{ active: currentTab === 'daily' }" @tap="switchTab('daily')">日榜</text>
      <text class="tab-item" :class="{ active: currentTab === 'weekly' }" @tap="switchTab('weekly')">周榜</text>
      <text class="tab-item" :class="{ active: currentTab === 'monthly' }" @tap="switchTab('monthly')">月榜</text>
      <text class="tab-item" :class="{ active: currentTab === 'new' }" @tap="switchTab('new')">新剧榜</text>
    </view>

    <!-- 排行列表 -->
    <view class="rank-list">
      <view class="rank-item" v-for="(d, i) in list" :key="d.dramaId || d.id" @tap="goDetail(d.dramaId || d.id)">
        <text class="rank-num" :class="{ gold: i === 0, silver: i === 1, bronze: i === 2 }">{{ i + 1 }}</text>
        <image class="rank-poster" :src="d.posterUrl || defaultPoster" mode="aspectFill" />
        <view class="rank-info">
          <text class="rank-title">{{ d.title || d.dramaTitle }}</text>
          <text class="rank-sub">{{ d.genres || '' }}</text>
        </view>
        <view class="rank-right">
          <text class="rank-score" v-if="d.score || d.userRating">{{ Number(d.score || d.userRating).toFixed(1) }}</text>
          <text class="rank-hot" v-if="d.hotScore">🔥 {{ formatHot(d.hotScore) }}</text>
        </view>
      </view>
    </view>

    <view v-if="list.length === 0" class="empty">
      <text>暂无排行数据</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDailyRanking, getWeeklyRanking, getMonthlyRanking, getNewRanking } from '@/api/ranking'

const defaultPoster = 'https://via.placeholder.com/300x420/1e293b/475569?text=No+Poster'
const currentTab = ref('daily')
const list = ref([])

const apiMap = { daily: getDailyRanking, weekly: getWeeklyRanking, monthly: getMonthlyRanking, new: getNewRanking }

function formatHot(val) {
  if (val >= 10000) return (val / 10000).toFixed(1) + '万'
  return val
}

function goDetail(id) { if (id) uni.navigateTo({ url: `/pages/drama-detail/index?id=${id}` }) }

async function loadData() {
  try {
    const fn = apiMap[currentTab.value]
    const data = await fn({ page: 1, size: 50 })
    list.value = data?.list || data?.records || data || []
  } catch { list.value = [] }
}

function switchTab(tab) { currentTab.value = tab; loadData() }

onMounted(loadData)
</script>

<style lang="scss">
.rank-tabs { display: flex; padding: 20rpx 24rpx; gap: 16rpx; }
.tab-item { flex: 1; text-align: center; padding: 16rpx; border-radius: 30rpx; font-size: 28rpx; color: #94a3b8; background: #1e293b; font-weight: 600; }
.tab-item.active { background: #6366f1; color: white; }

.rank-list { padding: 0 24rpx; }
.rank-item {
  display: flex; align-items: center; gap: 16rpx;
  background: #1e293b; border-radius: 16rpx; padding: 16rpx; margin-bottom: 12rpx;
}
.rank-num { width: 52rpx; height: 52rpx; border-radius: 50%; background: rgba(148,163,184,0.1); text-align: center; line-height: 52rpx; font-size: 28rpx; font-weight: 800; color: #94a3b8; flex-shrink: 0; }
.rank-num.gold { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; }
.rank-num.silver { background: linear-gradient(135deg, #94a3b8, #64748b); color: white; }
.rank-num.bronze { background: linear-gradient(135deg, #d97706, #92400e); color: white; }
.rank-poster { width: 90rpx; height: 126rpx; border-radius: 12rpx; flex-shrink: 0; background: #334155; }
.rank-info { flex: 1; min-width: 0; }
.rank-title { font-size: 28rpx; font-weight: 700; color: #f1f5f9; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-sub { font-size: 22rpx; color: #94a3b8; margin-top: 6rpx; display: block; }
.rank-right { flex-shrink: 0; text-align: right; }
.rank-score { font-size: 32rpx; font-weight: 800; color: #fbbf24; display: block; }
.rank-hot { font-size: 22rpx; color: #94a3b8; }

.empty { text-align: center; padding: 100rpx; color: #64748b; font-size: 28rpx; }
</style>
