# 🎬 Global Drama Tracker（全球影视追踪平台）

一个追踪中日韩欧美电视剧和电影每日更新的平台，支持用户评论、智能评分和讨论社区。

## ✨ 功能特性

### 📺 影视追踪
- 支持中国、日本、韩国、欧洲、美国等地区的电视剧和电影
- 每日自动更新最新剧集信息
- 分类浏览：按地区、类型、年份、状态筛选

### ⭐ 评分系统
- 用户打分（1-10分）
- 基于评论情感分析的智能评分
- 综合评分算法（用户评分 + 情感分析 + 热度权重）

### 📊 排行榜
- 日榜、周榜、月榜、总榜
- 地区榜单（中剧榜、日剧榜、韩剧榜、欧美剧榜）
- 类型榜单（剧情、喜剧、悬疑、科幻等）
- 新剧热度榜

### 💬 评论与讨论
- 剧集评论
- 讨论区（话题帖子）
- 回复和点赞
- @提及和通知

### 👤 用户系统
- 注册/登录
- 个人追剧清单
- 观看进度记录
- 消息通知

## 🛠️ 技术栈

### 后端
- **语言**: Java 17
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0 + Redis
- **ORM**: MyBatis Plus
- **安全**: Spring Security + JWT
- **文档**: SpringDoc OpenAPI

### 前端（规划中）
- **框架**: Vue 3 + TypeScript
- **UI**: Element Plus
- **状态管理**: Pinia

## 📁 项目结构

```
global-drama-tracker/
├── backend/                    # 后端服务
│   └── drama-tracker-service/
│       ├── src/main/java/
│       │   └── com/drama/tracker/
│       │       ├── DramaTrackerApplication.java
│       │       ├── config/         # 配置类
│       │       ├── controller/     # 控制器
│       │       ├── service/        # 业务逻辑
│       │       ├── dao/            # 数据访问
│       │       │   ├── entity/     # 实体类
│       │       │   └── mapper/     # MyBatis Mapper
│       │       ├── dto/            # 数据传输对象
│       │       ├── vo/             # 视图对象
│       │       ├── common/         # 公共组件
│       │       └── utils/          # 工具类
│       └── src/main/resources/
│           ├── application.yml
│           └── mapper/             # MyBatis XML
├── frontend/                   # 前端应用（规划中）
├── docs/                       # 项目文档
│   ├── api/                    # API 文档
│   └── database/               # 数据库设计
└── docker/                     # Docker 配置
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 本地运行

```bash
# 克隆项目
git clone https://github.com/yourusername/global-drama-tracker.git

# 进入后端目录
cd global-drama-tracker/backend/drama-tracker-service

# 安装依赖并运行
mvn clean install
mvn spring-boot:run
```

### 访问地址
- API 文档: http://localhost:8080/swagger-ui.html
- 健康检查: http://localhost:8080/actuator/health

## 📝 数据库设计

### 核心表
- `drama` - 剧集/电影信息
- `episode` - 剧集分集信息
- `user` - 用户信息
- `comment` - 评论
- `rating` - 评分
- `discussion` - 讨论帖子
- `reply` - 回复
- `user_watchlist` - 用户追剧清单
- `ranking` - 排行榜快照

## 📈 后续规划

- [ ] v1.0 - 基础功能（影视信息、评论、评分、排行榜）
- [ ] v1.1 - 讨论社区
- [ ] v1.2 - 用户追剧清单
- [ ] v2.0 - 数据爬取与自动更新
- [ ] v2.1 - 评论情感分析
- [ ] v3.0 - 推荐系统

## 📄 License

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
