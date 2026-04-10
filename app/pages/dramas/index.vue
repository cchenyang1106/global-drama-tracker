<template>
  <view class="page-bg">
    <!-- 搜索 -->
    <view class="search-bar">
      <input class="search-input" placeholder="搜索剧集..." v-model="keyword" confirm-type="search" @confirm="loadData(true)" />
    </view>

    <!-- 地区筛选 -->
    <scroll-view scroll-x class="filter-scroll">
      <view class="filter-item" :class="{ active: !selectedRegion }" @tap="selectRegion('')">全部</view>
      <view class="filter-item" :class="{ active: selectedRegion === r.code }" v-for="r in regionList" :key="r.code" @tap="selectRegion(r.code)">
        {{ r.emoji }} {{ r.label }}
      </view>
    </scroll-view>

    <!-- 剧集列表 -->
    <view class="drama-list">
      <view class="list-item" v-for="d in list" :key="d.id" @tap="goDetail(d.id)">
        <image class="item-poster" :src="d.posterUrl || defaultPoster" mode="aspectFill" />
        <view class="item-info">
          <text class="item-title">{{ d.title }}</text>
          <text class="item-sub">{{ d.originalTitle || '' }}</text>
          <view class="item-tags">
            <text class="tag region-tag">{{ getRegionLabel(d.region) }}</text>
            <text class="tag" v-if="d.genres" v-for="g in d.genres.split(',').slice(0,2)" :key="g">{{ g.trim() }}</text>
          </view>
          <view class="item-bottom">
            <text class="item-rating" v-if="d.userRating">⭐ {{ Number(d.userRating).toFixed(1) }}</text>
            <text class="item-status" :style="{ color: getStatusColor(d.status) }">{{ getStatusLabel(d.status) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore" @tap="loadMore">
      <text>{{ loading ? '加载中...' : '加载更多' }}</text>
    </view>
    <view class="load-more" v-if="!hasMore && list.length > 0">
      <text>没有更多了</text>
    </view>
    <view class="empty" v-if="!loading && list.length === 0">
      <text>暂无剧集</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDramaList, searchDramas } from '@/api/drama'
import { REGIONS, STATUS_MAP } from '@/utils/config'

const defaultPoster = 'https://via.placeholder.com/300x420/1e293b/475569?text=No+Poster'
const regionList = Object.entries(REGIONS).map(([code, info]) => ({ code, ...info }))
const list = ref([])
const keyword = ref('')
const selectedRegion = ref('')
const page = ref(1)
const hasMore = ref(true)
const loading = ref(false)

function getRegionLabel(code) { return REGIONS[code]?.label || '其他' }
function getStatusLabel(s) { return STATUS_MAP[s]?.label || '未知' }
function getStatusColor(s) { return STATUS_MAP[s]?.color || '#94a3b8' }

function goDetail(id) { uni.navigateTo({ url: `/pages/drama-detail/index?id=${id}` }) }

function selectRegion(code) {
  selectedRegion.value = code
  loadData(true)
}

async function loadData(reset = false) {
  if (reset) { page.value = 1; list.value = []; hasMore.value = true }
  if (loading.value) return
  loading.value = true
  try {
    let data
    if (keyword.value.trim()) {
      data = await searchDramas(keyword.value.trim(), page.value, 20)
    } else {
      data = await getDramaList({ page: page.value, size: 20, region: selectedRegion.value || undefined })
    }
    const newList = data?.list || data?.records || []
    if (reset) { list.value = newList } else { list.value.push(...newList) }
    hasMore.value = list.value.length < (data?.total || 0)
  } catch {}
  loading.value = false
}

function loadMore() { page.value++; loadData() }

onMounted(() => loadData(true))
</script>

<style lang="scss">
.search-bar { padding: 16rpx 24rpx; }
.search-input { background: #1e293b; border-radius: 40rpx; padding: 16rpx 28rpx; color: #f1f5f9; font-size: 28rpx; }

.filter-scroll { white-space: nowrap; padding: 0 24rpx 20rpx; }
.filter-item {
  display: inline-block; padding: 10rpx 24rpx; margin-right: 12rpx;
  background: #1e293b; border-radius: 40rpx; font-size: 24rpx; color: #94a3b8;
}
.filter-item.active { background: #6366f1; color: white; }

.drama-list { padding: 0 24rpx; }
.list-item {
  display: flex; gap: 20rpx; padding: 20rpx;
  background: #1e293b; border-radius: 16rpx; margin-bottom: 16rpx;
}
.item-poster { width: 160rpx; height: 224rpx; border-radius: 12rpx; flex-shrink: 0; background: #334155; }
.item-info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: space-between; }
.item-title { font-size: 30rpx; font-weight: 700; color: #f1f5f9; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-sub { font-size: 24rpx; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4rpx; }
.item-tags { display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 10rpx; }
.tag { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 20rpx; background: rgba(99,102,241,0.12); color: #818cf8; }
.region-tag { background: rgba(239,68,68,0.12); color: #f87171; }
.item-bottom { display: flex; justify-content: space-between; margin-top: auto; }
.item-rating { font-size: 26rpx; color: #fbbf24; font-weight: 600; }
.item-status { font-size: 22rpx; }

.load-more { text-align: center; padding: 30rpx; color: #64748b; font-size: 26rpx; }
.empty { text-align: center; padding: 100rpx; color: #64748b; font-size: 28rpx; }
</style>
