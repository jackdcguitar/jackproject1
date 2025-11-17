-- ==========================================
-- 商城系統資料庫結構設計
-- 資料庫：MySQL 8.0+
-- 字元集：utf8mb4（支援繁體中文和表情符號）
-- 排序規則：utf8mb4_unicode_ci
-- ==========================================

-- 建立資料庫
CREATE DATABASE IF NOT EXISTS mall_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE mall_db;

-- ==========================================
-- 用戶相關資料表
-- ==========================================

-- 用戶表
-- 技術：MyBatis Plus ORM 映射
-- 用途：儲存用戶基本資訊、帳號資訊、統計資料
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用戶ID（主鍵，自動增長）',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用戶名（唯一，用於登入）',
    password VARCHAR(255) NOT NULL COMMENT '密碼（BCrypt 加密）',
    nickname VARCHAR(50) COMMENT '暱稱',
    real_name VARCHAR(50) COMMENT '真實姓名',
    phone VARCHAR(20) UNIQUE COMMENT '手機號碼（唯一）',
    email VARCHAR(100) COMMENT '電子郵件',
    gender TINYINT DEFAULT 0 COMMENT '性別：0-未知，1-男，2-女',
    avatar VARCHAR(255) COMMENT '頭像 URL（阿里雲 OSS）',
    birthday DATE COMMENT '生日',
    level TINYINT DEFAULT 1 COMMENT '用戶等級：1-普通，2-銀牌，3-金牌，4-鑽石',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '帳戶餘額',
    points INT DEFAULT 0 COMMENT '積分',
    status TINYINT DEFAULT 1 COMMENT '帳號狀態：0-停用，1-啟用',
    last_login_time DATETIME COMMENT '最後登入時間',
    last_login_ip VARCHAR(50) COMMENT '最後登入 IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '邏輯刪除：0-未刪除，1-已刪除',

    INDEX idx_username (username) COMMENT '用戶名索引（加快登入查詢）',
    INDEX idx_phone (phone) COMMENT '手機號索引',
    INDEX idx_create_time (create_time) COMMENT '建立時間索引（用於統計）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用戶表';

-- ==========================================
-- 商品相關資料表
-- ==========================================

-- 商品分類表
-- 技術：樹狀結構（parent_id 指向父分類）
-- 用途：多層級商品分類管理
DROP TABLE IF EXISTS t_category;
CREATE TABLE t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分類ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分類ID（0 表示頂層分類）',
    name VARCHAR(100) NOT NULL COMMENT '分類名稱',
    icon VARCHAR(255) COMMENT '分類圖示 URL',
    level TINYINT DEFAULT 1 COMMENT '分類層級：1-一級，2-二級，3-三級',
    sort_order INT DEFAULT 0 COMMENT '排序序號（越小越靠前）',
    status TINYINT DEFAULT 1 COMMENT '狀態：0-停用，1-啟用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '邏輯刪除',

    INDEX idx_parent_id (parent_id) COMMENT '父分類索引（查詢子分類）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分類表';

-- 商品表
-- 技術：MyBatis Plus + Redis 快取
-- 用途：儲存商品詳細資訊
DROP TABLE IF EXISTS t_product;
CREATE TABLE t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    category_id BIGINT NOT NULL COMMENT '分類ID（外鍵）',
    name VARCHAR(200) NOT NULL COMMENT '商品名稱',
    brand VARCHAR(100) COMMENT '品牌',
    main_image VARCHAR(255) COMMENT '主圖 URL',
    detail_images TEXT COMMENT '詳情圖片（JSON 陣列）',
    original_price DECIMAL(10,2) COMMENT '原價',
    current_price DECIMAL(10,2) NOT NULL COMMENT '現價（售價）',
    cost_price DECIMAL(10,2) COMMENT '成本（僅管理員可見）',
    stock INT DEFAULT 0 COMMENT '庫存數量',
    sales INT DEFAULT 0 COMMENT '已售數量',
    unit VARCHAR(20) DEFAULT '件' COMMENT '商品單位',
    weight INT COMMENT '重量（克）',
    summary VARCHAR(500) COMMENT '商品簡介',
    description TEXT COMMENT '商品詳細描述（HTML）',
    specifications TEXT COMMENT '商品規格（JSON）',
    is_hot TINYINT DEFAULT 0 COMMENT '是否熱門：0-否，1-是',
    is_new TINYINT DEFAULT 0 COMMENT '是否新品：0-否，1-是',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推薦：0-否，1-是',
    status TINYINT DEFAULT 1 COMMENT '狀態：0-下架，1-上架',
    sort_order INT DEFAULT 0 COMMENT '排序序號',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '評分（0.0-5.0）',
    comment_count INT DEFAULT 0 COMMENT '評論數量',
    view_count INT DEFAULT 0 COMMENT '瀏覽次數',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '邏輯刪除',

    INDEX idx_category_id (category_id) COMMENT '分類索引',
    INDEX idx_status (status) COMMENT '狀態索引',
    INDEX idx_sales (sales DESC) COMMENT '銷量索引（熱門商品排序）',
    INDEX idx_create_time (create_time DESC) COMMENT '建立時間索引（新品排序）',
    FULLTEXT INDEX idx_fulltext (name, brand, summary) COMMENT '全文索引（商品搜尋）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ==========================================
