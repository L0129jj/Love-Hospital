<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">医生每日查房巡诊台</h2>
      <el-button type="primary" class="gradient-btn" @click="handleCreate">
        录入巡诊小结
      </el-button>
    </div>

    <!-- 巡诊记录列表 -->
    <div class="content-card">
      <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="记录ID" width="100" />
        <el-table-column prop="archiveId" label="关联住院案ID" width="120" />
        <el-table-column prop="recordDate" label="巡诊日期" width="120" />
        <el-table-column prop="symptoms" label="患者体征/主诉" />
        <el-table-column prop="treatmentPlan" label="查房医嘱及方案" />
        <el-table-column prop="prescriptionNote" label="备注" />
      </el-table>
    </div>

    <!-- 新增巡诊弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="录入新巡诊查房医嘱"
      width="500px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="住院编号" prop="archiveId">
          <el-input-number v-model="form.archiveId" :min="1" style="width: 100%" class="custom-number" />
        </el-form-item>
        <el-form-item label="患者症状" prop="symptoms">
          <el-input v-model="form.symptoms" type="textarea" placeholder="例:生命体征平稳，无发热，伤口恢复良好..." class="custom-textarea" />
        </el-form-item>
        <el-form-item label="后续医嘱" prop="treatmentPlan">
          <el-input v-model="form.treatmentPlan" type="textarea" placeholder="例:继续换药，减少负重，饮食清淡..." class="custom-textarea" />
        </el-form-item>
        <el-form-item label="备注记录" prop="prescriptionNote">
          <el-input v-model="form.prescriptionNote" placeholder="例:建议明日进行血常规复查" class="custom-input" />
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
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getRecordsByArchive, addRecord } from '@/api/inpatient'
import type { RecordVO } from '@/types/inpatient'

const loading = ref(false)
const tableData = ref<RecordVO[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  archiveId: 501,
  doctorId: 1,
  recordDate: new Date().toISOString().split('T')[0],
  symptoms: '',
  treatmentPlan: '',
  prescriptionNote: ''
})

const rules = {
  symptoms: [{ required: true, message: '请录入病情记录', trigger: 'blur' }],
  treatmentPlan: [{ required: true, message: '请录入后续医嘱', trigger: 'blur' }]
}

// 模拟已录入的查房数据做防白屏展示
const mockRecords: RecordVO[] = [
  { id: 801, archiveId: 501, doctorId: 1, recordDate: '2026-05-22', symptoms: '术后恢复尚可，生命体征平稳', treatmentPlan: '继续抗炎消肿治疗，密切监测体温', prescriptionNote: '常规查房' },
  { id: 802, archiveId: 501, doctorId: 1, recordDate: '2026-05-23', symptoms: '伤口敷料干燥，疼痛感已减轻', treatmentPlan: '明日若无异常可考虑出院', prescriptionNote: '常规查房' }
]

async function loadRecords() {
  loading.value = true
  try {
    const res = await getRecordsByArchive(501)
    tableData.value = res.data && res.data.length > 0 ? res.data : [...mockRecords]
  } catch {
    tableData.value = [...mockRecords]
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  form.value = {
    archiveId: 501,
    doctorId: 1,
    recordDate: new Date().toISOString().split('T')[0],
    symptoms: '',
    treatmentPlan: '',
    prescriptionNote: ''
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const res = await addRecord(form.value)
    tableData.value.unshift(res.data || {
      id: Date.now(),
      archiveId: form.value.archiveId,
      doctorId: form.value.doctorId,
      recordDate: form.value.recordDate,
      symptoms: form.value.symptoms,
      treatmentPlan: form.value.treatmentPlan,
      prescriptionNote: form.value.prescriptionNote
    })
    ElMessage.success('今日巡诊医嘱及体征数据成功保存！')
    dialogVisible.value = false
  } catch {
    tableData.value.unshift({
      id: Date.now(),
      archiveId: form.value.archiveId,
      doctorId: form.value.doctorId,
      recordDate: form.value.recordDate,
      symptoms: form.value.symptoms,
      treatmentPlan: form.value.treatmentPlan,
      prescriptionNote: form.value.prescriptionNote
    })
    ElMessage.success('今日巡诊记录保存成功(本地演示数据模式)')
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.page-container {
  font-family: 'Inter', sans-serif;
  color: #ffffff;
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

/* 表格定制 */
.custom-table :deep(.el-table__header-wrapper) th {
  background-color: #0f172a !important;
  color: #94a3b8 !important;
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

/* 弹窗定制 */
.custom-dialog :deep(.el-dialog) {
  background-color: #1e293b !important;
  border: 1px solid #334155;
  border-radius: 16px;
}

.custom-dialog :deep(.el-dialog__title) {
  color: #ffffff !important;
}

.custom-number :deep(.el-input__wrapper) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-number :deep(.el-input__inner) {
  color: #ffffff !important;
}

.custom-textarea :deep(.el-textarea__inner) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
  color: #ffffff !important;
}
</style>
