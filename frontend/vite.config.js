/**
 * Vite 配置檔案
 *
 * 技術說明：
 * - Vite：下一代前端構建工具，比 Webpack 更快
 * - 使用位置：整個前端專案的構建配置
 * - 作用：配置開發伺服器、構建選項、插件等
 *
 * 主要功能：
 * 1. Vue 3 插件支援
 * 2. 自動導入 Element Plus 組件
 * 3. 路徑別名配置
 * 4. 代理配置（解決跨域問題）
 * 5. 構建優化
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  /**
   * 插件配置
   */
  plugins: [
    // Vue 3 插件
    vue(),

    /**
     * 自動導入插件
     * 技術：unplugin-auto-import
     * 作用：自動導入 Vue 3 API 和 Element Plus API，無需手動 import
     * 例如：ref, reactive, computed 等可直接使用
     */
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts'
    }),

    /**
     * 組件自動註冊插件
     * 技術：unplugin-vue-components
     * 作用：自動註冊 Element Plus 組件，無需手動註冊
     * 例如：<el-button> 可直接使用，無需 import
     */
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts'
    })
  ],

  /**
   * 路徑別名配置
   * 作用：簡化導入路徑
   * 使用：import xxx from '@/xxx' 等同於 import xxx from 'src/xxx'
   */
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },

  /**
   * 開發伺服器配置
   */
  server: {
    // 埠號
    port: 3000,

    // 自動開啟瀏覽器
    open: true,

    // 允許外部訪問
    host: '0.0.0.0',

    /**
     * 代理配置
     * 技術：HTTP 代理
     * 作用：解決開發環境的跨域問題
     * 原理：將前端請求代理到後端伺服器
     *
     * 使用範例：
     * - 前端請求：http://localhost:3000/api/users/login
     * - 實際請求：http://localhost:8080/api/users/login
     */
    proxy: {
      '/api': {
        // 後端伺服器地址
        target: 'http://localhost:8080',

        // 改變請求來源（解決跨域）
        changeOrigin: true,

        // 是否重寫路徑（這裡不需要，因為後端也是 /api 開頭）
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },

  /**
   * 構建配置
   */
  build: {
    // 輸出目錄
    outDir: 'dist',

    // 資源目錄
    assetsDir: 'assets',

    // 啟用 CSS 程式碼分割
    cssCodeSplit: true,

    // 構建後是否生成 source map（生產環境建議關閉）
    sourcemap: false,

    /**
     * Rollup 配置
     * 作用：優化構建輸出
     */
    rollupOptions: {
      output: {
        // 分包策略：將第三方庫單獨打包
        manualChunks: {
          // Vue 核心
          'vue-vendor': ['vue', 'vue-router', 'pinia'],

          // Element Plus UI 組件庫
          'element-plus': ['element-plus', '@element-plus/icons-vue'],

          // 工具庫
          'utils': ['axios', 'dayjs']
        }
      }
    },

    // 壓縮選項
    minify: 'terser',
    terserOptions: {
      compress: {
        // 移除 console.log（生產環境）
        drop_console: true,

        // 移除 debugger
        drop_debugger: true
      }
    }
  },

  /**
   * CSS 配置
   */
  css: {
    preprocessorOptions: {
      // SCSS 全域變數
      scss: {
        additionalData: `@use "@/styles/variables.scss" as *;`
      }
    }
  }
})
