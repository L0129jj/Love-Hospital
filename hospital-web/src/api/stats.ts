import request from '@/utils/request'
import type { Result } from '@/types/api'
import type { ScheduleStatsVO, WorkloadStatsVO, TreatmentStatsVO, RevenueStatsVO } from '@/types/inpatient'

export const getScheduleStats = (deptId: number, startDate: string, endDate: string) =>
  request.get<any, Result<ScheduleStatsVO[]>>('/stats/schedule-by-dept', { params: { deptId, startDate, endDate } })

export const getDoctorWorkload = (doctorId: number, startDate: string, endDate: string) =>
  request.get<any, Result<WorkloadStatsVO[]>>('/stats/doctor-workload', { params: { doctorId, startDate, endDate } })

export const getPatientTreatment = (patientId: number) =>
  request.get<any, Result<TreatmentStatsVO>>('/stats/patient-treatment', { params: { patientId } })

export const getRevenue = (startDate: string, endDate: string) =>
  request.get<any, Result<RevenueStatsVO>>('/stats/revenue', { params: { startDate, endDate } })
