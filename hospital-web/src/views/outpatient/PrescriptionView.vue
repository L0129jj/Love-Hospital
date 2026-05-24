<template>
  <div class="prescription-container">
    <el-row :gutter="20" style="height: 100%">
      <!-- 左侧：已接诊患者列表 -->
      <el-col :span="8" style="height: 100%">
        <div class="content-card full-height">
          <h3 class="card-title">已接诊患者 (诊断中)</h3>
          <el-table 
            v-loading="loadingRegs" 
            :data="registrations" 
            highlight-current-row
            @current-change="handleSelectReg"
            style="width: 100%" 
            class="custom-table clickable-row"
          >
            <el-table-column prop="patientId" label="患者ID" width="100" />
            <el-table-column prop="visitType" label="类型" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.visitType === '初诊' ? 'primary' : 'success'">
                  {{ scope.row.visitType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="regTime" label="挂号时间">
              <template #default="scope">
                <span>{{ formatShortDate(scope.row.regTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 右侧：开具处方工作区 -->
      <el-col :span="16" style="height: 100%">
        <div class="content-card full-height relative">
          <h3 class="card-title">就诊处方开记台</h3>
          
          <div v-if="!selectedReg" class="empty-state">
            <el-icon size="48" color="#64748b"><Document /></el-icon>
            <p>请在左侧选择一位挂号患者开始接诊</p>
          </div>

          <div v-else class="prescription-form">
            <div class="patient-summary">
              <el-descriptions title="当前就诊患者信息" :column="2" border class="custom-descriptions">
                <el-descriptions-item label="患者ID">{{ selectedReg.patientId }}</el-descriptions-item>
                <el-descriptions-item label="就诊科室">{{ getDeptName(selectedReg.deptId) }}</el-descriptions-item>
                <el-descriptions-item label="接诊医生">{{ getDoctorName(selectedReg.doctorId) }}</el-descriptions-item>
                <el-descriptions-item label="就诊状态">
                  <el-tag type="warning">诊断中</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form-scroll">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="主诉症状" prop="symptoms">
                    <el-input 
                      v-model="form.symptoms" 
                      type="textarea" 
                      rows="3" 
                      placeholder="请详细描述患者主诉及现病史症状..." 
                      class="custom-textarea"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="临床诊断" prop="diagnosis">
                    <el-input 
                      v-model="form.diagnosis" 
                      type="textarea" 
                      rows="3" 
                      placeholder="请录入临床初步诊断结论..." 
                      class="custom-textarea"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="诊查费 (元)" prop="consultationFee">
                    <el-input-number 
                      v-model="form.consultationFee" 
                      :min="0" 
                      :precision="2" 
                      style="width: 100%"
                      class="custom-number"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 药品明细录入 -->
              <div class="drug-section">
                <div class="section-header">
                  <span class="section-title">开具药品明细</span>
                  <el-button type="primary" size="small" @click="addDrugRow">添加药品</el-button>
                </div>

                <div v-for="(item, index) in form.details" :key="index" class="drug-row">
                  <el-row :gutter="15" align="middle">
                    <el-col :span="6">
                      <el-select 
                        v-model="item.drugId" 
                        placeholder="选择药品" 
                        @change="(val: any) => handleDrugChange(val, index)"
                        class="custom-select"
                      >
                        <el-option v-for="d in drugStock" :key="d.id" :label="d.name" :value="d.id" />
                      </el-select>
                    </el-col>
                    <el-col :span="4">
                      <el-input-number 
                        v-model="item.quantity" 
                        :min="1" 
                        placeholder="数量" 
                        @change="calcSubtotal(index)"
                        style="width: 100%"
                        class="custom-number"
                      />
                    </el-col>
                    <el-col :span="4">
                      <el-input 
                        v-model="item.price" 
                        disabled 
                        placeholder="单价"
                        class="custom-disabled-input"
                      >
                        <template #append>元</template>
                      </el-input>
                    </el-col>
                    <el-col :span="6">
                      <el-input v-model="item.dosage" placeholder="用法用量 (例:一日三次)" class="custom-input" />
                    </el-col>
                    <el-col :span="4" class="subtotal-col">
                      <span>￥{{ item.subtotal.toFixed(2) }}</span>
                      <el-icon class="delete-icon" @click="removeDrugRow(index)"><Delete /></el-icon>
                    </el-col>
                  </el-row>
                </div>
              </div>
            </el-form>

            <!-- 底部提交栏 -->
            <div class="prescription-actions">
              <div class="total-summary">
                <span>总金额：</span>
                <span class="total-price">￥{{ calcTotalFee.toFixed(2) }}</span>
              </div>
              <el-button type="success" size="large" :loading="submitting" @click="handleSavePrescription">
                确认开具并发送处方
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Document, Delete } from '@element-plus/icons-vue'
import { getMyPatients, getRegPage, addPrescription } from '@/api/outpatient'
import { getAvailableDrugs } from '@/api/system'
import type { RegVO, PrescriptionDetailDTO } from '@/types/outpatient'
import type { DrugVO } from '@/types/system'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()
const loadingRegs = ref(false)
const registrations = ref<RegVO[]>([])
const selectedReg = ref<RegVO | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const doctorsList = ref<any[]>([])
const deptsList = ref<any[]>([])

// 模拟挂号列表
const mockRegs: RegVO[] = [
  { id: 1, patientId: 101, doctorId: 1, deptId: 1, regTime: '2026-05-23T10:00:00', visitType: '初诊', status: '待就诊' },
  { id: 2, patientId: 102, doctorId: 1, deptId: 1, regTime: '2026-05-23T11:15:00', visitType: '复诊', status: '待就诊' },
  { id: 3, patientId: 103, doctorId: 3, deptId: 2, regTime: '2026-05-23T14:30:00', visitType: '初诊', status: '待就诊' }
]

// 动态拉取可用药品
const drugStock = ref<DrugVO[]>([])

async function loadDrugs() {
  try {
    const res = await getAvailableDrugs()
    drugStock.value = res.data || []
  } catch {
    drugStock.value = [
      { id: 1, name: '阿莫西林胶囊', price: 18.5, stock: 99 },
      { id: 2, name: '布洛芬缓释胶囊', price: 24.0, stock: 50 },
      { id: 3, name: '对乙酰氨基酚片', price: 9.8, stock: 80 },
      { id: 4, name: '盐酸二甲双胍片', price: 32.5, stock: 40 }
    ]
  }
}

const form = ref({
  symptoms: '',
  diagnosis: '',
  consultationFee: 15.00,
  details: [] as (PrescriptionDetailDTO & { subtotal: number })[]
})

const rules = {
  symptoms: [{ required: true, message: '请录入主诉症状', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请录入初步诊断结论', trigger: 'blur' }]
}

function getDeptName(id: number) {
  const dept = deptsList.value.find(d => d.id === id)
  return dept ? dept.name : '通用诊室'
}

function getDoctorName(id: number) {
  const doc = doctorsList.value.find(d => d.id === id)
  return doc ? doc.name : '坐诊医生'
}

function formatShortDate(isoStr: string) {
  return isoStr.substring(11, 16)
}

function handleSelectReg(row: RegVO | null) {
  if (!row) return
  selectedReg.value = row
  form.value = {
    symptoms: '',
    diagnosis: '',
    consultationFee: 15.00,
    details: []
  }
}

function addDrugRow() {
  form.value.details.push({
    drugId: undefined as any,
    quantity: 1,
    price: 0,
    dosage: '一日三次，餐后服用',
    subtotal: 0
  })
}

function removeDrugRow(index: number) {
  form.value.details.splice(index, 1)
}

function handleDrugChange(drugId: number, index: number) {
  const d = drugStock.value.find(x => x.id === drugId)
  if (d) {
    form.value.details[index].price = d.price || 0
    calcSubtotal(index)
  }
}

function calcSubtotal(index: number) {
  const item = form.value.details[index]
  item.subtotal = (item.price || 0) * (item.quantity || 1)
}

const calcTotalFee = computed(() => {
  const drugTotal = form.value.details.reduce((sum, item) => sum + item.subtotal, 0)
  return drugTotal + form.value.consultationFee
})

async function handleSavePrescription() {
  if (!selectedReg.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (form.value.details.length === 0) {
    ElMessage.warning('开具的处方药品明细不能为空！')
    return
  }

  submitting.value = true
  try {
    const payload = {
      regId: selectedReg.value.id,
      doctorId: selectedReg.value.doctorId,
      patientId: selectedReg.value.patientId,
      symptoms: form.value.symptoms,
      diagnosis: form.value.diagnosis,
      consultationFee: form.value.consultationFee,
      details: form.value.details.map(d => ({
        drugId: d.drugId,
        quantity: d.quantity,
        price: d.price,
        dosage: d.dosage,
        subtotal: d.subtotal
      }))
    }

    await addPrescription(payload)
    ElMessage.success('电子处方已成功开具！')
    // 接诊成功后从列表移除
    registrations.value = registrations.value.filter(x => x.id !== selectedReg.value!.id)
    selectedReg.value = null
  } catch {
    ElMessage.error('电子处方开具失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loadingRegs.value = true
  try {
    await loadDrugs()
    
    // 动态拉取全量科室与医生字典数据
    try {
      const docRes = await request.get('/system/doctors/list', { params: { page: 1, size: 100 } })
      if (docRes && (docRes as any).records) {
        doctorsList.value = (docRes as any).records
      }
      const deptRes = await request.get<any, any>('/system/depts/all')
      if (deptRes && deptRes.data) {
        deptsList.value = deptRes.data
      } else {
        const deptList = await request.get('/system/depts/list')
        if (deptList) deptsList.value = deptList as any
      }
    } catch (err) {
      console.error('Failed to load dictionary data for mapping names:', err)
    }

    // 如果是医生登录，只拉取当前医生本人、并且在就诊中（已接诊）的患者！
    if (userStore.role === 'ROLE_DOCTOR' && userStore.userId) {
      const res = await getMyPatients(userStore.userId)
      if (res && res.data) {
        // 只保留就诊中的患者，这样已接诊的患者会立刻且自动更新到处方处！
        registrations.value = res.data.filter(r => r.status === '就诊中')
      } else {
        registrations.value = []
      }
    } else {
      // 管理员等非医生角色，依然使用通用拉取
      const res = await getRegPage({ status: '待就诊' })
      registrations.value = res.data?.records || [...mockRegs]
    }
  } catch (e) {
    console.error('Failed to load active registrations for prescription:', e)
    registrations.value = [...mockRegs]
  } finally {
    loadingRegs.value = false
  }
})
</script>

<style scoped>
.prescription-container {
  height: calc(100vh - 120px);
  font-family: 'Inter', sans-serif;
  color: #ffffff;
}

.content-card {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.full-height {
  height: 100%;
}

.relative {
  position: relative;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #f1f5f9;
}

.clickable-row :deep(.el-table__row) {
  cursor: pointer;
}

/* 处方工作区样式 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #64748b;
  gap: 15px;
}

.prescription-form {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.patient-summary {
  margin-bottom: 20px;
}

.custom-descriptions :deep(.el-descriptions__body) {
  background-color: transparent !important;
}

.custom-descriptions :deep(.el-descriptions__label) {
  background-color: #0f172a !important;
  color: #94a3b8 !important;
  border-color: #334155 !important;
}

.custom-descriptions :deep(.el-descriptions__content) {
  color: #f1f5f9 !important;
  border-color: #334155 !important;
  background-color: transparent !important;
}

.form-scroll {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
}

/* 药品明细录入部分 */
.drug-section {
  border-top: 1px dashed #334155;
  margin-top: 20px;
  padding-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #cbd5e1;
}

.drug-row {
  background-color: #0f172a;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
}

.subtotal-col {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #f59e0b;
}

.delete-icon {
  color: #ef4444;
  cursor: pointer;
  transition: transform 0.2s;
}

.delete-icon:hover {
  transform: scale(1.15);
}

/* 底部提交区 */
.prescription-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #334155;
  padding-top: 20px;
  margin-top: 15px;
}

.total-summary {
  font-size: 16px;
  color: #cbd5e1;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #f59e0b;
}

/* 输入控件定制 */
.custom-textarea :deep(.el-textarea__inner) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
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

.custom-disabled-input :deep(.el-input__wrapper) {
  background-color: #1e293b !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-disabled-input :deep(.el-input__inner) {
  color: #94a3b8 !important;
}

/* 表格样式定制 */
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
</style>
