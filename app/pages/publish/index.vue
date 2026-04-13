<template>
  <view class="page">
    <!-- 流程规则提示 -->
    <view v-if="showRules" class="rules-card">
      <view class="rules-head">
        <text class="rules-title">📋 发布活动流程说明</text>
        <text class="rules-close" @tap="showRules = false">收起</text>
      </view>
      <view class="rules-steps">
        <view class="step-item">
          <text class="step-num">①</text>
          <view class="step-content">
            <text class="step-main">发布活动，设置人数</text>
            <text class="step-desc">填写活动信息，设定需要的搭子人数</text>
          </view>
        </view>
        <view class="step-item">
          <text class="step-num">②</text>
          <view class="step-content">
            <text class="step-main">出题筛选搭子</text>
            <text class="step-desc">发布后进入活动详情，设置题目（最多10题），用来了解申请人</text>
          </view>
        </view>
        <view class="step-item">
          <text class="step-num">③</text>
          <view class="step-content">
            <text class="step-main">申请人答题</text>
            <text class="step-desc">感兴趣的人需回答你的题目才能申请加入</text>
          </view>
        </view>
        <view class="step-item">
          <text class="step-num">④</text>
          <view class="step-content">
            <text class="step-main">批改答卷，选择搭子</text>
            <text class="step-desc">查看答卷，通过合适的人，不合适的可以拒绝</text>
          </view>
        </view>
        <view class="step-item">
          <text class="step-num">⑤</text>
          <view class="step-content">
            <text class="step-main">自动拉群，组队成功！</text>
            <text class="step-desc">通过的搭子自动加入群聊，大家可以在群里沟通细节</text>
          </view>
        </view>
      </view>
      <text class="rules-tip">💡 人数满额后可标记"组队完成"，停止接收新申请</text>
    </view>
    <view v-else class="rules-collapsed" @tap="showRules = true">
      <text class="rules-collapsed-text">📋 查看发布流程说明</text>
    </view>

    <view class="card">
      <text class="form-title">发布活动</text>

      <view class="field"><text class="label">标题 *</text>
        <input v-model="form.title" placeholder="一句话描述活动" maxlength="50" class="input" @input="saveDraft" />
      </view>

      <view class="field"><text class="label">分类</text>
        <view class="cat-row">
          <text v-for="c in categories" :key="c" :class="['cat-tag', form.category === c && 'active']" @tap="form.category = c; saveDraft()">{{ c }}</text>
        </view>
      </view>

      <view class="field"><text class="label">描述</text>
        <textarea v-model="form.description" placeholder="详细介绍一下活动..." maxlength="500" class="textarea" @input="saveDraft" />
      </view>

      <view class="field-row">
        <view class="field" style="flex:1;"><text class="label">地点</text>
          <input v-model="form.location" placeholder="线上/城市/具体地点" class="input" @input="saveDraft" />
        </view>
        <view class="field" style="flex:1;"><text class="label">时间</text>
          <input v-model="form.activityTime" placeholder="例如：周六下午2点" class="input" @input="saveDraft" />
        </view>
      </view>

      <view class="field-row">
        <view class="field" style="flex:1;"><text class="label">需要人数</text>
          <input v-model="form.maxPeople" type="number" placeholder="1" class="input" @input="saveDraft" />
        </view>
        <view class="field" style="flex:1;"><text class="label">标签</text>
          <input v-model="form.tags" placeholder="户外,徒步,周末" class="input" @input="saveDraft" />
        </view>
      </view>

      <!-- 筛选偏好 -->
      <view class="divider"></view>
      <text class="sub-title">🎯 希望看到的人群（选填）</text>

      <view class="field"><text class="label">偏好性别</text>
        <view class="cat-row">
          <text :class="['cat-tag', form.preferGender === 0 && 'active']" @tap="form.preferGender = 0">不限</text>
          <text :class="['cat-tag', form.preferGender === 1 && 'active']" @tap="form.preferGender = 1">男生</text>
          <text :class="['cat-tag', form.preferGender === 2 && 'active']" @tap="form.preferGender = 2">女生</text>
        </view>
      </view>

      <view class="field-row">
        <view class="field" style="flex:1;"><text class="label">最小年龄</text>
          <input v-model="form.preferAgeMin" type="number" placeholder="如：18" class="input" />
        </view>
        <view class="field" style="flex:1;"><text class="label">最大年龄</text>
          <input v-model="form.preferAgeMax" type="number" placeholder="如：35" class="input" />
        </view>
      </view>

      <view class="field"><text class="label">偏好城市</text>
        <input v-model="form.preferCity" placeholder="如：深圳（留空不限）" class="input" />
      </view>

      <view class="field"><text class="label">偏好标签</text>
        <input v-model="form.preferTags" placeholder="如：户外,摄影（留空不限）" class="input" />
      </view>

      <button class="btn-primary" @tap="submit" :disabled="submitting">{{ submitting ? '发布中...' : '发布活动' }}</button>

      <view v-if="hasDraft" style="text-align:center;margin-top:16rpx;">
        <text style="font-size:22rpx;color:#b8929e;" @tap="clearDraft">清除草稿</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { request } from '@/api/request'

