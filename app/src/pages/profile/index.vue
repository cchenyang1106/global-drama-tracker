<template>
  <view style="padding: 24rpx; background: #fff5f7; min-height: 100vh;">
    <!-- 查看他人资料 -->
    <view v-if="viewUserId && loading" style="text-align:center;padding:100rpx;color:#b8929e;">加载中...</view>
    <view v-else-if="viewUserId && !profile.nickname && !profile.city" style="background:#fff;border-radius:20rpx;padding:60rpx;text-align:center;">
      <text style="font-size:30rpx;color:#b8929e;display:block;">该用户暂未填写个人资料</text>
    </view>
    <view v-else-if="viewUserId" style="background: #fff; border-radius: 20rpx; padding: 32rpx;">
      <view style="display: flex; gap: 20rpx; align-items: center; margin-bottom: 24rpx;">
        <image v-if="profile.avatarUrl" :src="profile.avatarUrl" style="width: 100rpx; height: 100rpx; border-radius: 50%;" mode="aspectFill" />
        <view v-else style="width: 100rpx; height: 100rpx; border-radius: 50%; background: #f472b6; color: white; display: flex; align-items: center; justify-content: center; font-size: 40rpx; font-weight: 700;">
          {{ (profile.nickname || '?').charAt(0) }}
        </view>
        <view>
          <text style="display: block; font-size: 36rpx; font-weight: 800; color: #4a2040;">{{ profile.nickname || '匿名用户' }}</text>
          <text style="display: block; font-size: 26rpx; color: #b8929e; margin-top: 4rpx;">{{ [profile.gender===1?'男':profile.gender===2?'女':'', profile.age?profile.age+'岁':'', profile.city].filter(Boolean).join(' · ') }}</text>
        </view>
      </view>
      <view v-if="profile.occupation" style="padding: 12rpx 0; border-top: 1rpx solid #fce4ec; font-size: 28rpx; color: #7c5270;">💼 {{ profile.occupation }}</view>
      <view v-if="profile.bio" style="padding: 12rpx 0; border-top: 1rpx solid #fce4ec; font-size: 28rpx; color: #7c5270;">📝 {{ profile.bio }}</view>
      <view v-if="profile.hobbies" style="padding: 12rpx 0; border-top: 1rpx solid #fce4ec;">
        <text style="font-size: 28rpx; color: #b8929e;">🎯 爱好</text>
        <view style="display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 8rpx;">
          <text v-for="h in profile.hobbies.split(',')" :key="h" style="background: rgba(244,114,182,0.12); color: #f472b6; padding: 6rpx 16rpx; border-radius: 16rpx; font-size: 24rpx;">{{ h.trim() }}</text>
        </view>
      </view>
      <!-- 照片 -->
      <view v-if="photoList.length > 0" style="padding: 12rpx 0; border-top: 1rpx solid #fce4ec;">
        <text style="font-size: 28rpx; color: #b8929e;">📸 照片</text>
        <view style="display: flex; flex-wrap: wrap; gap: 10rpx; margin-top: 10rpx;">
          <image v-for="(p, i) in photoList" :key="i" :src="p" style="width: 200rpx; height: 200rpx; border-radius: 12rpx;" mode="aspectFill" @tap="previewImage(i)" />
        </view>
      </view>
    </view>

    <!-- 编辑自己资料 -->
    <view v-else style="background: #fff; border-radius: 20rpx; padding: 32rpx;">
      <text style="display: block; font-size: 36rpx; font-weight: 800; color: #f472b6; margin-bottom: 24rpx; text-align: center;">编辑资料</text>

      <!-- 头像上传 -->
      <view style="text-align: center; margin-bottom: 24rpx;">
        <view @tap="chooseAvatar" style="display: inline-block; position: relative;">
          <image v-if="form.avatarUrl" :src="form.avatarUrl" style="width: 140rpx; height: 140rpx; border-radius: 50%; border: 4rpx solid #fce4ec;" mode="aspectFill" />
          <view v-else style="width: 140rpx; height: 140rpx; border-radius: 50%; background: #fff5f7; border: 4rpx dashed #fce4ec; display: flex; align-items: center; justify-content: center; font-size: 48rpx; color: #b8929e;">📷</view>
          <text style="display: block; font-size: 22rpx; color: #b8929e; margin-top: 8rpx;">点击更换头像</text>
        </view>
      </view>

      <view style="margin-bottom: 24rpx;"><text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">昵称</text><input v-model="form.nickname" placeholder="你的昵称" style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx;" /></view>
      <view style="margin-bottom: 24rpx;">
        <text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">性别</text>
        <view style="display: flex; gap: 12rpx;">
          <text @tap="form.gender = 0" :style="'padding: 12rpx 24rpx; border-radius: 32rpx; font-size: 26rpx;' + (form.gender === 0 ? 'background: rgba(244,114,182,0.15); color: #f472b6;' : 'background: #fff5f7; color: #7c5270;')">保密</text>
          <text @tap="form.gender = 1" :style="'padding: 12rpx 24rpx; border-radius: 32rpx; font-size: 26rpx;' + (form.gender === 1 ? 'background: rgba(244,114,182,0.15); color: #f472b6;' : 'background: #fff5f7; color: #7c5270;')">男</text>
          <text @tap="form.gender = 2" :style="'padding: 12rpx 24rpx; border-radius: 32rpx; font-size: 26rpx;' + (form.gender === 2 ? 'background: rgba(244,114,182,0.15); color: #f472b6;' : 'background: #fff5f7; color: #7c5270;')">女</text>
        </view>
      </view>
      <view style="margin-bottom: 24rpx;"><text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">年龄</text><input v-model="form.age" type="number" placeholder="年龄" style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx;" /></view>
      <view style="margin-bottom: 24rpx;"><text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">城市</text><input v-model="form.city" placeholder="例如：深圳" style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx;" /></view>
      <view style="margin-bottom: 24rpx;"><text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">职业</text><input v-model="form.occupation" placeholder="例如：程序员" style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx;" /></view>
      <view style="margin-bottom: 24rpx;"><text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">简介</text><textarea v-model="form.bio" placeholder="介绍一下自己..." maxlength="300" style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx; width: 100%; min-height: 160rpx;" /></view>
      <view style="margin-bottom: 24rpx;"><text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">爱好（逗号分隔）</text><input v-model="form.hobbies" placeholder="旅游,摄影,健身" style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx;" /></view>
      <view style="margin-bottom: 24rpx;"><text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">微信号</text><input v-model="form.wechat" placeholder="组队成功后对方可见" style="background: #fff5f7; border: 2rpx solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx;" /></view>

      <!-- 照片上传 -->
      <view style="margin-bottom: 24rpx;">
        <text style="display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600;">个人照片（最多6张）</text>
        <view style="display: flex; flex-wrap: wrap; gap: 10rpx;">
          <view v-for="(p, i) in photoList" :key="i" style="position: relative;">
            <image :src="p" style="width: 180rpx; height: 180rpx; border-radius: 12rpx;" mode="aspectFill" />
            <text @tap="removePhoto(i)" style="position: absolute; top: 4rpx; right: 4rpx; width: 40rpx; height: 40rpx; border-radius: 50%; background: rgba(244,63,94,0.9); color: white; display: flex; align-items: center; justify-content: center; font-size: 24rpx;">✕</text>
          </view>
          <view v-if="photoList.length < 6" @tap="choosePhoto" style="width: 180rpx; height: 180rpx; border-radius: 12rpx; border: 2rpx dashed #fce4ec; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #b8929e;">
            <text style="font-size: 48rpx;">＋</text>
            <text style="font-size: 22rpx;">添加照片</text>
          </view>
        </view>
      </view>

      <button @tap="save" :loading="saving" style="background: linear-gradient(135deg,#f472b6,#c084fc); color: white; border: none; border-radius: 12rpx; font-size: 32rpx; font-weight: 700;">保存资料</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyProfile, saveProfile, getUserProfile } from '@/api/profile'

