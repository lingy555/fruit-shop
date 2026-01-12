package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderDetailResponse;
import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderListResponse;
import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderStatisticsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AdminOrderService {
    AdminOrderListResponse list(int page, int pageSize, String orderNo, Integer status, Long shopId, Long userId, String keyword, String startTime, String endTime);
    AdminOrderDetailResponse detail(long orderId);
    AdminOrderStatisticsResponse statistics(String dateType, String startDate, String endDate);
}
