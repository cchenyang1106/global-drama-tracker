import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '找搭子' } },
  { path: '/activity/:id', name: 'ActivityDetail', component: () => import('@/views/ActivityDetail.vue'), meta: { title: '活动详情' } },
  { path: '/publish', name: 'Publish', component: () => import('@/views/Publish.vue'), meta: { title: '发布活动', requiresLogin: true } },
  { path: '/messages', name: 'Messages', component: () => import('@/views/Messages.vue'), meta: { title: '消息', requiresLogin: true } },
  { path: '/chat/:matchId', name: 'Chat', component: () => import('@/views/Chat.vue'), meta: { title: '聊天', requiresLogin: true } },
  { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人资料', requiresLogin: true } },
  { path: '/user/:userId', name: 'UserPage', component: () => import('@/views/UserPage.vue'), meta: { title: '用户资料' } },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/privacy', name: 'Privacy', component: () => import('@/views/Privacy.vue'), meta: { title: '隐私政策' } },
  { path: '/my-data', name: 'MyData', component: () => import('@/views/MyData.vue'), meta: { title: '我的数据', requiresLogin: true } },
  // 管理端
  { path: '/admin/login', name: 'AdminLogin', component: () => import('@/views/AdminLogin.vue'), meta: { title: '管理端登录' } },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      { path: '', name: 'AdminAnalytics', component: () => import('@/views/AdminAnalytics.vue'), meta: { title: '数据统计', layout: 'admin', requiresAuth: true } },
      { path: 'content', name: 'AdminContent', component: () => import('@/views/AdminContent.vue'), meta: { title: '内容管理', layout: 'admin', requiresAuth: true } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } },
})

router.beforeEach((to, from, next) => {
  // 管理端登录守卫
  if (to.meta.requiresAuth && !localStorage.getItem('admin_token')) {
    return next('/admin/login')
  }
  // 用户端登录守卫
  if (to.meta.requiresLogin && !localStorage.getItem('token')) {
    return next(`/login?redirect=${to.fullPath}`)
  }
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '找搭子'
  next()
})

export default router
