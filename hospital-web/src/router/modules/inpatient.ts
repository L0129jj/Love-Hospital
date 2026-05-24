import type { RouteRecordRaw } from 'vue-router'

const inpatientRoutes: RouteRecordRaw[] = [
  {
    path: '/inpatient/archive',
    name: 'Archive',
    component: () => import('@/views/inpatient/ArchiveView.vue'),
    meta: { title: '住院档案', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT'] }
  },
  {
    path: '/inpatient/record',
    name: 'Record',
    component: () => import('@/views/inpatient/RecordView.vue'),
    meta: { title: '巡诊记录', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
  },
  {
    path: '/inpatient/fee',
    name: 'Fee',
    component: () => import('@/views/inpatient/FeeView.vue'),
    meta: { title: '费用查询', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT'] }
  },
]
export default inpatientRoutes
