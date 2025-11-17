package com.jackproject.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jackproject.mall.common.Result;
import com.jackproject.mall.dto.ProductDTO;
import com.jackproject.mall.entity.Product;
import com.jackproject.mall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 *
 * 技術說明：
 * - Spring MVC：構建 RESTful API
 * - 使用位置：處理商品相關的 HTTP 請求
 * - 作用：商品查詢、搜尋、分類瀏覽等功能
 *
 * API 路徑設計：
 * - GET /api/products - 分頁查詢商品列表
 * - GET /api/products/{id} - 獲取商品詳情
 * - GET /api/products/category/{categoryId} - 根據分類查詢商品
 * - GET /api/products/search - 搜尋商品
 * - GET /api/products/hot - 獲取熱門商品
 * - GET /api/products/new - 獲取新品
 * - GET /api/products/recommend - 獲取推薦商品
 * - POST /api/products - 新增商品（管理員）
 * - PUT /api/products/{id} - 更新商品（管理員）
 * - DELETE /api/products/{id} - 刪除商品（管理員）
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品查詢、搜尋、分類等介面")
public class ProductController {

    /**
     * 商品服務
     * 技術：Spring 依賴注入
     */
    private final ProductService productService;

    /**
     * 分頁查詢商品列表
     *
     * 技術：
     * - MyBatis Plus 分頁插件：自動處理分頁查詢
     * - @RequestParam：接收 URL 查詢參數
     *
     * 查詢參數：
     * - current：當前頁碼（預設 1）
     * - size：每頁記錄數（預設 10）
     * - keyword：搜尋關鍵字（可選）
     * - categoryId：分類 ID（可選）
     * - minPrice：最低價格（可選）
     * - maxPrice：最高價格（可選）
     * - sortBy：排序欄位（可選：price, sales, create_time）
     * - sortOrder：排序方向（可選：asc, desc）
     *
     * @param current 當前頁碼
     * @param size 每頁記錄數
     * @param keyword 搜尋關鍵字
     * @return 分頁結果
     */
    @GetMapping
    @Operation(summary = "分頁查詢商品", description = "支援關鍵字搜尋、分類篩選、價格區間、排序等")
    public Result<Page<ProductDTO>> getProducts(
            @Parameter(description = "當前頁碼") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每頁記錄數") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "搜尋關鍵字") @RequestParam(required = false) String keyword,
            @Parameter(description = "分類 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "最低價格") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "最高價格") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "排序欄位") @RequestParam(required = false) String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false) String sortOrder) {

        Page<ProductDTO> page = productService.getProducts(
                current, size, keyword, categoryId, minPrice, maxPrice, sortBy, sortOrder);
        return Result.success(page);
    }

    /**
     * 獲取商品詳情
     *
     * 技術：
     * - @PathVariable：從 URL 路徑中獲取參數
     * - Redis 快取：先從 Redis 查詢，沒有再查資料庫
     *
     * 業務流程：
     * 1. 檢查 Redis 快取
     * 2. 快取不存在則查詢資料庫
     * 3. 將結果儲存到 Redis
     * 4. 增加瀏覽次數（非同步，使用 Redis 計數器）
     * 5. 返回商品詳情
     *
     * @param id 商品 ID
     * @return 商品詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "獲取商品詳情", description = "根據商品 ID 獲取詳細資訊")
    public Result<ProductDTO> getProductById(
            @Parameter(description = "商品 ID", required = true) @PathVariable Long id) {
        ProductDTO productDTO = productService.getProductById(id);
        return Result.success(productDTO);
    }

    /**
     * 根據分類查詢商品
     *
     * 使用場景：用戶點擊分類時查看該分類下的所有商品
     *
     * @param categoryId 分類 ID
     * @param current 當前頁碼
     * @param size 每頁記錄數
     * @return 分頁結果
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根據分類查詢商品", description = "查詢指定分類下的所有商品")
    public Result<Page<ProductDTO>> getProductsByCategory(
            @Parameter(description = "分類 ID", required = true) @PathVariable Long categoryId,
            @Parameter(description = "當前頁碼") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每頁記錄數") @RequestParam(defaultValue = "10") Long size) {

        Page<ProductDTO> page = productService.getProductsByCategory(categoryId, current, size);
        return Result.success(page);
    }

    /**
     * 搜尋商品
     *
     * 技術：
     * - MySQL 全文索引：提高搜尋效能
     * - 或使用 Elasticsearch：更強大的搜尋引擎（進階功能）
     *
     * 搜尋範圍：
     * - 商品名稱
     * - 商品描述
     * - 商品品牌
     *
     * @param keyword 搜尋關鍵字
     * @param current 當前頁碼
     * @param size 每頁記錄數
     * @return 搜尋結果
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋商品", description = "根據關鍵字搜尋商品名稱、描述、品牌")
    public Result<Page<ProductDTO>> searchProducts(
            @Parameter(description = "搜尋關鍵字", required = true) @RequestParam String keyword,
            @Parameter(description = "當前頁碼") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每頁記錄數") @RequestParam(defaultValue = "10") Long size) {

        Page<ProductDTO> page = productService.searchProducts(keyword, current, size);
        return Result.success(page);
    }

    /**
     * 獲取熱門商品
     *
     * 排序規則：按銷量排序
     * 快取策略：Redis 快取 1 小時
     *
     * @param limit 返回數量（預設 10）
     * @return 熱門商品列表
     */
    @GetMapping("/hot")
    @Operation(summary = "獲取熱門商品", description = "按銷量排序的熱門商品列表")
    public Result<Page<ProductDTO>> getHotProducts(
            @Parameter(description = "返回數量") @RequestParam(defaultValue = "10") Long limit) {
        Page<ProductDTO> page = productService.getHotProducts(limit);
        return Result.success(page);
    }

    /**
     * 獲取新品
     *
     * 排序規則：按建立時間倒序
     *
     * @param limit 返回數量（預設 10）
     * @return 新品列表
     */
    @GetMapping("/new")
    @Operation(summary = "獲取新品", description = "最新上架的商品列表")
    public Result<Page<ProductDTO>> getNewProducts(
            @Parameter(description = "返回數量") @RequestParam(defaultValue = "10") Long limit) {
        Page<ProductDTO> page = productService.getNewProducts(limit);
        return Result.success(page);
    }

    /**
     * 獲取推薦商品
     *
     * 推薦策略：
     * - 管理員手動標記為推薦
     * - 或根據用戶瀏覽歷史推薦（進階功能）
     *
     * @param limit 返回數量（預設 10）
     * @return 推薦商品列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "獲取推薦商品", description = "系統推薦的商品列表")
    public Result<Page<ProductDTO>> getRecommendProducts(
            @Parameter(description = "返回數量") @RequestParam(defaultValue = "10") Long limit) {
        Page<ProductDTO> page = productService.getRecommendProducts(limit);
        return Result.success(page);
    }

    /**
     * 新增商品（管理員）
     *
     * 安全性：需要管理員權限
     * 技術：Spring Security 權限控制
     *
     * @param productDTO 商品資訊
     * @return 新增成功的商品
     */
    @PostMapping
    @Operation(summary = "新增商品", description = "新增商品資訊（管理員）")
    public Result<ProductDTO> createProduct(
            @Parameter(description = "商品資訊", required = true)
            @Validated @RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.createProduct(productDTO);
        return Result.success(created, "新增成功");
    }

    /**
     * 更新商品（管理員）
     *
     * @param id 商品 ID
     * @param productDTO 商品資訊
     * @return 更新後的商品
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商品", description = "更新商品資訊（管理員）")
    public Result<ProductDTO> updateProduct(
            @Parameter(description = "商品 ID", required = true) @PathVariable Long id,
            @Parameter(description = "商品資訊", required = true)
            @Validated @RequestBody ProductDTO productDTO) {
        ProductDTO updated = productService.updateProduct(id, productDTO);
        return Result.success(updated, "更新成功");
    }

    /**
     * 刪除商品（管理員）
     *
     * 刪除方式：邏輯刪除（不真正從資料庫刪除）
     * 技術：MyBatis Plus 邏輯刪除
     *
     * @param id 商品 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除商品", description = "刪除商品（管理員）")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品 ID", required = true) @PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("刪除成功");
    }
}
