package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminChartDataItemResponse;
import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminPlatformDataResponse;
import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminTodayDataResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminDashboardService {
    AdminTodayDataResponse todayData(String date);
    AdminPlatformDataResponse platformData();
    List<AdminChartDataItemResponse> chartData(String type, String startDate, String endDate);
}
