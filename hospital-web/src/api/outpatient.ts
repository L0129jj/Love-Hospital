import request from '@/utils/request'
import type { Result, PageResult } from '@/types/api'
import type {
  RegDTO, RegVO, RegQuery,
  PrescriptionDTO, PrescriptionVO,
  PaymentDTO, PaymentVO
} from '@/types/outpatient'

// 挂号
export const addReg = (data: RegDTO) =>
  request.post<any, Result<RegVO>>('/outpatient/registrations', data)

export const getRegPage = (params: RegQuery) =>
  request.get<any, Result<PageResult<RegVO>>>('/outpatient/registrations', { params })

export const cancelReg = (id: number) =>
  request.patch<any, Result<void>>(`/outpatient/registrations/${id}/cancel`)

// 接诊流程
export const startConsultation = (id: number) =>
  request.patch<any, Result<void>>(`/outpatient/registrations/${id}/start`)

export const completeConsultation = (id: number) =>
  request.patch<any, Result<void>>(`/outpatient/registrations/${id}/complete`)

// 处方
export const addPrescription = (data: PrescriptionDTO) =>
  request.post<any, Result<PrescriptionVO>>('/outpatient/prescriptions', data)

export const getPrescriptionDetail = (id: number) =>
  request.get<any, Result<PrescriptionVO>>(`/outpatient/prescriptions/${id}`)

// 缴费
export const addPayment = (data: PaymentDTO) =>
  request.post<any, Result<PaymentVO>>('/outpatient/payments', data)

export const getPaymentsByPatient = (patientId: number) =>
  request.get<any, Result<PaymentVO[]>>('/outpatient/payments/by-patient', { params: { patientId } })

export const getPatientHistory = (params: { page: number; size: number; patientId: number }) =>
  request.get<any, Result<PageResult<RegVO>>>('/outpatient/registrations/patient-history', { params })

export const getMyPatients = (doctorId: number) =>
  request.get<any, Result<RegVO[]>>('/outpatient/registrations/my-patients', { params: { doctorId } })


