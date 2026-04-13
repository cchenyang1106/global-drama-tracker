<template>
  <div class="privacy-page">
    <div class="privacy-card">
      <h1>隐私政策</h1>
      <p class="update-date">最后更新：2026年4月11日</p>

      <section>
        <h2>一、我们收集的信息</h2>
        <p>为了提供服务，我们会收集以下信息：</p>
        <ul>
          <li><strong>注册信息</strong>：手机号码（作为登录账号）、密码（加密存储）</li>
          <li><strong>个人资料</strong>：昵称、头像、年龄、性别、城市、职业、个人简介、兴趣爱好（均为自愿填写）</li>
          <li><strong>活动信息</strong>：您发布的活动内容、评论内容</li>
          <li><strong>聊天记录</strong>：组队成功后的活动沟通消息（AES-256 加密存储，30天后自动删除）</li>
          <li><strong>设备信息</strong>：IP 地址、浏览器类型、屏幕分辨率（用于访问统计）</li>
        </ul>
      </section>

      <section>
        <h2>二、信息使用方式</h2>
        <ul>
          <li>提供活动组队和活动沟通服务</li>
          <li>展示您的公开个人资料给其他用户</li>
          <li>分析网站使用情况以优化产品体验</li>
          <li>处理用户举报和违规内容审核</li>
        </ul>
      </section>

      <section>
        <h2>三、信息保护措施</h2>
        <ul>
          <li><strong>密码安全</strong>：使用 BCrypt 不可逆加密存储</li>
          <li><strong>聊天加密</strong>：所有聊天消息使用 AES-256-GCM 加密后存储</li>
          <li><strong>传输加密</strong>：全站使用 HTTPS 加密传输</li>
          <li><strong>手机号脱敏</strong>：对外展示时隐藏中间四位（如 138****1234）</li>
          <li><strong>微信号保护</strong>：仅组队成功后对双方可见</li>
          <li><strong>API 限流</strong>：防止恶意刷接口和数据爬取</li>
          <li><strong>定期清理</strong>：聊天记录保留 30 天后自动删除</li>
        </ul>
      </section>

      <section>
        <h2>四、信息共享</h2>
        <p>我们<strong>不会</strong>将您的个人信息出售、交易或转让给第三方。以下情况除外：</p>
        <ul>
          <li>获得您的明确同意</li>
          <li>法律法规要求</li>
          <li>保护平台和其他用户的合法权益</li>
        </ul>
      </section>

      <section>
        <h2>五、您的权利</h2>
        <ul>
          <li><strong>查看数据</strong>：在"我的"页面查看和修改您的个人资料</li>
          <li><strong>导出数据</strong>：可以导出您的所有个人数据（JSON 格式）</li>
          <li><strong>删除数据</strong>：可以注销账号并删除所有关联数据</li>
          <li><strong>举报违规</strong>：可以举报违规用户、活动、评论或聊天内容</li>
        </ul>
      </section>

      <section>
        <h2>六、未成年人保护</h2>
        <p>本平台不面向 18 岁以下未成年人提供服务。如果我们发现未成年人注册使用，将主动删除相关信息。</p>
      </section>

      <section>
        <h2>七、联系我们</h2>
        <p>如果您对本隐私政策有任何疑问，请通过平台内的举报功能联系我们。</p>
      </section>
    </div>
  </div>
</template>

<style scoped>
.privacy-page { max-width: 700px; margin: 0 auto; padding: 20px; }
.privacy-card { background: var(--bg-card); border-radius: 16px; padding: 30px; border: 1px solid var(--border-color); }
h1 { font-size: 24px; font-weight: 800; color: var(--primary-dark); margin-bottom: 4px; }
.update-date { color: var(--text-muted); font-size: 13px; margin-bottom: 24px; }
h2 { font-size: 16px; font-weight: 700; margin: 20px 0 10px; color: var(--text-primary); }
p, li { font-size: 14px; line-height: 1.8; color: var(--text-secondary); }
ul { padding-left: 20px; }
section { margin-bottom: 10px; }
</style>
