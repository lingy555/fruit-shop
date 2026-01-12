package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderDetailResponse;
import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderListResponse;
import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderStatisticsResponse;
import com.lingnan.fruitshop.service.AdminOrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping("/list")
    public ApiResponse<AdminOrderListResponse> list(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int pageSize,
                                                    @RequestParam(required = false) String orderNo,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) Long shopId,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String startTime,
                                                    @RequestParam(required = false) String endTime) {
        return ApiResponse.success(adminOrderService.list(page, pageSize, orderNo, status, shopId, userId, keyword, startTime, endTime));
    }

    @GetMapping("/detail/{orderId}")
    public ApiResponse<AdminOrderDetailResponse> detail(@PathVariable long orderId) {
        return ApiResponse.success(adminOrderService.detail(orderId));
    }

    @GetMapping("/statistics")
    public ApiResponse<AdminOrderStatisticsResponse> statistics(@RequestParam(required = false) String dateType,
                                                                @RequestParam(required = false) String startDate,
                                                                @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminOrderService.statistics(dateType, startDate, endDate));
    }
}
