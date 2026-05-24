import { PageQuery } from './api'

// ---- 科室 ----
export interface DeptDTO {
  name: string
  deptType?: string
  location?: string
  description?: string
}
export interface DeptVO {
  id: number
  name: string
  deptType?: string
  location?: string
  description?: string
}

// ---- 医生 ----
export interface DoctorDTO {
  name: string
  deptCode: string
}
export interface DoctorVO {
  id: number
  name: string
  deptCode: string
  title?: string
  consultationFee?: number
  deptId?: number
}
export interface DoctorQuery extends PageQuery {
  deptId?: number
}

// ---- 药品 ----
export interface DrugDTO {
  name: string
  specification?: string
  unit?: string
  price?: number
  stock: number
  description?: string
}
export interface DrugVO {
  id: number
  name: string
  specification?: string
  unit?: string
  price?: number
  stock?: number
  description?: string
}

