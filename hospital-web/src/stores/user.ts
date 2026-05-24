import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, patientLoginApi, doctorLoginApi, patientRegisterApi, doctorRegisterApi, logoutApi, refreshApi } from '@/api/auth'
import { getToken, setToken, removeToken, setUserInfo, getRole, getUserId, getName } from '@/utils/auth'
import type { LoginDTO, PatientLoginDTO, DoctorLoginDTO, PatientRegisterDTO, DoctorRegisterDTO, TokenVO } from '@/types/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // ---- state ----
  const token = ref<string | null>(getToken())
  const role = ref<string | null>(getRole())
  const userId = ref<number | null>(getUserId())
  const name = ref<string | null>(getName())

  // ---- getters ----
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ROLE_ADMIN')
  const isDoctor = computed(() => role.value === 'ROLE_DOCTOR')
  const isPatient = computed(() => role.value === 'ROLE_PATIENT')

  // ---- actions ----
  /** 医护人员（管理员）登录 */
  async function login(dto: LoginDTO) {
    const res = await loginApi(dto)
    handleLoginSuccess(res.data)
  }

  /** 医生登录 */
  async function doctorLogin(dto: DoctorLoginDTO) {
    const res = await doctorLoginApi(dto)
    handleLoginSuccess(res.data)
  }

  /** 患者登录 */
  async function patientLogin(dto: PatientLoginDTO) {
    const res = await patientLoginApi(dto)
    handleLoginSuccess(res.data)
  }

  /** 患者注册 */
  async function patientRegister(dto: PatientRegisterDTO) {
    await patientRegisterApi(dto)
  }

  /** 医生注册 */
  async function doctorRegister(dto: DoctorRegisterDTO) {
    await doctorRegisterApi(dto)
  }

  function handleLoginSuccess(data: TokenVO) {
    token.value = data.token
    role.value = data.role
    userId.value = data.userId
    name.value = data.name || null
    setToken(data.token)
    setUserInfo(data.role, data.userId, data.expiresAt, data.name)
  }

  /** 登出 */
  async function logout() {
    try { await logoutApi() } catch { /* ignore */ }
    resetState()
    router.push('/login')
  }

  /** 刷新 Token */
  async function refresh() {
    const res = await refreshApi()
    handleLoginSuccess(res.data)
  }

  function resetState() {
    token.value = null
    role.value = null
    userId.value = null
    name.value = null
    removeToken()
  }

  return {
    token, role, userId, name,
    isLoggedIn, isAdmin, isDoctor, isPatient,
    login, doctorLogin, patientLogin, patientRegister, doctorRegister, logout, refresh, resetState
  }
})
