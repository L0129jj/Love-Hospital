import request from '@/utils/request'
import type { Result, PageResult } from '@/types/api'
import type { ScheduleDTO, ScheduleVO, ScheduleQuery } from '@/types/schedule'

export const getSchedulePage = (params: ScheduleQuery) =>
  request.get<any, Result<PageResult<ScheduleVO>>>('/schedules', { params })

export const addSchedule = (data: ScheduleDTO) =>
  request.post<any, Result<void>>('/schedules', data)

export const updateSchedule = (id: number, data: ScheduleDTO) =>
  request.put<any, Result<ScheduleVO>>(`/schedules/${id}`, data)

export const deleteSchedule = (id: number) =>
  request.delete<any, Result<void>>(`/schedules/${id}`)

export const getScheduleByDept = (deptId: number, date?: string) =>
  request.get<any, Result<ScheduleVO[]>>('/schedules/by-dept', { params: { deptId, date } })
