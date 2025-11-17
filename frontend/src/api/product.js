/**
 * 商品相關 API
 *
 * 技術說明：
 * - Axios：HTTP 請求庫
 * - 使用位置：商品相關的所有 API 請求
 * - 作用：封裝商品查詢、搜尋、管理等 API 介面
 *
 * API 分類：
 * 1. 商品查詢：列表、詳情、分類
 * 2. 商品搜尋：關鍵字搜尋
 * 3. 特殊商品：熱門、新品、推薦
 * 4. 商品管理：新增、更新、刪除（管理員）
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */

import request from '@/utils/request'

/**
 * 分頁查詢商品列表
 *
 * 技術：GET 請求 + 查詢參數
 * 後端介面：GET /api/products
 *
 * 支援功能：
 * - 分頁查詢
 * - 關鍵字搜尋
 * - 分類篩選
 * - 價格區間篩選
 * - 排序（價格、銷量、時間）
 *
 * @param {Object} params - 查詢參數
 * @param {number} params.current - 當前頁碼（預設 1）
 * @param {number} params.size - 每頁記錄數（預設 10）
 * @param {string} params.keyword - 搜尋關鍵字（可選）
 * @param {number} params.categoryId - 分類 ID（可選）
 * @param {number} params.minPrice - 最低價格（可選）
 * @param {number} params.maxPrice - 最高價格（可選）
 * @param {string} params.sortBy - 排序欄位（可選：price, sales, create_time）
 * @param {string} params.sortOrder - 排序方向（可選：asc, desc）
 * @returns {Promise} 返回分頁資料
 */
export function getProducts(params) {
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

/**
 * 獲取商品詳情
 *
 * 技術：GET 請求 + 路徑參數
 * 後端介面：GET /api/products/{id}
 * 快取：後端使用 Redis 快取商品詳情
 *
 * 業務邏輯：
 * 1. 後端先查 Redis 快取
 * 2. 快取不存在則查資料庫
 * 3. 將結果快取到 Redis
 * 4. 非同步增加瀏覽次數
 *
 * @param {number} id - 商品 ID
 * @returns {Promise} 返回商品詳情
 */
export function getProductById(id) {
  return request({
    url: `/products/${id}`,
    method: 'get'
  })
}

/**
 * 根據分類查詢商品
 *
 * 技術：GET 請求
 * 後端介面：GET /api/products/category/{categoryId}
 *
 * 使用場景：
 * - 用戶點擊分類時查看該分類下的商品
 * - 支援分頁
 *
 * @param {number} categoryId - 分類 ID
 * @param {Object} params - 分頁參數
 * @param {number} params.current - 當前頁碼
 * @param {number} params.size - 每頁記錄數
 * @returns {Promise} 返回分頁資料
 */
export function getProductsByCategory(categoryId, params) {
  return request({
    url: `/products/category/${categoryId}`,
    method: 'get',
    params
  })
}

/**
 * 搜尋商品
 *
 * 技術：GET 請求 + 全文索引
 * 後端介面：GET /api/products/search
 * 搜尋範圍：商品名稱、描述、品牌
 *
 * 進階功能（可選）：
 * - Elasticsearch：更強大的搜尋引擎
 * - 搜尋建議（自動完成）
 * - 搜尋歷史記錄
 *
 * @param {string} keyword - 搜尋關鍵字
 * @param {Object} params - 分頁參數
 * @returns {Promise} 返回搜尋結果
 */
export function searchProducts(keyword, params = {}) {
  return request({
    url: '/products/search',
    method: 'get',
    params: {
      keyword,
      ...params
    }
  })
}

/**
 * 獲取熱門商品
 *
 * 技術：GET 請求 + Redis 快取
 * 後端介面：GET /api/products/hot
 * 排序規則：按銷量排序
 * 快取策略：Redis 快取 1 小時
 *
 * 使用場景：首頁熱門商品展示
 *
 * @param {number} limit - 返回數量（預設 10）
 * @returns {Promise} 返回熱門商品列表
 */
export function getHotProducts(limit = 10) {
  return request({
    url: '/products/hot',
    method: 'get',
    params: { limit }
  })
}

/**
 * 獲取新品
 *
 * 技術：GET 請求
 * 後端介面：GET /api/products/new
 * 排序規則：按建立時間倒序
 *
 * 使用場景：首頁新品展示
 *
 * @param {number} limit - 返回數量（預設 10）
 * @returns {Promise} 返回新品列表
 */
export function getNewProducts(limit = 10) {
  return request({
    url: '/products/new',
    method: 'get',
    params: { limit }
  })
}

/**
 * 獲取推薦商品
 *
 * 技術：GET 請求
 * 後端介面：GET /api/products/recommend
 *
 * 推薦策略：
 * 1. 基礎版：管理員手動標記為推薦
 * 2. 進階版：根據用戶瀏覽歷史推薦（協同過濾）
 *
 * @param {number} limit - 返回數量（預設 10）
 * @returns {Promise} 返回推薦商品列表
 */
export function getRecommendProducts(limit = 10) {
  return request({
    url: '/products/recommend',
    method: 'get',
    params: { limit }
  })
}

/**
 * 新增商品（管理員）
 *
 * 技術：POST 請求
 * 後端介面：POST /api/products
 * 權限：需要管理員權限
 *
 * @param {Object} data - 商品資料
 * @returns {Promise} 返回新增成功的商品
 */
export function createProduct(data) {
  return request({
    url: '/products',
    method: 'post',
    data
  })
}

/**
 * 更新商品（管理員）
 *
 * 技術：PUT 請求
 * 後端介面：PUT /api/products/{id}
 * 權限：需要管理員權限
 *
 * @param {number} id - 商品 ID
 * @param {Object} data - 商品資料
 * @returns {Promise} 返回更新後的商品
 */
export function updateProduct(id, data) {
  return request({
    url: `/products/${id}`,
    method: 'put',
    data
  })
}

/**
 * 刪除商品（管理員）
 *
 * 技術：DELETE 請求 + 邏輯刪除
 * 後端介面：DELETE /api/products/{id}
 * 權限：需要管理員權限
 * 刪除方式：邏輯刪除（不真正從資料庫刪除）
 *
 * @param {number} id - 商品 ID
 * @returns {Promise} 返回刪除結果
 */
export function deleteProduct(id) {
  return request({
    url: `/products/${id}`,
    method: 'delete'
  })
}
