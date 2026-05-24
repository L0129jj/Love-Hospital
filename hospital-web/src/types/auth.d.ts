export interface LoginDTO {
  username: string
  password: string
}

export interface PatientLoginDTO {
  phone: string
  password: string
}

export interface DoctorLoginDTO {
  workNo: string
  password: string
}

export interface PatientRegisterDTO {
  username: string
  password: string
  name: string
  gender: string
  phone: string
  address?: string
}

export interface DoctorRegisterDTO {
  workNo: string
  password: string
  name: string
  gender: string
  title: string
  phone: string
  deptId: number
  consultationFee: number
}

export interface RefreshDTO {}

export interface TokenVO {
  token: string
  role: string       // ROLE_ADMIN | ROLE_DOCTOR | ROLE_PATIENT
  userId: number
  expiresAt: number
  name?: string
}
