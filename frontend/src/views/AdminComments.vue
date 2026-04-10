<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1>💬 评论管理</h1>
      <p class="subtitle">查看、加精、删除用户评论</p>
    </div>

    <div class="comment-table">
      <div v-if="loading" style="text-align:center;padding:40px">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="comments.length === 0" style="text-align:center;padding:40px;color:#94a3b8">
        暂无评论
      </div>
      <div v-else>
        <div v-for="c in comments" :key="c.id" class="comment-row" :class="{ featured: c.featured }">
          <div class="row-main">
            <div class="row-header">
              <span class="author">{{ c.nickname }}</span>
              <el-tag v-if="c.featured" type="warning" size="small">精选</el-tag>
              <el-tag v-if="c.rating" size="small">{{ Number(c.rating).toFixed(1) }}分</el-tag>
              <span class="drama-name" v-if="c.dramaTitle">{{ c.dramaTitle }}</span>
              <span class="time">{{ formatTime(c.createTime) }}</span>
            </div>
            <p class="row-content">{{ c.content }}</p>
            <div class="row-stats">
              👍 {{ c.likeCount || 0 }}
            </div>
          </div>
          <div class="row-actions">
            <el-button v-if="!c.featured" type="warning" size="small" @click="handleFeatured(c, true)">加精</el-button>
            <el-button v-else size="small" @click="handleFeatured(c, false)">取消加精</el-button>
            <el-popconfirm title="确定删除这条评论？" @confirm="handleDelete(c.id)">
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="page"
            :page-size="20"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadComments"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllComments, setFeatured, deleteCommentAdmin } from '@/api/comment'

const comments = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

async function loadComments() {
  loading.value = true
  try {
    const data = await getAllComments(page.value, 20)
    comments.value = data.records || data.list || []
    total.value = data.total || 0
  } catch {
    ElMessage.error('加载评论失败')
  }
  loading.value = false
}

async function handleFeatured(comment, featured) {
  try {
    await setFeatured(comment.id, featured)
    comment.featured = featured
    ElMessage.success(featured ? '已加精' : '已取消加精')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(id) {
  try {
    await deleteCommentAdmin(id)
    comments.value = comments.value.filter(c => c.id !== id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(loadComments)
</script>

<style scoped>
.admin-page { max-width: 960px; margin: 0 auto; padding: 32px 28px 80px; }
.admin-header { text-align: center; margin-bottom: 32px; }
.admin-header h1 { font-size: 1.8rem; background: linear-gradient(135deg, #60a5fa, #a78bfa); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 6px; }
.subtitle { color: #94a3b8; }

.comment-row {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}
.comment-row.featured {
  border-color: rgba(245, 158, 11, 0.4);
  background: rgba(245, 158, 11, 0.05);
}
.row-main { flex: 1; min-width: 0; }
.row-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; flex-wrap: wrap; }
.author { font-weight: 600; color: #60a5fa; font-size: 14px; }
.drama-name { color: #94a3b8; font-size: 12px; }
.time { color: #64748b; font-size: 12px; margin-left: auto; }
.row-content { font-size: 14px; color: #cbd5e1; line-height: 1.6; margin-bottom: 6px; }
.row-stats { font-size: 12px; color: #64748b; }
.row-actions { display: flex; flex-direction: column; gap: 6px; flex-shrink: 0; }
.pagination { text-align: center; margin-top: 20px; }
</style>
