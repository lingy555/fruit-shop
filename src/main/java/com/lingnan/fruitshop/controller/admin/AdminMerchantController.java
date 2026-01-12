package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.merchant.AdminMerchantAuditRequest;
import com.lingnan.fruitshop.dto.admin.merchant.AdminMerchantUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantDetailResponse;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantListResponse;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantStatisticsResponse;
import com.lingnan.fruitshop.service.AdminMerchantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/merchant")
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    public AdminMerchantController(AdminMerchantService adminMerchantService) {
        this.adminMerchantService = adminMerchantService;
    }

    @GetMapping("/list")
    public ApiResponse<AdminMerchantListResponse> list(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) String province,
                                                       @RequestParam(required = false) String city,
                                                       @RequestParam(required = false) String startTime,
                                                       @RequestParam(required = false) String endTime) {
        return ApiResponse.success(adminMerchantService.list(page, pageSize, keyword, status, province, city, startTime, endTime));
    }

    @GetMapping("/detail/{merchantId}")
    public ApiResponse<AdminMerchantDetailResponse> detail(@PathVariable long merchantId) {
        return ApiResponse.success(adminMerchantService.detail(merchantId));
    }

    @PostMapping("/audit")
    public ApiResponse<Void> audit(@Valid @RequestBody AdminMerchantAuditRequest req) {
        adminMerchantService.audit(req);
        return ApiResponse.success(null);
    }

    @PutMapping("/updateStatus")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody AdminMerchantUpdateStatusRequest req) {
        adminMerchantService.updateStatus(req);
        return ApiResponse.success(null);
    }

    @GetMapping("/statistics/{merchantId}")
    public ApiResponse<AdminMerchantStatisticsResponse> statistics(@PathVariable long merchantId,
                                                                   @RequestParam(required = false) String dateType) {
        return ApiResponse.success(adminMerchantService.statistics(merchantId, dateType));
    }
}
