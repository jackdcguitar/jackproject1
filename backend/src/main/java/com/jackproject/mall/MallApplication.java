package com.jackproject.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 商城系統主啟動類
 *
 * 技術說明：
 * - Spring Boot：用於快速構建 Spring 應用程式的框架
 * - 使用位置：整個後端應用的入口點
 *
 * 註解說明：
 * @SpringBootApplication：Spring Boot 應用程式的核心註解，包含以下三個註解：
 *   - @Configuration：標記為配置類
 *   - @EnableAutoConfiguration：啟用自動配置
 *   - @ComponentScan：啟用組件掃描
 *
 * @MapperScan：MyBatis 的註解，用於掃描 Mapper 介面
 *   - 使用位置：指定 DAO 層介面所在的包路徑
 *   - 作用：自動為介面創建實現類，無需手動編寫 XML 配置
 *
 * @EnableCaching：啟用 Spring 快取功能
 *   - 使用技術：Spring Cache + Redis
 *   - 用途：提高資料查詢效能，減少資料庫訪問
 *
 * @EnableScheduling：啟用定時任務功能
 *   - 用途：執行定期任務，如清理過期資料、生成統計報表等
 *
 * @EnableAsync：啟用非同步方法執行
 *   - 用途：處理耗時操作，如發送郵件、簡訊通知等
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */
@SpringBootApplication
@MapperScan("com.jackproject.mall.mapper")
@EnableCaching
@EnableScheduling
@EnableAsync
public class MallApplication {

    /**
     * 應用程式主入口方法
     *
     * @param args 命令列參數
     */
    public static void main(String[] args) {
        // 啟動 Spring Boot 應用程式
        SpringApplication.run(MallApplication.class, args);

        // 輸出啟動成功資訊
        System.out.println("\n========================================");
        System.out.println("商城系統後端服務啟動成功！");
        System.out.println("API 文檔地址：http://localhost:8080/api/doc.html");
        System.out.println("========================================\n");
    }
}
