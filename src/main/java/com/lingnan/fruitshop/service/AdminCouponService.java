package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.coupon.AdminCouponAddRequest;
import com.lingnan.fruitshop.dto.admin.coupon.AdminCouponUpdateRequest;
import com.lingnan.fruitshop.dto.admin.coupon.vo.AdminCouponDetailResponse;
import com.lingnan.fruitshop.dto.admin.coupon.vo.AdminCouponListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminCouponService {
    PageResponse<AdminCouponListItemResponse> list(int page, int pageSize, Integer type, Integer status);
    AdminCouponDetailResponse detail(long couponId);
    void add(AdminCouponAddRequest req);
    void update(long couponId, AdminCouponUpdateRequest req);
    void delete(long couponId);
}
