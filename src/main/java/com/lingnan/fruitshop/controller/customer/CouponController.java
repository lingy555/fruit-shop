package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.coupon.vo.CustomerCouponAvailableItemResponse;
import com.lingnan.fruitshop.dto.customer.coupon.vo.CustomerMyCouponItemResponse;
import com.lingnan.fruitshop.security.SecurityUtils;
import com.lingnan.fruitshop.service.CustomerCouponService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/coupon")
public class CouponController {

    private final CustomerCouponService customerCouponService;

    public CouponController(CustomerCouponService customerCouponService) {
        this.customerCouponService = customerCouponService;
    }

    @GetMapping("/available")
    public ApiResponse<PageResponse<CustomerCouponAvailableItemResponse>> available(@RequestParam(defaultValue = "1") int page,
                                                                                   @RequestParam(defaultValue = "20") int pageSize) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(customerCouponService.available(userId, page, pageSize));
    }

    @PostMapping("/receive/{couponId}")
    public ApiResponse<Void> receive(@PathVariable long couponId) {
        long userId = SecurityUtils.currentUserId();
        customerCouponService.receive(userId, couponId);
        return ApiResponse.success(null);
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<CustomerMyCouponItemResponse>> my(@RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "20") int pageSize,
                                                                      @RequestParam(required = false) Integer status) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(customerCouponService.my(userId, page, pageSize, status));
    }
}
