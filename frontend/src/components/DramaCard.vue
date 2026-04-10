<template>
  <router-link :to="`/drama/${drama.id}`" class="drama-card hover-lift">
    <div class="poster-wrapper">
      <img
        :src="drama.posterUrl || DEFAULT_POSTER"
        :alt="drama.title"
        class="poster"
        loading="lazy"
        @error="onImgError"
      />
      <div class="poster-overlay">
        <span v-if="drama.status === 1" class="badge badge-airing">🔴 连载中</span>
        <span v-else-if="drama.status === 2" class="badge badge-completed">✅ 已完结</span>
        <span v-else-if="drama.status === 0" class="badge badge-upcoming">🕐 未播出</span>
      </div>
      <div v-if="drama.userRating" class="rating-badge">
        <el-icon><StarFilled /></el-icon>
        {{ Number(drama.userRating).toFixed(1) }}
      </div>
      <div v-if="rank" class="rank-badge" :class="`rank-${rank}`">
        {{ rank }}
      </div>
    </div>
    <div class="card-info">
      <h3 class="title">{{ drama.title }}</h3>
      <div class="meta">
        <span class="region-tag" :class="`region-tag-${drama.region}`">
          {{ getRegionInfo(drama.region).emoji }} {{ getRegionInfo(drama.region).label }}
        </span>
        <span v-if="drama.type" class="type-tag">{{ getTypeName(drama.type) }}</span>
      </div>
      <p v-if="drama.genres" class="genres">{{ drama.genres }}</p>
      <div v-if="showProgress && drama.status === 1" class="progress-info">
        <el-progress
          :percentage="drama.totalEpisodes ? Math.round((drama.airedEpisodes / drama.totalEpisodes) * 100) : 0"
          :stroke-width="4"
          :show-text="false"
          color="var(--primary)"
        />
        <span class="ep-text">{{ drama.airedEpisodes || 0 }}/{{ drama.totalEpisodes || '?' }} 集</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { DEFAULT_POSTER, getRegionInfo, getTypeName } from '@/utils/constants'

defineProps({
  drama: { type: Object, required: true },
  rank: { type: Number, default: 0 },
  showProgress: { type: Boolean, default: false },
})

function onImgError(e) {
  e.target.src = DEFAULT_POSTER
}
</script>

<style scoped>
.drama-card {
  display: block;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-card);
  cursor: pointer;
}

.poster-wrapper {
  position: relative;
  aspect-ratio: 2 / 3;
  overflow: hidden;
}

.poster {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.drama-card:hover .poster {
  transform: scale(1.05);
}

.poster-overlay {
  position: absolute;
  top: 8px;
  left: 8px;
}

.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  backdrop-filter: blur(8px);
}

.badge-airing {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.badge-completed {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}

.badge-upcoming {
  background: rgba(100, 116, 139, 0.2);
  color: #94a3b8;
}

.rating-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  color: #fbbf24;
  font-size: 13px;
  font-weight: 700;
}

.rank-badge {
  position: absolute;
  top: 0;
  left: 0;
  width: 32px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 800;
  color: white;
  clip-path: polygon(0 0, 100% 0, 100% 70%, 50% 100%, 0 70%);
}

.rank-1 { background: linear-gradient(135deg, #fbbf24, #f59e0b); }
.rank-2 { background: linear-gradient(135deg, #94a3b8, #64748b); }
.rank-3 { background: linear-gradient(135deg, #d97706, #b45309); }
.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) {
  background: rgba(99, 102, 241, 0.8);
}

.card-info {
  padding: 12px;
}

.title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.region-tag,
.type-tag {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.type-tag {
  background: rgba(99, 102, 241, 0.12);
  color: var(--primary-light);
}

.genres {
  font-size: 12px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-info {
  margin-top: 6px;
}

.ep-text {
  font-size: 11px;
  color: var(--text-muted);
}
</style>