const categories = ['旅游', '运动', '美食', '学习', '游戏', '追星', '其他']
const submitting = ref(false)
const hasDraft = ref(false)
const showRules = ref(true)
const form = reactive({
  title: '', category: '旅游', description: '', location: '', activityTime: '',
  maxPeople: '', tags: '', preferGender: 0, preferAgeMin: '', preferAgeMax: '',
  preferCity: '', preferTags: ''
})

function saveDraft() {
  uni.setStorageSync('publish_draft', JSON.stringify(form))
  hasDraft.value = true
}

function loadDraft() {
  try {
    const draft = uni.getStorageSync('publish_draft')
    if (draft) {
      const d = JSON.parse(draft)
      Object.assign(form, d)
      hasDraft.value = true
    }
  } catch {}
}

function clearDraft() {
  uni.removeStorageSync('publish_draft')
  Object.assign(form, { title: '', category: '旅游', description: '', location: '', activityTime: '', maxPeople: '', tags: '', preferGender: 0, preferAgeMin: '', preferAgeMax: '', preferCity: '', preferTags: '' })
  hasDraft.value = false
  uni.showToast({ title: '草稿已清除', icon: 'success' })
}

async function submit() {
  if (!form.title.trim()) return uni.showToast({ title: '请填写标题', icon: 'none' })
  submitting.value = true
  try {
    const data = { ...form, maxPeople: form.maxPeople ? parseInt(form.maxPeople) : 1,
      preferAgeMin: form.preferAgeMin ? parseInt(form.preferAgeMin) : null,
      preferAgeMax: form.preferAgeMax ? parseInt(form.preferAgeMax) : null }
    const id = await request({ url: '/activity/publish', method: 'POST', data, needAuth: true })
    uni.removeStorageSync('publish_draft')
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => uni.redirectTo({ url: `/pages/activity-detail/index?id=${id}` }), 500)
  } catch (e) { uni.showToast({ title: e?.message || '发布失败', icon: 'none' }) }
  submitting.value = false
}

onMounted(loadDraft)
</script>

<style scoped>
.page { padding: 20rpx; }
.rules-card { background: linear-gradient(135deg, #fef3f8, #f0e6ff); border-radius: 24rpx; padding: 24rpx; margin-bottom: 20rpx; border: 1px solid #f0d0e8; }
.rules-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.rules-title { font-size: 28rpx; font-weight: 800; color: #7c3a5e; }
.rules-close { font-size: 24rpx; color: #c084fc; }
.rules-steps { display: flex; flex-direction: column; gap: 16rpx; }
.step-item { display: flex; align-items: flex-start; gap: 12rpx; }
.step-num { font-size: 28rpx; font-weight: 800; color: #f472b6; min-width: 36rpx; line-height: 1.5; }
.step-content { flex: 1; }
.step-main { display: block; font-size: 26rpx; font-weight: 700; color: #4a2040; line-height: 1.5; }
.step-desc { display: block; font-size: 22rpx; color: #9e7a8e; line-height: 1.5; margin-top: 2rpx; }
.rules-tip { display: block; font-size: 22rpx; color: #c084fc; margin-top: 16rpx; padding-top: 12rpx; border-top: 1px dashed #e8d0f0; }
.rules-collapsed { background: linear-gradient(135deg, #fef3f8, #f0e6ff); border-radius: 24rpx; padding: 20rpx; margin-bottom: 20rpx; text-align: center; border: 1px solid #f0d0e8; }
.rules-collapsed-text { font-size: 26rpx; color: #7c3a5e; font-weight: 600; }
.card { background: #fff; border-radius: 24rpx; padding: 32rpx; border: 1px solid #fce4ec; }
.form-title { display: block; font-size: 36rpx; font-weight: 800; color: #f472b6; text-align: center; margin-bottom: 28rpx; }
.sub-title { display: block; font-size: 28rpx; font-weight: 600; color: #7c3a5e; margin-bottom: 16rpx; }
.field { margin-bottom: 20rpx; }
.field-row { display: flex; gap: 16rpx; }
.label { display: block; font-size: 26rpx; color: #7c5270; margin-bottom: 8rpx; font-weight: 600; }
.input { background: #fff5f7; border: 1px solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx; }
.textarea { width: 100%; height: 160rpx; background: #fff5f7; border: 1px solid #fce4ec; border-radius: 12rpx; padding: 16rpx; color: #4a2040; font-size: 28rpx; box-sizing: border-box; }
.cat-row { display: flex; flex-wrap: wrap; gap: 12rpx; }
.cat-tag { padding: 10rpx 24rpx; border-radius: 20rpx; background: #fff5f7; color: #b8929e; font-size: 24rpx; border: 1px solid #fce4ec; }
.cat-tag.active { background: #fce4ec; color: #ec4899; border-color: #f472b6; }
.divider { height: 1px; background: #fce4ec; margin: 24rpx 0; }
.btn-primary { margin-top: 24rpx; background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 30rpx; font-weight: 700; }
</style>
