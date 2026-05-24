import request from '@/utils/request'
import type { Result } from '@/types/api'
import type { LoginDTO, PatientLoginDTO, DoctorLoginDTO, PatientRegisterDTO, DoctorRegisterDTO, TokenVO } from '@/types/auth'

export function loginApi(data: LoginDTO) {
  return request.post<any, Result<TokenVO>>('/auth/login', data)
}

export function patientLoginApi(data: PatientLoginDTO) {
  return request.post<any, Result<TokenVO>>('/auth/patient-login', data)
}

export function doctorLoginApi(data: DoctorLoginDTO) {
  return request.post<any, Result<TokenVO>>('/auth/doctor-login', data)
}

export function patientRegisterApi(data: PatientRegisterDTO) {
  return request.post<any, Result<void>>('/auth/patient-register', data)
}

export function doctorRegisterApi(data: DoctorRegisterDTO) {
  return request.post<any, Result<void>>('/auth/doctor-register', data)
}

export function logoutApi() {
  return request.post<any, Result<void>>('/auth/logout')
}

export function refreshApi() {
  return request.post<any, Result<TokenVO>>('/auth/refresh')
}