-- 購物車相關資料表
-- ==========================================

-- 購物車表
-- 技術：Redis 快取（主） + MySQL（輔，持久化）
-- 用途：儲存用戶購物車商品
DROP TABLE IF EXISTS t_cart;
CREATE TABLE t_cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '購物車ID',
    user_id BIGINT NOT NULL COMMENT '用戶ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT DEFAULT 1 COMMENT '商品數量',
    selected TINYINT DEFAULT 1 COMMENT '是否選中：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入時間',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    UNIQUE INDEX uk_user_product (user_id, product_id) COMMENT '用戶商品唯一索引',
    INDEX idx_user_id (user_id) COMMENT '用戶索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='購物車表';

-- ==========================================
-- 訂單相關資料表
-- ==========================================

-- 訂單表
-- 技術：分庫分表（大型系統）
-- 用途：儲存訂單基本資訊
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '訂單ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '訂單編號（唯一）',
    user_id BIGINT NOT NULL COMMENT '用戶ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '訂單總金額',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '實付金額（扣除優惠後）',
    freight DECIMAL(10,2) DEFAULT 0.00 COMMENT '運費',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '優惠金額',
    pay_type TINYINT COMMENT '支付方式：1-支付寶，2-微信，3-餘額',
    status TINYINT DEFAULT 1 COMMENT '訂單狀態：1-待付款，2-待發貨，3-待收貨，4-已完成，5-已取消',
    receiver_name VARCHAR(50) COMMENT '收貨人姓名',
    receiver_phone VARCHAR(20) COMMENT '收貨人電話',
    receiver_province VARCHAR(50) COMMENT '收貨地址-省',
    receiver_city VARCHAR(50) COMMENT '收貨地址-市',
    receiver_district VARCHAR(50) COMMENT '收貨地址-區',
    receiver_address VARCHAR(255) COMMENT '收貨地址-詳細地址',
    remark VARCHAR(500) COMMENT '訂單備註',
    pay_time DATETIME COMMENT '支付時間',
    delivery_time DATETIME COMMENT '發貨時間',
    receive_time DATETIME COMMENT '收貨時間',
    cancel_time DATETIME COMMENT '取消時間',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '邏輯刪除',

    INDEX idx_order_no (order_no) COMMENT '訂單編號索引',
    INDEX idx_user_id (user_id) COMMENT '用戶索引',
    INDEX idx_status (status) COMMENT '狀態索引',
    INDEX idx_create_time (create_time DESC) COMMENT '建立時間索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單表';

-- 訂單商品表（訂單項）
-- 用途：儲存訂單中的商品明細
DROP TABLE IF EXISTS t_order_item;
CREATE TABLE t_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '訂單項ID',
    order_id BIGINT NOT NULL COMMENT '訂單ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名稱（冗餘，防止商品資訊變更）',
    product_image VARCHAR(255) COMMENT '商品圖片',
    product_price DECIMAL(10,2) NOT NULL COMMENT '商品單價',
    quantity INT NOT NULL COMMENT '購買數量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小計',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_order_id (order_id) COMMENT '訂單索引',
    INDEX idx_product_id (product_id) COMMENT '商品索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單商品表';

-- ==========================================
-- 收貨地址表
-- ==========================================

DROP TABLE IF EXISTS t_address;
CREATE TABLE t_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用戶ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收貨人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收貨人電話',
    province VARCHAR(50) NOT NULL COMMENT '省',
    city VARCHAR(50) NOT NULL COMMENT '市',
    district VARCHAR(50) NOT NULL COMMENT '區',
    address VARCHAR(255) NOT NULL COMMENT '詳細地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否預設地址：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '邏輯刪除',

    INDEX idx_user_id (user_id) COMMENT '用戶索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收貨地址表';

-- ==========================================
-- 評論表
-- ==========================================

DROP TABLE IF EXISTS t_comment;
CREATE TABLE t_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '評論ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    user_id BIGINT NOT NULL COMMENT '用戶ID',
    order_id BIGINT NOT NULL COMMENT '訂單ID',
    rating TINYINT NOT NULL COMMENT '評分（1-5星）',
    content TEXT COMMENT '評論內容',
    images TEXT COMMENT '評論圖片（JSON 陣列）',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    status TINYINT DEFAULT 1 COMMENT '狀態：0-待審核，1-已通過，2-已拒絕',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    deleted TINYINT DEFAULT 0 COMMENT '邏輯刪除',

    INDEX idx_product_id (product_id) COMMENT '商品索引',
    INDEX idx_user_id (user_id) COMMENT '用戶索引',
    INDEX idx_create_time (create_time DESC) COMMENT '建立時間索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品評論表';
