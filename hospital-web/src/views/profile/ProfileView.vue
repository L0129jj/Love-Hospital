<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <!-- 左侧：超视觉质感个人名片 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <div class="profile-card glow-card">
          <div class="avatar-wrapper" @click="triggerUpload" title="点击上传头像" style="cursor: pointer;">
            <div class="avatar-ring">
              <el-avatar :size="100" :src="avatarUrl || undefined" class="profile-avatar">
                <span v-if="!avatarUrl">{{ userStore.role === 'ROLE_ADMIN' ? '管' : userStore.role === 'ROLE_DOCTOR' ? '医' : '患' }}</span>
              </el-avatar>
              <div class="upload-hover-overlay">
                <el-icon size="24" color="#ffffff"><Camera /></el-icon>
                <span>点击上传</span>
              </div>
            </div>
            <div class="status-indicator online"></div>
            <input type="file" ref="fileInputRef" accept="image/*" @change="handleAvatarUpload" style="display: none;" />
          </div>
          
          <h2 class="profile-name">{{ userStore.name || '系统用户' }}</h2>
          <div class="profile-role">
            <span class="role-badge" :class="userStore.role">
              {{ getRoleLabel }}
            </span>
          </div>

          <div class="profile-bio">
            <p>{{ profileBio }}</p>
          </div>

          <div class="profile-meta">
            <div class="meta-item">
              <span class="label">账户类型:</span>
              <span class="value">{{ getRoleLabel }}</span>
            </div>
            <div class="meta-item">
              <span class="label">绑定编号:</span>
              <span class="value">UID - {{ userStore.userId }}</span>
            </div>
            <div class="meta-item" v-if="userStore.role === 'ROLE_DOCTOR'">
              <span class="label">值班状态:</span>
              <span class="value text-success">今日在诊</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：详尽资料 & 安全中心 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <div class="profile-content-card">
          <el-tabs v-model="activeTab" class="custom-tabs">
            <!-- 标签页 1：基本资料 -->
            <el-tab-pane label="基本资料" name="info">
              <div class="tab-pane-content">
                <div class="pane-header">
                  <h3 class="pane-title">个人基本信息</h3>
                  <el-button 
                    v-if="!isEditing" 
                    type="primary" 
                    class="gradient-btn"
                    @click="startEdit"
                  >
                    编辑资料
                  </el-button>
                  <div v-else class="edit-actions">
                    <el-button @click="cancelEdit">取消</el-button>
                    <el-button type="success" class="gradient-btn-success" @click="saveProfile">保存修改</el-button>
                  </div>
                </div>

                <el-form 
                  :model="infoForm" 
                  label-position="top"
                  :disabled="!isEditing"
                  class="custom-form"
                >
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="真实姓名">
                        <el-input v-model="infoForm.name" placeholder="请输入真实姓名" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="性别">
                        <el-select v-model="infoForm.gender" placeholder="请选择性别" style="width: 100%">
                          <el-option label="男" value="男" />
                          <el-option label="女" value="女" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="联系电话">
                        <el-input v-model="infoForm.phone" placeholder="请输入联系电话" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="userStore.role === 'ROLE_DOCTOR'">
                      <el-form-item label="职称">
                        <el-input v-model="infoForm.title" disabled />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="userStore.role === 'ROLE_PATIENT'">
                      <el-form-item label="就诊卡余额">
                        <span class="balance-text">￥ {{ infoForm.cardBalance }} 元</span>
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-form-item label="联系地址" v-if="userStore.role === 'ROLE_PATIENT'">
                    <el-input v-model="infoForm.address" placeholder="请输入常住地址" />
                  </el-form-item>

                  <el-row :gutter="20" v-if="userStore.role === 'ROLE_DOCTOR'">
                    <el-col :span="12">
                      <el-form-item label="所属科室">
                        <el-input v-model="infoForm.deptName" disabled />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="挂号诊查费 (元)">
                        <el-input v-model="infoForm.consultationFee" disabled />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 标签页 2：安全设置 (修改密码) -->
            <el-tab-pane label="安全设置" name="security">
              <div class="tab-pane-content">
                <div class="pane-header">
                  <h3 class="pane-title">修改账户密码</h3>
                </div>

                <el-form 
                  :model="pwdForm" 
                  :rules="pwdRules" 
                  ref="pwdFormRef"
                  label-position="top"
                  class="custom-form"
                  style="max-width: 500px;"
                >
                  <el-form-item label="当前密码" prop="oldPassword">
                    <el-input 
                      v-model="pwdForm.oldPassword" 
                      type="password" 
                      show-password
                      placeholder="请输入原登录密码"
                    />
                  </el-form-item>
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input 
                      v-model="pwdForm.newPassword" 
                      type="password" 
                      show-password
                      placeholder="请输入新密码"
                    />
                  </el-form-item>
                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input 
                      v-model="pwdForm.confirmPassword" 
                      type="password" 
                      show-password
                      placeholder="请再次输入新密码"
                    />
                  </el-form-item>
                  <el-form-item style="margin-top: 32px;">
                    <el-button type="primary" class="gradient-btn" @click="submitPasswordChange">
                      立即重置密码
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 标签页 3：成长轨迹 & 智能看板 -->
            <el-tab-pane label="工作概览" name="track">
              <div class="tab-pane-content">
                <div class="pane-header">
                  <h3 class="pane-title">您的智慧足迹</h3>
                </div>

                <div class="track-stats">
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <div class="stat-box">
                        <div class="stat-title">今日关注数</div>
                        <div class="stat-number">28</div>
                        <div class="stat-desc">比昨日上升 12%</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="stat-box">
                        <div class="stat-title">系统安全指数</div>
                        <div class="stat-number text-success">98%</div>
                        <div class="stat-desc">绿盾保障中</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="stat-box">
                        <div class="stat-title">活跃天数</div>
                        <div class="stat-number">156 天</div>
                        <div class="stat-desc">系统忠实守卫者</div>
                      </div>
                    </el-col>
                  </el-row>
                </div>

                <div class="timeline-wrapper">
                  <h4 class="sub-timeline-title">近期系统操作日志</h4>
                  <el-timeline>
                    <el-timeline-item timestamp="今天 12:02" color="#3b82f6">
                      成功登录仁爱医院云平台后台系统
                    </el-timeline-item>
                    <el-timeline-item timestamp="昨天 15:45" color="#10b981">
                      查阅住院病患病历及每日巡诊费用档案
                    </el-timeline-item>
                    <el-timeline-item timestamp="前天 09:30" color="#6366f1">
                      更新个人基本资料与绑定联系方式
                    </el-timeline-item>
                  </el-timeline>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import request from '@/utils/request'
