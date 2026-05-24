import type { RouteRecordRaw } from 'vue-router'

const systemRoutes: RouteRecordRaw[] = [
  {
    path: '/system/depts',
    name: 'Depts',
    component: () => import('@/views/system/DeptView.vue'),
    meta: { title: '科室管理', roles: ['ROLE_ADMIN'] }
  },
  {
    path: '/system/doctors',
    name: 'Doctors',
    component: () => import('@/views/system/DoctorView.vue'),
    meta: { title: '医生管理', roles: ['ROLE_ADMIN'] }
  },
  {
    path: '/system/drugs',
    name: 'Drugs',
    component: () => import('@/views/system/DrugView.vue'),
    meta: { title: '药品管理', roles: ['ROLE_ADMIN'] }
  },
]
export default systemRoutes
