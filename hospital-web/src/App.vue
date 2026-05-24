<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<style>
/* 全局基础重置 */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  background-color: #0f172a;
  overflow: hidden;
}

/* 修改滚动条默认样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #475569;
}

/* 解决表格黑底黑字颜色冲突的全局霸道适配 */
.el-table.custom-table {
  --el-table-border-color: #334155 !important;
  --el-table-text-color: #f1f5f9 !important;
  --el-table-header-bg-color: #0f172a !important;
  --el-table-header-text-color: #94a3b8 !important;
  --el-table-row-hover-bg-color: rgba(51, 65, 85, 0.5) !important;
  background-color: transparent !important;
  color: #f1f5f9 !important;
}

.el-table.custom-table .el-table__inner-wrapper::before {
  background-color: #334155 !important;
}

/* 强制穿透覆盖所有行、单元格、以及单元格内的 cell 文字颜色，100% 杜绝黑字 */
.el-table.custom-table tr,
.el-table.custom-table td.el-table__cell,
.el-table.custom-table th.el-table__cell,
.el-table.custom-table .el-table__row td.el-table__cell .cell,
.el-table.custom-table .el-table__row td.el-table__cell {
  background-color: transparent !important;
  color: #f1f5f9 !important;
}

/* 针对表头的特殊处理 */
.el-table.custom-table th.el-table__cell .cell {
  color: #94a3b8 !important;
  font-weight: 600 !important;
}
</style>
