package com.jackproject.mall.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/Knife4j API 文檔配置類
 *
 * 技術說明：
 * - Knife4j：基於 Swagger/OpenAPI 3.0 的 API 文檔工具
 * - 使用位置：整個專案的 API 文檔生成
 * - 作用：自動生成互動式 API 文檔，方便前後端聯調
 *
 * 訪問地址：
 * - Knife4j UI：http://localhost:8080/api/doc.html
 * - Swagger UI：http://localhost:8080/api/swagger-ui/index.html
 * - OpenAPI JSON：http://localhost:8080/api/v3/api-docs
 *
 * 功能特點：
 * - 自動掃描 Controller 生成 API 文檔
 * - 支援線上測試 API
 * - 支援參數說明和範例
 * - 支援認證（JWT Token）
 * - 繁體中文介面
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 資訊
     *
     * 註解說明：
     * @Bean：將方法返回值註冊為 Spring Bean
     *
     * @return OpenAPI 配置物件
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本資訊
                .info(new Info()
                        // API 標題
                        .title("商城系統 API 文檔")

                        // API 版本
                        .version("1.0.0")

                        // API 描述
                        .description("基於 Spring Boot + Vue.js 的電商平台後端 API 介面文檔\n\n" +
                                "## 技術棧\n" +
                                "- **後端框架**：Spring Boot 3.2.0\n" +
                                "- **資料庫**：MySQL 8.0 + MyBatis Plus\n" +
                                "- **快取**：Redis\n" +
                                "- **認證**：Spring Security + JWT\n" +
                                "- **文檔**：Knife4j (OpenAPI 3.0)\n\n" +
                                "## 認證說明\n" +
                                "除了註冊和登入介面外，其他介面都需要在請求頭中攜帶 Token：\n" +
                                "```\n" +
                                "Authorization: Bearer {token}\n" +
                                "```\n\n" +
                                "## 狀態碼說明\n" +
                                "- **200**：操作成功\n" +
                                "- **400**：請求參數錯誤\n" +
                                "- **401**：未登入或 Token 失效\n" +
                                "- **403**：無權限訪問\n" +
                                "- **404**：資源不存在\n" +
                                "- **500**：伺服器錯誤")

                        // 聯絡資訊
                        .contact(new Contact()
                                .name("Jack")
                                .email("jack@example.com")
                                .url("https://github.com/jackdcguitar/jackproject1"))

                        // 授權資訊
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