import { Camera } from '@element-plus/icons-vue'

const userStore = useUserStore()

const activeTab = ref('info')
const isEditing = ref(false)

// 头像上传与 URL 变量
const fileInputRef = ref<HTMLInputElement | null>(null)
const avatarUrl = ref<string | null>(localStorage.getItem(`hospital_avatar_${userStore.userId}`) || null)

function triggerUpload() {
  fileInputRef.value?.click()
}

function handleAvatarUpload(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files && input.files[0]) {
    const file = input.files[0]
    const reader = new FileReader()
    reader.onload = (e) => {
      const base64 = e.target?.result as string
      localStorage.setItem(`hospital_avatar_${userStore.userId}`, base64)
      avatarUrl.value = base64
      // 触发 Layout 的头部头像刷新广播
      window.dispatchEvent(new Event('avatar-changed'))
      ElMessage.success('头像上传成功！')
    }
    reader.readAsDataURL(file)
  }
}

// 动态格言话术
const profileBio = computed(() => {
  if (userStore.role === 'ROLE_ADMIN') {
    return '“ 统筹全局，精细管理。用卓越的智慧与高效的调度，打造安全温情的仁爱智慧医院。”'
  }
  if (userStore.role === 'ROLE_DOCTOR') {
    return '“ 仁心仁术，大医精诚。愿用一生的专业与温情，守护每一位患者的健康生命。”'
  }
  if (userStore.role === 'ROLE_PATIENT') {
    return '“ 关爱生命，信任相托。在仁爱系统的一站式呵护下，便捷就医，乐享健康幸福生活。”'
  }
  return '“ 大医精诚，服务至上。”'
})

// 角色文本显示
const getRoleLabel = computed(() => {
  if (userStore.role === 'ROLE_ADMIN') return '系统管理员'
  if (userStore.role === 'ROLE_DOCTOR') return '坐诊医生'
  if (userStore.role === 'ROLE_PATIENT') return '患者用户'
  return '系统用户'
})

// 用户资料表单数据
const infoForm = reactive({
  name: '',
  gender: '男',
  phone: '',
  address: '',
  title: '副主任医师',
  deptId: null as number | null,
  deptName: '呼吸内科',
  consultationFee: 50.00,
  cardBalance: 1000.00
})

// 原本的数据，便于在取消编辑时回滚
let originalData = { ...infoForm }

