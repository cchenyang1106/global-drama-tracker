<template>
  <div class="search-page page-container fade-in">
    <h1 class="page-title">
      🔍 搜索: <span class="keyword">"{{ keyword }}"</span>
    </h1>

    <div v-if="loading" class="loading-grid">
      <el-skeleton v-for="i in 8" :key="i" :rows="4" animated class="skeleton-card" />
    </div>

    <div v-else-if="dramaList.length" class="drama-grid">
      <DramaCard
        v-for="drama in dramaList"
        :key="drama.id"
        :drama="drama"
      />
    </div>

    <el-empty v-else :description="'没有找到与「' + keyword + '」相关的剧集'" :image-size="120">
      <el-button type="primary" @click="$router.push('/dramas')">浏览全部剧集</el-button>
    </el-empty>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :total="total"
        :page-size="pageSize"
        layout="prev, pager, next"
        background
        @current-change="doSearch"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchDramas } from '@/api/drama'
import DramaCard from '@/components/DramaCard.vue'

const route = useRoute()
const keyword = ref('')
const dramaList = ref([])
const loading = ref(true)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

async function doSearch() {
  if (!keyword.value) return
  loading.value = true
  try {
    const data = await searchDramas(keyword.value, currentPage.value, pageSize)
    dramaList.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    dramaList.value = []
    total.value = 0
  }
  loading.value = false
}

watch(() => route.query.q, (newQ) => {
  keyword.value = newQ || ''
  currentPage.value = 1
  doSearch()
})

onMounted(() => {
  keyword.value = route.query.q || ''
  doSearch()
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
}

.keyword {
  color: var(--primary-light);
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
}
</style>
