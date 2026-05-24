import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

export function useForm<T extends Record<string, any>>(initialValues: T) {
  const formRef = ref<FormInstance>()
  const formData = ref<T>({ ...initialValues })
  const submitting = ref(false)
  const dialogVisible = ref(false)

  function resetForm() {
    formData.value = { ...initialValues }
    formRef.value?.resetFields()
  }

  async function submitForm(
    api: (data: T) => Promise<any>,
    successMsg = '操作成功'
  ) {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return false
    submitting.value = true
    try {
      await api(formData.value)
      ElMessage.success(successMsg)
      dialogVisible.value = false
      return true
    } catch {
      return false
    } finally {
      submitting.value = false
    }
  }

  return { formRef, formData, submitting, dialogVisible, resetForm, submitForm }
}