const viewUserId = ref(null)
const profile = ref({})
const saving = ref(false)
const loading = ref(false)
const photoList = ref([])
const form = reactive({ nickname: '', avatarUrl: '', gender: 0, age: '', city: '', occupation: '', bio: '', hobbies: '', wechat: '' })

function fileToBase64(filePath) {
  return new Promise((resolve) => {
    // #ifdef H5
    resolve(filePath)
    // #endif
    // #ifndef H5
    uni.getFileSystemManager().readFile({
      filePath,
      encoding: 'base64',
      success: (res) => resolve('data:image/jpeg;base64,' + res.data),
      fail: () => resolve(filePath),
    })
    // #endif
  })
}

async function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const path = res.tempFilePaths[0]
      form.avatarUrl = await fileToBase64(path)
    },
  })
}

async function choosePhoto() {
  if (photoList.value.length >= 6) return uni.showToast({ title: '最多6张', icon: 'none' })
  uni.chooseImage({
    count: 6 - photoList.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      for (const path of res.tempFilePaths) {
        if (photoList.value.length >= 6) break
        const base64 = await fileToBase64(path)
        photoList.value.push(base64)
      }
    },
  })
}

function removePhoto(i) { photoList.value.splice(i, 1) }

function previewImage(idx) {
  uni.previewImage({ urls: photoList.value, current: idx })
}

async function save() {
  saving.value = true
  try {
    await saveProfile({
      ...form,
      age: form.age ? parseInt(form.age) : null,
      photos: JSON.stringify(photoList.value),
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    uni.setStorageSync('nickname', form.nickname)
  } catch {}
  saving.value = false
}

onLoad(async (options) => {
  viewUserId.value = options?.userId || null
  if (viewUserId.value) {
    loading.value = true
    try {
      const data = await getUserProfile(viewUserId.value)
      profile.value = data || {}
      if (data?.photos) {
        try { photoList.value = JSON.parse(data.photos) } catch {}
      }
    } catch (e) {
      console.error('获取资料失败', e)
      uni.showToast({ title: '获取资料失败', icon: 'none' })
    }
    loading.value = false
  } else {
    try {
      const data = await getMyProfile()
      if (data) {
        form.nickname = data.nickname || ''
        form.avatarUrl = data.avatarUrl || ''
        form.gender = data.gender || 0
        form.age = data.age ? String(data.age) : ''
        form.city = data.city || ''
        form.occupation = data.occupation || ''
        form.bio = data.bio || ''
        form.hobbies = data.hobbies || ''
        form.wechat = data.wechat || ''
        try { photoList.value = data.photos ? JSON.parse(data.photos) : [] } catch {}
      }
    } catch {}
  }
})
</script>
