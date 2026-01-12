package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.coupon.AdminCouponAddRequest;
import com.lingnan.fruitshop.dto.admin.coupon.AdminCouponUpdateRequest;
import com.lingnan.fruitshop.dto.admin.coupon.vo.AdminCouponDetailResponse;
import com.lingnan.fruitshop.dto.admin.coupon.vo.AdminCouponListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.service.AdminCouponService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupon")
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    public AdminCouponController(AdminCouponService adminCouponService) {
        this.adminCouponService = adminCouponService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<AdminCouponListItemResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                                       @RequestParam(required = false) Integer type,
                                                                       @RequestParam(required = false) Integer status) {
        return ApiResponse.success(adminCouponService.list(page, pageSize, type, status));
    }

    @GetMapping("/detail/{couponId}")
    public ApiResponse<AdminCouponDetailResponse> detail(@PathVariable long couponId) {
        return ApiResponse.success(adminCouponService.detail(couponId));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody AdminCouponAddRequest req) {
        adminCouponService.add(req);
        return ApiResponse.success(null);
    }

    @PutMapping("/update/{couponId}")
    public ApiResponse<Void> update(@PathVariable long couponId, @Valid @RequestBody AdminCouponUpdateRequest req) {
        adminCouponService.update(couponId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{couponId}")
    public ApiResponse<Void> delete(@PathVariable long couponId) {
        adminCouponService.delete(couponId);
        return ApiResponse.success(null);
    }
}
