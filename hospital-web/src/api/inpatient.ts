import request from '@/utils/request'
import type { Result, PageResult } from '@/types/api'
import type { ArchiveDTO, ArchiveVO, ArchiveQuery, RecordDTO, RecordVO, FeeVO } from '@/types/inpatient'

// 住院档案
export const addArchive = (data: ArchiveDTO) =>
  request.post<any, Result<ArchiveVO>>('/inpatient/archives', data)

export const getArchivePage = (params: ArchiveQuery) =>
  request.get<any, Result<PageResult<ArchiveVO>>>('/inpatient/archives', { params })

export const dischargeArchive = (id: number) =>
  request.patch<any, Result<ArchiveVO>>(`/inpatient/archives/${id}/discharge`)

export const rechargeArchive = (id: number, amount: number) =>
  request.patch<any, Result<ArchiveVO>>(`/inpatient/archives/${id}/recharge`, null, { params: { amount } })

// 巡诊记录
export const addRecord = (data: RecordDTO) =>
  request.post<any, Result<RecordVO>>('/inpatient/records', data)

export const getRecordsByArchive = (archiveId: number) =>
  request.get<any, Result<RecordVO[]>>('/inpatient/records/by-archive', { params: { archiveId } })

// 费用
export const getFeesByArchive = (archiveId: number) =>
  request.get<any, Result<FeeVO[]>>('/inpatient/fees/by-archive', { params: { archiveId } })

export const getAvailableBeds = () =>
  request.get<any, Result<any[]>>('/inpatient/archives/available-beds')


