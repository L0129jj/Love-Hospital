import { ref, reactive } from 'vue'
import type { PageResult } from '@/types/api'

export function useTable<T>(fetchApi: (params: any) => Promise<any>) {
  const loading = ref(false)
  const tableData = ref<T[]>([])
  const pagination = reactive({ page: 1, size: 10, total: 0 })

  async function loadData(extraParams: Record<string, any> = {}) {
    loading.value = true
    try {
      const res = await fetchApi({ page: pagination.page, size: pagination.size, ...extraParams })
      const pageResult: PageResult<T> = res.data
      tableData.value = pageResult.records
      pagination.total = pageResult.total
    } finally {
      loading.value = false
    }
  }

  function handlePageChange(page: number) {
    pagination.page = page
    loadData()
  }

  function handleSizeChange(size: number) {
    pagination.size = size
    pagination.page = 1
    loadData()
  }

  return { loading, tableData, pagination, loadData, handlePageChange, handleSizeChange }
}
