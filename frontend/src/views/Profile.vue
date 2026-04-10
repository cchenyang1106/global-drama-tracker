<template>
  <div class="profile-page">
    <h2>个人资料</h2>
    <el-form :model="form" label-position="top" class="profile-form">
      <!-- 头像上传 -->
      <el-form-item label="头像">
        <div class="avatar-upload" @click="triggerAvatarUpload">
          <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar-preview" />
          <div v-else class="avatar-placeholder">📷<br><small>点击上传</small></div>
          <div class="avatar-overlay">更换</div>
        </div>
        <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="onAvatarChange" />
      </el-form-item>

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

      <!-- 个人照片上传 -->
      <el-form-item label="个人照片（最多6张）">
        <div class="photos-grid">
          <div v-for="(photo, index) in photoList" :key="index" class="photo-item">
            <img :src="photo" class="photo-preview" />
            <div class="photo-remove" @click="removePhoto(index)">✕</div>
          </div>
          <div v-if="photoList.length < 6" class="photo-add" @click="triggerPhotoUpload">
            <span>＋</span>
            <small>添加照片</small>
          </div>
        </div>
        <input ref="photoInput" type="file" accept="image/*" style="display:none" @change="onPhotoChange" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" size="large" :loading="saving" @click="save" style="width:100%;border-radius:20px">
          保存资料
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyProfile, saveProfile } from '@/api/profile'
import { uploadImage } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const saving = ref(false)
const avatarInput = ref(null)
const photoInput = ref(null)
const photoList = ref([])

const form = reactive({
  nickname: '', avatarUrl: '', gender: 0, age: null, city: '', occupation: '',
  bio: '', hobbies: '', wechat: '',
})

function triggerAvatarUpload() { avatarInput.value?.click() }
function triggerPhotoUpload() { photoInput.value?.click() }

async function compressAndUpload(file) {
  // 前端压缩到 800px 宽度
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const maxW = 800
        let w = img.width, h = img.height
        if (w > maxW) { h = (maxW / w) * h; w = maxW }
        canvas.width = w
        canvas.height = h
        canvas.getContext('2d').drawImage(img, 0, 0, w, h)
        resolve(canvas.toDataURL('image/jpeg', 0.7))
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

async function onAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) return ElMessage.warning('图片不能超过5MB')
  try {
    const dataUrl = await compressAndUpload(file)
    form.avatarUrl = dataUrl
    ElMessage.success('头像已更新')
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

async function onPhotoChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) return ElMessage.warning('图片不能超过5MB')
  if (photoList.value.length >= 6) return ElMessage.warning('最多6张照片')
  try {
    const dataUrl = await compressAndUpload(file)
    photoList.value.push(dataUrl)
    ElMessage.success('照片已添加')
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

function removePhoto(index) {
  photoList.value.splice(index, 1)
}

async function load() {
  try {
    const data = await getMyProfile()
    if (data) {
      form.nickname = data.nickname || ''
      form.avatarUrl = data.avatarUrl || ''
      form.gender = data.gender || 0
      form.age = data.age || null
      form.city = data.city || ''
      form.occupation = data.occupation || ''
      form.bio = data.bio || ''
      form.hobbies = data.hobbies || ''
      form.wechat = data.wechat || ''
      try { photoList.value = data.photos ? JSON.parse(data.photos) : [] } catch { photoList.value = [] }
    }
  } catch { /* ignore */ }
}

async function save() {
  saving.value = true
  try {
    await saveProfile({
      ...form,
      photos: JSON.stringify(photoList.value),
    })
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
.profile-page h2 { font-size: 22px; font-weight: 800; margin-bottom: 20px; text-align: center; color: var(--primary-dark); }
.profile-form { background: var(--bg-card); border-radius: 16px; padding: 24px; border: 1px solid var(--border-color); }

/* 头像上传 */
.avatar-upload {
  width: 100px; height: 100px; border-radius: 50%; overflow: hidden;
  cursor: pointer; position: relative; border: 3px solid var(--border-color);
  transition: all 0.2s;
}
.avatar-upload:hover { border-color: var(--primary); }
.avatar-upload:hover .avatar-overlay { opacity: 1; }
.avatar-preview { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder {
  width: 100%; height: 100%; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  background: var(--bg-card-hover); color: var(--text-muted); font-size: 28px;
}
.avatar-placeholder small { font-size: 11px; margin-top: 2px; }
.avatar-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: rgba(0,0,0,0.5); color: white; text-align: center;
  padding: 4px; font-size: 12px; opacity: 0; transition: opacity 0.2s;
}

/* 照片上传 */
.photos-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px;
}
.photo-item {
  position: relative; aspect-ratio: 1; border-radius: 12px; overflow: hidden;
  border: 1px solid var(--border-color);
}
.photo-preview { width: 100%; height: 100%; object-fit: cover; }
.photo-remove {
  position: absolute; top: 4px; right: 4px;
  width: 24px; height: 24px; border-radius: 50%;
  background: rgba(244,63,94,0.9); color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; cursor: pointer; opacity: 0; transition: opacity 0.2s;
}
.photo-item:hover .photo-remove { opacity: 1; }
.photo-add {
  aspect-ratio: 1; border-radius: 12px;
  border: 2px dashed var(--border-color);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; color: var(--text-muted); transition: all 0.2s;
}
.photo-add:hover { border-color: var(--primary); color: var(--primary); }
.photo-add span { font-size: 28px; line-height: 1; }
.photo-add small { font-size: 12px; margin-top: 4px; }
</style>
