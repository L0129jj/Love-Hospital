<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">住院费用结算清单</h2>
    </div>

    <!-- 工作人员专用的检索控制台 -->
    <div v-if="userStore.role !== 'ROLE_PATIENT'" class="staff-search-card">
      <el-form :inline="true" style="margin-bottom: 0px; display: flex; align-items: center;">
        <span class="search-label">财务及病历对账中心：</span>
        <el-form-item style="margin-bottom: 0px; margin-right: 15px;">
          <el-input-number 
            v-model="searchPatientId" 
            placeholder="输入要检索的患者 ID (例: 101)" 
            :min="1" 
            :controls="false"
            style="width: 260px;" 
          />
        </el-form-item>
        <el-form-item style="margin-bottom: 0px;">
          <el-button type="primary" class="gradient-btn" @click="loadFees">
            检索指定患者账单
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 工作人员无档案时的友好提示 -->
    <el-alert
      v-if="!hasActiveArchive && userStore.role !== 'ROLE_PATIENT'"
      title="当前检索的患者未处于住院状态"
      type="info"
      description="检索提示：指定患者当前在院状态并非“住院中”，可能是已出院或尚未在住院处登记，故其住院账户不可用。但您仍可查看或充值其就诊卡账户。"
      show-icon
      :closable="false"
      style="margin-bottom: 24px; background-color: rgba(30, 41, 59, 0.8); border: 1px solid #334155; color: #94a3b8;"
    />

    <!-- 患者本人的温馨提示 -->
    <el-alert
      v-if="!hasActiveArchive && userStore.role === 'ROLE_PATIENT'"
      title="住院预备账户未激活"
      type="warning"
      description="您当前尚未成功办理入院登记手续，住院预缴押金账户暂未激活（可用余额为 ￥0.00）。如需开立病床进行住院治疗，请先携带就诊卡至【住院档案登记处】办理入院登记并预缴押金。"
      show-icon
      :closable="false"
      style="margin-bottom: 24px; background-color: rgba(245, 158, 11, 0.1); border: 1px solid rgba(245, 158, 11, 0.2); color: #fbbf24;"
    />

    <!-- 住院账单总揽与就诊卡钱包联动 -->
    <el-row :gutter="20" class="summary-cards">
      <!-- 电子就诊卡钱包（核心联动） -->
      <el-col :span="6">
        <div class="summary-box purple">
          <div class="wallet-header">
            <span class="summary-label">就诊卡账户可用余额</span>
            <span v-if="patientName" class="patient-badge">{{ patientName }}</span>
          </div>
          <h3 class="summary-value font-mono">￥{{ patientCardBalance.toFixed(2) }}</h3>
          <div class="wallet-actions">
            <el-button 
              type="primary" 
              size="small" 
              class="action-btn charge"
              :loading="recharging"
              @click="handleQuickRechargeCard"
            >
              充值 5000
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              class="action-btn transfer"
              :disabled="!hasActiveArchive || patientCardBalance <= 0"
              @click="openTransferDialog"
            >
              划拨续缴
            </el-button>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="summary-box blue">
          <p class="summary-label">住院预缴押金总额</p>
          <h3 class="summary-value">￥{{ prepaidTotal.toFixed(2) }}</h3>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="summary-box red">
          <p class="summary-label">目前已消耗总额</p>
          <h3 class="summary-value">￥{{ consumedTotal.toFixed(2) }}</h3>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="summary-box green">
          <p class="summary-label">账户剩余可用余额</p>
          <h3 class="summary-value">￥{{ remainingTotal.toFixed(2) }}</h3>
        </div>
      </el-col>
    </el-row>

    <!-- 费用产生明细 -->
    <h3 class="section-title">每日收费明细账目</h3>
    <div class="content-card">
      <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="流水单号" width="120" />
        <el-table-column prop="feeDate" label="产生日期" width="130" />
        <el-table-column prop="feeType" label="收费项目大类" width="130">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.feeType)">
              {{ scope.row.feeType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="scope">
            <span class="price-text">￥{{ scope.row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="详细描述明细" />
      </el-table>
    </div>

    <!-- 划拨押金弹窗 -->
    <el-dialog
      v-model="transferDialogVisible"
      title="就诊卡余额划拨至住院押金"
      width="400px"
      class="custom-dialog"
      destroy-on-close
    >
      <div style="padding: 10px 0;">
        <p class="dialog-text">将就诊卡可用余额直接划拨并充值到患者当前的住院预缴押金账户中。</p>
        <div class="transfer-info-box">
          <div>当前就诊卡余额：<span class="highlight-text">￥{{ patientCardBalance.toFixed(2) }}</span></div>
          <div>当前住院可用余额：<span class="highlight-text font-green">￥{{ remainingTotal.toFixed(2) }}</span></div>
        </div>
        <el-form label-width="80px" style="margin-top: 20px;">
          <el-form-item label="划拨金额">
            <el-input-number 
              v-model="transferAmount" 
              :min="1" 
              :max="patientCardBalance"
              :step="100"
              style="width: 100%" 
              class="custom-number"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transferDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="transferring" @click="handleTransfer">确 定 划 拨</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFeesByArchive, getArchivePage, rechargeArchive } from '@/api/inpatient'
import type { FeeVO } from '@/types/inpatient'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<FeeVO[]>([])

// 如果是患者角色，检索本人的 ID；若是医护人员/管理员，初始化检索 101 患者
const searchPatientId = ref<number | undefined>(
  userStore.role === 'ROLE_PATIENT' ? userStore.userId : 101
)

const hasActiveArchive = ref(false)
const archiveInfo = ref<any>(null)

// 动态汇总卡片状态
const prepaidTotal = ref(0)
const consumedTotal = ref(0)
const remainingTotal = ref(0)

// 就诊卡余额联动状态
const patientCardBalance = ref(0)
const patientName = ref('')
const recharging = ref(false)
const transferring = ref(false)
const transferDialogVisible = ref(false)
const transferAmount = ref(1000)

function getTagType(type: string) {
  if (type === '病房床位费') return 'info'
  if (type === '西药费') return 'success'
  return 'primary'
}

async function loadFees() {
  loading.value = true
  try {
    if (searchPatientId.value) {
      // 1. 获取对应患者的最真实就诊卡余额与姓名
      try {
        const patientRes = await request.get(`/auth/patient/${searchPatientId.value}`)
        if (patientRes && patientRes.data) {
          patientCardBalance.value = patientRes.data.cardBalance || 0
          patientName.value = patientRes.data.name || ''
        } else {
          patientCardBalance.value = 0
          patientName.value = ''
        }
      } catch (err) {
        console.error('获取患者就诊卡信息失败:', err)
        patientCardBalance.value = 0
        patientName.value = ''
      }

      // 2. 查询该患者当前处于“住院中”的档案
      const archiveRes = await getArchivePage({
        page: 1,
        size: 1,
        patientId: searchPatientId.value,
        status: '住院中'
      })
      
      if (archiveRes && archiveRes.data && archiveRes.data.records && archiveRes.data.records.length > 0) {
        const activeArchive = archiveRes.data.records[0]
        hasActiveArchive.value = true
        archiveInfo.value = activeArchive
        
        prepaidTotal.value = activeArchive.prepaidAmount || 0
        remainingTotal.value = activeArchive.balance || 0
        consumedTotal.value = Math.max(0, prepaidTotal.value - remainingTotal.value)
        
        // 3. 拉取此住院编号名下的每日费用账单明细
        const res = await getFeesByArchive(activeArchive.id)
        tableData.value = res.data && res.data.length > 0 ? res.data : []
        return
      }
    }
    
    // 如果无真实在院档案，清空展示为0.00
    hasActiveArchive.value = false
    archiveInfo.value = null
    prepaidTotal.value = 0.00
    consumedTotal.value = 0.00
    remainingTotal.value = 0.00
    tableData.value = []
  } catch (e) {
    console.error('Failed to load active archive fees:', e)
    hasActiveArchive.value = false
    archiveInfo.value = null
    prepaidTotal.value = 0.00
    consumedTotal.value = 0.00
    remainingTotal.value = 0.00
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 就诊卡一键充值 5000 元
async function handleQuickRechargeCard() {
  if (!searchPatientId.value) return
  recharging.value = true
  try {
    const res = await request.put(`/auth/patient/${searchPatientId.value}/recharge-card`, null, {
      params: { amount: 5000 }
    })
    ElMessage.success(`就诊卡快捷充值成功！已为患者充入 5000 元！当前就诊卡余额已变更为 ￥${res.data.toFixed(2)}`)
    patientCardBalance.value = res.data
  } catch (err) {
    ElMessage.error('就诊卡快捷充值失败')
  } finally {
    recharging.value = false
  }
}

function openTransferDialog() {
  transferAmount.value = Math.min(patientCardBalance.value, 2000)
  if (transferAmount.value <= 0) transferAmount.value = 100
  transferDialogVisible.value = true
}

// 将就诊卡余额划拨并续缴至住院账户
async function handleTransfer() {
  if (!archiveInfo.value || !archiveInfo.value.id) {
    ElMessage.warning('当前患者无有效的住院档案，无法进行费用划拨')
    return
  }
  if (transferAmount.value <= 0) {
    ElMessage.warning('划拨金额必须大于0')
    return
  }
  if (transferAmount.value > patientCardBalance.value) {
    ElMessage.error('就诊卡账户余额不足，请先充值')
    return
  }
  transferring.value = true
  try {
    await rechargeArchive(archiveInfo.value.id, transferAmount.value)
    ElMessage.success(`划拨续缴成功！已划拨 ￥${transferAmount.value.toFixed(2)} 注入住院押金！`)
    transferDialogVisible.value = false
    await loadFees() // 重新刷账单
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '划拨续缴失败，请检查余额后重试')
  } finally {
    transferring.value = false
  }
}

onMounted(() => {
  loadFees()
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
  margin: 0;
}

.staff-search-card {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.7) 0%, rgba(51, 65, 85, 0.7) 100%);
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 15px -3px rgba(0, 0, 0, 0.3);
}

.search-label {
  color: #e2e8f0;
  font-size: 14px;
  font-weight: 600;
}

.gradient-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  border: none !important;
  font-weight: 600;
}

/* 费用总览卡片 */
.summary-cards {
  margin-bottom: 30px;
}

.summary-box {
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #334155;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  min-height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.summary-box.purple {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(139, 92, 246, 0.15) 100%);
  border: 1px solid rgba(139, 92, 246, 0.3);
}
.summary-box.blue { background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(59, 130, 246, 0.15) 100%); }
.summary-box.red { background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(239, 68, 68, 0.15) 100%); }
.summary-box.green { background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(16, 185, 129, 0.15) 100%); }

