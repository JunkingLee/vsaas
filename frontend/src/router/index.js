import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'device', name: 'Device', component: () => import('../views/Device.vue') },
      { path: 'alarm', name: 'Alarm', component: () => import('../views/Alarm.vue') },
      { path: 'billing', name: 'Billing', component: () => import('../views/Billing.vue') },
      { path: 'user', name: 'User', component: () => import('../views/User.vue') },
      { path: 'live', name: 'Live', component: () => import('../views/Live.vue') },
      { path: 'playback', name: 'Playback', component: () => import('../views/Playback.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path !== '/login' && !localStorage.getItem('token')) {
    next('/login')
  } else {
    next()
  }
})

export default router
