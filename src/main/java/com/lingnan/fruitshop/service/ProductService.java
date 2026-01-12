package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.*;

import java.util.List;

public interface ProductService {
    List<CategoryTreeResponse> categories();
    
    PageResponse<ProductListItemResponse> list(int page, int pageSize,
                                             Long categoryId,
                                             Long shopId,
                                             String keyword,
                                             String sortBy,
                                             String sortOrder,
                                             Double minPrice,
                                             Double maxPrice,
                                             Boolean tag);
    
    ProductDetailResponse detail(Long productId);
    
    ProductCommentsResponse comments(Long productId, int page, int pageSize, Integer type);
    
    List<String> hotKeywords();
    
    List<String> searchHistory(long userId);
    
    void clearSearchHistory(long userId);
}
