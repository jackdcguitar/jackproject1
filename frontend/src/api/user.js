/**
 * 用戶相關 API
 *
 * 技術說明：
 * - Axios：HTTP 請求庫
 * - 使用位置：用戶相關的所有 API 請求
 * - 作用：封裝用戶相關的 API 介面
 *
 * API 分類：
 * 1. 認證相關：登入、註冊、登出
 * 2. 用戶資訊：獲取、更新用戶資訊
 * 3. 密碼管理：修改密碼、找回密碼
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */

import request from '@/utils/request'

/**
 * 用戶註冊
 *
 * 技術：POST 請求
 * 後端介面：POST /api/users/register
 *
 * @param {Object} data - 註冊資料
 * @param {string} data.username - 用戶名
 * @param {string} data.password - 密碼
 * @param {string} data.phone - 手機號
 * @param {string} data.email - 電子郵件（可選）
 * @returns {Promise} 返回註冊結果
 */
export function register(data) {
  return request({
    url: '/users/register',
    method: 'post',
    data
  })
}

/**
 * 用戶登入
 *
 * 技術：POST 請求 + JWT
 * 後端介面：POST /api/users/login
 *
 * 業務流程：
 * 1. 發送登入請求
 * 2. 後端驗證用戶名和密碼
 * 3. 返回 JWT Token
 * 4. 前端儲存 Token 到 localStorage
 * 5. 後續請求自動攜帶 Token
 *
 * @param {Object} data - 登入資料
 * @param {string} data.username - 用戶名
 * @param {string} data.password - 密碼
 * @returns {Promise} 返回 Token 和用戶資訊
 */
export function login(data) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

/**
 * 用戶登出
 *
 * 技術：POST 請求
 * 後端介面：POST /api/users/logout
 *
 * 業務流程：
 * 1. 發送登出請求
 * 2. 後端清除 Session/Redis 快取
 * 3. 前端清除 Token
 * 4. 跳轉到登入頁
 *
 * @returns {Promise} 返回登出結果
 */
export function logout() {
  return request({
    url: '/users/logout',
    method: 'post'
  })
}

/**
 * 獲取當前登入用戶資訊
 *
 * 技術：GET 請求 + JWT
 * 後端介面：GET /api/users/current
 *
 * 使用場景：
 * - 頁面載入時獲取用戶資訊
 * - 更新後重新獲取最新資訊
 *
 * @returns {Promise} 返回用戶資訊
 */
export function getCurrentUser() {
  return request({
    url: '/users/current',
    method: 'get'
  })
}

/**
 * 更新當前用戶資訊
 *
 * 技術：PUT 請求
 * 後端介面：PUT /api/users/current
 *
 * 可更新欄位：
 * - nickname：暱稱
 * - avatar：頭像
 * - gender：性別
 * - birthday：生日
 * - email：電子郵件
 *
 * @param {Object} data - 要更新的用戶資訊
 * @returns {Promise} 返回更新後的用戶資訊
 */
export function updateCurrentUser(data) {
  return request({
    url: '/users/current',
    method: 'put',
    data
  })
}

/**
 * 修改密碼
 *
 * 技術：PUT 請求
 * 後端介面：PUT /api/users/password
 *
 * 安全性：
 * - 需要驗證舊密碼
 * - 密碼在傳輸前應該加密（可選）
 * - 後端使用 BCrypt 儲存密碼
 *
 * @param {string} oldPassword - 舊密碼
 * @param {string} newPassword - 新密碼
 * @returns {Promise} 返回修改結果
 */
export function updatePassword(oldPassword, newPassword) {
  return request({
    url: '/users/password',
    method: 'put',
    params: {
      oldPassword,
      newPassword
    }
  })
}

/**
 * 根據 ID 獲取用戶資訊（管理員使用）
 *
 * 技術：GET 請求
 * 後端介面：GET /api/users/{id}
 * 權限：需要管理員權限
 *
 * @param {number} id - 用戶 ID
 * @returns {Promise} 返回用戶資訊
 */
export function getUserById(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}
