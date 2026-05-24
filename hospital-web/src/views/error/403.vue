<template>
  <div class="forbidden-container">
    <div class="forbidden-card">
      <h1 class="err-code">403</h1>
      <h2 class="err-title">拒绝访问</h2>
      <p class="err-desc">对不起，您没有权限访问此模块。请联系管理员或切换账号。</p>
      <el-button type="primary" size="large" class="back-btn" @click="goHome">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.forbidden-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #0f172a;
  color: #ffffff;
  font-family: 'Inter', sans-serif;
}

.forbidden-card {
  text-align: center;
  max-width: 480px;
  padding: 40px;
  border-radius: 16px;
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid #334155;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.err-code {
  font-size: 120px;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1;
}

.err-title {
  font-size: 24px;
  font-weight: 600;
  margin: 20px 0 10px 0;
  color: #f1f5f9;
}

.err-desc {
  font-size: 14px;
  color: #94a3b8;
  margin-bottom: 30px;
  line-height: 1.6;
}

.back-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  font-weight: 600;
  padding: 12px 30px;
  border-radius: 8px;
  transition: all 0.3s;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}
</style>
