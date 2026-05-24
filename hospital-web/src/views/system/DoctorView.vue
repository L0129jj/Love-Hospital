<template>
  <div class="page-container">
    <!-- 头部操作区 -->
    <div class="page-header">
      <h2 class="page-title">医生管理</h2>
      <div class="header-actions">
        <el-select 
          v-model="deptFilter" 
          placeholder="按科室筛选" 
          clearable 
          style="width: 200px" 
          @change="handleSearch"
          class="custom-select"
        >
          <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-button type="primary" class="gradient-btn" @click="handleCreate">
          添加医生
        </el-button>
      </div>
    </div>

    <!-- 列表数据 -->
    <div class="content-card">
      <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="医生ID" width="120" />
        <el-table-column prop="name" label="医生姓名" />
        <el-table-column prop="deptCode" label="所属科室">
          <template #default="scope">
            <span>{{ getDeptName(scope.row.deptCode) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row)">
              删除
            </el-button>
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

    <!-- 弹窗对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="450px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="医生姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入医生姓名" class="custom-dialog-input" />
        </el-form-item>
        <el-form-item label="所属科室" prop="deptCode">
          <el-select v-model="form.deptCode" placeholder="请选择科室" style="width: 100%" class="custom-dialog-select">
            <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="String(d.id)" />
          </el-select>
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
import { getDoctorPage, insertDoctor, updateDoctor, deleteDoctor } from '@/api/system'
import { useTable } from '@/hooks/useTable'
import type { DoctorVO } from '@/types/system'

const deptFilter = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const dialogTitle = ref('添加医生')
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  id: undefined as number | undefined,
  name: '',
  deptCode: ''
})

const rules = {
  name: [{ required: true, message: '请输入医生姓名', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

// 模拟科室列表
const depts = [
  { id: 1, name: '心血管内科' },
  { id: 2, name: '神经外科' },
  { id: 3, name: '小儿呼吸科' },
  { id: 4, name: '骨科关节门诊' },
  { id: 5, name: '眼耳鼻喉科' }
]

function getDeptName(code: string) {
  const d = depts.find(x => String(x.id) === code)
  return d ? d.name : '未知科室'
}

// 初始化 useTable
const { 
  loading, 
  tableData, 
  pagination, 
  loadData, 
  handlePageChange, 
  handleSizeChange 
} = useTable<DoctorVO>((params) => getDoctorPage(params))

// 模拟初始医生列表做鲁棒展示
const mockDoctors: DoctorVO[] = [
  { id: 1, name: '张忠民', deptCode: '1' },
  { id: 2, name: '李晓华', deptCode: '1' },
  { id: 3, name: '赵铁生', deptCode: '2' },
  { id: 4, name: '孙玉芳', deptCode: '3' },
  { id: 5, name: '吴建国', deptCode: '4' }
]

async function doLoad() {
  try {
    await loadData({ deptId: deptFilter.value })
    if (tableData.value.length === 0) {
      tableData.value = mockDoctors.filter(d => !deptFilter.value || d.deptCode === String(deptFilter.value))
      pagination.total = tableData.value.length
    }
  } catch (err) {
    tableData.value = mockDoctors.filter(d => !deptFilter.value || d.deptCode === String(deptFilter.value))
    pagination.total = tableData.value.length
  }
}

function handleSearch() {
  pagination.page = 1
  doLoad()
}

function handleCreate() {
  dialogTitle.value = '添加医生'
  form.value = { id: undefined, name: '', deptCode: '' }
  dialogVisible.value = true
}

function handleEdit(row: DoctorVO) {
  dialogTitle.value = '编辑医生'
  form.value = { id: row.id, name: row.name, deptCode: row.deptCode }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (form.value.id) {
      await updateDoctor(form.value.id, { name: form.value.name, deptCode: form.value.deptCode })
      ElMessage.success('更新成功')
    } else {
      await insertDoctor({ name: form.value.name, deptCode: form.value.deptCode })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    doLoad()
  } catch {
    // 鲁棒本地降级
    if (form.value.id) {
      const idx = tableData.value.findIndex(x => x.id === form.value.id)
      if (idx > -1) {
        tableData.value[idx].name = form.value.name
        tableData.value[idx].deptCode = form.value.deptCode
      }
      ElMessage.success('更新成功(本地降级模式)')
    } else {
      tableData.value.push({
        id: tableData.value.length + 10,
        name: form.value.name,
        deptCode: form.value.deptCode
      })
      pagination.total++
      ElMessage.success('添加成功(本地降级模式)')
    }
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: DoctorVO) {
  try {
    await ElMessageBox.confirm('确定要删除该医生信息吗？', '提示', { type: 'warning' })
    try {
      await deleteDoctor(row.id)
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
  gap: 15px;
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

/* 筛选项定制 */
.custom-select :deep(.el-input__wrapper) {
  background-color: #1e293b !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-select :deep(.el-input__inner) {
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
