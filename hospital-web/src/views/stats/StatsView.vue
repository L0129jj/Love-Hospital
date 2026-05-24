<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">医院运营统计大屏</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="custom-date-range"
          style="width: 320px"
        />
        <el-button type="primary" class="gradient-btn" @click="handleSearch">刷新数据</el-button>
      </div>
    </div>

    <!-- 顶层总揽 -->
    <el-row :gutter="20" class="mb-24">
      <el-col :span="6">
        <div class="stats-card gradient-cyan">
          <p class="stats-label">总计营业收入 (本月)</p>
          <h3 class="stats-value">{{ monthRevenue }}</h3>
          <p class="stats-sub">{{ averageRevenue }}</p>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card gradient-blue">
          <p class="stats-label">本月预约挂号总数</p>
          <h3 class="stats-value">1,420 <span class="unit">单</span></h3>
          <p class="stats-sub">预约到诊率 94.6%</p>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card gradient-purple">
          <p class="stats-label">在编临床医护人员</p>
          <h3 class="stats-value">84 <span class="unit">人</span></h3>
          <p class="stats-sub">主治及以上医师 32 人</p>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card gradient-orange">
          <p class="stats-label">床位占用负荷率</p>
          <h3 class="stats-value">84.0%</h3>
          <p class="stats-sub">开放病床 100 张，占用 84 张</p>
        </div>
      </el-col>
    </el-row>

    <!-- 图表和分析网格 -->
    <el-row :gutter="20">
      <!-- 科室排班及负载分析 -->
      <el-col :span="12">
        <div class="content-card full-height">
          <h3 class="card-title">临床科室挂号及排班负载排名</h3>
          <div class="chart-mock-container">
            <div v-for="dept in deptStats" :key="dept.name" class="bar-progress-row">
              <div class="progress-info">
                <span class="dept-name">{{ dept.name }}</span>
                <span class="dept-val">{{ dept.count }} 人次候诊</span>
              </div>
              <el-progress 
                :percentage="dept.percentage" 
                :color="dept.color" 
                :show-text="false" 
                stroke-width="12"
                class="custom-progress"
              />
            </div>
          </div>
        </div>
      </el-col>

      <!-- 坐诊医生工作负载排行 -->
      <el-col :span="12">
        <div class="content-card full-height">
          <h3 class="card-title">本期坐诊专家工作量大盘</h3>
          <el-table :data="doctorWorkload" style="width: 100%" class="custom-table compact">
            <el-table-column prop="rank" label="排名" width="80" align="center">
              <template #default="scope">
                <span class="rank-badge" :class="'top-' + scope.row.rank">{{ scope.row.rank }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="医生姓名" />
            <el-table-column prop="dept" label="科室" />
            <el-table-column prop="patients" label="接待诊疗人次" align="right">
              <template #default="scope">
                <span class="patients-count">{{ scope.row.patients }} 次</span>
              </template>
            </el-table-column>
            <el-table-column prop="schedules" label="已排班次" align="center" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRevenue, getScheduleStats, getDoctorWorkload } from '@/api/stats'

const dateRange = ref<[string, string]>(['2026-05-01', '2026-05-31'])

const monthRevenue = ref('￥284,590.00')
const averageRevenue = ref('￥9,486.30')
const loading = ref(false)

const deptStats = ref([
  { name: '小儿呼吸科', count: 485, percentage: 95, color: '#f87171' },
  { name: '心血管内科', count: 320, percentage: 75, color: '#60a5fa' },
  { name: '神经外科', count: 210, percentage: 55, color: '#c084fc' },
  { name: '骨科关节门诊', count: 180, percentage: 45, color: '#fbbf24' }
])

const doctorWorkload = ref([
  { rank: 1, name: '张忠民', dept: '心血管内科', patients: 145, schedules: 12 },
  { rank: 2, name: '孙玉芳', dept: '小儿呼吸科', patients: 130, schedules: 10 },
  { rank: 3, name: '李晓华', dept: '心血管内科', patients: 95, schedules: 8 },
  { rank: 4, name: '赵铁生', dept: '神经外科', patients: 84, schedules: 6 }
])

