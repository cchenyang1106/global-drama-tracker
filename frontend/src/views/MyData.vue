<template>
  <div class="data-page">
    <div class="data-card">
      <h2>📦 我的数据</h2>
      <p class="desc">根据《个人信息保护法》，你有权导出和删除你的所有个人数据。</p>

      <div class="action-section">
        <h3>📥 导出数据</h3>
        <p>导出你的所有个人信息、发布的活动、评论和聊天记录（JSON 格式）。</p>
        <el-button type="primary" @click="doExport" :loading="exporting" round>导出我的数据</el-button>
      </div>

      <el-divider />

      <div class="action-section danger-section">
        <h3>⚠️ 注销账号</h3>
        <p>注销后将<strong>永久删除</strong>你的所有数据，包括个人资料、发布的活动、评论和聊天记录。此操作不可撤销。</p>
        <el-button type="danger" @click="showDeleteConfirm = true" round plain>注销我的账号</el-button>
      </div>
    </div>

    <!-- 注销确认弹窗 -->
    <el-dialog v-model="showDeleteConfirm" title="确认注销账号" width="400px">
      <p style="color: #e11d48; font-weight: 600; margin-bottom: 12px;">⚠️ 此操作不可恢复！</p>
      <p>注销后将永久删除：</p>
      <ul style="margin: 8px 0 16px 20px; font-size: 14px; color: var(--text-secondary);">
        <li>你的个人资料和照片</li>
        <li>你发布的所有活动</li>
        <li>你的评论和聊天记录</li>
      </ul>
      <p>请输入 <strong>确认注销</strong> 来确认：</p>
      <el-input v-model="deleteConfirmText" placeholder="确认注销" style="margin-top: 8px;" />
      <template #footer>
        <el-button @click="showDeleteConfirm = false" round>取消</el-button>
        <el-button type="danger" @click="doDelete" :loading="deleting"
          :disabled="deleteConfirmText !== '确认注销'" round>永久注销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const exporting = ref(false)
const deleting = ref(false)
const showDeleteConfirm = ref(false)
const deleteConfirmText = ref('')

const authHeader = () => ({ headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } })

async function doExport() {
  exporting.value = true
  try {
    const res = await request.get('/user-data/export', authHeader())
    // 下载为 JSON 文件
    const blob = new Blob([JSON.stringify(res, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `my-data-${new Date().toISOString().split('T')[0]}.json`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('数据导出成功')
  } catch (e) { ElMessage.error(e?.message || '导出失败') }
  exporting.value = false
}

async function doDelete() {
  deleting.value = true
  try {
    await request.post('/user-data/delete-account', {}, authHeader())
    ElMessage.success('账号已注销')
    userStore.logout()
    router.push('/')
  } catch (e) { ElMessage.error(e?.message || '注销失败') }
  deleting.value = false
}
</script>

<style scoped>
.data-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.data-card { background: var(--bg-card); border-radius: 16px; padding: 24px; border: 1px solid var(--border-color); }
h2 { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
h3 { font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.desc { color: var(--text-muted); font-size: 13px; margin-bottom: 20px; }
.action-section { margin: 16px 0; }
.action-section p { font-size: 14px; color: var(--text-secondary); margin-bottom: 12px; line-height: 1.6; }
.danger-section h3 { color: #e11d48; }
</style>
