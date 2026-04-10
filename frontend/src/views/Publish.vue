<template>
  <div class="publish-page">
    <h2>发布活动</h2>
    <el-form :model="form" label-position="top" class="publish-form">
      <el-form-item label="活动标题" required>
        <el-input v-model="form.title" placeholder="例如：周末一起爬白云山" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="活动分类" required>
        <el-radio-group v-model="form.category">
          <el-radio-button v-for="c in cats" :key="c" :value="c">{{ c }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="活动描述">
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="详细描述你的活动计划..." maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="活动地点">
        <el-input v-model="form.location" placeholder="例如：深圳南山" />
      </el-form-item>
      <el-form-item label="活动时间">
        <el-input v-model="form.activityTime" placeholder="例如：本周六下午2点" />
      </el-form-item>
      <el-form-item label="需要人数">
        <el-input-number v-model="form.maxPeople" :min="1" :max="50" />
      </el-form-item>
      <el-form-item label="标签（逗号分隔）">
        <el-input v-model="form.tags" placeholder="例如：户外,徒步,交友" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" :loading="submitting" @click="submit" style="width:100%">发布活动</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publishActivity } from '@/api/activity'

const router = useRouter()
const submitting = ref(false)
const cats = ['旅游', '运动', '美食', '电影', '学习', '游戏', '其他']
const form = reactive({
  title: '', category: '运动', description: '', location: '',
  activityTime: '', maxPeople: 2, tags: '',
})

async function submit() {
  if (!form.title.trim()) return ElMessage.warning('请输入活动标题')
  submitting.value = true
  try {
    const id = await publishActivity(form)
    ElMessage.success('发布成功！')
    router.push(`/activity/${id}`)
  } catch (e) { ElMessage.error(e?.message || '发布失败') }
  submitting.value = false
}
</script>

<style scoped>
.publish-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.publish-page h2 { font-size: 22px; font-weight: 800; margin-bottom: 20px; text-align: center; }
.publish-form { background: var(--bg-card); border-radius: 12px; padding: 24px; }
</style>
