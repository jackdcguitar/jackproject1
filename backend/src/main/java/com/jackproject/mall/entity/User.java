package com.jackproject.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用戶實體類
 *
 * 技術說明：
 * - MyBatis Plus：用於簡化資料庫操作的持久層框架
 * - 使用位置：與資料庫表 t_user 對應
 * - 作用：封裝用戶資料，提供 ORM 映射
 *
 * 註解說明：
 * @TableName：指定對應的資料表名稱
 * @TableId：標記主鍵欄位
 *   - type = IdType.AUTO：主鍵自動增長
 * @TableField：標記普通欄位
 *   - fill = FieldFill.INSERT：插入時自動填充
 *   - fill = FieldFill.INSERT_UPDATE：插入和更新時自動填充
 * @TableLogic：標記邏輯刪除欄位
 *   - 刪除時不會真正刪除資料，而是將該欄位設為 1
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用戶 ID（主鍵）
     * 資料類型：BIGINT
     * 自動增長
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用戶名
     * 資料類型：VARCHAR(50)
     * 唯一索引
     * 用途：用戶登入時使用
     */
    @TableField("username")
    private String username;

    /**
     * 密碼（加密後）
     * 資料類型：VARCHAR(255)
     * 加密方式：BCrypt（由 Spring Security 提供）
     * 安全性：單向加密，無法解密
     */
    @TableField("password")
    private String password;

    /**
     * 暱稱
     * 資料類型：VARCHAR(50)
     * 用途：顯示在頁面上的名稱
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 真實姓名
     * 資料類型：VARCHAR(50)
     * 用途：訂單配送、發票開立等
     */
    @TableField("real_name")
    private String realName;

    /**
     * 手機號碼
     * 資料類型：VARCHAR(20)
     * 唯一索引
     * 用途：登入、簡訊通知、找回密碼等
     */
    @TableField("phone")
    private String phone;

    /**
     * 電子郵件
     * 資料類型：VARCHAR(100)
     * 用途：郵件通知、找回密碼等
     */
    @TableField("email")
    private String email;

    /**
     * 性別
     * 資料類型：TINYINT
     * 0：未知，1：男，2：女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 頭像 URL
     * 資料類型：VARCHAR(255)
     * 儲存位置：阿里雲 OSS
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 生日
     * 資料類型：DATE
     */
    @TableField("birthday")
    private LocalDateTime birthday;

    /**
     * 用戶等級
     * 資料類型：TINYINT
     * 1：普通會員，2：銀牌會員，3：金牌會員，4：鑽石會員
     * 用途：享受不同的優惠和折扣
     */
    @TableField("level")
    private Integer level;

    /**
     * 帳戶餘額
     * 資料類型：DECIMAL(10,2)
     * 用途：購物支付、充值提現
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 積分
     * 資料類型：INT
     * 用途：積分兌換、積分抵扣
     */
    @TableField("points")
    private Integer points;

    /**
     * 帳號狀態
     * 資料類型：TINYINT
     * 0：停用，1：啟用
     * 用途：管理員可以停用違規用戶
     */
    @TableField("status")
    private Integer status;

    /**
     * 最後登入時間
     * 資料類型：DATETIME
     * 用途：統計用戶活躍度
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最後登入 IP
     * 資料類型：VARCHAR(50)
     * 用途：安全監控
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 建立時間
     * 資料類型：DATETIME
     * 自動填充：插入時自動設定為當前時間
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新時間
     * 資料類型：DATETIME
     * 自動填充：插入和更新時自動設定為當前時間
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 邏輯刪除標記
     * 資料類型：TINYINT
     * 0：未刪除，1：已刪除
     * 技術：MyBatis Plus 邏輯刪除功能
     * 作用：刪除資料時不會真正從資料庫移除，便於資料恢復和審計
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
