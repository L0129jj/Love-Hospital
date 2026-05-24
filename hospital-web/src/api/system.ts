import request from '@/utils/request'
import type { Result, PageResult } from '@/types/api'
import type { DeptDTO, DeptVO, DoctorDTO, DoctorVO, DoctorQuery, DrugDTO, DrugVO } from '@/types/system'

// ---- 科室 ----
export const getDeptPage = (params: { page?: number; size?: number; name?: string }) =>
  request.get<any, Result<PageResult<DeptVO>>>('/system/depts/list', { params })

export const getAllDepts = () =>
  request.get<any, Result<DeptVO[]>>('/system/depts/all')

export const insertDept = (data: DeptDTO) =>
  request.post<any, Result<DeptVO>>('/system/depts/insert', data)

export const updateDept = (id: number, data: DeptDTO) =>
  request.put<any, Result<DeptVO>>(`/system/depts/update/${id}`, data)

export const deleteDept = (id: number) =>
  request.post<any, Result<void>>(`/system/depts/delete/${id}`)

// ---- 医生 ----
export const getDoctorPage = (params: DoctorQuery) =>
  request.get<any, Result<PageResult<DoctorVO>>>('/system/doctors/list', { params })

export const insertDoctor = (data: DoctorDTO) =>
  request.post<any, Result<DoctorVO>>('/system/doctors/insert', data)

export const updateDoctor = (id: number, data: DoctorDTO) =>
  request.put<any, Result<DoctorVO>>(`/system/doctors/update/${id}`, data)

export const deleteDoctor = (id: number) =>
  request.post<any, Result<void>>(`/system/doctors/delete/${id}`)

// ---- 药品 ----
export const getDrugPage = (params: { page?: number; size?: number; name?: string }) =>
  request.get<any, Result<PageResult<DrugVO>>>('/system/drugs/list', { params })

export const getAvailableDrugs = () =>
  request.get<any, Result<DrugVO[]>>('/system/drugs/available')

export const insertDrug = (data: DrugDTO) =>
  request.post<any, Result<DrugVO>>('/system/drugs/insert', data)

export const updateDrug = (id: number, data: DrugDTO) =>
  request.put<any, Result<DrugVO>>(`/system/drugs/update/${id}`, data)

export const deleteDrug = (id: number) =>
  request.post<any, Result<void>>(`/system/drugs/delete/${id}`)

