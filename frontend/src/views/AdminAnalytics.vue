<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1>📈 数据统计</h1>
      <p class="subtitle">实时查看网站 PV / UV 数据</p>
    </div>

    <!-- 概览卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">👀</div>
        <div class="stat-info">
          <span class="stat-num">{{ overview.todayPv }}</span>
          <span class="stat-label">今日 PV</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👤</div>
        <div class="stat-info">
          <span class="stat-num">{{ overview.todayUv }}</span>
          <span class="stat-label">今日 UV</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <span class="stat-num">{{ overview.totalPv }}</span>
          <span class="stat-label">累计 PV</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🌍</div>
        <div class="stat-info">
          <span class="stat-num">{{ overview.totalUv }}</span>
          <span class="stat-label">累计 UV</span>
        </div>
      </div>
    </div>

    <!-- 今日时段分布 -->
    <div class="section">
      <h2 class="section-title"><span class="icon">⏰</span> 今日访问时段分布</h2>
      <div class="hour-chart">
        <div v-for="h in hourlyData" :key="h.hour" class="hour-bar-wrap">
          <div class="hour-bar" :style="{ height: getBarHeight(h.pv) + 'px' }">
            <span class="bar-val" v-if="h.pv > 0">{{ h.pv }}</span>
          </div>
          <span class="hour-label">{{ h.hour }}时</span>
        </div>
      </div>
    </div>

    <!-- 每日趋势 -->
    <div class="section">
      <h2 class="section-title"><span class="icon">📅</span> 近30天 PV/UV 趋势</h2>
      <div class="trend-table">
        <div class="trend-header">
          <span class="col-date">日期</span>
          <span class="col-pv">PV</span>
          <span class="col-uv">UV</span>
          <span class="col-bar">PV 分布</span>
        </div>
        <div v-for="d in dailyData" :key="d.date" class="trend-row">
          <span class="col-date">{{ d.date }}</span>
          <span class="col-pv">{{ d.pv }}</span>
          <span class="col-uv">{{ d.uv }}</span>
          <span class="col-bar">
            <div class="mini-bar" :style="{ width: getDayBarWidth(d.pv) + '%' }"></div>
          </span>
        </div>
        <div v-if="dailyData.length === 0" class="no-data">暂无数据</div>
      </div>
    </div>

    <!-- 页面访问排行 -->
    <div class="section">
      <h2 class="section-title"><span class="icon">🏆</span> 页面访问排行 TOP 20</h2>
      <div class="page-rank">
        <div v-for="(p, i) in pageData" :key="p.path" class="rank-row">
          <span class="rank-num" :class="{ top3: i < 3 }">{{ i + 1 }}</span>
          <span class="rank-path">{{ p.path }}</span>
          <span class="rank-pv">{{ p.pv }} PV</span>
          <span class="rank-uv">{{ p.uv }} UV</span>
        </div>
        <div v-if="pageData.length === 0" class="no-data">暂无数据</div>
      </div>
    </div>

    <!-- 刷新按钮 -->
    <div style="text-align:center;margin-top:24px">
      <el-button type="primary" :loading="loading" @click="loadAll">🔄 刷新数据</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOverview, getDailyStats, getPageStats } from '@/api/analytics'

const loading = ref(false)
const overview = reactive({ todayPv: 0, todayUv: 0, totalPv: 0, totalUv: 0 })
const hourlyData = ref([])
const dailyData = ref([])
const pageData = ref([])
const maxHourPv = ref(1)
const maxDayPv = ref(1)

function getBarHeight(pv) {
  return Math.max(4, (pv / maxHourPv.value) * 120)
}

function getDayBarWidth(pv) {
  return Math.max(2, (pv / maxDayPv.value) * 100)
}

async function loadAll() {
  loading.value = true
  try {
    const ov = await getOverview()
    overview.todayPv = ov.today?.totalPv || 0
    overview.todayUv = ov.today?.totalUv || 0
    overview.totalPv = ov.allTime?.totalPv || 0
    overview.totalUv = ov.allTime?.totalUv || 0

    // 时段数据 (填充 0-23 小时)
    const hourMap = {}
    ;(ov.hourly || []).forEach(h => { hourMap[h.hour] = h.pv })
    const hours = []
    for (let i = 0; i < 24; i++) {
      hours.push({ hour: i, pv: hourMap[i] || 0 })
    }
    hourlyData.value = hours
    maxHourPv.value = Math.max(1, ...hours.map(h => h.pv))

    const daily = await getDailyStats(30)
    dailyData.value = daily || []
    maxDayPv.value = Math.max(1, ...dailyData.value.map(d => d.pv || 0))

    const pages = await getPageStats(30)
    pageData.value = pages || []
  } catch { /* ignore */ }
  loading.value = false
}

