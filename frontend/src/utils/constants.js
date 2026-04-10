/** 地区代码映射 */
export const REGIONS = {
  CN: { label: '国产剧', emoji: '🇨🇳', color: '#ef4444' },
  KR: { label: '韩剧', emoji: '🇰🇷', color: '#3b82f6' },
  JP: { label: '日剧', emoji: '🇯🇵', color: '#eab308' },
  US: { label: '美剧', emoji: '🇺🇸', color: '#22c55e' },
  UK: { label: '英剧', emoji: '🇬🇧', color: '#a855f7' },
  EU: { label: '欧洲剧', emoji: '🇪🇺', color: '#06b6d4' },
  OT: { label: '其他', emoji: '🌍', color: '#64748b' },
}

/** 剧集类型 */
export const DRAMA_TYPES = {
  1: '电视剧',
  2: '电影',
  3: '网剧',
  4: '综艺',
  5: '动漫',
  6: '纪录片',
}

/** 剧集状态 */
export const DRAMA_STATUS = {
  0: { label: '未播出', color: '#64748b', type: 'info' },
  1: { label: '连载中', color: '#22c55e', type: 'success' },
  2: { label: '已完结', color: '#3b82f6', type: '' },
  3: { label: '已停播', color: '#ef4444', type: 'danger' },
}

/** 排行榜类型 */
export const RANK_TYPES = {
  1: '日榜',
  2: '周榜',
  3: '月榜',
  4: '总榜',
}

/** 地区列表 */
export const REGION_LIST = Object.entries(REGIONS).map(([code, info]) => ({
  code,
  ...info,
}))

/** 获取地区信息 */
export function getRegionInfo(code) {
  return REGIONS[code] || REGIONS.OT
}

/** 获取类型名称 */
export function getTypeName(type) {
  return DRAMA_TYPES[type] || '未知'
}

/** 获取状态信息 */
export function getStatusInfo(status) {
  return DRAMA_STATUS[status] || DRAMA_STATUS[0]
}

/** 默认海报占位图 */
const _svgPoster = `<svg xmlns="http://www.w3.org/2000/svg" width="300" height="420" viewBox="0 0 300 420">
  <rect width="300" height="420" fill="#1e293b"/>
  <text x="150" y="210" text-anchor="middle" fill="#475569" font-size="14">No Poster</text>
</svg>`
export const DEFAULT_POSTER = 'data:image/svg+xml;base64,' + btoa(_svgPoster)
