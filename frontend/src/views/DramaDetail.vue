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

      <!-- 评论讨论区 -->
      <section class="section">
        <h2 class="section-title">💬 评论讨论区</h2>

        <!-- 发表评论表单 -->
        <div class="comment-form">
          <div class="form-row">
            <el-input v-model="commentForm.nickname" placeholder="你的昵称" size="default" style="width: 200px" />
            <div class="rating-input">
              <span class="rating-label">我的评分：</span>
              <el-rate v-model="commentForm.rating" :max="10" :colors="['#ff9900', '#ff9900', '#ff0000']" allow-half show-score />
            </div>
          </div>
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="3"
            placeholder="写下你对这部剧的看法..."
            maxlength="500"
            show-word-limit
          />
          <div class="form-actions">
            <el-checkbox v-model="commentForm.spoiler">包含剧透</el-checkbox>
            <el-button type="primary" :loading="submitting" @click="submitComment" :disabled="!commentForm.content.trim()">
              发表评论
            </el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-if="comments.length === 0" class="no-comments">
            暂无评论，来发表第一条评论吧！
          </div>
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">{{ c.nickname || '匿名用户' }}</span>
              <el-tag v-if="c.rating" type="warning" size="small" round>{{ Number(c.rating).toFixed(1) }} 分</el-tag>
              <el-tag v-if="c.spoiler" type="danger" size="small" round>剧透</el-tag>
              <span class="comment-time">{{ formatTime(c.createTime) }}</span>
            </div>
            <p class="comment-content" :class="{ 'spoiler-blur': c.spoiler && !c._showSpoiler }">
              {{ c.content }}
            </p>
            <span v-if="c.spoiler && !c._showSpoiler" class="spoiler-tip" @click="c._showSpoiler = true">点击查看剧透内容</span>
            <div class="comment-footer">
              <el-button text size="small" @click="handleLike(c)">
                👍 {{ c.likeCount || 0 }}
              </el-button>
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="hasMore" class="load-more">
            <el-button text @click="loadMoreComments">加载更多评论...</el-button>
          </div>
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDramaDetail } from '@/api/drama'
import { getComments, addComment, likeComment } from '@/api/comment'
import { DEFAULT_POSTER, getRegionInfo, getTypeName, getStatusInfo } from '@/utils/constants'

const route = useRoute()
const drama = ref(null)
const loading = ref(true)

// 评论相关
const comments = ref([])
const commentPage = ref(1)
const hasMore = ref(false)
const submitting = ref(false)
const commentForm = reactive({
  nickname: '',
  content: '',
  rating: 0,
  spoiler: false,
})

function onImgError(e) {
  e.target.src = DEFAULT_POSTER
}

function formatHot(val) {
  if (val >= 100000000) return (val / 100000000).toFixed(1) + '亿'
  if (val >= 10000) return (val / 10000).toFixed(1) + '万'
  return val
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function loadComments() {
  try {
    const data = await getComments(route.params.id, commentPage.value, 10)
    const newComments = (data.records || data.list || []).map(c => ({ ...c, _showSpoiler: false }))
    if (commentPage.value === 1) {
      comments.value = newComments
    } else {
      comments.value.push(...newComments)
    }
    hasMore.value = comments.value.length < (data.total || 0)
  } catch {
    // ignore
  }
}

function loadMoreComments() {
  commentPage.value++
  loadComments()
}

async function submitComment() {
  if (!commentForm.content.trim()) return
  submitting.value = true
  try {
    await addComment({
      dramaId: Number(route.params.id),
      nickname: commentForm.nickname || '匿名用户',
      content: commentForm.content,
      rating: commentForm.rating > 0 ? commentForm.rating : null,
      spoiler: commentForm.spoiler,
    })
    ElMessage.success('评论发表成功！')
    commentForm.content = ''
    commentForm.rating = 0
    commentForm.spoiler = false
    commentPage.value = 1
    await loadComments()
  } catch {
    ElMessage.error('评论发表失败')
  }
  submitting.value = false
}

async function handleLike(comment) {
  try {
    await likeComment(comment.id)
    comment.likeCount = (comment.likeCount || 0) + 1
  } catch {
    // ignore
  }
}

onMounted(async () => {
  loading.value = true
  try {
    drama.value = await getDramaDetail(route.params.id)
  } catch { drama.value = null }
  loading.value = false
  loadComments()
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

/* 评论区样式 */
.comment-form {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-row {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.rating-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-label {
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.no-comments {
  text-align: center;
  color: var(--text-muted);
  padding: 40px 0;
  font-size: 14px;
}

.comment-item {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 16px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
  color: var(--primary-light);
}

.comment-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: auto;
}

.comment-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.spoiler-blur {
  filter: blur(6px);
  user-select: none;
  cursor: pointer;
}

.spoiler-tip {
  font-size: 12px;
  color: var(--primary-light);
  cursor: pointer;
}

.spoiler-tip:hover {
  text-decoration: underline;
}

.comment-footer {
  display: flex;
  gap: 12px;
}

.load-more {
  text-align: center;
  padding: 12px 0;
}
</style>
