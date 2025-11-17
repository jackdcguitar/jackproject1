/**
 * Axios 請求封裝
 *
 * 技術說明：
 * - Axios：基於 Promise 的 HTTP 客戶端
 * - 使用位置：所有 API 請求的基礎
 * - 作用：統一處理請求和響應、添加認證、錯誤處理等
 *
 * 功能特點：
 * 1. 請求攔截：自動添加 Token
 * 2. 響應攔截：統一處理錯誤
 * 3. 超時設定
 * 4. 取消重複請求（可選）
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */

import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

/**
 * 創建 Axios 實例
 *
 * 配置說明：
 * - baseURL：API 基礎路徑
 * - timeout：請求超時時間（毫秒）
 * - headers：預設請求頭
 */
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

/**
 * Loading 實例（全域）
 * 用於顯示載入動畫
 */
let loadingInstance = null

/**
 * 請求攔截器
 *
 * 技術：Axios Interceptors
 * 執行時機：發送請求前
 * 作用：
 * 1. 添加 JWT Token 到請求頭
 * 2. 顯示 Loading 動畫
 * 3. 處理請求參數
 */
service.interceptors.request.use(
  (config) => {
    /**
     * 添加 JWT Token
     * 技術：JWT（JSON Web Token）
     * 位置：Authorization 請求頭
     * 格式：Bearer {token}
     */
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    /**
     * 顯示 Loading（可選）
     * 如果請求配置中設定了 showLoading: true
     */
    if (config.showLoading) {
      loadingInstance = ElLoading.service({
        lock: true,
        text: '載入中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }

    return config
  },
  (error) => {
    // 請求錯誤處理
    console.error('請求錯誤：', error)
    return Promise.reject(error)
  }
)

/**
 * 響應攔截器
 *
 * 執行時機：收到響應後
 * 作用：
 * 1. 統一處理響應資料
 * 2. 統一處理錯誤
 * 3. Token 失效處理
 */
service.interceptors.response.use(
  (response) => {
    // 關閉 Loading
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }

    /**
     * 響應資料結構：
     * {
     *   code: 200,      // 狀態碼
     *   message: '成功', // 提示訊息
     *   data: { ... }   // 業務資料
     * }
     */
    const res = response.data

    /**
     * 根據狀態碼處理響應
     */
    if (res.code === 200) {
      // 成功：返回資料
      return res
    } else if (res.code === 401) {
      /**
       * 未授權（Token 失效）
       * 處理流程：
       * 1. 清除本地 Token
       * 2. 跳轉到登入頁
       * 3. 提示用戶重新登入
       */
      ElMessage.error(res.message || '請先登入')
      const userStore = useUserStore()
      userStore.logout()
      router.push({
        name: 'Login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
      return Promise.reject(new Error(res.message || '請先登入'))
    } else if (res.code === 403) {
      // 無權限
      ElMessage.error(res.message || '無權限訪問')
      return Promise.reject(new Error(res.message || '無權限訪問'))
    } else {
      // 其他錯誤
      ElMessage.error(res.message || '操作失敗')
      return Promise.reject(new Error(res.message || '操作失敗'))
    }
  },
  (error) => {
    // 關閉 Loading
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }

    /**
     * HTTP 錯誤處理
     * 技術：HTTP 狀態碼
     */
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400:
          ElMessage.error('請求參數錯誤')
          break
        case 401:
          ElMessage.error('請先登入')
          const userStore = useUserStore()
          userStore.logout()
          router.push({ name: 'Login' })
          break
        case 403:
          ElMessage.error('無權限訪問')
          break
        case 404:
          ElMessage.error('請求的資源不存在')
          break
        case 500:
          ElMessage.error('伺服器錯誤')
          break
        default:
          ElMessage.error(`請求失敗（${status}）`)
      }
    } else if (error.message.includes('timeout')) {
      // 請求超時
      ElMessage.error('請求超時，請稍後再試')
    } else if (error.message.includes('Network Error')) {
      // 網路錯誤
      ElMessage.error('網路連接失敗，請檢查網路')
    } else {
      ElMessage.error('請求失敗，請稍後再試')
    }

    return Promise.reject(error)
  }
)

/**
 * 匯出 Axios 實例
 * 使用方式：
 * import request from '@/utils/request'
 * request.get('/users')
 * request.post('/login', { username, password })
 */
export default service
