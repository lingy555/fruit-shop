package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.*;
import com.lingnan.fruitshop.security.SecurityUtils;
import com.lingnan.fruitshop.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryTreeResponse>> categories() {
        return ApiResponse.success(productService.categories());
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<ProductListItemResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "20") int pageSize,
                                                                   @RequestParam(required = false) Long categoryId,
                                                                   @RequestParam(required = false) Long shopId,
                                                                   @RequestParam(required = false) String keyword,
                                                                   @RequestParam(required = false) String sortBy,
                                                                   @RequestParam(defaultValue = "desc") String sortOrder,
                                                                   @RequestParam(required = false) Double minPrice,
                                                                   @RequestParam(required = false) Double maxPrice,
                                                                   @RequestParam(required = false) Boolean onlyStock) {
        return ApiResponse.success(productService.list(page, pageSize, categoryId, shopId, keyword, sortBy, sortOrder, minPrice, maxPrice, onlyStock));
    }

    @GetMapping("/detail/{productId}")
    public ApiResponse<ProductDetailResponse> detail(@PathVariable Long productId) {
        return ApiResponse.success(productService.detail(productId));
    }

    @GetMapping("/comments/{productId}")
    public ApiResponse<ProductCommentsResponse> comments(@PathVariable Long productId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                         @RequestParam(required = false) Integer type) {
        return ApiResponse.success(productService.comments(productId, page, pageSize, type));
    }

    @GetMapping("/hotKeywords")
    public ApiResponse<List<String>> hotKeywords() {
        return ApiResponse.success(productService.hotKeywords());
    }

    @GetMapping("/searchHistory")
    public ApiResponse<List<String>> searchHistory() {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(productService.searchHistory(userId));
    }

    @DeleteMapping("/searchHistory")
    public ApiResponse<Void> clearSearchHistory() {
        long userId = SecurityUtils.currentUserId();
        productService.clearSearchHistory(userId);
        return ApiResponse.success(null);
    }
}
