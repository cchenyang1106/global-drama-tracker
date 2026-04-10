import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ========== 用户端路由 ==========
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/dramas',
    name: 'DramaList',
    component: () => import('@/views/DramaList.vue'),
    meta: { title: '剧集库' },
  },
  {
    path: '/drama/:id',
    name: 'DramaDetail',
    component: () => import('@/views/DramaDetail.vue'),
    meta: { title: '剧集详情' },
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: () => import('@/views/Ranking.vue'),
    meta: { title: '排行榜' },
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/SearchResult.vue'),
    meta: { title: '搜索结果' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' },
  },

  // ========== 管理端登录页（独立布局） ==========
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/AdminLogin.vue'),
    meta: { title: '管理端登录', layout: 'admin' },
  },

  // ========== 管理端路由（需要登录） ==========
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { layout: 'admin', requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { title: '数据管理', layout: 'admin', requiresAuth: true },
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/AdminComments.vue'),
        meta: { title: '评论管理', layout: 'admin', requiresAuth: true },
      },
      {
        path: 'analytics',
        name: 'AdminAnalytics',
        component: () => import('@/views/AdminAnalytics.vue'),
        meta: { title: '数据统计', layout: 'admin', requiresAuth: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// 管理端登录守卫
router.beforeEach((to) => {
  document.title = `${to.meta.title || ''} - Global Drama Tracker`

  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('admin_token')
    if (!token) {
      return { name: 'AdminLogin' }
    }
  }
})

export default router
