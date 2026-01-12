package com.lingnan.fruitshop.controller.merchant;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.merchant.finance.vo.MerchantFinanceBillItemResponse;
import com.lingnan.fruitshop.dto.merchant.finance.vo.MerchantFinanceOverviewResponse;
import com.lingnan.fruitshop.security.MerchantSecurityUtils;
import com.lingnan.fruitshop.service.MerchantFinanceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/finance")
public class MerchantFinanceController {

    private final MerchantFinanceService merchantFinanceService;

    public MerchantFinanceController(MerchantFinanceService merchantFinanceService) {
        this.merchantFinanceService = merchantFinanceService;
    }

    @GetMapping("/overview")
    public ApiResponse<MerchantFinanceOverviewResponse> overview() {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantFinanceService.overview(shopId));
    }

    @GetMapping("/bills")
    public ApiResponse<PageResponse<MerchantFinanceBillItemResponse>> bills(@RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                                            @RequestParam(required = false) Integer type,
                                                                            @RequestParam(required = false) String startDate,
                                                                            @RequestParam(required = false) String endDate) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantFinanceService.bills(shopId, page, pageSize, type, startDate, endDate));
    }
}
