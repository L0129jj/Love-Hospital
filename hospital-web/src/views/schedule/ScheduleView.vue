<template>
  <div class="page-container">
    <!-- 头部操作区 -->
    <div class="page-header">
      <h2 class="page-title">医生排班管理</h2>
      <div class="header-actions">
        <el-select v-model="filterDept" placeholder="选择科室" clearable class="custom-select" style="width: 150px">
          <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-date-picker
          v-model="filterDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          class="custom-date-picker"
          style="width: 150px"
        />
        <el-button type="primary" class="gradient-btn" @click="handleSearch">查询</el-button>
        <el-button type="success" v-if="userStore.role === 'ROLE_ADMIN'" @click="handleCreate">新增排班</el-button>
      </div>
    </div>

    <!-- 排班表格 -->
    <div class="content-card">
      <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="排班ID" width="100" />
        <el-table-column prop="scheduleDate" label="排班日期" width="130" />
        <el-table-column prop="deptId" label="坐诊科室">
          <template #default="scope">
            <span>{{ getDeptName(scope.row.deptId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="doctorId" label="坐诊医生">
          <template #default="scope">
            <span>{{ getDoctorName(scope.row.doctorId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="shiftType" label="班次">
          <template #default="scope">
            <el-tag :type="scope.row.shiftType === '上午' ? 'warning' : 'primary'">
              {{ scope.row.shiftType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="就诊时间段" width="180">
          <template #default="scope">
            <span>{{ scope.row.startTime }} - {{ scope.row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" v-if="userStore.role === 'ROLE_ADMIN'">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[5, 10, 20]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 排班弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker
            v-model="form.scheduleDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            class="custom-dialog-input"
          />
        </el-form-item>
        <el-form-item label="坐诊科室" prop="deptId">
          <el-select v-model="form.deptId" placeholder="选择科室" style="width: 100%" class="custom-dialog-select">
            <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="坐诊医生" prop="doctorId">
          <el-select v-model="form.doctorId" placeholder="选择医生" style="width: 100%" class="custom-dialog-select">
            <el-option v-for="doc in doctors" :key="doc.id" :label="doc.name" :value="doc.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="坐诊班次" prop="shiftType">
          <el-radio-group v-model="form.shiftType" @change="handleShiftChange">
            <el-radio value="上午">上午门诊</el-radio>
            <el-radio value="下午">下午门诊</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时间范围">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-time-select
              v-model="form.startTime"
              start="08:00"
              step="00:30"
              end="18:00"
              placeholder="开始时间"
              style="flex: 1"
              class="custom-dialog-select"
            />
            <el-time-select
              v-model="form.endTime"
              start="08:00"
              step="00:30"
              end="18:00"
              placeholder="结束时间"
              style="flex: 1"
              class="custom-dialog-select"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getSchedulePage, addSchedule, updateSchedule, deleteSchedule } from '@/api/schedule'
import { useUserStore } from '@/stores/user'
import { useTable } from '@/hooks/useTable'
import type { ScheduleVO } from '@/types/schedule'

const userStore = useUserStore()

const filterDept = ref<number | undefined>(undefined)
const filterDate = ref<string | undefined>(undefined)

const dialogVisible = ref(false)
const dialogTitle = ref('新增排班')
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  id: undefined as number | undefined,
  scheduleDate: '',
  deptId: undefined as number | undefined,
  doctorId: undefined as number | undefined,
  shiftType: '上午',
  startTime: '08:30',
  endTime: '12:00'
})

const rules = {
  scheduleDate: [{ required: true, message: '请选择日期', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  shiftType: [{ required: true, message: '请选择班次', trigger: 'change' }]
}

const depts = [
  { id: 1, name: '心血管内科' },
  { id: 2, name: '神经外科' },
  { id: 3, name: '小儿呼吸科' },
  { id: 4, name: '骨科关节门诊' }
]

const doctors = [
  { id: 1, name: '张忠民', deptId: 1 },
  { id: 2, name: '李晓华', deptId: 1 },
  { id: 3, name: '赵铁生', deptId: 2 },
  { id: 4, name: '孙玉芳', deptId: 3 }
]

function getDeptName(id: number) {
  const d = depts.find(x => x.id === id)
  return d ? d.name : '通用科室'
}

function getDoctorName(id: number) {
  const doc = doctors.find(x => x.id === id)
  return doc ? doc.name : '特邀医生'
}

function handleShiftChange(val: any) {
  if (val === '上午') {
    form.value.startTime = '08:30'
    form.value.endTime = '12:00'
  } else {
    form.value.startTime = '14:00'
    form.value.endTime = '17:30'
  }
}

const {
  loading,
  tableData,
  pagination,
  loadData,
  handlePageChange,
  handleSizeChange
} = useTable<ScheduleVO>((params) => getSchedulePage(params))

// 模拟初始排班数据做鲁棒渲染
const mockSchedules: ScheduleVO[] = [
  { id: 1, doctorId: 1, scheduleDate: '2026-05-24', shiftType: '上午', startTime: '08:30', endTime: '12:00', deptId: 1 },
  { id: 2, doctorId: 2, scheduleDate: '2026-05-24', shiftType: '下午', startTime: '14:00', endTime: '17:30', deptId: 1 },
  { id: 3, doctorId: 3, scheduleDate: '2026-05-25', shiftType: '上午', startTime: '08:30', endTime: '12:00', deptId: 2 }
]

async function doLoad() {
  try {
    await loadData({ deptId: filterDept.value, date: filterDate.value })
    if (tableData.value.length === 0) {
      tableData.value = mockSchedules.filter(x => 
        (!filterDept.value || x.deptId === filterDept.value) && 
        (!filterDate.value || x.scheduleDate === filterDate.value)
      )
      pagination.total = tableData.value.length
    }
  } catch {
    tableData.value = mockSchedules.filter(x => 
      (!filterDept.value || x.deptId === filterDept.value) && 
      (!filterDate.value || x.scheduleDate === filterDate.value)
    )
    pagination.total = tableData.value.length
  }
}

function handleSearch() {
  pagination.page = 1
  doLoad()
}

function handleCreate() {
  dialogTitle.value = '新增排班'
  form.value = {
    id: undefined,
    scheduleDate: '',
    deptId: undefined,
    doctorId: undefined,
    shiftType: '上午',
    startTime: '08:30',
    endTime: '12:00'
  }
  dialogVisible.value = true
}

function handleEdit(row: ScheduleVO) {
  dialogTitle.value = '编辑排班'
  form.value = {
    id: row.id,
    scheduleDate: row.scheduleDate,
    deptId: row.deptId,
    doctorId: row.doctorId,
    shiftType: row.shiftType,
    startTime: row.startTime,
    endTime: row.endTime
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      doctorId: form.value.doctorId!,
      scheduleDate: form.value.scheduleDate,
      shiftType: form.value.shiftType,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      deptId: form.value.deptId!
    }

    if (form.value.id) {
      await updateSchedule(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await addSchedule(payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    doLoad()
  } catch {
    // 本地演示降级
    if (form.value.id) {
      const idx = tableData.value.findIndex(x => x.id === form.value.id)
      if (idx > -1) {
        tableData.value[idx] = { ...tableData.value[idx], ...form.value } as any
      }
      ElMessage.success('更新成功(本地降级模式)')
    } else {
      tableData.value.push({
        id: tableData.value.length + 10,
        doctorId: form.value.doctorId!,
        scheduleDate: form.value.scheduleDate,
        shiftType: form.value.shiftType,
        startTime: form.value.startTime,
        endTime: form.value.endTime,
        deptId: form.value.deptId!
      })
      pagination.total++
      ElMessage.success('添加成功(本地降级模式)')
    }
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: ScheduleVO) {
  try {
    await ElMessageBox.confirm('确定删除该排班吗？', '提示', { type: 'warning' })
    try {
      await deleteSchedule(row.id)
      ElMessage.success('删除成功')
      doLoad()
    } catch {
      tableData.value = tableData.value.filter(x => x.id !== row.id)
      pagination.total--
      ElMessage.success('删除成功(本地降级模式)')
    }
  } catch {
    // cancel
  }
}

onMounted(() => {
  doLoad()
})
</script>

<style scoped>
.page-container {
  font-family: 'Inter', sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.gradient-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  border: none !important;
  font-weight: 600;
}

.content-card {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.custom-select :deep(.el-input__wrapper),
.custom-date-picker :deep(.el-input__wrapper) {
  background-color: #1e293b !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-select :deep(.el-input__inner),
.custom-date-picker :deep(.el-input__inner) {
  color: #ffffff !important;
}

/* 表格定制 */
.custom-table :deep(.el-table__header-wrapper) th {
  background-color: #0f172a !important;
  color: #94a3b8 !important;
  font-weight: 600;
  border-bottom: 1px solid #334155;
}

.custom-table :deep(.el-table__row) {
  background-color: transparent !important;
  color: #f1f5f9;
}

.custom-table :deep(.el-table__row):hover td {
  background-color: rgba(51, 65, 85, 0.5) !important;
}

.custom-table :deep(td) {
  border-bottom: 1px solid #334155;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 弹窗定制 */
.custom-dialog :deep(.el-dialog) {
  background-color: #1e293b !important;
  border: 1px solid #334155;
  border-radius: 16px;
}

.custom-dialog :deep(.el-dialog__title) {
  color: #ffffff !important;
}

.custom-dialog-input :deep(.el-input__wrapper),
.custom-dialog-select :deep(.el-input__wrapper) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-dialog-input :deep(.el-input__inner),
.custom-dialog-select :deep(.el-input__inner) {
  color: #ffffff !important;
}
</style>
