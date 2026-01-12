package com.lingnan.fruitshop.controller.merchant;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.merchant.auth.*;
import com.lingnan.fruitshop.dto.merchant.auth.vo.MerchantLoginResponse;
import com.lingnan.fruitshop.dto.merchant.auth.vo.MerchantRegisterResponse;
import com.lingnan.fruitshop.security.MerchantSecurityUtils;
import com.lingnan.fruitshop.service.MerchantAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/auth")
public class MerchantAuthController {

    private final MerchantAuthService merchantAuthService;

    public MerchantAuthController(MerchantAuthService merchantAuthService) {
        this.merchantAuthService = merchantAuthService;
    }

    @PostMapping("/register")
    public ApiResponse<MerchantRegisterResponse> register(@Valid @RequestBody MerchantRegisterRequest req) {
        return ApiResponse.success("申请提交成功，请等待审核", merchantAuthService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<MerchantLoginResponse> login(@Valid @RequestBody MerchantLoginRequest req) {
        return ApiResponse.success("登录成功", merchantAuthService.login(req));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null);
    }

    @GetMapping("/info")
    public ApiResponse<MerchantLoginResponse> getMerchantInfo(HttpServletRequest request) {
        long merchantId = MerchantSecurityUtils.currentMerchantId();
        return ApiResponse.success("获取成功", merchantAuthService.getMerchantInfo(merchantId));
    }

    @PutMapping("/changePassword")
    public ApiResponse<Void> changePassword(@Valid @RequestBody MerchantChangePasswordRequest req) {
        long merchantId = MerchantSecurityUtils.currentMerchantId();
        merchantAuthService.changePassword(merchantId, req);
        return ApiResponse.success(null);
    }

    @PostMapping("/resetPassword")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody MerchantResetPasswordRequest req) {
        merchantAuthService.resetPassword(req);
        return ApiResponse.success(null);
    }
}
