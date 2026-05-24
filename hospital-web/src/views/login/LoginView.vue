<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 医院 Logo 和名称 -->
      <div class="login-header">
        <div class="logo-icon">
          <el-icon size="40" color="#ffffff"><HomeFilled /></el-icon>
        </div>
        <h2 class="hospital-name">仁爱医院智能管理平台</h2>
        <p class="hospital-tagline">用心呵护生命 · 科技赋能医疗</p>
      </div>

      <!-- 登录通道选项卡 -->
      <div class="tab-container">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'admin' }" 
          @click="changeTab('admin')"
        >
          系统管理员
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'doctor' }" 
          @click="changeTab('doctor')"
        >
          坐诊医生
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'patient' }" 
          @click="changeTab('patient')"
        >
          就诊患者
        </div>
      </div>

      <!-- 管理员登录表单 -->
      <el-form 
        v-if="activeTab === 'admin'" 
        ref="adminFormRef" 
        :model="adminForm" 
        :rules="adminRules" 
        size="large"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="adminForm.username" 
            placeholder="请输入管理员用户名"
            :prefix-icon="User"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="adminForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            :prefix-icon="Lock"
            class="custom-input"
            @keyup.enter="handleAdminLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            class="submit-btn admin-btn" 
            :loading="loading" 
            @click="handleAdminLogin"
          >
            管 理 员 登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 医生登录表单 -->
      <el-form 
        v-else-if="activeTab === 'doctor'" 
        ref="doctorFormRef" 
        :model="doctorForm" 
        :rules="doctorRules" 
        size="large"
      >
        <el-form-item prop="workNo">
          <el-input 
            v-model="doctorForm.workNo" 
            placeholder="请输入坐诊医生工号 (如 doc001)"
            :prefix-icon="User"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="doctorForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            :prefix-icon="Lock"
            class="custom-input"
            @keyup.enter="handleDoctorLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="warning" 
            class="submit-btn doctor-btn" 
            :loading="loading" 
            @click="handleDoctorLogin"
          >
            医 生 登 录
          </el-button>
        </el-form-item>
        <!-- 医生注册辅助链接 -->
        <div class="register-tip">
          还没有医生账号？<span class="register-link doctor-link-color" @click="openDoctorRegisterDialog">立即申请注册</span>
        </div>
      </el-form>

      <!-- 患者登录表单 -->
      <el-form 
        v-else 
        ref="patientFormRef" 
        :model="patientForm" 
        :rules="patientRules" 
        size="large"
      >
        <el-form-item prop="phone">
          <el-input 
            v-model="patientForm.phone" 
            placeholder="请输入就诊手机号或账号名称"
            :prefix-icon="User"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="patientForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            :prefix-icon="Lock"
            class="custom-input"
            @keyup.enter="handlePatientLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="success" 
            class="submit-btn patient-btn" 
            :loading="loading" 
            @click="handlePatientLogin"
          >
            患 者 登 录
          </el-button>
        </el-form-item>
        
        <!-- 注册辅助链接 -->
        <div class="register-tip">
          还没有账号？<span class="register-link" @click="openRegisterDialog">立即注册</span>
        </div>
      </el-form>
    </div>

    <!-- 坐诊医生自助注册对话框 (磨砂玻璃风格) -->
    <el-dialog
      v-model="doctorRegisterDialogVisible"
      title="🩺 坐诊医生注册与资质申报"
      width="500px"
      append-to-body
      class="glass-dialog"
      :close-on-click-modal="false"
      @close="resetDoctorRegisterForm"
    >
      <el-form
        ref="doctorRegisterFormRef"
        :model="doctorRegisterForm"
        :rules="doctorRegisterRules"
        label-position="top"
        size="default"
        class="register-form"
      >
        <el-form-item label="登录工号" prop="workNo">
          <el-input v-model="doctorRegisterForm.workNo" placeholder="请输入您的登录工号 (如 doc017)" />
        </el-form-item>
        
        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="登录密码" prop="password">
              <el-input v-model="doctorRegisterForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="doctorRegisterForm.confirmPassword" type="password" show-password placeholder="再次输入密码" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="name">
              <el-input v-model="doctorRegisterForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生性别" prop="gender">
              <el-radio-group v-model="doctorRegisterForm.gender">
                <el-radio-button label="男">男</el-radio-button>
                <el-radio-button label="女">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="执业职称" prop="title">
              <el-select 
                v-model="doctorRegisterForm.title" 
                placeholder="请选择您的职称" 
                style="width: 100%"
                @change="handleTitleChange"
              >
                <el-option label="主任医师" value="主任医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主治医师" value="主治医师" />
                <el-option label="住院医师" value="住院医师" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属科室" prop="deptId">
              <el-select v-model="doctorRegisterForm.deptId" placeholder="请选择执业科室" style="width: 100%">
                <el-option label="内科" :value="1" />
                <el-option label="外科" :value="2" />
                <el-option label="儿科" :value="3" />
                <el-option label="妇产科" :value="4" />
                <el-option label="眼科" :value="5" />
                <el-option label="皮肤科" :value="6" />
                <el-option label="骨科" :value="7" />
                <el-option label="神经内科" :value="8" />
                <el-option label="急诊科" :value="9" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input 
                v-model="doctorRegisterForm.phone" 
                placeholder="请输入11位手机号码" 
                maxlength="11"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="诊疗费 (元)" prop="consultationFee">
              <el-input-number 
                v-model="doctorRegisterForm.consultationFee" 
                :precision="2" 
                :step="5" 
                :min="10" 
                :max="500" 
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="doctorRegisterDialogVisible = false" class="cancel-btn">取 消</el-button>
          <el-button type="warning" :loading="doctorRegisterLoading" @click="handleDoctorRegister" class="confirm-doctor-btn">申 请 执 业</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 患者自助注册对话框 (磨砂玻璃风格) -->
    <el-dialog
      v-model="registerDialogVisible"
      title="🏥 患者自助注册"
      width="480px"
      append-to-body
      class="glass-dialog"
      :close-on-click-modal="false"
      @close="resetRegisterForm"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        size="default"
        class="register-form"
      >
        <el-form-item label="登录用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用于登录的账号名称" />
        </el-form-item>
        
        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="登录密码" prop="password">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="再次输入密码" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="name">
              <el-input v-model="registerForm.name" placeholder="请输入您的真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者性别" prop="gender">
              <el-radio-group v-model="registerForm.gender">
                <el-radio-button label="男">男</el-radio-button>
                <el-radio-button label="女">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="联系电话" prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="请输入11位手机号码" 
            maxlength="11"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="现居住址" prop="address">
          <el-input v-model="registerForm.address" type="textarea" :rows="2" placeholder="请输入您目前的居住地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="registerDialogVisible = false" class="cancel-btn">取 消</el-button>
          <el-button type="success" :loading="registerLoading" @click="handleRegister" class="confirm-btn">注 册 账 户</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { 
  HomeFilled, 
  User, 
  Lock 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// tab: admin / doctor / patient
const activeTab = ref<'admin' | 'doctor' | 'patient'>('admin')
const loading = ref(false)

// 1. 管理员表单
const adminFormRef = ref<FormInstance>()
const adminForm = reactive({
  username: '',
  password: ''
})
const adminRules = {
  username: [{ required: true, message: '请输入管理员用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 2. 坐诊医生表单
const doctorFormRef = ref<FormInstance>()
const doctorForm = reactive({
  workNo: '',
  password: ''
})
const doctorRules = {
  workNo: [{ required: true, message: '请输入坐诊医生工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 3. 患者表单
const patientFormRef = ref<FormInstance>()
const patientForm = reactive({
  phone: '',
  password: ''
})
const patientRules = {
  phone: [
    { required: true, message: '请输入手机号或账号名称', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 4. 患者注册相关
const registerDialogVisible = ref(false)
const registerLoading = ref(false)
const registerFormRef = ref<FormInstance>()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  gender: '男',
  phone: '',
  address: ''
})

// 5. 坐诊医生注册相关
const doctorRegisterDialogVisible = ref(false)
const doctorRegisterLoading = ref(false)
const doctorRegisterFormRef = ref<FormInstance>()
const doctorRegisterForm = reactive({
  workNo: '',
  password: '',
  confirmPassword: '',
  name: '',
  gender: '男',
  title: '主治医师',
  phone: '',
  deptId: 1,
  consultationFee: 40.00
})

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validateDoctorConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== doctorRegisterForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入您的真实姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const doctorRegisterRules = {
  workNo: [
    { required: true, message: '请输入医生工号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateDoctorConfirmPassword, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入您的真实姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请选择执业职称', trigger: 'change' }
  ],
  deptId: [
    { required: true, message: '请选择执业科室', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的联系电话格式', trigger: 'blur' }
  ],
  consultationFee: [
    { required: true, message: '请输入坐诊诊疗费', trigger: 'blur' }
  ]
}

function changeTab(tab: 'admin' | 'doctor' | 'patient') {
  activeTab.value = tab
}

// 登录成功路由跳转
function goToRedirect() {
  const redirect = route.query.redirect as string || '/'
  router.push(redirect)
}

// 执行管理员登录
async function handleAdminLogin() {
  const valid = await adminFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(adminForm)
    ElMessage.success('系统管理员登录成功')
    goToRedirect()
  } catch (err: any) {
    // 拦截器已处理错误提示
  } finally {
    loading.value = false
  }
}

// 执行坐诊医生登录
async function handleDoctorLogin() {
  const valid = await doctorFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.doctorLogin(doctorForm)
    ElMessage.success('坐诊医生登录成功')
    goToRedirect()
  } catch (err: any) {
    // 拦截器已处理错误提示
  } finally {
    loading.value = false
  }
}

// 执行患者登录
async function handlePatientLogin() {
  const valid = await patientFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.patientLogin(patientForm)
    ElMessage.success('就诊患者登录成功')
    goToRedirect()
  } catch (err: any) {
    // 拦截器已处理错误提示
  } finally {
    loading.value = false
  }
}

// 打开患者注册弹框
function openRegisterDialog() {
  registerDialogVisible.value = true
}

// 重置患者注册表单
function resetRegisterForm() {
  registerFormRef.value?.resetFields()
  registerForm.address = ''
}

// 患者注册事件
async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  registerLoading.value = true
  try {
    await userStore.patientRegister({
      username: registerForm.username,
      password: registerForm.password,
      name: registerForm.name,
      gender: registerForm.gender,
      phone: registerForm.phone,
      address: registerForm.address
    })
    ElMessage.success('注册成功，您现在可以登录了！')
    registerDialogVisible.value = false
    // 自动填充登录框的手机号并切换到患者Tab
    patientForm.phone = registerForm.phone
    activeTab.value = 'patient'
  } catch (err: any) {
    // 拦截器自动提示错误
  } finally {
    registerLoading.value = false
  }
}

// 职称切换，动态生成建议的诊疗费
function handleTitleChange(val: string) {
  if (val === '主任医师') {
    doctorRegisterForm.consultationFee = 80.00
  } else if (val === '副主任医师') {
    doctorRegisterForm.consultationFee = 60.00
  } else if (val === '主治医师') {
    doctorRegisterForm.consultationFee = 40.00
  } else if (val === '住院医师') {
    doctorRegisterForm.consultationFee = 30.00
  }
}

// 打开坐诊医生注册弹框
function openDoctorRegisterDialog() {
  doctorRegisterDialogVisible.value = true
}

// 重置坐诊医生注册表单
function resetDoctorRegisterForm() {
  doctorRegisterFormRef.value?.resetFields()
  doctorRegisterForm.deptId = 1
  doctorRegisterForm.consultationFee = 40.00
}

// 医生注册申请执业
async function handleDoctorRegister() {
  const valid = await doctorRegisterFormRef.value?.validate().catch(() => false)
  if (!valid) return
  doctorRegisterLoading.value = true
  try {
    await userStore.doctorRegister({
      workNo: doctorRegisterForm.workNo,
      password: doctorRegisterForm.password,
      name: doctorRegisterForm.name,
      gender: doctorRegisterForm.gender,
      title: doctorRegisterForm.title,
      phone: doctorRegisterForm.phone,
      deptId: doctorRegisterForm.deptId,
      consultationFee: doctorRegisterForm.consultationFee
    })
    ElMessage.success('执业资格注册成功，您现在可以登录了！')
    doctorRegisterDialogVisible.value = false
    // 自动填充工号，并切换到医生登录 Tab
    doctorForm.workNo = doctorRegisterForm.workNo
    activeTab.value = 'doctor'
  } catch (err: any) {
    // 拦截器自动拦截提示
  } finally {
    doctorRegisterLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at center, #1e293b 0%, #0f172a 100%);
  font-family: 'Inter', sans-serif;
  overflow: hidden;
}

.login-box {
  width: 440px;
  padding: 40px;
  border-radius: 20px;
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(15px);
  border: 1px solid #334155;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.5);
}

.hospital-name {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px 0;
}

.hospital-tagline {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

/* 选项卡样式 */
.tab-container {
  display: flex;
  border-bottom: 2px solid #334155;
  margin-bottom: 25px;
  padding-bottom: 5px;
  gap: 10px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #94a3b8;
}

.tab-item.active {
  color: #3b82f6;
  border-bottom: 2px solid #3b82f6;
  margin-bottom: -7px;
}

.custom-input :deep(.el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
  border-radius: 10px;
}

.custom-input :deep(.el-input__inner) {
  color: #ffffff !important;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6 !important;
}

.submit-btn {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  border: none;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3);
  transition: transform 0.2s;
}

.submit-btn:hover {
  transform: translateY(-1px);
}

.admin-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
}

.doctor-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.patient-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.register-tip {
  text-align: center;
  font-size: 13px;
  color: #64748b;
  margin-top: 15px;
}

.register-link {
  color: #10b981;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s;
}

.register-link:hover {
  color: #34d399;
  text-decoration: underline;
}

.doctor-link-color {
  color: #f59e0b;
}

.doctor-link-color:hover {
  color: #fbbf24;
}

/* 注册弹框样式 */
:deep(.glass-dialog) {
  background: rgba(15, 23, 42, 0.85) !important;
  backdrop-filter: blur(15px);
  border: 1px solid #334155;
  border-radius: 16px;
}

:deep(.glass-dialog .el-dialog__title) {
  color: #ffffff;
  font-weight: 700;
}

:deep(.glass-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #94a3b8;
}

:deep(.glass-dialog .el-form-item__label) {
  color: #94a3b8 !important;
  font-weight: 600;
}

:deep(.glass-dialog .el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

:deep(.glass-dialog .el-input__inner) {
  color: #ffffff !important;
}

:deep(.glass-dialog .el-select .el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

:deep(.glass-dialog .el-textarea__inner) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid #334155 !important;
  color: #ffffff !important;
  box-shadow: none !important;
}

:deep(.glass-dialog .el-radio-button__inner) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid #334155 !important;
  color: #94a3b8 !important;
}

:deep(.glass-dialog .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #f59e0b !important;
  color: #ffffff !important;
  border-color: #f59e0b !important;
}

:deep(.glass-dialog .el-input-number__decrease),
:deep(.glass-dialog .el-input-number__increase) {
  background-color: rgba(30, 41, 59, 0.8) !important;
  border-color: #334155 !important;
  color: #94a3b8 !important;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  background-color: transparent !important;
  border: 1px solid #475569 !important;
  color: #94a3b8 !important;
}

.cancel-btn:hover {
  color: #ffffff !important;
  border-color: #64748b !important;
}

.confirm-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  border: none !important;
  color: white !important;
}

.confirm-doctor-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  border: none !important;
  color: white !important;
}
</style>
