/**
 * 用戶狀態管理
 *
 * 技術說明：
 * - Pinia：Vue 3 官方推薦的狀態管理庫
 * - 使用位置：全域用戶狀態管理
 * - 作用：管理用戶資訊、登入狀態、Token 等
 *
 * Pinia 優勢：
 * - 比 Vuex 更簡單易用
 * - 完整的 TypeScript 支援
 * - 模組化設計，無需命名空間
 * - 更好的 DevTools 支援
 *
 * State 持久化：
 * - 使用 localStorage 儲存 Token 和用戶資訊
 * - 頁面重新整理後自動恢復狀態
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 定義用戶 Store
 *
 * 參數說明：
 * - 第一個參數：Store 的唯一 ID
 * - 第二個參數：Setup 函數（類似組件的 setup）
 *
 * Setup Store 語法：
 * - ref()：定義 state
 * - computed()：定義 getters
 * - function：定義 actions
 */
export const useUserStore = defineStore('user', () => {
  /**
   * ==================== State ====================
   */

  /**
   * JWT Token
   * 技術：JWT（JSON Web Token）
   * 儲存位置：localStorage
   * 用途：用戶認證，每次請求自動攜帶
   */
  const token = ref(localStorage.getItem('token') || '')

  /**
   * 用戶資訊
   * 包含：用戶名、暱稱、頭像、等級等
   */
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  /**
   * ==================== Getters ====================
   */

  /**
   * 是否已登入
   * 計算屬性：根據 token 是否存在判斷
   */
  const isLogin = computed(() => !!token.value)

  /**
   * 用戶 ID
   */
  const userId = computed(() => userInfo.value.id || null)

  /**
   * 用戶名
   */
  const username = computed(() => userInfo.value.username || '')

  /**
   * 暱稱
   */
  const nickname = computed(() => userInfo.value.nickname || userInfo.value.username || '遊客')

  /**
   * 頭像
   */
  const avatar = computed(() => userInfo.value.avatar || '/default-avatar.png')

  /**
   * 用戶等級
   */
  const level = computed(() => userInfo.value.level || 1)

  /**
   * 帳戶餘額
   */
  const balance = computed(() => userInfo.value.balance || 0)

  /**
   * 積分
   */
  const points = computed(() => userInfo.value.points || 0)

  /**
   * ==================== Actions ====================
   */

  /**
   * 登入
   *
   * 技術：Async/Await + Promise
   * 業務流程：
   * 1. 調用登入 API
   * 2. 儲存 Token 到 localStorage
   * 3. 獲取用戶資訊
   * 4. 跳轉到首頁或原頁面
   *
   * @param {Object} loginForm - 登入表單資料
   * @param {string} loginForm.username - 用戶名
   * @param {string} loginForm.password - 密碼
   * @returns {Promise<void>}
   */
  async function login(loginForm) {
    try {
      // 調用登入 API
      const res = await loginApi(loginForm)

      // 儲存 Token
      token.value = res.data
      localStorage.setItem('token', res.data)

      // 獲取用戶資訊
      await fetchUserInfo()

      // 提示登入成功
      ElMessage.success('登入成功')

      // 跳轉頁面
      const redirect = router.currentRoute.value.query.redirect || '/'
      router.push(redirect)
    } catch (error) {
      console.error('登入失敗：', error)
      throw error
    }
  }

  /**
   * 登出
   *
   * 業務流程：
   * 1. 調用登出 API（可選）
   * 2. 清除本地 Token 和用戶資訊
   * 3. 跳轉到登入頁
   */
  async function logout() {
    try {
      // 調用登出 API
      await logoutApi()
    } catch (error) {
      console.error('登出 API 調用失敗：', error)
    } finally {
      // 清除本地資料
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')

      // 提示登出成功
      ElMessage.success('登出成功')

      // 跳轉到登入頁
      router.push({ name: 'Login' })
    }
  }

  /**
   * 獲取用戶資訊
   *
   * 使用場景：
   * - 登入後獲取
   * - 頁面重新整理後恢復
   * - 更新資料後重新獲取
   *
   * @returns {Promise<void>}
   */
  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      userInfo.value = res.data

      // 儲存到 localStorage
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    } catch (error) {
      console.error('獲取用戶資訊失敗：', error)
      // 如果獲取失敗，清除 Token（可能已失效）
      token.value = ''
      localStorage.removeItem('token')
      throw error
    }
  }

  /**
   * 更新用戶資訊
   *
   * 用途：前端臨時更新用戶資訊（如修改頭像後）
   *
   * @param {Object} data - 要更新的用戶資訊
   */
  function updateUserInfo(data) {
    userInfo.value = { ...userInfo.value, ...data }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  /**
   * 初始化用戶資訊
   *
   * 使用場景：應用啟動時檢查登入狀態
   * 如果有 Token，嘗試獲取用戶資訊
   */
  async function initUserInfo() {
    if (token.value) {
      try {
        await fetchUserInfo()
      } catch (error) {
        console.error('初始化用戶資訊失敗：', error)
      }
    }
  }

  /**
   * 匯出 Store
   * 包含：state、getters、actions
   */
  return {
    // State
    token,
    userInfo,

    // Getters
    isLogin,
    userId,
    username,
    nickname,
    avatar,
    level,
    balance,
    points,

    // Actions
    login,
    logout,
    fetchUserInfo,
    updateUserInfo,
    initUserInfo
  }
})
