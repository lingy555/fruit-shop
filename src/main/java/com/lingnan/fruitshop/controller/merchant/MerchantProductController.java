package com.lingnan.fruitshop.controller.merchant;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.CategoryTreeResponse;
import com.lingnan.fruitshop.dto.merchant.product.*;
import com.lingnan.fruitshop.dto.merchant.product.vo.*;
import com.lingnan.fruitshop.security.MerchantSecurityUtils;
import com.lingnan.fruitshop.service.MerchantProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/product")
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    public MerchantProductController(MerchantProductService merchantProductService) {
        this.merchantProductService = merchantProductService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<MerchantProductListItemResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "20") int pageSize,
                                                                           @RequestParam(required = false) Long categoryId,
                                                                           @RequestParam(required = false) Integer status,
                                                                           @RequestParam(required = false) String keyword,
                                                                           @RequestParam(required = false) String sortBy,
                                                                           @RequestParam(required = false) String sortOrder) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantProductService.list(shopId, page, pageSize, categoryId, status, keyword, sortBy, sortOrder));
    }

    @GetMapping("/detail/{productId}")
    public ApiResponse<MerchantProductDetailResponse> detail(@PathVariable long productId) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantProductService.detail(shopId, productId));
    }

    @PostMapping("/add")
    public ApiResponse<MerchantProductAddResponse> add(@Valid @RequestBody MerchantProductAddRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        long productId = merchantProductService.add(shopId, req);
        return ApiResponse.success("商品添加成功，等待审核", new MerchantProductAddResponse(productId));
    }

    @PutMapping("/update/{productId}")
    public ApiResponse<Void> update(@PathVariable long productId, @Valid @RequestBody MerchantProductUpdateRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantProductService.update(shopId, productId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{productId}")
    public ApiResponse<Void> delete(@PathVariable long productId) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantProductService.delete(shopId, productId);
        return ApiResponse.success(null);
    }

    @PutMapping("/batchStatus")
    public ApiResponse<Void> batchStatus(@Valid @RequestBody MerchantProductBatchStatusRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantProductService.batchStatus(shopId, req);
        return ApiResponse.success(null);
    }

    @PutMapping("/updateStock")
    public ApiResponse<Void> updateStock(@Valid @RequestBody MerchantProductUpdateStockRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantProductService.updateStock(shopId, req);
        return ApiResponse.success(null);
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryTreeResponse>> categories() {
        return ApiResponse.success(merchantProductService.categories());
    }
}
