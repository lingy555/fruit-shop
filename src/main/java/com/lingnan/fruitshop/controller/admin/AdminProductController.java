package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.product.AdminProductAuditRequest;
import com.lingnan.fruitshop.dto.admin.product.AdminProductForceOfflineRequest;
import com.lingnan.fruitshop.dto.admin.product.vo.AdminProductDetailResponse;
import com.lingnan.fruitshop.dto.admin.product.vo.AdminProductListResponse;
import com.lingnan.fruitshop.service.AdminProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping("/list")
    public ApiResponse<AdminProductListResponse> list(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) Long shopId,
                                                      @RequestParam(required = false) Integer status,
                                                      @RequestParam(required = false) BigDecimal minPrice,
                                                      @RequestParam(required = false) BigDecimal maxPrice) {
        return ApiResponse.success(adminProductService.list(page, pageSize, keyword, categoryId, shopId, status, minPrice, maxPrice));
    }

    @GetMapping("/detail/{productId}")
    public ApiResponse<AdminProductDetailResponse> detail(@PathVariable long productId) {
        return ApiResponse.success(adminProductService.detail(productId));
    }

    @PostMapping("/audit")
    public ApiResponse<Void> audit(@Valid @RequestBody AdminProductAuditRequest req) {
        adminProductService.audit(req);
        return ApiResponse.success(null);
    }

    @PutMapping("/forceOffline")
    public ApiResponse<Void> forceOffline(@Valid @RequestBody AdminProductForceOfflineRequest req) {
        adminProductService.forceOffline(req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{productId}")
    public ApiResponse<Void> delete(@PathVariable long productId) {
        adminProductService.delete(productId);
        return ApiResponse.success(null);
    }
}
