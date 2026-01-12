package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.user.AdminUserAdjustBalanceRequest;
import com.lingnan.fruitshop.dto.admin.user.AdminUserAdjustPointsRequest;
import com.lingnan.fruitshop.dto.admin.user.AdminUserUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserDetailResponse;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserListItemResponse;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserOrderItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<AdminUserListItemResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "20") int pageSize,
                                                                     @RequestParam(required = false) String keyword,
                                                                     @RequestParam(required = false) Integer level,
                                                                     @RequestParam(required = false) Integer status,
                                                                     @RequestParam(required = false) String startTime,
                                                                     @RequestParam(required = false) String endTime) {
        return ApiResponse.success(adminUserService.list(page, pageSize, keyword, level, status, startTime, endTime));
    }

    @GetMapping("/detail/{userId}")
    public ApiResponse<AdminUserDetailResponse> detail(@PathVariable long userId) {
        return ApiResponse.success(adminUserService.detail(userId));
    }

    @PutMapping("/updateStatus")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody AdminUserUpdateStatusRequest req) {
        adminUserService.updateStatus(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/adjustBalance")
    public ApiResponse<Void> adjustBalance(@Valid @RequestBody AdminUserAdjustBalanceRequest req) {
        adminUserService.adjustBalance(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/adjustPoints")
    public ApiResponse<Void> adjustPoints(@Valid @RequestBody AdminUserAdjustPointsRequest req) {
        adminUserService.adjustPoints(req);
        return ApiResponse.success(null);
    }

    @GetMapping("/orders/{userId}")
    public ApiResponse<PageResponse<AdminUserOrderItemResponse>> orders(@PathVariable long userId,
                                                                        @RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(adminUserService.userOrders(userId, page, pageSize));
    }
}
