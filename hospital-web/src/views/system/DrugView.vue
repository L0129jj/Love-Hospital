<template>
  <div class="page-container">
    <!-- 头部操作区 -->
    <div class="page-header">
      <h2 class="page-title">药品库管理</h2>
      <el-button type="primary" class="gradient-btn" @click="handleCreate">
        新增药品
      </el-button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="6">
          <el-input 
            v-model="searchQuery" 
            placeholder="输入药品名称搜索..." 
            clearable 
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            class="custom-search-input"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 列表数据 -->
    <div class="content-card">
      <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="药品ID" width="100" />
        <el-table-column prop="name" label="药品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="价格(元)" width="120">
          <template #default="scope">
            <span class="price-text">￥{{ scope.row.price?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存余量" width="120" align="center">
          <template #default="scope">
            <el-tag :type="(scope.row.stock || 0) < 50 ? 'danger' : 'success'">
              {{ scope.row.stock || 0 }} {{ scope.row.unit || '盒' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalElements"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="custom-pagination"
        />
      </div>
    </div>

    <!-- 弹窗对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="550px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left">
        <el-form-item label="药品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入药品名称" class="custom-dialog-input" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="form.specification" placeholder="如 0.25g*12粒" class="custom-dialog-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如 盒/瓶/袋" class="custom-dialog-input" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单价 (元)" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" class="custom-dialog-number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" class="custom-dialog-number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="药品说明" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="请输入使用说明或注意事项" class="custom-dialog-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getDrugPage, insertDrug, updateDrug, deleteDrug } from '@/api/system'
import type { DrugVO } from '@/types/system'

const loading = ref(false)
const tableData = ref<DrugVO[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增药品')
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 分页与搜索状态
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

const form = ref({
  id: undefined as number | undefined,
  name: '',
  specification: '0.25g*24片',
  unit: '盒',
  price: 15.00,
  stock: 100,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入初始库存', trigger: 'blur' }]
}

// 模拟初始药品列表做鲁棒展示
const mockDrugs: DrugVO[] = [
  { id: 1, name: '阿莫西林胶囊', specification: '0.25g*24粒', unit: '盒', price: 18.5, stock: 500, description: '广谱青霉素类抗生素。' },
  { id: 2, name: '布洛芬缓释胶囊', specification: '0.3g*10粒', unit: '盒', price: 24.0, stock: 30, description: '用于缓解轻至中度疼痛如头痛、关节痛等。' },
  { id: 3, name: '对乙酰氨基酚片', specification: '0.5g*10片', unit: '盒', price: 9.8, stock: 120, description: '用于普通感冒或流行性感冒引起的发热。' },
  { id: 4, name: '盐酸二甲双胍片', specification: '0.5g*30片', unit: '盒', price: 32.5, stock: 45, description: '双胍类口服降血糖药。' },
  { id: 5, name: '阿司匹林肠溶片', specification: '100mg*30片', unit: '盒', price: 12.0, stock: 350, description: '抗血小板聚集。' }
]

async function loadDrugs() {
  loading.value = true
  try {
    const res = await getDrugPage({
      page: currentPage.value,
      size: pageSize.value,
      name: searchQuery.value || undefined
    })
    if (res.data && res.data.records) {
      tableData.value = res.data.records
      totalElements.value = res.data.total
    } else {
      tableData.value = [...mockDrugs]
      totalElements.value = mockDrugs.length
    }
  } catch {
    // 降级使用Mock数据展示
    tableData.value = mockDrugs.filter(d => 
      !searchQuery.value || d.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
    totalElements.value = tableData.value.length
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadDrugs()
}

function handleSizeChange(val: number) {
  pageSize.value = val
  currentPage.value = 1
  loadDrugs()
}

function handleCurrentChange(val: number) {
  currentPage.value = val
  loadDrugs()
}

function handleCreate() {
  dialogTitle.value = '新增药品'
  form.value = {
    id: undefined,
    name: '',
    specification: '0.25g*24片',
    unit: '盒',
    price: 15.00,
    stock: 100,
    description: ''
  }
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑药品'
  form.value = {
    id: row.id,
    name: row.name,
    specification: row.specification || '0.25g*24片',
    unit: row.unit || '盒',
    price: row.price || 15.00,
    stock: row.stock || 0,
    description: row.description || ''
  }
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除药品「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteDrug(row.id)
    ElMessage.success('删除成功')
    loadDrugs()
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err?.message || '删除失败')
    }
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      name: form.value.name,
      specification: form.value.specification,
      unit: form.value.unit,
      price: form.value.price,
      stock: form.value.stock,
      description: form.value.description
    }

    if (form.value.id) {
      await updateDrug(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await insertDrug(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadDrugs()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadDrugs()
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

.gradient-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  border: none !important;
  font-weight: 600;
}

.filter-card {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 20px;
}

.custom-search-input :deep(.el-input__wrapper) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-search-input :deep(.el-input__inner) {
  color: #ffffff !important;
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
  font-weight: 600;
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

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 分页定制 */
.custom-pagination :deep(.el-pagination__total),
.custom-pagination :deep(.el-pagination__sizes),
.custom-pagination :deep(.el-pagination__jump) {
  color: #94a3b8 !important;
}

.custom-pagination :deep(.el-pager li) {
  background: #0f172a !important;
  color: #94a3b8 !important;
  border: 1px solid #334155;
}

.custom-pagination :deep(.el-pager li.is-active) {
  background: #3b82f6 !important;
  color: #ffffff !important;
  border-color: #3b82f6;
}

.custom-pagination :deep(.btn-prev),
.custom-pagination :deep(.btn-next) {
  background: #0f172a !important;
  color: #94a3b8 !important;
  border: 1px solid #334155 !important;
}

/* 弹窗定制 */
.custom-dialog :deep(.el-dialog) {
  background-color: #1e293b !important;
  border: 1px solid #334155;
  border-radius: 16px;
}

.custom-dialog :deep(.el-dialog__title) {
  color: #ffffff !important;
}

.custom-dialog-input :deep(.el-input__wrapper),
.custom-dialog-input :deep(.el-textarea__inner) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
  color: #ffffff !important;
}

.custom-dialog-input :deep(.el-input__inner) {
  color: #ffffff !important;
}

.custom-dialog-number :deep(.el-input__wrapper) {
  background-color: #0f172a !important;
  border: 1px solid #334155 !important;
  box-shadow: none !important;
}

.custom-dialog-number :deep(.el-input__inner) {
  color: #ffffff !important;
}

.dialog-footer button {
  border-radius: 8px;
}
</style>
