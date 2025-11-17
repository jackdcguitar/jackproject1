package com.jackproject.mall.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 統一 API 返回結果封裝類
 *
 * 技術說明：
 * - Lombok：用於簡化 Java 代碼的工具庫
 * - 使用位置：所有 Controller 的返回值類型
 * - 作用：統一 API 返回格式，提供一致的介面響應結構
 *
 * 返回格式：
 * {
 *   "code": 200,           // 狀態碼
 *   "message": "操作成功",  // 提示訊息
 *   "data": { ... }        // 返回資料
 * }
 *
 * 註解說明：
 * @Data：Lombok 註解，自動生成 getter、setter、toString、equals、hashCode 方法
 * @NoArgsConstructor：自動生成無參建構子
 * @AllArgsConstructor：自動生成全參建構子
 *
 * @param <T> 返回資料的類型（泛型）
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 狀態碼
     * 200：成功
     * 400：客戶端錯誤（參數錯誤）
     * 401：未授權（未登入或 Token 失效）
     * 403：禁止訪問（無權限）
     * 404：資源不存在
     * 500：伺服器錯誤
     */
    private Integer code;

    /**
     * 提示訊息
     * 用於向前端顯示操作結果的描述訊息
     */
    private String message;

    /**
     * 返回資料
     * 可以是任何類型的物件（使用泛型）
     */
    private T data;

    /**
     * 成功返回（帶資料）
     *
     * 使用場景：查詢、新增、更新、刪除等操作成功時返回資料
     *
     * @param data 要返回的資料
     * @param <T> 資料類型
     * @return Result 物件
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回（帶資料和自訂訊息）
     *
     * 使用場景：需要自訂成功訊息時
     *
     * @param data 要返回的資料
     * @param message 自訂訊息
     * @param <T> 資料類型
     * @return Result 物件
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 成功返回（無資料）
     *
     * 使用場景：操作成功但不需要返回資料時
     *
     * @param <T> 資料類型
     * @return Result 物件
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功返回（自訂訊息，無資料）
     *
     * @param message 自訂訊息
     * @param <T> 資料類型
     * @return Result 物件
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 失敗返回（使用預定義的錯誤碼）
     *
     * 使用場景：使用系統定義的標準錯誤碼
     *
     * @param resultCode 錯誤碼枚舉
     * @param <T> 資料類型
     * @return Result 物件
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失敗返回（自訂錯誤訊息）
     *
     * 使用場景：需要返回特定的錯誤訊息
     *
     * @param message 錯誤訊息
     * @param <T> 資料類型
     * @return Result 物件
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }

    /**
     * 失敗返回（自訂狀態碼和錯誤訊息）
     *
     * 使用場景：需要返回特定的狀態碼和錯誤訊息
     *
     * @param code 狀態碼
     * @param message 錯誤訊息
     * @param <T> 資料類型
     * @return Result 物件
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 判斷是否成功
     *
     * @return true：成功，false：失敗
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
