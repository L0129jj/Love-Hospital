<template>
  <div class="dashboard-container">
    <!-- 头部欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-info">
        <h1 class="welcome-title">您好，{{ welcomeName }}！</h1>
        <p class="welcome-subtitle">欢迎登录仁爱医院综合管理系统。今天是：{{ currentDate }}</p>
      </div>
      <div class="welcome-action" v-if="userStore.role === 'ROLE_PATIENT'">
        <el-button type="success" size="large" @click="router.push('/outpatient/reg')">立即预约挂号</el-button>
      </div>
    </div>

    <!-- 管理员 & 医生大屏数据看板 -->
    <div v-if="userStore.role === 'ROLE_ADMIN' || userStore.role === 'ROLE_DOCTOR'" class="stats-row">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-box blue">
            <div class="stat-header">
              <span>今日接待患者</span>
              <el-tag size="small" type="primary">实时</el-tag>
            </div>
            <div class="stat-value">128 <span class="unit">人</span></div>
            <div class="stat-footer">相比昨日 <span class="trend up">+12%</span></div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-box purple">
            <div class="stat-header">
              <span>当前在院人数</span>
              <el-tag size="small" type="warning">在院</el-tag>
            </div>
            <div class="stat-value">84 <span class="unit">人</span></div>
            <div class="stat-footer">空置床位 <span class="trend">16 张</span></div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-box green">
            <div class="stat-header">
              <span>今日新增挂号</span>
              <el-tag size="small" type="success">挂号</el-tag>
            </div>
            <div class="stat-value">46 <span class="unit">单</span></div>
            <div class="stat-footer">已接诊 <span class="trend">38 单</span></div>
          </div>
        </el-col>
        <el-col :span="6" v-if="userStore.role === 'ROLE_ADMIN'">
          <div class="stat-box orange">
            <div class="stat-header">
              <span>今日门诊收入</span>
              <el-tag size="small" type="danger">财务</el-tag>
            </div>
            <div class="stat-value">￥8,420</div>
            <div class="stat-footer">相比上周 <span class="trend up">+8.5%</span></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 主要业务入口/功能矩阵 -->
    <h3 class="section-title">快捷入口</h3>
    <el-row :gutter="20" class="actions-grid">
      <template v-for="act in filteredActions" :key="act.title">
        <el-col :span="6">
          <div class="action-card" @click="router.push(act.path)">
            <div class="action-icon" :style="{ background: act.color }">
              <el-icon size="24" color="#ffffff"><component :is="act.icon" /></el-icon>
            </div>
            <div class="action-details">
              <h4 class="action-title">{{ act.title }}</h4>
              <p class="action-desc">{{ act.desc }}</p>
            </div>
          </div>
        </el-col>
      </template>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  Setting, 
  Calendar, 
  HomeFilled, 
  TrendCharts,
  User,
  Money,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const currentDate = computed(() => {
  const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  return new Date().toLocaleDateString('zh-CN', options)
})

const welcomeName = computed(() => {
  const defaultRoleName = 
    userStore.role === 'ROLE_ADMIN' ? '系统管理员' :
    userStore.role === 'ROLE_DOCTOR' ? '坐诊医生' :
    userStore.role === 'ROLE_PATIENT' ? '患者用户' : '来宾'
  return userStore.name || defaultRoleName
})

// 快捷操作矩阵
const actionItems = [
  {
    title: '科室管理',
    desc: '科室信息的新增与CRUD配置',
    path: '/system/depts',
    icon: Setting,
    color: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)',
    roles: ['ROLE_ADMIN']
  },
  {
    title: '医生管理',
    desc: '维护医院执业医生资质与部门',
    path: '/system/doctors',
    icon: User,
    color: 'linear-gradient(135deg, #a855f7 0%, #7e22ce 100%)',
    roles: ['ROLE_ADMIN']
  },
  {
    title: '药品库管理',
    desc: '药品库存量与信息更新维护',
    path: '/system/drugs',
    icon: Document,
    color: 'linear-gradient(135deg, #10b981 0%, #047857 100%)',
    roles: ['ROLE_ADMIN']
  },
  {
    title: '排班设置',
    desc: '医生坐诊排班与时间分段规划',
    path: '/schedule',
    icon: Calendar,
    color: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
    roles: ['ROLE_ADMIN', 'ROLE_DOCTOR']
  },
  {
    title: '预约挂号',
    desc: '患者预约门诊挂号登记通道',
    path: '/outpatient/reg',
    icon: Calendar,
    color: 'linear-gradient(135deg, #6366f1 0%, #4338ca 100%)',
    roles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT']
  },
  {
    title: '电子处方开具',
    desc: '就诊患者病历和电子处方管理',
    path: '/outpatient/prescription',
    icon: Document,
    color: 'linear-gradient(135deg, #ec4899 0%, #be185d 100%)',
    roles: ['ROLE_ADMIN', 'ROLE_DOCTOR']
  },
  {
    title: '门诊缴费',
    desc: '患者就诊费用及药品费在线支付',
    path: '/outpatient/payment',
    icon: Money,
    color: 'linear-gradient(135deg, #14b8a6 0%, #0f766e 100%)',
    roles: ['ROLE_ADMIN', 'ROLE_PATIENT']
  },
  {
    title: '住院档案登记',
    desc: '办理患者住院手续及床位分配',
    path: '/inpatient/archive',
    icon: HomeFilled,
    color: 'linear-gradient(135deg, #3b82f6 0%, #1e40af 100%)',
    roles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT']
  },
  {
    title: '统计分析报表',
    desc: '医院营收及各科室挂号多维分析',
    path: '/stats',
    icon: TrendCharts,
    color: 'linear-gradient(135deg, #ef4444 0%, #b91c1c 100%)',
    roles: ['ROLE_ADMIN']
  }
]

const filteredActions = computed(() => {
  const role = userStore.role || ''
  return actionItems.filter(item => {
    if (!item.roles || item.roles.length === 0) return true
    return item.roles.includes(role)
  })
})
</script>

<style scoped>
.dashboard-container {
  font-family: 'Inter', sans-serif;
  color: #ffffff;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #ffffff 0%, #cbd5e1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-subtitle {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 30px;
}

.stat-box {
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #334155;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-box.blue { background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(59, 130, 246, 0.1) 100%); }
.stat-box.purple { background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(168, 85, 247, 0.1) 100%); }
.stat-box.green { background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(16, 185, 129, 0.1) 100%); }
.stat-box.orange { background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(249, 115, 22, 0.1) 100%); }

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
}

.stat-value .unit {
  font-size: 16px;
  color: #94a3b8;
  font-weight: 400;
}

.stat-footer {
  font-size: 12px;
  color: #64748b;
}

.trend.up {
  color: #10b981;
  font-weight: 600;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 30px 0 20px 0;
  color: #f1f5f9;
}

/* 快捷操作 */
.actions-grid {
  row-gap: 20px;
}

.action-card {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-4px);
  border-color: #3b82f6;
  box-shadow: 0 10px 20px -10px rgba(59, 130, 246, 0.3);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: #f8fafc;
}

.action-desc {
  font-size: 12px;
  color: #64748b;
  margin: 0;
  line-height: 1.4;
}
</style>
