import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/components/Layout.vue'
import { getToken, getRole } from '@/utils/auth'

// 导入模块路由
import systemRoutes from './modules/system'
import scheduleRoutes from './modules/schedule'
import outpatientRoutes from './modules/outpatient'
import inpatientRoutes from './modules/inpatient'

// 公开路由（无需登录）
const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限', public: true }
  },
]

// 受保护路由（需要登录 + 角色）
const protectedRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘' }
      },
      ...systemRoutes,
      ...scheduleRoutes,
      ...outpatientRoutes,
      ...inpatientRoutes,
      {
        path: '/stats',
        name: 'Stats',
        component: () => import('@/views/stats/StatsView.vue'),
        meta: { title: '统计分析', roles: ['ROLE_ADMIN'] }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心' }
      },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, ...protectedRoutes],
})

// ========== 全局前置守卫 ==========
router.beforeEach((to, _from, next) => {
  const token = getToken()
  const role = getRole()

  // 1. 公开页面直接放行
  if (to.meta.public) {
    // 已登录用户访问登录页 → 重定向到首页
    if (to.path === '/login' && token) {
      return next('/')
    }
    return next()
  }

  // 2. 未登录 → 跳转登录页
  if (!token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 3. 角色权限检查
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && role && !requiredRoles.includes(role)) {
    return next('/403')
  }

  next()
})

export default router
