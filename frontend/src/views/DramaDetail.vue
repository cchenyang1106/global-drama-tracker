<template>
  <div class="detail-page fade-in" v-if="drama">
    <!-- 顶部背景 -->
    <div class="detail-hero">
      <div class="hero-backdrop" :style="{ backgroundImage: `url(${drama.coverUrl || drama.posterUrl})` }"></div>
      <div class="hero-overlay"></div>
      <div class="hero-body page-container">
        <div class="hero-poster">
          <img :src="drama.posterUrl || DEFAULT_POSTER" :alt="drama.title" @error="onImgError" />
        </div>
        <div class="hero-info">
          <div class="info-tags">
            <span class="region-tag" :class="`region-tag-${drama.region}`">
              {{ getRegionInfo(drama.region).emoji }} {{ getRegionInfo(drama.region).label }}
            </span>
            <el-tag :type="getStatusInfo(drama.status).type" size="small" round>
              {{ getStatusInfo(drama.status).label }}
            </el-tag>
            <el-tag v-if="drama.type" size="small" round type="info">
              {{ getTypeName(drama.type) }}
            </el-tag>
          </div>
          <h1 class="info-title">{{ drama.title }}</h1>
          <p v-if="drama.originalTitle" class="info-original">{{ drama.originalTitle }}</p>

          <div class="info-stats">
            <div v-if="drama.userRating" class="stat-item rating-item">
              <span class="stat-value">{{ Number(drama.userRating).toFixed(1) }}</span>
              <span class="stat-label">用户评分</span>
              <span class="stat-sub">{{ drama.ratingCount || 0 }} 人评</span>
            </div>
            <div v-if="drama.hotScore" class="stat-item">
              <span class="stat-value">🔥 {{ formatHot(drama.hotScore) }}</span>
              <span class="stat-label">热度</span>
            </div>
            <div v-if="drama.totalEpisodes" class="stat-item">
              <span class="stat-value">{{ drama.airedEpisodes || 0 }} / {{ drama.totalEpisodes }}</span>
              <span class="stat-label">集数</span>
            </div>
            <div v-if="drama.episodeDuration" class="stat-item">
              <span class="stat-value">{{ drama.episodeDuration }}分钟</span>
              <span class="stat-label">单集时长</span>
            </div>
          </div>

          <div class="info-meta-list">
            <div v-if="drama.directors" class="meta-row">
              <span class="meta-key">导演</span>
              <span class="meta-val">{{ drama.directors }}</span>
            </div>
            <div v-if="drama.writers" class="meta-row">
              <span class="meta-key">编剧</span>
              <span class="meta-val">{{ drama.writers }}</span>
            </div>
            <div v-if="drama.actors" class="meta-row">
              <span class="meta-key">主演</span>
              <span class="meta-val">{{ drama.actors }}</span>
            </div>
            <div v-if="drama.genres" class="meta-row">
              <span class="meta-key">类型</span>
              <span class="meta-val">
                <el-tag v-for="g in drama.genres.split(',')" :key="g" size="small" class="genre-tag">{{ g.trim() }}</el-tag>
              </span>
            </div>
            <div v-if="drama.platforms" class="meta-row">
              <span class="meta-key">平台</span>
              <span class="meta-val">{{ drama.platforms }}</span>
            </div>
            <div v-if="drama.releaseDate" class="meta-row">
              <span class="meta-key">首播</span>
              <span class="meta-val">{{ drama.releaseDate }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 简介 -->
    <div class="page-container">
      <section v-if="drama.description" class="section">
        <h2 class="section-title">📖 剧情简介</h2>
        <p class="description">{{ drama.description }}</p>
      </section>

      <!-- 外部链接 -->
      <section v-if="drama.doubanId || drama.imdbId || drama.officialUrl" class="section">
        <h2 class="section-title">🔗 相关链接</h2>
        <div class="external-links">
          <a v-if="drama.doubanId" :href="`https://movie.douban.com/subject/${drama.doubanId}/`" target="_blank" class="ext-link">
            🟢 豆瓣
          </a>
          <a v-if="drama.imdbId" :href="`https://www.imdb.com/title/${drama.imdbId}/`" target="_blank" class="ext-link">
            🟡 IMDb
          </a>
          <a v-if="drama.officialUrl" :href="drama.officialUrl" target="_blank" class="ext-link">
            🌐 官方网站
          </a>
        </div>
      </section>
    </div>
  </div>

  <!-- 加载中 -->
  <div v-else-if="loading" class="page-container">
    <el-skeleton :rows="10" animated />
  </div>

  <!-- 未找到 -->
  <div v-else class="page-container">
    <el-empty description="剧集未找到" :image-size="150">
      <el-button type="primary" @click="$router.push('/dramas')">返回剧集库</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getDramaDetail } from '@/api/drama'
import { DEFAULT_POSTER, getRegionInfo, getTypeName, getStatusInfo } from '@/utils/constants'

const route = useRoute()
const drama = ref(null)
const loading = ref(true)

function onImgError(e) {
  e.target.src = DEFAULT_POSTER
}

function formatHot(val) {
  if (val >= 100000000) return (val / 100000000).toFixed(1) + '亿'
  if (val >= 10000) return (val / 10000).toFixed(1) + '万'
  return val
}

onMounted(async () => {
  loading.value = true
  try {
    drama.value = await getDramaDetail(route.params.id)
  } catch { drama.value = null }
  loading.value = false
})
</script>

<style scoped>
.detail-hero {
  position: relative;
  min-height: 420px;
}

.hero-backdrop {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: blur(30px) brightness(0.3);
  transform: scale(1.1);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(15, 23, 42, 0.6), var(--bg-dark));
}

.hero-body {
  position: relative;
  display: flex;
  gap: 36px;
  padding-top: 40px;
  padding-bottom: 40px;
}

.hero-poster {
  flex-shrink: 0;
  width: 220px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.hero-poster img {
  width: 100%;
  aspect-ratio: 2 / 3;
  object-fit: cover;
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.info-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.info-title {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 4px;
}

.info-original {
  font-size: 15px;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.info-stats {
  display: flex;
  gap: 28px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.rating-item .stat-value {
  color: #fbbf24;
  font-size: 28px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

.stat-sub {
  font-size: 11px;
  color: var(--text-muted);
}

.info-meta-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.meta-key {
  flex-shrink: 0;
  width: 48px;
  color: var(--text-muted);
  font-weight: 500;
}

.meta-val {
  color: var(--text-secondary);
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  align-items: center;
}

.genre-tag {
  background: rgba(99, 102, 241, 0.12) !important;
  color: var(--primary-light) !important;
  border: none !important;
}

.section {
  margin-bottom: 36px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 14px;
}

.description {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
}

.external-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.ext-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  background: var(--bg-card);
  color: var(--text-secondary);
  font-size: 14px;
  transition: all 0.2s;
}

.ext-link:hover {
  background: var(--bg-card-hover);
  color: var(--text-primary);
  transform: translateY(-2px);
}
</style>