async function fetchStats() {
  if (!dateRange.value || dateRange.value.length < 2) return
  loading.value = true
  const [start, end] = dateRange.value

  try {
    // 1. 加载营收统计数据
    const revenueRes = await getRevenue(start, end)
    if (revenueRes.data) {
      const val = revenueRes.data.totalRevenue || 0
      monthRevenue.value = `￥${val.toFixed(2)}`
      averageRevenue.value = `日均营收 ￥${(val / 30).toFixed(2)}`
    }

    // 2. 加载科室排班排队统计 (使用默认 deptId=1 做聚合统计)
    const scheduleRes = await getScheduleStats(1, start, end)
    if (scheduleRes.data && scheduleRes.data.length > 0) {
      const totalSchedules = scheduleRes.data.reduce((sum, item) => sum + (item.totalSchedules || 0), 0)
      deptStats.value = scheduleRes.data.map((item, index) => {
        const colors = ['#f87171', '#60a5fa', '#c084fc', '#fbbf24']
        return {
          name: item.deptName || '普通科室',
          count: Number(item.totalSchedules),
          percentage: totalSchedules > 0 ? Math.round((Number(item.totalSchedules) / totalSchedules) * 100) : 0,
          color: colors[index % colors.length]
        }
      })
    }

    // 3. 加载医生工作量大盘 (使用默认 doctorId=1 做大盘排行)
    const doctorRes = await getDoctorWorkload(1, start, end)
    if (doctorRes.data && doctorRes.data.length > 0) {
      doctorWorkload.value = doctorRes.data.map((item, index) => ({
        rank: index + 1,
        name: item.doctorName || '专科医生',
        dept: '全科',
        patients: Number(item.totalPatients),
        schedules: Number(item.totalSchedules)
      }))
    }

    ElMessage.success('统计分析大屏数据已实时刷新同步！')
  } catch {
    ElMessage.warning('未能连通统计接口，已自动降级展示模拟统计大屏')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchStats()
}

onMounted(() => {
  fetchStats()
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

.header-actions {
  display: flex;
  gap: 12px;
}

.gradient-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  border: none !important;
  font-weight: 600;
}

.mb-24 {
  margin-bottom: 24px;
}

/* 顶部炫彩大卡片 */
.stats-card {
  padding: 24px;
  border-radius: 20px;
  color: #ffffff;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.gradient-cyan { background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%); }
.gradient-blue { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); }
.gradient-purple { background: linear-gradient(135deg, #a855f7 0%, #9333ea 100%); }
.gradient-orange { background: linear-gradient(135deg, #f97316 0%, #ea580c 100%); }

.stats-label {
  font-size: 13px;
  opacity: 0.85;
  margin: 0 0 10px 0;
}

.stats-value {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 8px 0;
}

.stats-value .unit {
  font-size: 16px;
  font-weight: 400;
}

.stats-sub {
  font-size: 11px;
  opacity: 0.75;
  margin: 0;
}

/* 大屏图表骨架 */
.content-card {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.card-title {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #f1f5f9;
}

.chart-mock-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px 0;
}

.bar-progress-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.dept-name {
  color: #cbd5e1;
  font-weight: 500;
}

.dept-val {
  color: #94a3b8;
}

.custom-progress :deep(.el-progress-bar__runway) {
  background-color: #0f172a !important;
}

/* 医生大盘表格 */
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

.rank-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  background-color: #334155;
  color: #cbd5e1;
  font-size: 12px;
  font-weight: 700;
}

.rank-badge.top-1 { background-color: #ef4444; color: #ffffff; }
.rank-badge.top-2 { background-color: #f97316; color: #ffffff; }
.rank-badge.top-3 { background-color: #f59e0b; color: #ffffff; }

.patients-count {
  color: #10b981;
  font-weight: 600;
}

.custom-date-range :deep(.el-range-input) {
  color: #ffffff !important;
}

.custom-date-range :deep(.el-range-separator) {
  color: #94a3b8 !important;
}

.custom-date-range :deep(.el-input__wrapper) {
  background-color: #1e293b !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}
</style>
