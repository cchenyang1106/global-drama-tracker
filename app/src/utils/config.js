// 生产环境 API 地址
export const BASE_URL = 'https://global-drama-tracker-production.up.railway.app'

// 地区映射
export const REGIONS = {
  CN: { label: '国产剧', emoji: '🇨🇳', color: '#ef4444' },
  KR: { label: '韩剧', emoji: '🇰🇷', color: '#3b82f6' },
  JP: { label: '日剧', emoji: '🇯🇵', color: '#eab308' },
  US: { label: '美剧', emoji: '🇺🇸', color: '#22c55e' },
  UK: { label: '英剧', emoji: '🇬🇧', color: '#a855f7' },
  EU: { label: '欧洲剧', emoji: '🇪🇺', color: '#06b6d4' },
  OT: { label: '其他', emoji: '🌍', color: '#64748b' },
}

export const DRAMA_TYPES = { 1: '电视剧', 2: '电影', 3: '网剧', 4: '综艺', 5: '动漫', 6: '纪录片' }

export const STATUS_MAP = {
  0: { label: '未播出', color: '#64748b' },
  1: { label: '连载中', color: '#22c55e' },
  2: { label: '已完结', color: '#3b82f6' },
  3: { label: '已停播', color: '#ef4444' },
}
