import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ========== 用户端路由（使用 App.vue 布局） ==========
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

  // ========== 管理端路由（使用独立 AdminLayout 布局） ==========
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { layout: 'admin' },
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { title: '数据管理', layout: 'admin' },
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

router.beforeEach((to) => {
  document.title = `${to.meta.title || ''} - Global Drama Tracker`
})

export default router
