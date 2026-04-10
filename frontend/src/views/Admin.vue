<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1>📊 数据管理中心</h1>
      <p class="subtitle">初始化数据、抓取影视信息、刷新排行榜</p>
    </div>

    <!-- 快速开始 -->
    <div class="section">
      <div class="section-title">
        <span class="icon">🚀</span>
        <h2>快速开始</h2>
      </div>
      <div class="quick-start-card">
        <div class="qs-info">
          <h3>一键初始化种子数据</h3>
          <p>内置 30+ 部精选中日韩美英欧影视作品，包含详细信息（海报、评分、演员、简介等）。<br><strong>无需配置任何 API Key</strong>，点击即可填充数据。</p>
        </div>
        <button class="btn btn-primary btn-large" @click="initSeed" :disabled="loading.seed">
          <span v-if="loading.seed" class="spinner"></span>
          <span v-else>🎬 初始化种子数据</span>
        </button>
      </div>
      <div v-if="seedResult" class="result-card" :class="seedResult.status">
        <p>{{ seedResult.message }}</p>
      </div>
    </div>

    <!-- TMDB 抓取 -->
    <div class="section">
      <div class="section-title">
        <span class="icon">🌐</span>
        <h2>TMDB 数据抓取</h2>
      </div>
      <div class="info-card">
        <p>从 <a href="https://www.themoviedb.org" target="_blank">TMDB</a>（The Movie Database）全球最大开放影视数据库获取数据。</p>
        <div class="steps">
          <div class="step">
            <span class="step-num">1</span>
            <span>访问 <a href="https://www.themoviedb.org/settings/api" target="_blank">TMDB API 申请页面</a> 注册并获取免费 API Key</span>
          </div>
          <div class="step">
            <span class="step-num">2</span>
            <span>在后端 <code>application.yml</code> 中配置 <code>tmdb.api-key</code></span>
          </div>
          <div class="step">
            <span class="step-num">3</span>
            <span>点击下方按钮抓取数据（或设置 <code>tmdb.auto-fetch-enabled: true</code> 启用每日自动抓取）</span>
          </div>
        </div>
      </div>

      <div class="action-grid">
        <div class="action-card">
          <h3>🌍 全量抓取</h3>
          <p>抓取 TMDB 热门/高分/正在播出的剧集和电影</p>
          <button class="btn btn-accent" @click="fetchAll" :disabled="loading.tmdb">
            <span v-if="loading.tmdb" class="spinner"></span>
            <span v-else>开始抓取</span>
          </button>
        </div>

        <div class="action-card" v-for="r in regions" :key="r.code">
          <h3>{{ r.flag }} {{ r.name }}</h3>
          <p>抓取{{ r.name }}地区热门影视</p>
          <button class="btn btn-outline" @click="fetchRegion(r.code)" :disabled="loading[r.code]">
            <span v-if="loading[r.code]" class="spinner"></span>
            <span v-else>抓取</span>
          </button>
        </div>
      </div>

      <div v-if="tmdbResult" class="result-card" :class="tmdbResult.status">
        <p>{{ tmdbResult.message || JSON.stringify(tmdbResult) }}</p>
      </div>
    </div>

    <!-- 排行榜管理 -->
    <div class="section">
      <div class="section-title">
        <span class="icon">🏆</span>
        <h2>排行榜管理</h2>
      </div>
      <div class="action-card inline">
        <div>
          <h3>刷新排行榜</h3>
          <p>根据当前剧集数据重新计算日榜/周榜/月榜/地区榜/新剧榜</p>
        </div>
        <button class="btn btn-warning" @click="refreshRank" :disabled="loading.ranking">
          <span v-if="loading.ranking" class="spinner"></span>
          <span v-else>🔄 刷新排行榜</span>
        </button>
      </div>
      <div v-if="rankResult" class="result-card success">
        <p>{{ rankResult }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { initSeedData, fetchFromTmdb, fetchByRegion, refreshRankings } from '../api/data'

const loading = reactive({
  seed: false,
  tmdb: false,
  ranking: false,
  CN: false, JP: false, KR: false, US: false, UK: false, EU: false
})

const seedResult = ref(null)
const tmdbResult = ref(null)
const rankResult = ref(null)

const regions = [
  { code: 'CN', name: '中国', flag: '🇨🇳' },
  { code: 'KR', name: '韩国', flag: '🇰🇷' },
  { code: 'JP', name: '日本', flag: '🇯🇵' },
  { code: 'US', name: '美国', flag: '🇺🇸' },
  { code: 'UK', name: '英国', flag: '🇬🇧' },
  { code: 'EU', name: '欧洲', flag: '🇪🇺' }
]

const initSeed = async () => {
  loading.seed = true
  seedResult.value = null
  try {
    const res = await initSeedData()
    seedResult.value = res.data || res
  } catch (e) {
    seedResult.value = { status: 'error', message: '初始化失败: ' + (e.message || '网络错误') }
  }
  loading.seed = false
}

const fetchAll = async () => {
  loading.tmdb = true
  tmdbResult.value = null
  try {
    const res = await fetchFromTmdb()
    tmdbResult.value = res.data || res
  } catch (e) {
    tmdbResult.value = { status: 'error', message: '抓取失败: ' + (e.message || '请检查 TMDB API Key 配置') }
  }
  loading.tmdb = false
}

const fetchRegion = async (region) => {
  loading[region] = true
  tmdbResult.value = null
  try {
    const res = await fetchByRegion(region)
    tmdbResult.value = res.data || res
  } catch (e) {
    tmdbResult.value = { status: 'error', message: `抓取 ${region} 失败: ` + (e.message || '请检查 TMDB API Key 配置') }
  }
  loading[region] = false
}

const refreshRank = async () => {
  loading.ranking = true
  rankResult.value = null
  try {
    await refreshRankings()
    rankResult.value = '✅ 排行榜已刷新成功！'
  } catch (e) {
    rankResult.value = '❌ 刷新失败: ' + (e.message || '网络错误')
  }
  loading.ranking = false
}
</script>

<style scoped>
.admin-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 28px 80px;
}

