import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const isLoggedIn = computed(() => !!user.value)
  const nickname = computed(() => user.value?.nickname || '')

  function init() {
    const token = uni.getStorageSync('token')
    const userId = uni.getStorageSync('userId')
    const name = uni.getStorageSync('nickname')
    if (token && userId) {
      user.value = { id: userId, nickname: name }
    }
  }

  function setUser(data) {
    user.value = { id: data.id, phone: data.phone, nickname: data.nickname }
    uni.setStorageSync('token', data.token)
    uni.setStorageSync('userId', data.id)
    uni.setStorageSync('nickname', data.nickname || '')
    uni.setStorageSync('phone', data.phone || '')
  }

  function logout() {
    user.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('userId')
    uni.removeStorageSync('nickname')
    uni.removeStorageSync('phone')
  }

  return { user, isLoggedIn, nickname, init, setUser, logout }
})
