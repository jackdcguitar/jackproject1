<!--
  應用根組件

  技術說明：
  - Vue 3 單文件組件（SFC）
  - 使用位置：整個應用的根組件
  - 作用：定義應用的基本結構和佈局

  組件結構：
  - template：HTML 模板
  - script：JavaScript 邏輯
  - style：CSS 樣式
-->

<template>
  <!--
    RouterView：路由視圖組件
    技術：Vue Router
    作用：根據當前路由動態渲染對應的頁面組件
    原理：類似於佔位符，會被替換為實際的頁面組件
  -->
  <router-view />
</template>

<script setup>
/**
 * script setup 語法
 * 技術：Vue 3 Composition API
 * 優勢：更簡潔的語法，無需 return，自動暴露給模板
 */

/**
 * 這裡可以添加全域的業務邏輯
 * 例如：
 * - 檢查用戶登入狀態
 * - 載入全域配置
 * - 監聽全域事件等
 */
</script>

<style scoped>
/**
 * scoped 樣式
 * 作用：樣式只對當前組件生效，不會影響其他組件
 * 原理：Vue 會為每個組件生成唯一的屬性選擇器
 */

/* 由於這是根組件，通常不需要特定樣式 */
/* 全域樣式應該放在 src/styles/index.scss */
</style>
