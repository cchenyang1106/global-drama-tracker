<template>
  <view class="page">
    <text class="title">📋 答题</text>
    <text class="hint">认真回答以下问题，发布人会根据你的答案决定是否通过</text>

    <view v-for="(q, i) in quizzes" :key="q.id" class="card">
      <text class="q-num">题目 {{ i + 1 }}</text>
      <text class="q-text">{{ q.questionText }}</text>
      <view v-if="q.imageList?.length" class="img-row">
        <image v-for="(img, j) in q.imageList" :key="j" :src="img" class="thumb" @tap="previewImg(q.imageList, j)" />
      </view>
      <text class="answer-label">你的回答：</text>
      <textarea v-model="answers[i].answerText" placeholder="请输入你的回答..." maxlength="500" class="textarea" />
      <view class="img-row">
        <image v-for="(img, j) in answers[i].imageList" :key="j" :src="img" class="thumb" />
        <view v-if="(answers[i].imageList || []).length < 3" class="add-img" @tap="addAnswerImage(i)">
          <text>+图片</text>
        </view>
      </view>
    </view>

    <view v-if="quizzes.length === 0" class="empty">发布人还没有出题</view>

    <button v-if="quizzes.length > 0" class="btn-primary" @tap="submit" :disabled="submitting">
      {{ submitting ? '提交中...' : '提交答卷' }}
    </button>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getQuizList, submitAnswer } from '@/api/quiz'

const quizzes = ref([])
const answers = ref([])
const submitting = ref(false)
let activityId = null

function previewImg(list, idx) { uni.previewImage({ urls: list, current: idx }) }

function addAnswerImage(i) {
  uni.chooseImage({
    count: 3,
    sizeType: ['compressed'],
    success: (res) => {
      if (!answers.value[i].imageList) answers.value[i].imageList = []
      for (const p of res.tempFilePaths) answers.value[i].imageList.push(p)
    }
  })
}

async function submit() {
  for (let i = 0; i < answers.value.length; i++) {
    if (!answers.value[i].answerText?.trim()) {
      return uni.showToast({ title: `请回答第${i+1}题`, icon: 'none' })
    }
  }
  submitting.value = true
  try {
    const data = {
      activityId: Number(activityId),
      answers: answers.value.map((a, i) => ({
        quizId: quizzes.value[i].id,
        answerText: a.answerText,
        answerImages: a.imageList?.length > 0 ? JSON.stringify(a.imageList) : null,
      }))
    }
    await submitAnswer(data)
    uni.showToast({ title: '答卷已提交！', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) { uni.showToast({ title: e?.message || '提交失败', icon: 'none' }) }
  submitting.value = false
}

onLoad(async (options) => {
  activityId = options?.activityId
  if (activityId) {
    const list = await getQuizList(activityId)
    quizzes.value = (list || []).map(q => ({
      ...q, imageList: q.questionImages ? JSON.parse(q.questionImages) : []
    }))
    answers.value = quizzes.value.map(() => ({ answerText: '', imageList: [] }))
  }
})
</script>

<style scoped>
.page { padding: 20rpx; }
.title { display: block; font-size: 36rpx; font-weight: 800; color: #4a2040; text-align: center; margin-bottom: 8rpx; }
.hint { display: block; font-size: 24rpx; color: #b8929e; text-align: center; margin-bottom: 24rpx; }
.card { background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx; border: 1px solid #fce4ec; }
.q-num { font-size: 26rpx; font-weight: 700; color: #f472b6; display: block; margin-bottom: 8rpx; }
.q-text { font-size: 28rpx; color: #4a2040; line-height: 1.6; display: block; margin-bottom: 12rpx; }
.answer-label { font-size: 26rpx; color: #7c5270; font-weight: 600; display: block; margin: 12rpx 0 8rpx; }
.textarea { width: 100%; min-height: 140rpx; background: #fff5f7; border: 1px solid #fce4ec; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; color: #4a2040; box-sizing: border-box; }
.img-row { display: flex; gap: 12rpx; margin-top: 12rpx; flex-wrap: wrap; }
.thumb { width: 150rpx; height: 150rpx; border-radius: 12rpx; }
.add-img { width: 150rpx; height: 150rpx; border: 2rpx dashed #fce4ec; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; color: #b8929e; font-size: 24rpx; }
.empty { text-align: center; padding: 60rpx; color: #b8929e; }
.btn-primary { margin-top: 16rpx; background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 30rpx; font-weight: 700; }
</style>
