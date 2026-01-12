package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.ProductListItemResponse;
import com.lingnan.fruitshop.dto.customer.shop.FavoriteShopRequest;
import com.lingnan.fruitshop.dto.customer.shop.vo.MyFavoriteShopItemResponse;
import com.lingnan.fruitshop.dto.customer.shop.vo.ShopDetailResponse;
import com.lingnan.fruitshop.security.SecurityUtils;
import com.lingnan.fruitshop.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/shop")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/{shopId}")
    public ApiResponse<ShopDetailResponse> detail(@PathVariable long shopId) {
        return ApiResponse.success(shopService.detail(shopId, SecurityUtils.optionalCurrentUserId()));
    }

    @GetMapping("/{shopId}/products")
    public ApiResponse<PageResponse<ProductListItemResponse>> products(@PathVariable long shopId,
                                                                       @RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                                       @RequestParam(required = false) String sortBy,
                                                                       @RequestParam(defaultValue = "desc") String sortOrder) {
        return ApiResponse.success(shopService.shopProducts(shopId, page, pageSize, sortBy, sortOrder));
    }

    @PostMapping("/favorite")
    public ApiResponse<Void> favorite(@Valid @RequestBody FavoriteShopRequest req) {
        long userId = SecurityUtils.currentUserId();
        shopService.favorite(userId, req.getShopId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/favorite/{shopId}")
    public ApiResponse<Void> unfavorite(@PathVariable long shopId) {
        long userId = SecurityUtils.currentUserId();
        shopService.unfavorite(userId, shopId);
        return ApiResponse.success(null);
    }

    @GetMapping("/myFavorites")
    public ApiResponse<PageResponse<MyFavoriteShopItemResponse>> myFavorites(@RequestParam(defaultValue = "1") int page,
                                                                             @RequestParam(defaultValue = "10") int pageSize) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(shopService.myFavorites(userId, page, pageSize));
    }
}
