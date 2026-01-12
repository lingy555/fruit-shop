package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.user.*;
import com.lingnan.fruitshop.dto.customer.user.vo.BalanceResponse;
import com.lingnan.fruitshop.dto.customer.user.vo.PointsRecordsResponse;
import com.lingnan.fruitshop.dto.customer.user.vo.UserProfileResponse;
import com.lingnan.fruitshop.security.SecurityUtils;
import com.lingnan.fruitshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> profile() {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(userService.profile(userId));
    }

    @PutMapping("/update")
    public ApiResponse<Void> update(@RequestBody UpdateProfileRequest req) {
        long userId = SecurityUtils.currentUserId();
        userService.updateProfile(userId, req);
        return ApiResponse.success(null);
    }

    @PutMapping("/changePassword")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        long userId = SecurityUtils.currentUserId();
        userService.changePassword(userId, req);
        return ApiResponse.success(null);
    }

    @PutMapping("/changePhone")
    public ApiResponse<Void> changePhone(@Valid @RequestBody ChangePhoneRequest req) {
        long userId = SecurityUtils.currentUserId();
        userService.changePhone(userId, req);
        return ApiResponse.success(null);
    }

    @GetMapping("/balance")
    public ApiResponse<BalanceResponse> balance() {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(userService.balance(userId));
    }

    @GetMapping("/points/records")
    public ApiResponse<PointsRecordsResponse> pointsRecords(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(userService.pointsRecords(userId, page, pageSize));
    }
}
