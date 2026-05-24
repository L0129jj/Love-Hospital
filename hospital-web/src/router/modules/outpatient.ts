import type { RouteRecordRaw } from 'vue-router'

const outpatientRoutes: RouteRecordRaw[] = [
  {
    path: '/outpatient/reg',
    name: 'Registration',
    component: () => import('@/views/outpatient/RegView.vue'),
    meta: { title: '挂号管理', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT'] }
  },
  {
    path: '/outpatient/prescription',
    name: 'Prescription',
    component: () => import('@/views/outpatient/PrescriptionView.vue'),
    meta: { title: '处方管理', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
  },
  {
    path: '/outpatient/payment',
    name: 'Payment',
    component: () => import('@/views/outpatient/PaymentView.vue'),
    meta: { title: '缴费管理', roles: ['ROLE_ADMIN', 'ROLE_PATIENT'] }
  },
  {
    path: '/outpatient/history',
    name: 'PatientHistory',
    component: () => import('@/views/outpatient/PatientRecordView.vue'),
    meta: { title: '我的就诊记录', roles: ['ROLE_ADMIN', 'ROLE_PATIENT'] }
  },
]
export default outpatientRoutes

