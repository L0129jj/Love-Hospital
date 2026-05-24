import { PageQuery } from './api'

// ---- 住院档案 ----
export interface ArchiveDTO {
  patientId: number
  deptId: number
  wardId: number
  bedId: number
  attendingDoctorId: number
  prepaidAmount: number
}
export interface ArchiveVO {
  id: number
  patientId: number
  deptId: number
  wardId: number
  bedId: number
  attendingDoctorId: number
  admissionTime: string
  dischargeTime: string | null
  prepaidAmount: number
  balance: number
  status: string
}
export interface ArchiveQuery extends PageQuery {
  patientId?: number
  status?: string
}

// ---- 巡诊记录 ----
export interface RecordDTO {
  archiveId: number
  doctorId: number
  recordDate: string
  symptoms: string
  treatmentPlan: string
  prescriptionNote: string
}
export interface RecordVO {
  id: number
  archiveId: number
  doctorId: number
  recordDate: string
  symptoms: string
  treatmentPlan: string
  prescriptionNote: string
}

// ---- 住院费用 ----
export interface FeeVO {
  id: number
  archiveId: number
  recordId: number
  feeDate: string
  feeType: string
  amount: number
  description: string
}

// ---- 统计 VO ----
export interface ScheduleStatsVO {
  deptId: number
  deptName: string
  totalSchedules: number
  startDate: string
  endDate: string
}
export interface WorkloadStatsVO {
  doctorId: number
  doctorName: string
  totalPatients: number
  totalSchedules: number
  startDate: string
  endDate: string
}
export interface TreatmentStatsVO {
  patientId: number
  patientName: string
  totalVisits: number
  totalHospitalizations: number
}
export interface RevenueStatsVO {
  totalRevenue: number
  startDate: string
  endDate: string
}
