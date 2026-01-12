package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.auth.*;
import com.lingnan.fruitshop.dto.customer.auth.vo.LoginResponse;
import com.lingnan.fruitshop.dto.customer.auth.vo.RegisterResponse;
import com.lingnan.fruitshop.dto.customer.auth.vo.CaptchaResponse;
import com.lingnan.fruitshop.security.SecurityUtils;
import com.lingnan.fruitshop.service.AuthService;
import com.lingnan.fruitshop.service.CaptchaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    public AuthController(AuthService authService, CaptchaService captchaService) {
        this.authService = authService;
        this.captchaService = captchaService;
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        CaptchaService.CaptchaResult result = captchaService.generate();
        return ApiResponse.success(new CaptchaResponse(result.captchaKey(), result.captchaImage()));
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.success("注册成功", authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.success("登录成功", authService.login(req));
    }

    @PostMapping("/loginByPhone")
    public ApiResponse<LoginResponse> loginByPhone(@Valid @RequestBody LoginByPhoneRequest req) {
        return ApiResponse.success("登录成功", authService.loginByPhone(req));
    }

    @PostMapping("/sendCode")
    public ApiResponse<Void> sendCode(@Valid @RequestBody SendCodeRequest req) {
        authService.sendCode(req);
        return ApiResponse.success("success", null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success("success", null);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh() {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success("success", authService.refreshToken(userId));
    }

    @PostMapping("/resetPassword")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ApiResponse.success("success", null);
    }
}
