<template>
  <!-- 管理端使用独立布局，不显示用户端导航 -->
  <div v-if="isAdminLayout">
    <router-view />
  </div>

  <!-- 用户端布局 -->
  <div v-else class="app">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="navbar-inner">
        <router-link to="/" class="logo">
          <span class="logo-icon">🎬</span>
          <span class="logo-text">Global Drama Tracker</span>
        </router-link>

        <nav class="nav-links">
          <router-link to="/" class="nav-link" exact-active-class="active">
            <el-icon><HomeFilled /></el-icon>首页
          </router-link>
          <router-link to="/dramas" class="nav-link" active-class="active">
            <el-icon><Film /></el-icon>剧集库
          </router-link>
          <router-link to="/ranking" class="nav-link" active-class="active">
            <el-icon><Trophy /></el-icon>排行榜
          </router-link>
        </nav>

        <div class="nav-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索剧集..."
            :prefix-icon="Search"
            clearable
            size="default"
            @keyup.enter="handleSearch"
            class="search-input"
          />
        </div>

        <div class="nav-user">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-avatar :size="28" style="background:#6366f1">{{ userStore.nickname?.charAt(0) }}</el-avatar>
                <span class="user-name">{{ userStore.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="login-btn">登录</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <p>© 2026 Global Drama Tracker · 全球剧集追踪平台</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')

const isAdminLayout = computed(() => {
  return route.matched.some(record => record.meta.layout === 'admin')
})

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { q: searchKeyword.value.trim() } })
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}

onMounted(() => {
  userStore.init()
})
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
  height: 64px;
}

.navbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  flex-shrink: 0;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-links {
  display: flex;
  gap: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.nav-link:hover {
  color: var(--text-primary);
  background: rgba(99, 102, 241, 0.1);
}

.nav-link.active {
  color: var(--primary-light);
  background: rgba(99, 102, 241, 0.15);
}

.nav-search {
  margin-left: auto;
  width: 260px;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: none;
  color: var(--text-primary);
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary);
}

.search-input :deep(.el-input__inner) {
  color: var(--text-primary);
}

.search-input :deep(.el-input__inner::placeholder) {
  color: var(--text-muted);
}

.main-content {
  min-height: calc(100vh - 64px - 60px);
  padding-top: 64px;
}

.nav-user {
  display: flex;
  align-items: center;
  margin-left: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: color 0.2s;
}

.user-info:hover {
  color: var(--text-primary);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.login-btn {
  padding: 6px 16px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: white;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s;
  text-decoration: none;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

.footer {
  text-align: center;
  padding: 20px;
  color: var(--text-muted);
  font-size: 13px;
  border-top: 1px solid var(--border-color);
}

/* 页面切换动画 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease;
}
.page-enter-from,
.page-leave-to {
  opacity: 0;
}
</style>
