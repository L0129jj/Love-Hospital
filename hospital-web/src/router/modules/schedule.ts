import type { RouteRecordRaw } from 'vue-router'

const scheduleRoutes: RouteRecordRaw[] = [
  {
    path: '/schedule',
    name: 'Schedule',
    component: () => import('@/views/schedule/ScheduleView.vue'),
    meta: { title: '排班管理', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
  },
]
export default scheduleRoutes
