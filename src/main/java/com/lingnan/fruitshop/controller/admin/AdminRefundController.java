package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.refund.AdminRefundArbitrateRequest;
import com.lingnan.fruitshop.dto.admin.refund.vo.AdminRefundDetailResponse;
import com.lingnan.fruitshop.dto.admin.refund.vo.AdminRefundListResponse;
import com.lingnan.fruitshop.service.AdminRefundService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/refund")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;

    public AdminRefundController(AdminRefundService adminRefundService) {
        this.adminRefundService = adminRefundService;
    }

    @GetMapping("/list")
    public ApiResponse<AdminRefundListResponse> list(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize,
                                                     @RequestParam(required = false) String refundNo,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) Long shopId,
                                                     @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminRefundService.list(page, pageSize, refundNo, status, shopId, keyword));
    }

    @GetMapping("/detail/{refundId}")
    public ApiResponse<AdminRefundDetailResponse> detail(@PathVariable long refundId) {
        return ApiResponse.success(adminRefundService.detail(refundId));
    }

    @PostMapping("/arbitrate")
    public ApiResponse<Void> arbitrate(@Valid @RequestBody AdminRefundArbitrateRequest req) {
        adminRefundService.arbitrate(req);
        return ApiResponse.success(null);
    }
}
