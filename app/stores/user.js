import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMe } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const isLoggedIn = computed(() => !!user.value)
  const nickname = computed(() => user.value?.nickname || '')

  async function init() {
    const token = uni.getStorageSync('user_token')
    if (!token) return
    try {
      user.value = await getMe()
    } catch {
      logout()
    }
  }

  function setUser(data) {
    user.value = { id: data.id, phone: data.phone, nickname: data.nickname }
    uni.setStorageSync('user_token', data.token)
  }

  function logout() {
    user.value = null
    uni.removeStorageSync('user_token')
  }

  return { user, isLoggedIn, nickname, init, setUser, logout }
})
