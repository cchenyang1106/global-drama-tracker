<template>
  <div class="user-page" v-if="profile">
    <el-button text @click="$router.back()" style="margin-bottom:12px">← 返回</el-button>
    <div class="user-card">
      <div class="user-top">
        <el-avatar :size="72" :src="profile.avatarUrl" style="background:#f472b6;font-size:28px">
          {{ (profile.nickname || '?').charAt(0) }}
        </el-avatar>
        <div>
          <h2>{{ profile.nickname || '匿名用户' }}</h2>
          <div class="user-sub">
            <span v-if="profile.gender === 1">♂ 男</span>
            <span v-if="profile.gender === 2">♀ 女</span>
            <span v-if="profile.age">{{ profile.age }}岁</span>
            <span v-if="profile.city">📍 {{ profile.city }}</span>
          </div>
        </div>
      </div>
      <div v-if="profile.occupation" class="info-row"><span class="info-label">💼 职业</span>{{ profile.occupation }}</div>
      <div v-if="profile.bio" class="info-row"><span class="info-label">📝 简介</span>{{ profile.bio }}</div>
      <div v-if="profile.hobbies" class="hobbies">
        <span class="info-label">🎯 爱好</span>
        <div class="hobby-tags">
          <el-tag v-for="h in profile.hobbies.split(',')" :key="h" size="small" round>{{ h.trim() }}</el-tag>
        </div>
      </div>
      <div v-if="photos.length > 0" class="photos-section">
        <span class="info-label">📸 照片</span>
        <div class="photos-grid">
          <el-image v-for="(p, i) in photos" :key="i" :src="p" :preview-src-list="photos" :initial-index="i"
            fit="cover" class="photo-img" :preview-teleported="true" />
        </div>
      </div>
    </div>
  </div>
  <div v-else class="loading-page">加载中...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getUserProfile } from '@/api/profile'

const route = useRoute()
const profile = ref(null)
const photos = ref([])

onMounted(async () => {
  try {
    profile.value = await getUserProfile(route.params.userId)
    if (profile.value?.photos) {
      try { photos.value = JSON.parse(profile.value.photos) } catch { photos.value = [] }
    }
  } catch { profile.value = {} }
})
</script>

<style scoped>
.user-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.user-card { background: var(--bg-card); border-radius: 12px; padding: 24px; }
.user-top { display: flex; gap: 16px; align-items: center; margin-bottom: 20px; }
.user-top h2 { font-size: 20px; font-weight: 800; }
.user-sub { display: flex; gap: 10px; font-size: 13px; color: var(--text-muted); margin-top: 4px; }
.info-row { padding: 10px 0; border-top: 1px solid var(--border-color); font-size: 14px; color: var(--text-secondary); }
.info-label { color: var(--text-muted); margin-right: 8px; }
.hobbies { padding: 10px 0; border-top: 1px solid var(--border-color); }
.hobby-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 8px; }
.photos-section { padding: 10px 0; border-top: 1px solid var(--border-color); }
.photos-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 8px; }
.photo-img { width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 10px; cursor: pointer; transition: transform 0.2s; }
.photo-img:hover { transform: scale(1.03); }
.loading-page { text-align: center; padding: 60px; color: var(--text-muted); }
</style>
