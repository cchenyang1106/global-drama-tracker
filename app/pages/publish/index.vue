<template>
  <view class="page">
    <view class="form-card">
      <view class="field">
        <text class="label">活动标题 *</text>
        <input class="input" v-model="form.title" placeholder="例如：周末一起爬白云山" maxlength="50" />
      </view>
      <view class="field">
        <text class="label">分类 *</text>
        <view class="cat-list">
          <text v-for="c in cats" :key="c" :class="['cat-btn', { active: form.category === c }]" @tap="form.category = c">{{ c }}</text>
        </view>
      </view>
      <view class="field">
        <text class="label">活动描述</text>
        <textarea class="textarea" v-model="form.description" placeholder="详细描述你的活动计划..." maxlength="500" />
      </view>
      <view class="field">
        <text class="label">活动地点</text>
        <input class="input" v-model="form.location" placeholder="例如：深圳南山" />
      </view>
      <view class="field">
        <text class="label">活动时间</text>
        <input class="input" v-model="form.activityTime" placeholder="例如：本周六下午2点" />
      </view>
      <view class="field">
        <text class="label">需要人数</text>
        <input class="input" v-model="form.maxPeople" type="number" placeholder="2" />
      </view>
      <view class="field">
        <text class="label">标签（逗号分隔）</text>
        <input class="input" v-model="form.tags" placeholder="例如：户外,徒步,周末" />
      </view>
      <button class="submit-btn" @tap="submit" :loading="submitting">发布活动</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { publishActivity } from '@/api/activity'

const submitting = ref(false)
const cats = ['旅游', '运动', '美食', '电影', '学习', '游戏', '其他']
const form = reactive({ title: '', category: '运动', description: '', location: '', activityTime: '', maxPeople: '2', tags: '' })

async function submit() {
  if (!form.title.trim()) return uni.showToast({ title: '请输入标题', icon: 'none' })
  submitting.value = true
  try {
    const id = await publishActivity({ ...form, maxPeople: parseInt(form.maxPeople) || 2 })
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => uni.redirectTo({ url: `/pages/activity-detail/index?id=${id}` }), 500)
  } catch {}
  submitting.value = false
}
</script>

<style>
.page { padding: 24rpx; background: #fff5f7; min-height: 100vh; }
.form-card { background: #ffffff; border-radius: 20rpx; padding: 32rpx; }
.field { margin-bottom: 24rpx; }
.label { display: block; font-size: 28rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600; }
.input { background: #fff5f7; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx; }
.textarea { background: #fff5f7; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx; width: 100%; min-height: 160rpx; }
.cat-list { display: flex; flex-wrap: wrap; gap: 12rpx; }
.cat-btn { padding: 12rpx 24rpx; border-radius: 32rpx; background: #fff5f7; color: #7c5270; font-size: 26rpx; }
.cat-btn.active { background: rgba(99,102,241,0.2); color: #f472b6; }
.submit-btn { background: linear-gradient(135deg,#f472b6,#c084fc); color: white; border-radius: 12rpx; font-size: 32rpx; font-weight: 700; margin-top: 16rpx; }
</style>
