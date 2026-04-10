<template>
  <div class="admin-layout">
    <!-- 管理端侧边栏 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <router-link to="/admin" class="admin-logo">
          <span class="logo-icon">⚙️</span>
          <span class="logo-text">管理中心</span>
        </router-link>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/admin" class="sidebar-link" exact-active-class="active">
          <span class="link-icon">📊</span>
          <span>数据管理</span>
        </router-link>
        <router-link to="/admin/comments" class="sidebar-link" active-class="active">
          <span class="link-icon">💬</span>
          <span>评论管理</span>
        </router-link>
        <router-link to="/admin/analytics" class="sidebar-link" active-class="active">
          <span class="link-icon">📈</span>
          <span>数据统计</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <a class="back-link" @click="logout" style="cursor:pointer">
          <span>🚪 退出登录</span>
        </a>
        <router-link to="/" class="back-link">
          <span>← 返回用户端</span>
        </router-link>
      </div>
    </aside>

    <!-- 管理端主内容 -->
    <div class="admin-main">
      <header class="admin-topbar">
        <div class="topbar-left">
          <h2 class="page-title">{{ route.meta.title || '管理中心' }}</h2>
        </div>
        <div class="topbar-right">
          <router-link to="/" class="topbar-link">
            🏠 返回首页
          </router-link>
        </div>
      </header>

      <div class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

function logout() {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_user')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-primary, #0f172a);
}

/* 侧边栏 */
.admin-sidebar {
  width: 220px;
  background: rgba(15, 23, 42, 0.95);
  border-right: 1px solid rgba(148, 163, 184, 0.1);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.admin-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.admin-logo .logo-icon {
  font-size: 24px;
}

.admin-logo .logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  text-decoration: none;
}

.sidebar-link:hover {
  color: #e2e8f0;
  background: rgba(99, 102, 241, 0.1);
}

.sidebar-link.active {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.12);
}

.link-icon {
  font-size: 18px;
}

.sidebar-footer {
  padding: 16px 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}

.back-link {
  display: block;
  text-align: center;
  padding: 10px;
  border-radius: 8px;
  color: #60a5fa;
  font-size: 13px;
  transition: all 0.2s;
  text-decoration: none;
}

.back-link:hover {
  background: rgba(96, 165, 250, 0.1);
}

/* 主内容区 */
.admin-main {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
}

.admin-topbar {
  height: 56px;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
}

.topbar-link {
  font-size: 13px;
  color: #94a3b8;
  transition: color 0.2s;
  text-decoration: none;
}

.topbar-link:hover {
  color: #60a5fa;
}

.admin-content {
  flex: 1;
  padding: 0;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 移动端响应 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 60px;
  }

  .admin-logo .logo-text,
  .sidebar-link span:not(.link-icon),
  .back-link span {
    display: none;
  }

  .sidebar-link {
    justify-content: center;
    padding: 12px;
  }

  .admin-main {
    margin-left: 60px;
  }

  .back-link {
    font-size: 18px;
  }
}
</style>
