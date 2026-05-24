import { PageQuery } from './api'

// ---- 挂号 ----
export interface RegDTO {
  patientId: number
  doctorId: number
  deptId: number
  visitType: string    // '初诊' | '复诊'
}
export interface RegVO {
  id: number
  patientId: number
  doctorId: number
  deptId: number
  regTime: string
  visitType: string
  status: string
}
export interface RegQuery extends PageQuery {
  patientId?: number
  doctorId?: number
  status?: string
}

// ---- 处方 ----
export interface PrescriptionDetailDTO {
  prescriptionId?: number
  drugId: number
  quantity: number
  price: number
  dosage: string
  subtotal: number
}
export interface PrescriptionDTO {
  regId: number
  doctorId: number
  patientId: number
  symptoms: string
  diagnosis: string
  consultationFee: number
  details: PrescriptionDetailDTO[]
}
export interface PrescriptionVO {
  id: number
  regId: number
  doctorId: number
  patientId: number
  symptoms: string
  diagnosis: string
  consultationFee: number
  totalFee: number
  createTime: string
  status: string
  details: PrescriptionDetailDTO[]
}

// ---- 缴费 ----
export interface PaymentDTO {
  prescriptionId: number
  patientId: number
  amount: number
  payMethod: string
}
export interface PaymentVO {
  id: number
  prescriptionId: number
  patientId: number
  amount: number
  payTime: string
  payMethod: string
}
