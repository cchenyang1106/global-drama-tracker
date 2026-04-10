<template>
  <div class="drama-list-page page-container fade-in">
    <h1 class="page-title">🎬 剧集库</h1>

    <!-- 筛选栏 -->
    <div class="filters">
      <div class="filter-row">
        <span class="filter-label">地区</span>
        <div class="filter-options">
          <button
            :class="['filter-btn', { active: !filters.region }]"
            @click="filters.region = ''"
          >全部</button>
          <button
            v-for="r in regionList"
            :key="r.code"
            :class="['filter-btn', { active: filters.region === r.code }]"
            @click="filters.region = r.code"
          >{{ r.emoji }} {{ r.label }}</button>
        </div>
      </div>

      <div class="filter-row">
        <span class="filter-label">类型</span>
        <div class="filter-options">
          <button
            :class="['filter-btn', { active: !filters.type }]"
            @click="filters.type = ''"
          >全部</button>
          <button
            v-for="(name, key) in DRAMA_TYPES"
            :key="key"
            :class="['filter-btn', { active: filters.type == key }]"
            @click="filters.type = key"
          >{{ name }}</button>
        </div>
      </div>

      <div class="filter-row">
        <span class="filter-label">状态</span>
        <div class="filter-options">
          <button
            :class="['filter-btn', { active: filters.status === '' }]"
            @click="filters.status = ''"
          >全部</button>
          <button
            v-for="(info, key) in DRAMA_STATUS"
            :key="key"
            :class="['filter-btn', { active: filters.status == key }]"
            @click="filters.status = key"
          >{{ info.label }}</button>
        </div>
      </div>

      <div class="filter-row">
        <span class="filter-label">排序</span>
        <div class="filter-options">
          <button
            v-for="s in sortOptions"
            :key="s.value"
            :class="['filter-btn', { active: filters.sortBy === s.value }]"
            @click="filters.sortBy = s.value"
          >{{ s.label }}</button>
        </div>
      </div>
    </div>

    <!-- 剧集列表 -->
    <div v-if="loading" class="loading-grid">
      <el-skeleton v-for="i in 10" :key="i" :rows="4" animated class="skeleton-card" />
    </div>
    <div v-else class="drama-grid">
      <DramaCard
        v-for="drama in dramaList"
        :key="drama.id"
        :drama="drama"
        :show-progress="true"
      />
    </div>

    <el-empty v-if="!loading && !dramaList.length" description="暂无符合条件的剧集" :image-size="120" />

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="filters.pageNum"
        v-model:page-size="filters.pageSize"
        :total="total"
        :page-sizes="[20, 40, 60]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="loadDramas"
        @size-change="loadDramas"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getDramaList } from '@/api/drama'
import DramaCard from '@/components/DramaCard.vue'
import { REGION_LIST, DRAMA_TYPES, DRAMA_STATUS } from '@/utils/constants'

const route = useRoute()
const regionList = REGION_LIST.filter(r => r.code !== 'OT')

const sortOptions = [
  { label: '最新更新', value: 'updateTime' },
  { label: '评分最高', value: 'userRating' },
  { label: '最热门', value: 'hotScore' },
  { label: '最新上映', value: 'releaseDate' },
]

const filters = reactive({
  pageNum: 1,
  pageSize: 20,
  region: '',
  type: '',
  status: '',
  sortBy: 'updateTime',
  sortOrder: 'desc',
})

const dramaList = ref([])
const total = ref(0)
const loading = ref(true)

async function loadDramas() {
  loading.value = true
  try {
    const params = { ...filters }
    // 清除空值
    Object.keys(params).forEach(k => {
      if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k]
    })
    const data = await getDramaList(params)
    dramaList.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    dramaList.value = []
    total.value = 0
  }
  loading.value = false
}

// 监听筛选条件变化
watch(
  () => [filters.region, filters.type, filters.status, filters.sortBy],
  () => {
    filters.pageNum = 1
    loadDramas()
  }
)

onMounted(() => {
  // 从 URL 获取预设筛选
  if (route.query.region) filters.region = route.query.region
  if (route.query.type) filters.type = route.query.type
  loadDramas()
})
</script>

<style scoped>
.page-title {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 24px;
}

.filters {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  margin-bottom: 28px;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(51, 65, 85, 0.4);
}

.filter-row:last-child {
  border-bottom: none;
}

.filter-label {
  flex-shrink: 0;
  width: 48px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  padding-top: 6px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-btn {
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  background: none;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  color: var(--text-primary);
  background: rgba(99, 102, 241, 0.08);
}

.filter-btn.active {
  color: var(--primary-light);
  background: rgba(99, 102, 241, 0.12);
  border-color: var(--primary);
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

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 20px;
}
</style>
