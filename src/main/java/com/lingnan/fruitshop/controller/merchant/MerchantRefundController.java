package com.lingnan.fruitshop.controller.merchant;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.merchant.refund.*;
import com.lingnan.fruitshop.dto.merchant.refund.vo.MerchantRefundDetailResponse;
import com.lingnan.fruitshop.dto.merchant.refund.vo.MerchantRefundListResponse;
import com.lingnan.fruitshop.security.MerchantSecurityUtils;
import com.lingnan.fruitshop.service.MerchantRefundService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/refund")
public class MerchantRefundController {

    private final MerchantRefundService merchantRefundService;

    public MerchantRefundController(MerchantRefundService merchantRefundService) {
        this.merchantRefundService = merchantRefundService;
    }

    @GetMapping("/list")
    public ApiResponse<MerchantRefundListResponse> list(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int pageSize,
                                                        @RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) String keyword) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantRefundService.list(shopId, page, pageSize, status, keyword));
    }

    @GetMapping("/detail/{refundId}")
    public ApiResponse<MerchantRefundDetailResponse> detail(@PathVariable long refundId) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantRefundService.detail(shopId, refundId));
    }

    @PostMapping("/agree")
    public ApiResponse<Void> agree(@Valid @RequestBody MerchantRefundAgreeRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantRefundService.agree(shopId, req);
        return ApiResponse.success(null);
    }

    @PostMapping("/reject")
    public ApiResponse<Void> reject(@Valid @RequestBody MerchantRefundRejectRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantRefundService.reject(shopId, req);
        return ApiResponse.success(null);
    }

    @PostMapping("/confirmReceive")
    public ApiResponse<Void> confirmReceive(@Valid @RequestBody MerchantRefundConfirmReceiveRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantRefundService.confirmReceive(shopId, req);
        return ApiResponse.success(null);
    }
}
