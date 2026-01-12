package com.lingnan.fruitshop.controller.merchant;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.merchant.order.*;
import com.lingnan.fruitshop.dto.merchant.order.vo.MerchantOrderDetailResponse;
import com.lingnan.fruitshop.dto.merchant.order.vo.MerchantOrderListResponse;
import com.lingnan.fruitshop.security.MerchantSecurityUtils;
import com.lingnan.fruitshop.service.MerchantOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/order")
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    public MerchantOrderController(MerchantOrderService merchantOrderService) {
        this.merchantOrderService = merchantOrderService;
    }

    @GetMapping("/list")
    public ApiResponse<MerchantOrderListResponse> list(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String startTime,
                                                       @RequestParam(required = false) String endTime) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantOrderService.list(shopId, page, pageSize, status, keyword, startTime, endTime));
    }

    @GetMapping("/detail/{orderId}")
    public ApiResponse<MerchantOrderDetailResponse> detail(@PathVariable long orderId) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantOrderService.detail(shopId, orderId));
    }

    @PostMapping("/deliver")
    public ApiResponse<Void> deliver(@Valid @RequestBody MerchantOrderDeliverRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantOrderService.deliver(shopId, req);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchDeliver")
    public ApiResponse<Void> batchDeliver(@Valid @RequestBody MerchantOrderBatchDeliverRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantOrderService.batchDeliver(shopId, req);
        return ApiResponse.success(null);
    }

    @PutMapping("/updateShippingFee")
    public ApiResponse<Void> updateShippingFee(@Valid @RequestBody MerchantOrderUpdateShippingFeeRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantOrderService.updateShippingFee(shopId, req);
        return ApiResponse.success(null);
    }

    @PutMapping("/updateRemark")
    public ApiResponse<Void> updateRemark(@Valid @RequestBody MerchantOrderUpdateRemarkRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantOrderService.updateRemark(shopId, req);
        return ApiResponse.success(null);
    }

    @PostMapping("/export")
    public ApiResponse<Map<String, Object>> export(@RequestBody MerchantOrderExportRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantOrderService.export(shopId, req));
    }
}
