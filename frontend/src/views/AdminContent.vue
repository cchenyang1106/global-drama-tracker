<template>
  <div class="admin-content-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- 活动管理 -->
      <el-tab-pane label="活动管理" name="activities">
        <div class="toolbar">
          <el-input v-model="activityKeyword" placeholder="搜索活动..." clearable style="width:240px"
            @keyup.enter="loadActivities" @clear="loadActivities" />
          <el-select v-model="activityStatus" placeholder="状态" clearable style="width:120px" @change="loadActivities">
            <el-option label="进行中" :value="1" />
            <el-option label="已关闭" :value="0" />
            <el-option label="已满员" :value="2" />
          </el-select>
          <span class="total-text">共 {{ activityTotal }} 条</span>
        </div>
        <el-table :data="activities" stripe style="width:100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="标题" min-width="150" />
          <el-table-column prop="category" label="分类" width="80" />
          <el-table-column prop="authorName" label="发布者" width="100" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.status === 1" type="success" size="small">进行中</el-tag>
              <el-tag v-else-if="row.status === 0" type="info" size="small">已关闭</el-tag>
              <el-tag v-else type="warning" size="small">已满员</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览" width="60" />
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.status === 1" text size="small" type="warning" @click="closeActivity(row.id)">关闭</el-button>
              <el-button text size="small" type="danger" @click="deleteActivity(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="activityTotal > 20" layout="prev, pager, next" :total="activityTotal" :page-size="20"
          v-model:current-page="activityPage" @current-change="loadActivities" style="margin-top:16px" />
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <div class="toolbar">
          <el-input v-model="userKeyword" placeholder="搜索用户..." clearable style="width:240px"
            @keyup.enter="loadUsers" @clear="loadUsers" />
          <span class="total-text">共 {{ userTotal }} 人</span>
        </div>
        <el-table :data="users" stripe style="width:100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="nickname" label="昵称" width="100" />
          <el-table-column prop="city" label="城市" width="80" />
          <el-table-column label="性别" width="60">
            <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="160" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button text size="small" :type="row.status === 1 ? 'danger' : 'success'" @click="toggleUser(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="userTotal > 20" layout="prev, pager, next" :total="userTotal" :page-size="20"
          v-model:current-page="userPage" @current-change="loadUsers" style="margin-top:16px" />
      </el-tab-pane>

      <!-- 评论管理 -->
      <el-tab-pane label="评论管理" name="comments">
        <div class="toolbar">
          <span class="total-text">共 {{ commentTotal }} 条评论</span>
        </div>
        <el-table :data="comments" stripe style="width:100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="authorName" label="评论者" width="100" />
          <el-table-column prop="activityTitle" label="所属活动" width="150" show-overflow-tooltip />
          <el-table-column prop="hidden" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.hidden ? 'info' : 'success'" size="small">{{ row.hidden ? '已隐藏' : '可见' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="160" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button text size="small" @click="toggleHide(row)">{{ row.hidden ? '显示' : '隐藏' }}</el-button>
              <el-button text size="small" type="danger" @click="deleteComment(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="commentTotal > 20" layout="prev, pager, next" :total="commentTotal" :page-size="20"
          v-model:current-page="commentPage" @current-change="loadComments" style="margin-top:16px" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const activeTab = ref('activities')
function authHeader() {
  return { Authorization: `Bearer ${localStorage.getItem('admin_token')}` }
}

// 活动
const activities = ref([])
const activityTotal = ref(0)
const activityPage = ref(1)
const activityKeyword = ref('')
const activityStatus = ref(null)

async function loadActivities() {
  const params = { page: activityPage.value, size: 20 }
  if (activityKeyword.value) params.keyword = activityKeyword.value
  if (activityStatus.value !== null) params.status = activityStatus.value
  const data = await request.get('/admin/content/activities', { params, headers: authHeader() })
  activities.value = data?.records || []
  activityTotal.value = data?.total || 0
}

async function closeActivity(id) {
  await ElMessageBox.confirm('确定关闭此活动？', '提示')
  await request.post(`/admin/content/activities/${id}/close`, null, { headers: authHeader() })
  ElMessage.success('已关闭')
  loadActivities()
}

async function deleteActivity(id) {
  await ElMessageBox.confirm('确定删除此活动？不可恢复', '警告', { type: 'warning' })
  await request.delete(`/admin/content/activities/${id}`, { headers: authHeader() })
  ElMessage.success('已删除')
  loadActivities()
}

// 用户
const users = ref([])
const userTotal = ref(0)
const userPage = ref(1)
const userKeyword = ref('')

async function loadUsers() {
  const params = { page: userPage.value, size: 20 }
  if (userKeyword.value) params.keyword = userKeyword.value
  const data = await request.get('/admin/content/users', { params, headers: authHeader() })
  users.value = data?.records || []
  userTotal.value = data?.total || 0
}

async function toggleUser(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户 ${row.nickname || row.phone}？`, '提示')
  await request.post(`/admin/content/users/${row.id}/toggle`, null, { headers: authHeader() })
  ElMessage.success(`已${action}`)
  loadUsers()
}

// 评论
const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)

async function loadComments() {
  const data = await request.get('/admin/content/comments', { params: { page: commentPage.value, size: 20 }, headers: authHeader() })
  comments.value = data?.records || []
  commentTotal.value = data?.total || 0
}

async function toggleHide(row) {
  await request.post(`/admin/content/comments/${row.id}/toggle-hide`, null, { headers: authHeader() })
  ElMessage.success(row.hidden ? '已显示' : '已隐藏')
  loadComments()
}

async function deleteComment(id) {
  await ElMessageBox.confirm('确定删除此评论？', '提示')
  await request.delete(`/admin/content/comments/${id}`, { headers: authHeader() })
  ElMessage.success('已删除')
  loadComments()
}

function onTabChange(tab) {
  if (tab === 'activities') loadActivities()
  else if (tab === 'users') loadUsers()
  else loadComments()
}

onMounted(() => { loadActivities(); loadUsers(); loadComments() })
</script>

<style scoped>
.admin-content-page { padding: 24px; }
.toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.total-text { font-size: 13px; color: var(--text-muted); margin-left: auto; }
</style>
