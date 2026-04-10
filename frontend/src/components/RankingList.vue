<template>
  <div class="ranking-list">
    <div
      v-for="(item, index) in list"
      :key="item.id || index"
      class="rank-item"
      @click="goDetail(item)"
    >
      <div class="rank-num" :class="{ 'top-3': index < 3 }">
        {{ index + 1 }}
      </div>
      <div class="rank-info">
        <h4 class="rank-title">{{ item.dramaTitle || item.title || `剧集 #${item.dramaId}` }}</h4>
        <div class="rank-meta">
          <span v-if="item.score" class="score">
            <el-icon><StarFilled /></el-icon>
            {{ Number(item.score).toFixed(1) }}
          </span>
          <span v-if="item.hotScore" class="hot">
            🔥 {{ formatHot(item.hotScore) }}
          </span>
        </div>
      </div>
      <div class="rank-change" v-if="item.rankChange !== null && item.rankChange !== undefined">
        <span v-if="item.rankChange > 0" class="change up">▲ {{ item.rankChange }}</span>
        <span v-else-if="item.rankChange < 0" class="change down">▼ {{ Math.abs(item.rankChange) }}</span>
        <span v-else class="change flat">—</span>
      </div>
    </div>
    <el-empty v-if="!list?.length" description="暂无排行数据" :image-size="80" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  list: { type: Array, default: () => [] },
})

const router = useRouter()

function goDetail(item) {
  if (item.dramaId) {
    router.push(`/drama/${item.dramaId}`)
  }
}

function formatHot(val) {
  if (val >= 100000000) return (val / 100000000).toFixed(1) + '亿'
  if (val >= 10000) return (val / 10000).toFixed(1) + '万'
  return val
}
</script>

<style scoped>
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s;
}

.rank-item:hover {
  background: var(--bg-card-hover);
}

.rank-num {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 800;
  color: var(--text-muted);
  background: var(--bg-card);
  flex-shrink: 0;
}

.rank-num.top-3 {
  color: white;
  background: var(--gradient-primary);
}

.rank-item:nth-child(1) .rank-num.top-3 {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
}
.rank-item:nth-child(2) .rank-num.top-3 {
  background: linear-gradient(135deg, #94a3b8, #64748b);
}
.rank-item:nth-child(3) .rank-num.top-3 {
  background: linear-gradient(135deg, #d97706, #b45309);
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-meta {
  display: flex;
  gap: 12px;
  margin-top: 2px;
}

.score {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #fbbf24;
}

.hot {
  font-size: 12px;
  color: var(--text-muted);
}

.rank-change {
  flex-shrink: 0;
}

.change {
  font-size: 12px;
  font-weight: 600;
}

.change.up { color: var(--accent-green); }
.change.down { color: var(--accent-red); }
.change.flat { color: var(--text-muted); }
</style>
