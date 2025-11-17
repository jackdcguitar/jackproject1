/**
 * Vue Router 路由配置
 *
 * 技術說明：
 * - Vue Router 4：Vue 官方路由管理器
 * - 使用位置：整個應用的路由配置
 * - 作用：定義頁面路由、導航守衛、路由元資訊等
 *
 * 路由模式：
 * - History 模式：URL 沒有 #，更美觀，但需要後端支援
 * - Hash 模式：URL 有 #，無需後端配置
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

/**
 * 路由配置
 *
 * 路由結構：
 * - path：URL 路徑
 * - name：路由名稱（用於程式化導航）
 * - component：對應的組件
 * - meta：路由元資訊（自訂資料）
 *   - title：頁面標題
 *   - requiresAuth：是否需要登入
 *   - roles：需要的角色權限
 */
const routes = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      /**
       * 首頁
       * 技術：Vue 3 組件
       * 功能：輪播圖、熱門商品、新品推薦等
       */
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首頁' }
      },

      /**
       * 商品列表頁
       * 功能：商品搜尋、分類篩選、排序等
       */
      {
        path: '/products',
        name: 'ProductList',
        component: () => import('@/views/product/ProductList.vue'),
        meta: { title: '商品列表' }
      },

      /**
       * 商品詳情頁
       * 動態路由：:id 為商品 ID
       * 功能：商品詳情、評價、加入購物車等
       */
      {
        path: '/products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/product/ProductDetail.vue'),
        meta: { title: '商品詳情' }
      },

      /**
       * 購物車頁面
       * 需要登入：requiresAuth: true
       * 功能：管理購物車商品、結算等
       */
      {
        path: '/cart',
        name: 'Cart',
        component: () => import('@/views/cart/Cart.vue'),
        meta: { title: '購物車', requiresAuth: true }
      },

      /**
       * 訂單確認頁
       * 需要登入
       * 功能：確認訂單資訊、選擇地址、提交訂單等
       */
      {
        path: '/checkout',
        name: 'Checkout',
        component: () => import('@/views/order/Checkout.vue'),
        meta: { title: '確認訂單', requiresAuth: true }
      },

      /**
       * 訂單列表頁
       * 需要登入
       * 功能：查看我的訂單、訂單狀態等
       */
      {
        path: '/orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '我的訂單', requiresAuth: true }
      },

      /**
       * 訂單詳情頁
       * 需要登入
       * 動態路由：:id 為訂單 ID
       */
      {
        path: '/orders/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '訂單詳情', requiresAuth: true }
      },

      /**
       * 個人中心
       * 需要登入
       * 功能：用戶資料、收貨地址、修改密碼等
       */
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '個人中心', requiresAuth: true }
      }
    ]
  },

  /**
   * 登入頁面
   * 獨立佈局（不使用 MainLayout）
   */
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登入' }
  },

  /**
   * 註冊頁面
   */
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '註冊' }
  },

  /**
   * 404 頁面
   * 路由匹配：當所有路由都不匹配時顯示
   */
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '頁面不存在' }
  }
]

/**
 * 創建路由實例
 *
 * 參數說明：
 * - history：路由模式（History 模式）
 * - routes：路由配置
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,

  /**
   * 路由切換時的滾動行為
   * 作用：切換頁面時自動滾動到頂部
   */
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

/**
 * 全域前置守衛
 * 技術：Vue Router 導航守衛
 * 作用：在路由跳轉前執行，用於權限驗證、頁面標題設定等
 *
 * 執行時機：每次路由跳轉前
 *
 * @param {Object} to - 即將進入的路由
 * @param {Object} from - 當前導航正要離開的路由
 * @param {Function} next - 進行下一步的函數
 */
router.beforeEach((to, from, next) => {
  /**
   * 設定頁面標題
   * 技術：HTML document.title
   */
  document.title = to.meta.title ? `${to.meta.title} - 商城系統` : '商城系統'

  /**
   * 權限驗證
   * 檢查路由是否需要登入
   */
  if (to.meta.requiresAuth) {
    // 獲取用戶狀態
    const userStore = useUserStore()

    // 檢查是否已登入
    if (!userStore.isLogin) {
      // 未登入，跳轉到登入頁
      ElMessage.warning('請先登入')
      next({
        name: 'Login',
        query: { redirect: to.fullPath } // 登入後跳轉回原頁面
      })
      return
    }

    /**
     * 角色權限驗證（可選）
     * 如果路由需要特定角色才能訪問
     */
    if (to.meta.roles && to.meta.roles.length > 0) {
      const hasRole = to.meta.roles.some(role => userStore.roles.includes(role))
      if (!hasRole) {
        ElMessage.error('無權限訪問該頁面')
        next({ name: 'Home' })
        return
      }
    }
  }

  // 通過驗證，繼續導航
  next()
})

/**
 * 全域後置鉤子
 * 執行時機：路由跳轉完成後
 * 作用：可用於頁面載入完成後的處理（如關閉 Loading）
 */
router.afterEach((to, from) => {
  // 這裡可以添加頁面切換後的邏輯
  // 例如：埋點統計、頁面訪問記錄等
})

export default router
