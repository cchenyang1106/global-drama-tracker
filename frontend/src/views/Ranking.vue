<template>
  <div class="ranking-page page-container fade-in">
    <h1 class="page-title">🏆 排行榜</h1>

    <!-- 排行类型切换 -->
    <div class="type-tabs">
      <button
        v-for="t in typeTabs"
        :key="t.key"
        :class="['type-tab', { active: activeType === t.key }]"
        @click="switchType(t.key)"
      >
        <span class="tab-icon">{{ t.icon }}</span>
        {{ t.label }}
      </button>
    </div>

    <!-- 分类筛选 -->
    <div class="category-tabs">
      <button
        v-for="c in categoryTabs"
        :key="c.key"
        :class="['cat-tab', { active: activeCategory === c.key }]"
        @click="switchCategory(c.key)"
      >{{ c.label }}</button>
    </div>

    <!-- 地区子筛选 -->
    <div v-if="activeCategory === 'region'" class="sub-filter">
      <button
        v-for="r in regionList"
        :key="r.code"
        :class="['sub-btn', { active: activeRegion === r.code }]"
        @click="switchRegion(r.code)"
      >{{ r.emoji }} {{ r.label }}</button>
    </div>

    <!-- 排行列表 -->
    <div class="ranking-panel">
      <div v-if="loading" style="padding: 20px;">
        <el-skeleton :rows="12" animated />
      </div>
      <RankingList v-else :list="rankList" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getDailyRanking,
  getWeeklyRanking,
  getMonthlyRanking,
  getNewDramaRanking,
  getRegionRanking,
} from '@/api/ranking'
import RankingList from '@/components/RankingList.vue'
import { REGION_LIST } from '@/utils/constants'

const regionList = REGION_LIST.filter(r => r.code !== 'OT')

const typeTabs = [
  { key: 'daily', label: '日榜', icon: '📅' },
  { key: 'weekly', label: '周榜', icon: '📆' },
  { key: 'monthly', label: '月榜', icon: '🗓️' },
  { key: 'new', label: '新剧', icon: '🆕' },
]

const categoryTabs = [
  { key: 'hot', label: '热门总榜' },
  { key: 'region', label: '按地区' },
]

const activeType = ref('daily')
const activeCategory = ref('hot')
const activeRegion = ref('CN')
const loading = ref(true)
const rankList = ref([])

const fetchMap = {
  daily: getDailyRanking,
  weekly: getWeeklyRanking,
  monthly: getMonthlyRanking,
}

async function loadRanking() {
  loading.value = true
  try {
    if (activeType.value === 'new') {
      rankList.value = (await getNewDramaRanking(30, 50)) || []
    } else if (activeCategory.value === 'region') {
      const typeMap = { daily: 1, weekly: 2, monthly: 3 }
      rankList.value = (await getRegionRanking(activeRegion.value, typeMap[activeType.value], 50)) || []
    } else {
      const fn = fetchMap[activeType.value]
      rankList.value = (await fn({ category: activeCategory.value, limit: 50 })) || []
    }
  } catch {
    rankList.value = []
  }
  loading.value = false
}

function switchType(key) {
  activeType.value = key
  if (key === 'new') activeCategory.value = 'hot'
  loadRanking()
}

function switchCategory(key) {
  activeCategory.value = key
  loadRanking()
}

function switchRegion(code) {
  activeRegion.value = code
  loadRanking()
}

onMounted(() => loadRanking())
</script>

<style scoped>
.page-title {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 24px;
}

.type-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.type-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.type-tab:hover {
  border-color: var(--primary);
  color: var(--text-primary);
}

.type-tab.active {
  color: white;
  background: var(--gradient-primary);
  border-color: transparent;
}

.tab-icon {
  font-size: 16px;
}

.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.cat-tab {
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-muted);
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.cat-tab:hover { color: var(--text-primary); }
.cat-tab.active {
  color: var(--primary-light);
  background: rgba(99, 102, 241, 0.1);
}

.sub-filter {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.sub-btn {
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.sub-btn:hover { border-color: var(--primary); }
.sub-btn.active {
  color: white;
  background: var(--primary);
  border-color: var(--primary);
}

.ranking-panel {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  min-height: 300px;
}
</style>
