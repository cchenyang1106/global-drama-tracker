<template>
  <div class="profile-page">
    <h2>个人资料</h2>
    <el-form :model="form" label-position="top" class="profile-form">
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="给自己取个昵称" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :value="0">保密</el-radio>
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="年龄">
        <el-input-number v-model="form.age" :min="16" :max="80" />
      </el-form-item>
      <el-form-item label="所在城市">
        <el-input v-model="form.city" placeholder="例如：深圳" />
      </el-form-item>
      <el-form-item label="职业">
        <el-input v-model="form.occupation" placeholder="例如：程序员" />
      </el-form-item>
      <el-form-item label="个人简介">
        <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="介绍一下自己..." maxlength="300" show-word-limit />
      </el-form-item>
      <el-form-item label="兴趣爱好（逗号分隔）">
        <el-input v-model="form.hobbies" placeholder="例如：旅游,摄影,健身,美食" />
      </el-form-item>
      <el-form-item label="微信号（匹配成功后对方可见）">
        <el-input v-model="form.wechat" placeholder="你的微信号" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" :loading="saving" @click="save" style="width:100%">保存资料</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyProfile, saveProfile } from '@/api/profile'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const saving = ref(false)
const form = reactive({
  nickname: '', gender: 0, age: null, city: '', occupation: '',
  bio: '', hobbies: '', wechat: '',
})

async function load() {
  try {
    const data = await getMyProfile()
    if (data) {
      form.nickname = data.nickname || ''
      form.gender = data.gender || 0
      form.age = data.age || null
      form.city = data.city || ''
      form.occupation = data.occupation || ''
      form.bio = data.bio || ''
      form.hobbies = data.hobbies || ''
      form.wechat = data.wechat || ''
    }
  } catch { /* ignore */ }
}

async function save() {
  saving.value = true
  try {
    await saveProfile(form)
    ElMessage.success('保存成功！')
    userStore.nickname = form.nickname
    localStorage.setItem('nickname', form.nickname)
  } catch (e) { ElMessage.error(e?.message || '保存失败') }
  saving.value = false
}

onMounted(load)
</script>

<style scoped>
.profile-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.profile-page h2 { font-size: 22px; font-weight: 800; margin-bottom: 20px; text-align: center; }
.profile-form { background: var(--bg-card); border-radius: 12px; padding: 24px; }
</style>
