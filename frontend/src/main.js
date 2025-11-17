/**
 * Vue 應用程式主入口檔案
 *
 * 技術說明：
 * - Vue 3：漸進式 JavaScript 框架
 * - Pinia：Vue 3 官方推薦的狀態管理庫
 * - Vue Router：Vue 官方路由管理器
 * - Element Plus：基於 Vue 3 的 UI 組件庫
 *
 * 使用位置：整個前端應用的啟動檔案
 * 作用：初始化 Vue 應用、註冊插件、掛載到 DOM
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhTw from 'element-plus/es/locale/lang/zh-tw'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

import './styles/index.scss'

/**
 * 創建 Vue 應用實例
 * 技術：Vue 3 Composition API
 */
const app = createApp(App)

/**
 * 註冊 Pinia 狀態管理
 * 技術：Pinia
 * 作用：管理全域狀態（如用戶資訊、購物車等）
 * 優勢：比 Vuex 更輕量、更易用、支援 TypeScript
 */
const pinia = createPinia()
app.use(pinia)

/**
 * 註冊 Vue Router 路由
 * 技術：Vue Router 4
 * 作用：管理頁面路由和導航
 * 模式：History 模式（需要後端支援）
 */
app.use(router)

/**
 * 註冊 Element Plus UI 組件庫
 * 技術：Element Plus
 * 作用：提供豐富的 UI 組件（按鈕、表單、對話框等）
 * 語言：繁體中文
 */
app.use(ElementPlus, {
  locale: zhTw
})

/**
 * 全域註冊 Element Plus 圖標
 * 作用：可在任何組件中直接使用圖標組件
 * 使用方式：<el-icon><Edit /></el-icon>
 */
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

/**
 * 掛載 Vue 應用到 DOM
 * 掛載點：index.html 中的 <div id="app"></div>
 */
app.mount('#app')

console.log('🚀 商城系統前端啟動成功！')
console.log('📖 技術棧：Vue 3 + Vite + Element Plus + Pinia + Vue Router')
