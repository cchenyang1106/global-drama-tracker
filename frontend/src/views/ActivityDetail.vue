<template>
  <div class="detail-page" v-if="activity">
    <div class="back-bar">
      <el-button text @click="$router.back()">← 返回</el-button>
    </div>

    <div class="detail-card">
      <div class="detail-header">
        <el-tag :type="catColor(activity.category)" round>{{ activity.category }}</el-tag>
        <el-tag v-if="activity.status === 0" type="info" round>已关闭</el-tag>
        <el-tag v-if="activity.status === 2" type="warning" round>已满员</el-tag>
      </div>
      <h1 class="detail-title">{{ activity.title }}</h1>

      <div class="author-block" @click="$router.push(`/user/${activity.userId}`)">
        <el-avatar :size="44" :src="activity.authorAvatar" style="background:#6366f1">
          {{ (activity.authorName || '?').charAt(0) }}
        </el-avatar>
        <div>
          <div class="ab-name">{{ activity.authorName }}</div>
          <div class="ab-sub">
            <span v-if="activity.authorCity">📍{{ activity.authorCity }}</span>
            <span v-if="activity.authorAge">{{ activity.authorAge }}岁</span>
            <span v-if="activity.authorGender === 1">♂</span>
            <span v-if="activity.authorGender === 2">♀</span>
          </div>
        </div>
      </div>

      <div class="detail-body">
        <p>{{ activity.description }}</p>
      </div>

      <div class="info-grid">
        <div v-if="activity.location" class="info-item"><span class="info-label">📍 地点</span><span>{{ activity.location }}</span></div>
        <div v-if="activity.activityTime" class="info-item"><span class="info-label">🕐 时间</span><span>{{ activity.activityTime }}</span></div>
        <div class="info-item"><span class="info-label">👥 人数</span><span>{{ activity.joinedCount || 0 }}/{{ activity.maxPeople || 1 }}</span></div>
        <div class="info-item"><span class="info-label">👁 浏览</span><span>{{ activity.viewCount || 0 }}</span></div>
      </div>

      <div v-if="activity.tags" class="detail-tags">
        <el-tag v-for="t in activity.tags.split(',')" :key="t" size="small" round>{{ t }}</el-tag>
      </div>
    </div>

    <!-- 申请加入 -->
    <div v-if="canApply" class="apply-section">
      <h3>想参加？发送申请吧！</h3>
      <el-input v-model="applyMsg" type="textarea" :rows="2" placeholder="写一句话介绍自己，增加通过率..." maxlength="200" show-word-limit />
      <el-button type="primary" :loading="applying" @click="doApply" style="margin-top:12px">申请加入</el-button>
    </div>
    <div v-else-if="!userStore.isLoggedIn" class="apply-section">
      <router-link :to="`/login?redirect=/activity/${activity.id}`" class="login-tip">登录后申请加入</router-link>
    </div>
    <div v-else-if="isOwner" class="apply-section owner-tip">
      这是你发布的活动
    </div>
    <div v-else-if="applied" class="apply-section owner-tip">
      ✅ 已申请，等待对方确认
    </div>
  </div>
  <div v-else class="loading-page">加载中...</div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActivityDetail } from '@/api/activity'
import { applyActivity, getSentRequests } from '@/api/match'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const activity = ref(null)
const applyMsg = ref('')
const applying = ref(false)
const applied = ref(false)

const isOwner = computed(() => activity.value && userStore.userId && activity.value.userId === userStore.userId)
const canApply = computed(() => {
  return userStore.isLoggedIn && !isOwner.value && !applied.value && activity.value?.status === 1
})

function catColor(cat) {
  const map = { '旅游': 'success', '运动': 'warning', '美食': 'danger', '电影': '', '学习': 'info' }
  return map[cat] || 'info'
}

async function doApply() {
  applying.value = true
  try {
    await applyActivity({ activityId: activity.value.id, message: applyMsg.value })
    ElMessage.success('申请已发送！')
    applied.value = true
  } catch (e) { ElMessage.error(e?.message || '申请失败') }
  applying.value = false
}

onMounted(async () => {
  activity.value = await getActivityDetail(route.params.id)
  if (userStore.isLoggedIn) {
    try {
      const sent = await getSentRequests()
      applied.value = (sent || []).some(r => r.activityId === activity.value?.id)
    } catch { /* ignore */ }
  }
})
</script>

<style scoped>
.detail-page { max-width: 700px; margin: 0 auto; padding: 20px; }
.back-bar { margin-bottom: 16px; }
.detail-card { background: var(--bg-card); border-radius: 12px; padding: 24px; }
.detail-header { display: flex; gap: 8px; margin-bottom: 12px; }
.detail-title { font-size: 22px; font-weight: 800; margin-bottom: 16px; }
.author-block { display: flex; gap: 12px; align-items: center; padding: 12px; background: rgba(255,255,255,0.03); border-radius: 10px; margin-bottom: 16px; cursor: pointer; }
.author-block:hover { background: rgba(99,102,241,0.08); }
.ab-name { font-weight: 600; font-size: 15px; }
.ab-sub { font-size: 12px; color: var(--text-muted); display: flex; gap: 8px; }
.detail-body { font-size: 15px; line-height: 1.8; color: var(--text-secondary); margin-bottom: 16px; white-space: pre-wrap; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px; }
.info-item { display: flex; justify-content: space-between; padding: 8px 12px; background: rgba(255,255,255,0.03); border-radius: 8px; font-size: 14px; }
.info-label { color: var(--text-muted); }
.detail-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.apply-section { background: var(--bg-card); border-radius: 12px; padding: 20px; margin-top: 16px; }
.apply-section h3 { font-size: 16px; margin-bottom: 12px; }
.login-tip { display: block; text-align: center; padding: 12px; background: linear-gradient(135deg,#6366f1,#8b5cf6); color: white; border-radius: 8px; font-weight: 600; }
.owner-tip { text-align: center; color: var(--text-muted); }
.loading-page { text-align: center; padding: 60px; color: var(--text-muted); }
</style>
