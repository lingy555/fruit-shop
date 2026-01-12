package com.lingnan.fruitshop.controller.merchant;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.merchant.shop.MerchantShopStatusRequest;
import com.lingnan.fruitshop.dto.merchant.shop.MerchantShopUpdateRequest;
import com.lingnan.fruitshop.dto.merchant.shop.vo.MerchantShopInfoResponse;
import com.lingnan.fruitshop.dto.merchant.shop.vo.MerchantShopStatisticsResponse;
import com.lingnan.fruitshop.security.MerchantSecurityUtils;
import com.lingnan.fruitshop.service.MerchantShopService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/shop")
public class MerchantShopController {

    private final MerchantShopService merchantShopService;

    public MerchantShopController(MerchantShopService merchantShopService) {
        this.merchantShopService = merchantShopService;
    }

    @GetMapping("/info")
    public ApiResponse<MerchantShopInfoResponse> info() {
        long merchantId = MerchantSecurityUtils.currentMerchantId();
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantShopService.info(merchantId, shopId));
    }

    @PutMapping("/update")
    public ApiResponse<Void> update(@RequestBody MerchantShopUpdateRequest req) {
        long merchantId = MerchantSecurityUtils.currentMerchantId();
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantShopService.update(merchantId, shopId, req);
        return ApiResponse.success(null);
    }

    @GetMapping("/statistics")
    public ApiResponse<MerchantShopStatisticsResponse> statistics(@RequestParam(required = false) String dateType) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantShopService.statistics(shopId, dateType));
    }

    @PutMapping("/status")
    public ApiResponse<Void> status(@Valid @RequestBody MerchantShopStatusRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantShopService.setStatus(shopId, req);
        return ApiResponse.success(null);
    }
}
