<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '240px'" class="aside-menu">
      <div class="logo-container" :class="{ collapsed: appStore.sidebarCollapsed }">
        <el-icon size="24" color="#409EFF"><HomeFilled /></el-icon>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">仁爱医院系统</span>
      </div>
      
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          class="el-menu-vertical"
          :collapse="appStore.sidebarCollapsed"
          router
          background-color="transparent"
          text-color="#a0aec0"
          active-text-color="#ffffff"
        >
          <template v-for="menu in filteredMenu" :key="menu.label">
            <!-- 有子菜单 -->
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.label">
              <template #title>
                <el-icon>
                  <component :is="getIconComponent(menu.icon)" />
                </el-icon>
                <span>{{ menu.label }}</span>
              </template>
              
              <el-menu-item 
                v-for="sub in filterChildren(menu.children)" 
                :key="sub.path" 
                :index="sub.path"
                class="sub-item"
              >
                <span>{{ sub.label }}</span>
              </el-menu-item>
            </el-sub-menu>

            <!-- 无子菜单 -->
            <el-menu-item v-else :index="menu.path">
              <el-icon>
                <component :is="getIconComponent(menu.icon)" />
              </el-icon>
              <span>{{ menu.label }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 主体容器 -->
    <el-container class="main-container">
      <!-- 头部导航 -->
      <el-header class="nav-header">
        <div class="header-left">
          <el-icon class="toggle-btn" size="20" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-profile">
              <el-avatar :size="32" :src="userAvatar || undefined" class="avatar-style">
                <span v-if="!userAvatar">{{ userStore.role === 'ROLE_ADMIN' ? '管' : userStore.role === 'ROLE_DOCTOR' ? '医' : '患' }}</span>
              </el-avatar>
              <span class="username">{{ welcomeText }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <span class="logout-text">安全退出</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-main class="page-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { 
  HomeFilled, 
  Setting, 
  Calendar, 
  Briefcase, 
  TrendCharts, 
  Fold, 
  Expand 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 动态头像响应式读取
const userAvatar = ref<string | null>(localStorage.getItem(`hospital_avatar_${userStore.userId}`) || null)

function updateAvatar() {
  userAvatar.value = localStorage.getItem(`hospital_avatar_${userStore.userId}`)
}

onMounted(() => {
  window.addEventListener('avatar-changed', updateAvatar)
})

onUnmounted(() => {
  window.removeEventListener('avatar-changed', updateAvatar)
})

// 映射图标字符串到组件
function getIconComponent(iconName: string) {
  switch (iconName) {
    case 'Setting': return Setting
    case 'Calendar': return Calendar
    case 'Briefcase': return Briefcase
    case 'HomeFilled': return HomeFilled
    case 'TrendCharts': return TrendCharts
    default: return HomeFilled
  }
}

const getRoleText = computed(() => {
  if (userStore.role === 'ROLE_ADMIN') return '系统管理员'
  if (userStore.role === 'ROLE_DOCTOR') return '坐诊医生'
  if (userStore.role === 'ROLE_PATIENT') return '患者用户'
  return '未登录'
})

// 拼接“您好，”+用户真实姓名（若无姓名则使用角色名）
const welcomeText = computed(() => {
  const displayName = userStore.name || getRoleText.value
  return `您好，${displayName}`
})

// 侧边栏菜单配置
const menuConfig: { label: string; icon: string; path?: string; roles?: string[]; children?: any[] }[] = [
  {
    label: '首页', icon: 'HomeFilled', path: '/dashboard', roles: []
  },
  {
    label: '系统管理', icon: 'Setting', roles: ['ROLE_ADMIN'],
    children: [
      { label: '科室管理', path: '/system/depts' },
      { label: '医生管理', path: '/system/doctors' },
      { label: '药品管理', path: '/system/drugs' },
    ]
  },
  {
    label: '排班管理', icon: 'Calendar', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'],
    children: [{ label: '排班列表', path: '/schedule' }]
  },
  {
    label: '门诊管理', icon: 'Briefcase', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT'],
    children: [
      { label: '预约挂号', path: '/outpatient/reg', roles: ['ROLE_ADMIN', 'ROLE_PATIENT'] },
      { label: '挂号患者', path: '/outpatient/reg', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] },
      { label: '电子处方', path: '/outpatient/prescription', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] },
      { label: '门诊缴费', path: '/outpatient/payment', roles: ['ROLE_ADMIN', 'ROLE_PATIENT'] },
    ]
  },
  {
    label: '住院管理', icon: 'HomeFilled', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_PATIENT'],
    children: [
      { label: '住院档案', path: '/inpatient/archive' },
      { label: '每日巡诊', path: '/inpatient/record', roles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] },
      { label: '住院费用', path: '/inpatient/fee' },
    ]
  },
  {
    label: '统计分析', icon: 'TrendCharts', roles: ['ROLE_ADMIN'],
    children: [{ label: '数据大屏', path: '/stats' }]
  },
]

// 根据角色过滤主菜单
const filteredMenu = computed(() => {
  const role = userStore.role || ''
  return menuConfig.filter(menu => {
    if (!menu.roles || menu.roles.length === 0) return true
    return menu.roles.includes(role)
  })
})

// 过滤子菜单选项
function filterChildren(children: any[]) {
  const role = userStore.role || ''
  return children.filter(sub => {
    if (!sub.roles || sub.roles.length === 0) return true
    return sub.roles.includes(role)
  })
}

// 选项操作
function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
  background-color: #0f172a;
  overflow: hidden;
}

/* 侧边栏样式 */
.aside-menu {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  border-right: 1px solid #334155;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid #334155;
  transition: all 0.3s;
}

.logo-container.collapsed {
  padding: 0;
  justify-content: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
  white-space: nowrap;
}

.el-menu-vertical {
  border: none;
  width: 100%;
}

.sub-item {
  padding-left: 50px !important;
}

/* 头部导航样式 */
.nav-header {
  background-color: #1e293b;
  border-bottom: 1px solid #334155;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.toggle-btn {
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}

.toggle-btn:hover {
  color: #ffffff;
}

:deep(.el-breadcrumb__inner) {
  color: #94a3b8 !important;
}
:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #ffffff !important;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.avatar-style {
  background-color: #3b82f6;
  font-weight: bold;
  color: #ffffff;
}

.username {
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
}

.logout-text {
  color: #f87171;
}

/* 内容区域 */
.page-main {
  background-color: #0f172a;
  padding: 24px;
  overflow-y: auto;
}

/* 路由动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-15px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(15px);
}
</style>
