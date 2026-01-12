package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.coupon.vo.CustomerCouponAvailableItemResponse;
import com.lingnan.fruitshop.dto.customer.coupon.vo.CustomerMyCouponItemResponse;
import java.time.LocalDateTime;

public interface CustomerCouponService {
    PageResponse<CustomerCouponAvailableItemResponse> available(long userId, int page, int pageSize);
    void receive(long userId, long couponId);
    PageResponse<CustomerMyCouponItemResponse> my(long userId, int page, int pageSize, Integer status);
}
