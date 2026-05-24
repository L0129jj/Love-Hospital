import { PageQuery } from './api'

export interface ScheduleDTO {
  doctorId: number
  scheduleDate: string   // YYYY-MM-DD
  shiftType: string
  startTime: string      // HH:mm
  endTime: string        // HH:mm
  deptId: number
}

export interface ScheduleVO {
  id: number
  doctorId: number
  scheduleDate: string
  shiftType: string
  startTime: string
  endTime: string
  deptId: number
}

export interface ScheduleQuery extends PageQuery {
  doctorId?: number
  date?: string
}
