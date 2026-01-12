package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.ProductListItemResponse;
import com.lingnan.fruitshop.dto.customer.shop.vo.MyFavoriteShopItemResponse;
import com.lingnan.fruitshop.dto.customer.shop.vo.ShopDetailResponse;
import java.util.Optional;

public interface ShopService {
    ShopDetailResponse detail(long shopId, Optional<Long> optionalUserId);
    PageResponse<ProductListItemResponse> shopProducts(long shopId, int page, int pageSize, String sortBy, String sortOrder);
    void favorite(long userId, long shopId);
    void unfavorite(long userId, long shopId);
    PageResponse<MyFavoriteShopItemResponse> myFavorites(long userId, int page, int pageSize);
}
