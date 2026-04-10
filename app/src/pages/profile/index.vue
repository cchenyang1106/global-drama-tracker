<template>
  <view class="page">
    <!-- 查看他人资料 -->
    <view v-if="viewUserId" class="card">
      <view class="user-top">
        <view class="big-avatar">{{ (profile.nickname || '?').charAt(0) }}</view>
        <view>
          <text class="uname">{{ profile.nickname || '匿名用户' }}</text>
          <text class="usub">{{ [profile.gender===1?'男':profile.gender===2?'女':'', profile.age?profile.age+'岁':'', profile.city].filter(Boolean).join(' · ') }}</text>
        </view>
      </view>
      <view v-if="profile.occupation" class="info-row">💼 {{ profile.occupation }}</view>
      <view v-if="profile.bio" class="info-row">📝 {{ profile.bio }}</view>
      <view v-if="profile.hobbies" class="hobbies">
        <text class="hlabel">🎯 爱好</text>
        <view class="htags">
          <text v-for="h in profile.hobbies.split(',')" :key="h" class="htag">{{ h.trim() }}</text>
        </view>
      </view>
    </view>

    <!-- 编辑自己资料 -->
    <view v-else class="card">
      <text class="section-title">编辑资料</text>
      <view class="field"><text class="label">昵称</text><input class="input" v-model="form.nickname" placeholder="你的昵称" /></view>
      <view class="field">
        <text class="label">性别</text>
        <view class="cat-list">
          <text :class="['cat-btn', { active: form.gender === 0 }]" @tap="form.gender = 0">保密</text>
          <text :class="['cat-btn', { active: form.gender === 1 }]" @tap="form.gender = 1">男</text>
          <text :class="['cat-btn', { active: form.gender === 2 }]" @tap="form.gender = 2">女</text>
        </view>
      </view>
      <view class="field"><text class="label">年龄</text><input class="input" v-model="form.age" type="number" placeholder="年龄" /></view>
      <view class="field"><text class="label">城市</text><input class="input" v-model="form.city" placeholder="例如：深圳" /></view>
      <view class="field"><text class="label">职业</text><input class="input" v-model="form.occupation" placeholder="例如：程序员" /></view>
      <view class="field"><text class="label">简介</text><textarea class="textarea" v-model="form.bio" placeholder="介绍一下自己..." maxlength="300" /></view>
      <view class="field"><text class="label">爱好（逗号分隔）</text><input class="input" v-model="form.hobbies" placeholder="旅游,摄影,健身" /></view>
      <view class="field"><text class="label">微信号</text><input class="input" v-model="form.wechat" placeholder="匹配成功后对方可见" /></view>
      <button class="save-btn" @tap="save" :loading="saving">保存资料</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyProfile, saveProfile, getUserProfile } from '@/api/profile'

const viewUserId = ref(null)
const profile = ref({})
const saving = ref(false)
const form = reactive({ nickname: '', gender: 0, age: '', city: '', occupation: '', bio: '', hobbies: '', wechat: '' })

async function save() {
  saving.value = true
  try {
    await saveProfile({ ...form, age: form.age ? parseInt(form.age) : null })
    uni.showToast({ title: '保存成功', icon: 'success' })
    uni.setStorageSync('nickname', form.nickname)
  } catch {}
  saving.value = false
}

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  viewUserId.value = page?.options?.userId || null

  if (viewUserId.value) {
    try { profile.value = await getUserProfile(viewUserId.value) } catch {}
  } else {
    try {
      const data = await getMyProfile()
      if (data) {
        form.nickname = data.nickname || ''
        form.gender = data.gender || 0
        form.age = data.age ? String(data.age) : ''
        form.city = data.city || ''
        form.occupation = data.occupation || ''
        form.bio = data.bio || ''
        form.hobbies = data.hobbies || ''
        form.wechat = data.wechat || ''
      }
    } catch {}
  }
})
</script>

<style>
.page { padding: 24rpx; background: #fff5f7; min-height: 100vh; }
.card { background: #ffffff; border-radius: 20rpx; padding: 32rpx; }
.section-title { display: block; font-size: 36rpx; font-weight: 800; color: #f472b6; margin-bottom: 24rpx; text-align: center; }
.user-top { display: flex; gap: 20rpx; align-items: center; margin-bottom: 24rpx; }
.big-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; background: #f472b6; color: white; display: flex; align-items: center; justify-content: center; font-size: 40rpx; font-weight: 700; }
.uname { display: block; font-size: 36rpx; font-weight: 800; color: #4a2040; }
.usub { display: block; font-size: 26rpx; color: #b8929e; margin-top: 4rpx; }
.info-row { padding: 16rpx 0; border-top: 1rpx solid rgba(255,255,255,0.05); font-size: 28rpx; color: #7c5270; }
.hobbies { padding: 16rpx 0; border-top: 1rpx solid rgba(255,255,255,0.05); }
.hlabel { font-size: 28rpx; color: #b8929e; }
.htags { display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 8rpx; }
.htag { background: rgba(99,102,241,0.15); color: #f472b6; padding: 6rpx 16rpx; border-radius: 16rpx; font-size: 24rpx; }
.field { margin-bottom: 24rpx; }
.label { display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600; }
.input { background: #fff5f7; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx; }
.textarea { background: #fff5f7; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx; width: 100%; min-height: 160rpx; }
.cat-list { display: flex; gap: 12rpx; }
.cat-btn { padding: 12rpx 24rpx; border-radius: 32rpx; background: #fff5f7; color: #7c5270; font-size: 26rpx; }
.cat-btn.active { background: rgba(99,102,241,0.2); color: #f472b6; }
.save-btn { background: linear-gradient(135deg,#f472b6,#c084fc); color: white; border-radius: 12rpx; font-size: 32rpx; font-weight: 700; }
</style>
