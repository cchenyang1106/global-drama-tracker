<template>
  <view class="page">
    <text class="title">📝 设置题目</text>
    <text class="hint">申请人需要回答以下问题才能参与活动（最多10题）</text>
    <text class="count-hint">已添加 {{ quizzes.length }} / 10 题</text>

    <!-- 智能题目推荐区域 -->
    <view v-if="suggestions.length > 0" class="suggest-box">
      <view class="suggest-head">
        <text class="suggest-title">💡 推荐题目（点击快速添加）</text>
        <text class="suggest-toggle" @tap="showSuggest = !showSuggest">{{ showSuggest ? '收起' : '展开' }}</text>
      </view>
      <view v-if="showSuggest" class="suggest-list">
        <text v-for="(s, si) in suggestions" :key="si" class="suggest-item" @tap="useSuggestion(s)">{{ s }}</text>
      </view>
    </view>

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

    <button v-if="quizzes.length < 10" class="btn-add" @tap="addQuiz">+ 添加题目</button>
    <view v-else class="limit-tip">
      <text>已达到题目上限（10题）</text>
    </view>
    <button class="btn-primary" @tap="saveAll" :disabled="saving">{{ saving ? '保存中...' : '保存全部题目' }}</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getQuizList, saveQuiz, deleteQuiz } from '@/api/quiz'
import { getActivityDetail } from '@/api/activity'

const MAX_QUIZ = 10
const quizzes = ref([])
const saving = ref(false)
const suggestions = ref([])
const showSuggest = ref(true)
let activityId = null

// 根据活动分类生成推荐题目
const CATEGORY_SUGGESTIONS = {
  '旅游': [
    '你的旅游偏好是什么？（自然风光/人文历史/美食之旅/城市探索）',
    '你是J人还是P人？（喜欢提前规划还是随性出发）',
    '喜欢特种兵旅游还是佛系躺平？',
    '你能接受的每日步行数大概是多少？',
    '旅行中最不能忍受的事情是什么？',
    '你喜欢拍照打卡还是深度体验？',
    '住宿偏好是什么？（青旅/民宿/酒店/露营）',
    '你的理想旅行节奏是怎样的？（早起暴走/睡到自然醒）',
    '有没有特别想去的目的地？',
    '你愿意AA还是有其他费用分摊方式？',
  ],
  '运动': [
    '你的运动频率是怎样的？（每天/每周几次/偶尔）',
    '你擅长或想尝试什么运动？',
    '你的运动水平如何？（小白/入门/进阶/大神）',
    '你喜欢室内运动还是户外运动？',
    '可以接受的运动强度是？（轻松/中等/高强度）',
    '有什么运动装备可以分享吗？',
    '你理想的运动时间段是？（早晨/下午/晚上）',
    '为什么想找运动伙伴？',
  ],
  '美食': [
    '你是什么口味偏好？（辣/甜/清淡/重口）',
    '有没有忌口或过敏的食物？',
    '你喜欢探店还是去熟悉的餐厅？',
    '可以接受的人均消费范围是？',
    '你喜欢什么类型的美食？（中餐/西餐/日料/东南亚/烧烤火锅）',
    '你是喜欢安静用餐还是热闹氛围？',
    '你会拍美食照片发朋友圈吗？',
    '最近有没有想打卡的餐厅？',
  ],
  '学习': [
    '你目前在学什么或想学什么？',
    '你更喜欢哪种学习方式？（自习/讨论/互相监督）',
    '你的学习目标是什么？（考证/考研/兴趣提升/技能学习）',
    '你通常在哪里学习？（图书馆/咖啡厅/自习室/家里）',
    '你学习时喜欢安静还是可以接受轻声讨论？',
    '你理想的学习时间段是？',
    '你希望同伴能提供什么帮助？（互相打卡/答疑/分享资料）',
    '你的学习风格是？（专注型/番茄钟/自由安排）',
  ],
  '游戏': [
    '你最近在玩什么游戏？',
    '你喜欢什么类型的游戏？（MOBA/FPS/RPG/休闲/桌游）',
    '你的游戏水平如何？（休闲玩家/中等/硬核）',
    '你一般什么时间段打游戏？',
    '你有麦吗？可以语音开黑吗？',
    '你对游戏伙伴有什么期待？（一起上分/娱乐为主/陪玩）',
    '你能接受队友偶尔发挥失常吗？',
    '你喜欢竞技对抗还是合作通关？',
  ],
  '追星': [
    '你追的是哪个明星/偶像/团体？',
    '你是什么时候入坑的？属于哪个时期的粉丝？',
    '你是唯粉还是团粉？（能接受同伴和你喜欢不同成员吗）',
    '你追星的方式是什么？（线上追行程/线下看演出/收藏周边/剪视频）',
    '你参加过线下活动吗？（演唱会/签售/接机/应援）',
    '你能接受的追星花费大概是多少？',
    '你理想的追星伙伴是什么样的？（一起看演出/交换周边/聊天分享日常）',
    '你最喜欢ta的哪一点？',
    '有没有想一起去的演唱会或活动？',
    '你是理智粉还是狂热粉？',
  ],
  '其他': [
    '简单介绍一下你自己吧',
    '你为什么想参加这个活动？',
    '你的MBTI是什么？',
    '你对同伴有什么期待或要求？',
    '你的空闲时间一般在什么时候？',
    '你是社牛还是社恐？',
    '你有什么兴趣爱好？',
    '你觉得活动伙伴之间最重要的是什么？（合拍/守时/随和）',
  ],
}