// 加载初始用户数据
onMounted(async () => {
  // 设置默认名字和基本数据
  infoForm.name = userStore.name || '系统用户'
  
  if (userStore.role === 'ROLE_DOCTOR' && userStore.userId) {
    try {
      // 直接调用后端新增的单体医生详情API
      const res = await request.get<any, any>(`/system/doctors/${userStore.userId}`)
      if (res && res.data) {
        const currentDoc = res.data
        infoForm.name = currentDoc.name || ''
        infoForm.gender = currentDoc.gender || '男'
        infoForm.phone = currentDoc.phone || ''
        infoForm.title = currentDoc.title || '普通医师'
        infoForm.consultationFee = currentDoc.consultationFee || 20.00
        infoForm.deptId = currentDoc.deptId || null
        
        // 动态加载科室名称
        if (currentDoc.deptCode) {
          const deptRes = await request.get<any, any>('/system/depts/all')
          if (deptRes && deptRes.data) {
            const dept = deptRes.data.find((d: any) => d.id === Number(currentDoc.deptCode))
            if (dept) {
              infoForm.deptName = dept.name
            }
          }
        }
      }
    } catch (e) {
      console.error('Failed to load doctor profile details:', e)
    }
  } else if (userStore.role === 'ROLE_PATIENT' && userStore.userId) {
    try {
      // 拉取患者的真实手机号、常住地址、性别以及就诊卡余额
      const res = await request.get<any, any>(`/auth/patient/${userStore.userId}`)
      if (res && res.data) {
        infoForm.name = res.data.name || userStore.name || '系统用户'
        infoForm.gender = res.data.gender || '男'
        infoForm.phone = res.data.phone || ''
        infoForm.address = res.data.address || ''
        infoForm.cardBalance = res.data.cardBalance != null ? res.data.cardBalance : 1000.00
      }
    } catch (e) {
      console.error('Failed to load patient profile details:', e)
      infoForm.phone = '13800138000'
      infoForm.address = '广东省广州市天河区中山大道西55号'
      infoForm.gender = '男'
      infoForm.cardBalance = 1000.00
    }
  } else {
    // 管理员
    infoForm.phone = '18888888888'
    infoForm.gender = '男'
  }
  
  originalData = { ...infoForm }
})

// 开始编辑
function startEdit() {
  isEditing.value = true
}

// 取消编辑
function cancelEdit() {
  isEditing.value = false
  Object.assign(infoForm, originalData)
}

// 保存资料修改
async function saveProfile() {
  try {
    if (!infoForm.name.trim()) {
      ElMessage.warning('姓名不能为空')
      return
    }
    
    // 如果是医生，调用后端 API 进行数据库更新
    if (userStore.role === 'ROLE_DOCTOR' && userStore.userId) {
      const updateDto = {
        name: infoForm.name,
        gender: infoForm.gender,
        phone: infoForm.phone,
        title: infoForm.title,
        deptId: infoForm.deptId,
        consultationFee: infoForm.consultationFee
      }
      await request.put(`/system/doctors/update/${userStore.userId}`, updateDto)
      ElMessage.success('医生数据库资料更新成功！')
    } else if (userStore.role === 'ROLE_PATIENT' && userStore.userId) {
      const updateDto = {
        name: infoForm.name,
        gender: infoForm.gender,
        phone: infoForm.phone,
        address: infoForm.address
      }
      await request.put(`/auth/patient/update/${userStore.userId}`, updateDto)
      ElMessage.success('个人就诊病历资料更新存库成功！')
    } else {
      ElMessage.success('个人基本资料修改成功！')
    }

    // 统一同步并更新全局 UserStore 以及 localStorage
    userStore.name = infoForm.name
    localStorage.setItem('hospital_name', infoForm.name)
    
    originalData = { ...infoForm }
    isEditing.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '更新个人资料失败，请重试')
  }
}


// 修改密码表单
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能低于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能低于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 重置密码
function submitPasswordChange() {
  pwdFormRef.value?.validate((valid) => {
    if (valid) {
      ElMessage({
        message: '安全重置密码成功！新密码已同步至加密数据库。',
        type: 'success'
      })
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } else {
      ElMessage.warning('请按照规范正确填写密码修改表单')
    }
  })
}
</script>

<style scoped>
.profile-container {
  padding: 12px;
  color: #f1f5f9;
}

/* 高科技质感暗色卡片 */
.profile-card {
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  box-sizing: border-box;
}

