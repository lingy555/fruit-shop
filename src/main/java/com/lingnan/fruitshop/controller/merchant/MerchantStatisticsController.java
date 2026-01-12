package com.lingnan.fruitshop.controller.merchant;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.merchant.statistics.vo.*;
import com.lingnan.fruitshop.security.MerchantSecurityUtils;
import com.lingnan.fruitshop.service.MerchantStatisticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/statistics")
public class MerchantStatisticsController {

    private final MerchantStatisticsService merchantStatisticsService;

    public MerchantStatisticsController(MerchantStatisticsService merchantStatisticsService) {
        this.merchantStatisticsService = merchantStatisticsService;
    }

    @GetMapping("/overview")
    public ApiResponse<MerchantOverviewResponse> overview() {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantStatisticsService.overview(shopId));
    }

    @GetMapping("/sales")
    public ApiResponse<MerchantSalesStatisticsResponse> sales(@RequestParam(required = false) String dateType,
                                                              @RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantStatisticsService.sales(shopId, dateType, startDate, endDate));
    }

    @GetMapping("/product")
    public ApiResponse<MerchantProductStatisticsResponse> product(@RequestParam(required = false) String sortBy,
                                                                  @RequestParam(required = false) Integer limit) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantStatisticsService.product(shopId, sortBy, limit));
    }

    @GetMapping("/customer")
    public ApiResponse<MerchantCustomerStatisticsResponse> customer() {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantStatisticsService.customer(shopId));
    }
}
