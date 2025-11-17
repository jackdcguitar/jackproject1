package com.jackproject.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品實體類
 *
 * 技術說明：
 * - MyBatis Plus：用於資料庫 ORM 映射
 * - 使用位置：與資料庫表 t_product 對應
 * - 作用：封裝商品資訊，包含商品的基本屬性、價格、庫存等
 *
 * 業務說明：
 * - 支援商品分類管理
 * - 支援庫存管理
 * - 支援價格管理（原價、現價）
 * - 支援商品上下架
 * - 支援熱門商品標記
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品 ID（主鍵）
     * 資料類型：BIGINT
     * 自動增長
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名稱
     * 資料類型：VARCHAR(200)
     * 用途：顯示在商品列表和詳情頁
     */
    @TableField("name")
    private String name;

    /**
     * 商品分類 ID
     * 資料類型：BIGINT
     * 外鍵：關聯 t_category 表
     * 用途：商品分類管理和篩選
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 商品品牌
     * 資料類型：VARCHAR(100)
     * 用途：品牌篩選和搜尋
     */
    @TableField("brand")
    private String brand;

    /**
     * 商品主圖
     * 資料類型：VARCHAR(255)
     * 儲存位置：阿里雲 OSS
     * 用途：商品列表縮圖展示
     */
    @TableField("main_image")
    private String mainImage;

    /**
     * 商品詳情圖片（多張）
     * 資料類型：TEXT
     * 格式：JSON 陣列，如 ["url1", "url2", "url3"]
     * 儲存位置：阿里雲 OSS
     * 用途：商品詳情頁輪播展示
     */
    @TableField("detail_images")
    private String detailImages;

    /**
     * 商品原價
     * 資料類型：DECIMAL(10,2)
     * 用途：顯示劃線價格，突顯優惠力度
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 商品現價（售價）
     * 資料類型：DECIMAL(10,2)
     * 用途：實際銷售價格
     */
    @TableField("current_price")
    private BigDecimal currentPrice;

    /**
     * 商品成本
     * 資料類型：DECIMAL(10,2)
     * 用途：利潤計算（僅管理員可見）
     */
    @TableField("cost_price")
    private BigDecimal costPrice;

    /**
     * 庫存數量
     * 資料類型：INT
     * 用途：庫存管理，下單時扣減
     * 注意：需要處理併發扣減庫存的問題（樂觀鎖或分散式鎖）
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 已售數量
     * 資料類型：INT
     * 用途：銷量統計和熱門商品排序
     */
    @TableField("sales")
    private Integer sales;

    /**
     * 商品單位
     * 資料類型：VARCHAR(20)
     * 例如：件、盒、組、套等
     */
    @TableField("unit")
    private String unit;

    /**
     * 商品重量（克）
     * 資料類型：INT
     * 用途：計算運費
     */
    @TableField("weight")
    private Integer weight;

    /**
     * 商品簡介
     * 資料類型：VARCHAR(500)
     * 用途：商品列表中的簡短描述
     */
    @TableField("summary")
    private String summary;

    /**
     * 商品詳細描述
     * 資料類型：TEXT
     * 格式：HTML 富文本
     * 用途：商品詳情頁展示
     */
    @TableField("description")
    private String description;

    /**
     * 商品規格參數
     * 資料類型：TEXT
     * 格式：JSON，如 {"顏色": "黑色", "尺寸": "L", "材質": "棉"}
     * 用途：商品詳情頁展示規格資訊
     */
    @TableField("specifications")
    private String specifications;

    /**
     * 是否熱門商品
     * 資料類型：TINYINT
     * 0：否，1：是
     * 用途：首頁熱門商品推薦
     */
    @TableField("is_hot")
    private Integer isHot;

    /**
     * 是否新品
     * 資料類型：TINYINT
     * 0：否，1：是
     * 用途：首頁新品推薦
     */
    @TableField("is_new")
    private Integer isNew;

    /**
     * 是否推薦
     * 資料類型：TINYINT
     * 0：否，1：是
     * 用途：首頁推薦商品展示
     */
    @TableField("is_recommend")
    private Integer isRecommend;

    /**
     * 商品狀態
     * 資料類型：TINYINT
     * 0：已下架，1：已上架
     * 用途：控制商品是否可以購買
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序序號
     * 資料類型：INT
     * 用途：商品列表排序（數字越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 商品評分
     * 資料類型：DECIMAL(2,1)
     * 範圍：0.0 - 5.0
     * 計算方式：根據用戶評價自動計算
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 評論數量
     * 資料類型：INT
     * 用途：顯示商品評論總數
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 瀏覽次數
     * 資料類型：INT
     * 用途：統計商品熱度
     * 技術：可以使用 Redis 統計，定時同步到資料庫
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 建立時間
     * 資料類型：DATETIME
     * 自動填充：插入時自動設定
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新時間
     * 資料類型：DATETIME
     * 自動填充：插入和更新時自動設定
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 邏輯刪除標記
     * 資料類型：TINYINT
     * 0：未刪除，1：已刪除
     * 技術：MyBatis Plus 邏輯刪除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
