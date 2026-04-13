<template>
  <view class="page">
    <view class="card">
      <text class="h1">隐私政策</text>
      <text class="update">最后更新：2026年4月11日</text>

      <text class="h2">一、我们收集的信息</text>
      <text class="p">为了提供服务，我们会收集以下信息：</text>
      <text class="p">• 注册信息：手机号码（作为登录账号）、密码（加密存储）</text>
      <text class="p">• 个人资料：昵称、头像、年龄、性别、城市、职业、个人简介、兴趣爱好（均为自愿填写）</text>
      <text class="p">• 活动信息：您发布的活动内容、评论内容</text>
      <text class="p">• 聊天记录：组队成功后的活动沟通消息（AES-256 加密存储，30天后自动删除）</text>
      <text class="p">• 设备信息：IP 地址、浏览器类型（用于访问统计）</text>

      <text class="h2">二、信息使用方式</text>
      <text class="p">• 提供活动组队和活动沟通服务</text>
      <text class="p">• 展示您的公开个人资料给其他用户</text>
      <text class="p">• 分析使用情况以优化产品体验</text>
      <text class="p">• 处理用户举报和违规内容审核</text>

      <text class="h2">三、信息保护措施</text>
      <text class="p">• 密码安全：使用 BCrypt 不可逆加密存储</text>
      <text class="p">• 聊天加密：所有聊天消息使用 AES-256-GCM 加密后存储</text>
      <text class="p">• 传输加密：全站使用 HTTPS 加密传输</text>
      <text class="p">• 手机号脱敏：对外展示时隐藏中间四位（如 138****1234）</text>
      <text class="p">• API 限流：防止恶意刷接口和数据爬取</text>
      <text class="p">• 定期清理：聊天记录保留 30 天后自动删除</text>

      <text class="h2">四、信息共享</text>
      <text class="p">我们不会将您的个人信息出售、交易或转让给第三方。以下情况除外：</text>
      <text class="p">• 获得您的明确同意</text>
      <text class="p">• 法律法规要求</text>
      <text class="p">• 保护平台和其他用户的合法权益</text>

      <text class="h2">五、您的权利</text>
      <text class="p">• 查看数据：在"我的"页面查看和修改您的个人资料</text>
      <text class="p">• 导出数据：可以导出您的所有个人数据（JSON 格式）</text>
      <text class="p">• 删除数据：可以注销账号并删除所有关联数据</text>
      <text class="p">• 举报违规：可以举报违规用户、活动、评论或聊天内容</text>

      <text class="h2">六、未成年人保护</text>
      <text class="p">本平台不面向 18 岁以下未成年人提供服务。如果我们发现未成年人注册使用，将主动删除相关信息。</text>

      <text class="h2">七、联系我们</text>
      <text class="p">如果您对本隐私政策有任何疑问，请通过平台内的举报功能联系我们。</text>
    </view>
  </view>
</template>

<style scoped>
.page { padding: 20rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 32rpx; border: 1px solid #fce4ec; }
.h1 { font-size: 40rpx; font-weight: 800; color: #7c3a5e; display: block; margin-bottom: 8rpx; }
.update { font-size: 24rpx; color: #b8929e; display: block; margin-bottom: 32rpx; }
.h2 { font-size: 30rpx; font-weight: 700; color: #4a2040; display: block; margin: 28rpx 0 12rpx; }
.p { font-size: 26rpx; color: #7c5270; line-height: 1.8; display: block; margin-bottom: 4rpx; }
</style>
