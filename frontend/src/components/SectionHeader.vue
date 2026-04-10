<template>
  <div class="section-header">
    <div class="left">
      <span v-if="icon" class="icon">{{ icon }}</span>
      <h2 class="title">{{ title }}</h2>
      <span v-if="subtitle" class="subtitle">{{ subtitle }}</span>
    </div>
    <router-link v-if="moreLink" :to="moreLink" class="more-link">
      查看更多 <el-icon><ArrowRight /></el-icon>
    </router-link>
    <slot name="right" />
  </div>
</template>

<script setup>
defineProps({
  icon: String,
  title: { type: String, required: true },
  subtitle: String,
  moreLink: String,
})
</script>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon {
  font-size: 22px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin-left: 4px;
}

.more-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--primary-light);
  transition: color 0.2s;
}

.more-link:hover {
  color: var(--primary);
}
</style>
