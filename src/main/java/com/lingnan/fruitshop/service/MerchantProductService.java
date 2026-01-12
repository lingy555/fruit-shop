package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.CategoryTreeResponse;
import com.lingnan.fruitshop.dto.merchant.product.*;
import com.lingnan.fruitshop.dto.merchant.product.vo.*;

import java.util.List;

public interface MerchantProductService {
    PageResponse<MerchantProductListItemResponse> list(long shopId, int page, int pageSize, Long categoryId, Integer status, String keyword, String sortBy, String sortOrder);
    MerchantProductDetailResponse detail(long shopId, long productId);
    long add(long shopId, MerchantProductAddRequest req);
    void update(long shopId, long productId, MerchantProductUpdateRequest req);
    void delete(long shopId, long productId);
    void batchStatus(long shopId, MerchantProductBatchStatusRequest req);
    void updateStock(long shopId, MerchantProductUpdateStockRequest req);
    List<CategoryTreeResponse> categories();
}
