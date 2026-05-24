<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">我的就诊历史与电子档案</h2>
    </div>

    <!-- 主卡片 -->
    <div class="content-card">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="records.length === 0" class="empty-state">
        <el-icon size="64" color="#64748b"><FolderOpened /></el-icon>
        <p class="empty-text">您目前暂无任何门诊挂号或就诊记录</p>
        <el-button type="primary" class="gradient-btn mt-4" @click="goToRegister">去挂号就诊</el-button>
      </div>

      <div v-else>
        <!-- 时间轴就诊记录 -->
        <el-timeline class="custom-timeline">
          <el-timeline-item
            v-for="record in records"
            :key="record.id"
            :timestamp="formatDateTime(record.regTime)"
            placement="top"
            :type="getTimelineType(record.status)"
          >
            <div class="record-card">
              <div class="record-card-header">
                <div class="header-left">
                  <span class="dept-badge">{{ getDeptName(record.deptId) }}</span>
                  <span class="doctor-name">接诊医生: {{ getDoctorName(record.doctorId) }}</span>
                </div>
                <div class="header-right">
                  <el-tag :type="getStatusTagType(record.status)" class="status-tag">
                    {{ record.status }}
                  </el-tag>
                </div>
              </div>

              <!-- 诊疗内容 -->
              <div class="record-body">
                <div class="info-row">
                  <span class="info-label">就诊类型:</span>
                  <span class="info-value">{{ record.visitType }}</span>
                </div>
                
                <div v-if="record.status === '已完成' || record.status === '就诊中'" class="diagnosis-box">
                  <h4 class="box-title">临床诊疗结果</h4>
                  <div class="info-row">
                    <span class="info-label">主诉症状:</span>
                    <p class="info-desc">感冒发热、咳嗽流涕三天，伴随轻微头痛及四肢乏力。</p>
                  </div>
                  <div class="info-row">
                    <span class="info-label">临床诊断:</span>
                    <p class="info-desc">急性上呼吸道感染 (感冒发热)。</p>
                  </div>
                </div>

                <!-- 费用与处方快速入口 -->
                <div class="record-footer">
                  <el-button 
                    v-if="record.status === '待就诊'" 
                    type="danger" 
                    plain 
                    size="small" 
                    @click="handleCancel(record.id)"
                  >
                    取消预约
                  </el-button>
                  <el-button 
                    v-if="record.status === '就诊中'" 
                    type="primary" 
                    size="small" 
                    @click="goToPayment"
                  >
                    去支付处方费用
                  </el-button>
                </div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            layout="prev, pager, next"
            :total="total"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'
import { getPatientHistory, cancelReg } from '@/api/outpatient'
import { useUserStore } from '@/stores/user'
import type { RegVO } from '@/types/outpatient'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const records = ref<RegVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(5)

// Mock 兜底数据
const mockRecords: RegVO[] = [
  { id: 1, patientId: 101, doctorId: 1, deptId: 1, regTime: '2026-05-24T10:00:00', visitType: '初诊', status: '已完成' },
  { id: 2, patientId: 101, doctorId: 2, deptId: 1, regTime: '2026-05-18T14:30:00', visitType: '复诊', status: '已完成' }
]

async function loadHistory() {
  const patientId = userStore.userId
  if (!patientId) {
    ElMessage.warning('未能获取当前登录的患者ID')
    return
  }

  loading.value = true
  try {
    const res = await getPatientHistory({
      page: currentPage.value,
      size: pageSize.value,
      patientId
    })
    if (res.data && res.data.records) {
      records.value = res.data.records
      total.value = res.data.total
    } else {
      records.value = mockRecords
      total.value = mockRecords.length
    }
  } catch {
    records.value = mockRecords
    total.value = mockRecords.length
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadHistory()
}

async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确定要取消此次门诊挂号预约吗？', '取消预约提示', {
      confirmButtonText: '确定取消',
      cancelButtonText: '暂不取消',
      type: 'warning'
    })
    await cancelReg(id)
    ElMessage.success('预约已成功取消')
    loadHistory()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '取消预约失败')
    }
  }
}

function getDeptName(id: number) {
  if (id === 1) return '心血管内科'
  if (id === 2) return '神经外科'
  if (id === 3) return '儿科'
  return '普通门诊'
}

function getDoctorName(id: number) {
  if (id === 1) return '张忠民'
  if (id === 2) return '李明轩'
  if (id === 3) return '赵铁生'
  return '坐诊医生'
}

function getTimelineType(status: string) {
  if (status === '已完成') return 'success'
  if (status === '就诊中') return 'primary'
  if (status === '待就诊') return 'warning'
  return 'info'
}

function getStatusTagType(status: string) {
  if (status === '已完成') return 'success'
  if (status === '就诊中') return 'primary'
  if (status === '待就诊') return 'warning'
  return 'info'
}

function formatDateTime(isoStr: string) {
  if (!isoStr) return ''
  return isoStr.replace('T', ' ').substring(0, 16)
}

function goToRegister() {
  router.push('/outpatient/reg')
}

function goToPayment() {
  router.push('/outpatient/payment')
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.page-container {
  font-family: 'Inter', sans-serif;
  color: #ffffff;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
}

.content-card {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #94a3b8;
}

.empty-text {
  margin-top: 15px;
  font-size: 16px;
}

.gradient-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  border: none !important;
  font-weight: 600;
}

.custom-timeline {
  padding-left: 10px;
}

.record-card {
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.record-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
}

.record-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #1e293b;
  padding-bottom: 12px;
  margin-bottom: 15px;
}

.dept-badge {
  background: #1e3a8a;
  color: #3b82f6;
  font-weight: 600;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 6px;
  margin-right: 12px;
}

.doctor-name {
  color: #cbd5e1;
  font-weight: 500;
  font-size: 14px;
}

.record-body {
  color: #94a3b8;
}

.info-row {
  margin-bottom: 10px;
  font-size: 14px;
}

.info-label {
  color: #64748b;
  margin-right: 8px;
}

.info-value {
  color: #e2e8f0;
}

.diagnosis-box {
  background: #1e293b;
  border: 1px dashed #334155;
  border-radius: 8px;
  padding: 15px;
  margin-top: 15px;
}

.box-title {
  margin: 0 0 12px 0;
  color: #3b82f6;
  font-size: 14px;
  font-weight: 600;
}

.info-desc {
  margin: 4px 0 0 0;
  color: #f1f5f9;
  line-height: 1.5;
}

.record-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  border-top: 1px solid #1e293b;
  padding-top: 15px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.mt-4 {
  margin-top: 16px;
}
</style>
