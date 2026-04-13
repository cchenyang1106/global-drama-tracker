<template>
  <view class="page">
    <text class="title">📝 设置题目</text>
    <text class="hint">申请人需要回答以下问题才能参与活动</text>

    <view v-for="(q, i) in quizzes" :key="i" class="card">
      <view class="card-head">
        <text class="q-num">题目 {{ i + 1 }}</text>
        <text class="del-btn" @tap="removeQuiz(i)">删除</text>
      </view>
      <textarea v-model="q.questionText" placeholder="请输入题目内容..." maxlength="500" class="textarea" />
      <view class="img-row">
        <image v-for="(img, j) in (q.imageList || [])" :key="j" :src="img" class="thumb" @tap="previewImg(q.imageList, j)" />
        <view v-if="(q.imageList || []).length < 3" class="add-img" @tap="addImage(i)">
          <text>+图片</text>
        </view>
      </view>
    </view>

    <button class="btn-add" @tap="addQuiz">+ 添加题目</button>
    <button class="btn-primary" @tap="saveAll" :disabled="saving">{{ saving ? '保存中...' : '保存全部题目' }}</button>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getQuizList, saveQuiz, deleteQuiz } from '@/api/quiz'

const quizzes = ref([])
const saving = ref(false)
let activityId = null

function addQuiz() {
  quizzes.value.push({ questionText: '', imageList: [], id: null })
}

function removeQuiz(i) {
  const q = quizzes.value[i]
  if (q.id) deleteQuiz(q.id).catch(() => {})
  quizzes.value.splice(i, 1)
}

function addImage(quizIndex) {
  uni.chooseImage({
    count: 3 - (quizzes.value[quizIndex].imageList || []).length,
    sizeType: ['compressed'],
    success: (res) => {
      for (const path of res.tempFilePaths) {
        if (!quizzes.value[quizIndex].imageList) quizzes.value[quizIndex].imageList = []
        quizzes.value[quizIndex].imageList.push(path)
      }
    }
  })
}

function previewImg(list, idx) { uni.previewImage({ urls: list, current: idx }) }

async function saveAll() {
  if (quizzes.value.length === 0) return uni.showToast({ title: '请至少添加一道题', icon: 'none' })
  saving.value = true
  try {
    for (let i = 0; i < quizzes.value.length; i++) {
      const q = quizzes.value[i]
      if (!q.questionText.trim()) { uni.showToast({ title: `第${i+1}题内容不能为空`, icon: 'none' }); saving.value = false; return }
      const data = {
        activityId: Number(activityId),
        questionText: q.questionText,
        questionImages: q.imageList?.length > 0 ? JSON.stringify(q.imageList) : null,
        sortOrder: i,
      }
      if (q.id) data.id = q.id
      const id = await saveQuiz(data)
      q.id = id
    }
    uni.showToast({ title: '题目保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) { uni.showToast({ title: e?.message || '保存失败', icon: 'none' }) }
  saving.value = false
}

onLoad(async (options) => {
  activityId = options?.activityId
  if (activityId) {
    try {
      const list = await getQuizList(activityId)
      quizzes.value = (list || []).map(q => ({
        ...q, imageList: q.questionImages ? JSON.parse(q.questionImages) : []
      }))
    } catch {}
  }
  if (quizzes.value.length === 0) addQuiz()
})
</script>

<style scoped>
.page { padding: 20rpx; }
.title { display: block; font-size: 36rpx; font-weight: 800; color: #4a2040; text-align: center; margin-bottom: 8rpx; }
.hint { display: block; font-size: 24rpx; color: #b8929e; text-align: center; margin-bottom: 24rpx; }
.card { background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx; border: 1px solid #fce4ec; }
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.q-num { font-size: 28rpx; font-weight: 700; color: #f472b6; }
.del-btn { font-size: 24rpx; color: #e11d48; }
.textarea { width: 100%; min-height: 160rpx; background: #fff5f7; border: 1px solid #fce4ec; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; color: #4a2040; box-sizing: border-box; }
.img-row { display: flex; gap: 12rpx; margin-top: 12rpx; flex-wrap: wrap; }
.thumb { width: 150rpx; height: 150rpx; border-radius: 12rpx; }
.add-img { width: 150rpx; height: 150rpx; border: 2rpx dashed #fce4ec; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; color: #b8929e; font-size: 24rpx; }
.btn-add { background: #fff; color: #f472b6; border: 2px solid #fce4ec; border-radius: 40rpx; font-size: 28rpx; margin-bottom: 16rpx; }
.btn-primary { background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 30rpx; font-weight: 700; }
</style>
