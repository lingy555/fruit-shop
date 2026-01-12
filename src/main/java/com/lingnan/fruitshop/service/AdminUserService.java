package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.user.AdminUserAdjustBalanceRequest;
import com.lingnan.fruitshop.dto.admin.user.AdminUserAdjustPointsRequest;
import com.lingnan.fruitshop.dto.admin.user.AdminUserUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserDetailResponse;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserListItemResponse;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserOrderItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AdminUserService {
    PageResponse<AdminUserListItemResponse> list(int page, int pageSize, String keyword, Integer level, Integer status, String startTime, String endTime);
    AdminUserDetailResponse detail(long userId);
    void updateStatus(AdminUserUpdateStatusRequest req);
    void adjustBalance(AdminUserAdjustBalanceRequest req);
    void adjustPoints(AdminUserAdjustPointsRequest req);
    PageResponse<AdminUserOrderItemResponse> userOrders(long userId, int page, int pageSize);
}