.admin-header {
  text-align: center;
  margin-bottom: 40px;
}

.admin-header h1 {
  font-size: 2rem;
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
}

.subtitle {
  color: #94a3b8;
  font-size: 1rem;
}

.section {
  margin-bottom: 40px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-title .icon {
  font-size: 1.5rem;
}

.section-title h2 {
  font-size: 1.3rem;
  color: #e2e8f0;
}

.quick-start-card {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(139, 92, 246, 0.15));
  border: 1px solid rgba(96, 165, 250, 0.3);
  border-radius: 16px;
  padding: 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.qs-info h3 {
  font-size: 1.2rem;
  color: #e2e8f0;
  margin-bottom: 8px;
}

.qs-info p {
  color: #94a3b8;
  line-height: 1.6;
  font-size: 0.9rem;
}

.info-card {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.info-card p {
  color: #94a3b8;
  margin-bottom: 16px;
}

.info-card a {
  color: #60a5fa;
  text-decoration: underline;
}

.steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #cbd5e1;
  font-size: 0.9rem;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.8rem;
  flex-shrink: 0;
}

.step code {
  background: rgba(96, 165, 250, 0.15);
  color: #60a5fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.85rem;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.action-card {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.action-card.inline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-align: left;
  gap: 20px;
}

.action-card h3 {
  color: #e2e8f0;
  margin-bottom: 8px;
  font-size: 1rem;
}

.action-card p {
  color: #94a3b8;
  font-size: 0.85rem;
  margin-bottom: 12px;
}

.btn {
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.4);
}

.btn-large {
  padding: 14px 32px;
  font-size: 1rem;
}

.btn-accent {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.btn-accent:hover:not(:disabled) {
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
}

.btn-outline {
  background: transparent;
  border: 1px solid rgba(96, 165, 250, 0.4);
  color: #60a5fa;
}

.btn-outline:hover:not(:disabled) {
  background: rgba(96, 165, 250, 0.1);
}

.btn-warning {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
}

.btn-warning:hover:not(:disabled) {
  box-shadow: 0 4px 15px rgba(245, 158, 11, 0.4);
}

.result-card {
  margin-top: 16px;
  padding: 16px 20px;
  border-radius: 10px;
  font-size: 0.9rem;
}

.result-card.success {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
  color: #34d399;
}

.result-card.error {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #f87171;
}

.spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 640px) {
  .quick-start-card {
    flex-direction: column;
    text-align: center;
  }

  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-card.inline {
    flex-direction: column;
    text-align: center;
  }
}
</style>
