<template>
  <div class="user-page" v-if="profile">
    <el-button text @click="$router.back()" style="margin-bottom:12px">← 返回</el-button>
    <div class="user-card">
      <div class="user-top">
        <el-avatar :size="72" :src="profile.avatarUrl" style="background:#6366f1;font-size:28px">
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

onMounted(async () => {
  try { profile.value = await getUserProfile(route.params.userId) } catch { profile.value = {} }
})
</script>

<style scoped>
.user-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.user-card { background: var(--bg-card); border-radius: 12px; padding: 24px; }
.user-top { display: flex; gap: 16px; align-items: center; margin-bottom: 20px; }
.user-top h2 { font-size: 20px; font-weight: 800; }
.user-sub { display: flex; gap: 10px; font-size: 13px; color: var(--text-muted); margin-top: 4px; }
.info-row { padding: 10px 0; border-top: 1px solid rgba(255,255,255,0.05); font-size: 14px; color: var(--text-secondary); }
.info-label { color: var(--text-muted); margin-right: 8px; }
.hobbies { padding: 10px 0; border-top: 1px solid rgba(255,255,255,0.05); }
.hobby-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 8px; }
.loading-page { text-align: center; padding: 60px; color: var(--text-muted); }
</style>
