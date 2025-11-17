package com.jackproject.mall.controller;

import com.jackproject.mall.common.Result;
import com.jackproject.mall.dto.LoginDTO;
import com.jackproject.mall.dto.RegisterDTO;
import com.jackproject.mall.dto.UserDTO;
import com.jackproject.mall.entity.User;
import com.jackproject.mall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用戶控制器
 *
 * 技術說明：
 * - Spring MVC：用於構建 RESTful API 的 Web 框架
 * - 使用位置：接收前端 HTTP 請求，調用 Service 層處理業務邏輯
 * - 作用：處理用戶相關的所有 API 請求
 *
 * 註解說明：
 * @RestController：標記為 REST 控制器，相當於 @Controller + @ResponseBody
 *   - 返回值自動序列化為 JSON
 *
 * @RequestMapping：定義 API 路徑前綴
 *   - 所有方法的路徑都會加上這個前綴
 *
 * @RequiredArgsConstructor：Lombok 註解，自動生成含 final 欄位的建構子
 *   - 用於依賴注入
 *
 * @Tag：Swagger 註解，用於 API 文檔分組
 *
 * API 路徑設計：
 * - POST /api/users/register - 用戶註冊
 * - POST /api/users/login - 用戶登入
 * - POST /api/users/logout - 用戶登出
 * - GET /api/users/current - 獲取當前登入用戶資訊
 * - PUT /api/users/current - 更新當前用戶資訊
 * - PUT /api/users/password - 修改密碼
 *
 * @author Jack
 * @version 1.0.0
 * @since 2024-11-17
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用戶管理", description = "用戶註冊、登入、個人資訊管理等介面")
public class UserController {

    /**
     * 用戶服務
     * 使用 final 關鍵字配合 @RequiredArgsConstructor 實現依賴注入
     * 技術：Spring 依賴注入（DI）
     */
    private final UserService userService;

    /**
     * 用戶註冊
     *
     * 技術：
     * - @PostMapping：處理 POST 請求
     * - @RequestBody：將請求體 JSON 轉換為 Java 物件
     * - @Validated：啟用參數驗證（使用 JSR-303 驗證註解）
     *
     * 業務流程：
     * 1. 驗證請求參數（用戶名、密碼、手機號等）
     * 2. 檢查用戶名和手機號是否已存在
     * 3. 對密碼進行加密（BCrypt）
     * 4. 儲存用戶資訊到資料庫
     * 5. 返回註冊結果
     *
     * @param registerDTO 註冊資訊（用戶名、密碼、手機號等）
     * @return 註冊結果
     */
    @PostMapping("/register")
    @Operation(summary = "用戶註冊", description = "新用戶註冊介面")
    public Result<UserDTO> register(
            @Parameter(description = "註冊資訊", required = true)
            @Validated @RequestBody RegisterDTO registerDTO) {
        UserDTO userDTO = userService.register(registerDTO);
        return Result.success(userDTO, "註冊成功");
    }

    /**
     * 用戶登入
     *
     * 技術：
     * - Spring Security：用於認證和授權
     * - JWT：生成和驗證 Token
     *
     * 業務流程：
     * 1. 驗證用戶名和密碼
     * 2. 檢查帳號狀態（是否被停用）
     * 3. 更新最後登入時間和 IP
     * 4. 生成 JWT Token
     * 5. 將用戶資訊快取到 Redis（可選）
     * 6. 返回 Token 和用戶資訊
     *
     * @param loginDTO 登入資訊（用戶名、密碼）
     * @return Token 和用戶資訊
     */
    @PostMapping("/login")
    @Operation(summary = "用戶登入", description = "用戶名密碼登入，返回 JWT Token")
    public Result<String> login(
            @Parameter(description = "登入資訊", required = true)
            @Validated @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success(token, "登入成功");
    }

    /**
     * 用戶登出
     *
     * 技術：
     * - Spring Security：清除認證資訊
     * - Redis：清除快取的用戶資訊
     *
     * 業務流程：
     * 1. 獲取當前登入用戶
     * 2. 從 Redis 中刪除用戶快取
     * 3. 清除 Security Context
     * 4. 返回登出結果
     *
     * @return 登出結果
     */
    @PostMapping("/logout")
    @Operation(summary = "用戶登出", description = "登出當前用戶")
    public Result<Void> logout() {
        userService.logout();
        return Result.success("登出成功");
    }

    /**
     * 獲取當前登入用戶資訊
     *
     * 技術：
     * - Spring Security：從 SecurityContext 獲取當前用戶
     * - @GetMapping：處理 GET 請求
     *
     * 業務流程：
     * 1. 從 SecurityContext 獲取當前用戶 ID
     * 2. 查詢用戶詳細資訊
     * 3. 返回用戶資訊（隱藏密碼）
     *
     * @return 當前用戶資訊
     */
    @GetMapping("/current")
    @Operation(summary = "獲取當前用戶", description = "獲取當前登入用戶的詳細資訊")
    public Result<UserDTO> getCurrentUser() {
        UserDTO userDTO = userService.getCurrentUser();
        return Result.success(userDTO);
    }

    /**
     * 更新當前用戶資訊
     *
     * 技術：
     * - @PutMapping：處理 PUT 請求（更新操作）
     *
     * 業務流程：
     * 1. 獲取當前用戶 ID
     * 2. 驗證要更新的資訊
     * 3. 更新資料庫
     * 4. 更新 Redis 快取
     * 5. 返回更新後的用戶資訊
     *
     * @param userDTO 要更新的用戶資訊
     * @return 更新後的用戶資訊
     */
    @PutMapping("/current")
    @Operation(summary = "更新用戶資訊", description = "更新當前用戶的個人資訊")
    public Result<UserDTO> updateCurrentUser(
            @Parameter(description = "用戶資訊", required = true)
            @Validated @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateCurrentUser(userDTO);
        return Result.success(updatedUser, "更新成功");
    }

    /**
     * 修改密碼
     *
     * 業務流程：
     * 1. 獲取當前用戶
     * 2. 驗證舊密碼
     * 3. 加密新密碼
     * 4. 更新密碼
     * 5. 清除所有登入 Token（強制重新登入）
     *
     * @param oldPassword 舊密碼
     * @param newPassword 新密碼
     * @return 修改結果
     */
    @PutMapping("/password")
    @Operation(summary = "修改密碼", description = "修改當前用戶密碼")
    public Result<Void> updatePassword(
            @Parameter(description = "舊密碼", required = true) @RequestParam String oldPassword,
            @Parameter(description = "新密碼", required = true) @RequestParam String newPassword) {
        userService.updatePassword(oldPassword, newPassword);
        return Result.success("密碼修改成功，請重新登入");
    }

    /**
     * 根據 ID 獲取用戶資訊（管理員使用）
     *
     * 安全性：需要管理員權限
     * 技術：Spring Security 權限控制
     *
     * @param id 用戶 ID
     * @return 用戶資訊
     */
    @GetMapping("/{id}")
    @Operation(summary = "獲取用戶資訊", description = "根據 ID 獲取用戶資訊（管理員）")
    public Result<UserDTO> getUserById(
            @Parameter(description = "用戶 ID", required = true) @PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return Result.success(userDTO);
    }
}
