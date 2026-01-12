package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.auth.AdminChangePasswordRequest;
import com.lingnan.fruitshop.dto.admin.auth.AdminLoginRequest;
import com.lingnan.fruitshop.dto.admin.auth.AdminUpdateProfileRequest;
import com.lingnan.fruitshop.dto.admin.auth.vo.AdminCaptchaResponse;
import com.lingnan.fruitshop.dto.admin.auth.vo.AdminLoginResponse;
import com.lingnan.fruitshop.security.AdminSecurityUtils;
import com.lingnan.fruitshop.service.AdminAuthService;
import com.lingnan.fruitshop.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final CaptchaService captchaService;
    private final AdminAuthService adminAuthService;

    public AdminAuthController(CaptchaService captchaService, AdminAuthService adminAuthService) {
        this.captchaService = captchaService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/captcha")
    public ApiResponse<AdminCaptchaResponse> captcha() {
        CaptchaService.CaptchaResult result = captchaService.generate();
        return ApiResponse.success(new AdminCaptchaResponse(result.captchaKey(), result.captchaImage()));
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return ApiResponse.success("登录成功", adminAuthService.login(req, ip));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null);
    }

    @GetMapping("/info")
    public ApiResponse<AdminLoginResponse.AdminInfo> info() {
        long adminId = AdminSecurityUtils.currentAdminId();
        return ApiResponse.success(adminAuthService.currentInfo(adminId));
    }

    @PutMapping("/changePassword")
    public ApiResponse<Void> changePassword(@Valid @RequestBody AdminChangePasswordRequest req) {
        long adminId = AdminSecurityUtils.currentAdminId();
        adminAuthService.changePassword(adminId, req);
        return ApiResponse.success(null);
    }

    @PutMapping("/updateProfile")
    public ApiResponse<Void> updateProfile(@RequestBody AdminUpdateProfileRequest req) {
        long adminId = AdminSecurityUtils.currentAdminId();
        adminAuthService.updateProfile(adminId, req);
        return ApiResponse.success(null);
    }
}
