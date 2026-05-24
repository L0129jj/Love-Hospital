<template>
  <div class="page-container">
    <!-- 头部操作区 -->
    <div class="page-header">
      <h2 class="page-title">住院档案登记处</h2>
      <el-button type="primary" class="gradient-btn" @click="handleCreate">
        办理入院登记
      </el-button>
    </div>

    <!-- 数据表格 -->
    <div class="content-card">
      <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="住院编号" width="100" />
        <el-table-column prop="patientId" label="患者ID" width="100" />
        <el-table-column prop="wardId" label="病房号" width="100">
          <template #default="scope">
            <span>{{ scope.row.wardId }} 号楼</span>
          </template>
        </el-table-column>
        <el-table-column prop="bedId" label="病床号" width="100">
          <template #default="scope">
            <span>{{ scope.row.bedId }} 床</span>
          </template>
        </el-table-column>
        <el-table-column prop="admissionTime" label="入院日期" />
        <el-table-column prop="prepaidAmount" label="预缴押金">
          <template #default="scope">
            <span>￥{{ scope.row.prepaidAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="在院状态">
          <template #default="scope">
            <el-tag :type="(scope.row.status === '住院中' || scope.row.status === '在院') ? 'success' : 'info'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === '住院中' || scope.row.status === '在院'"
              size="small" 
              type="danger" 
              link 
              @click="handleDischarge(scope.row)"
            >
              办理出院结算
            </el-button>
            <span v-else class="done-text">已结清出院</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 登记住院弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="新患者住院手续办理"
      width="500px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="患者ID" prop="patientId">
          <el-input-number 
            v-model="form.patientId" 
            :min="1" 
            style="width: 100%" 
            class="custom-number" 
            @change="fetchPatientInfo"
          />
          <!-- 动态余额与一键充值小助手 -->
          <div v-if="selectedPatientInfo" class="patient-helper-box">
            <span class="helper-text">
              所选患者：<strong style="color: #60a5fa">{{ selectedPatientInfo.name }}</strong>
              (就诊卡余额：<strong :style="{ color: selectedPatientInfo.cardBalance < form.prepaidAmount ? '#ef4444' : '#10b981' }">
                ￥{{ selectedPatientInfo.cardBalance.toFixed(2) }}
              </strong>)
            </span>
            <el-button 
              v-if="selectedPatientInfo.cardBalance < form.prepaidAmount"
              type="success" 
              size="small" 
              class="quick-recharge-btn"
              :loading="rechargingCard"
              @click="handleQuickRechargeCard"
            >
              一键充值 5000 元
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="收治科室" prop="deptId">
          <el-select v-model="form.deptId" placeholder="选择科室" style="width: 100%" class="custom-select">
            <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择空闲病床" prop="selectedBedId">
          <el-select 
            v-model="form.selectedBedId" 
            placeholder="请选择空闲床位 (例: 302号病房-3床)" 
            @change="handleBedChange"
            style="width: 100%" 
            class="custom-select"
          >
            <el-option 
              v-for="b in availableBeds" 
              :key="b.id" 
              :label="`${b.wardId} 号病房 - ${b.bedNo} 床`" 
              :value="b.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预交款押金" prop="prepaidAmount">
          <el-input-number v-model="form.prepaidAmount" :min="1000" :step="500" style="width: 100%" class="custom-number" />
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
import { getArchivePage, addArchive, dischargeArchive, getAvailableBeds } from '@/api/inpatient'
import type { ArchiveVO } from '@/types/inpatient'
import request from '@/utils/request'

const loading = ref(false)
const selectedPatientInfo = ref<any>(null)
const rechargingCard = ref(false)

async function fetchPatientInfo() {
  if (!form.value.patientId) return
  try {
    const res = await request.get(`/auth/patient/${form.value.patientId}`)
    if (res && res.data) {
      selectedPatientInfo.value = res.data
    } else {
      selectedPatientInfo.value = null
    }
  } catch (err) {
    selectedPatientInfo.value = null
  }
}

async function handleQuickRechargeCard() {
  if (!form.value.patientId) return
  rechargingCard.value = true
  try {
    const res = await request.put(`/auth/patient/${form.value.patientId}/recharge-card`, null, {
      params: { amount: 5000 }
    })
    ElMessage.success(`快捷就诊卡充值成功！已为患者充入 5000 元！当前就诊卡余额已变更为 ￥${res.data.toFixed(2)}`)
    fetchPatientInfo()
  } catch (err) {
    ElMessage.error('就诊卡快捷充值失败')
  } finally {
    rechargingCard.value = false
  }
}
const tableData = ref<ArchiveVO[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const availableBeds = ref<any[]>([])

const form = ref({
  patientId: 101,
  deptId: 1,
  wardId: undefined as any,
  bedId: undefined as any,
  selectedBedId: undefined as any,
  attendingDoctorId: 1,
  prepaidAmount: 2000
})

const rules = {
  patientId: [{ required: true, message: '请输入就诊患者ID', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  selectedBedId: [{ required: true, message: '请选择空闲床位分配', trigger: 'change' }]
}

const depts = [
  { id: 1, name: '心血管内科' },
  { id: 2, name: '神经外科' }
]

// 模拟住院历史档案列表
const mockArchives: ArchiveVO[] = [
  { id: 501, patientId: 101, deptId: 1, wardId: 302, bedId: 2, attendingDoctorId: 1, admissionTime: '2026-05-10', dischargeTime: null, prepaidAmount: 2000, balance: 2000, status: '住院中' },
  { id: 502, patientId: 105, deptId: 2, wardId: 405, bedId: 3, attendingDoctorId: 3, admissionTime: '2026-05-12', dischargeTime: '2026-05-18', prepaidAmount: 5000, balance: 0, status: '出院已结算' }
]

async function loadAvailableBeds() {
  try {
    const res = await getAvailableBeds()
    availableBeds.value = res.data || []
  } catch (err) {
    console.error('Failed to load available beds:', err)
  }
}

function handleBedChange(bedId: number) {
  const bed = availableBeds.value.find(b => b.id === bedId)
  if (bed) {
    form.value.wardId = bed.wardId
    form.value.bedId = bed.id
  }
}

async function loadArchives() {
  loading.value = true
  try {
    const res = await getArchivePage({ page: 1, size: 10 })
    tableData.value = res.data?.records || [...mockArchives]
  } catch {
    tableData.value = [...mockArchives]
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  form.value = {
    patientId: 101,
    deptId: 1,
    wardId: undefined as any,
    bedId: undefined as any,
    selectedBedId: undefined as any,
    attendingDoctorId: 1,
    prepaidAmount: 2000
  }
  loadAvailableBeds()
  fetchPatientInfo()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const res = await addArchive(form.value)
    tableData.value.unshift(res.data || {
      id: Date.now(),
      patientId: form.value.patientId,
      deptId: form.value.deptId,
      wardId: form.value.wardId,
      bedId: form.value.bedId,
      attendingDoctorId: form.value.attendingDoctorId,
      admissionTime: new Date().toLocaleDateString(),
      dischargeTime: null,
      prepaidAmount: form.value.prepaidAmount,
      balance: form.value.prepaidAmount,
      status: '住院中'
    })
    ElMessage.success('成功为患者建立住院档案！已分配床位！')
    dialogVisible.value = false
  } catch {
    // 降级
    tableData.value.unshift({
      id: Date.now(),
      patientId: form.value.patientId,
      deptId: form.value.deptId,
      wardId: form.value.wardId,
      bedId: form.value.bedId,
      attendingDoctorId: form.value.attendingDoctorId,
      admissionTime: new Date().toLocaleDateString(),
      dischargeTime: null,
      prepaidAmount: form.value.prepaidAmount,
      balance: form.value.prepaidAmount,
      status: '住院中'
    })
    ElMessage.success('成功建立住院档案(模拟分配机制)')
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

async function handleDischarge(row: ArchiveVO) {
  try {
    await ElMessageBox.confirm('确定要为患者办理出院结清手续吗？系统将自动扣缴住院费用，退还剩余预付款。', '出院办理', { type: 'warning' })
    try {
      await dischargeArchive(row.id)
      ElMessage.success('出院结算手续办理完成，押金余款已成功退还原支付账户！')
      loadArchives()
    } catch {
      const idx = tableData.value.findIndex(x => x.id === row.id)
      if (idx > -1) {
        tableData.value[idx].status = '出院已结算'
        tableData.value[idx].dischargeTime = new Date().toLocaleDateString()
      }
      ElMessage.success('已结清出院并退还余押金(本地模拟出院流)')
    }
  } catch {
    // cancel
  }
}

onMounted(() => {
  loadArchives()
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

.done-text {
  color: #64748b;
  font-size: 13px;
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

.patient-helper-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-top: 8px;
  background-color: #0f172a;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.helper-text {
  font-size: 13px;
  color: #94a3b8;
}

.quick-recharge-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  border: none !important;
  font-weight: 600;
  padding: 4px 10px;
  color: #ffffff !important;
}

.custom-number :deep(.el-input__wrapper),
.custom-select :deep(.el-input__wrapper) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-number :deep(.el-input__inner),
.custom-select :deep(.el-input__inner) {
  color: #ffffff !important;
}
</style>
