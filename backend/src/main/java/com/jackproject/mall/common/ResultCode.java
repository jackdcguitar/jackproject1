package com.jackproject.mall.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 統一狀態碼枚舉類
 *
 * 技術說明：
 * - Java 枚舉（Enum）：用於定義一組常量
 * - 使用位置：Result 類中的狀態碼定義
 * - 作用：統一管理系統中所有的狀態碼和訊息
 *
 * 狀態碼分類：
 * - 2xx：成功
 * - 4xx：客戶端錯誤
 * - 5xx：伺服器錯誤
 *
 * 註解說明：
 * @Getter：Lombok 註解，自動生成 getter 方法
 * @AllArgsConstructor：自動生成全參建構子
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /* ==================== 成功狀態碼 ==================== */

    /**
     * 操作成功
     * 使用場景：所有成功的操作
     */
    SUCCESS(200, "操作成功"),

    /* ==================== 客戶端錯誤狀態碼（4xx） ==================== */

    /**
     * 請求參數錯誤
     * 使用場景：參數驗證失敗、參數格式錯誤等
     */
    PARAM_ERROR(400, "請求參數錯誤"),

    /**
     * 未授權（未登入）
     * 使用場景：用戶未登入或 Token 失效
     * 技術：Spring Security + JWT
     */
    UNAUTHORIZED(401, "請先登入"),

    /**
     * 禁止訪問（無權限）
     * 使用場景：用戶已登入但沒有訪問權限
     * 技術：Spring Security 權限控制
     */
    FORBIDDEN(403, "無權限訪問"),

    /**
     * 資源不存在
     * 使用場景：查詢的資料不存在
     */
    NOT_FOUND(404, "資源不存在"),

    /**
     * 請求方法不支援
     * 使用場景：HTTP 方法錯誤（如應該用 POST 卻用了 GET）
     */
    METHOD_NOT_ALLOWED(405, "請求方法不支援"),

    /**
     * 資料已存在
     * 使用場景：新增資料時發現重複（如用戶名已存在）
     */
    ALREADY_EXISTS(409, "資料已存在"),

    /* ==================== 業務錯誤狀態碼 ==================== */

    /**
     * 用戶名或密碼錯誤
     * 使用場景：登入驗證失敗
     */
    LOGIN_ERROR(4001, "用戶名或密碼錯誤"),

    /**
     * 驗證碼錯誤
     * 使用場景：圖形驗證碼或簡訊驗證碼錯誤
     */
    CAPTCHA_ERROR(4002, "驗證碼錯誤"),

    /**
     * 用戶名已存在
     * 使用場景：註冊時用戶名重複
     */
    USERNAME_EXISTS(4003, "用戶名已存在"),

    /**
     * 手機號已存在
     * 使用場景：註冊時手機號重複
     */
    PHONE_EXISTS(4004, "手機號已存在"),

    /**
     * 庫存不足
     * 使用場景：下單時商品庫存不足
     */
    STOCK_NOT_ENOUGH(4005, "庫存不足"),

    /**
     * 商品已下架
     * 使用場景：購買已下架的商品
     */
    PRODUCT_OFF_SHELF(4006, "商品已下架"),

    /**
     * 訂單不存在
     * 使用場景：查詢或操作不存在的訂單
     */
    ORDER_NOT_FOUND(4007, "訂單不存在"),

    /**
     * 訂單狀態不允許該操作
     * 使用場景：如已取消的訂單不能再次取消
     */
    ORDER_STATUS_ERROR(4008, "訂單狀態不允許該操作"),

    /**
     * 餘額不足
     * 使用場景：使用餘額支付時餘額不足
     */
    BALANCE_NOT_ENOUGH(4009, "餘額不足"),

    /* ==================== 伺服器錯誤狀態碼（5xx） ==================== */

    /**
     * 系統錯誤
     * 使用場景：伺服器內部錯誤、未預期的異常
     */
    ERROR(500, "系統錯誤，請稍後再試"),

    /**
     * 資料庫錯誤
     * 使用場景：資料庫操作失敗
     * 技術：MyBatis Plus + MySQL
     */
    DATABASE_ERROR(5001, "資料庫錯誤"),

    /**
     * Redis 快取錯誤
     * 使用場景：Redis 連接失敗或操作失敗
     * 技術：Spring Data Redis
     */
    REDIS_ERROR(5002, "快取服務錯誤"),

    /**
     * 檔案上傳失敗
     * 使用場景：圖片或檔案上傳到 OSS 失敗
     * 技術：阿里雲 OSS
     */
    FILE_UPLOAD_ERROR(5003, "檔案上傳失敗"),

    /**
     * 第三方服務錯誤
     * 使用場景：調用外部 API 失敗（如支付介面）
     */
    THIRD_PARTY_ERROR(5004, "第三方服務錯誤");

    /**
     * 狀態碼
     */
    private final Integer code;

    /**
     * 提示訊息
     */
    private final String message;
}