onMounted(loadAll)
</script>

<style scoped>
.admin-page { max-width: 960px; margin: 0 auto; padding: 32px 28px 80px; }
.admin-header { text-align: center; margin-bottom: 32px; }
.admin-header h1 { font-size: 1.8rem; background: linear-gradient(135deg, #60a5fa, #a78bfa); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 6px; }
.subtitle { color: #94a3b8; }

/* 概览卡片 */
.stats-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 36px; }
.stat-card {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon { font-size: 32px; }
.stat-info { display: flex; flex-direction: column; }
.stat-num { font-size: 28px; font-weight: 800; color: #e2e8f0; }
.stat-label { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* 时段图 */
.section { margin-bottom: 36px; }
.section-title { display: flex; align-items: center; gap: 8px; font-size: 1.2rem; color: #e2e8f0; margin-bottom: 16px; }
.section-title .icon { font-size: 1.3rem; }

.hour-chart {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  background: rgba(30, 41, 59, 0.6);
  border-radius: 12px;
  padding: 20px 16px 12px;
  height: 180px;
}
.hour-bar-wrap { flex: 1; display: flex; flex-direction: column; align-items: center; }
.hour-bar {
  width: 100%;
  max-width: 28px;
  background: linear-gradient(to top, #3b82f6, #60a5fa);
  border-radius: 4px 4px 0 0;
  position: relative;
  min-height: 4px;
  transition: height 0.5s ease;
}
.bar-val { position: absolute; top: -18px; left: 50%; transform: translateX(-50%); font-size: 10px; color: #94a3b8; white-space: nowrap; }
.hour-label { font-size: 10px; color: #64748b; margin-top: 6px; }

/* 每日趋势 */
.trend-table {
  background: rgba(30, 41, 59, 0.6);
  border-radius: 12px;
  padding: 4px 0;
  max-height: 400px;
  overflow-y: auto;
}
.trend-header, .trend-row { display: flex; padding: 10px 20px; align-items: center; }
.trend-header { color: #94a3b8; font-size: 12px; font-weight: 600; border-bottom: 1px solid rgba(148, 163, 184, 0.1); position: sticky; top: 0; background: rgba(30, 41, 59, 0.95); }
.trend-row { font-size: 13px; color: #cbd5e1; border-bottom: 1px solid rgba(148, 163, 184, 0.05); }
.trend-row:hover { background: rgba(99, 102, 241, 0.05); }
.col-date { width: 120px; flex-shrink: 0; }
.col-pv { width: 80px; flex-shrink: 0; font-weight: 600; color: #60a5fa; }
.col-uv { width: 80px; flex-shrink: 0; font-weight: 600; color: #a78bfa; }
.col-bar { flex: 1; }
.mini-bar { height: 8px; background: linear-gradient(to right, #3b82f6, #60a5fa); border-radius: 4px; transition: width 0.5s ease; }

/* 页面排行 */
.page-rank {
  background: rgba(30, 41, 59, 0.6);
  border-radius: 12px;
  padding: 4px 0;
}
.rank-row { display: flex; padding: 10px 20px; align-items: center; gap: 12px; font-size: 13px; color: #cbd5e1; border-bottom: 1px solid rgba(148, 163, 184, 0.05); }
.rank-row:hover { background: rgba(99, 102, 241, 0.05); }
.rank-num { width: 28px; height: 28px; border-radius: 50%; background: rgba(148, 163, 184, 0.1); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; flex-shrink: 0; }
.rank-num.top3 { background: linear-gradient(135deg, #f59e0b, #ef4444); color: white; }
.rank-path { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-pv { color: #60a5fa; font-weight: 600; flex-shrink: 0; }
.rank-uv { color: #a78bfa; font-weight: 600; flex-shrink: 0; }

.no-data { text-align: center; padding: 30px; color: #64748b; }

@media (max-width: 768px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
}
</style>
