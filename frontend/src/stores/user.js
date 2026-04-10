import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const isLoggedIn = computed(() => !!user.value)
  const nickname = computed(() => user.value?.nickname || '')
  const userId = computed(() => user.value?.id || null)

  function init() {
    const token = localStorage.getItem('token')
    const id = localStorage.getItem('userId')
    const name = localStorage.getItem('nickname')
    if (token && id) {
      user.value = { id: Number(id), nickname: name || '' }
    }
  }

  function setUser(data) {
    user.value = { id: data.id, phone: data.phone, nickname: data.nickname }
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.id)
    localStorage.setItem('nickname', data.nickname || '')
  }

  function logout() {
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('nickname')
  }

  return { user, isLoggedIn, nickname, userId, init, setUser, logout }
})
