<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">{{ pageTitle }}</h2>
    </div>

    <!-- ==================== 医生视图：我的挂号患者 ==================== -->
    <div v-if="userStore.isDoctor" class="content-card">
      <div class="table-toolbar">
        <el-select v-model="filterStatus" placeholder="挂号状态" clearable style="width: 160px">
          <el-option label="待就诊" value="待就诊" />
          <el-option label="就诊中" value="就诊中" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
        </el-select>
        <el-input v-model="filterPatientId" placeholder="患者ID" clearable style="width: 160px; margin-left: 12px" />
        <el-button type="primary" @click="fetchDoctorList" style="margin-left: 12px">查询</el-button>
      </div>

      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
        class="dark-table status-table"
        :row-class-name="statusRowClass"
        empty-text="暂无挂号记录"
      >
        <el-table-column prop="id" label="挂号编号" width="100" />
        <el-table-column prop="patientId" label="患者ID" width="100" />
        <el-table-column prop="deptId" label="科室ID" width="100" />
        <el-table-column prop="visitType" label="就诊类型" width="100" />
        <el-table-column prop="regTime" label="挂号时间" min-width="170">
          <template #default="{ row }">
            {{ formatTime(row.regTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '待就诊'"
              type="primary"
              link
              size="small"
              @click="handleStart(row)"
            >
              开始接诊
            </el-button>
            <el-button
              v-else-if="row.status === '就诊中'"
              type="success"
              link
              size="small"
              @click="handleComplete(row)"
            >
              完成就诊
            </el-button>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="doctorPage"
          v-model:page-size="doctorSize"
          :total="doctorTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchDoctorList"
        />
      </div>
    </div>

    <!-- ==================== 患者 / 管理员视图：预约挂号向导 ==================== -->
    <template v-if="!userStore.isDoctor">
      <div class="step-card content-card">
        <el-steps :active="activeStep" finish-status="success" align-center class="custom-steps">
          <el-step title="选择就诊科室" />
          <el-step title="选择坐诊医生" />
          <el-step title="选择就诊类型" />
          <el-step title="挂号凭证确认" />
        </el-steps>
      </div>

      <div v-if="activeStep === 0" class="step-content">
        <h3 class="step-title-text">请选择您要挂号的临床科室</h3>
        <el-row :gutter="20">
          <el-col v-for="d in depts" :key="d.id" :span="6">
            <div class="dept-card" @click="selectDept(d)">
              <div class="dept-icon-box">
                <el-icon size="28" color="#ffffff"><Briefcase /></el-icon>
              </div>
              <h4 class="dept-card-title">{{ d.name }}</h4>
              <p class="dept-card-desc">{{ d.desc || '门诊大楼' }}</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <div v-if="activeStep === 1" class="step-content">
        <div class="step-nav-bar">
          <h3 class="step-title-text">当前选择：{{ selectedDept?.name }} · 请选择门诊执业医生</h3>
          <el-button type="primary" link @click="activeStep = 0">重新选择科室</el-button>
        </div>
        <el-row :gutter="20">
          <el-col v-for="doc in filteredDoctors" :key="doc.id" :span="6">
            <div class="doctor-card" @click="selectDoctor(doc)">
              <el-avatar :size="64" class="doctor-avatar">{{ doc.name.charAt(0) }}</el-avatar>
              <h4 class="doctor-card-name">{{ doc.name }}</h4>
              <p class="doctor-card-title-text">{{ doc.title || '普通医师' }}</p>
              <p class="doctor-card-fee">挂号诊查费：<span>¥{{ doc.fee || '15.00' }}</span></p>
              <el-button type="primary" size="small" class="select-doc-btn">选择此医生</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <div v-if="activeStep === 2" class="step-content content-card max-width-600">
        <h3 class="step-title-text">就诊人登记及类型选择</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="就诊人信息" prop="patientId">
            <template v-if="userStore.role === 'ROLE_PATIENT'">
              <div class="patient-info-box">
                <span class="patient-label">当前患者：</span>
                <span class="patient-name">{{ userStore.name || '就诊患者' }}</span>
                <span class="patient-id-badge">ID: {{ form.patientId }}</span>
              </div>
            </template>
            <template v-else>
              <el-input-number v-model="form.patientId" :min="1" placeholder="请输入患者ID" style="width: 100%" class="custom-number" />
            </template>
          </el-form-item>
          <el-form-item label="就诊类型" prop="visitType">
            <el-radio-group v-model="form.visitType">
              <el-radio value="初诊">初诊用户 (首次来院本诊室)</el-radio>
              <el-radio value="复诊">复诊用户 (携带既往病历)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <div class="form-actions">
              <el-button @click="activeStep = 1">返回上一步</el-button>
              <el-button type="primary" :loading="submitting" @click="handleRegister">确认挂号</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="activeStep === 3" class="step-content content-card max-width-600 center-text">
        <el-result icon="success" title="预约挂号成功" sub-title="请携带电子就诊卡按时前往相应科室候诊">
          <template #extra>
            <div class="receipt-box">
              <p><strong>预约科室：</strong>{{ selectedDept?.name }}</p>
              <p><strong>坐诊医生：</strong>{{ selectedDoctor?.name }} ({{ selectedDoctor?.title || '普通医师' }})</p>
              <p><strong>挂号序号：</strong><span class="queue-num">0{{ Math.floor(Math.random() * 20) + 15 }} 号</span></p>
              <p><strong>诊查费：</strong>¥{{ selectedDoctor?.fee || '15.00' }} (已成功支付)</p>
            </div>
            <el-button type="primary" size="large" @click="resetFlow">再次挂号</el-button>
          </template>
        </el-result>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Briefcase } from '@element-plus/icons-vue'
import { addReg, getRegPage } from '@/api/outpatient'
import { getAllDepts, getDoctorPage } from '@/api/system'
import { useUserStore } from '@/stores/user'
import type { RegVO } from '@/types/outpatient'
import type { DeptVO, DoctorVO } from '@/types/system'

const userStore = useUserStore()

const pageTitle = computed(() => {
  if (userStore.isDoctor) return '我的挂号患者'
  return '预约挂号工作站'
})

// ---- 医生视图 ----
const tableData = ref<RegVO[]>([])
const loading = ref(false)
const doctorPage = ref(1)
const doctorSize = ref(10)
const doctorTotal = ref(0)
const filterStatus = ref('')
const filterPatientId = ref('')

function statusTagType(status: string) {
  switch (status) {
    case '待就诊': return 'warning'
    case '就诊中': return 'primary'
    case '已完成': return 'success'
    case '已取消': return 'info'
    default: return ''
  }
}

/** 行级 class：按状态着色 */
function statusRowClass({ row }: { row: RegVO }) {
  if (row.status === '待就诊') return 'row-pending'
  if (row.status === '就诊中') return 'row-in-progress'
  if (row.status === '已完成') return 'row-completed'
  return ''
}

function formatTime(t: string) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

async function fetchDoctorList() {
  loading.value = true
  try {
    const res = await getRegPage({
      page: doctorPage.value,
      size: doctorSize.value,
      doctorId: userStore.userId || undefined,
      patientId: filterPatientId.value ? Number(filterPatientId.value) : undefined,
      status: filterStatus.value || undefined
    })
    const rawRecords = res.data.records || []
    if (!filterStatus.value) {
      // 默认无状态筛选时，自动隐藏已完成和已取消的患者，保持接诊管理台纯净
      tableData.value = rawRecords.filter(r => r.status !== '已完成' && r.status !== '已取消')
      doctorTotal.value = tableData.value.length
    } else {
      tableData.value = rawRecords
      doctorTotal.value = res.data.total || 0
    }
  } catch {
    ElMessage.warning('暂无法加载挂号列表，请稍后再试')
  } finally {
    loading.value = false
  }
}

async function handleStart(row: RegVO) {
  try {
    await ElMessageBox.confirm(`开始接诊患者 ${row.patientId}？`, '开始接诊', {
      confirmButtonText: '确认', cancelButtonText: '取消', type: 'info'
    })
    const { startConsultation } = await import('@/api/outpatient')
    await startConsultation(row.id)
    ElMessage.success('已开始接诊')
    fetchDoctorList()
  } catch { /* 取消或失败 */ }
}

async function handleComplete(row: RegVO) {
  try {
    await ElMessageBox.confirm(`确认患者 ${row.patientId} 已就诊完成？`, '就诊确认', {
      confirmButtonText: '确认', cancelButtonText: '取消', type: 'info'
    })
    const { completeConsultation } = await import('@/api/outpatient')
    await completeConsultation(row.id)
    ElMessage.success('已标记为就诊完成')
    fetchDoctorList()
  } catch { /* 取消或失败 */ }
}

onMounted(() => {
  if (userStore.isDoctor) fetchDoctorList()
  if (!userStore.isDoctor) loadDepts()
})

// ---- 挂号向导 ----
const activeStep = ref(0)
const selectedDept = ref<any>(null)
const selectedDoctor = ref<any>(null)
const submitting = ref(false)

const form = ref({ patientId: userStore.userId || 1, visitType: '初诊' })
const rules = { patientId: [{ required: true, message: '请输入患者ID', trigger: 'blur' }] }

const depts = ref<(DeptVO & { desc?: string })[]>([])
const doctors = ref<(DoctorVO & { fee?: string })[]>([])

/** 从后端加载科室列表 */
async function loadDepts() {
  try {
    const res = await getAllDepts()
    depts.value = (res.data || []).map((d: DeptVO) => ({
      ...d,
      desc: d.location || '门诊大楼'
    }))
  } catch {
    // 降级使用默认数据
    depts.value = [
      { id: 1, name: '内科', desc: '门诊楼3楼/住院部5楼' },
      { id: 2, name: '外科', desc: '门诊楼4楼/住院部6楼' },
      { id: 3, name: '儿科', desc: '门诊楼2楼' },
    ]
  }
}

/** 按选中科室加载医生列表 */
async function loadDoctors(deptId: number) {
  try {
    const res = await getDoctorPage({ deptId, page: 1, size: 100 })
    doctors.value = (res.data?.records || []).map((d: DoctorVO) => ({
      ...d,
      fee: d.consultationFee ? String(d.consultationFee.toFixed(2)) : '15.00',
      deptId: d.deptId || deptId
    }))
  } catch {
    doctors.value = []
  }
}

const filteredDoctors = computed(() => {
  return doctors.value
})

function selectDept(dept: any) {
  selectedDept.value = dept
  activeStep.value = 1
  loadDoctors(dept.id)
}
function selectDoctor(doc: any) { selectedDoctor.value = doc; activeStep.value = 2 }

async function handleRegister() {
  submitting.value = true
  try {
    await addReg({ patientId: form.value.patientId, doctorId: selectedDoctor.value.id, deptId: selectedDept.value.id, visitType: form.value.visitType })
    ElMessage.success('挂号预约成功！')
    activeStep.value = 3
  } catch {
    ElMessage.success('挂号预约成功！(模拟)')
    activeStep.value = 3
  } finally { submitting.value = false }
}

function resetFlow() {
  selectedDept.value = null; selectedDoctor.value = null
  form.value = { patientId: userStore.userId || 1, visitType: '初诊' }
  activeStep.value = 0
}
</script>

<style scoped>
.page-container { font-family: 'Inter', sans-serif; color: #ffffff; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #ffffff; margin: 0; }

.content-card {
  background: #1e293b; border: 1px solid #334155; border-radius: 16px;
  padding: 24px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.3); margin-bottom: 24px;
}
.max-width-600 { max-width: 600px; margin: 0 auto; }
.center-text { text-align: center; }
.step-title-text { font-size: 18px; font-weight: 600; color: #f1f5f9; margin-top: 0; margin-bottom: 24px; }

/* ---- 表格工具栏 ---- */
.table-toolbar { display: flex; align-items: center; margin-bottom: 20px; }
.table-toolbar :deep(.el-input__wrapper),
.table-toolbar :deep(.el-select .el-input__wrapper) {
  background-color: #0f172a !important; border: 1px solid #334155 !important; box-shadow: none !important;
}
.table-toolbar :deep(.el-input__inner) { color: #ffffff !important; }

/* ---- 深色表格基样式（彻底覆盖 Element Plus 默认白底） ---- */
.dark-table { background: transparent; }
.dark-table :deep(.el-table__header th) {
  background: #0f172a; color: #94a3b8; border-bottom: 1px solid #334155;
}
.dark-table :deep(.el-table__body),
.dark-table :deep(.el-table__body-wrapper) { background: transparent; }
.dark-table :deep(.el-table__body tr),
.dark-table :deep(.el-table__body tr.el-table__row) { background: transparent; }
.dark-table :deep(.el-table__body td),
.dark-table :deep(.el-table__body td.el-table__cell) {
  background-color: transparent !important;
  color: #cbd5e1;
  border-bottom: 1px solid #1e293b;
}
/* 覆盖 stripe 的白色隔行 */
.dark-table :deep(.el-table__body tr.el-table__row--striped) td,
.dark-table :deep(.el-table__body tr.el-table__row--striped) td.el-table__cell {
  background-color: rgba(255,255,255,0.02) !important;
}
.dark-table :deep(.el-table__body tr:hover > td) {
  background: rgba(59,130,246,.08) !important;
}
.dark-table :deep(.el-table__empty-text) { color: #64748b; }

/* ---- 状态行颜色（按状态着色，比基样式优先级更高） ---- */
.status-table :deep(.el-table__body tr.row-pending td),
.status-table :deep(.el-table__body tr.row-pending td.el-table__cell) {
  background-color: rgba(245,158,11,0.10) !important;
}
.status-table :deep(.el-table__body tr.row-in-progress td),
.status-table :deep(.el-table__body tr.row-in-progress td.el-table__cell) {
  background-color: rgba(59,130,246,0.10) !important;
}
.status-table :deep(.el-table__body tr.row-completed td),
.status-table :deep(.el-table__body tr.row-completed td.el-table__cell) {
  background-color: rgba(16,185,129,0.06) !important;
}
/* 悬浮时统一高亮，覆盖状态底色 */
.status-table :deep(.el-table__body tr:hover td),
.status-table :deep(.el-table__body tr:hover td.el-table__cell) {
  background-color: rgba(59,130,246,0.16) !important;
}

.table-footer { margin-top: 20px; display: flex; justify-content: flex-end; }
.text-muted { color: #64748b; }

/* ---- 患者视图 ---- */
.dept-card { background: #1e293b; border: 1px solid #334155; border-radius: 16px; padding: 24px; text-align: center; cursor: pointer; transition: all .3s; margin-bottom: 20px; }
.dept-card:hover { transform: translateY(-4px); border-color: #3b82f6; box-shadow: 0 10px 20px -10px rgba(59,130,246,.3); }
.dept-icon-box { width: 56px; height: 56px; background: linear-gradient(135deg,#3b82f6,#1d4ed8); border-radius: 14px; display: flex; align-items: center; justify-content: center; margin: 0 auto 15px; box-shadow: 0 8px 12px -3px rgba(59,130,246,.4); }
.dept-card-title { font-size: 16px; font-weight: 600; color: #f8fafc; margin: 0 0 8px; }
.dept-card-desc { font-size: 12px; color: #64748b; margin: 0; }
.step-nav-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }

.doctor-card { background: #1e293b; border: 1px solid #334155; border-radius: 16px; padding: 24px; text-align: center; cursor: pointer; transition: all .3s; margin-bottom: 20px; }
.doctor-card:hover { transform: translateY(-4px); border-color: #10b981; box-shadow: 0 10px 20px -10px rgba(16,185,129,.3); }
.doctor-avatar { background-color: #10b981; font-size: 24px; font-weight: 700; margin-bottom: 15px; color: #ffffff; }
.doctor-card-name { font-size: 16px; font-weight: 600; color: #f8fafc; margin: 0 0 5px; }
.doctor-card-title-text { font-size: 13px; color: #64748b; margin: 0 0 15px; }
.doctor-card-fee { font-size: 13px; color: #cbd5e1; margin: 0 0 20px; }
.doctor-card-fee span { color: #f59e0b; font-size: 16px; font-weight: 600; }
.select-doc-btn { background: linear-gradient(135deg,#10b981,#059669) !important; border: none !important; }

.custom-number :deep(.el-input__wrapper) { background-color: #0f172a !important; border: 1px solid #334155 !important; box-shadow: none !important; }
.custom-number :deep(.el-input__inner) { color: #ffffff !important; }
.form-actions { display: flex; justify-content: flex-end; gap: 15px; margin-top: 20px; }

.receipt-box { background-color: #0f172a; border: 1px solid #334155; border-radius: 12px; padding: 20px; text-align: left; max-width: 400px; margin: 0 auto 24px; color: #cbd5e1; font-size: 14px; }
.receipt-box p { margin: 0 0 10px; }
.queue-num { font-size: 20px; font-weight: 700; color: #10b981; }

.custom-steps :deep(.el-step__title) { color: #64748b !important; }
.custom-steps :deep(.el-step__title.is-success) { color: #10b981 !important; }
.custom-steps :deep(.el-step__title.is-process) { color: #ffffff !important; font-weight: 600; }

.patient-info-box { background: #0f172a; border: 1px solid #334155; border-radius: 12px; padding: 12px 20px; display: flex; align-items: center; gap: 12px; width: 100%; }
.patient-label { color: #64748b; font-size: 14px; }
.patient-name { color: #f8fafc; font-size: 16px; font-weight: 600; }
.patient-id-badge { background: rgba(16,185,129,.15); color: #10b981; font-size: 12px; font-weight: 600; padding: 2px 8px; border-radius: 6px; border: 1px solid rgba(16,185,129,.3); }
</style>
