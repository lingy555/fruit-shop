package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.merchant.shop.MerchantShopStatusRequest;
import com.lingnan.fruitshop.dto.merchant.shop.MerchantShopUpdateRequest;
import com.lingnan.fruitshop.dto.merchant.shop.vo.MerchantShopInfoResponse;
import com.lingnan.fruitshop.dto.merchant.shop.vo.MerchantShopStatisticsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface MerchantShopService {
    MerchantShopInfoResponse info(long merchantId, long shopId);
    void update(long merchantId, long shopId, MerchantShopUpdateRequest req);
    void setStatus(long shopId, MerchantShopStatusRequest req);
    MerchantShopStatisticsResponse statistics(long shopId, String dateType);
}
