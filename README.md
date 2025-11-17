# 商城系統（E-commerce Mall System）

> 基於 Spring Boot 和 Vue.js 的全功能電商平台

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.0-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-20.10+-blue.svg)](https://www.docker.com/)

---

## 📖 專案簡介

這是一個功能完整的電商平台系統，採用**前後端分離**架構，使用主流技術棧開發。系統包含用戶管理、商品管理、購物車、訂單處理等完整的電商功能。

### ✨ 主要特點

- 🎨 **現代化技術棧**：Spring Boot 3 + Vue 3 + Element Plus
- 🔐 **安全認證**：Spring Security + JWT Token
- 💾 **資料持久化**：MySQL + MyBatis Plus
- ⚡ **效能優化**：Redis 快取 + Nginx 負載均衡
- 🐳 **容器化部署**：Docker + Docker Compose 一鍵部署
- 📝 **完整文檔**：詳細的繁體中文技術文檔和代碼註釋
- 🎯 **API 文檔**：Knife4j (Swagger) 互動式 API 文檔

---

## 🏗️ 技術架構

### 系統架構圖

```
┌─────────────────────────────────────────────────────────────┐
│                     前端層 (Frontend)                          │
│            Vue 3 + Vite + Element Plus + Pinia               │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP/HTTPS (RESTful API)
┌─────────────────────▼───────────────────────────────────────┐
│                   Web 伺服器 (Nginx)                           │
│              反向代理 + 靜態資源服務                             │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                     後端層 (Backend)                           │
│        Spring Boot 3 + Spring Security + MyBatis Plus        │
└─────────┬───────────┴──────────┬────────────────────────────┘
          │                      │
┌─────────▼───────────┐  ┌──────▼────────┐
│   MySQL 8.0         │  │   Redis 7     │
│   持久化儲存         │  │   快取/Session │
└─────────────────────┘  └───────────────┘
```

### 技術棧

#### 後端技術

| 技術 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 應用框架 |
| Spring Security | 6.2.0 | 安全認證 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0+ | 關係型資料庫 |
| Redis | 7.0+ | 快取和 Session |
| Druid | 1.2.20 | 資料庫連接池 |
| JWT | 0.11.5 | Token 認證 |
| Knife4j | 4.3.0 | API 文檔 |
| Lombok | - | 簡化代碼 |

#### 前端技術

| 技術 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4.0 | 前端框架 |
| Vite | 5.0.8 | 構建工具 |
| Element Plus | 2.5.0 | UI 組件庫 |
| Pinia | 2.1.7 | 狀態管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.2 | HTTP 請求 |

#### 部署技術

| 技術 | 版本 | 用途 |
|------|------|------|
| Docker | 20.10+ | 容器化 |
| Docker Compose | 2.0+ | 容器編排 |
| Nginx | 1.25 | Web 伺服器 |

---

## 📂 專案結構

```
jackproject1/
├── backend/                      # 後端 Spring Boot 專案
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/jackproject/mall/
│   │   │   │   ├── MallApplication.java     # 主啟動類
│   │   │   │   ├── common/                   # 通用類（Result, ResultCode）
│   │   │   │   ├── config/                   # 配置類（Security, Swagger, Redis）
│   │   │   │   ├── controller/               # REST 控制器
│   │   │   │   ├── entity/                   # 實體類
│   │   │   │   ├── dto/                      # 數據傳輸對象
│   │   │   │   ├── mapper/                   # MyBatis Mapper 介面
│   │   │   │   ├── service/                  # 業務邏輯層
│   │   │   │   └── utils/                    # 工具類
│   │   │   └── resources/
│   │   │       ├── application.yml           # 主配置檔案
│   │   │       ├── application-dev.yml       # 開發環境配置
│   │   │       └── application-prod.yml      # 生產環境配置
│   │   └── test/                             # 測試代碼
│   └── pom.xml                               # Maven 依賴配置
│
├── frontend/                     # 前端 Vue 專案
│   ├── public/                   # 靜態資源
│   ├── src/
│   │   ├── api/                  # API 請求封裝
│   │   ├── assets/               # 資源檔案（圖片、樣式）
│   │   ├── components/           # 可複用組件
│   │   ├── layouts/              # 佈局組件
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # Pinia 狀態管理
│   │   ├── utils/                # 工具函數
│   │   ├── views/                # 頁面組件
│   │   ├── App.vue               # 根組件
│   │   └── main.js               # 入口檔案
│   ├── index.html                # HTML 模板
│   ├── vite.config.js            # Vite 配置
│   └── package.json              # npm 依賴配置
│
├── database/                     # 資料庫相關
│   ├── schema.sql                # 資料表結構
│   └── init-data.sql             # 初始化資料
│
├── docker/                       # Docker 配置
│   ├── Dockerfile.backend        # 後端 Dockerfile
│   ├── Dockerfile.frontend       # 前端 Dockerfile
│   ├── docker-compose.yml        # Docker Compose 配置
│   └── nginx.conf                # Nginx 配置
│
├── docs/                         # 文檔
│   ├── 技術棧說明.md              # 技術棧詳細說明
│   └── 部署指南.md                # 部署步驟詳解
│
├── .gitignore                    # Git 忽略規則
└── README.md                     # 專案說明（本檔案）
```

---

## 🚀 快速開始

### 方式一：Docker 部署（推薦）

**前置要求**：
- Docker 20.10+
- Docker Compose 2.0+

**啟動步驟**：

```bash
# 1. 克隆專案
git clone https://github.com/jackdcguitar/jackproject1.git
cd jackproject1

# 2. 一鍵啟動所有服務
docker-compose -f docker/docker-compose.yml up -d

# 3. 查看服務狀態
docker-compose -f docker/docker-compose.yml ps

# 4. 查看日誌
docker-compose -f docker/docker-compose.yml logs -f
```

**訪問地址**：
- 前端：http://localhost
- 後端 API：http://localhost:8080/api
- API 文檔：http://localhost:8080/api/doc.html

**測試帳號**：
- 用戶名：`user01`
- 密碼：`123456`

---

### 方式二：本地開發部署

**前置要求**：
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+

**後端啟動**：

```bash
# 1. 建立資料庫
mysql -u root -p
CREATE DATABASE mall_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 2. 執行 SQL 腳本
mysql -u root -p mall_db < database/schema.sql
mysql -u root -p mall_db < database/init-data.sql

# 3. 啟動 Redis
redis-server

# 4. 啟動後端
cd backend
mvn spring-boot:run
```

**前端啟動**：

```bash
# 1. 安裝依賴
cd frontend
npm install

# 2. 啟動開發伺服器
npm run dev
```

詳細部署步驟請參考：[部署指南](./docs/部署指南.md)

---

## 📱 功能模組

### 用戶端功能

- ✅ **用戶認證**
  - 用戶註冊、登入、登出
  - JWT Token 認證
  - 密碼加密（BCrypt）

- ✅ **商品瀏覽**
  - 商品列表展示
  - 商品詳情查看
  - 分類篩選
  - 關鍵字搜尋
  - 價格區間篩選
  - 多條件排序

- ✅ **購物車**
  - 添加商品到購物車
  - 修改商品數量
  - 刪除購物車商品
  - 購物車統計

- ✅ **訂單管理**
  - 建立訂單
  - 訂單支付
  - 訂單查詢
  - 訂單詳情
  - 訂單取消

- ✅ **個人中心**
  - 個人資料管理
  - 收貨地址管理
  - 修改密碼
  - 訂單歷史

### 管理端功能

- ✅ **商品管理**
  - 商品新增、編輯、刪除
  - 商品上下架
  - 庫存管理
  - 分類管理

- ✅ **訂單管理**
  - 訂單列表
  - 訂單狀態更新
  - 訂單統計

- ✅ **用戶管理**
  - 用戶列表
  - 用戶停用/啟用
  - 權限管理

---

## 📊 資料庫設計

### 核心資料表

| 資料表 | 說明 | 主要欄位 |
|-------|------|---------|
| `t_user` | 用戶表 | id, username, password, phone, email |
| `t_product` | 商品表 | id, name, price, stock, sales, category_id |
| `t_category` | 分類表 | id, parent_id, name, level |
| `t_cart` | 購物車表 | id, user_id, product_id, quantity |
| `t_order` | 訂單表 | id, order_no, user_id, total_amount, status |
| `t_order_item` | 訂單項表 | id, order_id, product_id, quantity, price |
| `t_address` | 收貨地址表 | id, user_id, receiver_name, phone, address |
| `t_comment` | 評論表 | id, product_id, user_id, rating, content |

詳細資料庫設計請查看：[database/schema.sql](./database/schema.sql)

---

## 🔧 配置說明

### 後端配置

編輯 `backend/src/main/resources/application.yml`：

```yaml
# 伺服器配置
server:
  port: 8080

# 資料庫配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall_db
    username: root
    password: 123456

  # Redis 配置
  data:
    redis:
      host: localhost
      port: 6379

# JWT 配置
jwt:
  secret: your-secret-key
  expiration: 604800000  # 7 天
```

### 前端配置

建立 `frontend/.env.development`：

```bash
# API 基礎路徑
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 📚 文檔

- [技術棧詳細說明](./docs/技術棧說明.md) - 每種技術的詳細介紹和使用說明
- [部署指南](./docs/部署指南.md) - 本地開發、Docker、生產環境部署
- [API 文檔](http://localhost:8080/api/doc.html) - Knife4j 互動式 API 文檔

---

## 🎯 代碼特色

### 詳細的繁體中文註釋

所有代碼都包含詳細的繁體中文註釋，包括：

- ✅ 類別說明：技術介紹、使用位置、作用
- ✅ 方法說明：業務流程、參數說明、返回值
- ✅ 技術註解：每個註解的作用和使用場景
- ✅ 配置說明：每個配置項的用途和推薦值

**範例**：

```java
/**
 * 用戶登入
 *
 * 技術：POST 請求 + JWT
 * 後端介面：POST /api/users/login
 *
 * 業務流程：
 * 1. 發送登入請求
 * 2. 後端驗證用戶名和密碼
 * 3. 生成 JWT Token
 * 4. 前端儲存 Token 到 localStorage
 * 5. 後續請求自動攜帶 Token
 *
 * @param loginDTO 登入資料
 * @return Token 和用戶資訊
 */
@PostMapping("/login")
public Result<String> login(@RequestBody LoginDTO loginDTO) {
    // ...
}
```

---

## 🤝 貢獻指南

歡迎提交 Issue 和 Pull Request！

1. Fork 本專案
2. 建立您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

---

## 📝 授權

本專案採用 MIT 授權 - 查看 [LICENSE](LICENSE) 檔案了解詳情

---

## 👨‍💻 作者

**Jack**

- Email: jack@example.com
- GitHub: [@jackdcguitar](https://github.com/jackdcguitar)

---

## 🙏 致謝

感謝以下開源專案：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis Plus](https://baomidou.com/)
- [Knife4j](https://doc.xiaominfo.com/)

---

## 📮 聯絡方式

如有任何問題或建議，歡迎通過以下方式聯繫：

- 提交 [GitHub Issue](https://github.com/jackdcguitar/jackproject1/issues)
- 發送郵件至：jack@example.com

---

<p align="center">
  ⭐ 如果這個專案對您有幫助，請給它一個 Star！⭐
</p>
