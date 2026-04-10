<template>
  <div class="home fade-in">
    <!-- Hero Banner -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="gradient-text">全球剧集</span> 一网打尽
        </h1>
        <p class="hero-desc">追踪全球热门剧集 · 实时更新排行榜 · 发现你的下一部好剧</p>
        <div class="hero-regions">
          <router-link
            v-for="r in regionList"
            :key="r.code"
            :to="`/dramas?region=${r.code}`"
            class="hero-region-btn"
          >
            {{ r.emoji }} {{ r.label }}
          </router-link>
        </div>
      </div>
    </section>

    <div class="page-container">
      <!-- 今日更新 -->
      <section class="section">
        <SectionHeader icon="📺" title="今日更新" subtitle="最新剧集动态" more-link="/dramas" />
        <div v-if="todayLoading" class="loading-grid">
          <el-skeleton v-for="i in 6" :key="i" :rows="4" animated class="skeleton-card" />
        </div>
        <div v-else class="drama-grid">
          <DramaCard
            v-for="drama in todayList"
            :key="drama.id"
            :drama="drama"
            :show-progress="true"
          />
        </div>
        <el-empty v-if="!todayLoading && !todayList.length" description="今日暂无更新" />
      </section>

      <!-- 热门排行 -->
      <section class="section">
        <SectionHeader icon="🔥" title="热门排行" subtitle="实时热度榜" more-link="/ranking" />
        <div class="ranking-panel">
          <div class="ranking-tabs">
            <button
              v-for="tab in rankTabs"
              :key="tab.key"
              :class="['rank-tab', { active: activeRankTab === tab.key }]"
              @click="switchRankTab(tab.key)"
            >
              {{ tab.label }}
            </button>
          </div>
          <div v-if="rankLoading" style="padding: 20px;">
            <el-skeleton :rows="8" animated />
          </div>
          <RankingList v-else :list="currentRankList" />
        </div>
      </section>

      <!-- 各地区热门 -->
      <section class="section">
        <SectionHeader icon="🌏" title="各地区热门" subtitle="全球剧集精选" />
        <div class="region-tabs">
          <button
            v-for="r in regionList"
            :key="r.code"
            :class="['region-tab', { active: activeRegion === r.code }]"
            @click="switchRegion(r.code)"
          >
            {{ r.emoji }} {{ r.label }}
          </button>
        </div>
        <div v-if="regionLoading" class="loading-grid">
          <el-skeleton v-for="i in 5" :key="i" :rows="4" animated class="skeleton-card" />
        </div>
        <div v-else class="drama-grid">
          <DramaCard
            v-for="drama in regionDramas"
            :key="drama.id"
            :drama="drama"
          />
        </div>
        <el-empty v-if="!regionLoading && !regionDramas.length" description="该地区暂无剧集" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTodayUpdated, getDramasByRegion } from '@/api/drama'
import { getDailyRanking, getWeeklyRanking, getMonthlyRanking } from '@/api/ranking'
import DramaCard from '@/components/DramaCard.vue'
import SectionHeader from '@/components/SectionHeader.vue'
import RankingList from '@/components/RankingList.vue'
import { REGION_LIST } from '@/utils/constants'

const regionList = REGION_LIST.filter(r => r.code !== 'OT')

// --- 今日更新 ---
const todayList = ref([])
const todayLoading = ref(true)

async function loadToday() {
  todayLoading.value = true
  try {
    const data = await getTodayUpdated()
    todayList.value = (data || []).slice(0, 12)
  } catch { todayList.value = [] }
  todayLoading.value = false
}

// --- 热门排行 ---
const rankTabs = [
  { key: 'daily', label: '日榜' },
  { key: 'weekly', label: '周榜' },
  { key: 'monthly', label: '月榜' },
]
const activeRankTab = ref('daily')
const rankLoading = ref(true)
const rankData = ref({ daily: [], weekly: [], monthly: [] })
const currentRankList = ref([])

async function loadRanking() {
  rankLoading.value = true
  try {
    const [daily, weekly, monthly] = await Promise.allSettled([
      getDailyRanking({ category: 'hot', limit: 10 }),
      getWeeklyRanking({ category: 'hot', limit: 10 }),
      getMonthlyRanking({ category: 'hot', limit: 10 }),
    ])
    rankData.value.daily = daily.status === 'fulfilled' ? (daily.value || []) : []
    rankData.value.weekly = weekly.status === 'fulfilled' ? (weekly.value || []) : []
    rankData.value.monthly = monthly.status === 'fulfilled' ? (monthly.value || []) : []
    currentRankList.value = rankData.value.daily
  } catch { /* ignore */ }
  rankLoading.value = false
}

function switchRankTab(key) {
  activeRankTab.value = key
  currentRankList.value = rankData.value[key] || []
}

// --- 各地区 ---
const activeRegion = ref('CN')
const regionDramas = ref([])
const regionLoading = ref(true)

async function switchRegion(code) {
  activeRegion.value = code
  regionLoading.value = true
  try {
    const data = await getDramasByRegion(code, 1, 10)
    regionDramas.value = data?.list || data || []
  } catch { regionDramas.value = [] }
  regionLoading.value = false
}

onMounted(() => {
  loadToday()
  loadRanking()
  switchRegion('CN')
})
</script>

<style scoped>
.hero {
  position: relative;
  height: 340px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1e1b4b 0%, #0f172a 50%, #1e293b 100%);
}

.hero-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 600px 300px at 20% 50%, rgba(99, 102, 241, 0.15), transparent),
    radial-gradient(ellipse 500px 250px at 80% 40%, rgba(168, 85, 247, 0.1), transparent);
}

.hero-content {
  position: relative;
  text-align: center;
  z-index: 1;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 12px;
}

.gradient-text {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 28px;
}

.hero-regions {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.hero-region-btn {
  padding: 8px 20px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  font-size: 14px;
  transition: all 0.25s;
  cursor: pointer;
}

.hero-region-btn:hover {
  background: rgba(99, 102, 241, 0.15);
  border-color: var(--primary);
  color: var(--primary-light);
  transform: translateY(-2px);
}

.section {
  margin-bottom: 48px;
}

.drama-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 16px;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 16px;
}

.skeleton-card {
  border-radius: var(--radius-md);
  overflow: hidden;
}

/* 排行榜面板 */
.ranking-panel {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.ranking-tabs {
  display: flex;
  border-bottom: 1px solid var(--border-color);
}

.rank-tab {
  flex: 1;
  padding: 14px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.rank-tab:hover { color: var(--text-primary); }
.rank-tab.active {
  color: var(--primary-light);
  border-bottom-color: var(--primary);
}

/* 地区选项卡 */
.region-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.region-tab {
  padding: 8px 18px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.region-tab:hover {
  color: var(--text-primary);
  border-color: var(--primary);
}

.region-tab.active {
  color: white;
  background: var(--gradient-primary);
  border-color: transparent;
}
</style>
