<template>
  <view class="page">
    <view class="card">
      <text class="h2">📦 我的数据</text>
      <text class="desc">根据《个人信息保护法》，你有权导出和删除你的所有个人数据。</text>

      <view class="section">
        <text class="h3">📥 导出数据</text>
        <text class="p">导出你的所有个人信息（JSON 格式），包括个人资料、发布的活动、评论和聊天记录。</text>
        <button class="btn-primary" @click="doExport" :disabled="exporting">{{ exporting ? '导出中...' : '导出我的数据' }}</button>
      </view>

      <view class="divider"></view>

      <view class="section">
        <text class="h3" style="color:#e11d48;">⚠️ 注销账号</text>
        <text class="p">注销后将永久删除你的所有数据，包括个人资料、发布的活动和留言记录。此操作不可撤销。</text>
        <button class="btn-danger" @click="confirmDelete">注销我的账号</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { request } from '@/api/request'

const exporting = ref(false)

async function doExport() {
  exporting.value = true
  try {
    const data = await request({ url: '/user-data/export', needAuth: true })
    // 复制到剪贴板
    uni.setClipboardData({
      data: JSON.stringify(data, null, 2),
      success: () => uni.showToast({ title: '数据已复制到剪贴板', icon: 'success' })
    })
  } catch (e) { uni.showToast({ title: e?.message || '导出失败', icon: 'none' }) }
  exporting.value = false
}

function confirmDelete() {
  uni.showModal({
    title: '确认注销账号',
    content: '注销后将永久删除你的所有数据，此操作不可恢复！',
    confirmText: '确认注销',
    confirmColor: '#e11d48',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({ url: '/user-data/delete-account', method: 'POST', needAuth: true })
          uni.showToast({ title: '账号已注销', icon: 'success' })
          // 清除本地存储并跳转首页
          uni.removeStorageSync('token')
          uni.removeStorageSync('userId')
          uni.removeStorageSync('nickname')
          uni.removeStorageSync('phone')
          setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 1500)
        } catch (e) { uni.showToast({ title: e?.message || '注销失败', icon: 'none' }) }
      }
    }
  })
}
</script>

<style scoped>
.page { padding: 20rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 32rpx; border: 1px solid #fce4ec; }
.h2 { font-size: 36rpx; font-weight: 800; color: #4a2040; display: block; margin-bottom: 8rpx; }
.h3 { font-size: 30rpx; font-weight: 700; color: #4a2040; display: block; margin-bottom: 8rpx; }
.desc { font-size: 24rpx; color: #b8929e; display: block; margin-bottom: 28rpx; }
.p { font-size: 26rpx; color: #7c5270; line-height: 1.7; display: block; margin-bottom: 16rpx; }
.section { margin: 20rpx 0; }
.divider { height: 1px; background: #fce4ec; margin: 28rpx 0; }
.btn-primary { background: linear-gradient(135deg, #f472b6, #c084fc); color: #fff; border: none; border-radius: 40rpx; font-size: 28rpx; font-weight: 600; }
.btn-danger { background: #fff; color: #e11d48; border: 2px solid #e11d48; border-radius: 40rpx; font-size: 28rpx; font-weight: 600; }
</style>
