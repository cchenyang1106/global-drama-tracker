<template>
  <div class="home-page">
    <section class="hero">
      <h1>趣活组队，一起玩 🎯</h1>
      <p>发布你的活动需求，找到一起参与活动的小伙伴</p>
      <div class="search-row">
        <el-input v-model="keyword" placeholder="搜索活动..." :prefix-icon="Search" clearable
          @keyup.enter="loadActivities()" @clear="loadActivities()" class="search-box" />
        <el-button type="primary" @click="loadActivities()" round>搜索</el-button>
      </div>
      <div class="publish-row">
        <router-link to="/publish" class="publish-btn" v-if="userStore.isLoggedIn">✏️ 发布活动</router-link>
        <router-link to="/login" class="publish-btn" v-else>登录后发布</router-link>
      </div>
    </section>

    <section class="categories">
      <span v-for="cat in categories" :key="cat.value"
        :class="['cat-tag', { active: currentCat === cat.value }]"
        @click="currentCat = cat.value; loadActivities()">
        {{ cat.icon }} {{ cat.label }}
      </span>
    </section>

    <section class="activity-list">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <div v-else-if="activities.length === 0" class="empty-tip">暂无活动，快来发布第一个吧！</div>
      <div v-for="item in activities" :key="item.id" class="activity-card" @click="$router.push(`/activity/${item.id}`)">
        <div class="card-header">
          <div class="author-info">
            <el-avatar :size="36" :src="item.authorAvatar" style="background:#6366f1">
              {{ (item.authorName || '?').charAt(0) }}
            </el-avatar>
            <div>
              <div class="author-name">{{ item.authorName || '匿名' }}</div>
              <div class="author-sub">{{ item.authorCity || '' }}</div>
            </div>
          </div>
          <el-tag size="small" :type="catColor(item.category)" round>{{ item.category }}</el-tag>
        </div>
        <h3 class="card-title">{{ item.title }}</h3>
        <p class="card-desc">{{ item.description }}</p>
        <div class="card-meta">
          <span v-if="item.location">📍 {{ item.location }}</span>
          <span v-if="item.activityTime">🕐 {{ item.activityTime }}</span>
          <span>👥 {{ item.joinedCount || 0 }}/{{ item.maxPeople || 1 }}</span>
          <span>👁 {{ item.viewCount || 0 }}</span>
        </div>
        <div v-if="item.tags" class="card-tags">
          <el-tag v-for="t in item.tags.split(',')" :key="t" size="small" type="info" round>{{ t }}</el-tag>
        </div>
      </div>
    </section>

    <div v-if="hasMore" class="load-more">
      <el-button text @click="page++; loadActivities(true)">加载更多...</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getActivities } from '@/api/activity'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const keyword = ref('')
const currentCat = ref('all')
const activities = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(false)

const categories = [
  { value: 'all', label: '全部', icon: '🔥' },
  { value: '旅游', label: '旅游', icon: '✈️' },
  { value: '运动', label: '运动', icon: '⚽' },
  { value: '美食', label: '美食', icon: '🍔' },
  { value: '电影', label: '电影', icon: '🎬' },
  { value: '学习', label: '学习', icon: '📚' },
  { value: '游戏', label: '游戏', icon: '🎮' },
  { value: '其他', label: '其他', icon: '💡' },
]

function catColor(cat) {
  const map = { '旅游': 'success', '运动': 'warning', '美食': 'danger', '电影': '', '学习': 'info', '游戏': 'primary' }
  return map[cat] || 'info'
}

async function loadActivities(append = false) {
  if (!append) { page.value = 1; activities.value = [] }
  loading.value = !append
  try {
    const data = await getActivities({
      page: page.value, size: 12,
      category: currentCat.value === 'all' ? '' : currentCat.value,
      keyword: keyword.value,
    })
    const records = data?.records || []
    if (append) activities.value.push(...records)
    else activities.value = records
    hasMore.value = activities.value.length < (data?.total || 0)
  } catch { /* ignore */ }
  loading.value = false
}

onMounted(() => loadActivities())
</script>

<style scoped>
.home-page { max-width: 800px; margin: 0 auto; padding: 20px; }
.hero { text-align: center; padding: 40px 0 20px; }
.hero h1 { font-size: 28px; font-weight: 800; background: linear-gradient(135deg, #f472b6, #c084fc); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.hero p { color: var(--text-secondary); margin-top: 8px; }
.search-row { display: flex; gap: 10px; justify-content: center; margin-top: 20px; max-width: 500px; margin-left: auto; margin-right: auto; }
.search-box { flex: 1; }
.publish-row { text-align: center; margin-top: 14px; }
.publish-btn { display: inline-block; padding: 10px 28px; border-radius: 20px; background: linear-gradient(135deg, #f472b6, #c084fc); color: white; font-weight: 600; font-size: 14px; text-decoration: none; transition: all 0.2s; }
.publish-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(244,114,182,0.4); }

.categories { display: flex; gap: 8px; flex-wrap: wrap; margin: 20px 0; justify-content: center; }
.cat-tag { padding: 6px 14px; border-radius: 20px; background: var(--bg-card); color: var(--text-secondary); cursor: pointer; font-size: 13px; transition: all 0.2s; border: 1px solid var(--border-color); }
.cat-tag:hover, .cat-tag.active { background: rgba(244,114,182,0.12); color: #ec4899; border-color: rgba(244,114,182,0.3); }

.activity-card { background: var(--bg-card); border-radius: 16px; padding: 20px; margin-bottom: 16px; cursor: pointer; transition: all 0.2s; border: 1px solid var(--border-color); }
.activity-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(244,114,182,0.12); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.author-info { display: flex; gap: 10px; align-items: center; }
.author-name { font-weight: 600; font-size: 14px; }
.author-sub { font-size: 12px; color: var(--text-muted); }
.card-title { font-size: 17px; font-weight: 700; margin-bottom: 8px; }
.card-desc { color: var(--text-secondary); font-size: 14px; line-height: 1.6; margin-bottom: 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-meta { display: flex; gap: 12px; flex-wrap: wrap; font-size: 13px; color: var(--text-muted); }
.card-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 10px; }
.loading-tip, .empty-tip { text-align: center; padding: 40px; color: var(--text-muted); }
.load-more { text-align: center; padding: 16px; }
</style>
