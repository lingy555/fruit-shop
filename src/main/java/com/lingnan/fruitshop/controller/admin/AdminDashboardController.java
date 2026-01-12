package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminChartDataItemResponse;
import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminPlatformDataResponse;
import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminTodayDataResponse;
import com.lingnan.fruitshop.service.AdminDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/todayData")
    public ApiResponse<AdminTodayDataResponse> todayData(@RequestParam(required = false) String date) {
        return ApiResponse.success(adminDashboardService.todayData(date));
    }

    @GetMapping("/platformData")
    public ApiResponse<AdminPlatformDataResponse> platformData() {
        return ApiResponse.success(adminDashboardService.platformData());
    }

    @GetMapping("/chartData")
    public ApiResponse<List<AdminChartDataItemResponse>> chartData(@RequestParam(required = false) String type,
                                                                   @RequestParam(required = false) String startDate,
                                                                   @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminDashboardService.chartData(type, startDate, endDate));
    }
}