function addQuiz() {
  if (quizzes.value.length >= MAX_QUIZ) {
    return uni.showToast({ title: `最多只能添加${MAX_QUIZ}道题`, icon: 'none' })
  }
  quizzes.value.push({ questionText: '', imageList: [], id: null })
}

function useSuggestion(text) {
  // 找第一个空题填入，否则新建
  const emptyIdx = quizzes.value.findIndex(q => !q.questionText.trim())
  if (emptyIdx >= 0) {
    quizzes.value[emptyIdx].questionText = text
  } else if (quizzes.value.length < MAX_QUIZ) {
    quizzes.value.push({ questionText: text, imageList: [], id: null })
  } else {
    uni.showToast({ title: '已达题目上限，请删除后再添加', icon: 'none' })
  }
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
  if (quizzes.value.length > MAX_QUIZ) return uni.showToast({ title: `题目不能超过${MAX_QUIZ}道`, icon: 'none' })
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
    // 并行加载题目列表和活动详情
    const [quizResult, detailResult] = await Promise.allSettled([
      getQuizList(activityId),
      getActivityDetail(activityId),
    ])
    // 加载已有题目
    if (quizResult.status === 'fulfilled' && quizResult.value) {
      quizzes.value = quizResult.value.map(q => ({
        ...q, imageList: q.questionImages ? JSON.parse(q.questionImages) : []
      }))
    }
    // 加载推荐题目
    if (detailResult.status === 'fulfilled' && detailResult.value) {
      const cat = detailResult.value.category || '其他'
      suggestions.value = CATEGORY_SUGGESTIONS[cat] || CATEGORY_SUGGESTIONS['其他']
    } else {
      // 兜底：如果获取不到活动详情，显示通用推荐
      suggestions.value = CATEGORY_SUGGESTIONS['其他']
    }
  } else {
    suggestions.value = CATEGORY_SUGGESTIONS['其他']
  }
  if (quizzes.value.length === 0) addQuiz()
})
</script>

<style scoped>
.page { padding: 20rpx; }
.title { display: block; font-size: 36rpx; font-weight: 800; color: #4a2040; text-align: center; margin-bottom: 8rpx; }
.hint { display: block; font-size: 24rpx; color: #b8929e; text-align: center; margin-bottom: 8rpx; }
.count-hint { display: block; font-size: 24rpx; color: #f472b6; text-align: center; margin-bottom: 24rpx; font-weight: 600; }
.suggest-box { background: linear-gradient(135deg, #fef3f8, #f5f0ff); border-radius: 20rpx; padding: 20rpx; margin-bottom: 20rpx; border: 1px solid #fce4ec; }
.suggest-head { display: flex; justify-content: space-between; align-items: center; }
.suggest-title { font-size: 26rpx; font-weight: 700; color: #7c5270; }
.suggest-toggle { font-size: 24rpx; color: #f472b6; }
.suggest-list { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.suggest-item { display: inline-block; background: #fff; border: 1px solid #f9d1e0; border-radius: 30rpx; padding: 10rpx 24rpx; font-size: 24rpx; color: #7c5270; line-height: 1.4; }
.card { background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx; border: 1px solid #fce4ec; }
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.q-num { font-size: 28rpx; font-weight: 700; color: #f472b6; }
.del-btn { font-size: 24rpx; color: #e11d48; }
.textarea { width: 100%; min-height: 160rpx; background: #fff5f7; border: 1px solid #fce4ec; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; color: #4a2040; box-sizing: border-box; }
.img-row { display: flex; gap: 12rpx; margin-top: 12rpx; flex-wrap: wrap; }
.thumb { width: 150rpx; height: 150rpx; border-radius: 12rpx; }
.add-img { width: 150rpx; height: 150rpx; border: 2rpx dashed #fce4ec; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; color: #b8929e; font-size: 24rpx; }
.btn-add { background: #fff; color: #f472b6; border: 2px solid #fce4ec; border-radius: 40rpx; font-size: 28rpx; margin-bottom: 16rpx; }
.limit-tip { text-align: center; color: #b8929e; font-size: 24rpx; margin-bottom: 16rpx; padding: 16rpx; background: #fff5f7; border-radius: 20rpx; }
.btn-primary { background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 30rpx; font-weight: 700; }
</style>
