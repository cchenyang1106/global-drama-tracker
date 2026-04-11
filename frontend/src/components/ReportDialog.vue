<template>
  <el-dialog v-model="visible" title="举报" width="420px" @close="close">
    <el-form label-position="top">
      <el-form-item label="举报原因">
        <el-radio-group v-model="reason">
          <el-radio value="色情低俗">色情低俗</el-radio>
          <el-radio value="辱骂骚扰">辱骂骚扰</el-radio>
          <el-radio value="虚假信息">虚假信息</el-radio>
          <el-radio value="广告推销">广告推销</el-radio>
          <el-radio value="诈骗欺诈">诈骗欺诈</el-radio>
          <el-radio value="其他">其他</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="详细说明（选填）">
        <el-input v-model="detail" type="textarea" :rows="3" placeholder="请描述具体情况..." maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close" round>取消</el-button>
      <el-button type="danger" @click="submit" :loading="loading" :disabled="!reason" round>提交举报</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { submitReport } from '@/api/report'

const props = defineProps({
  modelValue: Boolean,
  targetType: String,  // user / activity / comment / chat
  targetId: [Number, String],
})
const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const reason = ref('')
const detail = ref('')
const loading = ref(false)

watch(() => props.modelValue, v => { visible.value = v })
watch(visible, v => emit('update:modelValue', v))

function close() { visible.value = false; reason.value = ''; detail.value = '' }

async function submit() {
  loading.value = true
  try {
    await submitReport({
      targetType: props.targetType,
      targetId: Number(props.targetId),
      reason: reason.value,
      detail: detail.value,
    })
    ElMessage.success('举报已提交，我们会尽快处理')
    close()
  } catch (e) { ElMessage.error(e?.message || '提交失败') }
  loading.value = false
}
</script>
