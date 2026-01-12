package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.product.AdminProductAuditRequest;
import com.lingnan.fruitshop.dto.admin.product.AdminProductForceOfflineRequest;
import com.lingnan.fruitshop.dto.admin.product.vo.AdminProductDetailResponse;
import com.lingnan.fruitshop.dto.admin.product.vo.AdminProductListResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AdminProductService {
    AdminProductListResponse list(int page, int pageSize, String keyword, Long categoryId, Long shopId, Integer status, BigDecimal minPrice, BigDecimal maxPrice);
    AdminProductDetailResponse detail(long productId);
    void audit(AdminProductAuditRequest req);
    void forceOffline(AdminProductForceOfflineRequest req);
    void delete(long productId);
}
