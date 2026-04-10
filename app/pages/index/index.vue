<template>
  <view class="page-bg">
    <!-- 自定义导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <text class="nav-title">🎬 Drama Tracker</text>
    </view>

    <!-- 搜索框 -->
    <view class="search-box">
      <input class="search-input" placeholder="搜索剧集..." confirm-type="search" @confirm="onSearch" v-model="keyword" />
    </view>

    <!-- 地区快捷入口 -->
    <scroll-view scroll-x class="region-scroll">
      <view class="region-item" v-for="r in regionList" :key="r.code"
        @tap="goRegion(r.code)">
        <text>{{ r.emoji }} {{ r.label }}</text>
      </view>
    </scroll-view>

    <!-- 今日更新 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">📺 今日更新</text>
        <text class="section-more" @tap="goTab(1)">更多 ></text>
      </view>
      <scroll-view scroll-x class="drama-scroll">
        <view class="drama-card" v-for="d in todayList" :key="d.id" @tap="goDetail(d.id)">
          <image class="card-poster" :src="d.posterUrl || defaultPoster" mode="aspectFill" />
          <text class="card-title">{{ d.title }}</text>
          <view class="card-meta">
            <text class="card-rating" v-if="d.userRating">⭐ {{ Number(d.userRating).toFixed(1) }}</text>
            <text class="card-region">{{ getRegionLabel(d.region) }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 热门剧集 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">🔥 热门剧集</text>
      </view>
      <view class="hot-list">
        <view class="hot-item" v-for="(d, i) in hotList" :key="d.dramaId || d.id" @tap="goDetail(d.dramaId || d.id)">
          <text class="hot-rank" :class="{ top3: i < 3 }">{{ i + 1 }}</text>
          <view class="hot-info">
            <text class="hot-title">{{ d.dramaTitle || d.title }}</text>
            <text class="hot-sub">🔥 {{ formatHot(d.hotScore) }}</text>
          </view>
          <text class="hot-score" v-if="d.score">{{ Number(d.score).toFixed(1) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTodayUpdated } from '@/api/drama'
import { getDailyRanking } from '@/api/ranking'
import { REGIONS } from '@/utils/config'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 44
const keyword = ref('')
const todayList = ref([])
const hotList = ref([])
const defaultPoster = 'https://via.placeholder.com/300x420/1e293b/475569?text=No+Poster'

const regionList = Object.entries(REGIONS).map(([code, info]) => ({ code, ...info }))

function getRegionLabel(code) {
  return REGIONS[code]?.label || '其他'
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/drama-detail/index?id=${id}` })
}

function goRegion(code) {
  uni.switchTab({ url: '/pages/dramas/index' })
}

function goTab(idx) {
  uni.switchTab({ url: '/pages/dramas/index' })
}

function formatHot(val) {
  if (!val) return '0'
  if (val >= 10000) return (val / 10000).toFixed(1) + '万'
  return val
}

function onSearch() {
  if (keyword.value.trim()) {
    uni.navigateTo({ url: `/pages/dramas/index?keyword=${keyword.value.trim()}` })
  }
}

onMounted(async () => {
  try {
    todayList.value = await getTodayUpdated() || []
  } catch {}
  try {
    const data = await getDailyRanking({ page: 1, size: 10 })
    hotList.value = (data?.list || data?.records || data || []).slice(0, 10)
  } catch {}
})
</script>

<style lang="scss">
.nav-bar {
  background: #0f172a;
  padding-bottom: 12rpx;
  text-align: center;
}
.nav-title { font-size: 36rpx; font-weight: 800; color: #f1f5f9; }

.search-box { padding: 16rpx 24rpx; }
.search-input {
  background: #1e293b;
  border-radius: 40rpx;
  padding: 16rpx 28rpx;
  color: #f1f5f9;
  font-size: 28rpx;
}

.region-scroll {
  white-space: nowrap;
  padding: 8rpx 24rpx 20rpx;
}
.region-item {
  display: inline-block;
  padding: 12rpx 24rpx;
  margin-right: 16rpx;
  background: #1e293b;
  border-radius: 40rpx;
  font-size: 26rpx;
  color: #cbd5e1;
}

.section { padding: 0 24rpx 32rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.section-title { font-size: 32rpx; font-weight: 700; color: #f1f5f9; }
.section-more { font-size: 24rpx; color: #6366f1; }

.drama-scroll { white-space: nowrap; }
.drama-card {
  display: inline-block;
  width: 240rpx;
  margin-right: 20rpx;
  vertical-align: top;
}
.card-poster { width: 240rpx; height: 340rpx; border-radius: 16rpx; background: #1e293b; }
.card-title { font-size: 26rpx; color: #f1f5f9; margin-top: 10rpx; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-meta { display: flex; gap: 12rpx; margin-top: 6rpx; }
.card-rating { font-size: 22rpx; color: #fbbf24; }
.card-region { font-size: 22rpx; color: #94a3b8; }

.hot-list { display: flex; flex-direction: column; gap: 16rpx; }
.hot-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #1e293b;
  border-radius: 16rpx;
  padding: 16rpx;
}
.hot-rank {
  width: 48rpx; height: 48rpx; border-radius: 50%;
  background: rgba(148,163,184,0.1);
  text-align: center; line-height: 48rpx;
  font-size: 24rpx; font-weight: 700; color: #94a3b8;
  flex-shrink: 0;
}
.hot-rank.top3 { background: linear-gradient(135deg, #f59e0b, #ef4444); color: white; }
.hot-poster { width: 80rpx; height: 112rpx; border-radius: 10rpx; flex-shrink: 0; background: #334155; }
.hot-info { flex: 1; min-width: 0; }
.hot-title { font-size: 28rpx; color: #f1f5f9; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hot-sub { font-size: 22rpx; color: #94a3b8; margin-top: 6rpx; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hot-score { font-size: 30rpx; font-weight: 700; color: #fbbf24; flex-shrink: 0; }
</style>