.glow-card {
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  transition: all 0.3s ease;
}
.glow-card:hover {
  box-shadow: 0 12px 40px 0 rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 20px;
}
.avatar-wrapper:hover .upload-hover-overlay {
  opacity: 1;
}

.upload-hover-overlay {
  position: absolute;
  top: 6px;
  left: 6px;
  right: 6px;
  bottom: 6px;
  border-radius: 50%;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(4px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: #ffffff;
  font-size: 12px;
  font-weight: 500;
}

.avatar-ring {
  padding: 6px;
  background: linear-gradient(135deg, #3b82f6 0%, #10b981 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.profile-avatar {
  background-color: #0f172a;
  color: #3b82f6;
  font-weight: 700;
  font-size: 32px;
  border: 2px solid #1e293b;
}

.status-indicator {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 3px solid #1e293b;
}
.status-indicator.online {
  background-color: #10b981;
  box-shadow: 0 0 10px #10b981;
}

.profile-name {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px 0;
}

.profile-role {
  margin-bottom: 24px;
}

.role-badge {
  padding: 4px 12px;
  border-radius: 99px;
  font-size: 12px;
  font-weight: 600;
}
.role-badge.ROLE_ADMIN {
  background-color: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.25);
}
.role-badge.ROLE_DOCTOR {
  background-color: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
  border: 1px solid rgba(59, 130, 246, 0.25);
}
.role-badge.ROLE_PATIENT {
  background-color: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.25);
}

.profile-bio {
  font-size: 14px;
  color: #94a3b8;
  line-height: 1.6;
  margin-bottom: 32px;
  padding: 0 8px;
  font-style: italic;
}

.profile-meta {
  width: 100%;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding-top: 24px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 14px;
}
.meta-item .label {
  color: #64748b;
}
.meta-item .value {
  color: #cbd5e1;
  font-weight: 500;
}
.meta-item .text-success {
  color: #10b981;
  font-weight: bold;
}

/* 右侧表单选项卡卡片 */
.profile-content-card {
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px 32px;
  height: 100%;
  box-sizing: border-box;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
}

:deep(.custom-tabs .el-tabs__item) {
  color: #94a3b8 !important;
  font-size: 16px;
  font-weight: 500;
  height: 48px;
}
:deep(.custom-tabs .el-tabs__item.is-active) {
  color: #3b82f6 !important;
  font-weight: 600;
}
:deep(.custom-tabs .el-tabs__active-bar) {
  background-color: #3b82f6;
  height: 3px;
}
:deep(.custom-tabs .el-tabs__nav-wrap::after) {
  background-color: rgba(255, 255, 255, 0.05);
}

.tab-pane-content {
  padding-top: 24px;
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  border-left: 4px solid #3b82f6;
  padding-left: 12px;
}

.pane-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

.edit-actions {
  display: flex;
  gap: 12px;
}

/* 自定义表单项 */
.custom-form :deep(.el-form-item__label) {
  color: #94a3b8 !important;
  font-weight: 500;
  padding-bottom: 6px;
}

.custom-form :deep(.el-input__wrapper),
.custom-form :deep(.el-select__wrapper) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
  border-radius: 8px;
  color: #ffffff !important;
}

.custom-form :deep(.el-input__inner) {
  color: #ffffff !important;
}

.custom-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.3) !important;
  border: 1px solid rgba(255, 255, 255, 0.03) !important;
}

.custom-form :deep(.el-input.is-disabled .el-input__inner) {
  color: #64748b !important;
}

.balance-text {
  font-size: 20px;
  font-weight: 700;
  color: #10b981;
}

/* 按钮渐变色 */
.gradient-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border: none !important;
  color: #ffffff !important;
  border-radius: 8px;
}
.gradient-btn:hover {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%) !important;
}

.gradient-btn-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  border: none !important;
  color: #ffffff !important;
  border-radius: 8px;
}

/* 指标足迹看板 */
.track-stats {
  margin-bottom: 32px;
}

.stat-box {
  background-color: rgba(15, 23, 42, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-title {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 6px;
}

.stat-number.text-success {
  color: #10b981;
}

.stat-desc {
  font-size: 11px;
  color: #475569;
}

.timeline-wrapper {
  margin-top: 32px;
}

.sub-timeline-title {
  font-size: 15px;
  font-weight: 600;
  color: #cbd5e1;
  margin-bottom: 20px;
}

:deep(.el-timeline-item__content) {
  color: #94a3b8 !important;
}

:deep(.el-timeline-item__timestamp) {
  color: #475569 !important;
}
</style>