.wallet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.patient-badge {
  background-color: rgba(139, 92, 246, 0.2);
  color: #a78bfa;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid rgba(139, 92, 246, 0.3);
}

.wallet-actions {
  display: flex;
  gap: 8px;
  width: 100%;
  margin-top: 12px;
}

.action-btn {
  flex: 1;
  font-weight: 600;
  border: none !important;
  padding: 5px 8px;
}

.action-btn.charge {
  background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%) !important;
}
.action-btn.charge:hover {
  background: linear-gradient(135deg, #a78bfa 0%, #7c3aed 100%) !important;
}
.action-btn.transfer {
  background: linear-gradient(135deg, #10b981 0%, #047857 100%) !important;
}
.action-btn.transfer:hover {
  background: linear-gradient(135deg, #34d399 0%, #059669 100%) !important;
}

.summary-label {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.summary-value {
  font-size: 26px;
  font-weight: 700;
  color: #ffffff;
  margin: 8px 0 0 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #cbd5e1;
  margin-bottom: 15px;
}

.content-card {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.price-text {
  color: #f59e0b;
  font-weight: 600;
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
  font-weight: 700;
}

.dialog-text {
  font-size: 14px;
  color: #94a3b8;
  line-height: 1.5;
  margin-top: 0;
  margin-bottom: 16px;
}

.transfer-info-box {
  background-color: #0f172a;
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 14px 18px;
  font-size: 14px;
  color: #cbd5e1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.highlight-text {
  font-family: 'Courier New', Courier, monospace;
  font-weight: 700;
  color: #f59e0b;
  font-size: 16px;
}

.highlight-text.font-green {
  color: #10b981;
}

.custom-number :deep(.el-input__wrapper) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-number :deep(.el-input__inner) {
  color: #ffffff !important;
  font-family: monospace;
}
</style>
